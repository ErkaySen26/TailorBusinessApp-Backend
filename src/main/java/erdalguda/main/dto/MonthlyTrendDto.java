package erdalguda.main.dto;

public class MonthlyTrendDto {
    private Integer year;
    private Integer month;
    private Long orderCount;
    private Double revenue;

    public MonthlyTrendDto() {}

    public MonthlyTrendDto(Integer year, Integer month, Long orderCount, Double revenue) {
        this.year = year;
        this.month = month;
        this.orderCount = orderCount;
        this.revenue = revenue;
    }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Long getOrderCount() { return orderCount; }
    public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }

    public Double getRevenue() { return revenue; }
    public void setRevenue(Double revenue) { this.revenue = revenue; }
} 