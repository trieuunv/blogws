package com.example.blogws.services;

import com.example.blogws.dto.UserRegistrationDto;
import com.example.blogws.models.User;
import com.example.blogws.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Map<String, Object> registerNewUser(UserRegistrationDto registrationDto) {
        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            response.put("success", false);
            response.put("message", "Tên đăng nhập đã tồn tại");
            return response;
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            response.put("success", false);
            response.put("message", "Email đã được sử dụng");
            return response;
        }

        // Check password confirmation
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            response.put("success", false);
            response.put("message", "Mật khẩu xác nhận không khớp");
            return response;
        }

        // Create new user
        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setEmail(registrationDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setRole("USER"); // Default role

        // Save user
        userRepository.save(newUser);

        response.put("success", true);
        response.put("message", "Đăng kí thành công");
        return response;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}