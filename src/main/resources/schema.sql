-- Schema recree a chaque demarrage (demo)
DROP TABLE IF EXISTS stocks;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id       INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20) NOT NULL
);

CREATE TABLE stocks (
    id       INT PRIMARY KEY AUTO_INCREMENT,
    symbol   VARCHAR(10) NOT NULL,
    name     VARCHAR(100) NOT NULL,
    sector   VARCHAR(60),
    price    DECIMAL(12,2),
    currency VARCHAR(5)
);
