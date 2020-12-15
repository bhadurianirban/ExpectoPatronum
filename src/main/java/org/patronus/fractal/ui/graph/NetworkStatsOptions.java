/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.ui.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import org.hedwig.leviosa.constants.CMSConstants;
import org.leviosa.core.driver.LeviosaClientService;
import org.hedwig.cms.dto.TermInstanceDTO;
import org.patronus.cms.ui.login.CMSClientAuthCredentialValue;
import org.patronus.constants.PatronusConstants;
import org.patronus.core.client.PatronusCoreClient;
import org.patronus.core.dto.FractalDTO;
import org.patronus.response.FractalResponseCode;
import org.patronus.termmeta.GraphMeta;
import org.patronus.termmeta.NetworkStatsMeta;

/**
 *
 * @author dgrfi
 */
@Named(value = "networkStatsOptions")
@ViewScoped
public class NetworkStatsOptions implements Serializable {

    /**
     * Creates a new instance of NetworkStatsOptions
     */
    private String termSlug;
    private String termInstanceSlug;
    private String selectedNetworkStatsOption;
    private String graphName;
    private String networtStatsTermInstanceSlug;
    private String networtStatsTermSlug;

    public NetworkStatsOptions() {
    }

    public void fillForm() {
        termSlug = PatronusConstants.TERM_SLUG_GRAPH;
        LeviosaClientService mts = new LeviosaClientService(CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServer(),CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServerPort());
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        termInstanceDTO.setTermSlug(termSlug);
        termInstanceDTO.setTermInstanceSlug(termInstanceSlug);
        termInstanceDTO = mts.getTermInstance(termInstanceDTO);
        Map<String,Object>  graphTermInstance = termInstanceDTO.getTermInstance();
        graphName = (String) graphTermInstance.get(GraphMeta.NAME);
    }
    
    public String calculateNetworkStats() {
        FractalDTO fractalDTO = new FractalDTO();
        networtStatsTermSlug = PatronusConstants.TERM_SLUG_NETWORK_STATS;
        Map<String,Object> networkStatsTermInstance = new HashMap<>();
        networkStatsTermInstance.put(CMSConstants.TERM_SLUG, networtStatsTermSlug);
        networkStatsTermInstance.put(CMSConstants.TERM_INSTANCE_SLUG, "Undecided");
        networkStatsTermInstance.put(NetworkStatsMeta.GRAPH, termInstanceSlug);
        networkStatsTermInstance.put("calctype", selectedNetworkStatsOption);
        fractalDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        fractalDTO.setFractalTermInstance(networkStatsTermInstance);
        PatronusCoreClient fcc = new PatronusCoreClient();
        fractalDTO = fcc.calculateNetworkStats(fractalDTO);
        //networkStatsTermInstance = fractalDTO.getFractalTermInstance();
        //networtStatsTermInstanceSlug = (String) networkStatsTermInstance.get(CMSConstants.TERM_INSTANCE_SLUG);
        
        
        FacesMessage message;
        if (fractalDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            message = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Something wrong.", "Contact Admin.");
            FacesContext f = FacesContext.getCurrentInstance();
            f.getExternalContext().getFlash().setKeepMessages(true);
            f.addMessage(null, message);
            String redirectUrl = "NetworkStatsOptions";
            return redirectUrl;
        } else {
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success","Calculation Done.");
            FacesContext f = FacesContext.getCurrentInstance();
            f.getExternalContext().getFlash().setKeepMessages(true);
            f.addMessage(null, message);
            String redirectUrl = "NetworkStatsList";
            return redirectUrl;
        }
    }
    
    public String getTermInstanceSlug() {
        return termInstanceSlug;
    }

    public void setTermInstanceSlug(String termInstanceSlug) {
        this.termInstanceSlug = termInstanceSlug;
    }

    public String getSelectedNetworkStatsOption() {
        return selectedNetworkStatsOption;
    }

    public void setSelectedNetworkStatsOption(String selectedNetworkStatsOption) {
        this.selectedNetworkStatsOption = selectedNetworkStatsOption;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public String getNetwortStatsTermInstanceSlug() {
        return networtStatsTermInstanceSlug;
    }

    public void setNetwortStatsTermInstanceSlug(String networtStatsTermInstanceSlug) {
        this.networtStatsTermInstanceSlug = networtStatsTermInstanceSlug;
    }

    public String getNetwortStatsTermSlug() {
        return networtStatsTermSlug;
    }

    public void setNetwortStatsTermSlug(String networtStatsTermSlug) {
        this.networtStatsTermSlug = networtStatsTermSlug;
    }

    public String getTermSlug() {
        return termSlug;
    }

    public void setTermSlug(String termSlug) {
        this.termSlug = termSlug;
    }

}
