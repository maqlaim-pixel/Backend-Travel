package com.travelvista.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "enquiries")
public class Enquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enquiry_ref", unique = true, nullable = false, length = 30)
    private String enquiryRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private TravelPackage travelPackage;

    @Column(name = "package_title", length = 200)
    private String packageTitle;

    @Column(name = "destination", length = 200)
    private String destination;

    @Column(name = "travel_date")
    private LocalDate travelDate;

    private Integer travelers = 1;

    @Column(length = 30)
    private String budget;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 30)
    private String status = "pending"; // pending, contacted, confirmed, rejected, cancelled

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Enquiry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEnquiryRef() { return enquiryRef; }
    public void setEnquiryRef(String enquiryRef) { this.enquiryRef = enquiryRef; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public TravelPackage getTravelPackage() { return travelPackage; }
    public void setTravelPackage(TravelPackage travelPackage) { this.travelPackage = travelPackage; }
    public String getPackageTitle() { return packageTitle; }
    public void setPackageTitle(String packageTitle) { this.packageTitle = packageTitle; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
    public Integer getTravelers() { return travelers; }
    public void setTravelers(Integer travelers) { this.travelers = travelers; }
    public String getBudget() { return budget; }
    public void setBudget(String budget) { this.budget = budget; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
