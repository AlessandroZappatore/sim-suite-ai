package it.uniupo.simnova.domain.scenario;

/**
 * Classe che rappresenta uno <strong>scenario di simulazione</strong>.
 * Contiene i dettagli principali dello scenario come titolo, nome del paziente,
 * patologia, descrizione, briefing, patto d'aula, obiettivo, moulage, liquidi,
 * timer generale, autori, tipologia, informazioni per i genitori e target.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class Scenario {
    /**
     * <strong>Nome</strong> del paziente.
     */
    private final String nome_paziente;
    /**
     * <strong>Patto d'aula</strong> dello scenario.
     */
    private final String patto_aula;
    /**
     * <strong>Obiettivo</strong> didattico dello scenario.
     */
    private final String obiettivo;
    /**
     * <strong>Timer generale</strong> dello scenario (in secondi).
     */
    private final float timer_generale;
    /**
     * <strong>Informazioni per il genitore</strong> (se scenario pediatrico).
     */
    private final String infoGenitore;
    /**
     * <strong>Identificativo univoco</strong> dello scenario.
     */
    private int id;
    /**
     * <strong>Titolo</strong> dello scenario.
     */
    private String titolo;
    /**
     * <strong>Patologia</strong> del paziente.
     */
    private String patologia;
    /**
     * <strong>Descrizione</strong> dettagliata dello scenario.
     */
    private String descrizione;
    /**
     * <strong>Briefing</strong> (informazioni preliminari) dello scenario.
     */
    private String briefing;
    /**
     * <strong>Moulage</strong> (trucco ed effetti speciali) dello scenario.
     */
    private String moulage;
    /**
     * <strong>Liquidi e dosi farmaci</strong> disponibili nello scenario.
     */
    private String liquidi;
    /**
     * <strong>Autori</strong> che hanno creato lo scenario.
     */
    private String autori;
    /**
     * <strong>Tipologia</strong> dello scenario (es. "Adulto", "Pediatrico").
     */
    private String tipologia;
    /**
     * <strong>Target</strong> di riferimento dello scenario (es. "Medici Specialisti").
     */
    private String target;

    /**
     * Costruttore completo per creare un nuovo oggetto <strong><code>Scenario</code></strong> con tutti i campi.
     * Applica una validazione per il `timer_generale` assicurando che non sia negativo.
     * Imposta `infoGenitore` a null se la tipologia non è "Pediatrico".
     *
     * @param id             <strong>Identificativo univoco</strong> dello scenario.
     * @param titolo         <strong>Titolo</strong> dello scenario.
     * @param nome_paziente  <strong>Nome</strong> del paziente.
     * @param patologia      <strong>Patologia</strong> del paziente.
     * @param descrizione    <strong>Descrizione</strong> dello scenario.
     * @param briefing       <strong>Briefing</strong> dello scenario.
     * @param patto_aula     <strong>Patto d'aula</strong> dello scenario.
     * @param obiettivo      <strong>Obiettivo</strong> dello scenario.
     * @param moulage        <strong>Moulage</strong> dello scenario.
     * @param liquidi        <strong>Liquidi</strong> e dosi farmaci dello scenario.
     * @param timer_generale <strong>Timer generale</strong> dello scenario. Se negativo, viene impostato a 0.
     * @param autori         <strong>Autori</strong> dello scenario.
     * @param tipologia      <strong>Tipologia</strong> dello scenario (es. "Adulto", "Pediatrico").
     * @param infoGenitore   <strong>Informazioni del genitore</strong> dello scenario (se applicabile).
     * @param target         <strong>Informazioni sul target</strong> dello scenario.
     */
    public Scenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String obiettivo, String moulage, String liquidi, float timer_generale, String autori, String tipologia, String infoGenitore, String target) {
        this.id = id;
        this.titolo = titolo;
        this.nome_paziente = nome_paziente;
        this.patologia = patologia;
        this.descrizione = descrizione;
        this.briefing = briefing;
        this.patto_aula = patto_aula;
        this.obiettivo = obiettivo;
        this.moulage = moulage;
        this.liquidi = liquidi;
        // Valida il timer_generale, assicurando che non sia negativo.
        if (timer_generale < 0) {
            this.timer_generale = 0;
        } else {
            this.timer_generale = timer_generale;
        }
        this.autori = autori;
        this.tipologia = tipologia;
        // Le informazioni del genitore sono rilevanti solo per scenari pediatrici.
        if (tipologia != null && tipologia.equals("Pediatrico")) {
            this.infoGenitore = infoGenitore;
        } else {
            this.infoGenitore = null;
        }
        this.target = target;
    }

    /**
     * Costruttore di default per creare un oggetto <strong><code>Scenario</code></strong> con i campi principali.
     * Usato per scenari di base o per un'inizializzazione parziale.
     *
     * @param id          <strong>Identificativo univoco</strong> dello scenario.
     * @param titolo      <strong>Titolo</strong> dello scenario.
     * @param autori      <strong>Autori</strong> dello scenario.
     * @param patologia   <strong>Patologia</strong> del paziente.
     * @param descrizione <strong>Descrizione</strong> dello scenario.
     * @param tipologia   <strong>Tipologia</strong> dello scenario.
     */
    public Scenario(int id, String titolo, String autori, String patologia, String descrizione, String tipologia) {
        this.id = id;
        this.titolo = titolo;
        this.autori = autori;
        this.patologia = patologia;
        this.descrizione = descrizione;
        this.tipologia = tipologia;
        // Inizializza gli altri campi a null o a valori predefiniti per evitare NullPointerException.
        this.nome_paziente = null;
        this.briefing = null;
        this.patto_aula = null;
        this.obiettivo = null;
        this.moulage = null;
        this.liquidi = null;
        this.timer_generale = 0;
        this.infoGenitore = null;
        this.target = null;
    }

    /**
     * Restituisce l'<strong>identificativo univoco</strong> dello scenario.
     *
     * @return L'ID dello scenario.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'<strong>identificativo univoco</strong> dello scenario.
     *
     * @param id Il nuovo ID dello scenario.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il <strong>titolo</strong> dello scenario.
     *
     * @return Il titolo dello scenario.
     */
    public String getTitolo() {
        return titolo;
    }

    /**
     * Imposta il <strong>titolo</strong> dello scenario.
     *
     * @param titolo Il nuovo titolo dello scenario.
     */
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    /**
     * Restituisce il <strong>nome del paziente</strong> associato allo scenario.
     *
     * @return Il nome del paziente.
     */
    public String getNomePaziente() {
        return nome_paziente;
    }

    /**
     * Restituisce la <strong>patologia</strong> del paziente.
     *
     * @return La patologia del paziente.
     */
    public String getPatologia() {
        return patologia;
    }

    /**
     * Imposta la <strong>patologia</strong> del paziente.
     *
     * @param patologia La nuova patologia.
     */
    public void setPatologia(String patologia) {
        this.patologia = patologia;
    }

    /**
     * Restituisce la <strong>descrizione</strong> dello scenario.
     *
     * @return La descrizione dello scenario.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Imposta la <strong>descrizione</strong> dello scenario.
     *
     * @param descrizione La nuova descrizione.
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Restituisce il <strong>briefing</strong> dello scenario.
     *
     * @return Il briefing dello scenario.
     */
    public String getBriefing() {
        return briefing;
    }

    /**
     * Imposta il <strong>briefing</strong> dello scenario.
     *
     * @param briefing Il nuovo briefing.
     */
    public void setBriefing(String briefing) {
        this.briefing = briefing;
    }

    /**
     * Restituisce il <strong>patto d'aula</strong> dello scenario.
     *
     * @return Il patto d'aula dello scenario.
     */
    public String getPattoAula() {
        return patto_aula;
    }

    /**
     * Restituisce l'<strong>obiettivo</strong> didattico dello scenario.
     *
     * @return L'obiettivo didattico dello scenario.
     */
    public String getObiettivo() {
        return obiettivo;
    }

    /**
     * Restituisce la descrizione del <strong>moulage</strong> dello scenario.
     *
     * @return Il moulage dello scenario.
     */
    public String getMoulage() {
        return moulage;
    }

    /**
     * Imposta la descrizione del <strong>moulage</strong> dello scenario.
     *
     * @param moulage La nuova descrizione del moulage.
     */
    public void setMoulage(String moulage) {
        this.moulage = moulage;
    }

    /**
     * Restituisce la descrizione dei <strong>liquidi e dosi farmaci</strong> dello scenario.
     *
     * @return I liquidi e dosi farmaci dello scenario.
     */
    public String getLiquidi() {
        return liquidi;
    }

    /**
     * Imposta la descrizione dei <strong>liquidi e dosi farmaci</strong> dello scenario.
     *
     * @param liquidi La nuova descrizione di liquidi e dosi farmaci.
     */
    public void setLiquidi(String liquidi) {
        this.liquidi = liquidi;
    }

    /**
     * Restituisce il valore del <strong>timer generale</strong> dello scenario.
     *
     * @return Il timer generale dello scenario (in secondi).
     */
    public float getTimerGenerale() {
        return timer_generale;
    }

    /**
     * Restituisce gli <strong>autori</strong> dello scenario.
     *
     * @return Gli autori dello scenario.
     */
    public String getAutori() {
        return autori;
    }

    /**
     * Imposta gli <strong>autori</strong> dello scenario.
     *
     * @param autori I nuovi autori.
     */
    public void setAutori(String autori) {
        this.autori = autori;
    }

    /**
     * Restituisce la <strong>tipologia</strong> dello scenario.
     *
     * @return La tipologia dello scenario (es. "Adulto", "Pediatrico"). Restituisce una stringa vuota se è null.
     */
    public String getTipologia() {
        return tipologia != null ? tipologia : "";
    }

    /**
     * Imposta la <strong>tipologia</strong> dello scenario.
     *
     * @param tipologia La nuova tipologia dello scenario.
     */
    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    /**
     * Restituisce le <strong>informazioni per il genitore</strong> dello scenario.
     * Questo campo è rilevante solo per scenari pediatrici.
     *
     * @return Le informazioni per il genitore.
     */
    public String getInfoGenitore() {
        return infoGenitore;
    }

    /**
     * Restituisce le <strong>informazioni sul target</strong> dello scenario.
     *
     * @return Le informazioni sul target dello scenario.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Imposta le <strong>informazioni sul target</strong> dello scenario.
     *
     * @param target Le nuove informazioni sul target.
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto <strong><code>Scenario</code></strong>,
     * utile per il debugging e la registrazione.
     *
     * @return Una stringa che descrive l'ID, il titolo, il nome del paziente, la patologia,
     * la descrizione, il briefing, il patto d'aula, l'obiettivo, il moulage, i liquidi,
     * il timer generale, gli autori, la tipologia, le info genitore e il target.
     */
    @Override
    public String toString() {
        return "Scenario{" +
                "id=" + id +
                ", titolo='" + titolo + '\'' +
                ", nome_paziente='" + nome_paziente + '\'' +
                ", patologia='" + patologia + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", briefing='" + briefing + '\'' +
                ", patto_aula='" + patto_aula + '\'' +
                ", obiettivo='" + obiettivo + '\'' +
                ", moulage='" + moulage + '\'' +
                ", liquidi='" + liquidi + '\'' +
                ", timer_generale=" + timer_generale +
                ", autori='" + autori + '\'' +
                ", tipologia='" + tipologia + '\'' +
                ", infoGenitore='" + infoGenitore + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}