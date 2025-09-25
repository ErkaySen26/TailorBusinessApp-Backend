package erdalguda.main.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private Long customerId;
    private String productType;
    private String status;
    private Long selectedFabricId;
    private String notes;
    private Double totalPrice;
    private String estimatedDeliveryDate; // Eklenen alan

    // === GÖMLEK ÖZELLEŞTİRMELERİ ===
    private String collarType;  // Yaka çeşidi
    private String sleeveType;  // Kol tipi

    // === PANTOLON ÖZELLEŞTİRMELERİ ===
    private String waistType;   // Bel tipi
    private String pleatType;   // Pile tipi
    private String legType;     // Paça tipi

    // === CEKET ÖZELLEŞTİRMELERİ ===
    private String buttonType;  // Düğme modeli
    private String pocketType;  // Cep modeli
    private String ventType;    // Yırtmaç modeli
    private String backType;    // Sırt modeli

    // Manual getter/setter methods
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getSelectedFabricId() { return selectedFabricId; }
    public void setSelectedFabricId(Long selectedFabricId) { this.selectedFabricId = selectedFabricId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public String getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(String estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }

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
