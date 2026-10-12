package com.librarybook.management.service;

import com.librarybook.management.entity.Role;
import com.librarybook.management.entity.User;
import com.librarybook.management.dto.ProfileUpdateRequest;
import com.librarybook.management.exception.BusinessException;
import com.librarybook.management.exception.ResourceNotFoundException;
import com.librarybook.management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("User is not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateCurrentUser(ProfileUpdateRequest updates) {
        User current = getCurrentUser();
        if (updates.getFullName() != null) current.setFullName(updates.getFullName());
        if (updates.getPhoneNumber() != null) current.setPhoneNumber(updates.getPhoneNumber());
        return userRepository.save(current);
    }

    public User updateUserStatus(Long id, boolean active) {
        User user = getUserById(id);
        user.setActive(active);
        return userRepository.save(user);
    }

    public User updateUserRole(Long id, Role role) {
        User user = getUserById(id);
        user.setRole(role);
        return userRepository.save(user);
    }
}
