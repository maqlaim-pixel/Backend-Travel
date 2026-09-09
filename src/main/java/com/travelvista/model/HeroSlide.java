package com.travelvista.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hero_slides", indexes = {
    @Index(name = "idx_hero_dest", columnList = "destination_id")
})
public class HeroSlide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    @JsonIgnore
    private Destination destination;

    @Column(nullable = false, length = 500)
    private String image; // Cloudinary URL

    @Column(length = 300)
    private String title; // Overlay title

    @Column(length = 500)
    private String subtitle; // Overlay subtitle

    @Column(length = 200)
    private String altText; // Image alt text for SEO

    @Column(length = 200)
    private String titleAttribute; // Image title attribute

    @Column(length = 100)
    private String ctaText; // Button text

    @Column(length = 300)
    private String ctaLink; // Button URL

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(length = 200)
    private String overlayColor; // CSS overlay color

    @Column(nullable = false)
    private Integer overlayOpacity = 40; // 0-100

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }

    public String getTitleAttribute() { return titleAttribute; }
    public void setTitleAttribute(String titleAttribute) { this.titleAttribute = titleAttribute; }

    public String getCtaText() { return ctaText; }
    public void setCtaText(String ctaText) { this.ctaText = ctaText; }

    public String getCtaLink() { return ctaLink; }
    public void setCtaLink(String ctaLink) { this.ctaLink = ctaLink; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getOverlayColor() { return overlayColor; }
    public void setOverlayColor(String overlayColor) { this.overlayColor = overlayColor; }

    public Integer getOverlayOpacity() { return overlayOpacity; }
    public void setOverlayOpacity(Integer overlayOpacity) { this.overlayOpacity = overlayOpacity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
