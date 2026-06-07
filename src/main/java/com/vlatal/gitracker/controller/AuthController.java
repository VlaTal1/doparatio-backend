package com.vlatal.gitracker.controller;

import com.vlatal.gitracker.serivce.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final JwtService jwtService;

    /**
     * Validate the JWT token from frontend
     * Frontend will handle Supabase authentication and send JWT to backend
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer "

            if (jwtService.validateToken(token)) {
                String role = jwtService.extractRole(token);
                String personId = jwtService.extractPersonId(token);
                String personName = jwtService.extractName(token);

                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("role", role);
                response.put("personId", personId);
                response.put("personName", personName);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401).body(Map.of("valid", false, "message", "Invalid token"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("valid", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "message", "Token validation failed"));
        }
    }

}
