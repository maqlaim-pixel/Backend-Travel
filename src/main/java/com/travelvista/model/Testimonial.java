package com.travelvista.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "testimonials")
public class Testimonial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 150)
    private String location;

    @Column(length = 500)
    private String photo;

    @Column(columnDefinition = "TEXT")
    private String quote;

    @Column(precision = 3, scale = 1)
    private BigDecimal rating = BigDecimal.valueOf(5);

    @Column(length = 20)
    private String status = "published";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Testimonial() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public String getQuote() { return quote; }
    public void setQuote(String quote) { this.quote = quote; }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
