package com.mangaon.recycleers.service;

import com.mangaon.recycleers.dto.UserRequest;
import com.mangaon.recycleers.dto.UserResponse;
import com.mangaon.recycleers.enums.Role;
import com.mangaon.recycleers.model.User;
import com.mangaon.recycleers.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ GET ALL USERS
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    // ✅ GET USER BY ID
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    // ✅ CREATE USER (called only by authenticated ADMIN/ACCOUNT via POST /api/users)
    @Transactional
    public UserResponse createUser(UserRequest req) {
        validateUsernameAndPassword(req);

        if (userRepository.existsByUsername(req.username().trim()))
            throw new IllegalArgumentException("Username already exists");

        Role role = req.role() != null ? req.role() : Role.USER;

        User user = new User(
                req.username().trim(),
                passwordEncoder.encode(req.password()),
                role
        );

        return UserResponse.from(userRepository.save(user));
    }

    // ✅ CREATE FIRST ADMIN (called publicly via POST /api/auth/register-admin)
    @Transactional
    public UserResponse createAdmin(UserRequest req) {
        validateUsernameAndPassword(req);


        User user = new User(
                req.username().trim(),
                passwordEncoder.encode(req.password()),
                Role.ADMIN
        );

        return UserResponse.from(userRepository.save(user));
    }

    // ✅ UPDATE USER
    @Transactional
    public UserResponse updateUser(Long id, UserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (req.username() == null || req.username().isBlank())
            throw new IllegalArgumentException("Username is required");

        if (!user.getUsername().equalsIgnoreCase(req.username().trim()) &&
                userRepository.existsByUsername(req.username().trim())) {
            throw new IllegalArgumentException("Username already taken");
        }

        user.setUsername(req.username().trim());

        if (req.role() != null) {
            user.setRole(req.role());
        }

        if (req.password() != null && !req.password().isBlank()) {
            if (req.password().length() < 6)
                throw new IllegalArgumentException("New password must be at least 6 characters");
            user.setPassword(passwordEncoder.encode(req.password()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    // ✅ DELETE USER
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new IllegalArgumentException("User not found: " + id);
        userRepository.deleteById(id);
    }

    // ✅ COUNT USERS
    @Transactional(readOnly = true)
    public long getUserCount() {
        return userRepository.count();
    }

    // ── Private helpers ────────────────────────────────────────────────────────
    private void validateUsernameAndPassword(UserRequest req) {
        if (req.username() == null || req.username().isBlank())
            throw new IllegalArgumentException("Username is required");
        if (req.password() == null || req.password().length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
    }
}