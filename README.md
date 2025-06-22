# 🧠 SIMSUITEAI

<img src="DOCS/img/icon.png" alt="SIMSUITEAI Logo" width="100" height="100">

**Intelligenza Artificiale per la Simulazione Clinica**

SimSuiteAI è un'estensione che utilizza l'intelligenza artificiale per creare automaticamente scenari di simulazione medica completi e realistici.
[Link sito SimSuiteAI](https://alessandrozappatore.github.io/sim-suite-ai/)

---

## 🚀 Avvio Rapido

### 1. Prepara l'Ambiente
```bash
# Vai nella cartella AGENTS
cd AGENTS

# Se non esiste, crea l'ambiente virtuale
python -m venv aienv

# Attiva l'ambiente virtuale
aienv\Scripts\activate  # Windows
# oppure
source aienv/bin/activate  # Linux/macOS

# Installa le dipendenze
pip install -r requirements.txt
```

### 2. Configura le API Key
Crea un file `.env` nella cartella `AGENTS/` con una delle seguenti chiavi:
```env
# Scegli UNA delle due opzioni:
GOOGLE_API_KEY=la_tua_chiave_google
# OPPURE
ANTRHOPIC_API_KEY=la_tua_chiave_anthropic
```

### 3. Avvia i Servizi

**Backend AI (Porta 8001):**
```bash
python main.py
```

**Interfaccia SimSuite (Porta 9090) 2 possibilità:**

***1. Tramite IntelliJ***

```bash
# Vai nella cartella Java
cd JAVA/sim-suite
```
Avvia l'applicazione tramite l'ide.

***2. Tramite l'installer (solo windows)***
1. Scarica l'installer per l'interfaccia da questo link: [Installer windows](https://github.com/AlessandroZappatore/sim-suite-ai/releases/download/1.0/SimSuiteAI_Installer_1.0.0.exe)
2. Avvia l'installer scaricato e segui le istruzioni a schermo per completare l'installazione.
3. Per dubbi sull'uso dell'installer consulta [SimSuite.it](https://simsuite.it)

**⚠️ Importante**: 
- **Solo per l'opzione 1 (IntelliJ)**: Sposta il file `database.db` nella cartella padre (fuori da `sim-suite-ai/`) per garantire la compatibilità con SimSuite
- **Per l'opzione 2 (Installer Windows)**: Non è necessario spostare il database - l'installer configura automaticamente tutto


### 4. Accedi all'Applicazione
- **SimSuite Interface**: [http://localhost:9090](http://localhost:9090)
- **API Backend**: [http://localhost:8001](http://localhost:8001)

---

## � Come Funziona

1. **Accedi a SimSuite** su porta 9090
2. **Descrivi lo scenario** che vuoi creare
3. **Seleziona il livello di difficoltà**:
   - **Facile**: Scenario semplice, parametri stabili, evoluzione prevedibile
   - **Medio**: Complessità moderata, 1-2 complicazioni gestibili
   - **Difficile**: Scenario complesso, multiple complicazioni, evoluzione critica
4. **L'AI genera automaticamente**:
   - Scenario completo con timeline adattata alla difficoltà
   - Esami di laboratorio
   - Referti medici
   - Script per pazienti simulati

---

## 📋 Funzionalità

- ✅ **Scenari Completi**: Genera scenari medici dettagliati con difficoltà personalizzabile
- ✅ **Livelli di Difficoltà**: Facile, Medio, Difficile per adattare la complessità
- ✅ **Esami di Laboratorio**: Crea esami realistici con referti
- ✅ **Timeline Clinica**: Evoluzione temporale degli scenari
- ✅ **Referti Medici**: Genera referti radiologici, ECG, ecc.
- ✅ **Interfaccia Semplice**: Facile da usare per tutti

---

## 🔧 Requisiti

- Python 3.12+
- Connessione Internet
- API Key Google AI o Anthropic (una delle due)
- Java (per interfaccia SimSuite)

---

## 📁 Struttura Progetto

```
sim-suite-ai/
├── AGENTS/          # Backend AI (Python)
├── JAVA/           # Interfaccia SimSuite (Java)
├── DOCS/           # Documentazione
└── database.db     # Database scenari (da spostare solo se si usa IntelliJ)
```

---

## ❓ Risoluzione Problemi

**Il backend non parte?**
- Verifica che l'ambiente virtuale sia attivo
- Controlla che almeno una API key sia configurata

**Non riesco ad accedere a SimSuite?**
- Assicurati che l'interfaccia Java sia avviata sulla porta 9090
- Consulta [SimSuite.it](https://simsuite.it) per supporto sull'interfaccia

**Non riesco a creare uno scenario?**
- **Solo se usi IntelliJ**: Controlla che il file `database.db` sia posizionato nella cartella padre (fuori da `sim-suite-ai/`)
- Verifica la connessione tra frontend e backend

**Errori di generazione?**
- Controlla che il backend sia attivo sulla porta 8001
- Verifica la connessione internet
- Controlla la validità della API key configurata


**Buona simulazione! 🏥**
