package example.org.proyectobase;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AMARTIN on 30/05/2017.
 */

public class ProcesadorFiltros {
    Mat paso_bajo;
    Mat Gx;
    Mat Gy;
    Mat ModGrad;
    Mat AngGrad;
    Mat red;


    public ProcesadorFiltros() {
        paso_bajo= new Mat();
        Gx= new Mat();
        Gy= new Mat();
        ModGrad= new Mat();
        AngGrad= new Mat();
        red = new Mat();
    }

    public Mat procesaFiltroAlto(Mat entrada) {
        Mat salida = new Mat();
        int filter_size = 17;
        Size s=new Size(filter_size,filter_size);
        Imgproc.blur(entrada, paso_bajo, s);
// Hacer la resta. Los valores negativos saturan a cero
        Core.subtract(paso_bajo, entrada, salida);
//Aplicar Ganancia para ver mejor. La multiplicacion satura
        Scalar ganancia = new Scalar(2);
        Core.multiply(salida, ganancia, salida);
        return salida;
    }

    public Mat procesa(Mat entrada) {

        Core.extractChannel(entrada, red, 0);

        Imgproc.Sobel(red, Gx, CvType.CV_32FC1, 1, 0); //Derivada primera rto x
        Imgproc.Sobel(red, Gy, CvType.CV_32FC1, 0, 1); //Derivada primera rto y

        // Core.cartToPolar(Gx, Gy, ModGrad, AngGrad);
        //return ModGrad;

        Core.convertScaleAbs(Gx, ModGrad);
        Core.convertScaleAbs(Gy, AngGrad);
        Mat salida = new Mat();
        Core.addWeighted(ModGrad, 0.5, AngGrad, 0.5, 0, salida);
        return salida;


/*
        Mat Gx2 = new Mat();
        Mat Gy2 = new Mat();
        Core.multiply(Gx, Gx , Gx2); //Gx2 = Gx*Gx elemento a elemento
        Core.multiply(Gy, Gy , Gy2); //Gy2 = Gy*Gy elemento a elemento
        Mat ModGrad2 = new Mat();
        Core.add( Gx2 , Gy2, ModGrad2);
        Mat ModGrad = new Mat();
        Core.sqrt(ModGrad2,ModGrad);
        return ModGrad;*/
    }

    public Mat procesaDilatacion(Mat entrada) {
        double tam = 3;
        Mat SE = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(tam,tam));
        Mat gray_dilation = new Mat(); // Result
       // Mat gray_erosion = new Mat(); // Result

        Mat grad = new Mat();
        Imgproc.dilate(entrada, gray_dilation, SE ); // 3x3 dilation
        Core.subtract(gray_dilation, entrada, grad);


        Mat binaria = new Mat();
        int contraste = 2;
        int tamano = 7;
        Imgproc.adaptiveThreshold(grad, binaria,255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, tamano, -contraste );


        List<MatOfPoint> blobs = new ArrayList< MatOfPoint >() ;
        Mat hierarchy = new Mat();
        Mat salida = binaria.clone();//Copia porque finContours modifica entrada
        Imgproc.cvtColor(salida, salida, Imgproc.COLOR_GRAY2RGBA);
        Imgproc.findContours(binaria, blobs, hierarchy, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_NONE );
        int minimumHeight = 30;
        float maxratio = (float) 0.75;
// Seleccionar candidatos a circulos
        for (int c= 0; c< blobs.size(); c++ ) {
            double[] data = hierarchy.get(0,c);
            int parent = (int) data[3];
            if(parent < 0) //Contorno exterior: rechazar
                continue;
            Rect BB = Imgproc.boundingRect(blobs.get(c) );
// Comprobar tamaño
            if ( BB.width < minimumHeight || BB.height < minimumHeight)
                continue;
// Comprobar anchura similar a altura
            float wf = BB.width;
            float hf = BB.height;
            float ratio = wf / hf;
            if(ratio < maxratio || ratio > 1.0/maxratio)
                continue;
// Comprobar no está cerca del borde
            if(BB.x < 2 || BB.y < 2)
                continue;
            if(entrada.width() - (BB.x + BB.width) < 3 || entrada.height() - (BB.y + BB.height) < 3)
                continue;
// Aqui cumple todos los criterios. Dibujamos
            final Point P1 = new Point(BB.x, BB.y);
            final Point P2 = new Point(BB.x+BB.width, BB.y+BB.height);
            Imgproc.rectangle(salida, P1, P2, new Scalar(255,0,0) );
        } // for
        return salida;
    }
}
