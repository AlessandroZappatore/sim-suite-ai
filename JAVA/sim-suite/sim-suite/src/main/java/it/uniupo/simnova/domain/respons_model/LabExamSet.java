package it.uniupo.simnova.domain.lab_exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LabExamSet {
    private int id;
    private int scenarioId;

    @JsonProperty("esami_laboratorio") // Mappa il campo JSON 'esami_laboratorio'
    private List<LabCategory> categorie = new ArrayList<>();

    public LabExamSet(int id, int scenarioId) {
        this.id = id;
        this.scenarioId = scenarioId;
    }

    public void addCategory(LabCategory category) {
        this.categorie.add(category);
    }
}