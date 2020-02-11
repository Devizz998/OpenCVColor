package com.example.opencvproject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * @author Mattia De Vivo
 * @version 1.0 16/12/19
 * @since 1.0 Processa la matrice rgb ricevuta in inputer alla ricerca del colore HSV specificato
 */
public class ColorBlobDetector {
    
    // Limite inderiore e superiore per il range di colori HSV da cercare
    private Scalar mLowerBound = new Scalar(0);
    
    private Scalar mUpperBound = new Scalar(0);
    
    // Percentuale minima di colore corrispondente al colore da cercare nell'area individuata
    private static double mMinContourArea = 0.3;
    
    // Range di tolleranza specificato in HSV
    private Scalar mColorRadius = new Scalar(30, 125, 60, 0);
    
    private Mat mSpectrum = new Mat();
    
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
    
    // Variabili di Cache
    private Mat mPyrDownMat = new Mat();
    
    private Mat mHsvMat = new Mat();
    
    private Mat mMask = new Mat();
    
    private Mat mDilatedMask = new Mat();
    
    private Mat mHierarchy = new Mat();
    
    /**
     * Setter per impostare il raggio di tolleranza
     *
     * @param radius
     */
    public void setColorRadius(Scalar radius) {
        
        this.mColorRadius = radius;
    }
    
    /**
     * Imposta il range di colori che devono essere accettati sulla base del colore HSV ricevuto in
     * input
     *
     * @param hsvColor colore HSV da individuare
     */
    void setHsvColor(Scalar hsvColor) {
        
        double minH = (hsvColor.val[0] >= this.mColorRadius.val[0]) ?
                      (hsvColor.val[0] - this.mColorRadius.val[0]) :
                      0;
        double maxH = ((hsvColor.val[0] + this.mColorRadius.val[0]) <= 255) ?
                      (hsvColor.val[0] + this.mColorRadius.val[0]) :
                      255;
        
        this.mLowerBound.val[0] = minH;
        this.mUpperBound.val[0] = maxH;
        
        this.mLowerBound.val[1] = hsvColor.val[1] - this.mColorRadius.val[1];
        this.mUpperBound.val[1] = hsvColor.val[1] + this.mColorRadius.val[1];
        
        this.mLowerBound.val[2] = hsvColor.val[2] - this.mColorRadius.val[2];
        this.mUpperBound.val[2] = hsvColor.val[2] + this.mColorRadius.val[2];
        
        this.mLowerBound.val[3] = 0;
        this.mUpperBound.val[3] = 255;
        
        Mat spectrumHsv = new Mat(1, (int) (maxH - minH), CvType.CV_8UC3);
        
        for (int j = 0; j < maxH - minH; j++) {
            byte[] tmp = {(byte) (minH + j), (byte) 255, (byte) 255};
            spectrumHsv.put(0, j, tmp);
        }
        
        Imgproc.cvtColor(spectrumHsv, this.mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }
    
    /**
     * Getter che ritorna il range di colori validi
     *
     * @return Mat con coordinate per disegnare il contorno
     */
    Mat getSpectrum() {
        
        return this.mSpectrum;
    }
    
    /**
     * Settter per la percentuale minima di colore corrispondente al colore da cercare nell'area
     * individuata
     *
     * @param area percentuale
     */
    public void setMinContourArea(double area) {
        
        mMinContourArea = area;
    }
    
    /**
     * Processa l'immagine attuale alla ricerca del colore specificato
     *
     * @param rgbaImage Mat che rappresenta l'immagine attuale
     */
    void process(Mat rgbaImage) {
        
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
        
        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        
        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());
        
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        
        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);
        
        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea) {
                maxArea = area;
            }
        }
        
        // Filter contours by area and resize to fit the original image size
        this.mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea * maxArea) {
                Core.multiply(contour, new Scalar(4, 4), contour);
                mContours.add(contour);
            }
        }
    }
    
    /**
     * @return List di punti usati per evidenziare l'area di colore individuata
     */
    List<MatOfPoint> getContours() {
        
        return this.mContours;
    }
    
    /**
     * @return true se c'è un'area dell'immagine con il colore cercato, false altrimenti
     */
    boolean isThereColor() {
        
        return !this.mContours.isEmpty();
    }
}