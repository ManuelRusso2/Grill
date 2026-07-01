DROP DATABASE IF EXISTS grill;
CREATE SCHEMA grill;
USE grill;

CREATE TABLE IF NOT EXISTS utente(
    id_utente INTEGER AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100),
    nome VARCHAR(50) NOT NULL,
    cognome VARCHAR(50) NOT NULL,
    isAdmin BOOLEAN NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    telefono VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS acquisto(
    id_acquisto INTEGER AUTO_INCREMENT PRIMARY KEY,
    prezzo_totale DOUBLE NOT NULL CHECK(prezzo_totale > 0),
    data_acquisto DATETIME DEFAULT CURRENT_TIMESTAMP,
    metodo_pagamento VARCHAR(50) NOT NULL,
    indirizzo_consegna VARCHAR(50) NOT NULL,
    id_utente INTEGER NOT NULL,
    
    FOREIGN KEY(id_utente) REFERENCES utente(id_utente)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS collezione(
    id_collezione INTEGER AUTO_INCREMENT PRIMARY KEY,
    nome_collezione VARCHAR(50) NOT NULL UNIQUE,
    descrizione VARCHAR(1024),
    data_creazione DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS prodotto(
    id_prodotto INTEGER AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    descrizione VARCHAR(1024) NOT NULL,
    costo DOUBLE NOT NULL CHECK(costo > 0),
    quantita INTEGER NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    attivo BOOLEAN DEFAULT TRUE,
    id_collezione INTEGER,
    
    FOREIGN KEY(id_collezione) REFERENCES collezione(id_collezione)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ordine(
    id_acquisto INTEGER NOT NULL,
    id_prodotto INTEGER NOT NULL,
    prezzo_unitario DOUBLE NOT NULL CHECK(prezzo_unitario > 0),
    sconto DECIMAL(5, 2) NOT NULL,
    data_acquisto DATETIME DEFAULT CURRENT_TIMESTAMP,
    iva DECIMAL(4,2) NOT NULL CHECK (iva >= 0),
    quantita_acquistata INTEGER NOT NULL CHECK(quantita_acquistata > 0),
    stato_spedizione VARCHAR(20) NOT NULL,
    PRIMARY KEY(id_acquisto, id_prodotto),
    
    FOREIGN KEY(id_acquisto) REFERENCES acquisto(id_acquisto)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
        
    FOREIGN KEY(id_prodotto) REFERENCES prodotto(id_prodotto)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS recensione(
    id_recensione INTEGER AUTO_INCREMENT PRIMARY KEY,
    data_recensione DATETIME DEFAULT CURRENT_TIMESTAMP,
    descrizione VARCHAR(1024),
    valutazione DECIMAL(2,1) NOT NULL CHECK (valutazione BETWEEN 0.5 AND 5.0),
    id_prodotto INTEGER NOT NULL,
    id_utente INTEGER NOT NULL,
    
    FOREIGN KEY(id_prodotto) REFERENCES prodotto(id_prodotto)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
        
    FOREIGN KEY(id_utente) REFERENCES utente(id_utente)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS categoria(
    id_categoria INTEGER AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    descrizione VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS tipologia(
    id_prodotto INTEGER NOT NULL,
    id_categoria INTEGER NOT NULL,
    PRIMARY KEY(id_prodotto, id_categoria),
    
    FOREIGN KEY(id_prodotto) REFERENCES prodotto(id_prodotto)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
        
    FOREIGN KEY(id_categoria) REFERENCES categoria(id_categoria)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS carrello(
    id_carrello INTEGER AUTO_INCREMENT PRIMARY KEY,
    id_utente INTEGER NOT NULL UNIQUE,
        
    FOREIGN KEY(id_utente) REFERENCES utente(id_utente)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS contenuto(
    id_carrello INTEGER NOT NULL,
    id_prodotto INTEGER NOT NULL,
    quantita INTEGER NOT NULL CHECK(quantita > 0),
    PRIMARY KEY(id_carrello, id_prodotto),
    
    FOREIGN KEY(id_carrello) REFERENCES carrello(id_carrello)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
        
    FOREIGN KEY(id_prodotto) REFERENCES prodotto(id_prodotto)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);