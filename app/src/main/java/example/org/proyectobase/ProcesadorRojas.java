package example.org.proyectobase;

import org.opencv.core.Core;
import org.opencv.core.Mat;

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
        Mat salida = new Mat();
        Core.extractChannel(entrada, red, 0);
        Core.extractChannel(entrada, green, 1);
        Core.extractChannel(entrada, blue, 2);
        Core.max(red, blue, maxGB);
        Core.subtract( green , maxGB , salida );
        return salida;
    }
}
