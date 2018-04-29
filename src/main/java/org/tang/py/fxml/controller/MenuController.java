package org.tang.py.fxml.controller;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable{

    private static Stage primaryStage;


    String pythonInterpreter, trainingSetDirectory;

    @FXML
    TextField txtTrSet;
    @FXML
    TextField txtPython;
    @FXML
    BorderPane borderPane;

    @FXML
    public void onChangeButton(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Python", "*.exe"));
        fileChooser.setTitle("choose Python interpreter");
        try {
            this.pythonInterpreter = fileChooser.showOpenDialog(primaryStage).getPath();
            txtPython.setText(this.pythonInterpreter);
        }
        catch (NullPointerException ex){}
    }


    @FXML
    public void onButtonStartRec()throws IOException{
        primaryStage = new Stage();
        primaryStage.setTitle("Face Recognition");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gui/WebCam.fxml"));
        Pane pane = (Pane) loader.load();
//        WebCamController wcc = (WebCamController) loader.getController();
//        wcc.setPrevStage(primaryStage);
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        Stage st = (Stage)borderPane.getScene().getWindow();
        st.close();
        primaryStage.show();
    }

    @FXML
    public void onExitButton(){
        System.exit(0);
    }

    @FXML
    public void onTrainButton() throws Exception{
        String cmd = "python " + "\"" + "E:/work/repo0/JavaFXSpringBootApp/scripts/fitting.py" + "\"";
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Train set");
        alert.setHeaderText("set trained");
        alert.showAndWait();
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1){
        txtTrSet.setText("E:\\work\\repo0\\JavaFXSpringBootApp\\scripts\\data");
    }
}
