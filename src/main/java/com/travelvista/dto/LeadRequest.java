package com.travelvista.dto;

public class LeadRequest {
    private String name;
    private String email;
    private String phone;
    private String whatsapp;
    private String destination;
    private String travelDate;
    private Integer travelers;
    private String budget;
    private String message;
    private String leadType;
    private String sourceUrl;

    public LeadRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getTravelDate() { return travelDate; }
    public void setTravelDate(String travelDate) { this.travelDate = travelDate; }
    public Integer getTravelers() { return travelers; }
    public void setTravelers(Integer travelers) { this.travelers = travelers; }
    public String getBudget() { return budget; }
    public void setBudget(String budget) { this.budget = budget; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getLeadType() { return leadType; }
    public void setLeadType(String leadType) { this.leadType = leadType; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}
