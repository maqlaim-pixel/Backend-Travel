package com.travelvista.service;

import com.travelvista.dto.LeadRequest;
import com.travelvista.model.Lead;
import com.travelvista.repository.LeadRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LeadService {

    private final LeadRepository leadRepository;

    public LeadService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    public List<Lead> getAll() {
        return leadRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Lead> getByStatus(String status) {
        return leadRepository.findByStatus(status);
    }

    public Optional<Lead> getById(Long id) {
        return leadRepository.findById(id);
    }

    public Lead create(LeadRequest req) {
        Lead lead = new Lead();
        lead.setName(req.getName());
        lead.setEmail(req.getEmail());
        lead.setPhone(req.getPhone());
        lead.setWhatsapp(req.getWhatsapp());
        lead.setDestination(req.getDestination());
        lead.setTravelers(req.getTravelers() != null ? req.getTravelers() : 1);
        lead.setBudget(req.getBudget());
        lead.setMessage(req.getMessage());
        lead.setLeadType(req.getLeadType() != null ? req.getLeadType() : "general");
        lead.setSourceUrl(req.getSourceUrl());

        if (req.getTravelDate() != null && !req.getTravelDate().isEmpty()) {
            try {
                lead.setTravelDate(LocalDate.parse(req.getTravelDate()));
            } catch (Exception ignored) {}
        }

        return leadRepository.save(lead);
    }

    public Lead updateStatus(Long id, String status) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        lead.setStatus(status);
        lead.setUpdatedAt(LocalDateTime.now());
        return leadRepository.save(lead);
    }

    public Lead addNote(Long leadId, String note) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        // For now, append to message field
        String existing = lead.getMessage() != null ? lead.getMessage() : "";
        lead.setMessage(existing + "\n---\n[Note] " + note);
        lead.setUpdatedAt(LocalDateTime.now());
        return leadRepository.save(lead);
    }

    public long countAll() {
        return leadRepository.countAll();
    }

    public long countNew() {
        return leadRepository.countNew();
    }
}
