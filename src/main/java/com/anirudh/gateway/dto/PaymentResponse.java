package com.anirudh.gateway.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private String paymentId;
    private String clientId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime publishedAt;
    private boolean degraded = true; // true when served from the fallback, not the live path

    public PaymentResponse() {
    }

    public PaymentResponse(String paymentId, String clientId, BigDecimal amount,
                            String status, LocalDateTime publishedAt) {
        this.paymentId = paymentId;
        this.clientId = clientId;
        this.amount = amount;
        this.status = status;
        this.publishedAt = publishedAt;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public boolean isDegraded() { return degraded; }
    public void setDegraded(boolean degraded) { this.degraded = degraded; }
}
