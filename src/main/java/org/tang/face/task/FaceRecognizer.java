package org.tang.face.task;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.javacpp.opencv_objdetect;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.util.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class FaceRecognizer {

	LBPHFaceRecognizer faceRecognizer;

	public File root;
	MatVector images;
	Mat labels;



	public void init() {
		// mention the directory the faces has been saved
		String trainingDir = ClassLoader.getSystemResource("").getPath()+"faces";

		String desDir = ClassLoader.getSystemResource("").getPath()+"genFace";

		root = new File(trainingDir);

		FilenameFilter imgFilter = (dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
        };

		File[] imageFiles = root.listFiles(imgFilter);


		java.util.Arrays.stream(imageFiles).forEach(f->System.out.println(f.getName()));


		this.images = new MatVector(imageFiles.length);

		this.labels = new Mat(imageFiles.length, 1, CV_32SC1);
		IntBuffer labelsBuf = labels.createBuffer();

		int counter = 0;
		// reading face images from the folder

		for (File image : imageFiles) {
			Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);


			if(img.cols() < 1000){
				System.out.println("放大图片"+image.getName()+"img.cols"+img.cols());
				pyrUp(img,img);
			}

			resize(img, img, new Size(512, 512));

			File des = new File(desDir);
			if(!des.exists()){
				des.mkdir();
			}

			try {
				String fileName = des.getAbsolutePath()+File.separator+image.getName()+".jpg";

				System.out.println("生成的图片地址"+fileName);

				imwrite(fileName,img);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// extracting unique face code from the face image names
			// this unique face will be used to fetch all other information from
			// the database
			int label = Integer.parseInt(image.getName().split("\\-")[0]);

			images.put(counter, img);

			labelsBuf.put(counter, label);

			counter++;
		}

		// face training
		this.faceRecognizer = LBPHFaceRecognizer.create(1,10,10,10,10);
		faceRecognizer.setThreshold(140);
		this.faceRecognizer.train(images, labels);

	}

	public int recognize(IplImage faceData) {

		if(faceRecognizer == null && images == null && labels == null ){
			init();
		}


		Mat faces = cvarrToMat(faceData);

		cvtColor(faces, faces, CV_BGR2GRAY);

//		if(faces.cols() < 368){
//			pyrUp(faces,faces);
//		}

		resize(faces, faces, new Size(512,512));


		IntPointer label = new IntPointer(1);
		DoublePointer confidence = new DoublePointer(0);


		this.faceRecognizer.predict(faces, label, confidence);

		int predictedLabel = label.get(0);

//		System.out.println("predict_label::::"+this.faceRecognizer.predict_label(faces));

		return predictedLabel;

	}
}
