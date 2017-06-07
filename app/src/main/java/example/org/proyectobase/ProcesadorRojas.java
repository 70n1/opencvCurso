package example.org.proyectobase;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AMARTIN on 29/05/2017.
 */

public class ProcesadorRojas {
    Mat red;
    Mat green;
    Mat blue;
    Mat maxGB;
    public ProcesadorRojas() { //Constructor
        red = new Mat();
        green = new Mat();
        blue = new Mat();
        maxGB = new Mat();
    }
    public Mat procesa(Mat entrada) {
        Mat binaria = new Mat();
        Core.extractChannel(entrada, red, 0);
        Core.extractChannel(entrada, green, 1);
        Core.extractChannel(entrada, blue, 2);
        Core.max(green, blue, maxGB);
        Core.subtract(red, maxGB, binaria);
        Core.MinMaxLocResult minMax = Core.minMaxLoc(binaria);
        int maximum = (int) minMax.maxVal;
        int thresh = maximum / 4;
        Imgproc.threshold(binaria, binaria, thresh, 255, Imgproc.THRESH_BINARY);


        List<MatOfPoint> blobs = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Mat salida = binaria.clone();//Copia porque finContours modifica entrada
        Imgproc.cvtColor(salida, salida, Imgproc.COLOR_GRAY2RGBA);
        Imgproc.findContours(binaria, blobs, hierarchy, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_NONE);
        int minimumHeight = 30;
        float maxratio = (float) 0.75;

        ArrayList<Point[]> listaPuntos = new ArrayList<Point[]>();
// Seleccionar candidatos a circulos
        for (int c = 0; c < blobs.size(); c++) {
            double[] data = hierarchy.get(0, c);
            int parent = (int) data[3];
            if (parent < 0) //Contorno exterior: rechazar
                continue;
            Rect BB = Imgproc.boundingRect(blobs.get(c));
// Comprobar tamaño
            if (BB.width < minimumHeight || BB.height < minimumHeight)
                continue;
// Comprobar anchura similar a altura
            float wf = BB.width;
            float hf = BB.height;
            float ratio = wf / hf;
            if (ratio < maxratio || ratio > 1.0 / maxratio)
                continue;
// Comprobar no está cerca del borde
            if (BB.x < 2 || BB.y < 2)
                continue;
            if (entrada.width() - (BB.x + BB.width) < 3 || entrada.height() - (BB.y + BB.height) < 3)
                continue;


// Aqui cumple todos los criterios. Dibujamos
            final Point P1 = new Point(BB.x, BB.y);
            final Point P2 = new Point(BB.x + BB.width, BB.y + BB.height);
            Imgproc.rectangle(salida, P1, P2, new Scalar(0, 0, 255));


        }

        return salida;
    }


    public Rect localizarCirCuloRojo(Mat entrada) {

        Rect rectCirculo = new Rect();
        Mat binaria = new Mat();
        Core.extractChannel(entrada, red, 0);
        Core.extractChannel(entrada, green, 1);
        Core.extractChannel(entrada, blue, 2);
        Core.max(green, blue, maxGB);
        Core.subtract(red, maxGB, binaria);

        Core.MinMaxLocResult minMax = Core.minMaxLoc(binaria);
        int maximum = (int) minMax.maxVal;
        int thresh = maximum / 4;

        Imgproc.threshold(binaria, binaria, thresh, 255, Imgproc.THRESH_BINARY);


        List<MatOfPoint> blobs = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Mat salida = binaria.clone();//Copia porque finContours modifica entrada
        Imgproc.cvtColor(salida, salida, Imgproc.COLOR_GRAY2RGBA);
        Imgproc.findContours(binaria, blobs, hierarchy, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_NONE);
        int minimumHeight = 30;
        float maxratio = (float) 0.75;

        ArrayList<Point[]> listaPuntos = new ArrayList<Point[]>();
// Seleccionar candidatos a circulos
        for (int c = 0; c < blobs.size(); c++) {
            double[] data = hierarchy.get(0, c);
            int parent = (int) data[3];
            if (parent < 0) //Contorno exterior: rechazar
                continue;
            Rect BB = Imgproc.boundingRect(blobs.get(c));
// Comprobar tamaño
            if (BB.width < minimumHeight || BB.height < minimumHeight)
                continue;
// Comprobar anchura similar a altura
            float wf = BB.width;
            float hf = BB.height;
            float ratio = wf / hf;
            if (ratio < maxratio || ratio > 1.0 / maxratio)
                continue;
// Comprobar no está cerca del borde
            if (BB.x < 2 || BB.y < 2)
                continue;
            if (entrada.width() - (BB.x + BB.width) < 3 || entrada.height() - (BB.y + BB.height) < 3)
                continue;


// Aqui cumple todos los criterios. Dibujamos
            final Point P1 = new Point(BB.x, BB.y);
            final Point P2 = new Point(BB.x + BB.width, BB.y + BB.height);
            Imgproc.rectangle(salida, P1, P2, new Scalar(0, 0, 255));

            rectCirculo = new Rect(P1,P2);

            return rectCirculo;
        }
        return rectCirculo;
    }

        public Mat procesaNumeros(Mat entrada) { //entrada: imagen color



            Rect rectCirculo = localizarCirCuloRojo(entrada);
            if (rectCirculo.width>0) {
                entrada = segmentarInteriorDisco(entrada, rectCirculo);
            } else {
                return entrada.clone();
            }
            return entrada;
        }

    private Mat segmentarInteriorDisco(Mat entrada, Rect rectCirculo) {
        Mat salida = entrada.clone();
        Mat recorte_digito = red.submat(rectCirculo);
//Binarizacion Otsu
        Imgproc.threshold(recorte_digito, recorte_digito, 0, 255, Imgproc.THRESH_BINARY_INV+ Imgproc.THRESH_OTSU);

        List<MatOfPoint> blobs = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(recorte_digito, blobs, hierarchy, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_NONE);
        int minimumHeight = 12;
        float maxratio = (float) 0.75;

// Seleccionar candidatos a circulos
        for (int c = 0; c < blobs.size(); c++) {
            double[] data = hierarchy.get(0, c);
            int parent = (int) data[3];
            //if (parent < 0) //Contorno exterior: rechazar
             //   continue;
            Rect BB = Imgproc.boundingRect(blobs.get(c));
// Comprobar tamaño
            if ((BB.height<(rectCirculo.height/3)) || BB.height <= minimumHeight || BB.height <= BB.width)
                continue;

            if ((BB.x ==0 ) || BB.y ==0 || (BB.x + BB.width) >= (rectCirculo.width)  || (BB.y + BB.height) >= (rectCirculo.height))
                continue;
// Comprobar anchura similar a altura
            float wf = BB.width;
            float hf = BB.height;
            float ratio = wf / hf;
            //if (ratio < maxratio || ratio > 1.0 / maxratio)
             //   continue;
// Comprobar no está cerca del borde
            //if (BB.x < 2 || BB.y < 2)
            //    continue;
            //if (entrada.width() - (BB.x + BB.width) < 3 || entrada.height() - (BB.y + BB.height) < 3)
             //   continue;


// Aqui cumple todos los criterios. Dibujamos
            final Point P1 = new Point(BB.x + rectCirculo.x, BB.y + rectCirculo.y);
            final Point P2 = new Point(BB.x + BB.width + rectCirculo.x, BB.y + BB.height + rectCirculo.y);
            Imgproc.rectangle(salida, P1, P2, new Scalar(255, 0, 0));


        }

        return salida;
    }

}
