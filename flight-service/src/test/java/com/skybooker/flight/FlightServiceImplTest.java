package com.skybooker.flight;

import com.skybooker.flight.dto.FlightRequest;
import com.skybooker.flight.dto.FlightResponse;
import com.skybooker.flight.entity.Flight;
import com.skybooker.flight.repository.FlightRepository;
import com.skybooker.flight.service.FlightServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

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
@DisplayName("FlightServiceImpl Tests")
class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FlightServiceImpl flightService;

    // Helpers

    private FlightRequest buildRequest(String flightNum, LocalDate depDate, LocalDate arrDate,
                                       String depTime, String arrTime) {
        FlightRequest req = new FlightRequest();
        req.setFlightNumber(flightNum);
        req.setAirline("IndiGo");
        req.setSource("DEL");
        req.setDestination("BOM");
        req.setDepartureDate(depDate);
        req.setDepartureTime(depTime);
        req.setArrivalDate(arrDate);
        req.setArrivalTime(arrTime);
        req.setTotalSeats(180);
        req.setPrice(5000.0);
        return req;
    }

    private Flight buildFlight(Long id, String flightNum, LocalDate depDate, int availableSeats) {
        Flight f = new Flight();
        f.setId(id);
        f.setFlightNumber(flightNum);
        f.setAirline("IndiGo");
        f.setSource("DEL");
        f.setDestination("BOM");
        f.setDepartureDate(depDate);
        f.setDepartureTime("08:00");
        f.setArrivalDate(depDate);
        f.setArrivalTime("10:00");
        f.setTotalSeats(180);
        f.setAvailableSeats(availableSeats);
        f.setPrice(5000.0);
        f.setCreatedAt(LocalDateTime.now());
        f.setUpdatedAt(LocalDateTime.now());
        return f;
    }


    //  ADD FLIGHT TESTS


    @Test
    @DisplayName("addFlight - valid future flight is saved successfully")
    void addFlight_ValidFutureFlight_Success() {
        LocalDate future = LocalDate.now().plusDays(5);
        FlightRequest req = buildRequest("6E-101", future, future, "08:00", "10:00");
        Flight saved = buildFlight(1L, "6E-101", future, 180);

        when(flightRepository.save(any(Flight.class))).thenReturn(saved);
        // RestTemplate call for seat generation — swallow
        when(restTemplate.postForObject(anyString(), any(), eq(Object.class))).thenReturn(null);

        FlightResponse response = flightService.addFlight(req);

        assertThat(response).isNotNull();
        assertThat(response.getFlightNumber()).isEqualTo("6E-101");
        assertThat(response.getAvailableSeats()).isEqualTo(180);
        verify(flightRepository).save(any(Flight.class));
    }

    @Test
    @DisplayName("addFlight - departure date in the past throws RuntimeException")
    void addFlight_PastDepartureDate_ThrowsException() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        FlightRequest req = buildRequest("6E-101", yesterday, yesterday, "08:00", "10:00");

        assertThatThrownBy(() -> flightService.addFlight(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("past");

        verify(flightRepository, never()).save(any());
    }

    @Test
    @DisplayName("addFlight - null departure date throws RuntimeException")
    void addFlight_NullDepartureDate_ThrowsException() {
        FlightRequest req = buildRequest("6E-101", null, null, "08:00", "10:00");

        assertThatThrownBy(() -> flightService.addFlight(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Departure date is required");
    }

    @Test
    @DisplayName("addFlight - arrival date before departure date throws RuntimeException")
    void addFlight_ArrivalBeforeDeparture_ThrowsException() {
        LocalDate future = LocalDate.now().plusDays(5);
        LocalDate earlier = future.minusDays(1);
        FlightRequest req = buildRequest("6E-101", future, earlier, "08:00", "10:00");

        assertThatThrownBy(() -> flightService.addFlight(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Arrival date cannot be before departure date");
    }

    @Test
    @DisplayName("addFlight - same-day flight with arrival time before departure throws exception")
    void addFlight_SameDayArrivalTimeBeforeDeparture_ThrowsException() {
        LocalDate future = LocalDate.now().plusDays(5);
        // departure 10:00, arrival 08:00 — invalid same-day
        FlightRequest req = buildRequest("6E-101", future, future, "10:00", "08:00");

        assertThatThrownBy(() -> flightService.addFlight(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("arrival time must be after departure time");
    }

    @Test
    @DisplayName("addFlight - same-day flight with equal departure and arrival time throws exception")
    void addFlight_SameDayEqualTimes_ThrowsException() {
        LocalDate future = LocalDate.now().plusDays(5);
        FlightRequest req = buildRequest("6E-101", future, future, "08:00", "08:00");

        assertThatThrownBy(() -> flightService.addFlight(req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("addFlight - overnight flight (different arrival date) is valid")
    void addFlight_OvernightFlight_Valid() {
        LocalDate departure = LocalDate.now().plusDays(5);
        LocalDate arrival = departure.plusDays(1); // next day
        FlightRequest req = buildRequest("AI-202", departure, arrival, "22:00", "02:00");
        Flight saved = buildFlight(2L, "AI-202", departure, 180);
        saved.setArrivalDate(arrival);

        when(flightRepository.save(any())).thenReturn(saved);
        when(restTemplate.postForObject(anyString(), any(), eq(Object.class))).thenReturn(null);

        assertThatNoException().isThrownBy(() -> flightService.addFlight(req));
    }

    @Test
    @DisplayName("addFlight - null arrival date defaults to departure date")
    void addFlight_NullArrivalDate_DefaultsToDepartureDate() {
        LocalDate future = LocalDate.now().plusDays(5);
        FlightRequest req = buildRequest("6E-101", future, null, "08:00", "10:00");
        Flight saved = buildFlight(1L, "6E-101", future, 180);

        when(flightRepository.save(any())).thenReturn(saved);
        when(restTemplate.postForObject(anyString(), any(), eq(Object.class))).thenReturn(null);

        assertThatNoException().isThrownBy(() -> flightService.addFlight(req));
    }

    @Test
    @DisplayName("addFlight - today's date is accepted as departure date")
    void addFlight_TodayDepartureDate_Valid() {
        LocalDate today = LocalDate.now();
        FlightRequest req = buildRequest("6E-101", today, today, "08:00", "10:00");
        Flight saved = buildFlight(1L, "6E-101", today, 180);

        when(flightRepository.save(any())).thenReturn(saved);
        when(restTemplate.postForObject(anyString(), any(), eq(Object.class))).thenReturn(null);

        assertThatNoException().isThrownBy(() -> flightService.addFlight(req));
    }


    //  QUERY TESTS


    @Test
    @DisplayName("getAllFlights - returns all flights from repository")
    void getAllFlights_ReturnsList() {
        LocalDate future = LocalDate.now().plusDays(5);
        Flight f1 = buildFlight(1L, "6E-101", future, 180);
        Flight f2 = buildFlight(2L, "AI-202", future, 200);
        when(flightRepository.findAll()).thenReturn(Arrays.asList(f1, f2));

        List<FlightResponse> result = flightService.getAllFlights();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(FlightResponse::getFlightNumber)
                .containsExactlyInAnyOrder("6E-101", "AI-202");
    }

    @Test
    @DisplayName("getAllFlights - returns empty list when no flights exist")
    void getAllFlights_EmptyList() {
        when(flightRepository.findAll()).thenReturn(Collections.emptyList());

        assertThat(flightService.getAllFlights()).isEmpty();
    }

    @Test
    @DisplayName("searchFlights - returns matching flights for source, destination, date")
    void searchFlights_ReturnsMatches() {
        LocalDate date = LocalDate.now().plusDays(5);
        Flight f = buildFlight(1L, "6E-101", date, 180);
        when(flightRepository.findBySourceAndDestinationAndDepartureDate("DEL", "BOM", date))
                .thenReturn(Collections.singletonList(f));

        List<FlightResponse> result = flightService.searchFlights("DEL", "BOM", date);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFlightNumber()).isEqualTo("6E-101");
    }

    @Test
    @DisplayName("searchFlights - returns empty list when no match found")
    void searchFlights_NoMatch() {
        LocalDate date = LocalDate.now().plusDays(5);
        when(flightRepository.findBySourceAndDestinationAndDepartureDate("DEL", "CCU", date))
                .thenReturn(Collections.emptyList());

        assertThat(flightService.searchFlights("DEL", "CCU", date)).isEmpty();
    }

    @Test
    @DisplayName("getFlightById - returns correct flight for valid ID")
    void getFlightById_Found() {
        LocalDate future = LocalDate.now().plusDays(5);
        Flight flight = buildFlight(1L, "6E-101", future, 180);
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));

        FlightResponse response = flightService.getFlightById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFlightNumber()).isEqualTo("6E-101");
    }

    @Test
    @DisplayName("getFlightById - non-existent ID throws RuntimeException")
    void getFlightById_NotFound() {
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.getFlightById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }


    //  REDUCE SEATS TESTS


    @Test
    @DisplayName("reduceSeats - successfully reduces available seats")
    void reduceSeats_Success() {
        LocalDate future = LocalDate.now().plusDays(5);
        Flight flight = buildFlight(1L, "6E-101", future, 10);
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(flightRepository.save(any())).thenReturn(flight);

        String result = flightService.reduceSeats(1L, 3);

        assertThat(result).isEqualTo("Seats reduced successfully");
        assertThat(flight.getAvailableSeats()).isEqualTo(7);
    }

    @Test
    @DisplayName("reduceSeats - throws exception when not enough seats available")
    void reduceSeats_NotEnoughSeats_ThrowsException() {
        LocalDate future = LocalDate.now().plusDays(5);
        Flight flight = buildFlight(1L, "6E-101", future, 2);
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));

        assertThatThrownBy(() -> flightService.reduceSeats(1L, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not enough seats");
    }

    @Test
    @DisplayName("reduceSeats - reduces exactly all available seats succeeds")
    void reduceSeats_ExactlyAllSeats_Succeeds() {
        LocalDate future = LocalDate.now().plusDays(5);
        Flight flight = buildFlight(1L, "6E-101", future, 5);
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(flightRepository.save(any())).thenReturn(flight);

        String result = flightService.reduceSeats(1L, 5);

        assertThat(result).contains("successfully");
        assertThat(flight.getAvailableSeats()).isZero();
    }

    @Test
    @DisplayName("reduceSeats - flight not found throws RuntimeException")
    void reduceSeats_FlightNotFound() {
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.reduceSeats(999L, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Flight not found");
    }

    @Test
    @DisplayName("addFlight - mapped response contains correct price")
    void addFlight_ResponseContainsCorrectPrice() {
        LocalDate future = LocalDate.now().plusDays(5);
        FlightRequest req = buildRequest("6E-101", future, future, "08:00", "10:00");
        req.setPrice(7500.0);
        Flight saved = buildFlight(1L, "6E-101", future, 180);
        saved.setPrice(7500.0);

        when(flightRepository.save(any())).thenReturn(saved);
        when(restTemplate.postForObject(anyString(), any(), eq(Object.class))).thenReturn(null);

        FlightResponse response = flightService.addFlight(req);

        assertThat(response.getPrice()).isEqualTo(7500.0);
    }

    @Test
    @DisplayName("addFlight - seat generation failure does not abort flight creation")
    void addFlight_SeatGenerationFails_FlightStillSaved() {
        LocalDate future = LocalDate.now().plusDays(5);
        FlightRequest req = buildRequest("6E-101", future, future, "08:00", "10:00");
        Flight saved = buildFlight(1L, "6E-101", future, 180);

        when(flightRepository.save(any())).thenReturn(saved);
        when(restTemplate.postForObject(anyString(), any(), eq(Object.class)))
                .thenThrow(new RuntimeException("Seat service down"));

        // Should not propagate seat generation exception
        assertThatNoException().isThrownBy(() -> flightService.addFlight(req));
        verify(flightRepository).save(any());
    }
}
