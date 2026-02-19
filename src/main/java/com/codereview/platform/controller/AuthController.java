package com.codereview.platform.controller;

import com.codereview.platform.dto.RegisterRequest;
import com.codereview.platform.dto.UserDTO;
import com.codereview.platform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    //Dependency injection
    private final AuthService authService;

    //Register endpoint
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register (@Valid @RequestBody RegisterRequest request){
        log.info("Received registration request for email: {}",request.getEmail());

        //Call AuthService
        UserDTO userDTO = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);

    }

    //Health check
    @GetMapping("/health")
    public ResponseEntity<String> getHealth(){
        return ResponseEntity.ok("Auth service is running");
    }

}
