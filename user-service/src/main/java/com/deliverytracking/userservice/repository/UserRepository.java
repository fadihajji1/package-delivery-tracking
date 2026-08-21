package com.deliverytracking.userservice.repository;

import com.deliverytracking.userservice.model.User;
import com.deliverytracking.userservice.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleAndAvailableTrue(UserRole role);
}