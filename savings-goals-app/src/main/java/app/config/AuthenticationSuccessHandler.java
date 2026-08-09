package app.config;

import app.model.entity.user.User;
import app.repository.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationSuccessHandler
        implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public AuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();

        request.getSession().setAttribute("currentUserId", user.getId());
        request.getSession().setAttribute("currentUsername", user.getUsername());
        request.getSession().setAttribute("currentUserEmail", user.getEmail());
        request.getSession().setAttribute("currentUserRole", user.getRole());

        response.sendRedirect("/home");
    }
}