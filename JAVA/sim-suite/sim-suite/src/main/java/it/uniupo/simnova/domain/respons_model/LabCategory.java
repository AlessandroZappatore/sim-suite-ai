package it.uniupo.simnova.domain.respons_model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta una categoria di laboratorio con i relativi test.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Getter
@Setter
public class LabCategory {
    /**
     * Identificativo della categoria di laboratorio.
     */
    private int id;

    /**
     * Nome della categoria di laboratorio.
     */
    @JsonProperty("categoria") // Mappa il campo JSON 'categoria'
    private String nomeCategoria;

    /**
     * Lista dei test associati a questa categoria di laboratorio.
     */
    @JsonProperty("test") // Mappa il campo JSON 'test'
    private List<LabTest> test = new ArrayList<>();

    /**
     * Costruttore di default.
     *
     * @param id l'identificativo della categoria di laboratorio
     * @param nomeCategoria il nome della categoria di laboratorio
     */
    public LabCategory(int id, String nomeCategoria) {
        this.id = id;
        this.nomeCategoria = nomeCategoria;
    }

    /**
     * Aggiunge un test alla lista dei test di questa categoria di laboratorio.
     *
     * @param labTest il test da aggiungere
     */
    public void addTest(LabTest labTest) {
        this.test.add(labTest);
    }
}