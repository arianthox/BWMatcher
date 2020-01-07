
package com.arianthox.predictor.hmm.dsp;

/**
 * <b>Descripcion:</b> Clase FFT para señales Reales. N Contiene el Numero de Puntos de DFT, real[] y imaginary[]
 * Contiene la Parte real e Imaginaria de la Entrada. retorna, real[] y imaginary[] Contiene la salida DFT.
 * Todas las Señales corren desde 0 to N - 1<br>
 * <b>Entrada:</b> Señal de Habla<br>
 * <b>Salida:</b> Salida en parte real e imaginaria de DFT
 * @author Danny Su
 */
public class Fft{
    /**
     * Numero de Puntos
     */
    protected static int numPoints;
    /**
     * Parte Real
     */
    public static double real[];
    /**
     * Parte Imaginaria
     */
    public static double imag[];
    
    /**
     * Transformacion Rapida de Furier<br>
     * @param signal Señal
     */
    public static void computeFFT(double signal[]){
        numPoints = signal.length;

        // Inicializacion real & imag array
        real = new double[numPoints];
        imag = new double[numPoints];
        
        // Mueve los N puntos de Señal en la parte real de los complejos DFT's
        real = signal;
        
        // Establece todas las muestras de la parte imaginaria en Cero
        for (int i = 0; i < imag.length; i++){
            imag[i] = 0;
        }
        
        // FFT Usando el Array Real & Imaginario
        FFT();
    }
    
    /**
     * Transformacion Rapida de Furier<br>
     */
    private static void FFT(){
        if (numPoints == 1) return;

        final double pi = Math.PI;
        final int numStages = (int)(Math.log(numPoints) / Math.log(2));
        
        int halfNumPoints = numPoints >> 1;
        int j = halfNumPoints;
        
        // FFT Descomposicion por el algoritmo de "bit reversal sorting"
        int k = 0;
        for (int i = 1; i < numPoints - 2; i++){
            if (i < j){
                // Intercambio
                double tempReal = real[j];
                double tempImag = imag[j];
                real[j] = real[i];
                imag[j] = imag[i];
                real[i] = tempReal;
                imag[i] = tempImag;
            }
            
            k = halfNumPoints;
            
            while ( k <= j ){
                j -= k;
                k >>=1;
            }
            
            j += k;
        }

        // Ciclo para cada stage
        for (int stage = 1; stage <= numStages; stage++){

            int LE = 1;
            for (int i = 0; i < stage; i++)
                LE <<= 1;

            int LE2 = LE >> 1;
            double UR = 1;
            double UI = 0;
            
            // Calcula los Valores del Seno y Coseno
            double SR = Math.cos( pi / LE2 );
            double SI = -Math.sin( pi / LE2 );
            
            // Ciclo por Cada subDFT
            for (int subDFT = 1; subDFT <= LE2; subDFT++){
                
                // Cilo por Cada butterfly
                for (int butterfly = subDFT - 1; butterfly <= numPoints - 1; butterfly+=LE){
                    int ip = butterfly + LE2;
                    
                    // Calculo de butterfly
                    double tempReal = real[ip] * UR - imag[ip] * UI;
                    double tempImag = real[ip] * UI + imag[ip] * UR;
                    real[ip] = real[butterfly] - tempReal;
                    imag[ip] = imag[butterfly] - tempImag;
                    real[butterfly] += tempReal;
                    imag[butterfly] += tempImag;
                }
                
                double tempUR = UR;
                UR = tempUR * SR - UI * SI;
                UI = tempUR * SI + UI * SR;
            }
        }
    }
}