# 🧠 SIMSUITEAI

<img src="DOCS/img/icon.png" alt="SIMSUITEAI Logo" width="100" height="100">

**Intelligenza Artificiale per la Simulazione Clinica**

SimSuiteAI è un'estensione che utilizza l'intelligenza artificiale per creare automaticamente scenari di simulazione medica completi e realistici.

---

## 🚀 Avvio Rapido

### 1. Prepara l'Ambiente
```bash
# Vai nella cartella AGENTS
cd AGENTS

# Attiva l'ambiente virtuale
aienv\Scripts\activate  # Windows
# oppure
source aienv/bin/activate  # Linux/macOS

# Installa le dipendenze
pip install -r requirements.txt
```

### 2. Configura le API Key
Crea un file `.env` nella cartella `AGENTS/` con:
```env
GOOGLE_API_KEY=la_tua_chiave_google
```

### 3. Avvia i Servizi

**Backend AI (Porta 8000):**
```bash
python main.py
```

**Interfaccia SimSuite (Porta 9090):**
```bash
# Vai nella cartella Java
cd JAVA/sim-suite

# Avvia l'interfaccia SimSuite
# (Seguire le istruzioni specifiche del progetto Java)
```

### 4. Accedi all'Applicazione
- **SimSuite Interface**: http://localhost:9090
- **API Backend**: http://localhost:8001

---

## � Come Funziona

1. **Accedi a SimSuite** su porta 9090
2. **Descrivi lo scenario** che vuoi creare
3. **L'AI genera automaticamente**:
   - Scenario completo con timeline
   - Esami di laboratorio
   - Referti medici
   - Script per pazienti simulati

---

## 📋 Funzionalità

- ✅ **Scenari Completi**: Genera scenari medici dettagliati
- ✅ **Esami di Laboratorio**: Crea esami realistici con referti
- ✅ **Timeline Clinica**: Evoluzione temporale degli scenari
- ✅ **Referti Medici**: Genera referti radiologici, ECG, ecc.
- ✅ **Interfaccia Semplice**: Facile da usare per tutti

---

## 🔧 Requisiti

- Python 3.12+
- Connessione Internet
- API Key Google AI
- Java (per interfaccia SimSuite)

---

## 📁 Struttura Progetto

```
sim-suite-ai/
├── AGENTS/          # Backend AI (Python)
├── JAVA/           # Interfaccia SimSuite (Java)
├── DOCS/           # Documentazione
└── database.db     # Database scenari
```

---

## ❓ Risoluzione Problemi

**Il backend non parte?**
- Verifica che l'ambiente virtuale sia attivo
- Controlla che la API key sia configurata

**Non riesco ad accedere a SimSuite?**
- Assicurati che l'interfaccia Java sia avviata sulla porta 9090
- Controlla che il backend sia attivo sulla porta 8000

**Errori di generazione?**
- Verifica la connessione internet
- Controlla la validità della API key Google


**Buona simulazione! 🏥**