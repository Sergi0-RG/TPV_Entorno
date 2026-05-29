CREATE DATABASE minitpv
    DEFAULT CHARACTER SET = 'utf8mb4';
DROP TABLE IF EXISTS sale_lines;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS products;

CREATE TABLE products (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(30)     NOT NULL UNIQUE,
    name        VARCHAR(120)    NOT NULL,
    description TEXT,
    price       NUMERIC(10, 2)  NOT NULL CHECK (price >= 0),
    stock       INT             NOT NULL CHECK (stock >= 0),
    active      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sales (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sale_date   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal    NUMERIC(10, 2)  NOT NULL CHECK (subtotal >= 0),
    tax         NUMERIC(10, 2)  NOT NULL CHECK (tax >= 0),
    total       NUMERIC(10, 2)  NOT NULL CHECK (total >= 0)
);

CREATE TABLE sale_lines (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sale_id     BIGINT          NOT NULL,
    product_id  BIGINT          NOT NULL,
    quantity    INT             NOT NULL CHECK (quantity > 0),
    unit_price  NUMERIC(10, 2)  NOT NULL CHECK (unit_price >= 0),
    line_total  NUMERIC(10, 2)  NOT NULL CHECK (line_total >= 0),
    CONSTRAINT fk_sale    FOREIGN KEY (sale_id)    REFERENCES sales(id)    ON DELETE CASCADE,
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products(id)
);
INSERT INTO products (code, name, description, price, stock)
VALUES
('PROD001', 'Ratón inalámbrico', 'Ratón óptico USB inalámbrico', 12.99, 20),
('PROD002', 'Teclado USB',       'Teclado estándar con cable',   19.90, 15),
('PROD003', 'Memoria USB 32GB',  'Pendrive USB 3.0',              7.50, 50),
('PROD004', 'Cable HDMI',        'Cable HDMI 1.5 metros',         6.99, 30);
