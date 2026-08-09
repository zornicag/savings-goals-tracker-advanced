package app.web;

import app.model.dto.user.UserDto;
import app.model.entity.user.UserRole;
import app.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private AdminController adminController;

    @Test
    void getUsersPage_ShouldLoadUsersAndRoles() {
        List<UserDto> users = List.of();

        when(userService.getAllUsers()).thenReturn(users);

        String result = adminController.getUsersPage(model);

        assertEquals("admin-users", result);

        verify(model).addAttribute("users", users);
        verify(model).addAttribute("roles", UserRole.values());
    }

    @Test
    void updateUserRole_ShouldUpdateRoleAndRedirect() {
        UUID id = UUID.randomUUID();

        String result =
                adminController.updateUserRole(id, UserRole.ADMIN);

        assertEquals("redirect:/admin/users?success", result);

        verify(userService).updateUserRole(id, UserRole.ADMIN);
    }
}
