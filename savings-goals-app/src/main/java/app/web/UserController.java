package app.web;

import app.model.dto.user.UserDto;
import app.model.dto.user.UserLoginRequest;
import app.model.dto.user.UserRegisterRequest;
import app.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String getLoginPage(@RequestParam(value = "error", required = false) String error,
                               Model model,
                               HttpSession session) {

        UserLoginRequest userLoginRequest = new UserLoginRequest();

        String savedEmail = (String) session.getAttribute("loginEmail");

        if (savedEmail != null) {
            userLoginRequest.setEmail(savedEmail);
            session.removeAttribute("loginEmail");
        }

        model.addAttribute("userLoginRequest", userLoginRequest);

        if (error != null) {
            model.addAttribute("error", "Wrong email or password");
        }

        return "login";
    }

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("userRegisterRequest", new UserRegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute UserRegisterRequest userRegisterRequest,
                           BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(userRegisterRequest);

        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String getProfilePage(Authentication authentication,
                                 Model model) {

        UserDto user = userService.getUserByEmail(authentication.getName());

        model.addAttribute("user", user);

        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @RequestParam String username,
                                HttpSession session) {

        String currentEmail = authentication.getName();

        UserDto updatedUser = userService.updateProfile(
                currentEmail,
                username,
                currentEmail
        );

        session.setAttribute("currentUsername", updatedUser.getUsername());

        return "redirect:/profile?success";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(Authentication authentication,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute(
                    "passwordError",
                    "New password and confirmation do not match."
            );

            return "redirect:/profile";
        }

        try {
            userService.changePassword(
                    authentication.getName(),
                    currentPassword,
                    newPassword
            );

            redirectAttributes.addFlashAttribute(
                    "passwordSuccess",
                    "Password changed successfully."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "passwordError",
                    exception.getMessage()
            );
        }

        return "redirect:/profile";
    }
}