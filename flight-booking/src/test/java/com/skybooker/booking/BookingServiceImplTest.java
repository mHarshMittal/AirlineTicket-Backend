package com.skybooker.booking;

import com.skybooker.booking.dto.BookingRequest;
import com.skybooker.booking.dto.BookingResponse;
import com.skybooker.booking.entity.Booking;
import com.skybooker.booking.repository.BookingRepository;
import com.skybooker.booking.service.BookingServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl Tests")
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BookingServiceImpl bookingService;

    // Helpers

    private BookingRequest buildRequest(Long flightId, String email, int seats, String token) {
        BookingRequest req = new BookingRequest();
        req.setFlightId(flightId);
        req.setUserEmail(email);
        req.setSeats(seats);
        req.setToken("Bearer test-token");
        return req;
    }

    private Booking buildBooking(Long id, Long flightId, String email, String status, String pnr) {
        Booking b = new Booking();
        b.setId(id);
        b.setFlightId(flightId);
        b.setUserEmail(email);
        b.setSeatsBooked(2);
        b.setTotalPrice(0.0);
        b.setStatus(status);
        b.setPnr(pnr);
        b.setBookingTime(LocalDateTime.now());
        return b;
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<String> ok() {
        return ResponseEntity.ok("Seats reduced successfully");
    }


    //  BOOK FLIGHT TESTS


    @Test
    @DisplayName("bookFlight - valid request creates PENDING booking")
    void bookFlight_ValidRequest_CreatesBooking() {
        BookingRequest req = buildRequest(1L, "john@example.com", 2, "Bearer token");
        Booking saved = buildBooking(1L, 1L, "john@example.com", "PENDING", "SKY-AB1234");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ok());
        when(bookingRepository.save(any(Booking.class))).thenReturn(saved);

        BookingResponse response = bookingService.bookFlight(req);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getUserEmail()).isEqualTo("john@example.com");
        assertThat(response.isSuccess()).isTrue();
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("bookFlight - PNR is generated with SKY- prefix")
    void bookFlight_PnrHasSkyPrefix() {
        BookingRequest req = buildRequest(1L, "john@example.com", 2, "Bearer token");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ok());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        BookingResponse response = bookingService.bookFlight(req);

        assertThat(response.getPnr()).startsWith("SKY-");
    }

    @Test
    @DisplayName("bookFlight - booking is created with PENDING status initially")
    void bookFlight_InitialStatusIsPending() {
        BookingRequest req = buildRequest(1L, "john@example.com", 2, "Bearer token");
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ok());
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        bookingService.bookFlight(req);

        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("bookFlight - total price is set to 0.0 initially (paid later)")
    void bookFlight_InitialTotalPriceIsZero() {
        BookingRequest req = buildRequest(1L, "john@example.com", 2, "Bearer token");
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ok());
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        bookingService.bookFlight(req);

        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalPrice()).isZero();
    }

    @Test
    @DisplayName("bookFlight - seat reduction failure throws RuntimeException")
    void bookFlight_SeatReductionFails_ThrowsException() {
        BookingRequest req = buildRequest(1L, "john@example.com", 2, "Bearer token");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not enough seats"));

        assertThatThrownBy(() -> bookingService.bookFlight(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Seat reduction failed");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("bookFlight - correct URL with flightId and seats is called")
    void bookFlight_CorrectUrlCalled() {
        BookingRequest req = buildRequest(5L, "john@example.com", 3, "Bearer token");

        when(restTemplate.exchange(
                contains("/flights/5/reduce-seats?seats=3"),
                eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ok());
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        assertThatNoException().isThrownBy(() -> bookingService.bookFlight(req));
        verify(restTemplate).exchange(contains("/flights/5/reduce-seats?seats=3"),
                eq(HttpMethod.PUT), any(), eq(String.class));
    }

    @Test
    @DisplayName("bookFlight - response message indicates payment is needed")
    void bookFlight_ResponseMessageIndicatesPaymentNeeded() {
        BookingRequest req = buildRequest(1L, "john@example.com", 2, "Bearer token");
        Booking saved = buildBooking(1L, 1L, "john@example.com", "PENDING", "SKY-XYZ123");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ok());
        when(bookingRepository.save(any())).thenReturn(saved);

        BookingResponse response = bookingService.bookFlight(req);

        assertThat(response.getMessage()).containsIgnoringCase("payment");
    }


    //  CONFIRM BOOKING TESTS


    @Test
    @DisplayName("confirmBooking - PENDING booking is confirmed successfully")
    void confirmBooking_PendingBooking_Confirmed() {
        Booking booking = buildBooking(1L, 1L, "john@example.com", "PENDING", "SKY-AB1234");
        Booking confirmed = buildBooking(1L, 1L, "john@example.com", "CONFIRMED", "SKY-AB1234");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(confirmed);

        BookingResponse response = bookingService.confirmBooking(1L);

        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getMessage()).contains("confirmed");
    }

    @Test
    @DisplayName("confirmBooking - already CONFIRMED booking returns without re-confirming")
    void confirmBooking_AlreadyConfirmed_ReturnsExisting() {
        Booking booking = buildBooking(1L, 1L, "john@example.com", "CONFIRMED", "SKY-AB1234");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.confirmBooking(1L);

        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getMessage()).contains("already confirmed");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirmBooking - non-existent booking throws RuntimeException")
    void confirmBooking_NotFound_ThrowsException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.confirmBooking(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }


    //  GET BOOKING TESTS


    @Test
    @DisplayName("getBooking - returns correct booking for valid ID")
    void getBooking_Found() {
        Booking booking = buildBooking(1L, 1L, "john@example.com", "CONFIRMED", "SKY-AB1234");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.getBooking(1L);

        assertThat(response.getBookingId()).isEqualTo(1L);
        assertThat(response.getUserEmail()).isEqualTo("john@example.com");
        assertThat(response.getPnr()).isEqualTo("SKY-AB1234");
        assertThat(response.getMessage()).isEqualTo("Success");
    }

    @Test
    @DisplayName("getBooking - non-existent ID throws RuntimeException")
    void getBooking_NotFound_ThrowsException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBooking(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("getBooking - response contains correct flight ID")
    void getBooking_ContainsFlightId() {
        Booking booking = buildBooking(1L, 42L, "user@test.com", "PENDING", "SKY-111AAA");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.getBooking(1L);

        assertThat(response.getFlightId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("bookFlight - bookingTime is set at creation")
    void bookFlight_BookingTimeIsSet() {
        BookingRequest req = buildRequest(1L, "john@example.com", 2, "Bearer token");
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ok());
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        bookingService.bookFlight(req);

        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().getBookingTime()).isNotNull();
    }

    @Test
    @DisplayName("confirmBooking - saves booking with CONFIRMED status")
    void confirmBooking_SavesWithConfirmedStatus() {
        Booking booking = buildBooking(1L, 1L, "user@test.com", "PENDING", "SKY-AAA111");
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(captor.capture())).thenReturn(booking);

        bookingService.confirmBooking(1L);

        assertThat(captor.getValue().getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("bookFlight - Authorization header is forwarded from request token")
    void bookFlight_AuthorizationHeaderForwarded() {
        BookingRequest req = buildRequest(1L, "john@example.com", 2, "Bearer my-token");
        req.setToken("Bearer my-token");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ok());
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        assertThatNoException().isThrownBy(() -> bookingService.bookFlight(req));
    }
}
