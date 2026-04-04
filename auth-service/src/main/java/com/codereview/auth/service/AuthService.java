package com.codereview.auth.service;

import com.codereview.auth.dto.AuthResponse;
import com.codereview.auth.dto.LoginRequest;
import com.codereview.auth.dto.RegisterRequest;
import com.codereview.auth.dto.UserDTO;
import com.codereview.auth.entity.Role;
import com.codereview.auth.entity.User;
import com.codereview.auth.exception.InvalidCredentialsException;
import com.codereview.auth.exception.ResourceAlreadyExistsException;
import com.codereview.auth.exception.ResourceNotFoundException;
import com.codereview.auth.repository.UserRepository;
import com.codereview.auth.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserDTO register(RegisterRequest request){

        log.info("Register new user: {}", request.getEmail());

        //Validation
        if(userRepository.existsByEmail(request.getEmail())){
            throw new ResourceAlreadyExistsException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already taken");
        }

        //Create user entity from request
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER) // All new users get USER role
                .isActive(true)
                .build();

        //Save to database
        user = userRepository.save(user);

        log.info("User registered successfully: {}", user.getEmail());

        return mapToDTO(user);
    }

    //Login
    public AuthResponse login(LoginRequest request){
        log.info("Login attempt : {}", request.getEmail());

        //Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        //Verify password
        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new InvalidCredentialsException("Invalid email or password");
        }

        //Generate token and return
        String token = jwtTokenProvider.generateToken(user);

        //Convert user to DTO
        UserDTO userDTO = mapToDTO(user);

        //Build auth response
        AuthResponse response = AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(userDTO)
                .build();

        log.info("User logged in successfully: {}", user.getEmail());

        return response;
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Cacheable(value = "users", key = "#userId")
    public UserDTO getCurrentUser(Long userId){
        log.info("Fetching user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User not found with id: "+userId));
        return mapToDTO(user);
    }

}
