package org.tang.py.fxml.controller;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.awt.Dimension;

import java.io.*;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.WebcamUtils;
import com.github.sarxos.webcam.util.ImageUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import com.github.sarxos.webcam.Webcam;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.*;
import javax.swing.*;

public class WebCamController implements Initializable{

	@FXML BorderPane bp;
	@FXML
	javafx.scene.control.Label predictionPerson;
	@FXML Button btnCancel;
	@FXML Button btnRecognize;
	@FXML ComboBox<WebCamInfo> cbCameraOptions;
	@FXML BorderPane bpWebCamPaneHolder;
	@FXML FlowPane fpBottomPane;
	@FXML ImageView imgWebCamCapturedImage;
	String imgFilename;
	private BufferedImage grabbedImage;
	private Webcam selWebCam = null;
	private boolean stopCamera = false;
	private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();
	private ObjectProperty<Image> ip = new SimpleObjectProperty<>();
	private String cameraListPromptText = "Choose Camera";
	Stage prevStage;

	@FXML
	public void onCancelButton() throws IOException{

		if (selWebCam != null) {
			this.stopCamera = true;
			this.selWebCam.close();
			Webcam.shutdown();
		}
		Stage stage = new Stage();
		stage.setTitle("Face Recognition");
		Pane myPane = FXMLLoader.load(getClass().getResource("/fxml/gui/Menu.fxml"));
		Scene scene = new Scene(myPane);
		stage.setScene(scene);
		prevStage.close();
		stage.show();
	}

	@FXML
	public void onLoadPhotoButton() throws IOException, InterruptedException{
		predictionPerson.setText("");
		cbCameraOptions.setValue(null);
		if (selWebCam != null) {
			this.stopCamera = true;
			this.selWebCam.close();
			Webcam.shutdown();
		}
		imgWebCamCapturedImage.imageProperty().bind(ip);
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image", "*.jpg"));
			fileChooser.setTitle("Choose photo");
			imgFilename = fileChooser.showOpenDialog(prevStage).getAbsolutePath();
		}
		catch (NullPointerException ex){}

		BufferedImage bi = ImageIO.read(new File(imgFilename));

		Image nbi = SwingFXUtils.toFXImage(bi, null);
		try {

			//bpWebCamPaneHolder.getChildren().remove(imgWebCamCapturedImage);
			//this.imgWebCamCapturedImage = new ImageView();
			//bpWebCamPaneHolder.Center();
			//bpWebCamPaneHolder.getChildren().add(imgWebCamCapturedImage);
			//BorderPane.setAlignment(imgWebCamCapturedImage, Pos.CENTER);

			//bpWebCamPaneHolder.setCenter(imgWebCamCapturedImage);
			ip.set(nbi);
			imgWebCamCapturedImage.imageProperty().bind(ip);
			//double w1 = imgWebCamCapturedImage.getImage().getWidth();
			//imgWebCamCapturedImage.setX(bpWebCamPaneHolder.getWidth()/2 - w1/5);
			//imgWebCamCapturedImage.setY(0);
		}
		catch (NullPointerException ex){
			return;
		}
		btnRecognize.setDisable(false);
	}

	public void setPrevStage(Stage stage){
		this.prevStage = stage;
	}
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//fpBottomPane.setDisable(true);
		btnRecognize.setDisable(true);
		ObservableList<WebCamInfo> options = FXCollections.observableArrayList();
		int webCamCounter = 0;
		for(Webcam webcam:Webcam.getWebcams())
		{
			WebCamInfo webCamInfo = new WebCamInfo();
			webCamInfo.setWebCamIndex(webCamCounter);
			webCamInfo.setWebCamName(webcam.getName());
			options.add(webCamInfo);
			webCamCounter ++;
		}
		cbCameraOptions.setItems(options);
		cbCameraOptions.setPromptText(cameraListPromptText);
		cbCameraOptions.getSelectionModel().selectedItemProperty().addListener(new  ChangeListener<WebCamInfo>() {

	        @Override
	        public void changed(ObservableValue<? extends WebCamInfo> arg0, WebCamInfo arg1, WebCamInfo arg2) {
	            if (arg2 != null) {

	            	System.out.println("WebCam Index: " + arg2.getWebCamIndex()+": WebCam Name:"+ arg2.getWebCamName());
	            	initializeWebCam(arg2.getWebCamIndex());
	            }
	        }
	    });
		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				setImageViewSize();

			}
		});

	}
	protected void setImageViewSize() {
		
		double height = bpWebCamPaneHolder.getHeight();
		double width  = bpWebCamPaneHolder.getWidth();
		imgWebCamCapturedImage.setFitHeight(height);
		imgWebCamCapturedImage.setFitWidth(width);
		imgWebCamCapturedImage.prefHeight(height);
		imgWebCamCapturedImage.prefWidth(width);
		imgWebCamCapturedImage.setPreserveRatio(true);
		
	}
	protected void initializeWebCam(final int webCamIndex) {
		
		Task<Void> webCamIntilizer = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				
				if(selWebCam == null)
				{
					Dimension[] nonStandardResolutions = new Dimension[] {
							WebcamResolution.PAL.getSize(),
							WebcamResolution.HD720.getSize(),
							new Dimension(2000, 1000),
							new Dimension(1000, 500),
					};
					selWebCam = Webcam.getWebcams().get(webCamIndex);
					selWebCam.setCustomViewSizes(nonStandardResolutions);
					selWebCam.setViewSize(WebcamResolution.HD720.getSize());
					selWebCam.open();
				}else
				{
					closeCamera();
					selWebCam = Webcam.getWebcams().get(webCamIndex);
					selWebCam.open();
					
				}
				startWebCamStream();
				return null;
			}
			
		};
		
		new Thread(webCamIntilizer).start();
		//fpBottomPane.setDisable(false);
		btnRecognize.setDisable(false);
	}
	
	protected void startWebCamStream() {
		
		stopCamera  = false;
		Task<Void> task = new Task<Void>() {

		
			@Override
			protected Void call() throws Exception {

				while (!stopCamera) {
					try {
						if ((grabbedImage = selWebCam.getImage()) != null) {
							
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									final Image mainImage = SwingFXUtils.toFXImage(grabbedImage, null);
									imageProperty.set(mainImage);
								}
							});

							grabbedImage.flush();

						}
					} catch (Exception e) {
					} finally {

					}

				}

				return null;

			}

		};
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
		imgWebCamCapturedImage.imageProperty().bind(imageProperty);
		
	}



	private void closeCamera()
	{
		if(selWebCam != null)
		{
			selWebCam.close();
		}
	}


	
	public void Recognize(ActionEvent event) throws Exception
	{
		//String workingDir = new File("").getAbsolutePath();
		String workingDir = "E:\\work\\repo0\\JavaFXSpringBootApp\\scripts";
		//WebcamUtils.capture(selWebCam, workingDir + "\\scripts\\image");
		try {
			ImageIO.write(selWebCam.getImage(), "jpg", new File(workingDir + "\\image.jpg"));
		}
		catch (Exception ex){
			copy(imgFilename, workingDir + "\\image.jpg");
		}
		String address = workingDir + "\\prediction.py";

		String cmd = "python " + "\"" + address + "\"";

		Process p = Runtime.getRuntime().exec(cmd);
		p.waitFor();

		File f = new File(workingDir + "\\result.txt");
		BufferedReader fin = new BufferedReader(new FileReader(f));
		String name;
		name = fin.readLine();
		predictionPerson.setText(name);
		fin.close();
	}
	
	class WebCamInfo
	{
		private String webCamName ;
		private int webCamIndex ;
		
		public String getWebCamName() {
			return webCamName;
		}
		public void setWebCamName(String webCamName) {
			this.webCamName = webCamName;
		}
		public int getWebCamIndex() {
			return webCamIndex;
		}
		public void setWebCamIndex(int webCamIndex) {
			this.webCamIndex = webCamIndex;
		}
		
		@Override
		public String toString() {
		        return webCamName;
	     }
		
	}
	public static void copy (String nameFrom, String nameTo)
			throws Exception
	{
		FileChannel source = new FileInputStream(new File(nameFrom)).getChannel();
		FileChannel dest = new FileOutputStream(new File(nameTo)).getChannel();
		try {
			source.transferTo(0, source.size(), dest);
		} finally {
			source.close();
			dest.close();
		}
	}

}
