package com.mangaon.recycleers.controller;

import com.mangaon.recycleers.dto.UserRequest;
import com.mangaon.recycleers.dto.UserResponse;
import com.mangaon.recycleers.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ✅ GET ALL USERS
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','OPERATOR','ADMIN','ACCOUNT')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ✅ GET USER BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','OPERATOR','ADMIN','ACCOUNT')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUser(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ CREATE USER (ADMIN / ACCOUNT ONLY)
    // NOTE: Do NOT call this for admin registration.
    //       Use POST /api/auth/register-admin instead.
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNT')")
    public ResponseEntity<?> createUser(@RequestBody UserRequest userRequest) {
        try {
            return new ResponseEntity<>(userService.createUser(userRequest), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ UPDATE USER
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNT')")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody UserRequest details) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, details));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ DELETE USER
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNT')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ COUNT USERS
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNT')")
    public ResponseEntity<Long> getUserCount() {
        return ResponseEntity.ok(userService.getUserCount());
    }
}