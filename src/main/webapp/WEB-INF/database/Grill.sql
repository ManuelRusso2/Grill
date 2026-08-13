DROP DATABASE IF EXISTS grill;
CREATE SCHEMA grill;
USE grill;

CREATE TABLE IF NOT EXISTS utente(
    id_utente INTEGER AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    nome VARCHAR(50) NOT NULL,
    cognome VARCHAR(50) NOT NULL,
    isAdmin BOOLEAN NOT NULL,
    password VARCHAR(255) NOT NULL,
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
        ON DELETE RESTRICT 
);

CREATE TABLE IF NOT EXISTS collezione(
    id_collezione INTEGER AUTO_INCREMENT PRIMARY KEY,
    nome_collezione VARCHAR(50) NOT NULL UNIQUE,
    descrizione VARCHAR(2000),
    data_creazione DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS prodotto(
    id_prodotto INTEGER AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    descrizione VARCHAR(1024) NOT NULL,
    costo DOUBLE NOT NULL CHECK(costo > 0),
    quantita INTEGER NOT NULL,
    attivo BOOLEAN DEFAULT TRUE,
    immagine VARCHAR(255) NOT NULL,
    id_collezione INTEGER,
    taglie VARCHAR(255),
    
    FOREIGN KEY(id_collezione) REFERENCES collezione(id_collezione)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ordine(
    id_acquisto INTEGER NOT NULL,
    id_prodotto INTEGER NOT NULL,
    taglia VARCHAR(50) NOT NULL,
    prezzo_unitario DOUBLE NOT NULL CHECK(prezzo_unitario > 0),
    iva DECIMAL(4,2) NOT NULL CHECK (iva >= 0),
    quantita_acquistata INTEGER NOT NULL CHECK(quantita_acquistata > 0),
    stato_spedizione VARCHAR(20) NOT NULL,
    PRIMARY KEY(id_acquisto, id_prodotto, taglia),
    
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

CREATE TABLE IF NOT EXISTS prodotto_categoria(
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
    taglia VARCHAR(50) NOT NULL,
    quantita INTEGER NOT NULL CHECK(quantita > 0),
    PRIMARY KEY(id_carrello, id_prodotto, taglia),
    
    FOREIGN KEY(id_carrello) REFERENCES carrello(id_carrello)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
        
    FOREIGN KEY(id_prodotto) REFERENCES prodotto(id_prodotto)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


-- ===================================================
-- POPOLAMENTO TABELLA COLLEZIONE
-- ===================================================

INSERT INTO collezione (nome_collezione, descrizione) VALUES 
('peace and love', 'PEACE, LOVE, GRILL.\n\nPace nella mente, amore nelle vibrazioni e il calore giusto per accendere la tua giornata. Benvenuto nella nuova era dello streetwear targato Grill.\n\nLa collezione Peace and Love nasce per chi non vuole passare inosservato, unendo uno stile urban-utility deciso a un’anima sfacciatamente positiva. Dalle giacche bomber in pelle dal sapore varsity e vintage, alle felpe oversize ultra-comode, fino alle t-shirt grafiche e gli accessori essenziali (cappellini, marsupi e borraccia termica): ogni pezzo è pensato per accompagnarti ovunque con la giusta attitude.\n\nDUE VARIANTI, LO STESSO VIBE ICONICO\nScegli l''energia che si adatta meglio al tuo stile:\n\nDark & Bold Edition: Toni scuri e decisi, con grafiche rosse e bianche a contrasto su fondo nero e crema per un look urban forte e d''impatto.\n\nEarth & Olive Edition: Palette di colori caldi e naturali, con tonalità verde militare, panna ed inserti terrosi, per uno stile streetwear più morbido ma sempre dal carattere inconfondibile.'),
('flowless', 'FLOWLESS COLLECTION\n\nRAGGIUNGI IL TUO STATO DI FLOW. MENTE LIBERA. STILE SENZA COMPROMESSI.\n\nSei pronto a dominare la strada? GRILL presenta la nuova ed esclusiva collezione FW24: FLOWLESS.\n\nQuesta non è solo abbigliamento, è un''attitude. "Flow State. Mind Free." è il nostro mantra, un invito a vivere il momento senza farsi ostacolare da nulla. E per farlo con stile, abbiamo creato una linea streetwear essenziale, premium e unicamente audace, tutta in total black.'),
('freeflow', 'FREEFLOW\n\nEXPLORE EVERYWHERE, IN TOTAL SYNC.\n\nRompi le regole, esplora nuovi confini. La nuova drop Freeflow firmata Grill è pensata per chi si muove in totale armonia con la città e con se stesso. Un''estetica minimal ma d''impatto, creata per esplorare nuove frontiere di identità e spazio.'),
('reality', 'REALITY COLLECTION\n\nGRILL YOUR REALITY.\n\nNon limitarti a vivere la realtà: modellala, trasformala, GRILL IT.\n\nLa nuova collezione REALITY targata Grill (FW24) è una statement capsule creata per chi ha una visione chiara e non teme di mostrarla. Caratterizzata da una tipografia audace avvolta da grafiche orbitali/astratte, questa linea fonde un’estetica streetwear premium a una vestibilità d’impatto.\n\nDalle giacche bomber in pelle coordinata, alle felpe hoodie oversize (disponibili sia in tinta che in contrasto beige/crema), fino a t-shirt, cappellini, marsupi e borraccia termica: il kit completo per dominare ogni scenario urbano.\n\n4 PALETTE ESCLUSIVE per esprimere il tuo mindset:\nDeep Navy: Blu notte profondo ed elegante, per uno stile minimal, freddo e d''impatto.\nWarm Earth (Brown): Toni caldi del marrone cioccolato e sabbia, perfetti per un look streetwear sofisticato e contemporaneo.\nBurgundy / Wine: Una sfumatura bordeaux intensa e audace, studiata per chi vuole distinguersi con personalità.\nForest Olive: Verde militare e toni naturali per un''attitudine utility e al tempo stesso ricercata.'),
('peace and love X flowless', 'PEACE AND LOVE X FLOWLESS\n\nTHE ULTIMATE CROSSOVER.\n\nQuando due delle collezioni più iconiche di Grill si incontrano, nasce un nuovo punto di riferimento per lo streetwear.\n\nPeace and Love X Flowless è la fusione perfetta tra l''energia positiva del messaggio "Peace, Love, Grill" e l''attitude total black, affilata e impeccabile del mondo Flowless. Un''esplosione di stile monocromatico dove il logo del grill fiammeggiante si unisce al lettering arcuato Flowless, creando un contrasto visivo unico su fondo nero profondo.'),
('speed', 'SPEED COLLECTION\n\nGRILL. BEYOND SPEED.\n\nAllaccia le cinture e prepara il tuo stile a bruciare l''asfalto. Grill presenta la collezione SPEED (FW24): una capsule pensata per chi vive la vita al massimo, spinge sempre oltre i propri limiti e non si guarda mai indietro.\n\nIspirata all''adrenalina del motorsport, alla velocità urbana e alla cultura racing, la linea si distingue per una grafica dinamica con effetto "motion blur" e dettagli graffiati che trasmettoni energia pura.\n\nDalle felpe hoodie oversize alle t-shirt grafiche, fino ad accessori ad alte prestazioni come cappellini da baseball con visiera, marsupi da viaggio e borraccia termica: ogni pezzo è progettato per accompagnare il tuo ritmo senza freni.\n\n3 COLORAZIONI AD ALTA VELOCITÀ:\nAsphalt Black: Il classico streetwear nero notte con dettagli sfumati a contrasto, per un look serale, aggressivo e decisamente racing.\nOff-Road Sand / Cream: Tonalità sabbiose e calde con accenti rosso corsa, ispirate alle avventure rally, al deserto e alle gare motocross.\nNight Racing Olive: Verde militare e dettagli verde acido a contrasto, perfetto per chi cerca uno stile utility, elettrico e fuori dagli schemi.'),
('built different', 'BUILT DIFFERENT.\n\nBUILT DIFFERENT. BUILT GRILL.\nNon siamo fatti per omologarci. La nuova capsule Built Different firmata Grill è creata per chi si distingue dalla massa, chi ha una "Grill Mentality" nel sangue e non si accontenta della media.\n\nCaratterizzata dal doppio logo GRILL GRILL in un font tondeggiante, audace e dal sapore retrò-streetwear, questa linea mette al centro l''autenticità e la qualità dei "reali ingredienti" che compongono il tuo stile.\n\nUn total look coordinato che comprende felpe hoodie oversize ultra-morbide, t-shirt grafiche dal taglio relaxed, cappellini da baseball, borraccia termica e marsupio utility.\n\n4 SHADES PER OGNI PERSONALITÀ:\nChocolate Brown: Una tonalità marrone scuro calda, intensa e super di tendenza per un look earth-tone impeccabile.\nCream / Off-White: Toni chiari, puliti ed essenziali che mettono in risalto la grafica e donano luce al tuo outfit.\nWashed Charcoal / Slate: Un grigio scuro/antracite dall''effetto vintage urbano, perfetto per le serate in città.\nClassic Pitch Black: Il nero profondo e intramontabile, azzeccatissimo con i dettagli ad alto contrasto.'),
('bloom', 'BLOOM COLLECTION\n\nGRILL IN BLOOM: PEACE, COLOR & FREEDOM.\n\nFai sbocciare il tuo stile con un tuffo negli anni ''70! Grill presenta BLOOM, la nuova capsule Free Spirit Streetwear pensata per le anime libere, gli amanti del colore e chi vive fuori dagli schemi.\n\nIspirata alla cultura psichedelica, ai festival retro e alla spensieratezza hippie, questa linea unisce grafiche floreali nostalgiche, illustrazioni di tramonti sognanti e font ondulati dal sapore vintage, il tutto rielaborato in chiave moderna e streetwear.'),
('metal', 'METAL COLLECTION\n\nGRILL NOISE EMPIRE — FORGED LOUD. BUILT GRILL.\n\nAlza il volume e fai tremare la strada. Grill presenta METAL, la capsule più estrema, aggressiva e senza filtri mai creata. Ispirata alla cultura heavy metal, all''estetica metalcore/goth e all''energia grezza dei festival underground, la linea Noise Empire è fatta per chi non ha paura di farsi sentire.\n\nGrafiche impattanti a tema teschio sanguigno, font spigolosi stile band metal, dettagli glitch e lavaggi acid-wash/washed black si fondeno per dare vita a un''estetica dark-streetwear assolutamente devastante.'),
('devil', 'DEVIL COLLECTION\n\nGRILL HELLFIRE — BORN DIFFERENT. BURN GRILL.\n\nAccendi le fiamme del tuo stile. Grill presenta DEVIL, la nuova capsule Hellfire pensata per chi non ha paura di mostrare il proprio lato più oscuro, provocatorio e ribelle.\n\nIspirata all''estetica gothic-streetwear, al mondo occult-urban e all''energia pura del fuoco infernale, questa linea combina un font gotico affilato, illustrazioni dettagliate di demoni cornuti, dettagli di fiamme sulle maniche e simboli mistici, il tutto in un contrasto infuocato su fondo nero profondo.'),
('elevate', 'ELEVATE COLLECTION\n\nTHE CROWN JEWEL OF GRILL — REFINED STREETWEAR\n\nDimentica tutto quello che hai visto finora. Se c''è una collezione che incarna l''essenza stessa di Grill portandola al suo livello supremo, è ELEVATE. Questa non è una semplice capsule: è l''apice, il capolavoro, il Santo Graal del nostro brand.\n\nCreata per chi non si accontenta del "solito" streetwear e vuole dominare la scena, ELEVATE fonde la grinta della street culture con il lusso contemporaneo più sofisticato (Refined. Modern. Timeless.). Pelle di prima scelta, cotoni ad altissima grammatura, ricami tridimensionali ad alta densità e una palette total black magnetica: è la collezione definitiva per chi pretende solo il meglio.');


-- ===================================================
-- POPOLAMENTO TABELLA PRODOTTO
-- ===================================================

INSERT INTO prodotto (nome, descrizione, costo, quantita, attivo, immagine, id_collezione, taglie) VALUES 

-- 1. PEACE AND LOVE (ID 1 - 9)
('Good Vibes Leather Varsity Jacket - Dark', 'Giacca bomber varsity in pelle nera con grafiche rosse e bianche a contrasto.', 249.90, 20, true, 'images/Giacche/Peace_And_Love/Nero-Retro.png', 1, 'S, M, L, XL'),
('Good Vibes Leather Varsity Jacket - Olive', 'Giacca bomber varsity in pelle verde militare con inserti panna e toni terrosi.', 249.90, 20, true, 'images/Giacche/Peace_And_Love/Verde-Fronte.png', 1, 'M, L, XL'),
('Spread Love Oversized Hoodie - Dark', 'Felpa oversize con cappuccio in cotone pesante nero con grafica Peace & Love.', 89.90, 35, true, 'images/Felpe/Peace_And_Love/Nero-Retro.png', 1, 'S, M, L, XL, XXL'),
('Spread Love Oversized Hoodie - Cream', 'Felpa oversize color crema con cappuccio e grafiche calde ad alta densita.', 89.90, 35, true, 'images/Felpe/Peace_And_Love/Crema-Retro.png', 1, 'S, M, L'),
('Stay Positive Graphic Tee - White', 'T-shirt grafica relaxed fit con stampa posizionata.', 45.00, 50, true, 'images/Magliette/Peace_And_Love/Bianca-Fronte.png', 1, 'S, M, L, XL'),
('Stay Positive Graphic Tee - Cream', 'T-shirt grafica color panna con dettagli verde militare, nero e rosso e logo Grill.', 45.00, 50, true, 'images/Magliette/Peace_And_Love/Crema-Fronte.png', 1, 'M, L, XL'),
('Peace and Love Cap', 'Cappellino con visiera e ricamo frontale Peace, Love, Grill.', 35.00, 40, true, 'images/Cappelli/Peace_And_Love/Nero.png', 1, NULL),
('Peace and Love Bottle', 'Borraccia termica in acciaio inox 500ml con finitura opaca.', 29.90, 30, true, 'images/Borracce/Peace_And_Love/Nera.png', 1, NULL),
('Peace and Love Bag', 'Marsupio utility regolabile con tasche multiple e stampa ad impatto.', 49.90, 25, true, 'images/Marsupi/Peace_And_Love/Nero.png', 1, NULL),

-- 2. FLOWLESS (ID 10 - 14)
('Flow State Leather Bomber', 'Giacca bomber in vera pelle total black con finiture minimal e lining personalizzato.', 279.90, 15, true, 'images/Giacche/Flowless/Nero-Fronte.png', 2, 'S, M, L'),
('Pure Flow Graphic Tee', 'T-shirt streetwear nera con stampa gommata tono su tono.', 49.90, 45, true, 'images/Magliette/Flowless/Nera-Fronte.png', 2, 'S, M, L, XL'),
('Flowless Cap', 'Cappellino strutturato total black con logo arcuato Flowless.', 35.00, 40, true, 'images/Cappelli/Flowless/Nero.png', 2, NULL),
('Flowless Bottle', 'Borraccia termica total black opaca ad alto isolamento.', 29.90, 30, true, 'images/Borracce/Flowless/Nera.png', 2, NULL),
('Flowless Bag', 'Borsa crossbody tattica in nylon ad alta resistenza.', 55.00, 20, true, 'images/Marsupi/Flowless/Nero.png', 2, NULL),

-- 3. FREEFLOW (ID 15 - 21)
('Total Sync Heavy Hoodie', 'Felpa pesante con cappuccio doppio strato ed estetica urban minimal.', 99.90, 25, true, 'images/Felpe/Freeflow/Nero-Fronte.png', 3, 'S, M, L, XL'),
('New Frontier Crewneck', 'Maglione girocollo in maglia tecnica essenziale per esplorazione urbana.', 85.00, 30, true, 'images/Felpe/Freeflow/Crema-Fronte.png', 3, 'M, L, XL'),
('Exploration Relaxed Tee', 'T-shirt dal taglio morbido e traspirante con stampa posteriore.', 42.00, 40, true, 'images/Magliette/Freeflow/Grigio-Fronte-1.png', 3, 'S, M, L, XL'),
('Freeflow Cap', 'Cappellino baseball rigido con regolatore in metallo.', 35.00, 35, true, 'images/Cappelli/Freeflow/Nero.png', 3, NULL),
('Freeflow Beanie', 'Berretto in maglia a costine elasticizzata con patch gommata.', 28.00, 50, true, 'images/Cappelli/Freeflow/Crema.png', 3, NULL),
('Freeflow Bottle', 'Borraccia ergonomica satinata con gancio da moschettone.', 29.90, 30, true, 'images/Borracce/Freeflow/Nera.png', 3, NULL),
('Freeflow Bag', 'Zaino monospalla compatto con scomparto hi-tech.', 59.90, 20, true, 'images/Marsupi/Freeflow/Nero.png', 3, NULL),

-- 4. REALITY (ID 22 - 30)
('Visionary Leather Jacket - Deep Navy', 'Giacca in pelle coordinata tonalita blu notte profondo.', 269.90, 12, true, 'images/Giacche/Reality/Blu-Fronte.png', 4, 'S, M, L'),
('Visionary Leather Jacket - Red', 'Giacca in pelle rossa sofisticata e contemporanea.', 269.90, 12, true, 'images/Giacche/Reality/Rosso-Retro.png', 4, 'M, L'),
('Reality Orbit Heavy Hoodie - Deep Navy', 'Felpa oversize blu notte con grafica orbitale e tipografia astratta.', 89.90, 25, true, 'images/Felpe/Reality/Blu-Fronte.png', 4, 'S, M, L, XL'),
('Reality Orbit Heavy Hoodie - Cream', 'Felpa pesante crema con grafica Orbit gommata.', 89.90, 25, true, 'images/Felpe/Reality/Crema-Fronte.png', 4, 'S, M, L'),
('True Mindset Boxy Tee - Red', 'T-shirt boxy fit bordeaux con stampa statement Reality.', 45.00, 35, true, 'images/Magliette/Reality/Rosso-Fronte.png', 4, 'S, M, L, XL, XXL'),
('True Mindset Boxy Tee - Forest Olive', 'T-shirt boxy fit verde foresta con dettagli a contrasto.', 45.00, 35, true, 'images/Magliette/Reality/Verde-Fronte.png', 4, 'M, L, XL'),
('Reality Cap', 'Cappellino con ricamo orbitale tridimensionale.', 35.00, 40, true, 'images/Cappelli/Reality/Blu.png', 4, NULL),
('Reality Bottle', 'Borraccia termica 750ml con stampa Reality Orbit.', 32.00, 25, true, 'images/Borracce/Reality/Blu.png', 4, NULL),
('Reality Bag', 'Marsupio scomparto doppio per uso quotidiano.', 48.00, 30, true, 'images/Marsupi/Reality/Blu.png', 4, NULL),

-- 5. PEACE AND LOVE X FLOWLESS (ID 31 - 36)
('Crossover Hybrid Leather Bomber', 'Bomber ibrido in pelle total black con dettagli crossover ricamati.', 299.90, 15, true, 'images/Giacche/Flowless_X_Peace_And_Love/Nero-Retro.png', 5, 'M, L, XL'),
('Flow & Love Heavy Hoodie', 'Felpa pesante nera con fusione tra logo fiammeggiante e font Flowless.', 99.90, 30, true, 'images/Felpe/Flowless_X_Peace_And_Love/Nero-Retro.png', 5, 'S, M, L, XL'),
('Dual Identity Graphic Tee', 'T-shirt nera a contrasto ad alta densita di stampa.', 49.90, 40, true, 'images/Magliette/Flowless_X_Peace_And_Love/Nera-Fronte-1.png', 5, 'S, M, L, XL'),
('Peace and Love x Flowless Cap', 'Cappellino nero con doppio logo gommato sul fronte.', 38.00, 35, true, 'images/Cappelli/Flowless_X_Peace_And_Love/Nero.png', 5, NULL),
('Peace and Love x Flowless Bottle', 'Borraccia termica total black crossover ediz. limitata.', 35.00, 25, true, 'images/Borracce/Flowless_X_Peace_And_Love/Nera.png', 5, NULL),
('Peace and Love x Flowless Bag', 'Tracolla tecnica rinforzata con fibbie metalliche.', 65.00, 20, true, 'images/Marsupi/Flowless_X_Peace_And_Love/Nero.png', 5, NULL),

-- 6. SPEED (ID 37 - 44)
('Fast Driven Motion Hoodie - Asphalt Black', 'Felpa racing nera con grafiche motion blur sfumate a contrasto.', 92.00, 30, true, 'images/Felpe/Speed/Nero-Fronte.png', 6, 'S, M, L, XL'),
('Fast Driven Motion Hoodie - Off-Road Sand', 'Felpa sabbia e crema con accenti rosso corsa e dettagli graffiati.', 92.00, 30, true, 'images/Felpe/Speed/Crema-Fronte.png', 6, 'M, L, XL'),
('Fast Driven Motion Hoodie - Night Racing Olive', 'Felpa verde militare con dettagli verde acido ad alta visibilita.', 92.00, 30, true, 'images/Felpe/Speed/Verde-Fronte.png', 6, 'M, L'),
('Beyond Speed Racing Tee - Asphalt Black', 'T-shirt grafica traspirante motorsport ad alte prestazioni.', 45.00, 40, true, 'images/Magliette/Speed/Nera-Fronte.png', 6, 'S, M, L, XL'),
('Beyond Speed Racing Tee - Off-Road Sand', 'T-shirt deserto e sabbia con stampa grafica stile motocross.', 45.00, 40, true, 'images/Magliette/Speed/Crema-Fronte.png', 6, 'S, M, L'),
('Speed Cap', 'Cappellino racing da baseball con visiera sagomata.', 35.00, 45, true, 'images/Cappelli/Speed/Nero.png', 6, NULL),
('Speed Bottle', 'Borraccia sportiva con tappo rapido a pressione.', 28.00, 35, true, 'images/Borracce/Speed/Nera.png', 6, NULL),
('Speed Bag', 'Marsupio tecnico da viaggio ad aggancio rapido.', 52.00, 25, true, 'images/Marsupi/Speed/Nero.png', 6, NULL),

-- 7. BUILT DIFFERENT (ID 45 - 54)
('Stand Out Heavy Hoodie - Chocolate Brown', 'Felpa marrone scuro ultra-morbida con doppio logo GRILL retro.', 89.90, 25, true, 'images/Felpe/Built_Different/Marrone-Fronte.png', 7, 'S, M, L, XL'),
('Stand Out Heavy Hoodie - Cream', 'Felpa panna essenziale con grafica centrale marrone a contrasto.', 89.90, 25, true, 'images/Felpe/Built_Different/Crema-Fronte.png', 7, 'M, L, XL'),
('Stand Out Heavy Hoodie - Black', 'Felpa nera con effetto vintage lavato.', 89.90, 25, true, 'images/Felpe/Built_Different/Nero-Fronte.png', 7, 'S, M, L, XL'),
('Real Ingredients Boxy Tee - Black', 'T-shirt dal taglio relaxed nero profondo con stampa ad alta densita.', 42.00, 40, true, 'images/Magliette/Built_Different/Nera-Fronte.png', 7, 'S, M, L, XL'),
('Real Ingredients Boxy Tee - Cream', 'T-shirt chiara vestibilita boxy con ricamo sul petto.', 42.00, 40, true, 'images/Magliette/Built_Different/Crema-Fronte.png', 7, 'S, M, L'),
('Built Different Cap', 'Cappellino stile retrò con cinturino in pelle.', 35.00, 40, true, 'images/Cappelli/Built_Different/Nero.png', 7, NULL),
('Built Different Bottle - Cream', 'Borraccia termica con finitura gommata variante Crema.', 29.90, 30, true, 'images/Borracce/Built_Different/Crema.png', 7, NULL),
('Built Different Bottle - Brown', 'Borraccia termica con finitura gommata variante Marrone.', 29.90, 30, true, 'images/Borracce/Built_Different/Marrone.png', 7, NULL),
('Built Different Bottle - Black', 'Borraccia termica con finitura gommata variante Nera.', 29.90, 30, true, 'images/Borracce/Built_Different/Nera.png', 7, NULL),
('Built Different Bag', 'Marsupio utility capiente per tutti i giorni.', 45.00, 30, true, 'images/Marsupi/Built_Different/Nero.png', 7, NULL),

-- 8. BLOOM (ID 55 - 60)
('70s Soul Bloom Varsity Bomber', 'Giacca bomber stile anni 70 con patch floreali e colletto a costine.', 219.90, 15, true, 'images/Giacche/Bloom/Crema-Retro.png', 8, 'M, L, XL'),
('Free Spirit Bloom Hoodie', 'Felpa colorata con grafica tramonto psichedelica e font ondulato.', 85.00, 30, true, 'images/Felpe/Bloom/Crema-Fronte.png', 8, 'S, M, L'),
('Peace & Freedom Retro Tee', 'T-shirt stile vintage con illustrazioni floreali e toni caldi.', 42.00, 45, true, 'images/Magliette/Bloom/Crema-Retro.png', 8, 'S, M, L, XL'),
('Bloom Cap', 'Cappellino ricamato con motifs floreali multicolor.', 32.00, 35, true, 'images/Cappelli/Bloom/Crema.png', 8, NULL),
('Bloom Bottle - Crema', 'Borraccia con pattern floreale nostalgico variante Crema.', 28.00, 30, true, 'images/Borracce/Bloom/Crema.png', 8, NULL),
('Bloom Bag', 'Borsa shopper in tela di cotone pesante ricamata.', 39.90, 25, true, 'images/Marsupi/Bloom/Crema.png', 8, NULL),

-- 9. METAL (ID 61 - 66)
('Noise Empire Flight Bomber', 'Giacca bomber da volo stile metalcore con dettagli distressed e zip metalliche.', 239.90, 15, true, 'images/Giacche/Metal/Nero-Retro.png', 9, 'M, L, XL'),
('Heavy Noise Acid-Wash Hoodie', 'Felpa con lavaggio acido grigio scuro e grafica teschio sanguigno.', 95.00, 30, true, 'images/Felpe/Metal/Nero-Fronte.png', 9, 'S, M, L, XL'),
('Skull Empire Washed Tee', 'T-shirt con lavaggio vintage washed e font spigoloso band metal.', 48.00, 50, true, 'images/Magliette/Metal/Nera-Retro.png', 9, 'S, M, L'),
('Metal Cap', 'Cappellino nero distrutturato con spille metalliche e font gothic.', 35.00, 35, true, 'images/Cappelli/Metal/Nero.png', 9, NULL),
('Metal Bottle', 'Borraccia in metallo grezzo satinato con incisione laser.', 32.00, 30, true, 'images/Borracce/Metal/Nera.png', 9, NULL),
('Metal Bag', 'Borsa tracolla rinforzata con borchie e chiusure in metallo.', 59.90, 20, true, 'images/Marsupi/Metal/Nero.png', 9, NULL),

-- 10. DEVIL (ID 67 - 72)
('Hellfire MA-1 Flight Bomber', 'Giacca bomber MA-1 con fiamme sulle maniche e fodera interna rossa inferno.', 249.90, 15, true, 'images/Giacche/Devil/Nero-Fronte-Retro.png', 10, 'S, M, L, XL'),
('Infernal Flame Heavy Hoodie', 'Felpa con cappuccio pesante e illustrazioni dettagliate di demoni cornuti.', 95.00, 30, true, 'images/Felpe/Devil/Nero-Fronte.png', 10, 'M, L, XL'),
('Horned Demon Boxy Tee', 'T-shirt vestibilita boxy con simboli mistici e grafica infuocata.', 48.00, 45, true, 'images/Magliette/Devil/Nera-Fronte-Retro.png', 10, 'S, M, L, XL'),
('Devil Cap', 'Cappellino gothic con ricamo di corna rosse in rilievo.', 35.00, 40, true, 'images/Cappelli/Devil/Nero.png', 10, NULL),
('Devil Bottle - Nera', 'Borraccia nera lucida con dettagli rossi ad alto contrasto.', 29.90, 30, true, 'images/Borracce/Devil/Nera.png', 10, NULL),
('Devil Bag', 'Marsupio tattico con moschettoni metallici e tirazip fiammeggianti.', 55.00, 25, true, 'images/Marsupi/Devil/Nero.png', 10, NULL),

-- 11. ELEVATE (ID 73 - 78)
('Masterpiece Leather Jacket', 'Giacca in pelle di agnello di prima scelta con finiture sartoriali e dettagli di lusso.', 389.90, 10, true, 'images/Giacche/Elevate/Nero-Regular-Fronte.png', 11, 'M, L'),
('Crown Oversized Leather Bomber', 'Bomber in pelle oversize con ricami tridimensionali ad alta densita.', 349.90, 10, true, 'images/Giacche/Elevate/Nero-Bomber-Fronte.png', 11, 'S, M, L'),
('Quiet Luxury Heavy Hoodie', 'Felpa in cotone pettinato ad altissima grammatura (500gsm) total black magnetica.', 120.00, 20, true, 'images/Felpe/Elevate/Nero-Fronte.png', 11, 'S, M, L, XL'),
('Elevate Cap', 'Cappellino di lusso con placchetta in metallo dorato/satinato.', 45.00, 30, true, 'images/Cappelli/Elevate/Nero.png', 11, NULL),
('Elevate Bag', 'Borsa da viaggio premium in pelle e nylon ad alta densita.', 89.90, 15, true, 'images/Marsupi/Elevate/Nero.png', 11, NULL),
('Elevate Bottle - Nera', 'Borraccia termica di lusso con finitura opaca e dettagli incisi variante Nera.', 38.00, 25, true, 'images/Borracce/Elevate/Nera.png', 11, NULL);


-- ===================================================
-- POPOLAMENTO UTENTI ADMIN
-- ===================================================

INSERT INTO utente (email, nome, cognome, isAdmin, password, telefono) VALUES 
('antonio.sicignano@grill.it', 'Antonio', 'Sicignano', true, SHA2('admin123', 256), '3331234567'),
('manuel.russo@grill.it', 'Nello Manuel', 'Russo', true, SHA2('admin123', 256), '3337654321');


-- ===================================================
-- POPOLAMENTO TABELLA CATEGORIA
-- Categorie: 1=Giacche, 2=Felpe, 3=T-Shirt, 4=Accessori, 5=Pelle
-- ===================================================

INSERT INTO categoria (nome, descrizione) VALUES 
('Giacche', 'Giacche, bomber varsity e capispalla per ogni stagione.'),
('Felpe', 'Felpe oversize, hoodie con cappuccio e girocollo essenziali.'),
('T-Shirt', 'T-shirt grafiche, magliette boxy e relaxed fit.'),
('Accessori', 'Cappellini, borracce termiche, borse e accessori streetwear.'),
('Pelle', 'Capi ed edizioni speciali realizzati in vera pelle e finiture premium.');


-- ===================================================
-- POPOLAMENTO TABELLA PRODOTTO_CATEGORIA (CORRETTO)
-- ===================================================

INSERT INTO prodotto_categoria (id_prodotto, id_categoria) VALUES 

-- 1. PEACE AND LOVE (Prodotti 1 - 9)
(1, 1), (1, 5),
(2, 1), (2, 5),
(3, 2),
(4, 2),
(5, 3),
(6, 3),
(7, 4),
(8, 4),
(9, 4),

-- 2. FLOWLESS (Prodotti 10 - 14)
(10, 1), (10, 5),
(11, 3),
(12, 4),
(13, 4),
(14, 4),

-- 3. FREEFLOW (Prodotti 15 - 21)
(15, 2),
(16, 2),
(17, 3),
(18, 4),
(19, 4),
(20, 4),
(21, 4),

-- 4. REALITY (Prodotti 22 - 30)
(22, 1), (22, 5),
(23, 1), (23, 5),
(24, 2),
(25, 2),
(26, 3),
(27, 3),
(28, 4),
(29, 4),
(30, 4),

-- 5. PEACE AND LOVE X FLOWLESS (Prodotti 31 - 36)
(31, 1), (31, 5),
(32, 2),
(33, 3),
(34, 4),
(35, 4),
(36, 4),

-- 6. SPEED (Prodotti 37 - 44)
(37, 2),
(38, 2),
(39, 2),
(40, 3),
(41, 3),
(42, 4),
(43, 4),
(44, 4),

-- 7. BUILT DIFFERENT (Prodotti 45 - 54)
(45, 2),
(46, 2),
(47, 2),
(48, 3),
(49, 3),
(50, 4),
(51, 4),
(52, 4),
(53, 4),
(54, 4),

-- 8. BLOOM (Prodotti 55 - 60)
(55, 1),
(56, 2),
(57, 3),
(58, 4),
(59, 4),
(60, 4),

-- 9. METAL (Prodotti 61 - 66)
(61, 1),
(62, 2),
(63, 3),
(64, 4),
(65, 4),
(66, 4),

-- 10. DEVIL (Prodotti 67 - 72)
(67, 1),
(68, 2),
(69, 3),
(70, 4),
(71, 4),
(72, 4),

-- 11. ELEVATE (Prodotti 73 - 78)
(73, 1), (73, 5),
(74, 1), (74, 5),
(75, 2),
(76, 4),
(77, 4),
(78, 4);