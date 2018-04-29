package org.tang.face.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.joda.time.DateTime;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Controller;
import org.tang.face.utils.Utils;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 4/18/2018.
 */

@Controller
public class FaceMainController implements Initializable {

    @FXML
    private Button startCam;
    @FXML
    private Button stopBtn;

    @FXML
    private Button recogniseBtn;

    @FXML
    private Button saveBtn;

    // the FXML area for showing the current frame
    @FXML
    private ImageView originalFrame;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that performs the video capture
    private VideoCapture capture;
    // a flag to change the button behavior
    private boolean cameraActive;

    // face cascade classifier 人脸
    private CascadeClassifier faceCascade;

    //鼻子
    private CascadeClassifier faceNoseCascade;

    private int absoluteFaceSize;

    private static  String picPath ;
    private static  String src_picPath ;


    @FXML
    protected void startCamera()  {

        this.checkboxSelection(faceCascade,"haarcascades/haarcascade_frontalface_alt2.xml");
        this.checkboxSelection(faceNoseCascade,"haarcascades/haarcascade_mcs_nose.xml");
        // set a fixed width for the frame
        originalFrame.setFitWidth(600);
        // preserve image ratio
        originalFrame.setPreserveRatio(true);

        if (!this.cameraActive)
        {
            // start the video capture
//            this.capture.open(0);
            try {
                 //可以读入视频流
//                this.capture =new VideoCapture("http://220.170.49.115/8/s/q/o/g/sqoghmqekmhkuanfsbhjacvnxkyain/hd.yinyuetai.com/00340160533ED8BE5CD388FF042A394E.mp4?sc=d7e2aeeabb50412f");//读取视频
                this.capture =new VideoCapture("F://6.mp4");//读取视频

            } catch (Exception e) {
                e.printStackTrace();
            }

            // is the video stream available?
            if (this.capture.isOpened())
            {
                this.cameraActive = true;

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = () -> {
                    // effectively grab and process a single frame
                    Mat frame = grabFrame();
                    // convert and show the frame
                    Image imageToShow = Utils.mat2Image(frame);
                    updateImageView(originalFrame, imageToShow);
                };


                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

            }
            else
            {
                // log the error
                System.err.println("Failed to open the camera connection...");
                stopAcquisition();
            }

        }
        else
        {
            // the camera is not active at this point
            this.cameraActive = false;
            // stop the timer
            try
            {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                // log the exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
                stopAcquisition();
            }

            // release the camera
            this.capture.release();
            // clean the frame
            this.originalFrame.setImage(null);

            stopAcquisition();
        }
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
        stopAcquisition();

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        this.capture = new VideoCapture();
        this.faceCascade = new CascadeClassifier();

        this.faceNoseCascade = new CascadeClassifier();

        this.absoluteFaceSize = 0;

        picPath  =  ClassLoader.getSystemResource("").getPath()+"pic";
        src_picPath  =  ClassLoader.getSystemResource("").getPath()+"srcPic";
        File file = new File(picPath);
        if(!file.exists()){
            file.mkdir();
        }

        File srcfile = new File(src_picPath);
        if(!srcfile.exists()){
            srcfile.mkdir();
        }

    }



    /**
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Image} to show
     */
    private Mat grabFrame()
    {
        Mat frame = new Mat();
        // check if the capture is open
        if (this.capture.isOpened())
        {
            try
            {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty())
                {
                    // face detection
                    this.detectAndDisplay(frame);
                }
            }
            catch (Exception e)
            {
                // log the (full) error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }
        return frame;
    }

    /**
     * Method for face detection and tracking
     *
     * @param frame
     *            it looks for faces in this frame
     */
    private void detectAndDisplay(Mat frame)
    {

        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height, in our case)
        if (this.absoluteFaceSize == 0)
        {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0)
            {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
//        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
//                new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
        /*
        参数1：image–待检测图片，一般为灰度图像加快检测速度；
        参数2：objects–被检测物体的矩形框向量组；为输出量，如人脸检测矩阵Mat
        参数3：scaleFactor–表示在前后两次相继的扫描中，搜索窗口的比例系数。默认为1.1即每次搜索窗口依次扩大10%;一般设置为1.1
        参数4：minNeighbors–表示构成检测目标的相邻矩形的最小个数(默认为3个)。
        如果组成检测目标的小矩形的个数和小于 min_neighbors - 1 都会被排除。
        如果min_neighbors 为 0, 则函数不做任何操作就返回所有的被检候选矩形框，
        这种设定值一般用在用户自定义对检测结果的组合程序上；
        参数5：flags–要么使用默认值，要么使用CV_HAAR_DO_CANNY_PRUNING，如果设置为CV_HAAR_DO_CANNY_PRUNING，那么函数将会使用Canny边缘检测来排除边缘过多或过少的区域，因此这些区域通常不会是人脸所在区域；
        参数6、7：minSize和maxSize用来限制得到的目标区域的范围。也就是我本次训练得到实际项目尺寸大小
        函数介绍：
        detectMultiscale函数为多尺度多目标检测：
        多尺度：通常搜索目标的模板尺寸大小是固定的，但是不同图片大小不同，所以目标对象的大小也是不定的，所以多尺度即不断缩放图片大小（缩放到与模板匹配），通过模板滑动窗函数搜索匹配；同一副图片可能在不同尺度下都得到匹配值，所以多尺度检测函数detectMultiscale是多尺度合并的结果。
        多目标：通过检测符合模板匹配对象，可得到多个目标，均输出到objects向量里面。
        */
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_DO_CANNY_PRUNING,
                new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());


        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();
        for (Rect rect : facesArray){
//            Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0), 3);

            Rect croppedRect = new Rect(rect.tl(), rect.br());//get each one
            Mat croppedFace = new Mat(frame, croppedRect); // cropped matric
            Imgproc.cvtColor(croppedFace, croppedFace, Imgproc.COLOR_BGR2GRAY);// transit photo type
            Imgproc.equalizeHist(croppedFace, croppedFace);//make more accuary
            Mat resizedFace = new Mat();
            Size faceLocalSize = new Size(250, 250);//resize the face photo
            Imgproc.resize(croppedFace, resizedFace, faceLocalSize);//make face photos to proper size

            String filename = "/faceDetection_"+ DateTime.now().toString("yyyyMMddHHmmss")+".jpg ";

            String destfilename =  picPath+filename;
            String srcfilename =  src_picPath+filename;

            destfilename = destfilename.substring(1,destfilename.length());
            srcfilename = srcfilename.substring(1,srcfilename.length());

            System.out.println(String.format("Writing %s", destfilename));
            System.out.println(String.format("Writing %s", srcfilename));

            Imgcodecs.imwrite(destfilename, resizedFace);


            //保存灰度图片
//            Mat sub = null;
//
//            Mat src_sub = null;
//
//            Mat mat = null;
//            Mat src_mat = null;
//
//            try {
//                sub = grayFrame.submat(rect);
//                src_sub = frame.submat(rect);
//                mat = new Mat();
//                src_mat = new Mat();
//
//                Size size = new Size(256, 256);
//                Imgproc.resize(sub, mat, size);
//                Imgproc.resize(src_sub, src_mat, size);
//
//                String filename = "/faceDetection_"+ DateTime.now().toString("yyyyMMddHHmmss")+".jpg ";
//
//                String destfilename =  picPath+filename;
//                String srcfilename =  src_picPath+filename;
//
//                destfilename = destfilename.substring(1,destfilename.length());
//                srcfilename = srcfilename.substring(1,srcfilename.length());
//
//                System.out.println(String.format("Writing %s", destfilename));
//                System.out.println(String.format("Writing %s", srcfilename));
//
//                Imgcodecs.imwrite(destfilename, mat);
//                Imgcodecs.imwrite(srcfilename, src_mat);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                sub.release();
//                src_sub.release();
//                mat.release();
//                src_mat.release();
//            }
//            sub.release();
//            src_sub.release();
//            mat.release();
//            src_mat.release();

        }


    }


    public  Mat getNose(Mat src) {
        Mat result = src.clone();
        if (src.cols() > 1000 || src.rows() > 1000) {
            Imgproc.resize(src, result, new Size(src.cols() / 3, src.rows() / 3));
        }

        MatOfRect objDetections = new MatOfRect();
        faceNoseCascade.detectMultiScale(result, objDetections);

        if(objDetections!=null && objDetections.toArray().length > 0){
            for (Rect rect : objDetections.toArray()) {
                Imgproc.rectangle(result, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 0), 3);
            }
            return result;
        }
        else{
            return null;
        }
    }




    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition()
    {
        if (this.timer!=null && !this.timer.isShutdown())
        {
            try
            {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened())
        {
            // release the camera
            this.capture.release();
        }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view
     *            the {@link ImageView} to update
     * @param image
     *            the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image)
    {
        Utils.onFXThread(view.imageProperty(), image);
    }


    private void checkboxSelection(CascadeClassifier faceCascade ,String classifierPath)
    {
        String path = ClassLoader.getSystemResource("").getPath()+classifierPath;
        path = path.substring(1,path.length());
        faceCascade.load(path);
    }


}
