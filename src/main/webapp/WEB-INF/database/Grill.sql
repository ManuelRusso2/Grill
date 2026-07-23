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
    tipo VARCHAR(50) NOT NULL,
    attivo BOOLEAN DEFAULT TRUE,
    id_collezione INTEGER,
    
    FOREIGN KEY(id_collezione) REFERENCES collezione(id_collezione)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ordine(
    id_acquisto INTEGER NOT NULL,
    id_prodotto INTEGER NOT NULL,
    prezzo_unitario DOUBLE NOT NULL CHECK(prezzo_unitario > 0),
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


-- ===================================================
-- POPOLAMENTO TABELLA COLLEZIONE
-- ===================================================

INSERT INTO collezione (nome_collezione, descrizione) VALUES 
(
    'peace and love', 
    'PEACE, LOVE, GRILL.\n\nPace nella mente, amore nelle vibrazioni e il calore giusto per accendere la tua giornata. Benvenuto nella nuova era dello streetwear targato Grill.\n\nLa collezione Peace and Love nasce per chi non vuole passare inosservato, unendo uno stile urban-utility deciso a un’anima sfacciatamente positiva. Dalle giacche bomber in pelle dal sapore varsity e vintage, alle felpe oversize ultra-comode, fino alle t-shirt grafiche e gli accessori essenziali (cappellini, marsupi e borraccia termica): ogni pezzo è pensato per accompagnarti ovunque con la giusta attitude.\n\nDUE VARIANTI, LO STESSO VIBE ICONICO\nScegli l''energia che si adatta meglio al tuo stile:\n\nDark & Bold Edition: Toni scuri e decisi, con grafiche rosse e bianche a contrasto su fondo nero e crema per un look urban forte e d''impatto.\n\nEarth & Olive Edition: Palette di colori caldi e naturali, con tonalità verde militare, panna ed inserti terrosi, per uno stile streetwear più morbido ma sempre dal carattere inconfondibile.'
),
(
    'flowless', 
    'FLOWLESS COLLECTION\n\nRAGGIUNGI IL TUO STATO DI FLOW. MENTE LIBERA. STILE SENZA COMPROMESSI.\n\nSei pronto a dominare la strada? GRILL presenta la nuova ed esclusiva collezione FW24: FLOWLESS.\n\nQuesta non è solo abbigliamento, è un''attitude. "Flow State. Mind Free." è il nostro mantra, un invito a vivere il momento senza farsi ostacolare da nulla. E per farlo con stile, abbiamo creato una linea streetwear essenziale, premium e unicamente audace, tutta in total black.'
),
(
    'freeflow', 
    'FREEFLOW\n\nEXPLORE EVERYWHERE, IN TOTAL SYNC.\n\nRompi le regole, esplora nuovi confini. La nuova drop Freeflow firmata Grill è pensata per chi si muove in totale armonia con la città e con se stesso. Un''estetica minimal ma d''impatto, creata per esplorare nuove frontiere di identità e spazio.'
),
(
    'reality', 
    'REALITY COLLECTION\n\nGRILL YOUR REALITY.\n\nNon limitarti a vivere la realtà: modellala, trasformala, GRILL IT.\n\nLa nuova collezione REALITY targata Grill (FW24) è una statement capsule creata per chi ha una visione chiara e non teme di mostrarla. Caratterizzata da una tipografia audace avvolta da grafiche orbitali/astratte, questa linea fonde un’estetica streetwear premium a una vestibilità d’impatto.\n\nDalle giacche bomber in pelle coordinata, alle felpe hoodie oversize (disponibili sia in tinta che in contrasto beige/crema), fino a t-shirt, cappellini, marsupi e borraccia termica: il kit completo per dominare ogni scenario urbano.\n\n4 PALETTE ESCLUSIVE per esprimere il tuo mindset:\nDeep Navy: Blu notte profondo ed elegante, per uno stile minimal, freddo e d''impatto.\nWarm Earth (Brown): Toni caldi del marrone cioccolato e sabbia, perfetti per un look streetwear sofisticato e contemporaneo.\nBurgundy / Wine: Una sfumatura bordeaux intensa e audace, studiata per chi vuole distinguersi con personalità.\nForest Olive: Verde militare e toni naturali per un''attitudine utility e al tempo stesso ricercata.'
),
(
    'peace and love X flowless', 
    'PEACE AND LOVE X FLOWLESS\n\nTHE ULTIMATE CROSSOVER.\n\nQuando due delle collezioni più iconiche di Grill si incontrano, nasce un nuovo punto di riferimento per lo streetwear.\n\nPeace and Love X Flowless è la fusione perfetta tra l''energia positiva del messaggio "Peace, Love, Grill" e l''attitude total black, affilata e impeccabile del mondo Flowless. Un''esplosione di stile monocromatico dove il logo del grill fiammeggiante si unisce al lettering arcuato Flowless, creando un contrasto visivo unico su fondo nero profondo.'
),
(
    'speed', 
    'SPEED COLLECTION\n\nGRILL. BEYOND SPEED.\n\nAllaccia le cinture e prepara il tuo stile a bruciare l''asfalto. Grill presenta la collezione SPEED (FW24): una capsule pensata per chi vive la vita al massimo, spinge sempre oltre i propri limiti e non si guarda mai indietro.\n\nIspirata all''adrenalina del motorsport, alla velocità urbana e alla cultura racing, la linea si distingue per una grafica dinamica con effetto "motion blur" e dettagli graffiati che trasmettono energia pura.\n\nDalle felpe hoodie oversize alle t-shirt grafiche, fino ad accessori ad alte prestazioni come cappellini da baseball con visiera, marsupi da viaggio e borraccia termica: ogni pezzo è progettato per accompagnare il tuo ritmo senza freni.\n\n3 COLORAZIONI AD ALTA VELOCITÀ:\nAsphalt Black: Il classico streetwear nero notte con dettagli sfumati a contrasto, per un look serale, aggressivo e decisamente racing.\nOff-Road Sand / Cream: Tonalità sabbiose e calde con accenti rosso corsa, ispirate alle avventure rally, al deserto e alle gare motocross.\nNight Racing Olive: Verde militare e dettagli verde acido a contrasto, perfetto per chi cerca uno stile utility, elettrico e fuori dagli schemi.'
),
(
    'built different', 
    'BUILT DIFFERENT.\n\nBUILT DIFFERENT. BUILT GRILL.\nNon siamo fatti per omologarci. La nuova capsule Built Different firmata Grill è creata per chi si distingue dalla massa, chi ha una "Grill Mentality" nel sangue e non si accontenta della media.\n\nCaratterizzata dal doppio logo GRILL GRILL in un font tondeggiante, audace e dal sapore retrò-streetwear, questa linea mette al centro l''autenticità e la qualità dei "reali ingredienti" che compongono il tuo stile.\n\nUn total look coordinato che comprende felpe hoodie oversize ultra-morbide, t-shirt grafiche dal taglio relaxed, cappellini da baseball, borraccia termica e marsupio utility.\n\n4 SHADES PER OGNI PERSONALITÀ:\nChocolate Brown: Una tonalità marrone scuro calda, intensa e super di tendenza per un look earth-tone impeccabile.\nCream / Off-White: Toni chiari, puliti ed essenziali che mettono in risalto la grafica e donano luce al tuo outfit.\nWashed Charcoal / Slate: Un grigio scuro/antracite dall''effetto vintage urbano, perfetto per le serate in città.\nClassic Pitch Black: Il nero profondo e intramontabile, azzeccatissimo con i dettagli ad alto contrasto.'
),
(
    'bloom', 
    'BLOOM COLLECTION\n\nGRILL IN BLOOM: PEACE, COLOR & FREEDOM.\n\nFai sbocciare il tuo stile con un tuffo negli anni ''70! Grill presenta BLOOM, la nuova capsule Free Spirit Streetwear pensata per le anime libere, gli amanti del colore e chi vive fuori dagli schemi.\n\nIspirata alla cultura psichedelica, ai festival retro e alla spensieratezza hippie, questa linea unisce grafiche floreali nostalgiche, illustrazioni di tramonti sognanti e font ondulati dal sapore vintage, il tutto rielaborato in chiave moderna e streetwear.'
),
(
    'metal', 
    'METAL COLLECTION\n\nGRILL NOISE EMPIRE — FORGED LOUD. BUILT GRILL.\n\nAlza il volume e fai tremare la strada. Grill presenta METAL, la capsule più estrema, aggressiva e senza filtri mai creata. Ispirata alla cultura heavy metal, all''estetica metalcore/goth e all''energia grezza dei festival underground, la linea Noise Empire è fatta per chi non ha paura di farsi sentire.\n\nGrafiche impattanti a tema teschio sanguigno, font spigolosi stile band metal, dettagli glitch e lavaggi acid-wash/washed black si fondeno per dare vita a un''estetica dark-streetwear assolutamente devastante.'
),
(
    'devil', 
    'DEVIL COLLECTION\n\nGRILL HELLFIRE — BORN DIFFERENT. BURN GRILL.\n\nAccendi le fiamme del tuo stile. Grill presenta DEVIL, la nuova capsule Hellfire pensata per chi non ha paura di mostrare il proprio lato più oscuro, provocatorio e ribelle.\n\nIspirata all''estetica gothic-streetwear, al mondo occult-urban e all''energia pura del fuoco infernale, questa linea combina un font gotico affilato, illustrazioni dettagliate di demoni cornuti, dettagli di fiamme sulle maniche e simboli mistici, il tutto in un contrasto infuocato su fondo nero profondo.'
),
(
    'elevate', 
    'ELEVATE COLLECTION\n\nTHE CROWN JEWEL OF GRILL — REFINED STREETWEAR\n\nDimentica tutto quello che hai visto finora. Se c''è una collezione che incarna l''essenza stessa di Grill portandola al suo livello supremo, è ELEVATE. Questa non è una semplice capsule: è l''apice, il capolavoro, il Santo Graal del nostro brand.\n\nCreata per chi non si accontenta del "solito" streetwear e vuole dominare la scena, ELEVATE fonde la grinta della street culture con il lusso contemporaneo più sofisticato (Refined. Modern. Timeless.). Pelle di prima scelta, cotoni ad altissima grammatura, ricami tridimensionali ad alta densità e una palette total black magnetica: è la collezione definitiva per chi pretende solo il meglio.'
);


-- ===================================================
-- POPOLAMENTO TABELLA PRODOTTO
-- ===================================================

INSERT INTO prodotto (nome, descrizione, costo, quantita, tipo, attivo, id_collezione) VALUES 

-- 1. PEACE AND LOVE (id_collezione = 1) - Dark & Olive Editions
('Good Vibes Leather Varsity Jacket - Dark', 'Giacca bomber varsity in pelle nera con grafiche rosse e bianche a contrasto.', 249.90, 20, 'Giacca', true, 1),
('Good Vibes Leather Varsity Jacket - Olive', 'Giacca bomber varsity in pelle verde militare con inserti panna e toni terrosi.', 249.90, 20, 'Giacca', true, 1),
('Spread Love Oversized Hoodie - Dark', 'Felpa oversize con cappuccio in cotone pesante nero con grafica Peace & Love.', 89.90, 35, 'Felpa', true, 1),
('Spread Love Oversized Hoodie - Olive', 'Felpa oversize verde oliva con cappuccio e grafiche calde ad alta densita.', 89.90, 35, 'Felpa', true, 1),
('Stay Positive Graphic Tee - Dark', 'T-shirt grafica relaxed fit su fondo nero con stampa posizionata.', 45.00, 50, 'T-Shirt', true, 1),
('Stay Positive Graphic Tee - Olive', 'T-shirt grafica color panna con dettagli verde militare e logo Grill.', 45.00, 50, 'T-Shirt', true, 1),
('Peace and Love Cap', 'Cappellino con visiera e ricamo frontale Peace, Love, Grill.', 35.00, 40, 'Accessorio', true, 1),
('Peace and Love Bottle', 'Borraccia termica in acciaio inox 500ml con finitura opaca.', 29.90, 30, 'Accessorio', true, 1),
('Peace and Love Bag', 'Marsupio utility regolabile con tasche multiple e stampa ad impatto.', 49.90, 25, 'Accessorio', true, 1),

-- 2. FLOWLESS (id_collezione = 2)
('Flow State Leather Bomber', 'Giacca bomber in vera pelle total black con finiture minimal e lining personalizzato.', 279.90, 15, 'Giacca', true, 2),
('Mind Free Oversized Hoodie', 'Felpa hoodie oversize nera in cotone 480gsm con mantra Mind Free ricamato.', 95.00, 30, 'Felpa', true, 2),
('Pure Flow Graphic Tee', 'T-shirt streetwear nera con stampa gommata tono su tono.', 49.90, 45, 'T-Shirt', true, 2),
('Flowless Cap', 'Cappellino strutturato total black con logo arcuato Flowless.', 35.00, 40, 'Accessorio', true, 2),
('Flowless Bottle', 'Borraccia termica total black opaca ad alto isolamento.', 29.90, 30, 'Accessorio', true, 2),
('Flowless Bag', 'Borsa crossbody tattica in nylon ad alta resistenza.', 55.00, 20, 'Accessorio', true, 2),

-- 3. FREEFLOW (id_collezione = 3)
('Total Sync Heavy Hoodie', 'Felpa pesante con cappuccio doppio strato ed estetica urban minimal.', 99.90, 25, 'Felpa', true, 3),
('New Frontier Crewneck', 'Maglione girocollo in maglia tecnica essenziale per esplorazione urbana.', 85.00, 30, 'Felpa', true, 3),
('Exploration Relaxed Tee', 'T-shirt dal taglio morbido e traspirante con stampa posteriore.', 42.00, 40, 'T-Shirt', true, 3),
('Freeflow Cap', 'Cappellino baseball rigido con regolatore in metallo.', 35.00, 35, 'Accessorio', true, 3),
('Freeflow Beanie', 'Berretto in maglia a costine elasticizzata con patch gommata.', 28.00, 50, 'Accessorio', true, 3),
('Freeflow Headphones', 'Cuffie wireless over-ear personalizzate con audio ad alta fedelta.', 129.90, 15, 'Accessorio', true, 3),
('Freeflow Bottle', 'Borraccia ergonomica satinata con gancio da moschettone.', 29.90, 30, 'Accessorio', true, 3),
('Freeflow Bag', 'Zaino monospalla compatto con scomparto hi-tech.', 59.90, 20, 'Accessorio', true, 3),

-- 4. REALITY (id_collezione = 4) - Navy, Earth Brown, Burgundy, Forest Olive
('Visionary Leather Jacket - Deep Navy', 'Giacca in pelle coordinata tonalita blu notte profondo.', 269.90, 12, 'Giacca', true, 4),
('Visionary Leather Jacket - Earth Brown', 'Giacca in pelle marrone cioccolato sofisticata e contemporanea.', 269.90, 12, 'Giacca', true, 4),
('Visionary Leather Jacket - Burgundy', 'Giacca in pelle bordeaux intensa con dettagli rifiniti a mano.', 269.90, 12, 'Giacca', true, 4),
('Visionary Leather Jacket - Forest Olive', 'Giacca in pelle verde militare per un look utility ad impatto.', 269.90, 12, 'Giacca', true, 4),
('Reality Orbit Heavy Hoodie - Deep Navy', 'Felpa oversize blu notte con grafica orbitale e tipografia astratta.', 89.90, 25, 'Felpa', true, 4),
('Reality Orbit Heavy Hoodie - Earth Brown', 'Felpa pesante marrone e crema con grafica Orbit gommata.', 89.90, 25, 'Felpa', true, 4),
('True Mindset Boxy Tee - Burgundy', 'T-shirt boxy fit bordeaux con stampa statement Reality.', 45.00, 35, 'T-Shirt', true, 4),
('True Mindset Boxy Tee - Forest Olive', 'T-shirt boxy fit verde foresta con dettagli a contrasto.', 45.00, 35, 'T-Shirt', true, 4),
('Reality Cap', 'Cappellino con ricamo orbitale tridimensionale.', 35.00, 40, 'Accessorio', true, 4),
('Reality Bottle', 'Borraccia termica 750ml con stampa Reality Orbit.', 32.00, 25, 'Accessorio', true, 4),
('Reality Bag', 'Marsupio scomparto doppio per uso quotidiano.', 48.00, 30, 'Accessorio', true, 4),

-- 5. PEACE AND LOVE X FLOWLESS (id_collezione = 5)
('Crossover Hybrid Leather Bomber', 'Bomber ibrido in pelle total black con dettagli crossover ricamati.', 299.90, 15, 'Giacca', true, 5),
('Flow & Love Heavy Hoodie', 'Felpa pesante nera con fusione tra logo fiammeggiante e font Flowless.', 99.90, 30, 'Felpa', true, 5),
('Dual Identity Graphic Tee', 'T-shirt nera a contrasto ad alta densita di stampa.', 49.90, 40, 'T-Shirt', true, 5),
('Peace and Love x Flowless Cap', 'Cappellino nero con doppio logo gommato sul fronte.', 38.00, 35, 'Accessorio', true, 5),
('Peace and Love x Flowless Bottle', 'Borraccia termica total black crossover ediz. limitata.', 35.00, 25, 'Accessorio', true, 5),
('Peace and Love x Flowless Bag', 'Tracolla tecnica rinforzata con fibbie metalliche.', 65.00, 20, 'Accessorio', true, 5),

-- 6. SPEED (id_collezione = 6) - Asphalt, Off-Road Sand, Night Racing Olive
('Fast Driven Motion Hoodie - Asphalt Black', 'Felpa racing nera con grafiche motion blur sfumate a contrasto.', 92.00, 30, 'Felpa', true, 6),
('Fast Driven Motion Hoodie - Off-Road Sand', 'Felpa sabbia e crema con accenti rosso corsa e dettagli graffiati.', 92.00, 30, 'Felpa', true, 6),
('Fast Driven Motion Hoodie - Night Racing Olive', 'Felpa verde militare con dettagli verde acido ad alta visibilita.', 92.00, 30, 'Felpa', true, 6),
('Beyond Speed Racing Tee - Asphalt Black', 'T-shirt grafica traspirante motorsport ad alte prestazioni.', 45.00, 40, 'T-Shirt', true, 6),
('Beyond Speed Racing Tee - Off-Road Sand', 'T-shirt deserto e sabbia con stampa grafica stile motocross.', 45.00, 40, 'T-Shirt', true, 6),
('Speed Cap', 'Cappellino racing da baseball con visiera sagomata.', 35.00, 45, 'Accessorio', true, 6),
('Speed Bottle', 'Borraccia sportiva con tappo rapido a pressione.', 28.00, 35, 'Accessorio', true, 6),
('Speed Bag', 'Marsupio tecnico da viaggio ad aggancio rapido.', 52.00, 25, 'Accessorio', true, 6),

-- 7. BUILT DIFFERENT (id_collezione = 7) - Chocolate, Cream, Slate, Pitch Black
('Stand Out Heavy Hoodie - Chocolate Brown', 'Felpa marrone scuro ultra-morbida con doppio logo GRILL retro.', 89.90, 25, 'Felpa', true, 7),
('Stand Out Heavy Hoodie - Cream', 'Felpa panna essenziale con grafica centrale marrone a contrasto.', 89.90, 25, 'Felpa', true, 7),
('Stand Out Heavy Hoodie - Washed Slate', 'Felpa grigio antracite con effetto vintage lavato.', 89.90, 25, 'Felpa', true, 7),
('Real Ingredients Boxy Tee - Pitch Black', 'T-shirt dal taglio relaxed nero profondo con stampa ad alta densita.', 42.00, 40, 'T-Shirt', true, 7),
('Real Ingredients Boxy Tee - Cream', 'T-shirt chiara vestibilita boxy con ricamo sul petto.', 42.00, 40, 'T-Shirt', true, 7),
('Built Different Cap', 'Cappellino stile retrò con cinturino in pelle.', 35.00, 40, 'Accessorio', true, 7),
('Built Different Bottle', 'Borraccia termica con finitura gommata piacevole al tatto.', 29.90, 30, 'Accessorio', true, 7),
('Built Different Bag', 'Marsupio utility capiente per tutti i giorni.', 45.00, 30, 'Accessorio', true, 7),

-- 8. BLOOM (id_collezione = 8)
('70s Soul Bloom Varsity Bomber', 'Giacca bomber stile anni 70 con patch floreali e colletto a costine.', 219.90, 15, 'Giacca', true, 8),
('Free Spirit Bloom Hoodie', 'Felpa colorata con grafica tramonto psichedelica e font ondulato.', 85.00, 30, 'Felpa', true, 8),
('Peace & Freedom Retro Tee', 'T-shirt stile vintage con illustrazioni floreali e toni caldi.', 42.00, 45, 'T-Shirt', true, 8),
('Bloom Cap', 'Cappellino ricamato con motivi floreali multicolor.', 32.00, 35, 'Accessorio', true, 8),
('Bloom Crochet Hat', 'Cappello lavorato all uncinetto stile hippie retro.', 38.00, 20, 'Accessorio', true, 8),
('Bloom Bottle', 'Borraccia con pattern floreale nostalgico.', 28.00, 30, 'Accessorio', true, 8),
('Bloom Bag', 'Borsa shopper in tela di cotone pesante ricamata.', 39.90, 25, 'Accessorio', true, 8),

-- 9. METAL (id_collezione = 9)
('Noise Empire Flight Bomber', 'Giacca bomber da volo stile metalcore con dettagli distressed e zip metalliche.', 239.90, 15, 'Giacca', true, 9),
('Heavy Noise Acid-Wash Hoodie', 'Felpa con lavaggio acido grigio scuro e grafica teschio sanguigno.', 95.00, 30, 'Felpa', true, 9),
('Skull Empire Washed Tee', 'T-shirt con lavaggio vintage washed e font spigoloso band metal.', 48.00, 50, 'T-Shirt', true, 9),
('Metal Cap', 'Cappellino nero distrutturato con spille metalliche e font gothic.', 35.00, 35, 'Accessorio', true, 9),
('Metal Bottle', 'Borraccia in metallo grezzo satinato con incisione laser.', 32.00, 30, 'Accessorio', true, 9),
('Metal Bag', 'Borsa tracolla rinforzata con borchie e chiusure in metallo.', 59.90, 20, 'Accessorio', true, 9),

-- 10. DEVIL (id_collezione = 10)
('Hellfire MA-1 Flight Bomber', 'Giacca bomber MA-1 con fiamme sulle maniche e fodera interna rossa inferno.', 249.90, 15, 'Giacca', true, 10),
('Infernal Flame Heavy Hoodie', 'Felpa con cappuccio pesante e illustrazioni dettagliate di demoni cornuti.', 95.00, 30, 'Felpa', true, 10),
('Horned Demon Boxy Tee', 'T-shirt vestibilita boxy con simboli mistici e grafica infuocata.', 48.00, 45, 'T-Shirt', true, 10),
('Devil Cap', 'Cappellino gothic con ricamo di corna rosse in rilievo.', 35.00, 40, 'Accessorio', true, 10),
('Devil Bottle', 'Borraccia nera lucida con dettagli rossi ad alto contrasto.', 29.90, 30, 'Accessorio', true, 10),
('Devil Bag', 'Marsupio tattico con moschettoni metallici e tirazip fiammeggianti.', 55.00, 25, 'Accessorio', true, 10),

-- 11. ELEVATE (id_collezione = 11)
('Masterpiece Leather Jacket', 'Giacca in pelle di agnello di prima scelta con finiture sartoriali e dettagli di lusso.', 389.90, 10, 'Giacca', true, 11),
('Crown Oversized Leather Bomber', 'Bomber in pelle oversize con ricami tridimensionali ad alta densita.', 349.90, 10, 'Giacca', true, 11),
('Quiet Luxury Heavy Hoodie', 'Felpa in cotone pettinato ad altissima grammatura (500gsm) total black magnetica.', 120.00, 20, 'Felpa', true, 11),
('Elevate Cap', 'Cappellino di lusso con placchetta in metallo dorato/satinato.', 45.00, 30, 'Accessorio', true, 11),
('Elevate Bag', 'Borsa da viaggio premium in pelle e nylon ad alta densita.', 89.90, 15, 'Accessorio', true, 11),
('Elevate Bottle', 'Borraccia termica di lusso con finitura opaca e dettagli incisi.', 38.00, 25, 'Accessorio', true, 11);


-- ===================================================
-- POPOLAMENTO UTENTI ADMIN
-- ===================================================

INSERT INTO utente (email, nome, cognome, isAdmin, password, telefono) VALUES 
(
    'antonio.sicignano@grill.it', 
    'Antonio', 
    'Sicignano', 
    true, 
    SHA2('admin123', 256),
    '3331234567'
),
(
    'manuel.russo@grill.it', 
    'Nello Manuel', 
    'Russo', 
    true, 
    SHA2('admin123', 256), 
    '3337654321'
);


-- ===================================================
-- POPOLAMENTO TABELLA CATEGORIA
-- ===================================================

INSERT INTO categoria (nome, descrizione) VALUES 
('Giacche', 'Giacche, bomber varsity e capispalla per ogni stagione.'),
('Felpe', 'Felpe oversize, hoodie con cappuccio e girocollo essenziali.'),
('T-Shirt', 'T-shirt grafiche, magliette boxy e relaxed fit.'),
('Accessori', 'Cappellini, borracce termiche, borse e accessori streetwear.'),
('Pelle', 'Capi ed edizioni speciali realizzati in vera pelle e finiture premium.');


-- ===================================================
-- POPOLAMENTO TABELLA TIPOLOGIA (Mappa Prodotto <-> Categoria)
-- ID Categorie: 1=Giacche, 2=Felpe, 3=T-Shirt, 4=Accessori, 5=Pelle
-- ===================================================


INSERT INTO tipologia (id_prodotto, id_categoria) VALUES 

-- ---------------------------------------------------
-- 1. PEACE AND LOVE (Prodotti 1 - 9)
-- ---------------------------------------------------
(1, 1), (1, 5), -- Good Vibes Leather Varsity Jacket - Dark (Giacca + Pelle)
(2, 1), (2, 5), -- Good Vibes Leather Varsity Jacket - Olive (Giacca + Pelle)
(3, 2),        -- Spread Love Oversized Hoodie - Dark (Felpa)
(4, 2),        -- Spread Love Oversized Hoodie - Olive (Felpa)
(5, 3),        -- Stay Positive Graphic Tee - Dark (T-Shirt)
(6, 3),        -- Stay Positive Graphic Tee - Olive (T-Shirt)
(7, 4),        -- Peace and Love Cap (Accessorio)
(8, 4),        -- Peace and Love Bottle (Accessorio)
(9, 4),        -- Peace and Love Bag (Accessorio)

-- ---------------------------------------------------
-- 2. FLOWLESS (Prodotti 10 - 15)
-- ---------------------------------------------------
(10, 1), (10, 5), -- Flow State Leather Bomber (Giacca + Pelle)
(11, 2),          -- Mind Free Oversized Hoodie (Felpa)
(12, 3),          -- Pure Flow Graphic Tee (T-Shirt)
(13, 4),          -- Flowless Cap (Accessorio)
(14, 4),          -- Flowless Bottle (Accessorio)
(15, 4),          -- Flowless Bag (Accessorio)

-- ---------------------------------------------------
-- 3. FREEFLOW (Prodotti 16 - 23)
-- ---------------------------------------------------
(16, 2), -- Total Sync Heavy Hoodie (Felpa)
(17, 2), -- New Frontier Crewneck (Felpa)
(18, 3), -- Exploration Relaxed Tee (T-Shirt)
(19, 4), -- Freeflow Cap (Accessorio)
(20, 4), -- Freeflow Beanie (Accessorio)
(21, 4), -- Freeflow Headphones (Accessorio)
(22, 4), -- Freeflow Bottle (Accessorio)
(23, 4), -- Freeflow Bag (Accessorio)

-- ---------------------------------------------------
-- 4. REALITY (Prodotti 24 - 34)
-- ---------------------------------------------------
(24, 1), (24, 5), -- Visionary Leather Jacket - Deep Navy (Giacca + Pelle)
(25, 1), (25, 5), -- Visionary Leather Jacket - Earth Brown (Giacca + Pelle)
(26, 1), (26, 5), -- Visionary Leather Jacket - Burgundy (Giacca + Pelle)
(27, 1), (27, 5), -- Visionary Leather Jacket - Forest Olive (Giacca + Pelle)
(28, 2),          -- Reality Orbit Heavy Hoodie - Deep Navy (Felpa)
(29, 2),          -- Reality Orbit Heavy Hoodie - Earth Brown (Felpa)
(30, 3),          -- True Mindset Boxy Tee - Burgundy (T-Shirt)
(31, 3),          -- True Mindset Boxy Tee - Forest Olive (T-Shirt)
(32, 4),          -- Reality Cap (Accessorio)
(33, 4),          -- Reality Bottle (Accessorio)
(34, 4),          -- Reality Bag (Accessorio)

-- ---------------------------------------------------
-- 5. PEACE AND LOVE X FLOWLESS (Prodotti 35 - 40)
-- ---------------------------------------------------
(35, 1), (35, 5), -- Crossover Hybrid Leather Bomber (Giacca + Pelle)
(36, 2),          -- Flow & Love Heavy Hoodie (Felpa)
(37, 3),          -- Dual Identity Graphic Tee (T-Shirt)
(38, 4),          -- Peace and Love x Flowless Cap (Accessorio)
(39, 4),          -- Peace and Love x Flowless Bottle (Accessorio)
(40, 4),          -- Peace and Love x Flowless Bag (Accessorio)

-- ---------------------------------------------------
-- 6. SPEED (Prodotti 41 - 48)
-- ---------------------------------------------------
(41, 2), -- Fast Driven Motion Hoodie - Asphalt Black (Felpa)
(42, 2), -- Fast Driven Motion Hoodie - Off-Road Sand (Felpa)
(43, 2), -- Fast Driven Motion Hoodie - Night Racing Olive (Felpa)
(44, 3), -- Beyond Speed Racing Tee - Asphalt Black (T-Shirt)
(45, 3), -- Beyond Speed Racing Tee - Off-Road Sand (T-Shirt)
(46, 4), -- Speed Cap (Accessorio)
(47, 4), -- Speed Bottle (Accessorio)
(48, 4), -- Speed Bag (Accessorio)

-- ---------------------------------------------------
-- 7. BUILT DIFFERENT (Prodotti 49 - 56)
-- ---------------------------------------------------
(49, 2), -- Stand Out Heavy Hoodie - Chocolate Brown (Felpa)
(50, 2), -- Stand Out Heavy Hoodie - Cream (Felpa)
(51, 2), -- Stand Out Heavy Hoodie - Washed Slate (Felpa)
(52, 3), -- Real Ingredients Boxy Tee - Pitch Black (T-Shirt)
(53, 3), -- Real Ingredients Boxy Tee - Cream (T-Shirt)
(54, 4), -- Built Different Cap (Accessorio)
(55, 4), -- Built Different Bottle (Accessorio)
(56, 4), -- Built Different Bag (Accessorio)

-- ---------------------------------------------------
-- 8. BLOOM (Prodotti 57 - 63)
-- ---------------------------------------------------
(57, 1), -- 70s Soul Bloom Varsity Bomber (Giacca)
(58, 2), -- Free Spirit Bloom Hoodie (Felpa)
(59, 3), -- Peace & Freedom Retro Tee (T-Shirt)
(60, 4), -- Bloom Cap (Accessorio)
(61, 4), -- Bloom Crochet Hat (Accessorio)
(62, 4), -- Bloom Bottle (Accessorio)
(63, 4), -- Bloom Bag (Accessorio)

-- ---------------------------------------------------
-- 9. METAL (Prodotti 64 - 69)
-- ---------------------------------------------------
(64, 1), -- Noise Empire Flight Bomber (Giacca)
(65, 2), -- Heavy Noise Acid-Wash Hoodie (Felpa)
(66, 3), -- Skull Empire Washed Tee (T-Shirt)
(67, 4), -- Metal Cap (Accessorio)
(68, 4), -- Metal Bottle (Accessorio)
(69, 4), -- Metal Bag (Accessorio)

-- ---------------------------------------------------
-- 10. DEVIL (Prodotti 70 - 75)
-- ---------------------------------------------------
(70, 1), -- Hellfire MA-1 Flight Bomber (Giacca)
(71, 2), -- Infernal Flame Heavy Hoodie (Felpa)
(72, 3), -- Horned Demon Boxy Tee (T-Shirt)
(73, 4), -- Devil Cap (Accessorio)
(74, 4), -- Devil Bottle (Accessorio)
(75, 4), -- Devil Bag (Accessorio)

-- ---------------------------------------------------
-- 11. ELEVATE (Prodotti 76 - 81)
-- ---------------------------------------------------
(76, 1), (76, 5), -- Masterpiece Leather Jacket (Giacca + Pelle)
(77, 1), (77, 5), -- Crown Oversized Leather Bomber (Giacca + Pelle)
(78, 2),          -- Quiet Luxury Heavy Hoodie (Felpa)
(79, 4),          -- Elevate Cap (Accessorio)
(80, 4),          -- Elevate Bag (Accessorio)
(81, 4);          -- Elevate Bottle (Accessorio)