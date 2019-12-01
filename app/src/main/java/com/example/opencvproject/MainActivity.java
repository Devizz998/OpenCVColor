package com.example.opencvproject;

import java.util.List;

import android.view.WindowManager.LayoutParams;
import com.example.opencvproject.R.id;
import com.example.opencvproject.R.layout;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
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

public class MainActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";
    
    private boolean              mIsColorSelected;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private ColorBlobDetector    mDetector2;
    private ColorBlobDetector    mDetector3;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    
    private CameraBridgeViewBase mOpenCvCameraView;
    
    private final BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(final int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(MainActivity.TAG, "OpenCV loaded successfully");
                    MainActivity.this.mOpenCvCameraView.enableView();
                    MainActivity.this.mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
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
        if (this.mOpenCvCameraView != null)
            this.mOpenCvCameraView.disableView();
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
    
        this.mRgba = new Mat(height, width, CvType.CV_8UC4);
        this.mDetector = new ColorBlobDetector();
        this.mDetector2 = new ColorBlobDetector();
        this.mDetector3 = new ColorBlobDetector();
        this.mSpectrum = new Mat();
        this.mBlobColorRgba = new Scalar(255);
        this.mBlobColorHsv = new Scalar(255);
        this.SPECTRUM_SIZE = new Size(200, 64);
        this.CONTOUR_COLOR = new Scalar(0, 255, 0, 255);
    }
    
    @Override
    public void onCameraViewStopped() {
    
        this.mRgba.release();
    }
    
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        /*final int cols = this.mRgba.cols();
        final int rows = this.mRgba.rows();
        
        final int xOffset = (this.mOpenCvCameraView.getWidth() - cols) / 2;
        final int yOffset = (this.mOpenCvCameraView.getHeight() - rows) / 2;
        
        final int x = (int)event.getX() - xOffset;
        final int y = (int)event.getY() - yOffset;
        
        Log.i(MainActivity.TAG, "Touch image coordinates: (" + x + ", " + y + ")");
        
        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;
        
        final Rect touchedRect = new Rect();
        
        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;
        
        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;
        
        final Mat touchedRegionRgba = this.mRgba.submat(touchedRect);
        
        //TEST ---------
        Log.i(MainActivity.TAG,"&&&&&& matrice: " + touchedRegionRgba.toString());
        
        final Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
        
        // Calculate average color of touched region
        this.mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        
        //TEST ---------
        Log.i(MainActivity.TAG,"&&&&&& colore prima: " + mBlobColorHsv.toString());
        //FINE TEST -------
        final int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < this.mBlobColorHsv.val.length; i++) {
            this.mBlobColorHsv.val[i] /= pointCount;
        }
    
        //TEST ---------
        Log.i(MainActivity.TAG,"&&&&&& colore dopo: " + mBlobColorHsv.toString());
        //FINE TEST -------
        
        this.mBlobColorRgba = this.converScalarHsv2Rgba(this.mBlobColorHsv);
        
        Log.i(MainActivity.TAG, "Touched rgba color: (" + this.mBlobColorRgba.val[0] + ", " +
                this.mBlobColorRgba.val[1] +
                ", " + this.mBlobColorRgba.val[2] + ", " + this.mBlobColorRgba.val[3] + ")");
        
        this.mDetector.setHsvColor(this.mBlobColorHsv);
        
        Imgproc.resize(this.mDetector.getSpectrum(), this.mSpectrum, this.SPECTRUM_SIZE, 0, 0, Imgproc.INTER_LINEAR_EXACT);
    
        this.mIsColorSelected = true;
        
       touchedRegionRgba.release();
       touchedRegionHsv.release();
        */
        // Colori specificati in rgba
        Scalar rgbaRed = new Scalar(255, 0, 0, 0);
        Scalar rgbaBlue = new Scalar(0, 0, 170, 0);
        Scalar rgbaYellow = new Scalar(225, 225, 0, 0);
        
        // Colori specificati in hsv
        Scalar hsvRed = converScalarRgba2HSV(rgbaRed);
        Scalar hsvBlue = converScalarRgba2HSV(rgbaBlue);
        Scalar hsvYellow = converScalarRgba2HSV(rgbaYellow);
        
        // Aggiorna le variabili locali
        this.mBlobColorHsv = hsvBlue;
        this.mBlobColorRgba = rgbaBlue;
        
        // Imposta il colore da riconoscere
        this.mDetector.setHsvColor(this.mBlobColorHsv);
        // --------TESTARE più colori contemporaneamente
        this.mDetector2.setHsvColor(hsvRed);
        this.mDetector3.setHsvColor(hsvYellow);
        
        // Imposta l'area da evidenziare
        Imgproc.resize(this.mDetector.getSpectrum(), this.mSpectrum, this.SPECTRUM_SIZE, 0, 0, Imgproc.INTER_LINEAR_EXACT);
        
        //indica che il colore è stato selezionato
        this.mIsColorSelected = true;
       
       return false; // don't need subsequent touch events
    }
    
    @Override
    public Mat onCameraFrame(final CvCameraViewFrame inputFrame) {
    
        this.mRgba = inputFrame.rgba();
        
        if (this.mIsColorSelected) {
            this.mDetector.process(this.mRgba);
            final List<MatOfPoint> contours = this.mDetector.getContours();
            //Log.e(MainActivity.TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(this.mRgba, contours, -1, this.CONTOUR_COLOR);
            
            final Mat colorLabel = this.mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(this.mBlobColorRgba);
            
            final Mat spectrumLabel = this.mRgba.submat(4, 4 + this.mSpectrum.rows(), 70, 70 +
                    this.mSpectrum.cols());
            this.mSpectrum.copyTo(spectrumLabel);
        }
        
        return this.mRgba;
    }
    
    
    private Scalar converScalarHsv2Rgba(final Scalar hsvColor) {
        final Mat pointMatRgba = new Mat();
        final Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
        
        return new Scalar(pointMatRgba.get(0, 0));
    }
    
    /**
     * Converte il colore da RGBA a HSV
     * Per ottenere i valori della palette in HSV fare
     * (valore / 255 * 360, valore/ 255 * 100, valore / 255 * 100)
     * @param rgba
     * @return
     */
    private Scalar converScalarRgba2HSV(Scalar rgba) {
        Mat  pointMatHsv= new Mat();
        Mat pointMatRgba = new Mat(1, 1, CvType.CV_8UC3, rgba);
        Imgproc.cvtColor(pointMatRgba,pointMatHsv, Imgproc.COLOR_RGB2HSV_FULL, 4);
        
        return new Scalar(pointMatHsv.get(0, 0));
    }
}