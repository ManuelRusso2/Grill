package control;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import model.bean.AcquistoBean;
import model.bean.OrdineBean;
import model.bean.ProdottoBean;
import model.bean.UtenteBean;
import model.dao.AcquistoDAO;
import model.dao.OrdineDAO;
import model.dao.ProdottoDAO;
import model.dao.UtenteDAO;
import model.dao.impl.AcquistoDAOImpl;
import model.dao.impl.OrdineDAOImpl;
import model.dao.impl.ProdottoDAOImpl;
import model.dao.impl.UtenteDAOImpl;

/**
 *FatturaServlet
 * Servlet responsabile della generazione dinamica delle fatture in formato PDF.
 *
 * Sfrutta la libreria iText 7 per costruire un documento PDF direttamente in memoria (stream)
 * a partire dai dati reali memorizzati nel database per uno specifico ordine d'acquisto.
 */
@WebServlet("/FatturaServlet")
public class FatturaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Oggetti DAO utilizzati per l'interazione con il database
    private AcquistoDAO acquistoDAO;
    private OrdineDAO ordineDAO;
    private UtenteDAO utenteDAO;
    private ProdottoDAO prodottoDAO;
    
    /**
     * Metodo di inizializzazione della Servlet.
     * Viene eseguito una sola volta all'avvio dell'applicazione o al primo caricamento della servlet
     * per istanziare le implementazioni dei DAO necessari.
     * 
     * @throws ServletException Se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init() throws ServletException {
        // Inizializzazione delle classi Data Access Object (DAO)
        this.acquistoDAO = new AcquistoDAOImpl();
        this.ordineDAO = new OrdineDAOImpl();
        this.utenteDAO = new UtenteDAOImpl();
        this.prodottoDAO = new ProdottoDAOImpl();
    }
    
    /**
     * Gestisce le richieste HTTP GET per il download/visualizzazione della fattura PDF.
     * 
     * @param request  L'oggetto {@link HttpServletRequest} contenente i parametri inviati dal client (es. id ordine)
     * @param response L'oggetto {@link HttpServletResponse} utilizzato per restituire il flusso PDF al browser
     * @throws ServletException Se si verifica un errore a livello di Servlet
     * @throws IOException      Se si verifica un errore durante le operazioni di I/O o scrittura stream
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // =========================================================================
        // 1. AUTENTICAZIONE UTENTE
        // Verifica se l'utente ha una sessione attiva prima di consentire il download
        // =========================================================================
        HttpSession session = request.getSession(false);
        UtenteBean utenteLoggato = session != null ? (UtenteBean) session.getAttribute("utente") : null;
        
        if (utenteLoggato == null) {
            // Risponde con un codice HTTP 401 (Non autorizzato)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Utente non autenticato. Effettuare il login.");
            return;
        }
        
        // =========================================================================
        // 2. VALIDAZIONE PARAMETRO DI INPUT
        // Estrazione e controllo della presenza e correttezza dell'ID d'acquisto
        // =========================================================================
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro ID ordine mancante nella richiesta.");
            return;
        }
        
        int idAcquisto;
        try {
            idAcquisto = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID ordine non valido (deve essere un valore numerico).");
            return;
        }
        
        try {
            // =========================================================================
            // 3. RECUPERO ACQUISTO DAL DATABASE
            // =========================================================================
            AcquistoBean acquisto = acquistoDAO.doRetrieveById(idAcquisto);
            if (acquisto == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "L'ordine specificato non è stato trovato nel database.");
                return;
            }
            
            // =========================================================================
            // 4. AUTORIZZAZIONE (CONTROLLO ACCESSO RISORSA)
            // L'utente standard può scaricare solo le PROPRIE fatture. L'admin può scaricarle tutte.
            // =========================================================================
            if (!utenteLoggato.isAdmin() && acquisto.getIdUtente() != utenteLoggato.getIdUtente()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Non si disporre dei permessi per visualizzare questa fattura.");
                return;
            }
            
            // =========================================================================
            // 5. RECUPERO DATI CLIENTE E DETTAGLI ORDINE
            // =========================================================================
            UtenteBean cliente = utenteDAO.doRetrieveById(acquisto.getIdUtente());
            if (cliente == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile recuperare i dati del cliente dall'ordine.");
                return;
            }
            
            // Recupero della lista dei singoli articoli che compongono l'acquisto
            List<OrdineBean> dettagliOrdine = ordineDAO.doRetrieveByAcquisto(idAcquisto);
            
            // =========================================================================
            // 6. GENERAZIONE DOCUMENTO PDF IN MEMORIA (iText 7)
            // Usiamo ByteArrayOutputStream per creare il PDF in memoria prima di inviarlo
            // =========================================================================
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);
            
            // Definizione dei margini della pagina (espressi in punti: 36pt = 0.5 pollici)
            document.setMargins(36, 36, 36, 36);
            
            // Costruzione effettiva del layout del PDF
            generaFatturaPDF(document, acquisto, cliente, dettagliOrdine);
            
            // Chiusura del documento per completare lo stream e flussare la memoria
            document.close();
            
            // =========================================================================
            // 7. INVIO RISPOSTA HTTP CON FLUSSO PDF
            // Configurazione degli Header HTTP per istruire il browser sul tipo di file
            // =========================================================================
            response.setContentType("application/pdf");
            
            // "inline" indica al browser di aprire il PDF nella scheda anziché forzare subito il download
            response.setHeader("Content-Disposition", "inline; filename=\"Fattura_" + idAcquisto + ".pdf\"");
            response.setContentLength(baos.size());
            
            // Scrittura del flusso di byte generato sullo stream di output della risposta
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();
            
        } catch (SQLException e) {
            // Log dell'eccezione sul server in caso di errori database
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il recupero dei dati dell'ordine dal database.");
        }
    }
    
    /**
     * Popola e assembla graficamente il documento PDF iText con le sezioni della fattura:
     * Intestazione, Dati Azienda/Cliente, Tabella Prodotti, Calcoli Fiscali e Pie' di pagina.
     * 
     * @param document       Il documento iText a cui aggiungere gli elementi grafici
     * @param acquisto       Bean contenente le informazioni generali dell'acquisto
     * @param cliente        Bean contenente i dati dell'utente intestatario della fattura
     * @param dettagliOrdine Lista contenente i singoli articoli acquistati nell'ordine
     * @throws SQLException Se si verifica un errore nel recupero del nome dei prodotti tramite DAO
     */
    private void generaFatturaPDF(Document document, AcquistoBean acquisto, UtenteBean cliente, 
                                  List<OrdineBean> dettagliOrdine) throws SQLException {
        
        // -------------------------------------------------------------------------
        // A. BRANDING E TITOLO
        // -------------------------------------------------------------------------
        document.add(new Paragraph("GRILL")
                .setBold()
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph("FATTURA N. " + acquisto.getIdAcquisto())
                .setBold()
                .setFontSize(18));
        
        // Formattazione della data nel formato italiano standard (GG/MM/AAAA HH:mm:ss)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dataFormattata = (acquisto.getDataAcquisto() != null) ? sdf.format(acquisto.getDataAcquisto()) : "N/D";
        document.add(new Paragraph("Data: " + dataFormattata));
        
        // -------------------------------------------------------------------------
        // B. DATI MITTENTE (AZIENDA)
        // -------------------------------------------------------------------------
        document.add(new Paragraph("\nVenditore:").setBold());
        document.add(new Paragraph("Grill Store\nVia Roma 10, Salerno\nP.IVA: 01234567890"));
        
        // -------------------------------------------------------------------------
        // C. DATI DESTINATARIO (CLIENTE)
        // -------------------------------------------------------------------------
        document.add(new Paragraph("\nCliente:").setBold());
        document.add(new Paragraph(cliente.getNome() + " " + cliente.getCognome()));
        document.add(new Paragraph("Email: " + cliente.getEmail()));
        document.add(new Paragraph("Telefono: " + (cliente.getTelefono() != null ? cliente.getTelefono() : "N/D")));
        document.add(new Paragraph("Indirizzo Consegna: " + acquisto.getIndirizzoConsegna()));
        document.add(new Paragraph("Metodo di Pagamento: " + (acquisto.getMetodoPagamento() != null ? acquisto.getMetodoPagamento() : "Carta di Credito")));
        
        document.add(new Paragraph("\nProdotti acquistati:").setBold());
        
        // -------------------------------------------------------------------------
        // D. TABELLA ARTICOLI
        // Definizione di una tabella a 5 colonne distribuite con larghezze relative (%)
        // Colonna 1: Prodotto (40%) | Col 2: Qtà (15%) | Col 3: Prezzo (20%) | Col 4: IVA (15%) | Col 5: Totale (20%)
        // -------------------------------------------------------------------------
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 1.5f, 2, 1.5f, 2}));
        table.setWidth(UnitValue.createPercentValue(100)); // Estensione a tutta larghezza
        
        // Intestazioni (Header) della tabella
        table.addHeaderCell(new Cell().add(new Paragraph("Prodotto").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Quantità").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Prezzo Unit.").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("IVA %").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Totale (IVA incl.)").setBold()));
        
        // Accumulatori per i calcoli del riepilogo fiscale
        double subtotaleNetto = 0.0;
        double totalIva = 0.0;
        
        // -------------------------------------------------------------------------
        // E. POPOLAMENTO RIGHE TABELLA E CALCOLI FISCALI
        // -------------------------------------------------------------------------
        if (dettagliOrdine != null && !dettagliOrdine.isEmpty()) {
            for (OrdineBean ordine : dettagliOrdine) {
                int idProdotto = ordine.getIdProdotto();
                double prezzoIvaInclusa = ordine.getPrezzoUnitario();
                double aliquotaIva = ordine.getIva();
                int quantita = ordine.getQuantitaAcquistata();
                
                // Recupero del nome del prodotto tramite il DAO dedicato
                ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);
                String nomeProdotto = (prodotto != null) ? prodotto.getNome() : "Prodotto #" + idProdotto;
                
                // Integrazione eventuale della taglia nel nome del prodotto
                if (ordine.getTaglia() != null && !ordine.getTaglia().trim().isEmpty()) {
                    nomeProdotto += " (Taglia: " + ordine.getTaglia() + ")";
                }
                
                // Calcolo totale riga lordo
                double totaleRigaIvaInclusa = prezzoIvaInclusa * quantita;
                
                // Formula di scomputo IVA ( Scorporo ):
                // Imponibile = Totale / (1 + (Aliquota / 100))
                double imponibileRiga = totaleRigaIvaInclusa / (1.0 + (aliquotaIva / 100.0));
                double ivaRiga = totaleRigaIvaInclusa - imponibileRiga;
                
                // Aggiornamento degli accumulatori per il totale
                subtotaleNetto += imponibileRiga;
                totalIva += ivaRiga;
                
                // Inserimento celle formattate (con indicatore di valuta Euro e simbolo %)
                table.addCell(new Cell().add(new Paragraph(nomeProdotto)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(quantita))));
                table.addCell(new Cell().add(new Paragraph(String.format(Locale.ITALY, "€ %.2f", prezzoIvaInclusa))));
                table.addCell(new Cell().add(new Paragraph(String.format(Locale.ITALY, "%.0f%%", aliquotaIva))));
                table.addCell(new Cell().add(new Paragraph(String.format(Locale.ITALY, "€ %.2f", totaleRigaIvaInclusa))));
            }
        }
        
        // Aggiunta della tabella compilata al documento
        document.add(table);
        
        // -------------------------------------------------------------------------
        // F. RIEPILOGO FISCALE E TOTALE COMPLESSIVO
        // -------------------------------------------------------------------------
        document.add(new Paragraph("\nImponibile (escl. IVA): " + String.format(Locale.ITALY, "€ %.2f", subtotaleNetto)));
        document.add(new Paragraph("IVA Totale: " + String.format(Locale.ITALY, "€ %.2f", totalIva)));
        
        // Evidenziazione del totale finale da pagare
        document.add(new Paragraph("Totale Complessivo: " + String.format(Locale.ITALY, "€ %.2f", acquisto.getPrezzoTotale()))
                .setBold()
                .setFontSize(14));
        
        document.add(new Paragraph("\n\n"));
        
        // -------------------------------------------------------------------------
        // G. PIÈ DI PAGINA (FOOTER)
        // -------------------------------------------------------------------------
        document.add(new Paragraph("Grazie per il vostro acquisto!").setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("2026 Grill. Tutti i diritti riservati.")
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER));
    }
    
    /**
     * Gestisce le richieste HTTP POST inoltrandole al metodo {@link #doGet}.
     * Permette alla servlet di essere richiamata indistintamente via GET o POST.
     * 
     * @param request  L'oggetto {@link HttpServletRequest}
     * @param response L'oggetto {@link HttpServletResponse}
     * @throws ServletException Se si verifica un errore a livello di Servlet
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}