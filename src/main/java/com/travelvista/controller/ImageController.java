// package com.travelvista.controller;

// import com.cloudinary.Cloudinary;
// import com.cloudinary.utils.ObjectUtils;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.File;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.UUID;

// @RestController
// @RequestMapping("/api/images")
// @CrossOrigin(origins = "*")
// public class ImageController {

//     @Autowired
//     private Cloudinary cloudinary;

//     // Fallback to local storage if Cloudinary is not configured
//     private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/images/";

//     @PostMapping("/upload")
//     public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
//         try {
//             // Check if Cloudinary is configured
//             boolean useCloudinary = isCloudinaryConfigured();

//             if (useCloudinary) {
//                 // Upload to Cloudinary
//                 Map<String, Object> params = ObjectUtils.asMap(
//                     "folder", "travelvista",
//                     "resource_type", "auto",
//                     "unique_filename", true
//                 );

//                 @SuppressWarnings("unchecked")
//                 Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);

//                 String imageUrl = (String) result.get("secure_url");
//                 String publicId = (String) result.get("public_id");

//                 return ResponseEntity.ok(Map.of(
//                     "url", imageUrl,
//                     "publicId", publicId,
//                     "originalName", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown",
//                     "size", file.getSize(),
//                     "provider", "cloudinary"
//                 ));
//             } else {
//                 // Fallback to local storage
//                 return uploadToLocal(file);
//             }
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body(Map.of("error", "Failed to upload image: " + e.getMessage()));
//         }
//     }

//     private ResponseEntity<?> uploadToLocal(MultipartFile file) throws IOException {
//         Path uploadPath = Paths.get(UPLOAD_DIR);
//         if (!Files.exists(uploadPath)) {
//             Files.createDirectories(uploadPath);
//         }

//         String originalFilename = file.getOriginalFilename();
//         String extension = "";
//         if (originalFilename != null && originalFilename.contains(".")) {
//             extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//         }
//         String filename = UUID.randomUUID().toString() + extension;

//         Path filePath = uploadPath.resolve(filename);
//         Files.copy(file.getInputStream(), filePath);

//         String imageUrl = "/api/images/" + filename;
//         return ResponseEntity.ok(Map.of(
//             "url", imageUrl,
//             "filename", filename,
//             "originalName", originalFilename != null ? originalFilename : "unknown",
//             "size", file.getSize(),
//             "provider", "local"
//         ));
//     }

//     @GetMapping("/{filename}")
//     public ResponseEntity<?> getImage(@PathVariable String filename) {
//         try {
//             Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
//             if (!Files.exists(filePath)) {
//                 return ResponseEntity.notFound().build();
//             }

//             byte[] imageBytes = Files.readAllBytes(filePath);
//             String contentType = determineContentType(filename);

//             return ResponseEntity.ok()
//                 .header("Content-Type", contentType)
//                 .header("Cache-Control", "public, max-age=31536000")
//                 .body(imageBytes);
//         } catch (IOException e) {
//             return ResponseEntity.badRequest().build();
//         }
//     }

//     @DeleteMapping("/{filename}")
//     public ResponseEntity<?> deleteImage(@PathVariable String filename) {
//         try {
//             Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
//             if (Files.exists(filePath)) {
//                 Files.delete(filePath);
//                 return ResponseEntity.ok(Map.of("message", "Image deleted"));
//             }
//             return ResponseEntity.notFound().build();
//         } catch (IOException e) {
//             return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete image"));
//         }
//     }

//     private boolean isCloudinaryConfigured() {
//         try {
//             String cloudName = System.getProperty("cloudinary.cloud_name");
//             String apiKey = System.getProperty("cloudinary.api_key");
//             String apiSecret = System.getProperty("cloudinary.api_secret");

//             if (cloudName != null && !cloudName.isEmpty() &&
//                 apiKey != null && !apiKey.isEmpty() &&
//                 apiSecret != null && !apiSecret.isEmpty()) {
//                 return true;
//             }

//             // Check environment variables
//             cloudName = System.getenv("CLOUDINARY_CLOUD_NAME");
//             apiKey = System.getenv("CLOUDINARY_API_KEY");
//             apiSecret = System.getenv("CLOUDINARY_API_SECRET");

//             return cloudName != null && !cloudName.isEmpty() &&
//                    apiKey != null && !apiKey.isEmpty() &&
//                    apiSecret != null && !apiSecret.isEmpty();
//         } catch (Exception e) {
//             return false;
//         }
//     }

//     private String determineContentType(String filename) {
//         String ext = filename.toLowerCase();
//         if (ext.endsWith(".jpg") || ext.endsWith(".jpeg")) return "image/jpeg";
//         if (ext.endsWith(".png")) return "image/png";
//         if (ext.endsWith(".gif")) return "image/gif";
//         if (ext.endsWith(".webp")) return "image/webp";
//         if (ext.endsWith(".svg")) return "image/svg+xml";
//         return "application/octet-stream";
//     }
// }
// package com.travelvista.controller;

// import com.cloudinary.Cloudinary;
// import com.cloudinary.utils.ObjectUtils;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.Collections;
// import java.util.Map;
// import java.util.UUID;

// @RestController
// @RequestMapping("/api/images")
// @CrossOrigin(origins = "*")
// public class ImageController {

//     @Autowired
//     private Cloudinary cloudinary;

//     // Local fallback storage
//     private static final String UPLOAD_DIR =
//             System.getProperty("user.dir") + "/uploads/images/";

//     /*
//     |--------------------------------------------------------------------------
//     | TEST CLOUDINARY CONNECTION
//     |--------------------------------------------------------------------------
//     */

//     @GetMapping("/cloudinary")
//     public ResponseEntity<?> testCloudinary() {

//         try {

//             // Ping Cloudinary
//             Map<?, ?> result =
//                     cloudinary.api().ping(Collections.emptyMap());

//             return ResponseEntity.ok(
//                     Map.of(
//                             "connected", true,
//                             "message",
//                             "Cloudinary connected successfully",
//                             "result",
//                             result
//                     )
//             );

//         } catch (Exception e) {

//             e.printStackTrace();

//             return ResponseEntity
//                     .internalServerError()
//                     .body(
//                             Map.of(
//                                     "connected", false,
//                                     "message",
//                                     e.getMessage() != null
//                                             ? e.getMessage()
//                                             : "Cloudinary connection failed"
//                             )
//                     );
//         }
//     }

//     /*
//     |--------------------------------------------------------------------------
//     | UPLOAD IMAGE
//     |--------------------------------------------------------------------------
//     */

//     @PostMapping("/upload")
//     public ResponseEntity<?> uploadImage(
//             @RequestParam("file") MultipartFile file
//     ) {

//         try {

//             // ------------------------------------------------------------
//             // Validate file
//             // ------------------------------------------------------------

//             if (file == null || file.isEmpty()) {

//                 return ResponseEntity
//                         .badRequest()
//                         .body(
//                                 Map.of(
//                                         "error",
//                                         "Image file is empty"
//                                 )
//                         );
//             }

//             String contentType =
//                     file.getContentType();

//             if (contentType == null ||
//                     !contentType.startsWith("image/")) {

//                 return ResponseEntity
//                         .badRequest()
//                         .body(
//                                 Map.of(
//                                         "error",
//                                         "Only image files are allowed"
//                                 )
//                         );
//             }

//             // 5 MB
//             if (file.getSize() > 5 * 1024 * 1024) {

//                 return ResponseEntity
//                         .badRequest()
//                         .body(
//                                 Map.of(
//                                         "error",
//                                         "Maximum image size is 5MB"
//                                 )
//                         );
//             }

//             // ------------------------------------------------------------
//             // Check Cloudinary
//             // ------------------------------------------------------------

//             boolean useCloudinary =
//                     isCloudinaryConfigured();

//             System.out.println(
//                     "Cloudinary configured: "
//                             + useCloudinary
//             );

//             // ------------------------------------------------------------
//             // Cloudinary upload
//             // ------------------------------------------------------------

//             if (useCloudinary) {

//                 Map<String, Object> params =
//                         ObjectUtils.asMap(
//                                 "folder",
//                                 "travelvista",

//                                 "resource_type",
//                                 "image",

//                                 "unique_filename",
//                                 true
//                         );

//                 Map<?, ?> result =
//                         cloudinary
//                                 .uploader()
//                                 .upload(
//                                         file.getBytes(),
//                                         params
//                                 );

//                 String imageUrl =
//                         (String) result.get(
//                                 "secure_url"
//                         );

//                 String publicId =
//                         (String) result.get(
//                                 "public_id"
//                         );

//                 if (imageUrl == null ||
//                         imageUrl.isBlank()) {

//                     throw new RuntimeException(
//                             "Cloudinary did not return secure_url"
//                     );
//                 }

//                 return ResponseEntity.ok(
//                         Map.of(
//                                 "url",
//                                 imageUrl,

//                                 "publicId",
//                                 publicId != null
//                                         ? publicId
//                                         : "",

//                                 "filename",
//                                 publicId != null
//                                         ? publicId
//                                         : "",

//                                 "originalName",
//                                 file.getOriginalFilename()
//                                         != null
//                                         ? file.getOriginalFilename()
//                                         : "unknown",

//                                 "size",
//                                 file.getSize(),

//                                 "provider",
//                                 "cloudinary"
//                         )
//                 );
//             }

//             // ------------------------------------------------------------
//             // Local fallback
//             // ------------------------------------------------------------

//             return uploadToLocal(file);

//         } catch (Exception e) {

//             e.printStackTrace();

//             return ResponseEntity
//                     .badRequest()
//                     .body(
//                             Map.of(
//                                     "error",
//                                     "Failed to upload image: "
//                                             + (
//                                             e.getMessage() != null
//                                                     ? e.getMessage()
//                                                     : "Unknown error"
//                                     )
//                             )
//                     );
//         }
//     }

//     /*
//     |--------------------------------------------------------------------------
//     | LOCAL UPLOAD
//     |--------------------------------------------------------------------------
//     */

//     private ResponseEntity<?> uploadToLocal(
//             MultipartFile file
//     ) throws IOException {

//         Path uploadPath =
//                 Paths.get(UPLOAD_DIR);

//         if (!Files.exists(uploadPath)) {
//             Files.createDirectories(uploadPath);
//         }

//         String originalFilename =
//                 file.getOriginalFilename();

//         String extension = "";

//         if (originalFilename != null &&
//                 originalFilename.contains(".")) {

//             extension =
//                     originalFilename.substring(
//                             originalFilename
//                                     .lastIndexOf(".")
//                     );
//         }

//         String filename =
//                 UUID.randomUUID()
//                         + extension;

//         Path filePath =
//                 uploadPath.resolve(filename);

//         Files.copy(
//                 file.getInputStream(),
//                 filePath
//         );

//         String imageUrl =
//                 "/api/images/" + filename;

//         return ResponseEntity.ok(
//                 Map.of(
//                         "url",
//                         imageUrl,

//                         "filename",
//                         filename,

//                         "originalName",
//                         originalFilename != null
//                                 ? originalFilename
//                                 : "unknown",

//                         "size",
//                         file.getSize(),

//                         "provider",
//                         "local"
//                 )
//         );
//     }

//     /*
//     |--------------------------------------------------------------------------
//     | GET LOCAL IMAGE
//     |--------------------------------------------------------------------------
//     */

//     @GetMapping("/{filename:.+}")
//     public ResponseEntity<?> getImage(
//             @PathVariable String filename
//     ) {

//         try {

//             Path filePath =
//                     Paths.get(UPLOAD_DIR)
//                             .resolve(filename)
//                             .normalize();

//             // Security: prevent path traversal
//             if (!filePath.startsWith(
//                     Paths.get(UPLOAD_DIR)
//             )) {
//                 return ResponseEntity
//                         .badRequest()
//                         .build();
//             }

//             if (!Files.exists(filePath)) {
//                 return ResponseEntity
//                         .notFound()
//                         .build();
//             }

//             byte[] imageBytes =
//                     Files.readAllBytes(filePath);

//             String contentType =
//                     determineContentType(
//                             filename
//                     );

//             return ResponseEntity
//                     .ok()
//                     .header(
//                             "Content-Type",
//                             contentType
//                     )
//                     .header(
//                             "Cache-Control",
//                             "public, max-age=31536000"
//                     )
//                     .body(imageBytes);

//         } catch (IOException e) {

//             e.printStackTrace();

//             return ResponseEntity
//                     .badRequest()
//                     .build();
//         }
//     }

//     /*
//     |--------------------------------------------------------------------------
//     | DELETE LOCAL IMAGE
//     |--------------------------------------------------------------------------
//     */

//     @DeleteMapping("/{filename:.+}")
//     public ResponseEntity<?> deleteImage(
//             @PathVariable String filename
//     ) {

//         try {

//             Path filePath =
//                     Paths.get(UPLOAD_DIR)
//                             .resolve(filename)
//                             .normalize();

//             if (!filePath.startsWith(
//                     Paths.get(UPLOAD_DIR)
//             )) {
//                 return ResponseEntity
//                         .badRequest()
//                         .build();
//             }

//             if (Files.exists(filePath)) {

//                 Files.delete(filePath);

//                 return ResponseEntity.ok(
//                         Map.of(
//                                 "message",
//                                 "Image deleted"
//                         )
//                 );
//             }

//             return ResponseEntity
//                     .notFound()
//                     .build();

//         } catch (IOException e) {

//             e.printStackTrace();

//             return ResponseEntity
//                     .badRequest()
//                     .body(
//                             Map.of(
//                                     "error",
//                                     "Failed to delete image"
//                             )
//                     );
//         }
//     }

//     /*
//     |--------------------------------------------------------------------------
//     | CHECK CLOUDINARY CONFIGURATION
//     |--------------------------------------------------------------------------
//     */

//     private boolean isCloudinaryConfigured() {

//         try {

//             // ------------------------------------------------------------
//             // System properties
//             // ------------------------------------------------------------

//             String cloudName =
//                     System.getProperty(
//                             "cloudinary.cloud_name"
//                     );

//             String apiKey =
//                     System.getProperty(
//                             "cloudinary.api_key"
//                     );

//             String apiSecret =
//                     System.getProperty(
//                             "cloudinary.api_secret"
//                     );

//             if (isNotEmpty(cloudName) &&
//                     isNotEmpty(apiKey) &&
//                     isNotEmpty(apiSecret)) {

//                 return true;
//             }

//             // ------------------------------------------------------------
//             // Environment variables
//             // ------------------------------------------------------------

//             cloudName =
//                     System.getenv(
//                             "CLOUDINARY_CLOUD_NAME"
//                     );

//             apiKey =
//                     System.getenv(
//                             "CLOUDINARY_API_KEY"
//                     );

//             apiSecret =
//                     System.getenv(
//                             "CLOUDINARY_API_SECRET"
//                     );

//             return isNotEmpty(cloudName) &&
//                     isNotEmpty(apiKey) &&
//                     isNotEmpty(apiSecret);

//         } catch (Exception e) {

//             return false;
//         }
//     }

//     /*
//     |--------------------------------------------------------------------------
//     | STRING CHECK
//     |--------------------------------------------------------------------------
//     */

//     private boolean isNotEmpty(
//             String value
//     ) {

//         return value != null &&
//                 !value.trim().isEmpty();
//     }

//     /*
//     |--------------------------------------------------------------------------
//     | CONTENT TYPE
//     |--------------------------------------------------------------------------
//     */

//     private String determineContentType(
//             String filename
//     ) {

//         String ext =
//                 filename.toLowerCase();

//         if (ext.endsWith(".jpg") ||
//                 ext.endsWith(".jpeg")) {

//             return "image/jpeg";
//         }

//         if (ext.endsWith(".png")) {
//             return "image/png";
//         }

//         if (ext.endsWith(".gif")) {
//             return "image/gif";
//         }

//         if (ext.endsWith(".webp")) {
//             return "image/webp";
//         }

//         if (ext.endsWith(".svg")) {
//             return "image/svg+xml";
//         }

//         return "application/octet-stream";
//     }
// }



package com.travelvista.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    private final Cloudinary cloudinary;

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/uploads/images/";

    public ImageController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // =========================================================
    // TEST CLOUDINARY CONNECTION
    // =========================================================

    @GetMapping("/cloudinary")
    public ResponseEntity<?> testCloudinary() {

        try {

            Map<?, ?> result =
                    cloudinary.api().ping(Collections.emptyMap());

            return ResponseEntity.ok(
                    Map.of(
                            "connected", true,
                            "message",
                            "Cloudinary connected successfully",
                            "result",
                            result
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "connected", false,
                                    "message",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Cloudinary connection failed"
                            )
                    );
        }
    }

    // =========================================================
    // UPLOAD IMAGE TO CLOUDINARY
    // =========================================================

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {

        System.out.println("==========================================");
        System.out.println("IMAGE UPLOAD REQUEST");
        System.out.println("==========================================");

        try {

            // -------------------------------------------------
            // Validate file
            // -------------------------------------------------

            if (file == null || file.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "Image file is empty"
                                )
                        );
            }

            String contentType =
                    file.getContentType();

            System.out.println(
                    "Original name: "
                            + file.getOriginalFilename()
            );

            System.out.println(
                    "Content type: "
                            + contentType
            );

            System.out.println(
                    "File size: "
                            + file.getSize()
                            + " bytes"
            );

            if (contentType == null ||
                    !contentType.startsWith("image/")) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "Only image files are allowed"
                                )
                        );
            }

            // 5 MB maximum
            if (file.getSize() > 5 * 1024 * 1024) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "Maximum image size is 5MB"
                                )
                        );
            }

            // -------------------------------------------------
            // Cloudinary upload
            // -------------------------------------------------

            System.out.println(
                    "Starting Cloudinary upload..."
            );

            Map<String, Object> params =
                    ObjectUtils.asMap(
                            "folder",
                            "travelvista",

                            "resource_type",
                            "image",

                            "unique_filename",
                            true
                    );

            Map<?, ?> result =
                    cloudinary
                            .uploader()
                            .upload(
                                    file.getBytes(),
                                    params
                            );

            System.out.println(
                    "Cloudinary upload completed."
            );

            System.out.println(
                    "Cloudinary result: "
                            + result
            );

            // -------------------------------------------------
            // Get Cloudinary URL
            // -------------------------------------------------

            String imageUrl =
                    (String) result.get(
                            "secure_url"
                    );

            String publicId =
                    (String) result.get(
                            "public_id"
                    );

            if (imageUrl == null ||
                    imageUrl.isBlank()) {

                System.err.println(
                        "Cloudinary did not return secure_url"
                );

                return ResponseEntity
                        .internalServerError()
                        .body(
                                Map.of(
                                        "error",
                                        "Cloudinary did not return secure_url"
                                )
                        );
            }

            // -------------------------------------------------
            // Success
            // -------------------------------------------------

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            imageUrl,

                            "publicId",
                            publicId != null
                                    ? publicId
                                    : "",

                            "filename",
                            publicId != null
                                    ? publicId
                                    : "",

                            "originalName",
                            file.getOriginalFilename() != null
                                    ? file.getOriginalFilename()
                                    : "unknown",

                            "size",
                            file.getSize(),

                            "provider",
                            "cloudinary"
                    )
            );

        } catch (Exception e) {

            // -------------------------------------------------
            // IMPORTANT:
            // Print actual backend error
            // -------------------------------------------------

            System.err.println(
                    "=========================================="
            );

            System.err.println(
                    "CLOUDINARY IMAGE UPLOAD ERROR"
            );

            System.err.println(
                    "=========================================="
            );

            e.printStackTrace();

            String message =
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Cloudinary upload failed";

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "error",
                                    message
                            )
                    );
        }
    }

    // =========================================================
    // LOCAL FALLBACK UPLOAD
    // =========================================================

    private ResponseEntity<?> uploadToLocal(
            MultipartFile file
    ) throws IOException {

        Path uploadPath =
                Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename =
                file.getOriginalFilename();

        String extension = "";

        if (originalFilename != null &&
                originalFilename.contains(".")) {

            extension =
                    originalFilename.substring(
                            originalFilename.lastIndexOf(".")
                    );
        }

        String filename =
                UUID.randomUUID()
                        + extension;

        Path filePath =
                uploadPath.resolve(filename);

        Files.copy(
                file.getInputStream(),
                filePath
        );

        String imageUrl =
                "/api/images/" + filename;

        return ResponseEntity.ok(
                Map.of(
                        "url",
                        imageUrl,

                        "filename",
                        filename,

                        "originalName",
                        originalFilename != null
                                ? originalFilename
                                : "unknown",

                        "size",
                        file.getSize(),

                        "provider",
                        "local"
                )
        );
    }

    // =========================================================
    // GET LOCAL IMAGE
    // =========================================================

    @GetMapping("/{filename:.+}")
    public ResponseEntity<?> getImage(
            @PathVariable String filename
    ) {

        try {

            Path basePath =
                    Paths.get(UPLOAD_DIR)
                            .toAbsolutePath()
                            .normalize();

            Path filePath =
                    basePath
                            .resolve(filename)
                            .normalize();

            // Prevent path traversal
            if (!filePath.startsWith(basePath)) {
                return ResponseEntity
                        .badRequest()
                        .build();
            }

            if (!Files.exists(filePath)) {
                return ResponseEntity
                        .notFound()
                        .build();
            }

            byte[] imageBytes =
                    Files.readAllBytes(filePath);

            String contentType =
                    determineContentType(filename);

            return ResponseEntity
                    .ok()
                    .header(
                            "Content-Type",
                            contentType
                    )
                    .header(
                            "Cache-Control",
                            "public, max-age=31536000"
                    )
                    .body(imageBytes);

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }

    // =========================================================
    // DELETE LOCAL IMAGE
    // =========================================================

    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<?> deleteImage(
            @PathVariable String filename
    ) {

        try {

            Path basePath =
                    Paths.get(UPLOAD_DIR)
                            .toAbsolutePath()
                            .normalize();

            Path filePath =
                    basePath
                            .resolve(filename)
                            .normalize();

            if (!filePath.startsWith(basePath)) {
                return ResponseEntity
                        .badRequest()
                        .build();
            }

            if (Files.exists(filePath)) {

                Files.delete(filePath);

                return ResponseEntity.ok(
                        Map.of(
                                "message",
                                "Image deleted"
                        )
                );
            }

            return ResponseEntity
                    .notFound()
                    .build();

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "error",
                                    "Failed to delete image"
                            )
                    );
        }
    }

    // =========================================================
    // CONTENT TYPE
    // =========================================================

    private String determineContentType(
            String filename
    ) {

        String ext =
                filename.toLowerCase();

        if (ext.endsWith(".jpg") ||
                ext.endsWith(".jpeg")) {

            return "image/jpeg";
        }

        if (ext.endsWith(".png")) {

            return "image/png";
        }

        if (ext.endsWith(".gif")) {

            return "image/gif";
        }

        if (ext.endsWith(".webp")) {

            return "image/webp";
        }

        if (ext.endsWith(".svg")) {

            return "image/svg+xml";
        }

        return "application/octet-stream";
    }
}
