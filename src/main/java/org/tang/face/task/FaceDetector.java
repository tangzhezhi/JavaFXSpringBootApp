package org.tang.face.task;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.*;
import org.tang.face.bean.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

public class FaceDetector implements Runnable {

	private volatile boolean inited = false;

	private List<User> user;
	private String userName; //first name

	//传入用户数据集
	public FaceDetector(List<User> user){
		this.user = user;
	}

//	FaceRecognizer faceRecognizer = new FaceRecognizer();
//	OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();

	private FaceRecognizer faceRecognizer;
	private OpenCVFrameConverter.ToIplImage grabberConverter ;

	private Java2DFrameConverter paintConverter ;
	ArrayList<User> output = null;

	private Exception exception = null;
	
	private int count = 0;
	public String classiferName;
	public File classifierFile;
	
	
	public boolean saveFace = false;
	public volatile  boolean isRecFace = false;
	private volatile boolean stop = false;

	private CvHaarClassifierCascade classifier = null;
	private CvHaarClassifierCascade classifierSideFace = null;
	
	public CvMemStorage storage = null;
	private FrameGrabber grabber = null;
	private IplImage grabbedImage = null, temp , grayImage = null, smallImage = null;
	public ImageView frames;

	private CvSeq faces = null;

	int recogniseCode;
	public String name;

	public void init() {
		if(!inited){
			faceRecognizer = new FaceRecognizer();
			faceRecognizer.init();
			setClassifier("haar/haarcascade_frontalface_alt.xml");
			setClassifierSideFace("haar/haarcascade_profileface.xml");
			inited = true;

			paintConverter = new Java2DFrameConverter();
			grabberConverter = new OpenCVFrameConverter.ToIplImage();
		}
		else{
			System.out.println("已经初始化过了train");
		}
	}

	public void start() {
		try {
			new Thread(this).start();
		} catch (Exception e) {
			if (exception == null) {
				exception = e;
			}
			e.printStackTrace();
		}
	}

	public void run() {

		if(paintConverter == null){
			paintConverter = new Java2DFrameConverter();
		}

		if(grabberConverter == null){
			grabberConverter = new OpenCVFrameConverter.ToIplImage();
		}

		try {
			try {

				if(grabber==null){
					grabber = OpenCVFrameGrabber.createDefault(0); //parameter 0 default camera , 1 for secondary
					grabber.setImageWidth(700);
					grabber.setImageHeight(700);
					grabber.start();

					grabbedImage = grabberConverter.convert(grabber.grab());
//					faceRecognizer = new FaceRecognizer();
					storage = CvMemStorage.create();
				}
				else {
						grabber.close();
						grabber = new OpenCVFrameGrabber(0);
						grabber.setImageWidth(700);
						grabber.setImageHeight(700);
						grabber.start();
						grabbedImage = grabberConverter.convert(grabber.grab());
				}
			} catch (Exception e) {
				if (grabber != null){
					grabber.close();
					grabber = new OpenCVFrameGrabber(0);
					grabber.setImageWidth(700);
					grabber.setImageHeight(700);
					grabber.start();
					grabbedImage = grabberConverter.convert(grabber.grab());
				}
				e.printStackTrace();
			}

			grayImage = cvCreateImage(cvGetSize(grabbedImage), 8, 1); //converting image to grayscale
			//reducing the size of the image to speed up the processing
			smallImage = cvCreateImage(cvSize(grabbedImage.width() / 4, grabbedImage.height() / 4), 8, 1);
			stop = false;

			while (!stop && (grabbedImage = grabberConverter.convert(grabber.grab())) != null) {

				Frame frame = grabberConverter.convert(grabbedImage);
				BufferedImage image = paintConverter.getBufferedImage(frame, 2.2 / grabber.getGamma());
				Graphics2D g2 = image.createGraphics();

				if (faces == null) {
					cvClearMemStorage(storage);
					
					//creating a temporary image
					temp = cvCreateImage(cvGetSize(grabbedImage), grabbedImage.depth(), grabbedImage.nChannels());

					cvCopy(grabbedImage, temp);

					cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
					cvResize(grayImage, smallImage, CV_INTER_AREA);
					
					//cvHaarDetectObjects(image, cascade, storage, scale_factor, min_neighbors, flags, min_size, max_size)
					faces = cvHaarDetectObjects(smallImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
					//face detection
					
					if (grabbedImage != null) {

						if (faces.total() == 0) {
							faces = cvHaarDetectObjects(smallImage, classifierSideFace, storage, 1.1, 3,
									CV_HAAR_DO_CANNY_PRUNING);

						}

						if (faces != null) {

							g2.setColor(Color.green);
							g2.setStroke(new BasicStroke(2));
							int total = faces.total();

//							System.out.println("识别的人脸数量:"+total);


							for (int i = 0; i < total; i++) {
								
								//printing rectange box where face detected frame by frame
								CvRect r = new CvRect(cvGetSeqElem(faces, i));
								g2.drawRect((r.x() * 4), (r.y() * 4), (r.width() * 4), (r.height() * 4));

								CvRect re = new CvRect((r.x() * 4), r.y() * 4, (r.width() * 4), r.height() * 4);

								cvSetImageROI(temp, re);


								if (isRecFace) {
									try {
										this.recogniseCode = faceRecognizer.recognize(temp);
									} catch (Exception e) {
										e.printStackTrace();
										continue;
									}

									try {
										if(user!=null){
											output = new ArrayList<>();

											user.stream().forEach(u->{
												if(u.getRecogniseCode() == this.recogniseCode){
													output.add(u);
													g2.setColor(Color.WHITE);
													g2.setFont(new Font("Arial Black", Font.BOLD, 20));
													String names = u.getUserName();
													g2.drawString(names, (int) (r.x() * 6.5), r.y() * 4);
												}
											});
										}
									} catch (Exception e) {
										e.printStackTrace();
										continue;
									}

								}

							}
							faces = null;
						}

						WritableImage showFrame = SwingFXUtils.toFXImage(image, null);

						frames.setImage(showFrame);

					}
					cvReleaseImage(temp);
				}
				else{
					faces = null;
				}

			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;
			}
			e.printStackTrace();
		}
	}

	public void stop() {
		stop = true;

		grabbedImage = grayImage = smallImage = null;

		try {
			if(grabber!=null){
				grabber.close();
			}
			paintConverter = null;
			grabberConverter = null;
		} catch (FrameGrabber.Exception e) {
			e.printStackTrace();
		}
		grabber = null;
	}

	public void setClassifier(String name) {

		try {

			setClassiferName(name);
//			classifierFile = Loader.extractResource(ClassLoader.getSystemResource("").getPath()+classiferName, null, "classifier", ".xml");

			classifierFile = new File(ClassLoader.getSystemResource("").getPath()+classiferName);

			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}


	public void setClassifierSideFace(String name) {

		try {

			classiferName = name;
//			classifierFile = Loader.extractResource(ClassLoader.getSystemResource("").getPath()+classiferName, null, "classifier", ".xml");

			classifierFile = new File(ClassLoader.getSystemResource("").getPath()+classiferName);

			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifierSideFace = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}

	public void setClassiferName(String classiferName) {
		this.classiferName = classiferName;
	}

	public void destroy() {

	}


	public void clearOutput() {
		if(this.output!=null){
			this.output.clear();
		}
	}

	public void setFrames(ImageView frames) {
		this.frames = frames;
	}

	public void setIsRecFace(boolean isRecFace) {
		this.isRecFace = isRecFace;
	}

	public ArrayList<User> getOutput() {
		return output;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setSaveFace(Boolean f) {
		this.saveFace = f;
	}
}
