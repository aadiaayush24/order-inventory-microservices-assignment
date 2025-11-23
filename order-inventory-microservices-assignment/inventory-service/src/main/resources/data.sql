-- Insert sample products
INSERT INTO products (id, product_id, name, description) VALUES 
(1, 'PROD-001', 'Laptop Battery', 'High capacity lithium-ion laptop battery'),
(2, 'PROD-002', 'USB Cable', 'USB-C to USB-A cable 2m'),
(3, 'PROD-003', 'Wireless Mouse', 'Ergonomic wireless mouse');

-- Insert sample inventory batches
INSERT INTO inventory_batches (id, batch_number, product_id, quantity, expiry_date, manufacturing_date) VALUES
(1, 'BATCH-001-A', 1, 50, '2025-12-31', '2024-01-15'),
(2, 'BATCH-001-B', 1, 30, '2025-06-30', '2024-06-01'),
(3, 'BATCH-001-C', 1, 75, '2026-03-31', '2024-09-15'),
(4, 'BATCH-002-A', 2, 200, '2027-01-31', '2024-02-01'),
(5, 'BATCH-002-B', 2, 150, '2026-08-31', '2024-08-01'),
(6, 'BATCH-003-A', 3, 100, '2025-11-30', '2024-05-01'),
(7, 'BATCH-003-B', 3, 80, '2026-05-31', '2024-11-01');

