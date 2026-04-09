package com.mangaon.recycleers.controller;

import com.mangaon.recycleers.dto.UserRequest;
import com.mangaon.recycleers.service.JwtService;
import com.mangaon.recycleers.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public record LoginRequest(String username, String password) {}

    // ✅ LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            UserDetails user = (UserDetails) authentication.getPrincipal();

            String role = user.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_USER");

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role);

            String token = jwtService.generateToken(user, claims);

            Map<String, Object> body = new HashMap<>();
            body.put("token", token);
            body.put("username", user.getUsername());
            body.put("role", role);

            return ResponseEntity.ok(body);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    // ✅ REGISTER FIRST ADMIN (PUBLIC — no token required)
    // This is the correct endpoint to call from your Create Admin page.
    // It will fail with 400 if an admin already exists.
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody UserRequest request) {
        try {
            return ResponseEntity.ok(userService.createAdmin(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }
}