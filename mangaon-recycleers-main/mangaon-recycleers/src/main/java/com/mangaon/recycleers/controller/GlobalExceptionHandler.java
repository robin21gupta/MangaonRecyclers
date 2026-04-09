package com.mangaon.recycleers.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex) {

        Map<String, Object> body = new HashMap<>();
        body.put("error",  "Access denied — you do not have permission to perform this action");
        body.put("status", 403);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * ── 401 Bad Credentials ──────────────────────────────────────────────
     * Handles wrong username/password during login.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex) {

        Map<String, Object> body = new HashMap<>();
        body.put("error",  "Invalid username or password");
        body.put("status", 401);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * ── 404 Not Found ────────────────────────────────────────────────────
     * Handles supplier/client/entry not found errors thrown by services.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            NoSuchElementException ex) {

        Map<String, Object> body = new HashMap<>();
        body.put("error",  ex.getMessage() != null ? ex.getMessage() : "Resource not found");
//        body.put("error",  "Resource not found");
        body.put("status", 404);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * ── 409 Conflict ─────────────────────────────────────────────────────
     * Handles duplicate entry / FK constraint violations from the DB.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        String msg = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : "Data integrity violation — possible duplicate entry";

        Map<String, Object> body = new HashMap<>();
        body.put("error",  msg);
        body.put("status", 409);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * ── 400 File too large ───────────────────────────────────────────────
     * Handles invoice photo upload exceeding the configured max size.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileTooLarge(
            MaxUploadSizeExceededException ex) {

        Map<String, Object> body = new HashMap<>();
        body.put("error",  "File size exceeds the maximum allowed limit (10 MB)");
        body.put("status", 400);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        Map<String, Object> body = new HashMap<>();
        body.put("error",  "Validation failed");
        body.put("detail", message);
        body.put("status", 400);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * ── 500 Catch-all ────────────────────────────────────────────────────
     * Catch-all for unexpected runtime errors — returns JSON instead of
     * HTML so the frontend can display something useful.
     *
     * NOTE: AccessDeniedException, BadCredentialsException etc. are handled
     * by their own specific handlers above, so they never reach here.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {

        // Print stack trace so you can see the real cause in the server console
        ex.printStackTrace();

        Map<String, Object> body = new HashMap<>();
        body.put("error",  ex.getMessage() != null ? ex.getMessage() : "Internal error");
        body.put("status", 500);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}