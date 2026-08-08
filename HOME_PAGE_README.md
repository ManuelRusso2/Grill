# Home Page - Documentazione

## Descrizione
La nuova home page contiene due sezioni principali:

### 1. Carousel delle Collezioni
- **Percorso**: `home.jsp`
- **Immagini**: Prelevate da `/images/Collezioni/`
- **Funzionalità**:
  - Scorrimento automatico ogni 5 secondi
  - Pulsanti di navigazione manuale (Prev/Next)
  - Indicatori di pagina per saltare direttamente a una collezione
  - Responsive design

### 2. Sezione Prodotti Casuali
- **Sorgente Dati**: API endpoint `/api/prodotti`
- **Funzionalità**:
  - Caricamento di 10 prodotti casuali
  - Scorrimento orizzontale con frecce
  - Mostra 4 prodotti per riga (desktop)
  - Bottoni disabilitati ai limiti dello scroll
  - Anteprima immagine, nome, prezzo e link "Dettagli"

### 3. Immagine Carrello nel Menu
- **Ubicazione**: Navigation bar (menu.jspf)
- **Funzionalità**:
  - Sostituisce il testo "Carrello"
  - Immagine: `carrello.png` (28x28px)
  - Effetto hover con zoom e shadow
  - Mantiene il badge con il conteggio degli articoli
  - Solo per utenti non-admin

## File Modificati/Creati

### Nuovi File
1. **`/jsp/user/home.jsp`** - Pagina home principale
2. **`/java/control/ProdottiApiServlet.java`** - API per recuperare prodotti in JSON

### File Modificati
1. **`/index.jsp`** - Reindirizza a `home.jsp` invece che a `CatalogoServlet`
2. **`/jsp/common/menu.jspf`** - Sostituito testo "Carrello" con immagine `carrello.png`
3. **`/css/global.css`** - Aggiunti stili per:
   - `.nav-cart-link` e `.nav-cart-icon` (carrello nel menu)
4. **`/css/user.css`** - Aggiunti stili per:
   - `.carousel-section` e componenti correlati
   - `.products-scroll-section` e componenti correlati
   - Stili responsive (768px, 480px)
   - Stili per bottoni disabilitati

## API Endpoint

### GET `/api/prodotti`
**Descrizione**: Restituisce un array JSON di prodotti casuali (max 10)

**Response Esempio**:
```json
[
  {
    "idProdotto": 1,
    "nome": "Maglietta Premium",
    "costo": 29.99,
    "immagine": "images/Magliette/maglietta1.jpg"
  },
  ...
]
```

## Responsive Design

### Desktop (>768px)
- Carousel: 16:9 aspect ratio
- Prodotti: 4 colonne (25% width ciascuno)
- Gap: 20px
- Bottoni: 45px

### Tablet (768px)
- Carousel: Ridotto
- Prodotti: 2 colonne (50% width ciascuno)
- Bottoni: 35px

### Mobile (<480px)
- Carousel: 100% width con padding ridotto
- Prodotti: 1 colonna (100% width)
- Bottoni: 30px

## Struttura Dati Prodotto

Il prodotto viene recuperato con i seguenti campi:
- `idProdotto` - ID univoco
- `nome` - Nome del prodotto
- `costo` - Prezzo (formattato con 2 decimali)
- `immagine` - Percorso relativo all'immagine (es: `images/Magliette/img.jpg`)

## Note di Implementazione

1. **Auto-scroll Carousel**: È attivo di default, passa alla slide successiva ogni 5 secondi
2. **Shuffle Prodotti**: I prodotti vengono mescolati casualmente ad ogni caricamento della pagina
3. **Error Handling**: Se l'API fallisce, viene visualizzato un messaggio console
4. **Lazy Loading**: Le immagini vengono caricate tramite tag `<img>` standard
5. **Context Path**: Viene utilizzato `${pageContext.request.contextPath}` per gestire dinamicamente i percorsi

## CSS Classes Utilizzate

### Carousel
- `.carousel-section` - Contenitore principale
- `.carousel-container` - Container con bottoni
- `.carousel-wrapper` - Overflow hidden
- `.carousel-track` - Track delle slide
- `.carousel-slide` - Singola slide
- `.carousel-btn` - Bottone di navigazione
- `.carousel-indicators` - Indicatori
- `.indicator` / `.indicator.active` - Singolo indicatore

### Menu Carrello
- `.nav-cart-link` - Link al carrello nel menu
- `.nav-cart-icon` - Immagine carrello
- `.badge` - Badge con conteggio

### Prodotti Scroll
- `.products-scroll-section` - Contenitore principale
- `.products-scroll-container` - Container con bottoni
- `.products-scroll-wrapper` - Overflow hidden
- `.products-scroll-track` - Track prodotti
- `.product-card-scroll` - Card prodotto
- `.product-image-scroll` - Immagine prodotto
- `.product-info-scroll` - Info prodotto
- `.scroll-btn` - Bottone di navigazione

## Browser Support
- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)
- Mobile browsers
