package erdalguda.main.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    // === GÖMLEK ÖZELLEŞTİRMELERİ ===
    @Enumerated(EnumType.STRING)
    private CollarType collarType; // Yaka çeşidi

    @Enumerated(EnumType.STRING)
    private SleeveType sleeveType; // Kol tipi

    // === PANTOLON ÖZELLEŞTİRMELERİ ===
    @Enumerated(EnumType.STRING)
    private WaistType waistType; // Bel tipi

    @Enumerated(EnumType.STRING)
    private PleatType pleatType; // Pile tipi

    @Enumerated(EnumType.STRING)
    private LegType legType; // Paça tipi

    // === CEKET ÖZELLEŞTİRMELERİ ===
    @Enumerated(EnumType.STRING)
    private ButtonType buttonType; // Düğme modeli

    @Enumerated(EnumType.STRING)
    private PocketType pocketType; // Cep modeli

    @Enumerated(EnumType.STRING)
    private VentType ventType; // Yırtmaç modeli

    @Enumerated(EnumType.STRING)
    private BackType backType; // Sırt modeli

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDate orderDate = LocalDate.now();
    private LocalDate estimatedDeliveryDate;
    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PREPARING;

    private String notes;

    // İlişkiler
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Fiyat bilgileri
    private Double totalPrice;

    // === ANA ENUM'LAR ===
    public enum ProductType {
        CEKET("Ceket"),
        GÖMLEK("Gömlek"),
        PANTOLON("Pantolon"),
        TAKIM("Takım Elbise");

        private final String displayName;

        ProductType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // === GÖMLEK ENUM'LARI ===
    public enum CollarType {
        MONO("Mono Yaka"),
        KIRLANGIC("Kırlangıç Yaka"),
        HAKIM("Hakim Yaka"),
        SAL("ŞAL Yaka");

        private final String displayName;

        CollarType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SleeveType {
        VATKALI("Vatkalı Kol"),
        VOTKASIZ("Votkasız Kol"),
        BUZGULU("Büzgülü Kol");

        private final String displayName;

        SleeveType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // === PANTOLON ENUM'LARI ===
    public enum WaistType {
        DUSUK_BEL("Düşük Bel"),
        ARA_BEL("Ara Bel"),
        YUKSEK_BEL("Yüksek Bel");

        private final String displayName;

        WaistType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PleatType {
        PILESIZ("Pilesiz Pantolon"),
        TEK_PILE("Tek Pile Pantolon"),
        CIFT_PILE("Çift Pile Pantolon");

        private final String displayName;

        PleatType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum LegType {
        DAR_PACA("Dar Paça Pantolon"),
        KLASIK("Klasik Pantolon"),
        BOL_PACA("Bol Paça Pantolon");

        private final String displayName;

        LegType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // === CEKET ENUM'LARI ===
    public enum ButtonType {
        TEK_DUGME("Tek Düğme"),
        IKI_DUGME("İki Düğme"),
        UC_DUGME("Üç Düğme"),
        DORT_DUGME("Dört Düğme");

        private final String displayName;

        ButtonType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PocketType {
        TEK_CEP("Tek Cep"),
        CIFT_CEP("Çift Cep"),
        FILO_CEP("Filo Cep"),
        EGIK_CEP("Eğik Cep"),
        TORBA_CEP("Torba Cep"),
        KORUKLU_CEP("Körüklü Cep");

        private final String displayName;

        PocketType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum VentType {
        YIRTMACSIZ("Yırtmaçsız Ceket"),
        TEK_YIRTMAC("Tek Yırtmaçlı Ceket"),
        CIFT_YIRTMAC("Çift Yırtmaçlı Ceket");

        private final String displayName;

        VentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum BackType {
        KORUKLU("Körüklü Ceket"),
        KORUKSUZ("Körüksüz Ceket"),
        ROBLI("Roblı Ceket");

        private final String displayName;

        BackType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum OrderStatus {
        PREPARING("Hazırlanıyor"),
        CUTTING("Kesim Aşamasında"),
        SEWING("Dikim Aşamasında"),
        FITTING("Prova Aşamasında"),
        READY("Hazır"),
        DELIVERED("Teslim Edildi"),
        CANCELLED("İptal Edildi");

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // === MANUAL GETTER/SETTER METHODS ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ProductType getProductType() { return productType; }
    public void setProductType(ProductType productType) { this.productType = productType; }
    
    // Gömlek özellikleri
    public CollarType getCollarType() { return collarType; }
    public void setCollarType(CollarType collarType) { this.collarType = collarType; }

    public SleeveType getSleeveType() { return sleeveType; }
    public void setSleeveType(SleeveType sleeveType) { this.sleeveType = sleeveType; }

    // Pantolon özellikleri
    public WaistType getWaistType() { return waistType; }
    public void setWaistType(WaistType waistType) { this.waistType = waistType; }

    public PleatType getPleatType() { return pleatType; }
    public void setPleatType(PleatType pleatType) { this.pleatType = pleatType; }

    public LegType getLegType() { return legType; }
    public void setLegType(LegType legType) { this.legType = legType; }

    // Ceket özellikleri
    public ButtonType getButtonType() { return buttonType; }
    public void setButtonType(ButtonType buttonType) { this.buttonType = buttonType; }

    public PocketType getPocketType() { return pocketType; }
    public void setPocketType(PocketType pocketType) { this.pocketType = pocketType; }

    public VentType getVentType() { return ventType; }
    public void setVentType(VentType ventType) { this.ventType = ventType; }

    public BackType getBackType() { return backType; }
    public void setBackType(BackType backType) { this.backType = backType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    
    public LocalDate getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }
    
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
}