/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.ui.MFDXA;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import org.patronus.response.FractalResponseCode;
import org.leviosa.core.driver.LeviosaClientService;
import org.hedwig.leviosa.constants.CMSConstants;
import org.hedwig.cms.dto.TermDTO;
import org.hedwig.cms.dto.TermInstanceDTO;
import org.hedwig.cms.dto.TermMetaDTO;
import org.patronus.core.client.PatronusCoreClient;
import org.patronus.termmeta.DataSeriesMeta;
import org.patronus.constants.PatronusConstants;
import org.patronus.core.dto.FractalDTO;
import org.patronus.termmeta.MFDXAResultsMeta;
import org.patronus.cms.ui.login.CMSClientAuthCredentialValue;

/**
 *
 * @author dgrfv
 */
@Named(value = "mfdxaCalc")
@ViewScoped
public class MfdxaCalc implements Serializable {

    private String termSlug;
    private String termName;
    private List<Map<String, Object>> mfdfaParamDataList;
    private Map<String, Object> selectedmfdfaParamData;
    private List<Map<String, Object>> dataSeriesList;
    private Map<String, Object> selectedFirstDataSeries;
    private Map<String, Object> selectedSecondDataSeries;
    private Map<String, String> mfdfaParamFieldsLabel;
    private Map<String, String> dataSeriesFieldsLabel;
    private Map<String, Object> screenTermInstance;

    /**
     * Creates a new instance of MfdxaCalc
     */
    public MfdxaCalc() {
    }

    public void creteTermForm() {
        LeviosaClientService mts = new LeviosaClientService(CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServer(),CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServerPort());
        TermDTO termDTO = new TermDTO();
        termDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        termDTO.setTermSlug(termSlug);
        termDTO = mts.getTermDetails(termDTO);
        termName = (String) termDTO.getTermDetails().get(CMSConstants.TERM_NAME);

        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);

        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_MFDFA_PARAM);
        termInstanceDTO = mts.getTermInstanceList(termInstanceDTO);
        mfdfaParamDataList = termInstanceDTO.getTermInstanceList();
        //creation of grid
        //get mfdfa param field labels. Mfdxa and mfdfa params are same
        TermMetaDTO termMetaDTO = new TermMetaDTO();
        termMetaDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        termMetaDTO.setTermSlug(PatronusConstants.TERM_SLUG_MFDFA_PARAM);
        termMetaDTO = mts.getTermMetaList(termMetaDTO);
        mfdfaParamFieldsLabel = termMetaDTO.getTermMetaFieldLabels();
        //get dataseries field labels
        termMetaDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        termMetaDTO.setTermSlug(PatronusConstants.TERM_SLUG_DATASERIES);
        termMetaDTO = mts.getTermMetaList(termMetaDTO);
        dataSeriesFieldsLabel = termMetaDTO.getTermMetaFieldLabels();

        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_DATASERIES);
        termInstanceDTO = mts.getTermInstanceList(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() == FractalResponseCode.SUCCESS) {
            List<Map<String, Object>> dataSeriesListAll = termInstanceDTO.getTermInstanceList();
            dataSeriesList = dataSeriesListAll.stream().filter(ds -> ds.get(DataSeriesMeta.DATA_SERIES_TYPE).equals(DataSeriesMeta.DATA_SERIES_UNIFORM)).collect(Collectors.toList());
        }
    }

    public String startCalc() {

        FacesMessage message;
        if (selectedmfdfaParamData == null) {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Paramenter required.", "Paramenter required.");
            FacesContext f = FacesContext.getCurrentInstance();
            f.getExternalContext().getFlash().setKeepMessages(true);
            f.addMessage(null, message);
            String redirectUrl = "/MFDXA/MFDXACalc?faces-redirect=true&termslug=" + termSlug;
            return redirectUrl;
        }
        if (selectedFirstDataSeries == null) {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Data required.", "Data required.");
            FacesContext f = FacesContext.getCurrentInstance();
            f.getExternalContext().getFlash().setKeepMessages(true);
            f.addMessage(null, message);
            String redirectUrl = "MFDXACalc";
            return redirectUrl;
        }

        if (selectedSecondDataSeries == null) {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Data required.", "Data required.");
            FacesContext f = FacesContext.getCurrentInstance();
            f.getExternalContext().getFlash().setKeepMessages(true);
            f.addMessage(null, message);
            String redirectUrl = "MFDXACalc";
            return redirectUrl;
        }
        String mfdfaParamSlug = (String) selectedmfdfaParamData.get("termInstanceSlug");
        String firstDataSeriesSlug = (String) selectedFirstDataSeries.get("termInstanceSlug");
        String secondDataSeriesSlug = (String) selectedSecondDataSeries.get("termInstanceSlug");

        //populate PSVG results instance
        Long dataCountFirst = Long.parseLong((String) selectedFirstDataSeries.get(DataSeriesMeta.DATA_SERIES_LENGTH));
        Long dataCountSecond = Long.parseLong((String) selectedSecondDataSeries.get(DataSeriesMeta.DATA_SERIES_LENGTH));

        Long dataCount;
        if (dataCountFirst < dataCountSecond) {
            dataCount = dataCountFirst;
        } else {
            dataCount = dataCountSecond;
        }
        PatronusCoreClient fractalCoreClient = new PatronusCoreClient();
        FractalDTO fractalDTO = new FractalDTO();
        fractalDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        fractalDTO.setParamSlug(mfdfaParamSlug);
        fractalDTO.setDataSeriesSlug(firstDataSeriesSlug);
        fractalDTO.setDataSeriesSlugSecond(secondDataSeriesSlug);

        if (dataCount < PatronusConstants.DATA_LIMIT) {
            fractalDTO = fractalCoreClient.calculateMFDXA(fractalDTO);
            screenTermInstance = fractalDTO.getFractalTermInstance();
            if (screenTermInstance == null) {
                message = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Something wrong.", "Contact Admin.");
                FacesContext f = FacesContext.getCurrentInstance();
                f.getExternalContext().getFlash().setKeepMessages(true);
                f.addMessage(null, message);
                String redirectUrl = "MFDXACalc";
                return redirectUrl;
            }
        } else {
            fractalDTO = fractalCoreClient.queueMFDXACalculation(fractalDTO);
            screenTermInstance = fractalDTO.getFractalTermInstance();
            if (screenTermInstance == null) {
                message = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Something wrong.", "Contact Admin.");
                FacesContext f = FacesContext.getCurrentInstance();
                f.getExternalContext().getFlash().setKeepMessages(true);
                f.addMessage(null, message);
                String redirectUrl = "MFDXACalc";
                return redirectUrl;
            }
        }

        String queued = (String) screenTermInstance.get("queued");
        if (queued.equals("Yes")) {
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Data length is more than " + PatronusConstants.DATA_LIMIT + ". Your data is queued for processing check after some time.");
        } else {
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "GammaX = " + screenTermInstance.get(MFDXAResultsMeta.GAMMA_X));
        }

        FacesContext f = FacesContext.getCurrentInstance();
        f.getExternalContext().getFlash().setKeepMessages(true);
        f.addMessage(null, message);
        String redirectUrl = "MFDXACalcList";
        return redirectUrl;

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

    public List<Map<String, Object>> getMfdfaParamDataList() {
        return mfdfaParamDataList;
    }

    public void setMfdfaParamDataList(List<Map<String, Object>> mfdfaParamDataList) {
        this.mfdfaParamDataList = mfdfaParamDataList;
    }

    public Map<String, Object> getSelectedmfdfaParamData() {
        return selectedmfdfaParamData;
    }

    public void setSelectedmfdfaParamData(Map<String, Object> selectedmfdfaParamData) {
        this.selectedmfdfaParamData = selectedmfdfaParamData;
    }

    public List<Map<String, Object>> getDataSeriesList() {
        return dataSeriesList;
    }

    public void setDataSeriesList(List<Map<String, Object>> dataSeriesList) {
        this.dataSeriesList = dataSeriesList;
    }

    public Map<String, Object> getSelectedFirstDataSeries() {
        return selectedFirstDataSeries;
    }

    public void setSelectedFirstDataSeries(Map<String, Object> selectedFirstDataSeries) {
        this.selectedFirstDataSeries = selectedFirstDataSeries;
    }

    public Map<String, Object> getSelectedSecondDataSeries() {
        return selectedSecondDataSeries;
    }

    public void setSelectedSecondDataSeries(Map<String, Object> selectedSecondDataSeries) {
        this.selectedSecondDataSeries = selectedSecondDataSeries;
    }

    public Map<String, Object> getScreenTermInstance() {
        return screenTermInstance;
    }

    public void setScreenTermInstance(Map<String, Object> screenTermInstance) {
        this.screenTermInstance = screenTermInstance;
    }

    public Map<String, String> getMfdfaParamFieldsLabel() {
        return mfdfaParamFieldsLabel;
    }

    public void setMfdfaParamFieldsLabel(Map<String, String> mfdfaParamFieldsLabel) {
        this.mfdfaParamFieldsLabel = mfdfaParamFieldsLabel;
    }

    public Map<String, String> getDataSeriesFieldsLabel() {
        return dataSeriesFieldsLabel;
    }

    public void setDataSeriesFieldsLabel(Map<String, String> dataSeriesFieldsLabel) {
        this.dataSeriesFieldsLabel = dataSeriesFieldsLabel;
    }

}
