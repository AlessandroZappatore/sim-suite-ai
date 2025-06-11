package it.uniupo.simnova.domain.lab_exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LabCategory {
    private int id;

    @JsonProperty("categoria") // Mappa il campo JSON 'categoria'
    private String nomeCategoria;

    @JsonProperty("test") // Mappa il campo JSON 'test'
    private List<LabTest> test = new ArrayList<>();

    public LabCategory(int id, String nomeCategoria) {
        this.id = id;
        this.nomeCategoria = nomeCategoria;
    }

    public void addTest(LabTest labTest) {
        this.test.add(labTest);
    }
}