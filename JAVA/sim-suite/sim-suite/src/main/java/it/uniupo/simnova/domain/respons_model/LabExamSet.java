package it.uniupo.simnova.domain.respons_model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta un set di esami di laboratorio associati a uno scenario.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Getter
@Setter
public class LabExamSet {
    /**
     * Identificativo del set di esami di laboratorio.
     */
    private int id;
    /**
     * Identificativo dello scenario associato a questo set di esami.
     */
    private int scenarioId;

    /**
     * Lista delle categorie di laboratorio associate a questo set di esami.
     */
    @JsonProperty("esami_laboratorio") // Mappa il campo JSON 'esami_laboratorio'
    private List<LabCategory> categorie = new ArrayList<>();

    /**
     * Costruttore di default.
     *
     * @param id         l'identificativo del set di esami
     * @param scenarioId l'identificativo dello scenario associato a questo set di esami
     */
    public LabExamSet(int id, int scenarioId) {
        this.id = id;
        this.scenarioId = scenarioId;
    }

    /**
     * Aggiunge una categoria di laboratorio a questo set di esami.
     *
     * @param category la categoria da aggiungere
     */
    public void addCategory(LabCategory category) {
        this.categorie.add(category);
    }
}