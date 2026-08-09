package app.service.user;

import app.mapper.user.UserMapper;
import app.model.dto.user.UserDto;
import app.model.dto.user.UserLoginRequest;
import app.model.dto.user.UserRegisterRequest;
import app.model.entity.user.User;
import app.model.entity.user.UserRole;
import app.repository.savingsgoal.SavingsGoalRepository;
import app.repository.transaction.TransactionRepository;
import app.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionRepository transactionRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TransactionRepository transactionRepository,
                       SavingsGoalRepository savingsGoalRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionRepository = transactionRepository;
        this.savingsGoalRepository = savingsGoalRepository;
    }

    public UserDto register(UserRegisterRequest userRegisterRequest) {

        User user = UserMapper.toUserEntity(userRegisterRequest);

        user.setPassword(
                passwordEncoder.encode(
                        userRegisterRequest.getPassword()
                )
        );

        user.setRole(UserRole.USER);

        User savedUser = userRepository.save(user);

        LOGGER.info(
                "User registered successfully. Id: {}, username: {}, email: {}",
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );

        return UserMapper.toUserDto(savedUser);
    }

    public UserDto login(UserLoginRequest userLoginRequest) {

        User user = userRepository
                .findByEmail(userLoginRequest.getEmail())
                .orElse(null);

        if (user == null) {

            LOGGER.warn(
                    "Login failed. User with email {} was not found",
                    userLoginRequest.getEmail()
            );

            return null;
        }

        if (!passwordEncoder.matches(
                userLoginRequest.getPassword(),
                user.getPassword())) {

            LOGGER.warn(
                    "Login failed due to invalid password for user: {}",
                    user.getEmail()
            );

            return null;
        }

        LOGGER.info(
                "User logged in successfully. Id: {}, email: {}",
                user.getId(),
                user.getEmail()
        );

        return UserMapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOGGER.warn(
                            "User with email {} was not found",
                            email
                    );

                    return new IllegalArgumentException(
                            "User not found"
                    );
                });

        return UserMapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {

        LOGGER.info("Loading all users");

        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public UserDto updateProfile(String currentEmail,
                                 String username,
                                 String email) {

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> {
                    LOGGER.warn(
                            "Profile update failed. User with email {} was not found",
                            currentEmail
                    );

                    return new IllegalArgumentException(
                            "User not found"
                    );
                });

        userRepository.findByUsername(username)
                .filter(existingUser ->
                        !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {

                    LOGGER.warn(
                            "Profile update failed. Username {} is already in use",
                            username
                    );

                    throw new IllegalArgumentException(
                            "Username is already in use"
                    );
                });

        userRepository.findByEmail(email)
                .filter(existingUser ->
                        !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {

                    LOGGER.warn(
                            "Profile update failed. Email {} is already in use",
                            email
                    );

                    throw new IllegalArgumentException(
                            "Email is already in use"
                    );
                });

        user.setUsername(username);
        user.setEmail(email);

        User updatedUser = userRepository.save(user);

        LOGGER.info(
                "User profile updated successfully. Id: {}, username: {}, email: {}",
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail()
        );

        return UserMapper.toUserDto(updatedUser);
    }

    public void changePassword(String email,
                               String currentPassword,
                               String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOGGER.warn(
                            "Password change failed. User with email {} was not found",
                            email
                    );

                    return new IllegalArgumentException(
                            "User not found"
                    );
                });

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPassword())) {

            LOGGER.warn(
                    "Password change failed due to invalid current password for user: {}",
                    email
            );

            throw new IllegalArgumentException(
                    "Current password is incorrect"
            );
        }

        if (!newPassword.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) {

            LOGGER.warn(
                    "Password change failed because the new password does not meet the requirements for user: {}",
                    email
            );

            throw new IllegalArgumentException(
                    "New password does not meet the password requirements"
            );
        }

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "New password must be different from the current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        LOGGER.info(
                "Password changed successfully for user with id: {}",
                user.getId()
        );
    }

    public UserDto updateUserRole(UUID userId, UserRole role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    LOGGER.warn(
                            "Role update failed. User with id {} was not found",
                            userId
                    );

                    return new IllegalArgumentException(
                            "User not found"
                    );
                });

        user.setRole(role);

        User updatedUser = userRepository.save(user);

        LOGGER.info(
                "User role updated successfully. Id: {}, new role: {}",
                updatedUser.getId(),
                updatedUser.getRole()
        );

        return UserMapper.toUserDto(updatedUser);
    }

    public void deleteUser(UUID userId) {

        if (!userRepository.existsById(userId)) {

            LOGGER.warn(
                    "User deletion failed. User with id {} was not found",
                    userId
            );

            throw new IllegalArgumentException(
                    "User not found"
            );
        }

        transactionRepository
                .deleteBySavingsGoal_User_Id(userId);

        savingsGoalRepository
                .deleteByUser_Id(userId);

        userRepository
                .deleteById(userId);

        LOGGER.info(
                "User deleted successfully. Id: {}",
                userId
        );
    }
}