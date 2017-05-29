package example.org.proyectobase;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AMARTIN on 29/05/2017.
 */

public class Procesador {
    MatOfInt canales;
    MatOfInt numero_bins;
    MatOfFloat intervalo;
    Mat hist;
    List<Mat> imagenes;
    float[] histograma;

    public Procesador() { //Constructor
        canales = new MatOfInt(0) ;
        numero_bins = new MatOfInt(256);
        intervalo = new MatOfFloat(0,256);
        hist = new Mat();
        imagenes = new ArrayList<Mat>() ;
        histograma = new float[256];
    }
    public Mat procesa(Mat entrada) {
        Mat salida = new Mat();
        imagenes.clear(); //Eliminar imagen anterior si la hay
        imagenes.add(entrada); //Añadir imagen actual
        Imgproc.calcHist(imagenes, canales, new Mat(), hist,
                numero_bins, intervalo);
//Lectura del histograma a un array de float
        hist.get(0, 0, histograma);
//Calcular xmin y xmax
        int total_pixeles = entrada.cols()*entrada.rows();
        float porcentaje_saturacion = (float) 0.05;
        int pixeles_saturados = (int) (porcentaje_saturacion * total_pixeles);
        int xmin=0;
        int xmax = 255;
        float acumulado = 0f;
        for(int n=0; n < 256; n++) { //xmin
            acumulado = acumulado + histograma[n];
            if(acumulado > pixeles_saturados) {
                xmin = n;
                break;
            }
        }
        acumulado = 0;
        for(int n = 255; n >=0; n--) { //xmax
            acumulado = acumulado + histograma[n];
            if(acumulado > pixeles_saturados) {
                xmax = n;
                break;
            }
        }
//Calculo de la salida
        Core.subtract(entrada, new Scalar(xmin) , salida);
        float pendiente = ((float) 255.0) / ((float) (xmax-xmin));
        Core.multiply(salida, new Scalar(pendiente) , salida);
        return salida;
    }

    void mitadMitad(Mat entrada, Mat salida) {
        if (entrada.channels() > salida.channels())
            Imgproc.cvtColor(salida, salida, Imgproc.COLOR_GRAY2RGBA);
        if (entrada.channels() < salida.channels())
            Imgproc.cvtColor(entrada, entrada, Imgproc.COLOR_GRAY2RGBA);
//Representar la entrada en la mitad izquierda
        Rect mitad_izquierda = new Rect();
        mitad_izquierda.x = 0; mitad_izquierda.y = 0;
        mitad_izquierda.height = entrada.height();
        mitad_izquierda.width = entrada.width()/2;
        Mat salida_mitad_izquierda = salida.submat( mitad_izquierda );
        Mat entrada_mitad_izquierda = entrada.submat( mitad_izquierda );
        entrada_mitad_izquierda.copyTo(salida_mitad_izquierda);
    }
}