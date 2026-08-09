package app.service.user;

import app.model.dto.user.UserDto;
import app.model.dto.user.UserLoginRequest;
import app.model.dto.user.UserRegisterRequest;
import app.model.entity.user.User;
import app.model.entity.user.UserRole;
import app.repository.savingsgoal.SavingsGoalRepository;
import app.repository.transaction.TransactionRepository;
import app.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void register_ShouldCreateUserWithEncodedPasswordAndUserRole() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@test.com");
        request.setPassword("Password1!");

        when(passwordEncoder.encode("Password1!"))
                .thenReturn("encoded");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.register(request);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@test.com", result.getEmail());

        verify(passwordEncoder).encode("Password1!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_ShouldReturnUser_WhenCredentialsAreCorrect() {
        User user = createUser();

        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("Password1!");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("Password1!", "encoded"))
                .thenReturn(true);

        UserDto result = userService.login(request);

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void login_ShouldReturnNull_WhenUserDoesNotExist() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("missing@test.com");
        request.setPassword("Password1!");

        when(userRepository.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        UserDto result = userService.login(request);

        assertNull(result);
    }

    @Test
    void login_ShouldReturnNull_WhenPasswordIsIncorrect() {
        User user = createUser();

        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("WrongPassword1!");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("WrongPassword1!", "encoded"))
                .thenReturn(false);

        UserDto result = userService.login(request);

        assertNull(result);
    }

    @Test
    void getUserByEmail_ShouldReturnUser() {
        User user = createUser();

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        UserDto result = userService.getUserByEmail("test@test.com");

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void getUserByEmail_ShouldThrow_WhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserByEmail("missing@test.com")
        );
    }

    @Test
    void getAllUsers_ShouldReturnUsers() {
        when(userRepository.findAll())
                .thenReturn(List.of(createUser()));

        List<UserDto> result = userService.getAllUsers();

        assertEquals(1, result.size());
    }

    @Test
    void updateUserRole_ShouldUpdateRole() {
        UUID id = UUID.randomUUID();

        User user = createUser();
        user.setId(id);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        UserDto result =
                userService.updateUserRole(id, UserRole.ADMIN);

        assertEquals(UserRole.ADMIN, result.getRole());
    }

    @Test
    void updateUserRole_ShouldThrow_WhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUserRole(id, UserRole.ADMIN)
        );
    }

    @Test
    void changePassword_ShouldChangePassword() {
        User user = createUser();

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("OldPassword1!", "encoded"))
                .thenReturn(true);

        when(passwordEncoder.matches("NewPassword1!", "encoded"))
                .thenReturn(false);

        when(passwordEncoder.encode("NewPassword1!"))
                .thenReturn("newEncoded");

        userService.changePassword(
                "test@test.com",
                "OldPassword1!",
                "NewPassword1!"
        );

        assertEquals("newEncoded", user.getPassword());

        verify(userRepository).save(user);
    }

    @Test
    void changePassword_ShouldThrow_WhenCurrentPasswordIsWrong() {
        User user = createUser();

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("WrongPassword1!", "encoded"))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(
                        "test@test.com",
                        "WrongPassword1!",
                        "NewPassword1!"
                )
        );
    }

    @Test
    void deleteUser_ShouldDeleteUserAndRelatedData() {
        UUID id = UUID.randomUUID();

        when(userRepository.existsById(id))
                .thenReturn(true);

        userService.deleteUser(id);

        verify(transactionRepository)
                .deleteBySavingsGoal_User_Id(id);

        verify(savingsGoalRepository)
                .deleteByUser_Id(id);

        verify(userRepository)
                .deleteById(id);
    }

    @Test
    void deleteUser_ShouldThrow_WhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(userRepository.existsById(id))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUser(id)
        );
    }

    private User createUser() {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("encoded");
        user.setRole(UserRole.USER);

        return user;
    }
}
