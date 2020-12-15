/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.ui.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import org.hedwig.leviosa.constants.CMSConstants;
import org.leviosa.core.driver.LeviosaClientService;
import org.hedwig.cms.dto.TermDTO;
import org.hedwig.cms.dto.TermInstanceDTO;
import org.hedwig.cms.dto.TermMetaDTO;
import org.patronus.cms.ui.login.CMSClientAuthCredentialValue;
import org.patronus.cms.ui.terminstance.TermInstanceUtil;
import org.patronus.cms.ui.terminstance.TermMetaKeyLabels;
import org.patronus.constants.PatronusConstants;
import org.patronus.core.client.PatronusCoreClient;
import org.patronus.core.dto.FractalDTO;
import org.patronus.response.FractalResponseCode;
import org.patronus.termmeta.GraphMeta;
import org.patronus.termmeta.PSVGResultsMeta;

/**
 *
 * @author dgrfi
 */
@Named(value = "importEdgeListFromVG")
@ViewScoped
public class ImportEdgeListFromVG implements Serializable {

    private String termSlug;
    private List<Map<String, Object>> screenTermInstanceList;
    private boolean metaDoesNotExistForTerm;
    private List<TermMetaKeyLabels> instanceMetaKeys;
    private String termName;
    private Map<String, Object> selectedVGGraph;
    private String graphTermSlug = PatronusConstants.TERM_SLUG_GRAPH;
    private String graphName;
    private String edgeLengthType;

    /**
     * Creates a new instance of ImportEdgeListFromVG
     */
    public ImportEdgeListFromVG() {
    }

    public void fillTermMetaData() {

        LeviosaClientService mts = new LeviosaClientService(CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServer(),CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServerPort());

        TermDTO termDTO = new TermDTO();
        termDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        termDTO.setTermSlug(termSlug);
        termDTO = mts.getTermDetails(termDTO);
        termName = (String) termDTO.getTermDetails().get(CMSConstants.TERM_NAME);

        //Creation of grid
        TermMetaDTO termMetaDTO = new TermMetaDTO();
        termMetaDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        termMetaDTO.setTermSlug(termSlug);
        termMetaDTO = mts.getTermMetaList(termMetaDTO);

        List<Map<String, Object>> termScreenFields = termMetaDTO.getTermMetaFields();

        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);

        //populate data from grid
        termInstanceDTO.setTermSlug(termSlug);
        termInstanceDTO = mts.getTermInstanceList(termInstanceDTO);
        List<Map<String, Object>> tempScreenTermInstanceList = termInstanceDTO.getTermInstanceList();
        screenTermInstanceList = new ArrayList<>();
        for (int i = 0; i < tempScreenTermInstanceList.size(); i++) {
            Map<String, Object> tempScreenTermInstance = tempScreenTermInstanceList.get(i);
            if (((String) tempScreenTermInstance.get(PSVGResultsMeta.QUEUED)).equals("No")) {
                screenTermInstanceList.add(tempScreenTermInstance);
            }
        }

        metaDoesNotExistForTerm = !termScreenFields.isEmpty();
        instanceMetaKeys = TermInstanceUtil.prepareMetaKeyList(termScreenFields);
        edgeLengthType = "real";

    }

    public String importVGIntoGraph() {
        FacesMessage message;
        if (selectedVGGraph == null) {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Paramenter required.", "Paramenter required.");
            FacesContext f = FacesContext.getCurrentInstance();
            f.getExternalContext().getFlash().setKeepMessages(true);
            f.addMessage(null, message);
            String redirectUrl = "VGGraphList";
            return redirectUrl;
        }
        String importFromVGSlug = (String) selectedVGGraph.get(CMSConstants.TERM_INSTANCE_SLUG);
        FractalDTO fractalDTO = new FractalDTO();
        fractalDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        
        Map<String, Object> graphTermInstance = new HashMap<>();
        graphTermInstance.put(GraphMeta.NAME, graphName);
        graphTermInstance.put("edgeLengthTypeForImport", edgeLengthType);
        graphTermInstance.put("importFromVGInstanceSlug", importFromVGSlug);
        fractalDTO.setFractalTermInstance(graphTermInstance);
        PatronusCoreClient fractalCoreClient = new PatronusCoreClient();
        fractalDTO = fractalCoreClient.importPSVGGraph(fractalDTO);
        if (fractalDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            message = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Something wrong.", "Contact Admin.");
            FacesContext f = FacesContext.getCurrentInstance();
            f.getExternalContext().getFlash().setKeepMessages(true);
            f.addMessage(null, message);
            String redirectUrl = "VGGraphList";
            return redirectUrl;
        } else {
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success","Graph Imported.");
            FacesContext f = FacesContext.getCurrentInstance();
            f.getExternalContext().getFlash().setKeepMessages(true);
            f.addMessage(null, message);
            String redirectUrl = "EdgeListList";
            return redirectUrl;
        }

    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public String getTermSlug() {
        return termSlug;
    }

    public void setTermSlug(String termSlug) {
        this.termSlug = termSlug;
    }

    public List<Map<String, Object>> getScreenTermInstanceList() {
        return screenTermInstanceList;
    }

    public void setScreenTermInstanceList(List<Map<String, Object>> screenTermInstanceList) {
        this.screenTermInstanceList = screenTermInstanceList;
    }

    public boolean isMetaDoesNotExistForTerm() {
        return metaDoesNotExistForTerm;
    }

    public void setMetaDoesNotExistForTerm(boolean metaDoesNotExistForTerm) {
        this.metaDoesNotExistForTerm = metaDoesNotExistForTerm;
    }

    public List<TermMetaKeyLabels> getInstanceMetaKeys() {
        return instanceMetaKeys;
    }

    public void setInstanceMetaKeys(List<TermMetaKeyLabels> instanceMetaKeys) {
        this.instanceMetaKeys = instanceMetaKeys;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public Map<String, Object> getSelectedVGGraph() {
        return selectedVGGraph;
    }

    public void setSelectedVGGraph(Map<String, Object> selectedVGGraph) {
        this.selectedVGGraph = selectedVGGraph;
    }

    public String getGraphTermSlug() {
        return graphTermSlug;
    }

    public void setGraphTermSlug(String graphTermSlug) {
        this.graphTermSlug = graphTermSlug;
    }

    public String getEdgeLengthType() {
        return edgeLengthType;
    }

    public void setEdgeLengthType(String edgeLengthType) {
        this.edgeLengthType = edgeLengthType;
    }

}
