package erdalguda.main.dto;

import erdalguda.main.model.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrderResponse {
    private Long id;
    private String productType;
    private LocalDateTime createdAt;
    private LocalDate orderDate;
    private LocalDate estimatedDeliveryDate;
    private LocalDate deliveryDate;
    private String status;
    private String notes;
    private Double totalPrice;
    private CustomerShortResponse customer;

    // === GÖMLEK ÖZELLEŞTİRMELERİ ===
    private String collarType;
    private String collarTypeDisplay;
    private String sleeveType;
    private String sleeveTypeDisplay;

    // === PANTOLON ÖZELLEŞTİRMELERİ ===
    private String waistType;
    private String waistTypeDisplay;
    private String pleatType;
    private String pleatTypeDisplay;
    private String legType;
    private String legTypeDisplay;

    // === CEKET ÖZELLEŞTİRMELERİ ===
    private String buttonType;
    private String buttonTypeDisplay;
    private String pocketType;
    private String pocketTypeDisplay;
    private String ventType;
    private String ventTypeDisplay;
    private String backType;
    private String backTypeDisplay;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.productType = order.getProductType() != null ? order.getProductType().name() : null;
        this.createdAt = order.getCreatedAt();
        this.orderDate = order.getOrderDate();
        this.estimatedDeliveryDate = order.getEstimatedDeliveryDate();
        this.deliveryDate = order.getDeliveryDate();
        this.status = order.getStatus() != null ? order.getStatus().name() : null;
        this.notes = order.getNotes();
        this.totalPrice = order.getTotalPrice();
        this.customer = order.getCustomer() != null ? new CustomerShortResponse(order.getCustomer()) : null;

        // Gömlek özellikleri
        this.collarType = order.getCollarType() != null ? order.getCollarType().name() : null;
        this.collarTypeDisplay = order.getCollarType() != null ? order.getCollarType().getDisplayName() : null;
        this.sleeveType = order.getSleeveType() != null ? order.getSleeveType().name() : null;
        this.sleeveTypeDisplay = order.getSleeveType() != null ? order.getSleeveType().getDisplayName() : null;

        // Pantolon özellikleri
        this.waistType = order.getWaistType() != null ? order.getWaistType().name() : null;
        this.waistTypeDisplay = order.getWaistType() != null ? order.getWaistType().getDisplayName() : null;
        this.pleatType = order.getPleatType() != null ? order.getPleatType().name() : null;
        this.pleatTypeDisplay = order.getPleatType() != null ? order.getPleatType().getDisplayName() : null;
        this.legType = order.getLegType() != null ? order.getLegType().name() : null;
        this.legTypeDisplay = order.getLegType() != null ? order.getLegType().getDisplayName() : null;

        // Ceket özellikleri
        this.buttonType = order.getButtonType() != null ? order.getButtonType().name() : null;
        this.buttonTypeDisplay = order.getButtonType() != null ? order.getButtonType().getDisplayName() : null;
        this.pocketType = order.getPocketType() != null ? order.getPocketType().name() : null;
        this.pocketTypeDisplay = order.getPocketType() != null ? order.getPocketType().getDisplayName() : null;
        this.ventType = order.getVentType() != null ? order.getVentType().name() : null;
        this.ventTypeDisplay = order.getVentType() != null ? order.getVentType().getDisplayName() : null;
        this.backType = order.getBackType() != null ? order.getBackType().name() : null;
        this.backTypeDisplay = order.getBackType() != null ? order.getBackType().getDisplayName() : null;
    }

    // Manual getter/setter methods for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public LocalDate getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public CustomerShortResponse getCustomer() { return customer; }
    public void setCustomer(CustomerShortResponse customer) { this.customer = customer; }

    // Gömlek özellikleri getters/setters
    public String getCollarType() { return collarType; }
    public void setCollarType(String collarType) { this.collarType = collarType; }

    public String getCollarTypeDisplay() { return collarTypeDisplay; }
    public void setCollarTypeDisplay(String collarTypeDisplay) { this.collarTypeDisplay = collarTypeDisplay; }

    public String getSleeveType() { return sleeveType; }
    public void setSleeveType(String sleeveType) { this.sleeveType = sleeveType; }

    public String getSleeveTypeDisplay() { return sleeveTypeDisplay; }
    public void setSleeveTypeDisplay(String sleeveTypeDisplay) { this.sleeveTypeDisplay = sleeveTypeDisplay; }

    // Pantolon özellikleri getters/setters
    public String getWaistType() { return waistType; }
    public void setWaistType(String waistType) { this.waistType = waistType; }

    public String getWaistTypeDisplay() { return waistTypeDisplay; }
    public void setWaistTypeDisplay(String waistTypeDisplay) { this.waistTypeDisplay = waistTypeDisplay; }

    public String getPleatType() { return pleatType; }
    public void setPleatType(String pleatType) { this.pleatType = pleatType; }

    public String getPleatTypeDisplay() { return pleatTypeDisplay; }
    public void setPleatTypeDisplay(String pleatTypeDisplay) { this.pleatTypeDisplay = pleatTypeDisplay; }

    public String getLegType() { return legType; }
    public void setLegType(String legType) { this.legType = legType; }

    public String getLegTypeDisplay() { return legTypeDisplay; }
    public void setLegTypeDisplay(String legTypeDisplay) { this.legTypeDisplay = legTypeDisplay; }

    // Ceket özellikleri getters/setters
    public String getButtonType() { return buttonType; }
    public void setButtonType(String buttonType) { this.buttonType = buttonType; }

    public String getButtonTypeDisplay() { return buttonTypeDisplay; }
    public void setButtonTypeDisplay(String buttonTypeDisplay) { this.buttonTypeDisplay = buttonTypeDisplay; }

    public String getPocketType() { return pocketType; }
    public void setPocketType(String pocketType) { this.pocketType = pocketType; }

    public String getPocketTypeDisplay() { return pocketTypeDisplay; }
    public void setPocketTypeDisplay(String pocketTypeDisplay) { this.pocketTypeDisplay = pocketTypeDisplay; }

    public String getVentType() { return ventType; }
    public void setVentType(String ventType) { this.ventType = ventType; }

    public String getVentTypeDisplay() { return ventTypeDisplay; }
    public void setVentTypeDisplay(String ventTypeDisplay) { this.ventTypeDisplay = ventTypeDisplay; }

    public String getBackType() { return backType; }
    public void setBackType(String backType) { this.backType = backType; }

    public String getBackTypeDisplay() { return backTypeDisplay; }
    public void setBackTypeDisplay(String backTypeDisplay) { this.backTypeDisplay = backTypeDisplay; }
}