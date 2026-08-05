package app.web;

import app.model.dto.user.UserDto;
import app.model.dto.user.UserLoginRequest;
import app.model.dto.user.UserRegisterRequest;
import app.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String getLoginPage(Model model) {
        model.addAttribute("userLoginRequest", new UserLoginRequest());
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

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute UserLoginRequest userLoginRequest,
                        BindingResult bindingResult,
                        Model model,
                        HttpSession session) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("userLoginRequest", userLoginRequest);
            return "login";
        }

        UserDto userDto = userService.login(userLoginRequest);

        if (userDto == null) {
            model.addAttribute("userLoginRequest", userLoginRequest);
            model.addAttribute("error", "Wrong email or password");
            return "login";
        }

        session.setAttribute("currentUserId", userDto.getId());
        session.setAttribute("currentUsername", userDto.getUsername());

        return "redirect:/home";
    }
}
