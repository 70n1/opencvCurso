package example.org.proyectobase;

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
 * Created by AMARTIN on 07/06/2017.
 */

public class ProcesadorCaracteres {
    Mat tabla_caracteristicas;
    Mat binaria1;
    Mat binaria2;
    Mat entrada_gris;
    int NUMERO_CLASES = 10;
    int MUESTRAS_POR_CLASE = 2;
    int NUMERO_CARACTERISTICAS = 9;
    public ProcesadorCaracteres() { //Constructor
        tabla_caracteristicas = new Mat(NUMERO_CLASES* MUESTRAS_POR_CLASE,
                NUMERO_CARACTERISTICAS, CvType.CV_64FC1);
        binaria1= new Mat();
        binaria2= new Mat();
        entrada_gris = new Mat();
        crearTabla();
    }

    public Mat procesa(Mat entrada) {
        Imgproc.cvtColor( entrada, entrada_gris, Imgproc.COLOR_RGBA2GRAY);
        Rect rect_digito = new Rect();
        boolean localizado = localizarCaracter(rect_digito);
        if(localizado == false)
            return entrada.clone();
        //Recortar rectangulo en imagen original
        Mat recorte_digito = entrada_gris.submat(rect_digito);
//Binarizacion Otsu
        Imgproc.threshold(recorte_digito, binaria2, 0, 255, Imgproc.THRESH_BINARY_INV+ Imgproc.THRESH_OTSU);
//Leer numero
        int digito = leerRectangulo(binaria2);
        Mat salida = dibujarResultado(entrada, rect_digito, digito);
        return salida;
    }

    boolean localizarCaracter(Rect digit_rect) {
        int contraste = 5;
        int tamano = 7;
        Imgproc.adaptiveThreshold(entrada_gris, binaria1, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, tamano, contraste );
        List<MatOfPoint> contornos = new ArrayList< MatOfPoint >() ;
        Mat jerarquia = new Mat();
        Imgproc.findContours(binaria1, contornos, jerarquia, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE );
        int altura_minima = 30;
        int anchura_minima = 10;
        double max_area=-1;
// Seleccionar objeto mas grande
        for (int c= 0; c < contornos.size(); c++ ) {
            Rect bb = Imgproc.boundingRect( contornos.get(c) );
// Comprobar tamaño
            if (bb.width < anchura_minima || bb.height < altura_minima)
                continue;
// Descartar proximos al borde
            if(bb.x < 2 || bb.y < 2)
                continue;
            if(binaria1.width() - (bb.x + bb.width) < 3 || binaria1.height() - (bb.y + bb.height) < 3)
                continue;
// Seleccionar el mayor
            //int area = bb.width * bb.height;
            double area = bb.area();
            if( area > max_area ) {
                max_area= area;
                digit_rect.x = bb.x;
                digit_rect.y = bb.y;
                digit_rect.width = bb.width;
                digit_rect.height = bb.height;
            }
        }
        if( max_area < 0) //No se ha detectado objeto valido
            return false;
        else
            return true;
    }

    public int leerRectangulo(Mat rectangulo) {
        Mat vectorCaracteristicas = caracteristicas(rectangulo);
// Buscamos la fila de la tabla que mas se parece
        double Sumvv = vectorCaracteristicas.dot(vectorCaracteristicas);
        int nmin = 0;
        double Sumvd = tabla_caracteristicas.row(nmin).dot( vectorCaracteristicas);
        double Sumdd = tabla_caracteristicas.row(nmin).dot( tabla_caracteristicas.row(nmin));
        double D = Sumvd / Math.sqrt(Sumvv * Sumdd);
        double dmin = D;
        for(int n= 1; n< tabla_caracteristicas.rows() ; n++) {
            Sumvd = tabla_caracteristicas.row(n).dot(vectorCaracteristicas);
            Sumdd = tabla_caracteristicas.row(n).dot( tabla_caracteristicas.row(n));
            D = Sumvd / Math.sqrt(Sumvv * Sumdd);
            if( D > dmin ){
                dmin = D;
                nmin = n;
            }
        }
// A partir de la fila determinamos el numero
        nmin = nmin % 10;
        return nmin;
    }


    public Mat caracteristicas(Mat recorteDigito) { //rectangulo: imagen binaria de digito
//Convertimos a flotante doble precisión
        Mat chardouble = new Mat();
        recorteDigito.convertTo(chardouble, CvType.CV_64FC1);
//Calculamos vector de caracteristicas
        Mat digito_3x3 = new Mat();
        Imgproc.resize(chardouble, digito_3x3, new Size(3,3), 0,0, Imgproc.INTER_AREA);
// convertimos de 3x3 a 1x9 en el orden adecuado
        digito_3x3 = digito_3x3.t();
        return digito_3x3.reshape(1, 1);
    }



    Mat dibujarResultado(Mat imagen, Rect digit_rect, int digit) {
        Mat salida = imagen.clone();
        Point P1 = new Point(digit_rect.x, digit_rect.y);
        Point P2 = new Point(digit_rect.x+digit_rect.width, digit_rect.y+digit_rect.height);
        Imgproc.rectangle(salida, P1, P2, new Scalar(255,0,0) );
// Escribir numero
        int fontFace = 6;//FONT_HERSHEY_SCRIPT_SIMPLEX;
        double fontScale = 1;
        int thickness = 5;
        Imgproc.putText(salida, Integer.toString(digit ),
                P1, fontFace, fontScale,
                new Scalar(0,0,0), thickness, 8,false);
        Imgproc.putText(salida, Integer.toString(digit ),
                P1, fontFace, fontScale,
                new Scalar(255,255,255), thickness/2, 8,false);
        return salida;
    }


    void crearTabla() {
        double datosEntrenamiento[][] = new double[][]{
                new double[]{0.5757916569709778, 0.8068438172340393, 0.6094995737075806, 0.6842694878578186, 0, 0.6750765442848206, 0.573646605014801, 0.814811110496521, 0.6094995737075806},
                new double[]{ 0.5408163070678711, 0.04897959157824516, 0, 0.8428571224212646, 0.79795902967453, 0.7795917987823486, 0.9938775897026062, 1, 0.995918333530426},
                new double[]{0.7524304986000061, 0.1732638627290726, 0.697916567325592, 0.6704860925674438, 0.3805555701255798, 0.9767361283302307, 0.6843749284744263, 0.7732638716697693, 0.6086806654930115},
                new double[]{0.6724254488945007, 0, 0.6819106936454773, 0.6561655402183533, 0.5406503081321716, 0.647357702255249, 0.6775066256523132, 0.8231707215309143, 0.732723593711853},
                new double[]{0.02636498026549816, 0.6402361392974854, 0.5215936899185181, 0.7385144829750061, 0.5210034847259521, 0.6062962412834167, 0.5685194730758667, 0.6251844167709351, 0.7910475134849548},
                new double[]{0.8133208155632019, 0.550218939781189, 0.6083046793937683, 0.7753458619117737, 0.4955636858940125, 0.6764461994171143, 0.4960368871688843, 0.8128473162651062, 0.6384715437889099},
                new double[]{0.6108391284942627, 0.985664427280426, 0.5884615778923035, 0.7125874161720276, 0.5996503829956055, 0.6629370450973511, 0.4828671216964722, 0.7608392238616943, 0.6695803999900818},
                new double[]{0.6381308436393738, 0, 0.1727102696895599, 0.7140188217163086, 0.5850467085838318, 0.8407476544380188, 0.943925142288208, 0.4654205441474915, 0.02728971838951111},
                new double[]{0.6880735158920288, 0.8049609065055847, 0.7363235950469971, 0.6299694776535034, 0.672782838344574, 0.6411824822425842, 0.6687054634094238, 0.7784574031829834, 0.7037037014961243},
                new double[]{0.6497123241424561,0.7168009877204895, 0.4542001485824585, 0.6476410031318665, 0.6150747537612915, 0.7033372521400452, 0.5941311717033386, 0.9686998724937439, 0.5930955410003662},
                new double[]{0.6764705777168274, 1, 0.7450980544090271, 0.7091502547264099, 0.05228758603334427, 0.6993464231491089, 0.6339869499206543, 0.9934640526771545, 0.7058823704719543},
                new double[]{0.3452012538909912, 0.3885449171066284, 0, 0.7770897746086121, 0.6501547694206238, 0.5789474248886108, 1, 1, 1},
                new double[]{0.6407563090324402, 0.06722689419984818, 0.7825630307197571, 0.7132352590560913, 0.6365545988082886, 0.9222689270973206, 0.7226890921592712, 0.5850840210914612, 0.7058823704719543},
                new double[]{0.5980392098426819, 0, 0.6666666865348816, 0.686274528503418, 0.5751633644104004, 0.6111111640930176, 0.6111112236976624, 0.7516340017318726, 0.7647058963775635},
                new double[]{0.03549695760011673, 0.717038631439209, 0.4705882370471954, 0.7474644780158997, 0.7109533548355103, 0.6531440615653992, 0.5862069725990295, 0.6744422316551208, 0.780933141708374},
                new double[]{0.6201297640800476, 0.5129870772361755, 0.5876624584197998, 0.7207792997360229, 0.5844155550003052, 0.6168831586837769, 0.5389610528945923, 0.8214285969734192, 0.7435064911842346},
                new double[]{0.6176470518112183, 1, 0.6764706373214722, 0.6699347496032715, 0.601307213306427, 0.6405228972434998, 0.5098039507865906, 0.7647058963775635, 0.8039215803146362},
                new double[]{0.7272727489471436, 0.0202020201832056, 0.2727272808551788, 0.8383838534355164, 0.8181818127632141, 0.7272727489471436, 0.8989898562431335, 0.1616161614656448, 0},
                new double[]{0.6928104758262634, 0.8071895837783813, 0.8333333134651184, 0.6764705777168274, 0.7026143074035645, 0.6209149956703186, 0.6601307392120361, 0.7712417840957642, 0.7941176891326904},
                new double[]{0.7320261597633362, 0.8202614784240723, 0.5653595328330994, 0.6503268480300903, 0.5882353186607361, 0.6732026338577271, 0.6045752167701721, 0.9869281649589539, 0.6339869499206543}};
        for (int i=0;i<20;i++)
            tabla_caracteristicas.put(i, 0, datosEntrenamiento [i]);
    }
}
