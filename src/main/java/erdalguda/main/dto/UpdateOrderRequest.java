package erdalguda.main.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderRequest {
    private String status;
    private String notes;
    private Long selectedFabricId;
    private Double totalPrice;
    private String estimatedDeliveryDate; // Format: yyyy-MM-dd
    private String deliveryDate;          // Format: yyyy-MM-dd

    // === GÖMLEK ÖZELLEŞTİRMELERİ ===
    private String collarType;
    private String sleeveType;

    // === PANTOLON ÖZELLEŞTİRMELERİ ===
    private String waistType;
    private String pleatType;
    private String legType;

    // === CEKET ÖZELLEŞTİRMELERİ ===
    private String buttonType;
    private String pocketType;
    private String ventType;
    private String backType;

    // Manual getter/setter methods
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getSelectedFabricId() { return selectedFabricId; }
    public void setSelectedFabricId(Long selectedFabricId) { this.selectedFabricId = selectedFabricId; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public String getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(String estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }

    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }

    // Gömlek özellikleri
    public String getCollarType() { return collarType; }
    public void setCollarType(String collarType) { this.collarType = collarType; }

    public String getSleeveType() { return sleeveType; }
    public void setSleeveType(String sleeveType) { this.sleeveType = sleeveType; }

    // Pantolon özellikleri
    public String getWaistType() { return waistType; }
    public void setWaistType(String waistType) { this.waistType = waistType; }

    public String getPleatType() { return pleatType; }
    public void setPleatType(String pleatType) { this.pleatType = pleatType; }

    public String getLegType() { return legType; }
    public void setLegType(String legType) { this.legType = legType; }

    // Ceket özellikleri
    public String getButtonType() { return buttonType; }
    public void setButtonType(String buttonType) { this.buttonType = buttonType; }

    public String getPocketType() { return pocketType; }
    public void setPocketType(String pocketType) { this.pocketType = pocketType; }

    public String getVentType() { return ventType; }
    public void setVentType(String ventType) { this.ventType = ventType; }

    public String getBackType() { return backType; }
    public void setBackType(String backType) { this.backType = backType; }
}