package com.travelvista.dto;

public class LoginResponse {
    private String token;
    private UserInfo user;

    // When true, the response carries NO token and the frontend must show the
    // OTP screen. When false/null, this is a normal authenticated response.
    private Boolean requiresOtp;
    private String message;

    public LoginResponse() {}
    public LoginResponse(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }

    public LoginResponse(Boolean requiresOtp, String message) {
        this.requiresOtp = requiresOtp;
        this.message = message;
    }

    public Boolean getRequiresOtp() { return requiresOtp; }
    public void setRequiresOtp(Boolean requiresOtp) { this.requiresOtp = requiresOtp; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }

    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String role;
        private String profileImage;

        public UserInfo() {}
        public UserInfo(Long id, String name, String email, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
        }

        public UserInfo(Long id, String name, String email, String phone, String role, String profileImage) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.profileImage = profileImage;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    }
}
