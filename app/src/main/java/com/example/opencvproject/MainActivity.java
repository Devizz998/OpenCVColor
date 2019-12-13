package com.example.opencvproject;

import java.util.List;

import android.annotation.SuppressLint;
import android.view.WindowManager.LayoutParams;
import com.example.opencvproject.R.id;
import com.example.opencvproject.R.layout;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;

/**
 * @author Mattia De Vivo
 * @version 1.2 13/12/19
 * @since 1.0
 */
public class MainActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OpenCVColor";
    // Matrix used to store rgba frame values
    private Mat                     matrixRgba = null;
    
    // ColorBlobDetector, each one for detecting a different color
    private ColorBlobDetector       mDetector = null;
    private ColorBlobDetector       mDetector2 = null;
    private ColorBlobDetector       mDetector3 = null;
    
    // Size and color of the lines that will highlith the color blobs recognized
    private Size                    SPECTRUM_SIZE = null;
    private Scalar                  CONTOUR_COLOR = null;
    
    private CameraBridgeViewBase    mOpenCvCameraView = null;
    
    private final BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(final int status) {
    
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(MainActivity.TAG, "OpenCV loaded successfully");
                MainActivity.this.mOpenCvCameraView.enableView();
                MainActivity.this.mOpenCvCameraView.setOnTouchListener(
                        MainActivity.this);
            } else {
                super.onManagerConnected(status);
            }
        }
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        Log.i(MainActivity.TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    
        this.setContentView(layout.activity_main);
    
        this.mOpenCvCameraView =
                this.findViewById(id.color_blob_detection_activity_surface_view);
        this.mOpenCvCameraView.setVisibility(View.VISIBLE);
        this.mOpenCvCameraView.setCvCameraViewListener(this);
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        if (this.mOpenCvCameraView != null) {
            this.mOpenCvCameraView.disableView();
        }
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(MainActivity.TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this,
                                   this.mLoaderCallback);
        } else {
            Log.d(MainActivity.TAG, "OpenCV library found inside package. Using it!");
            this.mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.mOpenCvCameraView != null) {
            this.mOpenCvCameraView.disableView();
        }
    }
    
    @Override
    public void onCameraViewStarted(final int width, final int height) {
    
        // HSV colors that we need to recognize
        Scalar                  colorToDetectHsv;
        Scalar                  colorToDetectHsv2;
        Scalar                  colorToDetectHsv3;
        // Matrices which contain coordinates of the color recognizes, one for
        // each color (values set by the ColorBlobDetectors)
        Mat                     matrixSpectrum = new Mat();
        Mat                     matrixSpectrum2 = new Mat();
        Mat                     matrixSpectrum3 = new Mat();
        
        this.matrixRgba = new Mat(height, width, CvType.CV_8UC4);
        // initialize ColorDetector, one for each color
        this.mDetector = new ColorBlobDetector();
        this.mDetector2 = new ColorBlobDetector();
        this.mDetector3 = new ColorBlobDetector();
        
        // Red
        final Scalar colorToDetectRgba = new Scalar(230, 0, 60, 0);
        colorToDetectHsv = this.converScalarRgba2HSV(colorToDetectRgba);
        // Blue
        final Scalar colorToDetectRgba2 = new Scalar(0, 0, 165, 0);
        colorToDetectHsv2 = this.converScalarRgba2HSV(colorToDetectRgba2);
        // Yellow
        final Scalar colorToDetectRgba3 = new Scalar(255, 255, 0, 0);
        colorToDetectHsv3 = this.converScalarRgba2HSV(colorToDetectRgba3);
        
        // Initial value = (200, 64)
        this.SPECTRUM_SIZE = new Size(200, 128);
        this.CONTOUR_COLOR = new Scalar(0, 255, 0, 255);
    
        // Set the color to be detected by each ColorBlobDetector
        this.mDetector.setHsvColor(colorToDetectHsv);
        this.mDetector2.setHsvColor(colorToDetectHsv2);
        this.mDetector3.setHsvColor(colorToDetectHsv3);
    
        // Geometrical transformation
        // void resize(inputMat src, OutputMat dst, Size dsize, double fx=0,
        // double fy=0, int interpolation=INTER_LINEAR )
        Imgproc.resize(this.mDetector.getSpectrum(), matrixSpectrum, this.SPECTRUM_SIZE, 0, 0, Imgproc.INTER_LINEAR_EXACT);
        Imgproc.resize(this.mDetector2.getSpectrum(), matrixSpectrum2, this.SPECTRUM_SIZE, 0, 0, Imgproc.INTER_LINEAR_EXACT );
        Imgproc.resize(this.mDetector3.getSpectrum(), matrixSpectrum3, this.SPECTRUM_SIZE, 0, 0, Imgproc.INTER_LINEAR_EXACT );
    }
    
    @Override
    public void onCameraViewStopped() {
    
        this.matrixRgba.release();
    }
    
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        // don't need subsequent touch events
       return false;
    }
    
    @SuppressLint("LogConditional")
    @Override
    public Mat onCameraFrame(final CvCameraViewFrame inputFrame) {
        // Extract rgba matrix from frame captured by camera
        this.matrixRgba = inputFrame.rgba();
        
        // Each ColorBlobDetector processes rgbMatrix's frame to find the 3 different colors
        this.mDetector.process(this.matrixRgba);
        this.mDetector2.process(this.matrixRgba);
        this.mDetector3.process(this.matrixRgba);
        // Get contours of red areas and draw them on the screen
        final List<MatOfPoint> contours = this.mDetector.getContours();
        Log.d(MainActivity.TAG, "Red areas count: " + contours.size() +" Ris =" +mDetector.isThereColor());
        Imgproc.drawContours(this.matrixRgba, contours, -1, this.CONTOUR_COLOR);
        // Get contours of blue areas and draw them on the screen
        final List<MatOfPoint> contours2 = this.mDetector2.getContours();
        Log.d(MainActivity.TAG, "Blue ares count: " + contours2.size());
        Imgproc.drawContours(this.matrixRgba, contours2, -1, this.CONTOUR_COLOR);
        // Get contours of yellow areas and draw them on the screen
        final List<MatOfPoint> contours3 = this.mDetector3.getContours();
        Log.d(MainActivity.TAG, "Yellow areas count: " + contours3.size());
        Imgproc.drawContours(this.matrixRgba, contours3, -1, this.CONTOUR_COLOR);
        
        return this.matrixRgba;
    }
    
    /**
     * Convert HSV color to RGBA color
     * @param hsvColor Scalar representing the color in HSV notation
     * @return Scalar representing the color in RGBA noation
     */
    private Scalar converScalarHsv2Rgba(final Scalar hsvColor) {
        final Mat pointMatRgba = new Mat();
        final Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
        
        return new Scalar(pointMatRgba.get(0, 0));
    }
    
    /**
     * Convert RGBA color to HSV color
     * For obtaining HSV values equal to the ones obtained in a HSV palette you
     * need to:
     * (value / 255 * 360, value/ 255 * 100, value / 255 * 100)
     * @param rgba Scalar representing the rgba color
     * @return Scalar representing the hsv color
     */
    private Scalar converScalarRgba2HSV(Scalar rgba) {
        Mat  pointMatHsv= new Mat();
        Mat pointMatRgba = new Mat(1, 1, CvType.CV_8UC3, rgba);
        Imgproc.cvtColor(pointMatRgba,pointMatHsv, Imgproc.COLOR_RGB2HSV_FULL, 4);
        
        return new Scalar(pointMatHsv.get(0, 0));
    }
}