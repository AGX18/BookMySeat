package io.agx.bookmyseat.service;

import io.agx.bookmyseat.dto.request.CreateUserRequest;
import io.agx.bookmyseat.dto.request.LoginRequest;
import io.agx.bookmyseat.dto.response.AuthResponse;
import io.agx.bookmyseat.dto.response.UserResponse;
import io.agx.bookmyseat.entity.User;
import io.agx.bookmyseat.repository.UserRepository;
import io.agx.bookmyseat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(CreateUserRequest request) {
        UserResponse user = userService.createUser(request); // reuse existing logic

        String token = jwtService.generateToken(user.getEmail(), user.getId().toString());

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(user.getEmail(), user.getId().toString());

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}