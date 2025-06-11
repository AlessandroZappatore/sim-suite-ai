# SIMSUITEAI - Documentazione

## Descrizione

Questo è il sito web di documentazione per **SIMSUITEAI**, un'estensione avanzata di SimSuite che integra l'intelligenza artificiale per la creazione automatica di scenari di simulazione clinica.

## Caratteristiche del Sito

### 🎨 Design Moderno
- Design responsivo e moderno con colori blu, celeste e accenti vivaci
- Animazioni fluide e effetti interattivi
- Supporto per tema scuro/chiaro
- Ottimizzato per dispositivi mobili e desktop

### 📱 Funzionalità
- **Navigazione fluida**: Menu di navigazione con smooth scrolling
- **Animazioni**: Effetti di apparizione e animazioni floating
- **Interattività**: Bottoni hover, effetti di transizione
- **Accessibilità**: Supporto keyboard navigation e screen readers
- **Performance**: Lazy loading e ottimizzazioni varie

### 🔧 Tecnologie Utilizzate
- **HTML5**: Struttura semantica moderna
- **CSS3**: Variabili CSS, Grid, Flexbox, animazioni
- **JavaScript ES6+**: Interattività e funzionalità avanzate
- **Font Awesome**: Icone moderne
- **Google Fonts**: Typography (Inter)

## Struttura del Progetto

```
DOCS/
├── index.html          # Pagina principale
├── styles.css          # Fogli di stile CSS
├── script.js           # JavaScript per interattività
└── README.md           # Questo file
```

## Sezioni del Sito

1. **Home**: Hero section con introduzione
2. **Panoramica**: Descrizione generale del progetto
3. **Intelligenza Artificiale**: Focus principale sulle funzionalità AI
4. **Tecnologie**: Stack tecnologico utilizzato
5. **Installazione**: Guida setup con link GitHub
6. **Simnova**: Contesto delle simulazioni cliniche

## Come Utilizzare

### Apertura del Sito
1. Assicurati di avere tutti i file nella cartella `DOCS`
2. Apri `index.html` in un browser web moderno
3. Il sito è completamente funzionale offline

### Navigazione
- Utilizza il menu di navigazione per spostarti tra le sezioni
- Su mobile, usa il menu hamburger
- Tutti i link sono funzionanti con smooth scrolling

### Personalizzazione
Per personalizzare il sito:

1. **Colori**: Modifica le variabili CSS in `:root` nel file `styles.css`
2. **Contenuti**: Aggiorna il testo nell'`index.html`
3. **Link GitHub**: Sostituisci `#` con l'URL reale del repository nella sezione setup

## Link GitHub da Aggiornare

Nel file `index.html`, nella sezione setup, aggiorna questo link:
```html
<a href="#" class="btn btn-github">
    <i class="fab fa-github"></i>
    Vai al Repository
</a>
```

Sostituisci `href="#"` con l'URL del tuo repository GitHub.

## Istruzioni per l'Installazione di SIMSUITEAI

Il sito include le seguenti istruzioni per gli utenti:

### 1. Configurazione API Keys
```bash
# Creare file .env nella cartella AGENTS
GOOGLE_API_KEY=your_google_api_key_here
ANTHROPIC_API_KEY=your_anthropic_api_key_here
```

### 2. Configurazione Environment Python
```bash
cd AGENTS
aienv\Scripts\activate
pip install -r requirements.txt
```

### 3. Avvio Backend Python
```bash
uvicorn main:main_app --reload
```

### 4. Avvio Frontend SimSuite
- Aprire progetto in IntelliJ IDEA
- Avviare applicazione
- Navigare su `localhost:9090`

## Browser Supportati

- Chrome/Chromium 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## Accessibilità

Il sito include:
- Navigazione keyboard
- Attributi ARIA appropriati
- Contrasto colori conforme WCAG
- Supporto screen readers
- Riduzione movimento per utenti sensibili

## Performance

Ottimizzazioni implementate:
- CSS minificato e ottimizzato
- JavaScript con lazy loading
- Immagini responsive
- Debounced scroll handlers
- Progressive enhancement

## Contributi

Per modificare o migliorare il sito:
1. Modifica i file HTML, CSS, o JS
2. Testa su diversi dispositivi e browser
3. Verifica l'accessibilità
4. Mantieni la coerenza del design

## Note Tecniche

### Variabili CSS
Le variabili principali sono definite in `:root` e includono:
- Colori primari e secondari
- Spaziature standardizzate
- Typography scale
- Ombre e border radius
- Transizioni

### JavaScript Modules
Il codice JavaScript è organizzato in:
- Event listeners per interattività
- Intersection Observer per animazioni
- Utility functions per performance
- Error handling

### Responsive Design
Breakpoints utilizzati:
- Mobile: < 768px
- Tablet: 768px - 1024px
- Desktop: > 1024px

## Licenza

Questo progetto di documentazione è stato creato per scopi educativi e di presentazione del progetto SIMSUITEAI.

---

**Sviluppato per il progetto SIMSUITEAI**  
*Innovazione nella simulazione clinica attraverso l'intelligenza artificiale*
