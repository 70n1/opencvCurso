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
        Core.subtract( red , maxGB , binaria );
        Core.MinMaxLocResult minMax = Core.minMaxLoc(binaria);
        int maximum = (int) minMax.maxVal;
        int thresh = maximum / 4;
        Imgproc.threshold(binaria, binaria, thresh, 255, Imgproc.THRESH_BINARY);


        List<MatOfPoint> blobs = new ArrayList< MatOfPoint >() ;
        Mat hierarchy = new Mat();
        Mat salida = binaria.clone();//Copia porque finContours modifica entrada
        Imgproc.cvtColor(salida, salida, Imgproc.COLOR_GRAY2RGBA);
        Imgproc.findContours(binaria, blobs, hierarchy, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_NONE );
        int minimumHeight = 30;
        float maxratio = (float) 0.75;

        ArrayList<Point[]> listaPuntos = new ArrayList<Point[]>();
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
            Imgproc.rectangle(salida, P1, P2, new Scalar(0,0,255) );


        }


        return salida;
    }
}
