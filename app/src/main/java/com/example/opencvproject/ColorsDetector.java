package com.example.opencvproject;

import java.util.List;

import android.util.Log;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Classe che comunica se sono presenti oggetti con colori giallo, rosso o blu utilizzando la Camera
 * di OpenCV
 *
 * @author Mattia De Vivo
 * @version 1.0 13/12/19
 * @since 1.0
 */
public class ColorsDetector {
    
    private static final String TAG = "OpenCVColor";
    
    /**
     * Matrice utilizzata per memorizzare i vari frame come immagine rgba
     */
    private Mat matrixRgba = null;
    
    /**
     * Detector del colore red
     */
    private ColorBlobDetector redDetector = null;
    
    /**
     * Detector del colore blue
     */
    private ColorBlobDetector blueDetector = null;
    
    /**
     * Detector del colore yellow
     */
    private ColorBlobDetector yellowDetector = null;
    
    /**
     * Colore dei contorni
     */
    private Scalar CONTOUR_COLOR = null;
    
    /**
     * Costruttore di classe
     */
    public ColorsDetector() {
        // Matrices which contain coordinates of the color recognizes, one for
        // each color (values set by the ColorBlobDetectors)
        Mat matrixSpectrum = new Mat();
        Mat matrixSpectrum2 = new Mat();
        Mat matrixSpectrum3 = new Mat();
        
        // Inizializza i ColorDetector, uno per ogni colore
        ColorsDetector.this.redDetector = new ColorBlobDetector();
        ColorsDetector.this.blueDetector = new ColorBlobDetector();
        ColorsDetector.this.yellowDetector = new ColorBlobDetector();
        
        // Rosso
        final Scalar redRgba = new Scalar(230, 0, 60, 0);
        final Scalar redHsv = ColorsDetector.this.converScalarRgba2HSV(redRgba);
        // Blu
        final Scalar blueRgba = new Scalar(0, 0, 165, 0);
        final Scalar blueHsv = ColorsDetector.this.converScalarRgba2HSV(blueRgba);
        // Giallo
        final Scalar yellowRgba = new Scalar(255, 255, 0, 0);
        final Scalar yellowHsv = ColorsDetector.this.converScalarRgba2HSV(yellowRgba);
        
        // Dimensione e colore delle line che evidenziano le aree dell'immagine
        // che contengono i colori cercati
        Size SPECTRUM_SIZE = new Size(200, 64);
        ColorsDetector.this.CONTOUR_COLOR = new Scalar(0, 255, 0, 255);
        
        // Imposta il colore che dev'essere individuato da ciascun Detector
        ColorsDetector.this.redDetector.setHsvColor(redHsv);
        ColorsDetector.this.blueDetector.setHsvColor(blueHsv);
        ColorsDetector.this.yellowDetector.setHsvColor(yellowHsv);
        
        // Trasformazione geometrica
        // void resize(inputMat src, OutputMat dst, Size dsize, double fx=0,
        // double fy=0, int interpolation=INTER_LINEAR )
        Imgproc.resize(ColorsDetector.this.redDetector.getSpectrum(), matrixSpectrum, SPECTRUM_SIZE,
                0, 0, Imgproc.INTER_LINEAR_EXACT);
        Imgproc.resize(ColorsDetector.this.blueDetector.getSpectrum(), matrixSpectrum2,
                SPECTRUM_SIZE, 0, 0, Imgproc.INTER_LINEAR_EXACT);
        Imgproc.resize(ColorsDetector.this.yellowDetector.getSpectrum(), matrixSpectrum3,
                SPECTRUM_SIZE, 0, 0, Imgproc.INTER_LINEAR_EXACT);
    }
    
    /**
     * Processa la matrice in input per cercare che ci siano i colori
     *
     * @param inputRgbaMatrix matrice rgba da processare per cercare i colori
     */
    public void processMatrix(final Mat inputRgbaMatrix) {
        // Inizializza la matrice
        this.matrixRgba = inputRgbaMatrix;
        // Ogni ColorBlobDetector processa la matrice rgba corrispondente al frame
        // attuale per cercare i 3 diversi colori
        ColorsDetector.this.redDetector.process(ColorsDetector.this.matrixRgba);
        ColorsDetector.this.blueDetector.process(ColorsDetector.this.matrixRgba);
        ColorsDetector.this.yellowDetector.process(ColorsDetector.this.matrixRgba);
        // Riceve i contorni dell'area rossa individuata e li disegna
        final List<MatOfPoint> contours = ColorsDetector.this.redDetector.getContours();
        Log.d(ColorsDetector.TAG, "Red areas count: " + contours.size() + " Ris =" +
                ColorsDetector.this.redDetector.isThereColor());
        Imgproc.drawContours(ColorsDetector.this.matrixRgba, contours, -1,
                ColorsDetector.this.CONTOUR_COLOR);
        // GRiceve i contorni dell'area blu individuata e li disegna
        final List<MatOfPoint> contours2 = ColorsDetector.this.blueDetector.getContours();
        Log.d(ColorsDetector.TAG, "Blue ares count: " + contours2.size());
        Imgproc.drawContours(ColorsDetector.this.matrixRgba, contours2, -1,
                ColorsDetector.this.CONTOUR_COLOR);
        // Riceve i contorni dell'area gialla individuata e li disegna
        final List<MatOfPoint> contours3 = ColorsDetector.this.yellowDetector.getContours();
        Log.d(ColorsDetector.TAG, "Yellow areas count: " + contours3.size());
        Imgproc.drawContours(ColorsDetector.this.matrixRgba, contours3, -1,
                ColorsDetector.this.CONTOUR_COLOR);
    }
    
    /**
     * Converte il colore HSV in colore RGBA
     *
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
     * Converte il colore RGBA in colore HSV Per ottenere i valori HSV uguali a quelli di una
     * palette HSV bisogna: (value / 255 * 360, value/ 255 * 100, value / 255 * 100)
     *
     * @param rgba Scalar che rappresenta il colore rgba
     * @return Scalar rappresentante il colore hsv
     */
    private Scalar converScalarRgba2HSV(Scalar rgba) {
        
        final Mat pointMatHsv = new Mat();
        final Mat pointMatRgba = new Mat(1, 1, CvType.CV_8UC3, rgba);
        Imgproc.cvtColor(pointMatRgba, pointMatHsv, Imgproc.COLOR_RGB2HSV_FULL, 4);
        
        return new Scalar(pointMatHsv.get(0, 0));
    }
    
    /**
     * @return true se c'è il colore rosso, false altrimenti
     */
    public boolean isThereRed() {
        
        return this.redDetector.isThereColor();
    }
    
    /**
     * @return true se c'è il colore blu, false altrimenti
     */
    public boolean isThereBlue() {
        
        return this.blueDetector.isThereColor();
    }
    
    /**
     * @return true se c'è il colore yellow, false altrimenti
     */
    public boolean isThereYellow() {
        
        return this.yellowDetector.isThereColor();
    }
}