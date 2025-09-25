package erdalguda.main.dto;

public class MeasurementRequest {
    
    private String regionName;
    private Double value;
    private String unit = "cm";
    
    // Constructors
    public MeasurementRequest() {}
    
    public MeasurementRequest(String regionName, Double value, String unit) {
        this.regionName = regionName;
        this.value = value;
        this.unit = unit != null ? unit : "cm";
    }
    
    // Getters and Setters
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
} 