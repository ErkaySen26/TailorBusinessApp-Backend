package erdalguda.main.dto;

public class OrderStatusDistributionDto {
    private String status;
    private Long count;

    public OrderStatusDistributionDto() {}

    public OrderStatusDistributionDto(String status, Long count) {
        this.status = status;
        this.count = count;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
} 