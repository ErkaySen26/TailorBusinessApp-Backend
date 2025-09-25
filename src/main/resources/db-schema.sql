-- Measurements tablosu
CREATE TABLE IF NOT EXISTS measurements (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    region_name VARCHAR(100) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(10) DEFAULT 'cm',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_measurements_customer 
        FOREIGN KEY (customer_id) 
        REFERENCES customers(id) 
        ON DELETE CASCADE,
    
    -- Unique constraint - müşteri başına aynı bölge adı olamaz
    CONSTRAINT uk_measurements_customer_region 
        UNIQUE (customer_id, region_name)
);

-- Index'ler
CREATE INDEX IF NOT EXISTS idx_measurements_customer_id ON measurements(customer_id);
CREATE INDEX IF NOT EXISTS idx_measurements_region_name ON measurements(region_name); 