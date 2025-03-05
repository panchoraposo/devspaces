INSERT INTO payment (paymentid, price, date, status) VALUES 
('PAY123456', 150.75, EXTRACT(EPOCH FROM NOW() - INTERVAL '4 days') * 1000, 'completed'),
('PAY123457', 320.50, EXTRACT(EPOCH FROM NOW() - INTERVAL '3 days') * 1000, 'pending'),
('PAY123458', 99.99, EXTRACT(EPOCH FROM NOW() - INTERVAL '2 days') * 1000, 'failed'),
('PAY123459', 500.00, EXTRACT(EPOCH FROM NOW() - INTERVAL '1 day') * 1000, 'completed'),
('PAY123460', 200.25, EXTRACT(EPOCH FROM NOW()) * 1000, 'refunded');