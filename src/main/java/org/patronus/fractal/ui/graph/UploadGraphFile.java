/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.ui.graph;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.hedwig.cloud.response.HedwigResponseMessage;
import org.hedwig.leviosa.constants.CMSConstants;
import org.patronus.cms.ui.login.CMSClientAuthCredentialValue;
import org.patronus.cms.ui.login.LoginController;
import org.patronus.constants.PatronusConstants;
import org.patronus.core.client.PatronusCoreClient;
import org.patronus.core.dto.FractalDTO;
import org.patronus.response.FractalResponseCode;
import org.patronus.termmeta.GraphMeta;

import org.patronus.fractal.ui.dataseries.DataSeriesAdd;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author bhaduri
 */
@Named(value = "uploadGraphFile")
@ViewScoped
public class UploadGraphFile implements Serializable{
    private String tempFilePath;
    private boolean fileUploaded;
    private String graphTermSlug = PatronusConstants.TERM_SLUG_GRAPH;
    private String graphName;
    /**
     * Creates a new instance of UploadGraphFile
     */
    @Inject
    LoginController loginController;
    public UploadGraphFile() {
    }
    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile uploadedFile = event.getFile();
        String fileName  = uploadedFile.getFileName();
        
        byte[] bytes = null;

        if (null != uploadedFile) {
            bytes = uploadedFile.getContent();
            String tempPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
            String userid = loginController.getUserID();
            tempFilePath = tempPath + "temp"+userid+".csv";
            
            FileOutputStream fo;

            try {
                File tempFile = new File(tempFilePath);
                fo = new FileOutputStream(tempFile);
                try (BufferedOutputStream stream = new BufferedOutputStream(fo)) {
                    stream.write(bytes);
                    stream.close();

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
            String redirectUrl =  "UploadGraphFile";
            return redirectUrl;
        }
        
        FractalDTO fractalDTO = new FractalDTO();
        fractalDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        fractalDTO.setCsvFilePath(tempFilePath);
        Map<String, Object> graphTermInstance = new HashMap<>();
        graphTermInstance.put(GraphMeta.NAME, graphName);
        graphTermInstance.put(CMSConstants.TERM_SLUG, PatronusConstants.TERM_SLUG_GRAPH);
        fractalDTO.setFractalTermInstance(graphTermInstance);
        
        fractalDTO = dss.uploadGraph(fractalDTO);
        if (fractalDTO.getResponseCode() == FractalResponseCode.SUCCESS) {
            HedwigResponseMessage responseMessage = new HedwigResponseMessage();
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", responseMessage.getResponseMessage(fractalDTO.getResponseCode()));
            FacesContext f = FacesContext.getCurrentInstance();
            f.getExternalContext().getFlash().setKeepMessages(true);
            f.addMessage(null, message);
            //String redirectUrl = "/DataSeries/DataSeriesList?faces-redirect=true&termslug=" + termSlug;
            return "EdgeListList";
        } else {
            HedwigResponseMessage responseMessage = new HedwigResponseMessage();
            message = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Data Error", responseMessage.getResponseMessage(fractalDTO.getResponseCode()));
            FacesContext f = FacesContext.getCurrentInstance();
            f.getExternalContext().getFlash().setKeepMessages(true);
            f.addMessage(null, message);
            String redirectUrl = "UploadGraphFile";
            return redirectUrl;
        }


    }

    public String getGraphTermSlug() {
        return graphTermSlug;
    }

    public void setGraphTermSlug(String graphTermSlug) {
        this.graphTermSlug = graphTermSlug;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }
    
}
