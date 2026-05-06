package com.skybooker.airline;

import com.skybooker.airline.dto.AirlineRequest;
import com.skybooker.airline.dto.AirlineResponse;
import com.skybooker.airline.dto.AirportRequest;
import com.skybooker.airline.dto.AirportResponse;
import com.skybooker.airline.entity.Airline;
import com.skybooker.airline.entity.Airport;
import com.skybooker.airline.repository.AirlineRepository;
import com.skybooker.airline.repository.AirportRepository;
import com.skybooker.airline.service.AirlineServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AirlineServiceImpl Tests")
class AirlineServiceImplTest {

    @Mock
    private AirlineRepository airlineRepository;

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private AirlineServiceImpl airlineService;

    // Helpers

    private Airline buildAirline(Long id, String name, String iata, boolean active) {
        Airline a = new Airline();
        a.setId(id);
        a.setName(name);
        a.setIataCode(iata);
        a.setIcaoCode("IGO");
        a.setCountry("India");
        a.setContactEmail("support@indigo.in");
        a.setContactPhone("1234567890");
        a.setActive(active);
        return a;
    }

    private AirlineRequest buildAirlineRequest(String name, String iata) {
        AirlineRequest req = new AirlineRequest();
        req.setName(name);
        req.setIataCode(iata);
        req.setIcaoCode("IGO");
        req.setCountry("India");
        req.setContactEmail("support@indigo.in");
        req.setContactPhone("1234567890");
        return req;
    }

    private Airport buildAirport(Long id, String name, String iata, String city) {
        Airport a = new Airport();
        a.setId(id);
        a.setName(name);
        a.setIataCode(iata);
        a.setIcaoCode("VIDP");
        a.setCity(city);
        a.setCountry("India");
        a.setLatitude(28.6139);
        a.setLongitude(77.2090);
        a.setTimezone("Asia/Kolkata");
        return a;
    }

    private AirportRequest buildAirportRequest(String name, String iata, String city) {
        AirportRequest req = new AirportRequest();
        req.setName(name);
        req.setIataCode(iata);
        req.setIcaoCode("VIDP");
        req.setCity(city);
        req.setCountry("India");
        req.setLatitude(28.6139);
        req.setLongitude(77.2090);
        req.setTimezone("Asia/Kolkata");
        return req;
    }


    //  AIRLINE TESTS


    @Test
    @DisplayName("addAirline - success: new airline saved and response returned")
    void addAirline_Success() {
        AirlineRequest req = buildAirlineRequest("IndiGo", "6E");
        Airline saved = buildAirline(1L, "IndiGo", "6E", true);

        when(airlineRepository.existsByIataCode("6E")).thenReturn(false);
        when(airlineRepository.save(any(Airline.class))).thenReturn(saved);

        AirlineResponse response = airlineService.addAirline(req);

        assertThat(response).isNotNull();
        assertThat(response.getIataCode()).isEqualTo("6E");
        assertThat(response.getName()).isEqualTo("IndiGo");
        assertThat(response.isActive()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Airline added successfully");
        verify(airlineRepository).save(any(Airline.class));
    }

    @Test
    @DisplayName("addAirline - duplicate IATA code throws RuntimeException")
    void addAirline_DuplicateIata_ThrowsException() {
        AirlineRequest req = buildAirlineRequest("IndiGo", "6E");
        when(airlineRepository.existsByIataCode("6E")).thenReturn(true);

        assertThatThrownBy(() -> airlineService.addAirline(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("6E")
                .hasMessageContaining("already exists");

        verify(airlineRepository, never()).save(any());
    }

    @Test
    @DisplayName("addAirline - IATA code is uppercased before saving")
    void addAirline_IataCodeUppercased() {
        AirlineRequest req = buildAirlineRequest("IndiGo", "6e");
        Airline saved = buildAirline(1L, "IndiGo", "6E", true);

        when(airlineRepository.existsByIataCode("6e")).thenReturn(false);
        when(airlineRepository.save(any(Airline.class))).thenReturn(saved);

        AirlineResponse response = airlineService.addAirline(req);
        assertThat(response.getIataCode()).isEqualTo("6E");
    }

    @Test
    @DisplayName("addAirline - ICAO code null is handled gracefully")
    void addAirline_NullIcaoCode_Handled() {
        AirlineRequest req = buildAirlineRequest("IndiGo", "6E");
        req.setIcaoCode(null);

        Airline saved = buildAirline(1L, "IndiGo", "6E", true);
        saved.setIcaoCode(null);

        when(airlineRepository.existsByIataCode("6E")).thenReturn(false);
        when(airlineRepository.save(any(Airline.class))).thenReturn(saved);

        assertThatNoException().isThrownBy(() -> airlineService.addAirline(req));
    }

    @Test
    @DisplayName("getAirlineById - existing ID returns correct response")
    void getAirlineById_Found() {
        Airline airline = buildAirline(1L, "IndiGo", "6E", true);
        when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));

        AirlineResponse response = airlineService.getAirlineById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("IndiGo");
        assertThat(response.getMessage()).isEqualTo("Success");
    }

    @Test
    @DisplayName("getAirlineById - non-existent ID throws RuntimeException")
    void getAirlineById_NotFound_ThrowsException() {
        when(airlineRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> airlineService.getAirlineById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("getAirlineByIata - returns correct airline for valid IATA")
    void getAirlineByIata_Found() {
        Airline airline = buildAirline(1L, "IndiGo", "6E", true);
        when(airlineRepository.findByIataCode("6E")).thenReturn(Optional.of(airline));

        AirlineResponse response = airlineService.getAirlineByIata("6e");

        assertThat(response.getIataCode()).isEqualTo("6E");
    }

    @Test
    @DisplayName("getAirlineByIata - lowercase input is uppercased for lookup")
    void getAirlineByIata_LowercaseInput_Uppercased() {
        Airline airline = buildAirline(1L, "Air India", "AI", true);
        when(airlineRepository.findByIataCode("AI")).thenReturn(Optional.of(airline));

        assertThatNoException().isThrownBy(() -> airlineService.getAirlineByIata("ai"));
        verify(airlineRepository).findByIataCode("AI");
    }



    @Test
    @DisplayName("getAllAirlines - returns all airlines from repository")
    void getAllAirlines_ReturnsList() {
        Airline a1 = buildAirline(1L, "IndiGo", "6E", true);
        Airline a2 = buildAirline(2L, "Air India", "AI", true);
        when(airlineRepository.findAll()).thenReturn(Arrays.asList(a1, a2));

        List<AirlineResponse> result = airlineService.getAllAirlines();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AirlineResponse::getIataCode)
                .containsExactlyInAnyOrder("6E", "AI");
    }

    @Test
    @DisplayName("getAllAirlines - returns empty list when no airlines exist")
    void getAllAirlines_EmptyList() {
        when(airlineRepository.findAll()).thenReturn(Collections.emptyList());

        List<AirlineResponse> result = airlineService.getAllAirlines();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getActiveAirlines - returns only active airlines")
    void getActiveAirlines_ReturnsOnlyActive() {
        Airline a1 = buildAirline(1L, "IndiGo", "6E", true);
        Airline a2 = buildAirline(2L, "Air India", "AI", true);
        when(airlineRepository.findByIsActive(true)).thenReturn(Arrays.asList(a1, a2));

        List<AirlineResponse> result = airlineService.getActiveAirlines();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(AirlineResponse::isActive);
    }

    @Test
    @DisplayName("updateAirline - updates fields and returns updated response")
    void updateAirline_Success() {
        Airline existing = buildAirline(1L, "OldName", "6E", true);
        AirlineRequest updateReq = buildAirlineRequest("NewName", "6E");
        updateReq.setContactEmail("new@email.com");
        Airline updated = buildAirline(1L, "NewName", "6E", true);
        updated.setContactEmail("new@email.com");

        when(airlineRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(airlineRepository.save(any())).thenReturn(updated);

        AirlineResponse response = airlineService.updateAirline(1L, updateReq);

        assertThat(response.getName()).isEqualTo("NewName");
        assertThat(response.getMessage()).isEqualTo("Airline updated successfully");
    }

    @Test
    @DisplayName("updateAirline - throws exception for non-existent ID")
    void updateAirline_NotFound_ThrowsException() {
        when(airlineRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> airlineService.updateAirline(999L, buildAirlineRequest("X", "XX")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("toggleAirlineStatus - active airline becomes inactive")
    void toggleAirlineStatus_ActiveToInactive() {
        Airline active = buildAirline(1L, "IndiGo", "6E", true);
        Airline deactivated = buildAirline(1L, "IndiGo", "6E", false);

        when(airlineRepository.findById(1L)).thenReturn(Optional.of(active));
        when(airlineRepository.save(any())).thenReturn(deactivated);

        AirlineResponse response = airlineService.toggleAirlineStatus(1L);

        assertThat(response.isActive()).isFalse();
        assertThat(response.getMessage()).contains("deactivated");
    }

    @Test
    @DisplayName("toggleAirlineStatus - inactive airline becomes active")
    void toggleAirlineStatus_InactiveToActive() {
        Airline inactive = buildAirline(1L, "IndiGo", "6E", false);
        Airline activated = buildAirline(1L, "IndiGo", "6E", true);

        when(airlineRepository.findById(1L)).thenReturn(Optional.of(inactive));
        when(airlineRepository.save(any())).thenReturn(activated);

        AirlineResponse response = airlineService.toggleAirlineStatus(1L);

        assertThat(response.isActive()).isTrue();
        assertThat(response.getMessage()).contains("activated");
    }

    @Test
    @DisplayName("toggleAirlineStatus - throws exception for non-existent airline")
    void toggleAirlineStatus_NotFound() {
        when(airlineRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> airlineService.toggleAirlineStatus(999L))
                .isInstanceOf(RuntimeException.class);
    }


    //  AIRPORT TESTS


    @Test
    @DisplayName("addAirport - success: new airport saved and response returned")
    void addAirport_Success() {
        AirportRequest req = buildAirportRequest("Indira Gandhi International Airport", "DEL", "Delhi");
        Airport saved = buildAirport(1L, "Indira Gandhi International Airport", "DEL", "Delhi");

        when(airportRepository.existsByIataCode("DEL")).thenReturn(false);
        when(airportRepository.save(any(Airport.class))).thenReturn(saved);

        AirportResponse response = airlineService.addAirport(req);

        assertThat(response.getIataCode()).isEqualTo("DEL");
        assertThat(response.getCity()).isEqualTo("Delhi");
        assertThat(response.getMessage()).isEqualTo("Airport added successfully");
    }

    @Test
    @DisplayName("addAirport - duplicate IATA code throws RuntimeException")
    void addAirport_DuplicateIata_ThrowsException() {
        AirportRequest req = buildAirportRequest("IGI Airport", "DEL", "Delhi");
        when(airportRepository.existsByIataCode("DEL")).thenReturn(true);

        assertThatThrownBy(() -> airlineService.addAirport(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DEL");

        verify(airportRepository, never()).save(any());
    }

    @Test
    @DisplayName("addAirport - IATA code is uppercased before saving")
    void addAirport_IataUppercased() {
        AirportRequest req = buildAirportRequest("IGI Airport", "del", "Delhi");
        Airport saved = buildAirport(1L, "IGI Airport", "DEL", "Delhi");

        when(airportRepository.existsByIataCode("del")).thenReturn(false);
        when(airportRepository.save(any())).thenReturn(saved);

        airlineService.addAirport(req);
        verify(airportRepository).existsByIataCode("del");
    }

    @Test
    @DisplayName("getAirportById - returns correct airport for valid ID")
    void getAirportById_Found() {
        Airport airport = buildAirport(1L, "IGI Airport", "DEL", "Delhi");
        when(airportRepository.findById(1L)).thenReturn(Optional.of(airport));

        AirportResponse response = airlineService.getAirportById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getIataCode()).isEqualTo("DEL");
    }

    @Test
    @DisplayName("getAirportById - non-existent ID throws RuntimeException")
    void getAirportById_NotFound() {
        when(airportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> airlineService.getAirportById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("getAirportByIata - returns correct airport for valid IATA")
    void getAirportByIata_Found() {
        Airport airport = buildAirport(1L, "IGI Airport", "DEL", "Delhi");
        when(airportRepository.findByIataCode("DEL")).thenReturn(Optional.of(airport));

        AirportResponse response = airlineService.getAirportByIata("del");

        assertThat(response.getIataCode()).isEqualTo("DEL");
    }

    @Test
    @DisplayName("getAirportByIata - unknown IATA throws RuntimeException")
    void getAirportByIata_NotFound() {
        when(airportRepository.findByIataCode("ZZZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> airlineService.getAirportByIata("ZZZ"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ZZZ");
    }

    @Test
    @DisplayName("getAllAirports - returns all airports")
    void getAllAirports_ReturnsList() {
        Airport a1 = buildAirport(1L, "IGI Airport", "DEL", "Delhi");
        Airport a2 = buildAirport(2L, "CSIA Airport", "BOM", "Mumbai");
        when(airportRepository.findAll()).thenReturn(Arrays.asList(a1, a2));

        List<AirportResponse> result = airlineService.getAllAirports();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AirportResponse::getIataCode)
                .containsExactlyInAnyOrder("DEL", "BOM");
    }

    @Test
    @DisplayName("searchAirports - returns matching airports by keyword")
    void searchAirports_ByKeyword() {
        Airport airport = buildAirport(1L, "IGI Airport", "DEL", "Delhi");
        when(airportRepository.findByCityContainingIgnoreCaseOrNameContainingIgnoreCase("delhi", "delhi"))
                .thenReturn(Collections.singletonList(airport));

        List<AirportResponse> result = airlineService.searchAirports("delhi");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCity()).isEqualTo("Delhi");
    }

    @Test
    @DisplayName("searchAirports - returns empty list when no match")
    void searchAirports_NoMatch_EmptyList() {
        when(airportRepository.findByCityContainingIgnoreCaseOrNameContainingIgnoreCase("xyz", "xyz"))
                .thenReturn(Collections.emptyList());

        List<AirportResponse> result = airlineService.searchAirports("xyz");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("updateAirport - updates fields and returns updated response")
    void updateAirport_Success() {
        Airport existing = buildAirport(1L, "Old Name", "DEL", "Delhi");
        AirportRequest req = buildAirportRequest("New Name", "DEL", "New Delhi");
        Airport updated = buildAirport(1L, "New Name", "DEL", "New Delhi");

        when(airportRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(airportRepository.save(any())).thenReturn(updated);

        AirportResponse response = airlineService.updateAirport(1L, req);

        assertThat(response.getName()).isEqualTo("New Name");
        assertThat(response.getCity()).isEqualTo("New Delhi");
        assertThat(response.getMessage()).isEqualTo("Airport updated successfully");
    }

    @Test
    @DisplayName("updateAirport - throws exception for non-existent ID")
    void updateAirport_NotFound() {
        when(airportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> airlineService.updateAirport(999L, buildAirportRequest("X", "X", "X")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("addAirline - response contains all mapped fields correctly")
    void addAirline_ResponseContainsAllFields() {
        AirlineRequest req = buildAirlineRequest("SpiceJet", "SG");
        req.setContactEmail("info@spicejet.com");
        req.setContactPhone("9999999999");

        Airline saved = buildAirline(5L, "SpiceJet", "SG", true);
        saved.setContactEmail("info@spicejet.com");
        saved.setContactPhone("9999999999");

        when(airlineRepository.existsByIataCode("SG")).thenReturn(false);
        when(airlineRepository.save(any())).thenReturn(saved);

        AirlineResponse response = airlineService.addAirline(req);

        assertThat(response.getContactEmail()).isEqualTo("info@spicejet.com");
        assertThat(response.getContactPhone()).isEqualTo("9999999999");
        assertThat(response.getCountry()).isEqualTo("India");
        assertThat(response.getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getAllAirports - returns empty list when no airports exist")
    void getAllAirports_EmptyList() {
        when(airportRepository.findAll()).thenReturn(Collections.emptyList());

        List<AirportResponse> result = airlineService.getAllAirports();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("addAirport - response contains all fields including coordinates")
    void addAirport_ResponseContainsCoordinates() {
        AirportRequest req = buildAirportRequest("IGI Airport", "DEL", "Delhi");
        req.setLatitude(28.5562);
        req.setLongitude(77.1000);

        Airport saved = buildAirport(1L, "IGI Airport", "DEL", "Delhi");
        saved.setLatitude(28.5562);
        saved.setLongitude(77.1000);

        when(airportRepository.existsByIataCode("DEL")).thenReturn(false);
        when(airportRepository.save(any())).thenReturn(saved);

        AirportResponse response = airlineService.addAirport(req);

        assertThat(response.getLatitude()).isEqualTo(28.5562);
        assertThat(response.getLongitude()).isEqualTo(77.1000);
    }
}
