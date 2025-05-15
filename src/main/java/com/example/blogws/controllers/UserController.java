package com.example.blogws.controllers;

import com.example.blogws.dto.UserRegistrationDto;
import com.example.blogws.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Add an empty DTO to the model for form binding
        model.addAttribute("userRegistrationDto", new UserRegistrationDto());
        return "pages/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("userRegistrationDto") UserRegistrationDto registrationDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            // If there are validation errors, return to the registration page
            return "pages/register";
        }

        // Attempt to register the user
        Map<String, Object> registrationResult = userService.registerNewUser(registrationDto);

        if (!(boolean) registrationResult.get("success")) {
            // If registration fails, add error message and return to registration page
            model.addAttribute("errorMessage", registrationResult.get("message"));
            return "pages/register";
        }

        // If registration is successful, redirect to login page with success message
        redirectAttributes.addFlashAttribute("successMessage", registrationResult.get("message"));
        return "redirect:/login";
    }
}