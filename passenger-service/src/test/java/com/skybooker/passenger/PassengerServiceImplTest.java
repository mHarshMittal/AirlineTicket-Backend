package com.skybooker.passenger;

import com.skybooker.passenger.dto.PassengerRequest;
import com.skybooker.passenger.dto.PassengerResponse;
import com.skybooker.passenger.dto.SeatAssignRequest;
import com.skybooker.passenger.entity.PassengerInfo;
import com.skybooker.passenger.repository.PassengerRepository;
import com.skybooker.passenger.service.PassengerServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PassengerServiceImpl Tests")
class PassengerServiceImplTest {

    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private PassengerServiceImpl passengerService;

    // Helpers

    private PassengerRequest buildRequest(String firstName, String lastName,
                                          String passengerType, LocalDate dob, LocalDate passportExpiry) {
        PassengerRequest req = new PassengerRequest();
        req.setBookingId("BOOK-001");
        req.setTitle("Mr");
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setDateOfBirth(dob);
        req.setGender("MALE");
        req.setPassportNumber("A1234567");
        req.setNationality("Indian");
        req.setPassportExpiry(passportExpiry);
        req.setPassengerType(passengerType);
        return req;
    }

    private PassengerInfo buildPassenger(Long id, String firstName, String lastName,
                                         String type, String ticketNum) {
        PassengerInfo p = new PassengerInfo();
        p.setPassengerId(id);
        p.setBookingId("BOOK-001");
        p.setTitle("Mr");
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setDateOfBirth(LocalDate.of(1990, 1, 15));
        p.setGender("MALE");
        p.setPassportNumber("A1234567");
        p.setNationality("Indian");
        p.setPassportExpiry(LocalDate.now().plusYears(5));
        p.setPassengerType(type);
        p.setTicketNumber(ticketNum);
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }

    //  ADD PASSENGER TESTS

    @Test
    @DisplayName("addPassenger - valid ADULT passenger is saved successfully")
    void addPassenger_ValidAdult_Success() {
        PassengerRequest req = buildRequest("John", "Doe", "ADULT",
                LocalDate.of(1990, 5, 15), LocalDate.now().plusYears(5));
        PassengerInfo saved = buildPassenger(1L, "John", "Doe", "ADULT", "TKT-ABC12345");

        when(passengerRepository.save(any(PassengerInfo.class))).thenReturn(saved);

        PassengerResponse response = passengerService.addPassenger(req);

        assertThat(response).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getMessage()).isEqualTo("Passenger added successfully");
        verify(passengerRepository).save(any(PassengerInfo.class));
    }

    @Test
    @DisplayName("addPassenger - expired passport throws RuntimeException")
    void addPassenger_ExpiredPassport_ThrowsException() {
        PassengerRequest req = buildRequest("John", "Doe", "ADULT",
                LocalDate.of(1990, 5, 15), LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> passengerService.addPassenger(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Passport is expired");

        verify(passengerRepository, never()).save(any());
    }

    @Test
    @DisplayName("addPassenger - null passport expiry is accepted (no validation)")
    void addPassenger_NullPassportExpiry_Accepted() {
        PassengerRequest req = buildRequest("Jane", "Doe", "ADULT",
                LocalDate.of(1990, 5, 15), null);
        PassengerInfo saved = buildPassenger(1L, "Jane", "Doe", "ADULT", "TKT-JANE1234");

        when(passengerRepository.save(any())).thenReturn(saved);

        assertThatNoException().isThrownBy(() -> passengerService.addPassenger(req));
    }

    @Test
    @DisplayName("addPassenger - valid INFANT (under 2 years) is accepted")
    void addPassenger_ValidInfant_Success() {
        LocalDate infantDob = LocalDate.now().minusMonths(6); // 6 months old
        PassengerRequest req = buildRequest("Baby", "Doe", "INFANT",
                infantDob, LocalDate.now().plusYears(5));
        PassengerInfo saved = buildPassenger(1L, "Baby", "Doe", "INFANT", "TKT-BABY1234");

        when(passengerRepository.save(any())).thenReturn(saved);

        assertThatNoException().isThrownBy(() -> passengerService.addPassenger(req));
    }

    @Test
    @DisplayName("addPassenger - INFANT over 2 years old throws RuntimeException")
    void addPassenger_InfantOverTwoYears_ThrowsException() {
        LocalDate tooOldForInfant = LocalDate.now().minusYears(3); // 3 years old
        PassengerRequest req = buildRequest("OldBaby", "Doe", "INFANT",
                tooOldForInfant, LocalDate.now().plusYears(5));

        assertThatThrownBy(() -> passengerService.addPassenger(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("under 2 years");
    }

    @Test
    @DisplayName("addPassenger - valid CHILD (under 12 years) is accepted")
    void addPassenger_ValidChild_Success() {
        LocalDate childDob = LocalDate.now().minusYears(8); // 8 years old
        PassengerRequest req = buildRequest("Kid", "Doe", "CHILD",
                childDob, LocalDate.now().plusYears(5));
        PassengerInfo saved = buildPassenger(1L, "Kid", "Doe", "CHILD", "TKT-KID12345");

        when(passengerRepository.save(any())).thenReturn(saved);

        assertThatNoException().isThrownBy(() -> passengerService.addPassenger(req));
    }

    @Test
    @DisplayName("addPassenger - CHILD over 12 years throws RuntimeException")
    void addPassenger_ChildOverTwelveYears_ThrowsException() {
        LocalDate tooOldForChild = LocalDate.now().minusYears(13); // 13 years old
        PassengerRequest req = buildRequest("Teen", "Doe", "CHILD",
                tooOldForChild, LocalDate.now().plusYears(5));

        assertThatThrownBy(() -> passengerService.addPassenger(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("under 12 years");
    }

    @Test
    @DisplayName("addPassenger - ticket number is auto-generated with TKT- prefix")
    void addPassenger_TicketNumberGenerated() {
        PassengerRequest req = buildRequest("John", "Doe", "ADULT",
                LocalDate.of(1990, 5, 15), LocalDate.now().plusYears(5));

        when(passengerRepository.save(any())).thenAnswer(inv -> {
            PassengerInfo p = inv.getArgument(0);
            p.setPassengerId(1L);
            return p;
        });

        PassengerResponse response = passengerService.addPassenger(req);

        assertThat(response.getTicketNumber()).startsWith("TKT-");
    }


    //  GET PASSENGER TESTS


    @Test
    @DisplayName("getPassengerById - returns correct passenger for valid ID")
    void getPassengerById_Found() {
        PassengerInfo p = buildPassenger(1L, "John", "Doe", "ADULT", "TKT-ABC12345");
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(p));

        PassengerResponse response = passengerService.getPassengerById(1L);

        assertThat(response.getPassengerId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getMessage()).isEqualTo("Success");
    }

    @Test
    @DisplayName("getPassengerById - non-existent ID throws RuntimeException")
    void getPassengerById_NotFound() {
        when(passengerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passengerService.getPassengerById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("getPassengersByBooking - returns all passengers for a booking")
    void getPassengersByBooking_ReturnsList() {
        PassengerInfo p1 = buildPassenger(1L, "John", "Doe", "ADULT", "TKT-001");
        PassengerInfo p2 = buildPassenger(2L, "Jane", "Doe", "ADULT", "TKT-002");
        when(passengerRepository.findByBookingId("BOOK-001")).thenReturn(Arrays.asList(p1, p2));

        List<PassengerResponse> result = passengerService.getPassengersByBooking("BOOK-001");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getPassengersByBooking - returns empty list when no passengers for booking")
    void getPassengersByBooking_EmptyList() {
        when(passengerRepository.findByBookingId("BOOK-999")).thenReturn(Collections.emptyList());

        assertThat(passengerService.getPassengersByBooking("BOOK-999")).isEmpty();
    }

    @Test
    @DisplayName("getByPassportNumber - returns passenger with matching passport")
    void getByPassportNumber_Found() {
        PassengerInfo p = buildPassenger(1L, "John", "Doe", "ADULT", "TKT-001");
        when(passengerRepository.findByPassportNumber("A1234567")).thenReturn(Optional.of(p));

        PassengerResponse response = passengerService.getByPassportNumber("A1234567");

        assertThat(response.getPassportNumber()).isEqualTo("A1234567");
    }

    @Test
    @DisplayName("getByPassportNumber - unknown passport throws RuntimeException")
    void getByPassportNumber_NotFound() {
        when(passengerRepository.findByPassportNumber("Z9999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passengerService.getByPassportNumber("Z9999999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Z9999999");
    }

    @Test
    @DisplayName("getByTicketNumber - returns passenger for valid ticket number")
    void getByTicketNumber_Found() {
        PassengerInfo p = buildPassenger(1L, "John", "Doe", "ADULT", "TKT-ABC12345");
        when(passengerRepository.findByTicketNumber("TKT-ABC12345")).thenReturn(Optional.of(p));

        PassengerResponse response = passengerService.getByTicketNumber("TKT-ABC12345");

        assertThat(response.getTicketNumber()).isEqualTo("TKT-ABC12345");
    }

    @Test
    @DisplayName("getByTicketNumber - unknown ticket throws RuntimeException")
    void getByTicketNumber_NotFound() {
        when(passengerRepository.findByTicketNumber("TKT-INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passengerService.getByTicketNumber("TKT-INVALID"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TKT-INVALID");
    }


    //  UPDATE PASSENGER TESTS


    @Test
    @DisplayName("updatePassenger - successfully updates passenger details")
    void updatePassenger_Success() {
        PassengerInfo existing = buildPassenger(1L, "John", "Doe", "ADULT", "TKT-001");
        PassengerRequest updateReq = buildRequest("John", "Smith", "ADULT",
                LocalDate.of(1990, 5, 15), LocalDate.now().plusYears(5));
        PassengerInfo updated = buildPassenger(1L, "John", "Smith", "ADULT", "TKT-001");

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passengerRepository.save(any())).thenReturn(updated);

        PassengerResponse response = passengerService.updatePassenger(1L, updateReq);

        assertThat(response.getLastName()).isEqualTo("Smith");
        assertThat(response.getMessage()).isEqualTo("Passenger updated successfully");
    }

    @Test
    @DisplayName("updatePassenger - throws exception for non-existent passenger")
    void updatePassenger_NotFound() {
        when(passengerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passengerService.updatePassenger(999L,
                buildRequest("X", "Y", "ADULT", LocalDate.of(1990, 1, 1), LocalDate.now().plusYears(1))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }


    //  SEAT ASSIGNMENT TESTS


    @Test
    @DisplayName("assignSeat - successfully assigns an available seat to passenger")
    void assignSeat_Success() {
        PassengerInfo passenger = buildPassenger(1L, "John", "Doe", "ADULT", "TKT-001");
        PassengerInfo withSeat = buildPassenger(1L, "John", "Doe", "ADULT", "TKT-001");
        withSeat.setSeatId(10L);
        withSeat.setSeatNumber("12A");

        SeatAssignRequest req = new SeatAssignRequest();
        req.setPassengerId(1L);
        req.setSeatId(10L);
        req.setSeatNumber("12A");

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(passengerRepository.findBySeatId(10L)).thenReturn(Optional.empty()); // seat not taken
        when(passengerRepository.save(any())).thenReturn(withSeat);

        PassengerResponse response = passengerService.assignSeat(req);

        assertThat(response.getSeatNumber()).isEqualTo("12A");
        assertThat(response.getMessage()).isEqualTo("Seat assigned successfully");
    }

    @Test
    @DisplayName("assignSeat - already taken seat throws RuntimeException")
    void assignSeat_SeatAlreadyTaken_ThrowsException() {
        PassengerInfo passenger = buildPassenger(1L, "John", "Doe", "ADULT", "TKT-001");
        PassengerInfo anotherPassenger = buildPassenger(2L, "Jane", "Doe", "ADULT", "TKT-002");
        anotherPassenger.setSeatId(10L);

        SeatAssignRequest req = new SeatAssignRequest();
        req.setPassengerId(1L);
        req.setSeatId(10L);
        req.setSeatNumber("12A");

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(passengerRepository.findBySeatId(10L)).thenReturn(Optional.of(anotherPassenger));

        assertThatThrownBy(() -> passengerService.assignSeat(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already assigned");
    }


    //  DELETE TESTS


    @Test
    @DisplayName("deletePassenger - deletes existing passenger")
    void deletePassenger_Success() {
        when(passengerRepository.existsById(1L)).thenReturn(true);

        assertThatNoException().isThrownBy(() -> passengerService.deletePassenger(1L));
        verify(passengerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletePassenger - throws exception for non-existent ID")
    void deletePassenger_NotFound() {
        when(passengerRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> passengerService.deletePassenger(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");

        verify(passengerRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteByBookingId - calls repository delete by booking ID")
    void deleteByBookingId_CallsRepository() {
        passengerService.deleteByBookingId("BOOK-001");

        verify(passengerRepository).deleteByBookingId("BOOK-001");
    }

    @Test
    @DisplayName("getPassengerCount - returns correct count for booking")
    void getPassengerCount_ReturnsCount() {
        when(passengerRepository.countByBookingId("BOOK-001")).thenReturn(3);

        int count = passengerService.getPassengerCount("BOOK-001");

        assertThat(count).isEqualTo(3);
    }
}
