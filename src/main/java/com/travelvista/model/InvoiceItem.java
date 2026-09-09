package com.travelvista.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    private Invoice invoice;

    @Column(nullable = false)
    private Integer serialNo = 1;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode = "9954";

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit", length = 20)
    private String unit = "NOS";

    @Column(name = "rate", precision = 12, scale = 2, nullable = false)
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "taxable_amount", precision = 12, scale = 2)
    private BigDecimal taxableAmount = BigDecimal.ZERO;

    @Column(name = "gst_rate", precision = 5, scale = 2)
    private BigDecimal gstRate = BigDecimal.ZERO;

    @Column(name = "cgst_rate", precision = 5, scale = 2)
    private BigDecimal cgstRate = BigDecimal.ZERO;

    @Column(name = "cgst_amount", precision = 12, scale = 2)
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(name = "sgst_rate", precision = 5, scale = 2)
    private BigDecimal sgstRate = BigDecimal.ZERO;

    @Column(name = "sgst_amount", precision = 12, scale = 2)
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(name = "igst_rate", precision = 5, scale = 2)
    private BigDecimal igstRate = BigDecimal.ZERO;

    @Column(name = "igst_amount", precision = 12, scale = 2)
    private BigDecimal igstAmount = BigDecimal.ZERO;

    @Column(name = "total_tax", precision = 12, scale = 2)
    private BigDecimal totalTax = BigDecimal.ZERO;

    @Column(name = "line_total", precision = 12, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    public InvoiceItem() {}

    // Calculate this item's amounts based on the GST type (intra/inter state)
    public void calculateAmounts(boolean isIntraState) {
        // Base = quantity * rate
        BigDecimal base = rate.multiply(BigDecimal.valueOf(quantity));

        // Discount
        BigDecimal disc = BigDecimal.ZERO;
        if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            disc = base.multiply(discountPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        discountAmount = disc;
        taxableAmount = base.subtract(disc);

        // GST
        BigDecimal gstAmt = taxableAmount.multiply(gstRate != null ? gstRate : new BigDecimal("18"))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        if (isIntraState) {
            cgstRate = gstRate != null ? gstRate.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP) : new BigDecimal("9");
            sgstRate = cgstRate;
            igstRate = BigDecimal.ZERO;
            cgstAmount = taxableAmount.multiply(cgstRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            sgstAmount = taxableAmount.multiply(sgstRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            igstAmount = BigDecimal.ZERO;
        } else {
            igstRate = gstRate != null ? gstRate : new BigDecimal("18");
            cgstRate = BigDecimal.ZERO;
            sgstRate = BigDecimal.ZERO;
            igstAmount = taxableAmount.multiply(igstRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            cgstAmount = BigDecimal.ZERO;
            sgstAmount = BigDecimal.ZERO;
        }

        totalTax = cgstAmount.add(sgstAmount).add(igstAmount);
        lineTotal = taxableAmount.add(totalTax);
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public Integer getSerialNo() { return serialNo; }
    public void setSerialNo(Integer serialNo) { this.serialNo = serialNo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }

    public BigDecimal getGstRate() { return gstRate; }
    public void setGstRate(BigDecimal gstRate) { this.gstRate = gstRate; }

    public BigDecimal getCgstRate() { return cgstRate; }
    public void setCgstRate(BigDecimal cgstRate) { this.cgstRate = cgstRate; }

    public BigDecimal getCgstAmount() { return cgstAmount; }
    public void setCgstAmount(BigDecimal cgstAmount) { this.cgstAmount = cgstAmount; }

    public BigDecimal getSgstRate() { return sgstRate; }
    public void setSgstRate(BigDecimal sgstRate) { this.sgstRate = sgstRate; }

    public BigDecimal getSgstAmount() { return sgstAmount; }
    public void setSgstAmount(BigDecimal sgstAmount) { this.sgstAmount = sgstAmount; }

    public BigDecimal getIgstRate() { return igstRate; }
    public void setIgstRate(BigDecimal igstRate) { this.igstRate = igstRate; }

    public BigDecimal getIgstAmount() { return igstAmount; }
    public void setIgstAmount(BigDecimal igstAmount) { this.igstAmount = igstAmount; }

    public BigDecimal getTotalTax() { return totalTax; }
    public void setTotalTax(BigDecimal totalTax) { this.totalTax = totalTax; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
