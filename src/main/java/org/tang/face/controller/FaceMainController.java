package org.tang.face.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.sql.SQLException;

/**
 * Created by Administrator on 4/18/2018.
 */

@Controller
public class FaceMainController {

    @FXML
    private Button startCam;
    @FXML
    private Button stopBtn;

    @FXML
    private Button recogniseBtn;

    @FXML
    private Button saveBtn;


    @FXML
    protected void startCamera()  {
        startCam.setVisible(false);
        stopBtn.setVisible(true);
        saveBtn.setDisable(false);
        recogniseBtn.setDisable(false);
    }

    @FXML
    protected void saveFace()  {
        startCam.setVisible(false);
        stopBtn.setVisible(true);
        saveBtn.setDisable(false);
        recogniseBtn.setDisable(false);
    }

    @FXML
    protected void faceRecognise()  {
        startCam.setVisible(false);
        stopBtn.setVisible(true);
        saveBtn.setDisable(false);
        recogniseBtn.setDisable(false);
    }

    @FXML
    protected void stopCam()  {
        startCam.setVisible(false);
        stopBtn.setVisible(true);
        saveBtn.setDisable(false);
        recogniseBtn.setDisable(false);
    }


}
