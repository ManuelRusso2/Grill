package control;

import java.io.ByteArrayOutputStream;
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
            
            // 7. Generazione della risposta PDF in memoria
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);
            document.setMargins(36, 36, 36, 36);
            
            // Aggiungiamo il contenuto della fattura
            generaFatturaHTML(document, acquisto, cliente, dettagliOrdine);
            
            document.close();
            
            // Invio il PDF al browser
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"Fattura_" + idAcquisto + ".pdf\"");
            response.setContentLength(baos.size());
            
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();
            
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
        
        // Scritta Grill in alto al centro
        document.add(new Paragraph("GRILL")
                .setBold()
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER));
        
        // Intestazione della fattura
        document.add(new Paragraph("FATTURA N. " + acquisto.getIdAcquisto())
                .setBold()
                .setFontSize(18));
        
        // Formattazione della data
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dataFormattata = (acquisto.getDataAcquisto() != null) ? sdf.format(acquisto.getDataAcquisto()) : "N/D";
        document.add(new Paragraph("Data: " + dataFormattata));
        
        // Dati del Venditore
        document.add(new Paragraph("\nVenditore:")
                .setBold());
        document.add(new Paragraph("Grill Store\nVia Roma 10, Salerno\nP.IVA: 01234567890"));
        
        // Dati del cliente (Intestatario della fattura)
        document.add(new Paragraph("\nCliente:")
                .setBold());
        document.add(new Paragraph(cliente.getNome() + " " + cliente.getCognome()));
        document.add(new Paragraph("Email: " + cliente.getEmail()));
        document.add(new Paragraph("Telefono: " + (cliente.getTelefono() != null ? cliente.getTelefono() : "N/A")));
        document.add(new Paragraph("Indirizzo Consegna: " + acquisto.getIndirizzoConsegna()));
        document.add(new Paragraph("Metodo di Pagamento: " + acquisto.getMetodoPagamento()));
        
        document.add(new Paragraph("\nProdotti acquistati:")
                .setBold());
        
        // Tabella dei prodotti
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2, 2, 2}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // Header della tabella
        table.addHeaderCell(new Cell().add(new Paragraph("Prodotto").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Quantità").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Prezzo Unit.").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("IVA %").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Totale (IVA incl.)").setBold()));
        
        // Variabili per i calcoli
        double subtotaleNetto = 0.0;
        double totalIva = 0.0;
        
        // Iteriamo su ogni prodotto dell'ordine e aggiungiamo le righe alla tabella
        if (dettagliOrdine != null && !dettagliOrdine.isEmpty()) {
            for (OrdineBean ordine : dettagliOrdine) {
                int idProdotto = ordine.getIdProdotto();
                double prezzoIvaInclusa = ordine.getPrezzoUnitario();
                double aliquotaIva = ordine.getIva();
                int quantita = ordine.getQuantitaAcquistata();
                
                // Recuperiamo il nome del prodotto dal DAO
                ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);
                String nomeProdotto = (prodotto != null) ? prodotto.getNome() : "Prodotto #" + idProdotto;
                
                double totaleRigaIvaInclusa = prezzoIvaInclusa * quantita;
                
                // Scomputo IVA: Imponibile = Totale / (1 + IVA/100)
                double imponibileRiga = totaleRigaIvaInclusa / (1.0 + (aliquotaIva / 100.0));
                double ivaRiga = totaleRigaIvaInclusa - imponibileRiga;
                
                // Aggiungiamo i totali
                subtotaleNetto += imponibileRiga;
                totalIva += ivaRiga;
                
                // Aggiungiamo i dati della riga alla tabella
                table.addCell(new Cell().add(new Paragraph(nomeProdotto)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(quantita))));
                table.addCell(new Cell().add(new Paragraph(String.format("€ %.2f", prezzoIvaInclusa))));
                table.addCell(new Cell().add(new Paragraph(String.format("%.0f%%", aliquotaIva))));
                table.addCell(new Cell().add(new Paragraph(String.format("€ %.2f", totaleRigaIvaInclusa))));
            }
        }
        
        document.add(table);
        
        // Riepilogo economico
        document.add(new Paragraph("\nImponibile (excl. IVA): € " + String.format("%.2f", subtotaleNetto)));
        document.add(new Paragraph("IVA Totale: € " + String.format("%.2f", totalIva)));
        
        // Totale finale (dal database, per garantire l'integrità)
        document.add(new Paragraph("Totale Complessivo: € " + String.format("%.2f", acquisto.getPrezzoTotale()))
                .setBold()
                .setFontSize(14));
        
        document.add(new Paragraph("\n\n"));
        
        // Footer
        document.add(new Paragraph("Grazie per il vostro acquisto!"));
        document.add(new Paragraph("Grill - Progetto Java EE")
                .setFontSize(9));
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Le richieste POST vengono redirette su doGet
        doGet(request, response);
    }
}