/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.ui.PSVG;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import org.patronus.response.FractalResponseCode;
import org.hedwig.cloud.response.HedwigResponseMessage;
import org.leviosa.core.driver.LeviosaClientService;
import org.hedwig.leviosa.constants.CMSConstants;
import org.hedwig.cms.dto.TermDTO;
import org.hedwig.cms.dto.TermInstanceDTO;
import org.hedwig.cms.dto.TermMetaDTO;
import org.patronus.core.client.PatronusCoreClient;
import org.patronus.constants.PatronusConstants;
import org.patronus.core.dto.FractalDTO;
import org.patronus.cms.ui.login.CMSClientAuthCredentialValue;
import org.patronus.cms.ui.terminstance.TermInstanceUtil;
import org.patronus.cms.ui.terminstance.TermMetaKeyLabels;

/**
 *
 * @author dgrfv
 */
@Named(value = "psvgList")
@ViewScoped
public class PsvgList implements Serializable {

    private String termSlug;
    private List<Map<String, Object>> screenTermInstanceList;
    private boolean metaDoesNotExistForTerm;
    private List<TermMetaKeyLabels> instanceMetaKeys;
    private String termName;
    private Map<String, Object> selectedMetaData;
    private List<Map<String, Object>> deletePSVGList;
    private String calcHorizontal;
    private String calcXY;
    private String calcNormal;

    /**
     * Creates a new instance of PsvgList
     */
    public PsvgList() {
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

        termInstanceDTO.setTermSlug(termSlug);
        termInstanceDTO = mts.getTermInstanceList(termInstanceDTO);

        screenTermInstanceList = termInstanceDTO.getTermInstanceList();

        metaDoesNotExistForTerm = !termScreenFields.isEmpty();
        instanceMetaKeys = TermInstanceUtil.prepareMetaKeyList(termScreenFields);

        calcNormal = PatronusConstants.PSVG_CALC_TYPE_N;
        calcHorizontal = PatronusConstants.PSVG_CALC_TYPE_H;
        calcXY = PatronusConstants.PSVG_CALC_TYPE_XY;
    }

    public String goToCalcPSVG() {
        String redirectUrl = "/PSVG/PSVGCalc?faces-redirect=true&termslug=" + termSlug + "&calctype=" + PatronusConstants.PSVG_CALC_TYPE_N;
        return redirectUrl;
    }

    public String goToCalcPSVGXY() {
        String redirectUrl = "/PSVG/PSVGCalc?faces-redirect=true&termslug=" + termSlug + "&calctype=" + PatronusConstants.PSVG_CALC_TYPE_XY;
        return redirectUrl;
    }

    public String goToCalcPSVGHorizontal() {
        String redirectUrl = "/PSVG/PSVGCalc?faces-redirect=true&termslug=" + termSlug + "&calctype=" + PatronusConstants.PSVG_CALC_TYPE_H;
        return redirectUrl;
    }

    public String goToViewPSVGResult() {

        //String selectedTermInstanceSlug = (String) termInstance.get("termInstanceSlug");
        return "PSVGResult";
    }

    public String deletePSVGResults() {
        FacesMessage message;
        HedwigResponseMessage responseMessage = new HedwigResponseMessage();
        //String selectedTermInstanceSlug = (String) selectedMetaData.get("termInstanceSlug");
        PatronusCoreClient fractalCoreClient = new PatronusCoreClient();
        FractalDTO fractalDTO = new FractalDTO();
        fractalDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        for (Map<String, Object> deletePSVG : deletePSVGList) {
            fractalDTO.setFractalTermInstance(deletePSVG);
            fractalDTO = fractalCoreClient.deletePSVGResults(fractalDTO);
            if (fractalDTO.getResponseCode() == FractalResponseCode.SUCCESS) {
                message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", responseMessage.getResponseMessage(fractalDTO.getResponseCode()));
                FacesContext f = FacesContext.getCurrentInstance();
                f.getExternalContext().getFlash().setKeepMessages(true);
                f.addMessage(null, message);
            } else {
                message = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", responseMessage.getResponseMessage(fractalDTO.getResponseCode()));
                FacesContext f = FacesContext.getCurrentInstance();
                f.getExternalContext().getFlash().setKeepMessages(true);
                f.addMessage(null, message);
            }
        }
        return "PSVGCalcList";
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

    public Map<String, Object> getSelectedMetaData() {
        return selectedMetaData;
    }

    public void setSelectedMetaData(Map<String, Object> selectedMetaData) {
        this.selectedMetaData = selectedMetaData;
    }

    public String getCalcHorizontal() {
        return calcHorizontal;
    }

    public String getCalcXY() {
        return calcXY;
    }

    public String getCalcNormal() {
        return calcNormal;
    }

    public List<Map<String, Object>> getDeletePSVGList() {
        return deletePSVGList;
    }

    public void setDeletePSVGList(List<Map<String, Object>> deletePSVGList) {
        this.deletePSVGList = deletePSVGList;
    }

}
