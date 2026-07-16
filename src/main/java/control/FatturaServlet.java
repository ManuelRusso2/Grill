package control;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

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
 * Servlet per generare fatture in PDF dinamicamente.
 * Recupera i dati dal database e genera un PDF con iText basato sui dati reali dell'ordine.
 */
@WebServlet("/FatturaServlet")
public class FatturaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private AcquistoDAO acquistoDAO;
    private OrdineDAO ordineDAO;
    private UtenteDAO utenteDAO;
    private ProdottoDAO prodottoDAO;
    
    @Override
    public void init() throws ServletException {
        // Inizializziamo tutti i DAO necessari
        this.acquistoDAO = new AcquistoDAOImpl();
        this.ordineDAO = new OrdineDAOImpl();
        this.utenteDAO = new UtenteDAOImpl();
        this.prodottoDAO = new ProdottoDAOImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Verifiche di autenticazione: l'utente deve essere loggato
        HttpSession session = request.getSession(false);
        UtenteBean utenteLoggato = session != null ? (UtenteBean) session.getAttribute("utente") : null;
        
        if (utenteLoggato == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // 2. Recupero del parametro ID dell'ordine (Acquisto) dalla richiesta
        String idParam = request.getParameter("id");
        
        // Validazione del parametro ID
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID ordine mancante");
            return;
        }
        
        int idAcquisto;
        try {
            idAcquisto = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID ordine non valido");
            return;
        }
        
        try {
            // 3. Recupero dell'acquisto dal database
            AcquistoBean acquisto = acquistoDAO.doRetrieveById(idAcquisto);
            
            if (acquisto == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Ordine non trovato");
                return;
            }
            
            // 4. Controllo di sicurezza: l'utente può vedere solo i propri ordini (a meno che non sia admin)
            if (!utenteLoggato.isAdmin() && acquisto.getIdUtente() != utenteLoggato.getIdUtente()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Non autorizzato a visualizzare questo ordine");
                return;
            }
            
            // 5. Recupero dei dati del cliente associato all'ordine
            UtenteBean cliente = utenteDAO.doRetrieveById(acquisto.getIdUtente());
            
            if (cliente == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Dati cliente non trovati");
                return;
            }
            
            // 6. Recupero dei dettagli dell'ordine (singoli prodotti con quantità e prezzi)
            List<OrdineBean> dettagliOrdine = ordineDAO.doRetrieveByAcquisto(idAcquisto);
            
            // 7. Generazione della risposta PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"Fattura_" + idAcquisto + ".pdf\"");
            
            // Creiamo il writer PDF collegato all'output stream della response
            PdfWriter writer = new PdfWriter(response.getOutputStream());
            com.itextpdf.kernel.pdf.PdfDocument pdfDocument = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Aggiungiamo il contenuto della fattura
            generaFatturaHTML(document, acquisto, cliente, dettagliOrdine);
            
            document.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel recupero dei dati");
        }
    }
    
    /**
     * Genera il contenuto della fattura in formato PDF.
     * 
     * @param document Il Document di iText dove scrivere
     * @param acquisto L'oggetto AcquistoBean con i dati dell'ordine
     * @param cliente L'oggetto UtenteBean con i dati del cliente
     * @param dettagliOrdine La lista di OrdineBean con i prodotti acquistati
     */
    private void generaFatturaHTML(Document document, AcquistoBean acquisto, UtenteBean cliente, 
                                     List<OrdineBean> dettagliOrdine) throws SQLException {
        
        // Intestazione della fattura
        document.add(new Paragraph("FATTURA")
                .setFontSize(20)
                .setBold()
                .setHorizontalAlignment(HorizontalAlignment.CENTER));
        
        document.add(new Paragraph("\n"));
        
        // Informazioni generali della fattura
        document.add(new Paragraph("Numero Fattura: " + acquisto.getIdAcquisto()));
        
        // Formattazione della data
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dataFormattata = sdf.format(acquisto.getDataAcquisto());
        document.add(new Paragraph("Data: " + dataFormattata));
        
        document.add(new Paragraph("\n"));
        
        // Dati del cliente (Intestatario della fattura)
        document.add(new Paragraph("CLIENTE")
                .setBold()
                .setFontSize(12));
        
        document.add(new Paragraph("Nome: " + cliente.getNome() + " " + cliente.getCognome()));
        document.add(new Paragraph("Email: " + cliente.getEmail()));
        document.add(new Paragraph("Telefono: " + (cliente.getTelefono() != null ? cliente.getTelefono() : "N/A")));
        
        document.add(new Paragraph("\n"));
        
        // Dati di consegna
        document.add(new Paragraph("INDIRIZZO DI CONSEGNA")
                .setBold()
                .setFontSize(12));
        document.add(new Paragraph(acquisto.getIndirizzoConsegna()));
        
        document.add(new Paragraph("\n"));
        
        // Dati di pagamento
        document.add(new Paragraph("METODO DI PAGAMENTO")
                .setBold()
                .setFontSize(12));
        document.add(new Paragraph(acquisto.getMetodoPagamento()));
        
        document.add(new Paragraph("\n"));
        
        // Tabella dei prodotti
        document.add(new Paragraph("DETTAGLIO ORDINE")
                .setBold()
                .setFontSize(12));
        
        // Creiamo una tabella con 6 colonne: ID Prodotto, Nome, Quantità, Prezzo Unitario, IVA, Totale
        Table table = new Table(6);
        
        // Header della tabella
        table.addCell(new Cell().add(new Paragraph("ID Prod")).setBold());
        table.addCell(new Cell().add(new Paragraph("Nome")).setBold());
        table.addCell(new Cell().add(new Paragraph("Quantità")).setBold());
        table.addCell(new Cell().add(new Paragraph("Prezzo Unit.")).setBold());
        table.addCell(new Cell().add(new Paragraph("IVA %")).setBold());
        table.addCell(new Cell().add(new Paragraph("Totale")).setBold());
        
        // Variabili per i calcoli
        double subtotale = 0.0;
        double totalIva = 0.0;
        
        // Iteriamo su ogni prodotto dell'ordine e aggiungiamo le righe alla tabella
        if (dettagliOrdine != null && !dettagliOrdine.isEmpty()) {
            for (OrdineBean ordine : dettagliOrdine) {
                int idProdotto = ordine.getIdProdotto();
                double prezzoUnitario = ordine.getPrezzoUnitario();
                double iva = ordine.getIva();
                int quantita = ordine.getQuantitaAcquistata();
                
                // Recuperiamo il nome del prodotto dal DAO
                ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);
                String nomeProdotto = (prodotto != null) ? prodotto.getNome() : "Prodotto " + idProdotto;
                
                // Calcoliamo il totale della riga
                double totaleRiga = prezzoUnitario * quantita;
                double ivaRiga = (totaleRiga * iva) / 100;
                double totaleConIva = totaleRiga + ivaRiga;
                
                // Aggiungiamo i totali
                subtotale += totaleRiga;
                totalIva += ivaRiga;
                
                // Aggiungiamo i dati della riga alla tabella
                table.addCell(new Cell().add(new Paragraph(String.valueOf(idProdotto))));
                table.addCell(new Cell().add(new Paragraph(nomeProdotto)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(quantita))));
                table.addCell(new Cell().add(new Paragraph("€ " + String.format("%.2f", prezzoUnitario))));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f%%", iva))));
                table.addCell(new Cell().add(new Paragraph("€ " + String.format("%.2f", totaleConIva))));
            }
        }
        
        document.add(table);
        document.add(new Paragraph("\n"));
        
        // Riepilogo economico
        document.add(new Paragraph("RIEPILOGO ECONOMICO")
                .setBold()
                .setFontSize(12));
        
        document.add(new Paragraph("Subtotale: € " + String.format("%.2f", subtotale)));
        document.add(new Paragraph("IVA Totale: € " + String.format("%.2f", totalIva)));
        
        // Totale finale (dal database, per garantire l'integrità)
        document.add(new Paragraph("TOTALE: € " + String.format("%.2f", acquisto.getPrezzoTotale()))
                .setBold()
                .setFontSize(14));
        
        document.add(new Paragraph("\n\n"));
        
        // Footer
        document.add(new Paragraph("Grazie per il vostro acquisto!")
                .setHorizontalAlignment(HorizontalAlignment.CENTER));
        document.add(new Paragraph("Grill - Progetto Java EE")
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setFontSize(9));
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Le richieste POST vengono redirette su doGet
        doGet(request, response);
    }
}
