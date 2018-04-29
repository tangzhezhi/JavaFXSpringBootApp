/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tang.face.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.tang.face.bean.User;
import org.tang.face.config.StageManager;
import org.tang.face.service.UserService;
import org.tang.face.view.FxmlView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author danml
 */
@Component
public class LoginController implements Initializable {

    @Lazy
    @Autowired
    private StageManager stageManager;

    @Autowired
    private UserService userService;

    @FXML
    private JFXButton btnLogin;
    @FXML
    private JFXSpinner loggingProgress;
    @FXML
    private JFXTextField txtUsername;
    @FXML
    private JFXPasswordField txtPassword;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loggingProgress.setVisible(false);
    }

    @FXML
    private void loginAction(ActionEvent event) throws IOException {
        loggingProgress.setVisible(true);
        PauseTransition pauseTransition = new PauseTransition();
        pauseTransition.setDuration(Duration.seconds(1));
        pauseTransition.setOnFinished(ev -> {
            System.out.println("Complte one");
            completeLogin();
            System.out.println("Complte two");
        });
        pauseTransition.play();

    }

    private boolean isValidInput() {
        return (txtUsername.getText().trim().length() > 0) && (txtPassword.getText().trim().length() > 0);
    }

    private void completeLogin() {
        loggingProgress.setVisible(false);
        if (isValidInput()) {
            User user=new User();
            user.setUserName(txtUsername.getText());

            if(userService.authenticate(txtUsername.getText(), txtPassword.getText())){

                stageManager.switchScene(FxmlView.FACE_MAIN);

            }else{
                btnLogin.setText("登录失败.");
            }


//                Stage stage = new Stage();
//                Parent root = FXMLLoader.load(getClass().getResource(Routes.MAINVIEW));
//                JFXDecorator decorator = new JFXDecorator(stage, root, false, false, true);
//                decorator.setCustomMaximize(false);
//                decorator.setBorder(Border.EMPTY);
//
//                Scene scene = new Scene(decorator);
//                scene.getStylesheets().add(Main.class.getResource("/styles/face-styles.css").toExternalForm());
//                stage.initStyle(StageStyle.UNDECORATED);
//                stage.setScene(scene);
//
//                stage.setIconified(false);
//                stage.show();
//                //Hide login window
//                btnLogin.getScene().getWindow().hide();

        }
    }

}
