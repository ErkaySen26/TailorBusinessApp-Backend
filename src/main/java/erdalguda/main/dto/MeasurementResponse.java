package erdalguda.main.dto;

import erdalguda.main.model.Measurement;
import java.time.LocalDateTime;

public class MeasurementResponse {
    
    private Long id;
    private Long customerId;
    private String regionName;
    private Double value;
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public MeasurementResponse() {}
    
    public MeasurementResponse(Measurement measurement) {
        this.id = measurement.getId();
        this.customerId = measurement.getCustomerId();
        this.regionName = measurement.getRegionName();
        this.value = measurement.getValue();
        this.unit = measurement.getUnit();
        this.createdAt = measurement.getCreatedAt();
        this.updatedAt = measurement.getUpdatedAt();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public String getRegionName() {
        return regionName;
    }
    
    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
    
    public Double getValue() {
        return value;
    }
    
    public void setValue(Double value) {
        this.value = value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 