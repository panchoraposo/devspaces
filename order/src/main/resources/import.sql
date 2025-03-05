INSERT INTO user_table (username, email, budget) VALUES 
('johndoe', 'johndoe@example.com', 100.50),
('janedoe', 'janedoe@example.com', 200.75),
('alice', 'alice@example.com', 150.00),
('bob', 'bob@example.com', 300.25);


-- Inserción de un pedido exitoso
--INSERT INTO "order" (userId, orderId, seatId, status, message, paymentId)
--VALUES 
--(1, 'ORD001', 'A3', 'success', 'Pago procesado correctamente', 'PAY123456');

-- Inserción de un pedido fallido por pago rechazado
--INSERT INTO "order" (userId, orderId, seatId, status, message, paymentId)
--VALUES 
--(2, 'ORD002', 'A2', 'failed', 'Pago rechazado, fondos insuficientes', 'PAY123457');

-- Inserción de un pedido pendiente de confirmación
--INSERT INTO "order" (userId, orderId, seatId, status, message, paymentId)
--VALUES 
--(3, 'ORD003', 'B4', 'pending', 'Esperando confirmación de pago', 'PAY003');

-- Inserción de un pedido con asiento no disponible
--INSERT INTO "order" (userId, orderId, seatId, status, message, paymentId)
--VALUES 
--(4, 'ORD004', 'C6', 'failed', 'Asiento no disponible', 'PAY004');

-- Inserción de un pedido exitoso con asiento en fila C
--INSERT INTO "order" (userId, orderId, seatId, status, message, paymentId)
--VALUES 
--(5, 'ORD005', 'C2', 'success', 'Pago procesado correctamente', 'PAY005');