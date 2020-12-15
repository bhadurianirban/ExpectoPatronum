/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.ui.IPSVG;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import org.hedwig.leviosa.constants.CMSConstants;
import org.leviosa.core.driver.LeviosaClientService;

import org.hedwig.cms.dto.TermDTO;
import org.hedwig.cms.dto.TermInstanceDTO;
import org.patronus.core.client.PatronusCoreClient;
import org.patronus.core.dto.FractalDTO;
import org.patronus.core.dto.IpsvgResultsDTO;
import org.patronus.cms.ui.login.CMSClientAuthCredentialValue;

/**
 *
 * @author bhaduri
 */
@Named(value = "iPSVGResultDetails")
@ViewScoped
public class IPSVGResultDetails implements Serializable {

    private String termSlug;
    private String termName;
    private String termInstanceSlug;
    private Map<String, Object> ipsvgResultInstance;
    private List<IpsvgResultsDTO> IPSVGResultsList;

    /**
     * Creates a new instance of IPSVGResultDetails
     */
    public IPSVGResultDetails() {
    }
    public void getIPsvgResultData () {
        LeviosaClientService mts = new LeviosaClientService(CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServer(),CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServerPort());

        TermDTO termDTO = new TermDTO();
        termDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        termDTO.setTermSlug(termSlug);
        termDTO = mts.getTermDetails(termDTO);

        termName = (String) termDTO.getTermDetails().get(CMSConstants.TERM_NAME);
        
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
termInstanceDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        
        termInstanceDTO.setTermSlug(termSlug);
        termInstanceDTO.setTermInstanceSlug(termInstanceSlug);
        
        termInstanceDTO = mts.getTermInstance(termInstanceDTO);
                
        ipsvgResultInstance = termInstanceDTO.getTermInstance();
        
        FractalDTO fractalDTO = new FractalDTO();
        fractalDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        fractalDTO.setFractalTermInstance(ipsvgResultInstance);
        PatronusCoreClient fractalCoreClient = new PatronusCoreClient();
        fractalDTO = fractalCoreClient.getIpsvgResults(fractalDTO);
        IPSVGResultsList = fractalDTO.getIpsvgResultsDTOs();
    }

    public String getTermSlug() {
        return termSlug;
    }

    public void setTermSlug(String termSlug) {
        this.termSlug = termSlug;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public String getTermInstanceSlug() {
        return termInstanceSlug;
    }

    public void setTermInstanceSlug(String termInstanceSlug) {
        this.termInstanceSlug = termInstanceSlug;
    }

    public Map<String, Object> getIpsvgResultInstance() {
        return ipsvgResultInstance;
    }

    public void setIpsvgResultInstance(Map<String, Object> ipsvgResultInstance) {
        this.ipsvgResultInstance = ipsvgResultInstance;
    }

    public List<IpsvgResultsDTO> getIPSVGResultsList() {
        return IPSVGResultsList;
    }

    public void setIPSVGResultsList(List<IpsvgResultsDTO> IPSVGResultsList) {
        this.IPSVGResultsList = IPSVGResultsList;
    }
    
}
