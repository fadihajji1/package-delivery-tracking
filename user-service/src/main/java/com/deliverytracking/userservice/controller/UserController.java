package com.deliverytracking.userservice.controller;

import com.deliverytracking.userservice.dto.CreateUserRequest;
import com.deliverytracking.userservice.dto.UpdateAvailabilityRequest;
import com.deliverytracking.userservice.dto.UserResponse;
import com.deliverytracking.userservice.model.User;
import com.deliverytracking.userservice.model.UserRole;
import com.deliverytracking.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        if (request.available() != null) {
            user.setAvailable(request.available());
        }

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(toResponse(user));
    }

    @GetMapping("/agents/available")
    public ResponseEntity<java.util.List<UserResponse>> getAvailableAgents() {
        return ResponseEntity.ok(userRepository.findByRoleAndAvailableTrue(UserRole.AGENT)
                .stream().map(this::toResponse).toList());
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<UserResponse> updateAvailability(
            @PathVariable Long id, @Valid @RequestBody UpdateAvailabilityRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != UserRole.AGENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only agents have availability");
        }
        user.setAvailable(request.available());
        return ResponseEntity.ok(toResponse(userRepository.save(user)));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getRole(), user.isAvailable(), user.getCreatedAt());
    }
}