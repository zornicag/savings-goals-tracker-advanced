package app.web;

import app.model.dto.user.UserDto;
import app.model.dto.user.UserRegisterRequest;
import app.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    @Test
    void getLoginPage_ShouldReturnLoginPage() {
        when(session.getAttribute("loginEmail"))
                .thenReturn(null);

        String result =
                userController.getLoginPage(null, model, session);

        assertEquals("login", result);

        verify(model)
                .addAttribute(eq("userLoginRequest"), any());
    }

    @Test
    void getLoginPage_ShouldRestoreSavedEmail() {
        when(session.getAttribute("loginEmail"))
                .thenReturn("test@test.com");

        String result =
                userController.getLoginPage(null, model, session);

        assertEquals("login", result);

        verify(session)
                .removeAttribute("loginEmail");
    }

    @Test
    void getLoginPage_ShouldAddError_WhenErrorParameterExists() {
        when(session.getAttribute("loginEmail"))
                .thenReturn(null);

        String result =
                userController.getLoginPage("true", model, session);

        assertEquals("login", result);

        verify(model)
                .addAttribute("error", "Wrong email or password");
    }

    @Test
    void getRegisterPage_ShouldReturnRegisterPage() {
        String result =
                userController.getRegisterPage(model);

        assertEquals("register", result);

        verify(model)
                .addAttribute(eq("userRegisterRequest"), any(UserRegisterRequest.class));
    }

    @Test
    void register_ShouldReturnRegister_WhenValidationFails() {
        UserRegisterRequest request =
                new UserRegisterRequest();

        when(bindingResult.hasErrors())
                .thenReturn(true);

        String result =
                userController.register(request, bindingResult);

        assertEquals("register", result);

        verify(userService, never())
                .register(any());
    }

    @Test
    void register_ShouldRegisterUser() {
        UserRegisterRequest request =
                new UserRegisterRequest();

        when(bindingResult.hasErrors())
                .thenReturn(false);

        String result =
                userController.register(request, bindingResult);

        assertEquals("redirect:/login", result);

        verify(userService)
                .register(request);
    }

    @Test
    void getProfilePage_ShouldReturnProfile() {
        UserDto user = new UserDto();
        user.setUsername("testuser");
        user.setEmail("test@test.com");

        when(authentication.getName())
                .thenReturn("test@test.com");

        when(userService.getUserByEmail("test@test.com"))
                .thenReturn(user);

        String result =
                userController.getProfilePage(authentication, model);

        assertEquals("profile", result);

        verify(model)
                .addAttribute("user", user);
    }

    @Test
    void updateProfile_ShouldUpdateUsername() {
        UserDto updatedUser = new UserDto();
        updatedUser.setId(UUID.randomUUID());
        updatedUser.setUsername("updated");
        updatedUser.setEmail("test@test.com");

        when(authentication.getName())
                .thenReturn("test@test.com");

        when(userService.updateProfile(
                "test@test.com",
                "updated",
                "test@test.com"
        )).thenReturn(updatedUser);

        String result =
                userController.updateProfile(
                        authentication,
                        "updated",
                        session
                );

        assertEquals(
                "redirect:/profile?success",
                result
        );

        verify(session)
                .setAttribute(
                        "currentUsername",
                        "updated"
                );
    }
}
