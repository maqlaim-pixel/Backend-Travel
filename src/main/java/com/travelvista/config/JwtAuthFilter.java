// package com.travelvista.config;

// import com.travelvista.model.User;
// import com.travelvista.repository.UserRepository;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;
// import java.util.List;

// @Component
// public class JwtAuthFilter extends OncePerRequestFilter {

//     private final JwtUtil jwtUtil;
//     private final UserRepository userRepository;

//     public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
//         this.jwtUtil = jwtUtil;
//         this.userRepository = userRepository;
//     }

//     @Override
//     protected void doFilterInternal(HttpServletRequest request,
//                                     HttpServletResponse response,
//                                     FilterChain filterChain) throws ServletException, IOException {
//         String header = request.getHeader("Authorization");

//         if (header != null && header.startsWith("Bearer ")) {
//             String token = header.substring(7);
//             try {
//                 if (jwtUtil.validateToken(token)) {
//                     String email = jwtUtil.extractEmail(token);
//                     String role = jwtUtil.extractRole(token);

//                     User user = userRepository.findByEmail(email).orElse(null);
//                     if (user != null && user.getIsActive()) {
//                         var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
//                         var auth = new UsernamePasswordAuthenticationToken(
//                                 user, null, authorities);
//                         SecurityContextHolder.getContext().setAuthentication(auth);
//                     }
//                 }
//             } catch (Exception ignored) {
//                 // Invalid token — continue without auth
//             }
//         }

//         filterChain.doFilter(request, response);
//     }
// }

package com.travelvista.config;

import com.travelvista.model.User;
import com.travelvista.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(
            JwtUtil jwtUtil,
            UserRepository userRepository
    ) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        // =====================================================
        // 1. CORS PREFLIGHT
        // =====================================================
        // OPTIONS request should NEVER go through JWT validation
        // =====================================================

        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // =====================================================
        // 2. ADMIN LOGIN
        // =====================================================
        // Login happens before JWT is created.
        // Therefore JWT validation is not required here.
        // =====================================================

        if ("/api/admin/login".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // =====================================================
        // 3. GET ADMIN ME
        // =====================================================
        // Your SecurityConfig currently permits this endpoint.
        // Keep it out of JWT filter if no token is supplied.
        // =====================================================

        String header = request.getHeader("Authorization");

        // =====================================================
        // 4. NO AUTHORIZATION HEADER
        // =====================================================

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // =====================================================
        // 5. EXTRACT JWT
        // =====================================================

        String token = header.substring(7).trim();

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // =====================================================
        // 6. VALIDATE JWT
        // =====================================================

        try {

            if (jwtUtil.validateToken(token)) {

                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                // -------------------------------------------------
                // Find user
                // -------------------------------------------------

                User user = userRepository
                        .findByEmail(email)
                        .orElse(null);

                // -------------------------------------------------
                // Check active user
                // -------------------------------------------------

                if (user != null && Boolean.TRUE.equals(user.getIsActive())) {

                    // -------------------------------------------------
                    // ROLE
                    // -------------------------------------------------

                    var authorities = List.of(
                        new SimpleGrantedAuthority(
                            "ROLE_" + role
                        )
                    );

                    // -------------------------------------------------
                    // AUTHENTICATION
                    // -------------------------------------------------

                    var authentication =
                        new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities
                        );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (Exception ignored) {

            // -----------------------------------------------------
            // Invalid JWT
            // -----------------------------------------------------
            // Do not crash the request.
            // Spring Security will decide whether authentication
            // is required for the requested endpoint.
            // -----------------------------------------------------

            SecurityContextHolder
                    .clearContext();
        }

        // =====================================================
        // 7. CONTINUE REQUEST
        // =====================================================

        filterChain.doFilter(request, response);
    }
}
