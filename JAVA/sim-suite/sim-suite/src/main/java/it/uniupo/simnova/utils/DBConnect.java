package it.uniupo.simnova.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe di utilità per la gestione della connessione al database SQLite.
 * <p>
 * Questa classe implementa il <strong>pattern Singleton</strong> per garantire che esista una sola istanza
 * della connessione al database durante l'esecuzione dell'applicazione.
 * Offre metodi per ottenere e chiudere le connessioni al database SQLite.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class DBConnect {
    /**
     * L'URL di connessione al database SQLite.
     * Il database viene creato o aperto nel percorso della directory corrente dell'applicazione.
     * Formato: <code>jdbc:sqlite:[percorso_alla_directory]/database.db</code>
     */
    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/database.db";

    /**
     * L'istanza Singleton di {@link DBConnect}.
     * È inizializzata a <code>null</code> e creata al primo accesso tramite {@link #getInstance()}.
     */
    private static DBConnect instance = null;

    /**
     * Costruttore privato della classe.
     * <p>
     * Questo costruttore è privato per imporre il pattern Singleton.
     * Al momento della creazione dell'istanza, tenta di caricare il driver JDBC per SQLite.
     * </p>
     *
     * @throws RuntimeException se il driver SQLite non viene trovato, indicando un problema di configurazione.
     */
    private DBConnect() {
        System.out.println("URL del database: " + DB_URL); // Stampa l'URL del DB per debugging/informazione.
        try {
            // Carica esplicitamente il driver SQLite per garantire la sua disponibilità.
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            // Lancia una RuntimeException se il driver non è disponibile.
            throw new RuntimeException("Driver SQLite non trovato. Assicurati che la libreria JDBC SQLite sia nel classpath.", e);
        }
    }

    /**
     * Restituisce l'unica istanza Singleton di {@link DBConnect}.
     * Se l'istanza non è ancora stata creata, viene inizializzata in modo thread-safe.
     *
     * @return L'istanza Singleton di {@link DBConnect}.
     */
    public static synchronized DBConnect getInstance() {
        if (instance == null) {
            instance = new DBConnect();
        }
        return instance;
    }

    /**
     * Ottiene una nuova connessione al database SQLite.
     * Ogni chiamata a questo metodo restituisce una nuova connessione, che dovrebbe essere
     * chiusa esplicitamente dopo l'uso per rilasciare le risorse.
     *
     * @return Una {@link Connection} al database.
     * @throws SQLException se si verifica un errore durante il tentativo di connessione al database.
     */
    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            // Lancia un'eccezione SQL personalizzata con un messaggio più descrittivo in caso di fallimento della connessione.
            throw new SQLException("Impossibile connettersi al database all'URL: " + DB_URL + ". Dettagli: " + e.getMessage(), e);
        }
    }
}