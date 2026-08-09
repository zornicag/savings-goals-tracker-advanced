package app.config;

import app.model.entity.user.User;
import app.model.entity.user.UserRole;
import app.repository.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    void authenticationSuccess_ShouldSaveUserInSessionAndRedirect()
            throws Exception {

        UserRepository userRepository = mock(UserRepository.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Authentication authentication = mock(Authentication.class);

        User user = new User();
        UUID id = UUID.randomUUID();

        user.setId(id);
        user.setUsername("testuser");
        user.setEmail("test@test.com");

        when(authentication.getName())
                .thenReturn("test@test.com");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(request.getSession())
                .thenReturn(session);

        AuthenticationSuccessHandler handler =
                new AuthenticationSuccessHandler(userRepository);

        handler.onAuthenticationSuccess(
                request,
                response,
                authentication
        );

        verify(session)
                .setAttribute("currentUserId", id);

        verify(session)
                .setAttribute("currentUsername", "testuser");

        verify(response)
                .sendRedirect("/home");
    }

    @Test
    void adminInitializer_ShouldCreateAdmin_WhenAdminDoesNotExist() {

        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ApplicationArguments arguments = mock(ApplicationArguments.class);

        when(userRepository.findByEmail(
                "admin@savingsgoalstracker.com"))
                .thenReturn(Optional.empty());

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("AdminPassword1!"))
                .thenReturn("encodedPassword");

        AdminUserInitializer initializer =
                new AdminUserInitializer(
                        userRepository,
                        passwordEncoder
                );

        ReflectionTestUtils.setField(
                initializer,
                "adminPassword",
                "AdminPassword1!"
        );

        initializer.run(arguments);

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("admin")
                        && user.getEmail().equals(
                        "admin@savingsgoalstracker.com")
                        && user.getRole() == UserRole.ADMIN
                        && user.getPassword().equals("encodedPassword")
        ));
    }

    @Test
    void adminInitializer_ShouldUpdateExistingAdmin() {

        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ApplicationArguments arguments = mock(ApplicationArguments.class);

        User existingAdmin = new User();
        existingAdmin.setId(UUID.randomUUID());
        existingAdmin.setUsername("admin");
        existingAdmin.setEmail(
                "admin@savingsgoalstracker.com"
        );
        existingAdmin.setRole(UserRole.ADMIN);

        when(userRepository.findByEmail(
                "admin@savingsgoalstracker.com"))
                .thenReturn(Optional.of(existingAdmin));

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(existingAdmin));

        when(passwordEncoder.encode("AdminPassword1!"))
                .thenReturn("newEncodedPassword");

        AdminUserInitializer initializer =
                new AdminUserInitializer(
                        userRepository,
                        passwordEncoder
                );

        ReflectionTestUtils.setField(
                initializer,
                "adminPassword",
                "AdminPassword1!"
        );

        initializer.run(arguments);

        assertEquals(
                "newEncodedPassword",
                existingAdmin.getPassword()
        );

        verify(userRepository).save(existingAdmin);
    }
}
