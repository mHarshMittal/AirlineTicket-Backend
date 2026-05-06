package com.skybooker.auth;

import com.skybooker.auth.dto.AuthResponse;
import com.skybooker.auth.dto.LoginRequest;
import com.skybooker.auth.dto.RegisterRequest;
import com.skybooker.auth.entity.User;
import com.skybooker.auth.repository.UserRepository;
import com.skybooker.auth.security.JwtUtil;
import com.skybooker.auth.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "adminSecretKey", "ADMIN_SECRET_123");
        ReflectionTestUtils.setField(authService, "staffSecretKey", "STAFF_SECRET_456");
    }

    // Helpers

    private RegisterRequest buildRegisterRequest(String email, String role) {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test User");
        req.setEmail(email);
        req.setPassword("password123");
        req.setPhone("9876543210");
        req.setRole(role);
        return req;
    }

    private User buildUser(Long id, String email, String role, boolean active) {
        User user = new User();
        user.setId(id);
        user.setFullName("Test User");
        user.setEmail(email);
        user.setPassword("$2a$encoded");
        user.setPhone("9876543210");
        user.setRole(role);
        user.setActive(active);
        user.setVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }


    //  REGISTER TESTS


    @Test
    @DisplayName("register - PASSENGER registration succeeds without secret key")
    void register_Passenger_Success() {
        RegisterRequest req = buildRegisterRequest("john@example.com", "PASSENGER");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenReturn(buildUser(1L, "john@example.com", "PASSENGER", true));

        AuthResponse response = authService.register(req);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("PASSENGER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - duplicate email throws RuntimeException")
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest req = buildRegisterRequest("john@example.com", "PASSENGER");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("john@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register - ADMIN registration succeeds with correct secret key")
    void register_Admin_CorrectSecretKey_Success() {
        RegisterRequest req = buildRegisterRequest("admin@sky.com", "ADMIN");
        req.setAdminSecretKey("ADMIN_SECRET_123");

        when(userRepository.existsByEmail("admin@sky.com")).thenReturn(false);
        when(userRepository.countByRole("ADMIN")).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenReturn(buildUser(1L, "admin@sky.com", "ADMIN", true));

        AuthResponse response = authService.register(req);

        assertThat(response.getMessage()).contains("ADMIN");
    }

    @Test
    @DisplayName("register - ADMIN registration fails with wrong secret key")
    void register_Admin_WrongSecretKey_ThrowsException() {
        RegisterRequest req = buildRegisterRequest("admin@sky.com", "ADMIN");
        req.setAdminSecretKey("WRONG_KEY");

        when(userRepository.existsByEmail("admin@sky.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid admin secret key");
    }

    @Test
    @DisplayName("register - ADMIN registration fails with missing secret key")
    void register_Admin_MissingSecretKey_ThrowsException() {
        RegisterRequest req = buildRegisterRequest("admin@sky.com", "ADMIN");
        req.setAdminSecretKey(null);

        when(userRepository.existsByEmail("admin@sky.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("admin secret key");
    }

    @Test
    @DisplayName("register - ADMIN registration fails when 4 admins already exist")
    void register_Admin_MaxAdminsReached_ThrowsException() {
        RegisterRequest req = buildRegisterRequest("admin5@sky.com", "ADMIN");
        req.setAdminSecretKey("ADMIN_SECRET_123");

        when(userRepository.existsByEmail("admin5@sky.com")).thenReturn(false);
        when(userRepository.countByRole("ADMIN")).thenReturn(4L);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Maximum 4 admin");
    }

    @Test
    @DisplayName("register - AIRLINE_STAFF registration succeeds with correct staff key")
    void register_AirlineStaff_CorrectKey_Success() {
        RegisterRequest req = buildRegisterRequest("staff@indigo.com", "AIRLINE_STAFF");
        req.setStaffSecretKey("STAFF_SECRET_456");

        when(userRepository.existsByEmail("staff@indigo.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenReturn(buildUser(1L, "staff@indigo.com", "AIRLINE_STAFF", true));

        AuthResponse response = authService.register(req);

        assertThat(response.getMessage()).contains("AIRLINE_STAFF");
    }

    @Test
    @DisplayName("register - AIRLINE_STAFF registration fails with wrong staff key")
    void register_AirlineStaff_WrongKey_ThrowsException() {
        RegisterRequest req = buildRegisterRequest("staff@indigo.com", "AIRLINE_STAFF");
        req.setStaffSecretKey("WRONG_STAFF_KEY");

        when(userRepository.existsByEmail("staff@indigo.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid staff secret key");
    }

    @Test
    @DisplayName("register - AIRLINE_STAFF registration fails without staff key")
    void register_AirlineStaff_MissingKey_ThrowsException() {
        RegisterRequest req = buildRegisterRequest("staff@indigo.com", "AIRLINE_STAFF");
        req.setStaffSecretKey(null);

        when(userRepository.existsByEmail("staff@indigo.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("staff secret key");
    }

    @Test
    @DisplayName("register - invalid role throws RuntimeException")
    void register_InvalidRole_ThrowsException() {
        RegisterRequest req = buildRegisterRequest("user@example.com", "HACKER");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid role");
    }

    @Test
    @DisplayName("register - null role defaults to PASSENGER")
    void register_NullRole_DefaultsToPassenger() {
        RegisterRequest req = buildRegisterRequest("user@example.com", null);

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenReturn(buildUser(1L, "user@example.com", "PASSENGER", true));

        AuthResponse response = authService.register(req);

        assertThat(response.getMessage()).contains("PASSENGER");
    }

    @Test
    @DisplayName("register - phone defaults to NOT_PROVIDED when null")
    void register_NullPhone_SetsDefault() {
        RegisterRequest req = buildRegisterRequest("user@example.com", "PASSENGER");
        req.setPhone(null);

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.register(req);

        verify(userRepository).save(argThat(user -> "NOT_PROVIDED".equals(user.getPhone())));
    }

    @Test
    @DisplayName("register - password is encoded before saving")
    void register_PasswordIsEncoded() {
        RegisterRequest req = buildRegisterRequest("user@example.com", "PASSENGER");
        req.setPassword("plaintext");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("$2a$hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.register(req);

        verify(passwordEncoder).encode("plaintext");
        verify(userRepository).save(argThat(user -> "$2a$hashed".equals(user.getPassword())));
    }

    @Test
    @DisplayName("register - new user is set active and unverified")
    void register_NewUser_IsActiveNotVerified() {
        RegisterRequest req = buildRegisterRequest("user@example.com", "PASSENGER");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.register(req);

        verify(userRepository).save(argThat(user -> user.isActive() && !user.isVerified()));
    }


    //  LOGIN TESTS


    @Test
    @DisplayName("login - valid credentials return JWT token")
    void login_ValidCredentials_ReturnsToken() {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("password123");

        User user = buildUser(1L, "john@example.com", "PASSENGER", true);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$encoded")).thenReturn(true);
        when(jwtUtil.generateToken("john@example.com", "PASSENGER")).thenReturn("jwt.token.here");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("jwt.token.here");
    }

    @Test
    @DisplayName("login - email not found throws RuntimeException")
    void login_EmailNotFound_ThrowsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("ghost@example.com");
        req.setPassword("pass");

        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ghost@example.com");
    }

    @Test
    @DisplayName("login - wrong password throws RuntimeException")
    void login_WrongPassword_ThrowsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("wrongpass");

        User user = buildUser(1L, "john@example.com", "PASSENGER", true);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "$2a$encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Incorrect password");
    }

    @Test
    @DisplayName("login - deactivated account throws RuntimeException")
    void login_DeactivatedAccount_ThrowsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("inactive@example.com");
        req.setPassword("password123");

        User user = buildUser(1L, "inactive@example.com", "PASSENGER", false);

        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("deactivated");
    }

    @Test
    @DisplayName("login - JWT is generated with correct email and role")
    void login_JwtGeneratedWithCorrectParams() {
        LoginRequest req = new LoginRequest();
        req.setEmail("staff@indigo.com");
        req.setPassword("pass");

        User user = buildUser(1L, "staff@indigo.com", "AIRLINE_STAFF", true);

        when(userRepository.findByEmail("staff@indigo.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "$2a$encoded")).thenReturn(true);
        when(jwtUtil.generateToken("staff@indigo.com", "AIRLINE_STAFF")).thenReturn("staff.token");

        authService.login(req);

        verify(jwtUtil).generateToken("staff@indigo.com", "AIRLINE_STAFF");
    }

    @Test
    @DisplayName("register - gender defaults to NOT_SPECIFIED when null")
    void register_NullGender_SetsDefault() {
        RegisterRequest req = buildRegisterRequest("user@example.com", "PASSENGER");
        req.setGender(null);

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.register(req);

        verify(userRepository).save(argThat(user -> "NOT_SPECIFIED".equals(user.getGender())));
    }

    @Test
    @DisplayName("register - role is case-insensitive (lowercase 'passenger' works)")
    void register_LowercaseRole_Works() {
        RegisterRequest req = buildRegisterRequest("user@example.com", "passenger");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() -> authService.register(req));
    }
}
