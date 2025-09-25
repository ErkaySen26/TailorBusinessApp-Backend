-- Database Performance Optimization Indexes
-- Bu SQL dosyasını database'inizde çalıştırarak performansı artırabilirsiniz

-- ===== ORDERS TABLOSU İÇİN İNDEXLER =====
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_order_date ON orders(order_date);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_delivery_date ON orders(delivery_date);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_estimated_delivery ON orders(estimated_delivery_date);
CREATE INDEX IF NOT EXISTS idx_orders_product_type ON orders(product_type);
CREATE INDEX IF NOT EXISTS idx_orders_fit_type ON orders(fit_type);
CREATE INDEX IF NOT EXISTS idx_orders_total_price ON orders(total_price);

-- Compound indexes for common complex queries
CREATE INDEX IF NOT EXISTS idx_orders_customer_status ON orders(customer_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_date_status ON orders(order_date, status);
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders(status, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_customer_date ON orders(customer_id, order_date);
CREATE INDEX IF NOT EXISTS idx_orders_product_status ON orders(product_type, status);

-- Date range queries için optimize edilmiş index
CREATE INDEX IF NOT EXISTS idx_orders_date_range ON orders(order_date, created_at, status);

-- ===== CUSTOMERS TABLOSU İÇİN İNDEXLER =====
CREATE INDEX IF NOT EXISTS idx_customers_name ON customers(first_name, last_name);
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone);
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);

-- Arama performansı için text search indexleri
CREATE INDEX IF NOT EXISTS idx_customers_first_name_gin ON customers USING gin(to_tsvector('turkish', first_name));
CREATE INDEX IF NOT EXISTS idx_customers_last_name_gin ON customers USING gin(to_tsvector('turkish', last_name));
CREATE INDEX IF NOT EXISTS idx_customers_full_name_gin ON customers USING gin(to_tsvector('turkish', first_name || ' ' || last_name));

-- Case-insensitive arama için
CREATE INDEX IF NOT EXISTS idx_customers_first_name_lower ON customers(lower(first_name));
CREATE INDEX IF NOT EXISTS idx_customers_last_name_lower ON customers(lower(last_name));
CREATE INDEX IF NOT EXISTS idx_customers_phone_clean ON customers(replace(replace(phone, ' ', ''), '-', ''));

-- ===== MEASUREMENTS TABLOSU İÇİN İNDEXLER =====
CREATE INDEX IF NOT EXISTS idx_measurements_customer_id ON measurements(customer_id);

-- ===== MESSAGES TABLOSU İÇİN İNDEXLER =====
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at);
CREATE INDEX IF NOT EXISTS idx_messages_is_read ON messages(is_read);
CREATE INDEX IF NOT EXISTS idx_messages_read_created ON messages(is_read, created_at);

-- ===== BLOG/POST TABLOSU İÇİN İNDEXLER =====
CREATE INDEX IF NOT EXISTS idx_posts_published ON posts(published);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at);
CREATE INDEX IF NOT EXISTS idx_posts_url_slug ON posts(url_slug);
CREATE INDEX IF NOT EXISTS idx_posts_published_created ON posts(published, created_at);

CREATE INDEX IF NOT EXISTS idx_blogs_published ON blog(published);
CREATE INDEX IF NOT EXISTS idx_blogs_created_at ON blog(created_at);
CREATE INDEX IF NOT EXISTS idx_blogs_slug ON blog(slug);

-- ===== USERS TABLOSU İÇİN İNDEXLER =====
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_is_approved ON users(is_approved);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- Admin onay sistemi için compound index
CREATE INDEX IF NOT EXISTS idx_users_approval_status ON users(is_approved, is_active, role);

-- ===== ADMIN TABLOSU İÇİN İNDEXLER =====
CREATE INDEX IF NOT EXISTS idx_admin_role ON admin(role);

-- ===== PARTIAL INDEXES (Daha verimli) =====
-- Sadece aktif siparişler için index
CREATE INDEX IF NOT EXISTS idx_orders_active_status ON orders(status, created_at) 
WHERE status IN ('PREPARING', 'CUTTING', 'SEWING', 'FITTING');

-- Sadece yayınlanmış blog postları için index
CREATE INDEX IF NOT EXISTS idx_posts_published_only ON posts(created_at) WHERE published = true;
CREATE INDEX IF NOT EXISTS idx_blogs_published_only ON blog(created_at) WHERE published = true;

-- Sadece onay bekleyen kullanıcılar için index
CREATE INDEX IF NOT EXISTS idx_users_pending_approval ON users(created_at) WHERE is_approved = false;

-- ===== İSTATİSTİKLER İÇİN VİEW'LAR =====
CREATE OR REPLACE VIEW order_statistics AS
SELECT 
    DATE_TRUNC('month', order_date) as month,
    COUNT(*) as total_orders,
    SUM(total_price) as total_revenue,
    AVG(total_price) as avg_order_value,
    status,
    product_type,
    fit_type
FROM orders 
WHERE total_price IS NOT NULL
GROUP BY DATE_TRUNC('month', order_date), status, product_type, fit_type;

CREATE OR REPLACE VIEW customer_statistics AS
SELECT 
    c.id,
    c.first_name,
    c.last_name,
    c.phone,
    c.email,
    COUNT(o.id) as total_orders,
    SUM(o.total_price) as total_spent,
    AVG(o.total_price) as avg_order_value,
    MAX(o.order_date) as last_order_date,
    MIN(o.order_date) as first_order_date
FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id
GROUP BY c.id, c.first_name, c.last_name, c.phone, c.email;

-- Son 30 günün performans özeti
CREATE OR REPLACE VIEW dashboard_performance AS
SELECT 
    'customers' as metric,
    COUNT(*) as total_count,
    COUNT(*) FILTER (WHERE created_at >= CURRENT_DATE - INTERVAL '30 days') as last_30_days
FROM customers
UNION ALL
SELECT 
    'orders' as metric,
    COUNT(*) as total_count,
    COUNT(*) FILTER (WHERE order_date >= CURRENT_DATE - INTERVAL '30 days') as last_30_days
FROM orders
UNION ALL
SELECT 
    'revenue' as metric,
    COALESCE(SUM(total_price), 0)::bigint as total_count,
    COALESCE(SUM(total_price) FILTER (WHERE order_date >= CURRENT_DATE - INTERVAL '30 days'), 0)::bigint as last_30_days
FROM orders WHERE status = 'DELIVERED';

-- ===== PERFORMANCE MONİTORİNG =====
-- Slow query monitoring için materialized view
CREATE MATERIALIZED VIEW IF NOT EXISTS slow_queries AS
SELECT 
    query,
    calls,
    total_exec_time,
    mean_exec_time,
    rows
FROM pg_stat_statements 
WHERE mean_exec_time > 100 -- 100ms'den yavaş sorgular
ORDER BY mean_exec_time DESC;

-- Index kullanım istatistikleri
CREATE OR REPLACE VIEW index_usage AS
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_tup_read,
    idx_tup_fetch,
    idx_scan
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;

-- Table size monitoring
CREATE OR REPLACE VIEW table_sizes AS
SELECT 
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
    pg_total_relation_size(schemaname||'.'||tablename) as bytes
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- ===== MAINTENANCE PROCEDURES =====
-- Index'leri yeniden oluşturmak için prosedür
CREATE OR REPLACE FUNCTION refresh_indexes() RETURNS void AS $$
BEGIN
    REINDEX TABLE customers;
    REINDEX TABLE orders;
    REINDEX TABLE measurements;
    REINDEX TABLE users;
    REINDEX TABLE admin;
    REFRESH MATERIALIZED VIEW slow_queries;
    RAISE NOTICE 'Tüm indexler yenilendi ve istatistikler güncellendi.';
END;
$$ LANGUAGE plpgsql;

-- Veritabanı istatistiklerini güncellemek için
CREATE OR REPLACE FUNCTION update_statistics() RETURNS void AS $$
BEGIN
    ANALYZE customers;
    ANALYZE orders;
    ANALYZE measurements;
    ANALYZE users;
    ANALYZE admin;
    RAISE NOTICE 'Veritabanı istatistikleri güncellendi.';
END;
$$ LANGUAGE plpgsql;

-- Performance monitoring queries
-- En yavaş sorgular için:
-- SELECT query, mean_exec_time, calls FROM pg_stat_statements ORDER BY mean_exec_time DESC LIMIT 10; 

-- Index kullanımı kontrolü:
-- SELECT * FROM index_usage WHERE idx_scan < 100;

-- Tablo boyutları:
-- SELECT * FROM table_sizes; 