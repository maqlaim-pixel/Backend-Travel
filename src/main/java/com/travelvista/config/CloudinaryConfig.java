// package com.travelvista.config;

// import com.cloudinary.Cloudinary;
// import com.cloudinary.utils.ObjectUtils;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// import java.util.HashMap;
// import java.util.Map;

// @Configuration
// public class CloudinaryConfig {

//     @Value("${cloudinary.cloud_name:}")
//     private String cloudName;

//     @Value("${cloudinary.api_key:}")
//     private String apiKey;

//     @Value("${cloudinary.api_secret:}")
//     private String apiSecret;

//     @Bean
//     public Cloudinary cloudinary() {
//         Map<String, String> config = new HashMap<>();
//         config.put("cloud_name", cloudName);
//         config.put("api_key", apiKey);
//         config.put("api_secret", apiSecret);
//         return new Cloudinary(config);
//     }
// }
package com.travelvista.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    /*
     * Preferred configuration:
     *
     * CLOUDINARY_URL=cloudinary://API_KEY:API_SECRET@CLOUD_NAME
     *
     * Railway environment variable:
     * CLOUDINARY_URL
     */

    @Value("${CLOUDINARY_URL:}")
    private String cloudinaryUrl;

    /*
     * Optional fallback configuration.
     *
     * These are useful if CLOUDINARY_URL is not available.
     */
    @Value("${cloudinary.cloud_name:}")
    private String cloudName;

    @Value("${cloudinary.api_key:}")
    private String apiKey;

    @Value("${cloudinary.api_secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {

        /*
         * ============================================================
         * OPTION 1: CLOUDINARY_URL
         * ============================================================
         */

        if (cloudinaryUrl != null &&
                !cloudinaryUrl.trim().isEmpty()) {

            String url = cloudinaryUrl.trim();

            // Do not print the URL because it contains API secret.
            System.out.println(
                    "Cloudinary configuration: CLOUDINARY_URL detected"
            );

            return new Cloudinary(url);
        }

        /*
         * ============================================================
         * OPTION 2: Individual environment variables
         * ============================================================
         */

        if (cloudName != null &&
                !cloudName.trim().isEmpty() &&
                apiKey != null &&
                !apiKey.trim().isEmpty() &&
                apiSecret != null &&
                !apiSecret.trim().isEmpty()) {

            System.out.println(
                    "Cloudinary configuration: individual credentials detected"
            );

            return new Cloudinary(
                    cloudName.trim(),
                    apiKey.trim(),
                    apiSecret.trim()
            );
        }

        /*
         * ============================================================
         * No configuration
         * ============================================================
         */

        System.err.println(
                "WARNING: Cloudinary credentials are not configured."
        );

        return new Cloudinary();
    }
}
