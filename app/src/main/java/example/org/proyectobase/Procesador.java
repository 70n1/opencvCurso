package example.org.proyectobase;

import org.opencv.core.Mat;

/**
 * Created by AMARTIN on 29/05/2017.
 */

public class Procesador {
    public Procesador() { //Constructor
    }
    public Mat procesa(Mat entrada) {
        Mat salida = entrada.clone();
        return salida;
    }
}