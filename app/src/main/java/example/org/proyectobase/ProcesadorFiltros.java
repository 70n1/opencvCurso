package example.org.proyectobase;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

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

        Mat salida = new Mat();
        int contraste = 2;
        int tamano = 7;
        Imgproc.adaptiveThreshold(grad, salida,255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, tamano, -contraste );
        return  salida;
    }
}
