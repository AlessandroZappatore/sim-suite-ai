package it.uniupo.simnova.views.constant;

import java.util.List;

/**
 * Classe di supporto contenente le ***costanti per gli esami** di laboratorio e strumentali.
 * Queste liste predefinite sono utilizzate per popolare i campi di selezione
 * nell'interfaccia utente, garantendo coerenza e facilità d'uso.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public final class ExamConst {

    /**
     * Lista di stringhe contenente tutti i ***tipi di esami di laboratorio** predefiniti.
     */
    public static final List<String> ALLLABSEXAMS = List.of(
            "Emocromo con formula", "Glicemia", "Elettroliti sierici (Na⁺, K⁺, Cl⁻, Ca²⁺, Mg²⁺)",
            "Funzionalità renale (Creatinina, Azotemia)", "Funzionalità epatica (AST, ALT, Bilirubina, ALP, GGT)",
            "PCR (Proteina C Reattiva)", "Procalcitonina", "D-Dimero", "CK-MB, Troponina I/T",
            "INR, PTT, PT", "Gas arteriosi (pH, PaO₂, PaCO₂, HCO₃⁻, BE, Lactati)",
            "Emogas venoso", "Osmolarità sierica", "CPK", "Mioglobina"
    );
    /**
     * Lista di stringhe contenente tutti i ***tipi di esami strumentali** predefiniti.
     */
    public static final List<String> ALLINSTREXAMS = List.of(
            "ECG (Elettrocardiogramma)", "RX Torace", "TC Torace (con mdc)", "TC Torace (senza mdc)",
            "TC Addome (con mdc)", "TC Addome (senza mdc)", "Ecografia addominale", "Ecografia polmonare",
            "Ecocardio (Transtoracico)", "Ecocardio (Transesofageo)", "Spirometria", "EEG (Elettroencefalogramma)",
            "RM Encefalo", "TC Cranio (con mdc)", "TC Cranio (senza mdc)", "Doppler TSA (Tronchi Sovraortici)",
            "Angio-TC Polmonare", "Fundus oculi"
    );

    /**
     * Costruttore privato per evitare l'istanza della classe.
     * Questa classe è pensata per essere utilizzata solo come contenitore di costanti.
     */
    private ExamConst() {
        // Costruttore privato per evitare l'istanza della classe
    }
}