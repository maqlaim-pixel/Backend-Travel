// package com.travelvista.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// import java.util.List;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {

//     private final JwtAuthFilter jwtAuthFilter;

//     public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
//         this.jwtAuthFilter = jwtAuthFilter;
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//             .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//             .csrf(csrf -> csrf.disable())
//             .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//             .authorizeHttpRequests(auth -> auth
//                 // ── Public endpoints (no auth needed) ──
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/admin/login")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/admin/me")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/packages/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/destinations/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotels/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/activities/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/blogs/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/testimonials")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/faqs")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/settings")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/dashboard/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher("/api/images/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/leads/public/submit")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/health")).permitAll()
//                 // ── Everything else under /api requires authentication ──
//                 .anyRequest().authenticated()
//             )
//             .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

//         return http.build();
//     }

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration config = new CorsConfiguration();
//         config.setAllowedOriginPatterns(List.of("*"));
//         config.setAllowedOrigins(List.of());
//         config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//         config.setAllowedHeaders(List.of("*"));
//         config.setAllowCredentials(true);

//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/api/**", config); // AntPathMatcher handles this fine in CorsConfigurationSource
//         return source;
//     }
// }

package com.travelvista.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

            // =================================================
            // CORS
            // =================================================
            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )

            // =================================================
            // CSRF
            // =================================================
            .csrf(csrf -> csrf.disable())

            // =================================================
            // STATELESS JWT SESSION
            // =================================================
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            // =================================================
            // AUTHORIZATION
            // =================================================
            .authorizeHttpRequests(auth -> auth

                // -------------------------------------------------
                // CORS PREFLIGHT
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.OPTIONS,
                    "/**"
                ).permitAll()

                // -------------------------------------------------
                // ADMIN LOGIN
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/admin/login"
                ).permitAll()

                // -------------------------------------------------
                // ADMIN ME
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/admin/me"
                ).permitAll()

                // -------------------------------------------------
                // AUTH APIs
                // -------------------------------------------------
                .requestMatchers(
                    "/api/auth/**"
                ).permitAll()

                // -------------------------------------------------
                // PACKAGES
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/packages/**"
                ).permitAll()

                // -------------------------------------------------
                // DESTINATIONS
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/destinations/**"
                ).permitAll()

                // -------------------------------------------------
                // HOTELS
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/hotels/**"
                ).permitAll()

                // -------------------------------------------------
                // ACTIVITIES
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/activities/**"
                ).permitAll()

                // -------------------------------------------------
                // BLOGS
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/blogs/**"
                ).permitAll()

                // -------------------------------------------------
                // TESTIMONIALS
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/testimonials"
                ).permitAll()

                // -------------------------------------------------
                // FAQS
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/faqs"
                ).permitAll()

                // -------------------------------------------------
                // SETTINGS
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/settings"
                ).permitAll()

                // -------------------------------------------------
                // DASHBOARD
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/dashboard/**"
                ).permitAll()

                // -------------------------------------------------
                // IMAGES
                // -------------------------------------------------
                .requestMatchers(
                    "/api/images/**"
                ).permitAll()

                // -------------------------------------------------
                // PUBLIC LEADS
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/leads/public/submit"
                ).permitAll()

                // -------------------------------------------------
                // HEALTH CHECK
                // -------------------------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/health"
                ).permitAll()

                // -------------------------------------------------
                // EVERYTHING ELSE
                // -------------------------------------------------
                .anyRequest().authenticated()
            )

            // =================================================
            // JWT FILTER
            // =================================================
            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    // =========================================================
    // CORS CONFIGURATION
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // -----------------------------------------------------
        // ALLOWED FRONTENDS
        // -----------------------------------------------------
        config.setAllowedOrigins(List.of(
            "https://frontend-travel-eyls-ejn44br4r-dhavalmaqlaim-5177.vercel.app",
            "http://localhost:5173",
            "http://localhost:3000"
        ));

        // -----------------------------------------------------
        // HTTP METHODS
        // -----------------------------------------------------
        config.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "PATCH",
            "OPTIONS"
        ));

        // -----------------------------------------------------
        // HEADERS
        // -----------------------------------------------------
        config.setAllowedHeaders(List.of("*"));

        // -----------------------------------------------------
        // EXPOSED HEADERS
        // -----------------------------------------------------
        config.setExposedHeaders(List.of(
            "Authorization"
        ));

        // -----------------------------------------------------
        // CREDENTIALS
        // -----------------------------------------------------
        config.setAllowCredentials(true);

        // -----------------------------------------------------
        // PREFLIGHT CACHE
        // -----------------------------------------------------
        config.setMaxAge(3600L);

        // -----------------------------------------------------
        // APPLY CORS TO ALL ENDPOINTS
        // -----------------------------------------------------
        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            config
        );

        return source;
    }
}
