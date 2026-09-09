//package com.travelvista.model;
//
//import jakarta.persistence.*;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "packages")
//public class TravelPackage {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, length = 200)
//    private String title;
//
//    @Column(unique = true, nullable = false, length = 220)
//    private String slug;
//
//    @Column(columnDefinition = "TEXT")
//    private String description;
//
//    @Column(name = "short_description", length = 500)
//    private String shortDescription;
//
//    @Column(length = 200)
//    private String destination;
//
//    @Column(length = 100)
//    private String state;
//
//    @Column(length = 100)
//    private String country;
//
//    @Column(name = "duration_days")
//    private Integer durationDays = 0;
//
//    @Column(name = "duration_nights")
//    private Integer durationNights = 0;
//
//    @Column(name = "starting_price", precision = 12, scale = 2)
//    private BigDecimal startingPrice;
//
//    @Column(length = 10)
//    private String currency = "INR";
//
//    @Column(name = "cover_image", length = 500)
//    private String coverImage;
//
//    @Column(columnDefinition = "TEXT")
//    private String highlights;
//
//    @Column(columnDefinition = "TEXT")
//    private String inclusions;
//
//    @Column(columnDefinition = "TEXT")
//    private String exclusions;
//
//    @Column(columnDefinition = "JSONB")
//    private String itinerary;
//
//    @Column(length = 50)
//    private String category = "domestic";
//
//    @Column(length = 255)
//    private String tags;
//
//    @Column(length = 20)
//    private String status = "draft";
//
//    private Boolean featured = false;
//
//    @Column(precision = 3, scale = 1)
//    private BigDecimal rating = BigDecimal.ZERO;
//
//    @Column(name = "review_count")
//    private Integer reviewCount = 0;
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt = LocalDateTime.now();
//
//    public TravelPackage() {}
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//    public String getSlug() { return slug; }
//    public void setSlug(String slug) { this.slug = slug; }
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//    public String getShortDescription() { return shortDescription; }
//    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
//    public String getDestination() { return destination; }
//    public void setDestination(String destination) { this.destination = destination; }
//    public String getState() { return state; }
//    public void setState(String state) { this.state = state; }
//    public String getCountry() { return country; }
//    public void setCountry(String country) { this.country = country; }
//    public Integer getDurationDays() { return durationDays; }
//    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
//    public Integer getDurationNights() { return durationNights; }
//    public void setDurationNights(Integer durationNights) { this.durationNights = durationNights; }
//    public BigDecimal getStartingPrice() { return startingPrice; }
//    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
//    public String getCurrency() { return currency; }
//    public void setCurrency(String currency) { this.currency = currency; }
//    public String getCoverImage() { return coverImage; }
//    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
//    public String getHighlights() { return highlights; }
//    public void setHighlights(String highlights) { this.highlights = highlights; }
//    public String getInclusions() { return inclusions; }
//    public void setInclusions(String inclusions) { this.inclusions = inclusions; }
//    public String getExclusions() { return exclusions; }
//    public void setExclusions(String exclusions) { this.exclusions = exclusions; }
//    public String getItinerary() { return itinerary; }
//    public void setItinerary(String itinerary) { this.itinerary = itinerary; }
//    public String getCategory() { return category; }
//    public void setCategory(String category) { this.category = category; }
//    public String getTags() { return tags; }
//    public void setTags(String tags) { this.tags = tags; }
//    public String getStatus() { return status; }
//    public void setStatus(String status) { this.status = status; }
//    public Boolean getFeatured() { return featured; }
//    public void setFeatured(Boolean featured) { this.featured = featured; }
//    public BigDecimal getRating() { return rating; }
//    public void setRating(BigDecimal rating) { this.rating = rating; }
//    public Integer getReviewCount() { return reviewCount; }
//    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
//}


package com.travelvista.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "packages")
public class TravelPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(unique = true, nullable = false, length = 220)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(length = 200)
    private String destination;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(name = "duration_days")
    private Integer durationDays = 0;

    @Column(name = "duration_nights")
    private Integer durationNights = 0;

    @Column(name = "starting_price", precision = 12, scale = 2)
    private BigDecimal startingPrice;

    @Column(length = 10)
    private String currency = "INR";

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(columnDefinition = "TEXT")
    private String highlights;

    @Column(columnDefinition = "TEXT")
    private String inclusions;

    @Column(columnDefinition = "TEXT")
    private String exclusions;

    // PostgreSQL JSONB field
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String itinerary;

    @Column(length = 50)
    private String category = "domestic";

    @Column(length = 255)
    private String tags;

    @Column(length = 20)
    private String status = "draft";

    private Boolean featured = false;

    @Column(precision = 3, scale = 1)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public TravelPackage() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Integer getDurationNights() {
        return durationNights;
    }

    public void setDurationNights(Integer durationNights) {
        this.durationNights = durationNights;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getHighlights() {
        return highlights;
    }

    public void setHighlights(String highlights) {
        this.highlights = highlights;
    }

    public String getInclusions() {
        return inclusions;
    }

    public void setInclusions(String inclusions) {
        this.inclusions = inclusions;
    }

    public String getExclusions() {
        return exclusions;
    }

    public void setExclusions(String exclusions) {
        this.exclusions = exclusions;
    }

    public String getItinerary() {
        return itinerary;
    }

    public void setItinerary(String itinerary) {
        this.itinerary = itinerary;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}