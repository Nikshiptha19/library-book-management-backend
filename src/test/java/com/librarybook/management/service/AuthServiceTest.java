package com.librarybook.management.service;

import com.librarybook.management.dto.RegisterRequest;
import com.librarybook.management.entity.Role;
import com.librarybook.management.entity.User;
import com.librarybook.management.repository.UserRepository;
import com.librarybook.management.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    void registrationAlwaysCreatesStudentEvenIfClientSendsPrivilegeFields() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("  New Student  ");
        request.setEmail("  Student@Example.com ");
        request.setPassword("Student123");
        request.setPhoneNumber("123456");
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Student123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken(any())).thenReturn("token");

        authService.register(request);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("New Student", savedUser.getValue().getFullName());
        assertEquals("student@example.com", savedUser.getValue().getEmail());
        assertEquals("encoded-password", savedUser.getValue().getPasswordHash());
        assertEquals(Role.STUDENT, savedUser.getValue().getRole());
    }
}
