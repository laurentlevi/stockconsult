-- Comptes (mots de passe en clair, VOLONTAIREMENT non securise pour la demo)
INSERT INTO users (username, password, role) VALUES ('admin', 'admin', 'ADMIN');
INSERT INTO users (username, password, role) VALUES ('john', 'password123', 'USER');
INSERT INTO users (username, password, role) VALUES ('alice', 'letmein', 'USER');

-- Cours de bourse (valeurs fictives)
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('AAPL', 'Apple Inc.', 'Technology', 231.40, 'USD');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('MSF', 'Microsoft Corporation', 'Technology', 415.22, 'USD');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('GOO', 'Alphabet Inc.', 'Technology', 168.90, 'USD');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('AMZ', 'Amazon.com Inc.', 'Consumer', 201.15, 'USD');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('TSL', 'Tesla Inc.', 'Automotive', 342.68, 'USD');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('NVD', 'NVIDIA Corporation', 'Technology', 138.07, 'USD');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('MC', 'LVMH Moet Hennessy', 'Luxury', 645.30, 'EUR');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('AIR', 'Airbus SE', 'Aerospace', 168.44, 'EUR');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('OR', 'L''Oreal SA', 'Consumer', 378.55, 'EUR');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('BNP', 'BNP Paribas', 'Banking', 72.19, 'EUR');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('SAN', 'Sanofi', 'Healthcare', 98.72, 'EUR');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('TTE', 'TotalEnergies SE', 'Energy', 55.83, 'EUR');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('DG', 'Vinci SA', 'Construction', 110.25, 'EUR');
INSERT INTO stocks (symbol, name, sector, price, currency) VALUES ('KER', 'Kering', 'Luxury', 232.90, 'EUR');
