package com.travelvista.service;

import com.travelvista.config.JwtUtil;
import com.travelvista.dto.LoginRequest;
import com.travelvista.dto.LoginResponse;
import com.travelvista.model.Role;
import com.travelvista.model.User;
import com.travelvista.repository.RoleRepository;
import com.travelvista.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil, OtpService otpService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
    }

    // ─────────────────────────────────────────────────────────────
    // Legacy admin login — used by AuthController (/api/admin/login)
    // Admin/editor/sales accounts keep the existing behavior: email +
    // password only, no OTP. Never returns a token for inactive users.
    // ─────────────────────────────────────────────────────────────
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null || !user.getIsActive()) {
            return null;
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return null;
        }

        String roleName = user.getRole() != null ? user.getRole().getName() : "user";
        String token = jwtUtil.generateToken(user.getEmail(), roleName, user.getName());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                roleName,
                user.getProfileImage()
        );

        return new LoginResponse(token, userInfo);
    }

    // ─────────────────────────────────────────────────────────────
    // CUSTOMER REGISTRATION — creates the user as INACTIVE and sends a
    // REGISTER OTP. No token is issued here. The user becomes active only
    // after OTP verification (activateUserByPurpose).
    // Throws OtpDeliveryException if the email cannot be sent (caller maps
    // it to an API error) — registration must never look successful when
    // the OTP email failed to go out.
    // ─────────────────────────────────────────────────────────────
    public User registerCustomer(String name, String email, String phone, String password) {

        Role customerRole = roleRepository.findByName("customer")
                .orElseGet(() -> roleRepository.save(new Role("customer", "Regular website user")));

        User user = new User(name, email, phone, passwordEncoder.encode(password), customerRole);
        user.setIsActive(false); // inactive until REGISTER OTP is verified
        User saved = userRepository.save(user);

        try {
            otpService.generateAndSendOtp(saved.getEmail(), "REGISTER",
                    OtpService.RECORD_ID_NONE, OtpService.RECORD_TYPE_ACCOUNT);
        } catch (RuntimeException e) {
            // Roll the inactive user back out so a failed email doesn't leave
            // the account in a state where re-registration is blocked by the
            // unique email constraint with no way to receive a new OTP.
            userRepository.delete(saved);
            throw e;
        }

        log.info("New customer registered (inactive pending OTP): email={}", saved.getEmail());
        return saved;
    }

    // ─────────────────────────────────────────────────────────────
    // REGISTRATION OTP VERIFICATION — activates the user.
    // Returns null when the code is wrong/expired.
    // ─────────────────────────────────────────────────────────────
    public User verifyRegistrationOtp(String email, String code) {
        boolean ok = otpService.verifyAuthOtp(email, "REGISTER", code);
        if (!ok) {
            return null;
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            user.setIsActive(true);
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        }

        log.info("Registration OTP verified, account activated: email={}", email);
        return user;
    }

    /**
     * Resend the REGISTER OTP (rate limiting is enforced inside OtpService).
     */
    public void resendRegistrationOtp(String email) {
        otpService.generateAndSendOtp(email, "REGISTER",
                OtpService.RECORD_ID_NONE, OtpService.RECORD_TYPE_ACCOUNT);
    }

    // ─────────────────────────────────────────────────────────────
    // CUSTOMER LOGIN — step 1: check credentials, then decide whether a
    // LOGIN OTP is required (never issued a token in that case).
    //
    // Login OTP is required when there is NO successful LOGIN OTP
    // verification within the last 24 hours — this covers both the first
    // login after registration and every login after a 24h gap.
    // Returns null when credentials are invalid or the account is inactive.
    // ─────────────────────────────────────────────────────────────
    public LoginResponse beginCustomerLogin(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            return null;
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return null;
        }

        String roleName = user.getRole() != null ? user.getRole().getName() : "customer";

        boolean otpRequired = !otpService.isLoginWithin24h(user.getEmail());

        if (otpRequired) {
            try {
                otpService.generateAndSendOtp(user.getEmail(), "LOGIN",
                        OtpService.RECORD_ID_NONE, OtpService.RECORD_TYPE_ACCOUNT);
            } catch (RuntimeException e) {
                log.error("Login OTP could not be delivered for {}", user.getEmail(), e);
                throw e; // controller maps this to an API error — no partial login
            }

            log.info("Login OTP required (no verification in last 24h): email={}", user.getEmail());
            return new LoginResponse(true,
                    "Login OTP sent to your email. Please verify to complete login.");
        }

        // Within 24h of last successful LOGIN OTP — straight token (existing behavior)
        return buildLoginResponse(user, roleName);
    }

    // ─────────────────────────────────────────────────────────────
    // CUSTOMER LOGIN — step 2: verify the LOGIN OTP and finish the login.
    // Credentials were already validated in beginCustomerLogin for this
    // email, so a correct OTP completes the login with a real token.
    // Returns null on wrong/expired/used code.
    // ─────────────────────────────────────────────────────────────
    public LoginResponse completeCustomerLogin(String email, String code) {

        boolean ok = otpService.verifyAuthOtp(email, "LOGIN", code);
        if (!ok) {
            return null;
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            return null;
        }

        String roleName = user.getRole() != null ? user.getRole().getName() : "customer";
        log.info("Login OTP verified, login completed: email={}", email);
        return buildLoginResponse(user, roleName);
    }

    /**
     * Resend the LOGIN OTP (used when the user clicks "resend" on the OTP screen).
     */
    public void resendLoginOtp(String email) {
        otpService.generateAndSendOtp(email, "LOGIN",
                OtpService.RECORD_ID_NONE, OtpService.RECORD_TYPE_ACCOUNT);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // ─────────────────────────────────────────────────────────────
    // Shared response builder (same shape the frontend already knows).
    // ─────────────────────────────────────────────────────────────
    private LoginResponse buildLoginResponse(User user, String roleName) {
        String token = jwtUtil.generateToken(user.getEmail(), roleName, user.getName());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                roleName,
                user.getProfileImage()
        );

        return new LoginResponse(token, userInfo);
    }
}
