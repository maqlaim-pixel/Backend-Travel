package com.travelvista.controller;

import com.travelvista.model.Invoice;
import com.travelvista.model.InvoiceItem;
import com.travelvista.model.User;
import com.travelvista.repository.UserRepository;
import com.travelvista.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final UserRepository userRepository;

    public InvoiceController(InvoiceService invoiceService, UserRepository userRepository) {
        this.invoiceService = invoiceService;
        this.userRepository = userRepository;
    }

    private User getAdmin(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof User u) return u;
        if (auth.getName() != null) return userRepository.findByEmail(auth.getName()).orElse(null);
        return null;
    }

    // ════════════════════════════════════════════════════════════════
    // 1. CREATE INVOICE (manual)
    // ════════════════════════════════════════════════════════════════
    @PostMapping
    public ResponseEntity<?> createInvoice(@RequestBody Map<String, Object> body, Authentication auth) {
        try {
            User admin = getAdmin(auth);

            Invoice invoice = new Invoice();
            invoice.setCustomerName((String) body.get("customerName"));
            invoice.setCustomerEmail((String) body.get("customerEmail"));
            invoice.setCustomerPhone((String) body.get("customerPhone"));
            invoice.setCustomerAddress((String) body.get("customerAddress"));
            invoice.setPackageTitle((String) body.get("packageTitle"));
            invoice.setNotes((String) body.get("notes"));

            if (body.get("travelDate") != null) {
                invoice.setTravelDate(LocalDate.parse((String) body.get("travelDate")));
            }
            if (body.get("endDate") != null) {
                invoice.setEndDate(LocalDate.parse((String) body.get("endDate")));
            }
            if (body.get("travelers") != null) {
                invoice.setTravelers((Integer) body.get("travelers"));
            }
            if (body.get("dueDate") != null) {
                invoice.setDueDate(LocalDate.parse((String) body.get("dueDate")));
            }

            Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
            String customerGstin = (String) body.get("customerGstin");
            String customerState = (String) body.get("customerState");

            // Parse items
            List<InvoiceItem> items = new ArrayList<>();
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) body.get("items");
            if (itemList != null) {
                for (Map<String, Object> itemData : itemList) {
                    InvoiceItem item = new InvoiceItem();
                    item.setDescription((String) itemData.get("description"));
                    item.setHsnCode(itemData.get("hsnCode") != null ? (String) itemData.get("hsnCode") : "9954");
                    item.setQuantity(itemData.get("quantity") != null ? Integer.valueOf(itemData.get("quantity").toString()) : 1);
                    item.setUnit(itemData.get("unit") != null ? (String) itemData.get("unit") : "NOS");
                    item.setRate(itemData.get("rate") != null ? new java.math.BigDecimal(itemData.get("rate").toString()) : java.math.BigDecimal.ZERO);
                    item.setDiscountPercent(itemData.get("discountPercent") != null ? new java.math.BigDecimal(itemData.get("discountPercent").toString()) : java.math.BigDecimal.ZERO);
                    items.add(item);
                }
            }

            String createdByName = admin != null ? admin.getName() : "Admin";
            String createdByEmail = admin != null ? admin.getEmail() : "admin@travelvista.com";

            Invoice saved = invoiceService.createInvoice(invoice, items, userId, customerGstin, customerState, createdByName, createdByEmail);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════════════════
    // 2. UPDATE INVOICE
    // ════════════════════════════════════════════════════════════════
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInvoice(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Invoice updates = new Invoice();
            updates.setCustomerName((String) body.get("customerName"));
            updates.setCustomerEmail((String) body.get("customerEmail"));
            updates.setCustomerPhone((String) body.get("customerPhone"));
            updates.setCustomerAddress((String) body.get("customerAddress"));
            updates.setPackageTitle((String) body.get("packageTitle"));
            updates.setNotes((String) body.get("notes"));

            if (body.get("travelDate") != null) updates.setTravelDate(LocalDate.parse((String) body.get("travelDate")));
            if (body.get("endDate") != null) updates.setEndDate(LocalDate.parse((String) body.get("endDate")));
            if (body.get("travelers") != null) updates.setTravelers((Integer) body.get("travelers"));
            if (body.get("dueDate") != null) updates.setDueDate(LocalDate.parse((String) body.get("dueDate")));

            Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
            String customerGstin = (String) body.get("customerGstin");
            String customerState = (String) body.get("customerState");

            List<InvoiceItem> items = new ArrayList<>();
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) body.get("items");
            if (itemList != null) {
                for (Map<String, Object> itemData : itemList) {
                    InvoiceItem item = new InvoiceItem();
                    item.setDescription((String) itemData.get("description"));
                    item.setHsnCode(itemData.get("hsnCode") != null ? (String) itemData.get("hsnCode") : "9954");
                    item.setQuantity(itemData.get("quantity") != null ? Integer.valueOf(itemData.get("quantity").toString()) : 1);
                    item.setUnit(itemData.get("unit") != null ? (String) itemData.get("unit") : "NOS");
                    item.setRate(itemData.get("rate") != null ? new java.math.BigDecimal(itemData.get("rate").toString()) : java.math.BigDecimal.ZERO);
                    item.setDiscountPercent(itemData.get("discountPercent") != null ? new java.math.BigDecimal(itemData.get("discountPercent").toString()) : java.math.BigDecimal.ZERO);
                    items.add(item);
                }
            }

            Invoice saved = invoiceService.updateInvoice(id, updates, items, userId, customerGstin, customerState);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════════════════
    // 3. DELETE INVOICE
    // ════════════════════════════════════════════════════════════════
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable Long id) {
        try {
            invoiceService.deleteInvoice(id);
            return ResponseEntity.ok(Map.of("message", "Invoice deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════════════════
    // 4. AUTO-GENERATE FROM BOOKING
    // ════════════════════════════════════════════════════════════════
    @PostMapping("/generate/{bookingId}")
    public ResponseEntity<?> generateFromBooking(
            @PathVariable Long bookingId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String customerGstin = body != null ? body.get("customerGstin") : null;
            String customerState = body != null ? body.get("customerState") : null;
            Invoice invoice = invoiceService.generateFromBooking(bookingId, customerGstin, customerState);
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════════════════
    // 5. SEND INVOICE
    // ════════════════════════════════════════════════════════════════
    @PostMapping("/{id}/send")
    public ResponseEntity<?> sendInvoice(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String sendVia = (String) body.getOrDefault("sendVia", "email");
            String recipientEmail = (String) body.get("recipientEmail");
            String recipientPhone = (String) body.get("recipientPhone");
            boolean sendAdminCopy = Boolean.TRUE.equals(body.get("sendAdminCopy"));

            Invoice invoice = invoiceService.sendInvoice(id, sendVia, recipientEmail, recipientPhone, sendAdminCopy);
            return ResponseEntity.ok(invoice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════════════════
    // 6. GET ALL INVOICES (admin)
    // ════════════════════════════════════════════════════════════════
    @GetMapping
    public ResponseEntity<?> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAll());
    }

    // ════════════════════════════════════════════════════════════════
    // 7. GET INVOICE BY ID
    // ════════════════════════════════════════════════════════════════
    @GetMapping("/{id}")
    public ResponseEntity<?> getInvoice(@PathVariable Long id) {
        return invoiceService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ════════════════════════════════════════════════════════════════
    // 8. GET MY INVOICES (customer)
    // ════════════════════════════════════════════════════════════════
    @GetMapping("/my")
    public ResponseEntity<?> getMyInvoices(Authentication auth) {
        User user = getAdmin(auth);
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(invoiceService.getByUser(user.getId()));
    }

    // ════════════════════════════════════════════════════════════════
    // 9. GET USERS LIST (for dropdown)
    // ════════════════════════════════════════════════════════════════
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        var users = userRepository.findAll();
        var list = users.stream().map(u -> Map.of(
                "id", u.getId(),
                "name", u.getName() != null ? u.getName() : "",
                "email", u.getEmail() != null ? u.getEmail() : "",
                "phone", u.getPhone() != null ? u.getPhone() : ""
        )).toList();
        return ResponseEntity.ok(list);
    }

    // ════════════════════════════════════════════════════════════════
    // 10. UPDATE STATUS
    // ════════════════════════════════════════════════════════════════
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Invoice invoice = invoiceService.updateStatus(id, body.get("status"), body.get("paymentMode"), body.get("paymentReference"));
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════════════════
    // 11. DASHBOARD STATS
    // ════════════════════════════════════════════════════════════════
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(Map.of(
                "totalInvoices", invoiceService.totalInvoices(),
                "paidCount", invoiceService.paidCount(),
                "pendingCount", invoiceService.pendingCount(),
                "totalRevenue", invoiceService.totalRevenue(),
                "totalTaxCollected", invoiceService.totalTaxCollected(),
                "totalIgst", invoiceService.totalIgst(),
                "totalCgstSgst", invoiceService.totalCgstSgst()
        ));
    }
}
