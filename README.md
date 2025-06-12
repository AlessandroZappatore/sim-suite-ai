# 🧠 SIMSUITEAI
<img src="DOCS/img/icon.png" alt="SIMSUITEAI Logo" width="100" height="100">

**Il Futuro della Simulazione Clinica**

*Estensione intelligente di SimSuite che rivoluziona la creazione di scenari di simulazione clinica attraverso l'intelligenza artificiale avanzata.*

[![Python](https://img.shields.io/badge/Python-3.12+-blue.svg)](https://python.org)
[![FastAPI](https://img.shields.io/badge/FastAPI-Latest-green.svg)](https://fastapi.tiangolo.com)
[![AI Powered](https://img.shields.io/badge/AI-Powered-purple.svg)](https://ai.google.dev)

[📖 Documentazione](https://alessandrozappatore.github.io/sim-suite-ai/) • [🛠️ Installazione](#installazione)

---

## 🌟 Panoramica

**SIMSUITEAI** è un'estensione rivoluzionaria per SimSuite che integra l'intelligenza artificiale per automatizzare e ottimizzare la creazione di scenari di simulazione clinica. Progettato specificamente per istruttori e formatori medici, SIMSUITEAI trasforma il processo di creazione di contenuti didattici da ore di lavoro manuale a pochi minuti di generazione automatica.

### 🎯 Funzionalità Principali

- **🧠 Generazione Intelligente di Scenari**: Crea automaticamente scenari clinici completi basati su tipologia, target e descrizione
- **🔬 Esami di Laboratorio Automatici**: Genera esami e referti medici realistici correlati agli scenari
- **⚡ Elaborazione in Background**: Continua a lavorare mentre l'AI genera contenuti
- **🎨 Interfaccia Moderna**: UI intuitiva e user-friendly
- **🔄 Integrazione Seamless**: Si integra perfettamente con SimSuite esistente

---

## 🚀 Installazione

### Prerequisiti

- **Python 3.12+**
- **SimSuite** (installazione base)
- **Connessione Internet** (per servizi AI)

### Installazione Rapida

1. **Clona il repository**
   ```bash
   git clone https://github.com/yourusername/sim-suite-ai.git
   cd sim-suite-ai
   ```

2. **Configura l'ambiente virtuale**
   ```bash
   cd AGENTS
   python -m venv aienv
   
   # Windows
   aienv\Scripts\activate
   
   # Linux/macOS
   source aienv/bin/activate
   ```

3. **Installa le dipendenze**
   ```bash
   pip install -r requirements.txt
   ```

4. **Configura l'ambiente**
   ```bash
   # Crea file .env con le tue API keys
   cp .env.example .env
   # Modifica .env con le tue credenziali
   ```

5. **Avvia l'applicazione**
   ```bash
   python main.py
   ```

---

## ⚙️ Configurazione

### Variabili d'Ambiente

Crea un file `.env` nella cartella `AGENTS/` con le seguenti variabili:

```env
# API Keys
GOOGLE_API_KEY=your_google_api_key_here
OPENAI_API_KEY=your_openai_api_key_here

# Database
DATABASE_URL=sqlite:///database.db

# Server
HOST=localhost
PORT=8000
DEBUG=True
```

### Configurazione Avanzata

Il file `config.py` contiene le impostazioni principali:

- **Modelli AI**: Configurazione dei modelli di intelligenza artificiale
- **Database**: Impostazioni di connessione database
- **Logging**: Configurazione dei log di sistema
- **Performance**: Ottimizzazioni e cache

---

## 🏗️ Architettura

```
sim-suite-ai/
├── 🤖 AGENTS/                 # Core AI e Backend
│   ├── sim_suite_ai.py       # Engine principale AI
│   ├── exam_agent.py         # Agente per esami di laboratorio
│   ├── main.py               # Entry point applicazione
│   ├── config.py             # Configurazioni sistema
│   └── requirements.txt      # Dipendenze Python
├── 📚 DOCS/                  # Documentazione e Frontend
│   ├── index.html           # Documentazione interattiva
│   ├── styles.css           # Stili moderni
│   ├── script.js            # Interattività
│   └── 🎥 videos/           # Video dimostrativi
├── ☕ JAVA/                 # Integrazione SimSuite
│   └── sim-suite/           # Moduli Java SimSuite
└── 📄 database.db           # Database scenari e configurazioni
```

---

## 🎮 Utilizzo

### 1. Generazione Scenari

```python
# Esempio di utilizzo programmatico
from sim_suite_ai import ScenarioGenerator

generator = ScenarioGenerator()
scenario = generator.create_scenario(
    tipo="Emergenza",
    target="Infermieri",
    descrizione="Paziente con arresto cardiaco in pronto soccorso"
)
```

### 2. Creazione Esami

```python
# Generazione automatica esami di laboratorio
from exam_agent import ExamAgent

agent = ExamAgent()
esami = agent.generate_lab_tests(
    scenario_id="scenario_123",
    tipo_paziente="adulto",
    patologia="infarto_miocardico"
)
```

### 3. Interfaccia Web

1. Avvia il server: `python main.py`
2. Apri browser su: `http://localhost:8000`
3. Segui il workflow guidato per creare scenari

---

## 🔧 API Reference

### Endpoints Principali

| Endpoint | Metodo | Descrizione |
|----------|--------|-------------|
| `/api/scenarios` | POST | Crea nuovo scenario |
| `/api/scenarios/{id}` | GET | Recupera scenario specifico |
| `/api/exams/generate` | POST | Genera esami di laboratorio |
| `/api/status/{task_id}` | GET | Stato elaborazione background |

### Esempio Request

```bash
curl -X POST "http://localhost:8000/api/scenarios" \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "Chirurgia",
    "target": "Specializzandi",
    "descrizione": "Intervento di appendicectomia laparoscopica"
  }'
```

---

## 🤝 Contribuire

Siamo aperti a contributi! Segui questi passi:

1. **Fork** il repository
2. **Crea** un branch per la tua feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** le modifiche (`git commit -m 'Add: Amazing Feature'`)
4. **Push** al branch (`git push origin feature/AmazingFeature`)
5. **Apri** una Pull Request

### Linee Guida

- Segui il coding style esistente
- Aggiungi test per nuove funzionalità
- Documenta le API pubbliche
- Aggiorna la documentazione se necessario

---

## 🐛 Troubleshooting

### Problemi Comuni

**Errore API Key non valida**
```bash
# Verifica che le API key siano configurate correttamente
echo $GOOGLE_API_KEY
```

**Porta già in uso**
```bash
# Cambia porta nel config.py o termina processo esistente
lsof -ti:8000 | xargs kill -9
```

**Dipendenze mancanti**
```bash
# Reinstalla tutte le dipendenze
pip install -r requirements.txt --force-reinstall
```

---

## 📊 Performance

- **Generazione Scenario**: ~30-60 secondi
- **Esami di Laboratorio**: ~15-30 secondi
- **Elaborazione Background**: Non blocca l'UI
- **Memory Usage**: ~200-500MB

---
**Realizzato con ❤️ per la formazione medica**

[⭐ Dai una stella](https://github.com/yourusername/sim-suite-ai) se questo progetto ti è utile!