package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.paziente.EsameFisico;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.service.scenario.components.PresidiService;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.views.common.utils.StyleApp;

import java.util.List;

/**
 * Classe di utility per la creazione del contenuto relativo al paziente al tempo T0.
 * Include la visualizzazione dei parametri vitali, degli accessi e dell'esame fisico.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class PatientT0Support {

    /**
     * Costruttore privato per evitare istanziazioni dirette della classe.
     * Utilizzare i metodi statici per creare il layout del paziente T0.
     */
    public PatientT0Support() {
        // Costruttore privato per evitare istanziazioni
    }

    /**
     * Crea un layout verticale che aggrega i componenti UI per visualizzare
     * i dati del paziente al tempo T0: parametri vitali, accessi ed esame fisico.
     * Fornisce anche un pulsante per l'aggiunta dei dati T0 se non presenti.
     *
     * @param paziente                L'oggetto {@link PazienteT0} contenente i dati iniziali del paziente.
     * @param esame                   L'oggetto {@link EsameFisico} contenente i dati dell'esame fisico.
     * @param scenarioId              L'ID dello scenario a cui si riferiscono i dati del paziente.
     * @param esameFisicoService      Il servizio per la gestione dell'esame fisico.
     * @param pazienteT0Service       Il servizio per la gestione dei dati del paziente T0.
     * @param presidiService          Il servizio per la gestione dei presidi.
     * @param advancedScenarioService Il servizio per gli scenari avanzati, usato per i parametri aggiuntivi del monitor.
     * @return Un {@link VerticalLayout} completo che rappresenta la sezione del paziente T0.
     */
    public static VerticalLayout createPatientContent(PazienteT0 paziente,
                                                      EsameFisico esame,
                                                      Integer scenarioId,
                                                      EsameFisicoService esameFisicoService,
                                                      PazienteT0Service pazienteT0Service,
                                                      PresidiService presidiService,
                                                      AdvancedScenarioService advancedScenarioService) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Sezione parametri vitali e accessi
        if (paziente != null) {
            Div patientCard = new Div();
            patientCard.addClassName("info-card");
            patientCard.getStyle()
                    .set("background-color", "var(--lumo-base-color)")
                    .set("border-radius", "var(--lumo-border-radius-l)")
                    .set("box-shadow", "var(--lumo-box-shadow-xs)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("margin-bottom", "var(--lumo-space-m)")
                    .set("transition", "box-shadow 0.3s ease-in-out")
                    .set("width", "80%")
                    .set("max-width", "800px");

            // Aggiunge effetti di hover al contenitore della card
            patientCard.getElement().executeJs(
                    "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                            "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
            );

            // Crea il monitor dei parametri vitali, passando i dati del paziente T0
            VitalSignsDataProvider t0DataProvider = new PazienteT0VitalSignsAdapter(paziente);
            Component vitalSignsMonitor = MonitorSupport.createVitalSignsMonitor(
                    t0DataProvider, scenarioId, true, presidiService, pazienteT0Service, advancedScenarioService, null);
            patientCard.add(vitalSignsMonitor);

            // Crea la card per gli accessi venosi e arteriosi
            Div accessCard = AccessSupport.getAccessoCard(pazienteT0Service, scenarioId);
            patientCard.add(accessCard);

            layout.add(patientCard);
        } else {
            // Se non ci sono dati paziente, mostra un messaggio e un pulsante per aggiungerli
            Div noDataCard = EmptySupport.createErrorContent("Nessun dato paziente disponibile");
            layout.add(noDataCard);
            HorizontalLayout buttonContainer = new HorizontalLayout();
            buttonContainer.setWidthFull();
            buttonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

            Button creatT0Button = StyleApp.getButton("Aggiungi i dati per T0", VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
            creatT0Button.addThemeVariants(ButtonVariant.LUMO_LARGE);
            creatT0Button.getStyle().set("background-color", "var(--lumo-success-color");
            // Naviga alla pagina di modifica/creazione del paziente T0
            creatT0Button.addClickListener(ev -> UI.getCurrent().navigate("pazienteT0/" + scenarioId + "/edit"));

            buttonContainer.add(creatT0Button);
            layout.add(buttonContainer);
        }

        // Sezione esame fisico
        Div examCard = PhysicalExamSupport.getExamCard(esame, esameFisicoService, scenarioId);
        layout.add(examCard);

        return layout;
    }

    /**
     * Implementazione dell'interfaccia {@link VitalSignsDataProvider} per l'oggetto {@link PazienteT0}.
     * Questo adapter permette di estrarre i parametri vitali specifici di PazienteT0
     * per il componente {@link MonitorSupport}.
     *
     * @param paziente L'oggetto PazienteT0 da cui estrarre i dati vitali.
     */
    private record PazienteT0VitalSignsAdapter(PazienteT0 paziente) implements VitalSignsDataProvider {

        @Override
        public String getPA() {
            return paziente.getPA();
        }

        @Override
        public Integer getFC() {
            return paziente.getFC();
        }

        @Override
        public Double getT() {
            return paziente.getT();
        }

        @Override
        public Integer getRR() {
            return paziente.getRR();
        }

        @Override
        public Integer getSpO2() {
            return paziente.getSpO2();
        }

        @Override
        public Integer getFiO2() {
            return paziente.getFiO2();
        }

        @Override
        public Double getLitriO2() {
            return paziente.getLitriO2();
        }

        @Override
        public Integer getEtCO2() {
            return paziente.getEtCO2();
        }

        @Override
        public String getAdditionalMonitorText() {
            return paziente.getMonitor();
        }

        @Override
        public List<ParametroAggiuntivo> getAdditionalParameters() {
            // Per il paziente T0, non ci sono parametri aggiuntivi gestiti separatamente dal monitor
            return List.of();
        }
    }
}