package app.web;

import app.model.entity.user.UserRole;
import app.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users")
    public String getUsersPage(Model model) {

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", UserRole.values());

        return "admin-users";
    }

    @PostMapping("/admin/users/{id}/role")
    public String updateUserRole(@PathVariable UUID id,
                                 @RequestParam UserRole role) {

        userService.updateUserRole(id, role);

        return "redirect:/admin/users?success";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable UUID id) {

        userService.deleteUser(id);

        return "redirect:/admin/users?deleted";
    }
}