package it.uniupo.simnova.views.constant;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Classe di supporto contenente le ***costanti dei parametri aggiuntivi** predefiniti.
 * Questi parametri possono essere utilizzati e visualizzati in diverse sezioni temporali
 * degli scenari di simulazione. La mappa garantisce un ordine di inserimento.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public final class AdditionParametersConst {

    /**
     * Mappa che contiene i ***parametri aggiuntivi predefiniti**.
     * La chiave della mappa è il nome tecnico del parametro (usato internamente),
     * mentre il valore è una descrizione più leggibile, spesso inclusiva dell'unità di misura.
     * `LinkedHashMap` viene usata per mantenere l'ordine di inserimento dei parametri.
     */
    public static final Map<String, String> ADDITIONAL_PARAMETERS = new LinkedHashMap<>();

    /**
     * Chiave speciale utilizzata come prefisso per identificare i ***parametri aggiuntivi personalizzati**
     * creati dall'utente, distinguendoli da quelli predefiniti.
     */
    public static final String CUSTOM_PARAMETER_KEY = "CUSTOM";

    // Blocco statico per l'inizializzazione della mappa dei parametri aggiuntivi
    static {
        // Parametri per Cardiologia / Monitor Multiparametrico
        ADDITIONAL_PARAMETERS.put("PVC", "Pressione Venosa Centrale (cmH₂O)");
        ADDITIONAL_PARAMETERS.put("QTc", "QT/QTc (ms)");
        ADDITIONAL_PARAMETERS.put("ST", "Segmento ST (mV)");
        ADDITIONAL_PARAMETERS.put("SI", "Indice di Shock (FC/PA sistolica)");

        // Parametri per Pneumologia / Ventilazione
        ADDITIONAL_PARAMETERS.put("PIP", "Pressione Inspiratoria Positiva (cmH₂O)");
        ADDITIONAL_PARAMETERS.put("VT", "Volume Corrente (mL/kg)");
        ADDITIONAL_PARAMETERS.put("COMP", "Compliance Polmonare (mL/cmH₂O)");
        ADDITIONAL_PARAMETERS.put("RAW", "Resistenza Vie Aeree (cmH₂O/L/s)");
        ADDITIONAL_PARAMETERS.put("RSBI", "Indice di Tobin (atti/min/L)");

        // Parametri per Neurologia / Neuro Monitoraggio
        ADDITIONAL_PARAMETERS.put("GCS", "Scala di Glasgow (3-15)");
        ADDITIONAL_PARAMETERS.put("ICP", "Pressione Intracranica (mmHg)");
        ADDITIONAL_PARAMETERS.put("PRx", "Indice di Pressione Cerebrale"); // Unità da definire se nota
        ADDITIONAL_PARAMETERS.put("BIS", "Bispectral Index (0-100)");
        ADDITIONAL_PARAMETERS.put("TOF", "Train of Four (%)");

        // Parametri per Emodinamica / Terapia Intensiva
        ADDITIONAL_PARAMETERS.put("CO", "Gittata Cardiaca (L/min)");
        ADDITIONAL_PARAMETERS.put("CI", "Indice Cardiaco (L/min/m²)");
        ADDITIONAL_PARAMETERS.put("PCWP", "Pressione Capillare Polmonare (mmHg)");
        ADDITIONAL_PARAMETERS.put("SvO2", "Saturazione Venosa Mista (%)");
        ADDITIONAL_PARAMETERS.put("SVR", "Resistenza Vascolare Sistemica (dyn·s·cm⁻⁵)");

        // Parametri per Metabolismo / Elettroliti
        ADDITIONAL_PARAMETERS.put("GLY", "Glicemia (mg/dL)");
        ADDITIONAL_PARAMETERS.put("LAC", "Lattati (mmol/L)");
        ADDITIONAL_PARAMETERS.put("NA", "Sodio (mmol/L)");
        ADDITIONAL_PARAMETERS.put("K", "Potassio (mmol/L)");
        ADDITIONAL_PARAMETERS.put("CA", "Calcio ionizzato (mmol/L)");

        // Parametri per Nefrologia / Diuresi
        ADDITIONAL_PARAMETERS.put("UO", "Diuresi oraria (mL/h)");
        ADDITIONAL_PARAMETERS.put("CR", "Creatinina (mg/dL)");
        ADDITIONAL_PARAMETERS.put("BUN", "Azotemia (mg/dL)");

        // Parametri per Infettivologia / Stato Infettivo
        ADDITIONAL_PARAMETERS.put("WBC", "Globuli Bianchi (10³/μL)");
        ADDITIONAL_PARAMETERS.put("qSOFA", "qSOFA (0-4)"); // Scala numerica

        // Parametri per Coagulazione / Ematologia
        ADDITIONAL_PARAMETERS.put("INR", "INR"); // Adimensionale
        ADDITIONAL_PARAMETERS.put("PTT", "PTT (sec)");
        ADDITIONAL_PARAMETERS.put("PLT", "Piastrine (10³/μL)");

        // Altri Monitoraggi Specializzati
        ADDITIONAL_PARAMETERS.put("pCO₂ cutanea", "pCO₂ cutanea (mmHg)");
        ADDITIONAL_PARAMETERS.put("NIRS", "Ossimetria cerebrale (%)");
    }

    /**
     * Costruttore privato per evitare l'istanza della classe.
     */
    public AdditionParametersConst() {
        // Costruttore privato per evitare l'istanza della classe
    }
}