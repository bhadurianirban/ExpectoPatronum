/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.ui.dataseries;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.patronus.response.FractalResponseCode;
import org.hedwig.cloud.response.HedwigResponseMessage;
import org.leviosa.core.driver.LeviosaClientService;
import org.hedwig.leviosa.constants.CMSConstants;
import org.hedwig.cms.dto.TermDTO;
import org.hedwig.cms.dto.TermMetaDTO;
import org.patronus.core.client.PatronusCoreClient;
import org.patronus.termmeta.DataSeriesMeta;
import org.patronus.core.dto.FractalDTO;
import org.patronus.cms.ui.login.CMSClientAuthCredentialValue;
import org.patronus.cms.ui.login.LoginController;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author dgrfv
 */
@Named(value = "dataSeriesAdd")
@ViewScoped
public class DataSeriesAdd implements Serializable {

    private String termSlug;
    private String termName;
    //private String tempFilePath;
    private boolean fileUploaded;
    private int fileUploadCount;

    List<Map<String, Object>> termScreenFields;
    List<Map<String, String>> fileNameAndTempPathList;
    Map<String, Object> screenTermInstance;
    @Inject
    LoginController loginController;

    /**
     * Creates a new instance of DataSeriesAdd
     */
    public DataSeriesAdd() {
        fileUploaded = false;
        fileUploadCount = 0;
        fileNameAndTempPathList = new ArrayList<>();
    }

    public void creteTermForm() {

        LeviosaClientService mts = new LeviosaClientService(CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServer(),CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServerPort());

        TermDTO termDTO = new TermDTO();
        termDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        termDTO.setTermSlug(termSlug);
        termDTO = mts.getTermDetails(termDTO);
        termName = (String) termDTO.getTermDetails().get(CMSConstants.TERM_NAME);

        TermMetaDTO termMetaDTO = new TermMetaDTO();
        termMetaDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        termMetaDTO.setTermSlug(termSlug);
        termMetaDTO = mts.getTermMetaList(termMetaDTO);
        termScreenFields = termMetaDTO.getTermMetaFields();

        PatronusCoreClient fractalCoreClient = new PatronusCoreClient();
        FractalDTO fractalDTO = new FractalDTO();
        fractalDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);

        fractalDTO = fractalCoreClient.createDataSeriesTermInstance(fractalDTO);
        screenTermInstance = fractalDTO.getFractalTermInstance();

    }

    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile uploadedFile = event.getFile();
        String fileName = uploadedFile.getFileName();
        String dataSeriesName = fileName.replaceAll(".csv", "");
        screenTermInstance.put(DataSeriesMeta.DATA_SERIES_NAME, dataSeriesName);
        screenTermInstance.put(DataSeriesMeta.DATA_SERIES_ORIGINAL_FILENAME, fileName);

        byte[] bytes = null;

        if (null != uploadedFile) {
            bytes = uploadedFile.getContents();
            String tempPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");

            String tempFilePath = tempPath + "temp" + CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getUserId() + fileNameAndTempPathList.size() + fileName;
            System.out.println(tempFilePath);

            FileOutputStream fo;

            try {
                File tempFile = new File(tempFilePath);
                fo = new FileOutputStream(tempFile);
                try (BufferedOutputStream stream = new BufferedOutputStream(fo)) {
                    stream.write(bytes);
                    stream.close();
                    Map<String, String> fileNameAndTempPath = new HashMap<>();
                    fileNameAndTempPath.put("filename", fileName);
                    fileNameAndTempPath.put("tempFilePath", tempFilePath);
                    fileNameAndTempPathList.add(fileNameAndTempPath);
                    fileUploadCount++;
                    fileUploaded = true;

                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(DataSeriesAdd.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DataSeriesAdd.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public String addTermInstance() {

        PatronusCoreClient dss = new PatronusCoreClient();
        FacesMessage message;

        if (!fileUploaded) {
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "File is mandatory.", "File is mandatory.");
            FacesContext f = FacesContext.getCurrentInstance();
            f.getExternalContext().getFlash().setKeepMessages(true);
            f.addMessage(null, message);
            String redirectUrl = "DataSeriesAdd";
            return redirectUrl;
        }
        fileNameAndTempPathList.stream().forEach(m -> {
            System.out.println(m.get("filename"));
            System.out.println(m.get("tempFilePath"));
        });
        for (Map<String, String> fileNameAndTempPath : fileNameAndTempPathList) {
            PatronusCoreClient fractalCoreClient = new PatronusCoreClient();
            FractalDTO fractalDTO = new FractalDTO();
            fractalDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
            fractalDTO = fractalCoreClient.createDataSeriesTermInstance(fractalDTO);
            Map<String, Object> dataSeriesTermInstance = fractalDTO.getFractalTermInstance();
            String fileName = fileNameAndTempPath.get("filename");
            String dataSeriesName = fileName.replaceAll(".csv", "");
            String tempFilePath = fileNameAndTempPath.get("tempFilePath");
            dataSeriesTermInstance.put(DataSeriesMeta.DATA_SERIES_NAME, dataSeriesName);
            dataSeriesTermInstance.put(DataSeriesMeta.DATA_SERIES_ORIGINAL_FILENAME, fileName);
            fractalDTO.setCsvFilePath(tempFilePath);
            fractalDTO.setFractalTermInstance(dataSeriesTermInstance);
            dataSeriesTermInstance.entrySet().stream().forEach(m->System.out.println(m.getKey()+" "+(String)m.getValue()));
            fractalDTO = dss.uploadDataSeries(fractalDTO);
            if (fractalDTO.getResponseCode() == FractalResponseCode.SUCCESS) {
                HedwigResponseMessage responseMessage = new HedwigResponseMessage();
                message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", responseMessage.getResponseMessage(fractalDTO.getResponseCode()));
                FacesContext f = FacesContext.getCurrentInstance();
                f.getExternalContext().getFlash().setKeepMessages(true);
                f.addMessage(null, message);
                //String redirectUrl = "/DataSeries/DataSeriesList?faces-redirect=true&termslug=" + termSlug;
                //return "DataSeriesList";
            } else {
                HedwigResponseMessage responseMessage = new HedwigResponseMessage();
                message = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Data Error", responseMessage.getResponseMessage(fractalDTO.getResponseCode()));
                FacesContext f = FacesContext.getCurrentInstance();
                f.getExternalContext().getFlash().setKeepMessages(true);
                f.addMessage(null, message);
                String redirectUrl = "DataSeriesAdd";
                return redirectUrl;
            }
        }
        return "DataSeriesList";
//        FractalDTO fractalDTO = new FractalDTO();
//        fractalDTO.setAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
//        fractalDTO.setCsvFilePath(tempFilePath);
//        fractalDTO.setFractalTermInstance(screenTermInstance);
//        fractalDTO = dss.uploadDataSeries(fractalDTO);
//        if (fractalDTO.getResponseCode() == FractalResponseCode.SUCCESS) {
//            HedwigResponseMessage responseMessage = new HedwigResponseMessage();
//            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", responseMessage.getResponseMessage(fractalDTO.getResponseCode()));
//            FacesContext f = FacesContext.getCurrentInstance();
//            f.getExternalContext().getFlash().setKeepMessages(true);
//            f.addMessage(null, message);
//            //String redirectUrl = "/DataSeries/DataSeriesList?faces-redirect=true&termslug=" + termSlug;
//            return "DataSeriesList";
//        } else {
//            HedwigResponseMessage responseMessage = new HedwigResponseMessage();
//            message = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Data Error", responseMessage.getResponseMessage(fractalDTO.getResponseCode()));
//            FacesContext f = FacesContext.getCurrentInstance();
//            f.getExternalContext().getFlash().setKeepMessages(true);
//            f.addMessage(null, message);
//            String redirectUrl = "DataSeriesAdd";
//            return redirectUrl;
//        }

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

    public List<Map<String, Object>> getTermScreenFields() {
        return termScreenFields;
    }

    public void setTermScreenFields(List<Map<String, Object>> termScreenFields) {
        this.termScreenFields = termScreenFields;
    }

    public Map<String, Object> getScreenTermInstance() {
        return screenTermInstance;
    }

    public void setScreenTermInstance(Map<String, Object> screenTermInstance) {
        this.screenTermInstance = screenTermInstance;
    }

}
