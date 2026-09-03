# Grill - E-Commerce Web Application

Progetto sviluppato per l'esame di "Tecnologie Software per il Web" (A.A. 2025-2026).
Un sistema e-commerce completo basato su architettura MVC (Model-View-Controller) per la gestione di un catalogo prodotti, carrello, acquisti e dashboard amministrativa.

## 🛠 Tecnologie Utilizzate
*   **Back-end:** Java EE (Servlet & JSP), Apache Tomcat v9.0
*   **Database:** MySQL, configurato con Connection Pool via JNDI DataSource
*   **Front-end:** HTML5, CSS3, JavaScript, AJAX/Fetch API
*   **Librerie addizionali:** JSTL, Driver MySQL Connector

## 📂 Architettura e Pattern MVC
Il progetto è rigorosamente strutturato su pattern MVC:
*   **Model:** Suddiviso in `bean` (oggetti di dominio come `Prodotto`, `Utente`) e `dao` (Data Access Object per interazione con DB tramite PreparedStatement).
*   **View:** Realizzate in `JSP` e separate logicamente in `admin` e `user`. Uso di file `fragment (.jspf)` per layout riutilizzabili (header, footer, menu). Nessuna Servlet genera HTML direttamente.
*   **Controller:** Sviluppato tramite `Servlet` nel package `control`, responsabili dello smistamento richieste. La sicurezza è delegata a filtri (`Filter`).

## ✅ Funzionalità Implementate

### Area Cliente
*   Registrazione con validazione campi tramite espressioni regolari
*   Controllo AJAX in tempo reale della disponibilità dell'email in fase di registrazione
*   Login e gestione sessione
*   Catalogo prodotti con visualizzazione dettagliata
*   Ricerca prodotti con suggerimenti dinamici via AJAX
*   Carrello: aggiunta, modifica quantità, rimozione (gestito in sessione)
*   Conferma ordine con svuotamento del carrello
*   Storico ordini personale
*   Messaggi di conferma per le azioni eseguite (registrazione, ordine, ecc.)

### Area Amministratore
*   Accesso protetto tramite autenticazione programmata
*   CRUD (create, read, update, delete) completo dei prodotti (inserimento, modifica, visualizzazione, cancellazione con conferma)
*   Visualizzazione di tutti gli ordini
*   Filtro ordini per intervallo di date e per cliente

## 🔒 Sicurezza
*   **SQL Injection:** tutte le query al database eseguite tramite `PreparedStatement`, nessuna concatenazione di stringhe
*   **Password:** salvate nel database in forma cifrata (ALGORITMO SHA-256)
*   **Filtri servlet:** controllano l'accesso all'area amministrativa e altre operazioni trasversali
*   **Pagine di errore personalizzate:** configurate in `web.xml` per i codici  403(forbidden, accesso negato), 404(not found. pagina non trovata), 500(internal server error, errore del server)

## 🗃️ Note su integrità dei dati
*   Prezzo e IVA vengono salvati direttamente nella riga d'ordine al momento dell'acquisto, per garantire l'integrità storica anche se il prodotto viene successivamente modificato
*   Gestito il vincolo di integrità referenziale per i prodotti cancellati ma ancora presenti in ordini storici

## ⚙️ Setup e Installazione (Ambiente Tomcat)
1. Importare il progetto in Eclipse EE (o IDE equivalente).
2. **Database:** Creare uno schema su MySQL ed eseguire il dump `WEB-INF/database/Grill.sql`.
3. **Context:** Verificare che in `META-INF/context.xml` la resource JDBC punti alle proprie credenziali MySQL (`username` e `password` locali).
4. Avviare il server Apache Tomcat e visitare `http://localhost:8080/Grill/`.

## 🔑 Credenziali di Test

*   **Ruolo Admin:**
    *   `antonio.sicignano@grill.it` / password: `admin123`
    *   `manuel.russo@grill.it` / password: `admin123`

*   **Ruolo Utente:** non sono previsti account utente precaricati. Per testare le funzionalità lato cliente, effettuare prima la registrazione tramite l'apposito form, poi accedere con le credenziali appena create.