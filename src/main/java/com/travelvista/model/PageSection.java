package com.travelvista.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Page Builder Section — each destination page is built from ordered sections.
 * Sections: hero, why-travel, destinations, packages, gallery, faq, enquiry, stats, custom, etc.
 */
@Entity
@Table(name = "page_sections", indexes = {
    @Index(name = "idx_section_dest", columnList = "destination_id"),
    @Index(name = "idx_section_slug", columnList = "slug")
})
public class PageSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    @JsonIgnore
    private Destination destination;

    @Column(nullable = false, length = 50)
    private String type; // hero, why-travel, destinations, packages, gallery, faq, enquiry, stats, custom-html, testimonials

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(length = 200)
    private String title; // Section heading

    @Column(length = 200)
    private String slug; // HTML anchor id: #why-travel-gujarat

    @Column(length = 10)
    private String headingLevel = "h2"; // h1, h2, h3

    @Column(length = 200)
    private String description; // Short description under heading

    @Column(columnDefinition = "TEXT")
    private String content; // HTML content for custom sections

    @Column(columnDefinition = "TEXT")
    private String config; // JSON config (e.g., which packages to show, gallery settings)

    @Column(length = 200)
    private String backgroundImage; // Optional section background

    @Column(length = 200)
    private String cssClass; // Custom CSS class

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean showOnMobile = true;

    @Column(nullable = false)
    private Boolean showOnDesktop = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getHeadingLevel() { return headingLevel; }
    public void setHeadingLevel(String headingLevel) { this.headingLevel = headingLevel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }

    public String getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(String backgroundImage) { this.backgroundImage = backgroundImage; }

    public String getCssClass() { return cssClass; }
    public void setCssClass(String cssClass) { this.cssClass = cssClass; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getShowOnMobile() { return showOnMobile; }
    public void setShowOnMobile(Boolean showOnMobile) { this.showOnMobile = showOnMobile; }

    public Boolean getShowOnDesktop() { return showOnDesktop; }
    public void setShowOnDesktop(Boolean showOnDesktop) { this.showOnDesktop = showOnDesktop; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
