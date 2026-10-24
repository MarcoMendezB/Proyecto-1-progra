-- SECCIÓN 1: Base de datos y tabla
CREATE DATABASE IF NOT EXISTS soda_pos
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE soda_pos;

CREATE TABLE IF NOT EXISTS producto (
                                        codigo VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    CONSTRAINT chk_precio CHECK (precio >= 0),
    CONSTRAINT chk_stock CHECK (stock >= 0)
    );

-- SECCIÓN 2: Usuarios y Privilegios Mínimos
-- (Marco debe cambiar las contraseñas antes de ejecutarlo)
CREATE USER IF NOT EXISTS 'soda_definer'@'localhost' IDENTIFIED BY 'definer123';
GRANT SELECT, INSERT, UPDATE, DELETE ON soda_pos.producto TO 'soda_definer'@'localhost';

CREATE USER IF NOT EXISTS 'soda_app'@'localhost' IDENTIFIED BY 'app123';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'soda_app'@'localhost';

-- SECCIÓN 3: Procedimientos Almacenados (CRUD)
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_listar_productos$$
CREATE DEFINER='soda_definer'@'localhost' PROCEDURE sp_listar_productos()
SQL SECURITY DEFINER
BEGIN
SELECT codigo, nombre, categoria, precio, stock FROM producto ORDER BY nombre;
END$$

DROP PROCEDURE IF EXISTS sp_insertar_producto$$
CREATE DEFINER='soda_definer'@'localhost' PROCEDURE sp_insertar_producto(
  IN p_codigo VARCHAR(20), IN p_nombre VARCHAR(80), IN p_categoria VARCHAR(50),
  IN p_precio DECIMAL(10,2), IN p_stock INT
)
SQL SECURITY DEFINER
BEGIN
INSERT INTO producto(codigo, nombre, categoria, precio, stock)
VALUES (p_codigo, p_nombre, p_categoria, p_precio, p_stock);
END$$

DROP PROCEDURE IF EXISTS sp_actualizar_producto$$
CREATE DEFINER='soda_definer'@'localhost' PROCEDURE sp_actualizar_producto(
  IN p_codigo VARCHAR(20), IN p_nombre VARCHAR(80), IN p_categoria VARCHAR(50),
  IN p_precio DECIMAL(10,2), IN p_stock INT
)
SQL SECURITY DEFINER
BEGIN
UPDATE producto SET nombre=p_nombre, categoria=p_categoria, precio=p_precio, stock=p_stock
WHERE codigo=p_codigo;

IF ROW_COUNT() = 0 AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo=p_codigo) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No existe un producto con ese código';
END IF;
END$$

DROP PROCEDURE IF EXISTS sp_eliminar_producto$$
CREATE DEFINER='soda_definer'@'localhost' PROCEDURE sp_eliminar_producto(IN p_codigo VARCHAR(20))
SQL SECURITY DEFINER
BEGIN
DELETE FROM producto WHERE codigo=p_codigo;
IF ROW_COUNT() = 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No existe un producto con ese código';
END IF;
END$$

DELIMITER ;

-- SECCIÓN 4: Permisos de la aplicación
GRANT EXECUTE ON soda_pos.* TO 'soda_definer'@'localhost';

GRANT EXECUTE ON PROCEDURE soda_pos.sp_listar_productos TO 'soda_app'@'localhost';
GRANT EXECUTE ON PROCEDURE soda_pos.sp_insertar_producto TO 'soda_app'@'localhost';
GRANT EXECUTE ON PROCEDURE soda_pos.sp_actualizar_producto TO 'soda_app'@'localhost';
GRANT EXECUTE ON PROCEDURE soda_pos.sp_eliminar_producto TO 'soda_app'@'localhost';
FLUSH PRIVILEGES;


-- TABLAS PARA LA RELACIÓN N:M (Pedidos y Detalles)
CREATE TABLE IF NOT EXISTS pedido (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      cliente VARCHAR(100) NOT NULL,
    tipo_pedido VARCHAR(20) NOT NULL, -- 'LOCAL' o 'EXPRESS'
    total DECIMAL(10,2) NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS detalle_pedido (
    pedido_id INT,
    producto_codigo VARCHAR(20),
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (pedido_id, producto_codigo),
    FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_codigo) REFERENCES producto(codigo)
    );

-- Permisos extra para el definidor
GRANT SELECT, INSERT, UPDATE, DELETE ON soda_pos.pedido TO 'soda_definer'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON soda_pos.detalle_pedido TO 'soda_definer'@'localhost';