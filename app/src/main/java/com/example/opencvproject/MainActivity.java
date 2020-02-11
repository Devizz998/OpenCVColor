package com.example.opencvproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import androidx.appcompat.app.AppCompatActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;


/**
 * @author Mattia De Vivo
 * @version 1.2 31/01/21
 * @since 1.0 Activity di prova che mostra l'utilizzo della classe ColorsDetector
 */
public class MainActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {
    
    private CameraBridgeViewBase mOpenCVCameraView = null;
    
    private ColorsDetector colorsDetector = null;
    
    private Mat matrixRgba = null;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        // Forza lo schermo a sempre acceso
        this.getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Controlla se la libreria OpenCv è stata caricata
        if (!OpenCVLoader.initDebug()) {
            Log.e("AndroidIngSwOpenCV", "Unable to load OpenCV");
        } else {
            Log.d("AndroidIngSwOpenCV", "OpenCV loaded");
        }
        
        this.mOpenCVCameraView = this.findViewById(R.id.OpenCvView);
        this.mOpenCVCameraView.setVisibility(View.VISIBLE);
        // ------- PER RENDERE INVISIBILE la CameraView
        //this.mOpenCVCameraView.setAlpha(0);
        this.mOpenCVCameraView.enableView();
        this.mOpenCVCameraView.setCvCameraViewListener(this);
    }
    
    @Override
    public void onPause() {
        
        super.onPause();
        if (this.mOpenCVCameraView != null) {
            this.mOpenCVCameraView.disableView();
        }
    }
    
    @Override
    protected void onResume() {
        
        super.onResume();
        if (this.mOpenCVCameraView != null) {
            this.mOpenCVCameraView.enableView();
        }
    }
    
    @Override
    public void onDestroy() {
        
        super.onDestroy();
        if (this.mOpenCVCameraView != null) {
            this.mOpenCVCameraView.disableView();
        }
    }
    
    @Override
    public void onCameraViewStarted(final int width, final int height) {
        
        this.matrixRgba = new Mat(height, width, CvType.CV_8UC4);
        this.colorsDetector = new ColorsDetector();
    }
    
    @Override
    public void onCameraViewStopped() {
    
    }
    
    @Override
    public Mat onCameraFrame(final CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        
        this.matrixRgba = inputFrame.rgba();
        this.colorsDetector.processMatrix(this.matrixRgba);
        final String tag = "TEST";
        Log.d(tag, "Red : " + this.colorsDetector.isThereRed() + "\nBlue: " +
                this.colorsDetector.isThereBlue() + "\nYellow: " +
                this.colorsDetector.isThereYellow());
        return this.matrixRgba;
    }
}