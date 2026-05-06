package com.skybooker.seat;

import com.skybooker.seat.dto.SeatRequest;
import com.skybooker.seat.dto.SeatResponse;
import com.skybooker.seat.entity.Seat;
import com.skybooker.seat.repository.SeatRepository;
import com.skybooker.seat.service.SeatServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeatServiceImpl Tests")
class SeatServiceImplTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatServiceImpl seatService;

    // ─── Helpers

    private Seat buildSeat(Long id, Long flightId, String seatNum, String status) {
        Seat s = new Seat();
        s.setId(id);
        s.setFlightId(flightId);
        s.setSeatNumber(seatNum);
        s.setSeatClass("ECONOMY");
        s.setRow(12);
        s.setColumn("A");
        s.setWindow(true);
        s.setAisle(false);
        s.setHasExtraLegroom(false);
        s.setStatus(status);
        s.setPriceMultiplier(1.0);
        return s;
    }

    private SeatRequest buildRequest(Long flightId, String seatNum, String seatClass) {
        SeatRequest req = new SeatRequest();
        req.setFlightId(flightId);
        req.setSeatNumber(seatNum);
        req.setSeatClass(seatClass);
        req.setRow(12);
        req.setColumn("A");
        req.setWindow(true);
        req.setAisle(false);
        req.setHasExtraLegroom(false);
        req.setPriceMultiplier(1.0);
        return req;
    }


    //  ADD SEAT TESTS


    @Test
    @DisplayName("addSeat - valid new seat is saved successfully")
    void addSeat_Success() {
        SeatRequest req = buildRequest(1L, "12A", "ECONOMY");
        Seat saved = buildSeat(1L, 1L, "12A", "AVAILABLE");

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A")).thenReturn(Optional.empty());
        when(seatRepository.save(any(Seat.class))).thenReturn(saved);

        SeatResponse response = seatService.addSeat(req);

        assertThat(response).isNotNull();
        assertThat(response.getSeatNumber()).isEqualTo("12A");
        assertThat(response.getStatus()).isEqualTo("AVAILABLE");
        assertThat(response.getMessage()).isEqualTo("Seat added successfully");
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    @DisplayName("addSeat - duplicate seat for same flight throws RuntimeException")
    void addSeat_DuplicateSeat_ThrowsException() {
        SeatRequest req = buildRequest(1L, "12A", "ECONOMY");
        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A"))
                .thenReturn(Optional.of(buildSeat(1L, 1L, "12A", "AVAILABLE")));

        assertThatThrownBy(() -> seatService.addSeat(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("12A")
                .hasMessageContaining("already exists");

        verify(seatRepository, never()).save(any());
    }

    @Test
    @DisplayName("addSeat - seat is initialized with AVAILABLE status")
    void addSeat_InitialStatusIsAvailable() {
        SeatRequest req = buildRequest(1L, "5B", "BUSINESS");
        ArgumentCaptor<Seat> captor = ArgumentCaptor.forClass(Seat.class);
        Seat saved = buildSeat(1L, 1L, "5B", "AVAILABLE");

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "5B")).thenReturn(Optional.empty());
        when(seatRepository.save(captor.capture())).thenReturn(saved);

        seatService.addSeat(req);

        assertThat(captor.getValue().getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    @DisplayName("addSeat - same seat number on different flight is allowed")
    void addSeat_SameSeatDifferentFlight_Allowed() {
        SeatRequest req = buildRequest(2L, "12A", "ECONOMY"); // flight 2, not flight 1
        Seat saved = buildSeat(2L, 2L, "12A", "AVAILABLE");

        when(seatRepository.findByFlightIdAndSeatNumber(2L, "12A")).thenReturn(Optional.empty());
        when(seatRepository.save(any())).thenReturn(saved);

        assertThatNoException().isThrownBy(() -> seatService.addSeat(req));
    }


    //  GET SEAT TESTS


    @Test
    @DisplayName("getSeatsByFlight - returns all seats for a given flight")
    void getSeatsByFlight_ReturnsList() {
        Seat s1 = buildSeat(1L, 1L, "1A", "AVAILABLE");
        Seat s2 = buildSeat(2L, 1L, "1B", "HELD");
        when(seatRepository.findByFlightId(1L)).thenReturn(Arrays.asList(s1, s2));

        List<SeatResponse> result = seatService.getSeatsByFlight(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SeatResponse::getSeatNumber)
                .containsExactlyInAnyOrder("1A", "1B");
    }

    @Test
    @DisplayName("getSeatsByFlight - returns empty list when flight has no seats")
    void getSeatsByFlight_EmptyList() {
        when(seatRepository.findByFlightId(999L)).thenReturn(Collections.emptyList());

        assertThat(seatService.getSeatsByFlight(999L)).isEmpty();
    }

    @Test
    @DisplayName("getAvailableSeats - returns only AVAILABLE seats for a flight")
    void getAvailableSeats_ReturnsAvailableOnly() {
        Seat s1 = buildSeat(1L, 1L, "12A", "AVAILABLE");
        Seat s2 = buildSeat(2L, 1L, "12B", "AVAILABLE");
        when(seatRepository.findByFlightIdAndStatus(1L, "AVAILABLE")).thenReturn(Arrays.asList(s1, s2));

        List<SeatResponse> result = seatService.getAvailableSeats(1L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.getStatus().equals("AVAILABLE"));
    }

    @Test
    @DisplayName("getSeatsByClass - returns seats of correct class for flight")
    void getSeatsByClass_ReturnsCorrectClass() {
        Seat s = buildSeat(1L, 1L, "1A", "AVAILABLE");
        s.setSeatClass("FIRST");
        when(seatRepository.findByFlightIdAndSeatClass(1L, "FIRST"))
                .thenReturn(Collections.singletonList(s));

        List<SeatResponse> result = seatService.getSeatsByClass(1L, "FIRST");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeatClass()).isEqualTo("FIRST");
    }

    @Test
    @DisplayName("getAvailableCount - returns correct count of available seats")
    void getAvailableCount_ReturnsCount() {
        when(seatRepository.countByFlightIdAndStatus(1L, "AVAILABLE")).thenReturn(45);

        int count = seatService.getAvailableCount(1L);

        assertThat(count).isEqualTo(45);
    }


    //  HOLD SEAT TESTS


    @Test
    @DisplayName("holdSeat - AVAILABLE seat is put on hold successfully")
    void holdSeat_AvailableSeat_Success() {
        Seat seat = buildSeat(1L, 1L, "12A", "AVAILABLE");
        Seat held = buildSeat(1L, 1L, "12A", "HELD");
        held.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A")).thenReturn(Optional.of(seat));
        when(seatRepository.save(any())).thenReturn(held);

        SeatResponse response = seatService.holdSeat(1L, "12A");

        assertThat(response.getStatus()).isEqualTo("HELD");
        assertThat(response.getMessage()).contains("15 minutes");
    }

    @Test
    @DisplayName("holdSeat - HELD seat cannot be held again")
    void holdSeat_AlreadyHeld_ThrowsException() {
        Seat seat = buildSeat(1L, 1L, "12A", "HELD");

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A")).thenReturn(Optional.of(seat));

        assertThatThrownBy(() -> seatService.holdSeat(1L, "12A"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("holdSeat - CONFIRMED seat cannot be held")
    void holdSeat_ConfirmedSeat_ThrowsException() {
        Seat seat = buildSeat(1L, 1L, "12A", "CONFIRMED");

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A")).thenReturn(Optional.of(seat));

        assertThatThrownBy(() -> seatService.holdSeat(1L, "12A"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("holdSeat - non-existent seat throws RuntimeException")
    void holdSeat_NotFound_ThrowsException() {
        when(seatRepository.findByFlightIdAndSeatNumber(1L, "99Z")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.holdSeat(1L, "99Z"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Seat not found");
    }

    @Test
    @DisplayName("holdSeat - holdExpiresAt is set to 15 minutes from now")
    void holdSeat_HoldExpiresIn15Minutes() {
        Seat seat = buildSeat(1L, 1L, "12A", "AVAILABLE");
        ArgumentCaptor<Seat> captor = ArgumentCaptor.forClass(Seat.class);
        Seat heldReturn = buildSeat(1L, 1L, "12A", "HELD");
        heldReturn.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A")).thenReturn(Optional.of(seat));
        when(seatRepository.save(captor.capture())).thenReturn(heldReturn);

        seatService.holdSeat(1L, "12A");

        LocalDateTime expiry = captor.getValue().getHoldExpiresAt();
        assertThat(expiry).isAfter(LocalDateTime.now().plusMinutes(14));
        assertThat(expiry).isBefore(LocalDateTime.now().plusMinutes(16));
    }


    //  CONFIRM SEAT TESTS


    @Test
    @DisplayName("confirmSeat - HELD seat with valid hold is confirmed successfully")
    void confirmSeat_HeldSeat_Success() {
        Seat seat = buildSeat(1L, 1L, "12A", "HELD");
        seat.setHoldExpiresAt(LocalDateTime.now().plusMinutes(10)); // still valid
        Seat confirmed = buildSeat(1L, 1L, "12A", "CONFIRMED");

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A")).thenReturn(Optional.of(seat));
        when(seatRepository.save(any())).thenReturn(confirmed);

        SeatResponse response = seatService.confirmSeat(1L, "12A");

        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getMessage()).isEqualTo("Seat confirmed successfully");
    }

    @Test
    @DisplayName("confirmSeat - AVAILABLE seat (not held) cannot be confirmed")
    void confirmSeat_NotHeld_ThrowsException() {
        Seat seat = buildSeat(1L, 1L, "12A", "AVAILABLE");

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A")).thenReturn(Optional.of(seat));

        assertThatThrownBy(() -> seatService.confirmSeat(1L, "12A"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not held");
    }

    @Test
    @DisplayName("confirmSeat - expired hold reverts to AVAILABLE and throws exception")
    void confirmSeat_ExpiredHold_RevertToAvailableAndThrow() {
        Seat seat = buildSeat(1L, 1L, "12A", "HELD");
        seat.setHoldExpiresAt(LocalDateTime.now().minusMinutes(5)); // expired

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A")).thenReturn(Optional.of(seat));
        when(seatRepository.save(any())).thenReturn(seat);

        assertThatThrownBy(() -> seatService.confirmSeat(1L, "12A"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("hold expired");
    }

    @Test
    @DisplayName("confirmSeat - non-existent seat throws RuntimeException")
    void confirmSeat_NotFound() {
        when(seatRepository.findByFlightIdAndSeatNumber(1L, "99Z")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.confirmSeat(1L, "99Z"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Seat not found");
    }


    //  RELEASE SEAT TESTS


    @Test
    @DisplayName("releaseSeat - HELD seat is released to AVAILABLE")
    void releaseSeat_HeldSeat_Success() {
        Seat seat = buildSeat(1L, 1L, "12A", "HELD");
        Seat released = buildSeat(1L, 1L, "12A", "AVAILABLE");

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A")).thenReturn(Optional.of(seat));
        when(seatRepository.save(any())).thenReturn(released);

        SeatResponse response = seatService.releaseSeat(1L, "12A");

        assertThat(response.getStatus()).isEqualTo("AVAILABLE");
        assertThat(response.getMessage()).isEqualTo("Seat released successfully");
    }

    @Test
    @DisplayName("releaseSeat - CONFIRMED seat cannot be released directly")
    void releaseSeat_ConfirmedSeat_ThrowsException() {
        Seat seat = buildSeat(1L, 1L, "12A", "CONFIRMED");

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A")).thenReturn(Optional.of(seat));

        assertThatThrownBy(() -> seatService.releaseSeat(1L, "12A"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Confirmed seat cannot be released");
    }

    @Test
    @DisplayName("releaseSeat - non-existent seat throws RuntimeException")
    void releaseSeat_NotFound() {
        when(seatRepository.findByFlightIdAndSeatNumber(1L, "99Z")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.releaseSeat(1L, "99Z"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Seat not found");
    }

    @Test
    @DisplayName("releaseSeat - holdExpiresAt is cleared on release")
    void releaseSeat_HoldExpiryCleared() {
        Seat seat = buildSeat(1L, 1L, "12A", "HELD");
        seat.setHoldExpiresAt(LocalDateTime.now().plusMinutes(5));
        ArgumentCaptor<Seat> captor = ArgumentCaptor.forClass(Seat.class);
        Seat released = buildSeat(1L, 1L, "12A", "AVAILABLE");

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A")).thenReturn(Optional.of(seat));
        when(seatRepository.save(captor.capture())).thenReturn(released);

        seatService.releaseSeat(1L, "12A");

        assertThat(captor.getValue().getHoldExpiresAt()).isNull();
    }


    //  RELEASE EXPIRED HOLDS TEST


    @Test
    @DisplayName("releaseExpiredHolds - releases all seats with expired holds")
    void releaseExpiredHolds_ReleasesExpired() {
        Seat expired1 = buildSeat(1L, 1L, "12A", "HELD");
        expired1.setHoldExpiresAt(LocalDateTime.now().minusMinutes(5));
        Seat expired2 = buildSeat(2L, 1L, "12B", "HELD");
        expired2.setHoldExpiresAt(LocalDateTime.now().minusMinutes(10));

        when(seatRepository.findByStatusAndHoldExpiresAtBefore(eq("HELD"), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(expired1, expired2));

        seatService.releaseExpiredHolds();

        verify(seatRepository, times(2)).save(any(Seat.class));
        assertThat(expired1.getStatus()).isEqualTo("AVAILABLE");
        assertThat(expired2.getStatus()).isEqualTo("AVAILABLE");
        assertThat(expired1.getHoldExpiresAt()).isNull();
        assertThat(expired2.getHoldExpiresAt()).isNull();
    }

    @Test
    @DisplayName("releaseExpiredHolds - no expired seats means no saves")
    void releaseExpiredHolds_NoneExpired_NoSaves() {
        when(seatRepository.findByStatusAndHoldExpiresAtBefore(eq("HELD"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        seatService.releaseExpiredHolds();

        verify(seatRepository, never()).save(any());
    }
}
