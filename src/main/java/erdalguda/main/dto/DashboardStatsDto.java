package erdalguda.main.dto;

public class DashboardStatsDto {
    private Long totalCustomers;
    private Long ordersLast30Days;
    private Double revenueLastMonth;
    private Long fittingOrders;
    private Long completedLastMonth;
    private Long deliveriesThisWeek;
    private Double ordersGrowth;
    private Double revenueGrowth;
    private Double customersGrowth;

    public DashboardStatsDto() {}

    public DashboardStatsDto(Long totalCustomers, Long ordersLast30Days, Double revenueLastMonth, 
                           Long fittingOrders, Long completedLastMonth, Long deliveriesThisWeek,
                           Double ordersGrowth, Double revenueGrowth, Double customersGrowth) {
        this.totalCustomers = totalCustomers;
        this.ordersLast30Days = ordersLast30Days;
        this.revenueLastMonth = revenueLastMonth;
        this.fittingOrders = fittingOrders;
        this.completedLastMonth = completedLastMonth;
        this.deliveriesThisWeek = deliveriesThisWeek;
        this.ordersGrowth = ordersGrowth;
        this.revenueGrowth = revenueGrowth;
        this.customersGrowth = customersGrowth;
    }

    // Getters and Setters
    public Long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(Long totalCustomers) { this.totalCustomers = totalCustomers; }

    public Long getOrdersLast30Days() { return ordersLast30Days; }
    public void setOrdersLast30Days(Long ordersLast30Days) { this.ordersLast30Days = ordersLast30Days; }

    public Double getRevenueLastMonth() { return revenueLastMonth; }
    public void setRevenueLastMonth(Double revenueLastMonth) { this.revenueLastMonth = revenueLastMonth; }

    public Long getFittingOrders() { return fittingOrders; }
    public void setFittingOrders(Long fittingOrders) { this.fittingOrders = fittingOrders; }

    public Long getCompletedLastMonth() { return completedLastMonth; }
    public void setCompletedLastMonth(Long completedLastMonth) { this.completedLastMonth = completedLastMonth; }

    public Long getDeliveriesThisWeek() { return deliveriesThisWeek; }
    public void setDeliveriesThisWeek(Long deliveriesThisWeek) { this.deliveriesThisWeek = deliveriesThisWeek; }

    public Double getOrdersGrowth() { return ordersGrowth; }
    public void setOrdersGrowth(Double ordersGrowth) { this.ordersGrowth = ordersGrowth; }

    public Double getRevenueGrowth() { return revenueGrowth; }
    public void setRevenueGrowth(Double revenueGrowth) { this.revenueGrowth = revenueGrowth; }

    public Double getCustomersGrowth() { return customersGrowth; }
    public void setCustomersGrowth(Double customersGrowth) { this.customersGrowth = customersGrowth; }
} 