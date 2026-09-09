package com.travelvista.controller;

import com.travelvista.dto.LoginRequest;
import com.travelvista.dto.LoginResponse;
import com.travelvista.model.User;
import com.travelvista.service.OtpDeliveryException;
import com.travelvista.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class CustomerAuthController {

    private final UserService userService;

    public CustomerAuthController(UserService userService) {
        this.userService = userService;
    }

    // STEP 1 — REGISTER: create inactive user + send REGISTER OTP.
    // The response contains NO token. Frontend must show the OTP screen.
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {

        String name = body.get("name");
        String email = body.get("email");
        String phone = body.get("phone");
        String password = body.get("password");

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
        }
        if (userService.findByEmail(email) != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is already registered"));
        }

        User user;
        try {
            user = userService.registerCustomer(name, email, phone, password);
        } catch (OtpDeliveryException e) {
            // Email could not be sent — registration must NOT look successful.
            return ResponseEntity.status(502).body(Map.of(
                    "error", "Your account could not be created because the verification email failed to send. Please try again.",
                    "emailFailed", true));
        }

        if (user == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Registration failed. Please try again."));
        }

        // Success: OTP email went out. No token is returned.
        return ResponseEntity.ok(Map.of(
                "success", true,
                "requiresOtp", true,
                "message", "OTP sent to your email",
                "email", user.getEmail(),
                "expiresInMinutes", 10
        ));
    }

    // STEP 2 — VERIFY REGISTRATION OTP: activates the account.
    @PostMapping("/verify-registration")
    public ResponseEntity<?> verifyRegistration(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String code = body.get("code");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "OTP code is required"));
        }

        User user = userService.verifyRegistrationOtp(email, code);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid or expired OTP. Please check the code or request a new one."));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "verified", true,
                "message", "Registration complete! You can now log in.",
                "email", user.getEmail()
        ));
    }

    // Resend the REGISTER OTP (rate-limited inside OtpService).
    @PostMapping("/resend-registration-otp")
    public ResponseEntity<?> resendRegistrationOtp(@RequestBody Map<String, String> body) {

        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        try {
            userService.resendRegistrationOtp(email);
        } catch (OtpDeliveryException e) {
            return ResponseEntity.status(502).body(Map.of(
                    "error", e.getMessage(),
                    "emailFailed", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "A new OTP has been sent to your email"));
    }

    // CUSTOMER LOGIN — step 1: credentials + 24h OTP gate.
    // Never returns a token when OTP is required.
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        LoginResponse response;
        try {
            response = userService.beginCustomerLogin(request);
        } catch (OtpDeliveryException e) {
            return ResponseEntity.status(502).body(Map.of(
                    "error", "We could not send the login OTP. Please try again later.",
                    "emailFailed", true));
        }

        if (response == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        // OTP step required — frontend shows the OTP screen. NO token returned.
        if (Boolean.TRUE.equals(response.getRequiresOtp())) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "requiresOtp", true,
                    "message", response.getMessage() != null
                            ? response.getMessage()
                            : "Login OTP sent to your email"));
        }

        // Within 24h of the last successful LOGIN OTP — normal authenticated response.
        return ResponseEntity.ok(response);
    }

    // CUSTOMER LOGIN — step 2: verify LOGIN OTP and receive the token.
    @PostMapping("/verify-login")
    public ResponseEntity<?> verifyLogin(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String code = body.get("code");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "OTP code is required"));
        }

        LoginResponse response;
        try {
            response = userService.completeCustomerLogin(email, code);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        if (response == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid or expired OTP. Please try again or request a new one."));
        }

        return ResponseEntity.ok(response);
    }

    // Resend the LOGIN OTP.
    @PostMapping("/resend-login-otp")
    public ResponseEntity<?> resendLoginOtp(@RequestBody Map<String, String> body) {

        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        try {
            userService.resendLoginOtp(email);
        } catch (OtpDeliveryException e) {
            return ResponseEntity.status(502).body(Map.of(
                    "error", e.getMessage(),
                    "emailFailed", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "A new login OTP has been sent to your email"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "role", user.getRole() != null ? user.getRole().getName() : "customer",
                "profileImage", user.getProfileImage() != null ? user.getProfileImage() : ""
        ));
    }
}
