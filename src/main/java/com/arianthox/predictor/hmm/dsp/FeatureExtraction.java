
package com.arianthox.predictor.hmm.dsp;

/**
 * <b>Descripcion:</b> Clase featureExtraction usada para extraer el mel-frequency coheficiente de la Señal de Entrada<br>
 * <b>Entrada:</b>Señal de Audio<br>
 * <b>Salida:</b>Coheficiente mel-frequency
 * @author Ricardo Sanchez Delgado
 */
public class FeatureExtraction{
    /**
     * Muestra en Hz
     */
    protected final static double samplingRate = 16000.0;
    /**
     * Numero de Muestras por Cuadro
     */
    protected final static int frameLength = 512;
    /**
     * Numero de Muestras Sobrepuestas (Usualmente 50% del tamaño del Cuadro)
     */
    protected final static int shiftInterval = frameLength / 2;
    /**
     * Numero de MFCCs por Cuadro
     */
    protected final static int numCepstra = 13;
    /**
     * FFT Tamaño ()
     */
    protected final static int fftSize = frameLength;
    /**
     * Pre-Emphasis Alpha (Establece a 0 si no pre-emphasis)
     */
    protected final static double preEmphasisAlpha = 0.95;
    /**
     * Limite inferior del Filtro (o 64 Hz?)
     */
    protected final static double lowerFilterFreq = 133.3334;
    /**
     * Limite Superior del filtro (o Frecuencia Media de Muestra.?)
     */
    protected final static double upperFilterFreq = 6855.4976;
    /**
     * Numero de Filtros (SPHINX-III uses 40)
     */
    protected final static int numMelFilters = 23;
    /**
     * Total de cuadros de la Señal de Entrada
     */
    protected static double frames[][];
    /**
     * hamming valores de ventana
     */
    protected static double hammingWindow[];
    /**
     * Transformacion Rapida de Fourier
     */
    protected static Fft FFT;
    /**
     * Toma la Señal de Habla y retorna el Coheficiente Mel-Frequency Cepstral (MFCC)<br>
     * @param inputSignal Señal de Habla (16 bit integer data)
     * @return Mel Frequency Cepstral Coefficients (32 bit floating point data)
     */
    public static double[][] process(short inputSignal[]){
        double MFCC[][];

        // Pre-Emphasis
        double outputSignal[] = preEmphasis(inputSignal);
        
        // Cuadro de Bloqueo
        framing(outputSignal);

        // Inicializacion del Array MFCC
        MFCC = new double[frames.length][numCepstra];

        // Ejecucion de Hamming Window a Todos los Frames
        hammingWindow();
        
        
        for (int k = 0; k < frames.length; k++){
            FFT = new Fft();
            
            // Magnitud de Espectro
            double bin[] = magnitudeSpectrum(frames[k]);

            // Filtro
            int cbin[] = fftBinIndices();
            // Banco de Filtros
            double fbank[] = melFilter(bin, cbin);

            // Transformacion no Linear
            double f[] = nonLinearTransformation(fbank);

            // Coheficiente
            double cepc[] = cepCoefficients(f);

            // Concatena el Resultado al MFCC array
            for (int i = 0; i < numCepstra; i++){
                MFCC[k][i] = cepc[i];
            }
        }

        return MFCC;
    }
    /**
     * Calcula los FFT indices<br>
     * @return array de FFT indices
     */
    private static int[] fftBinIndices(){
        int cbin[] = new int[numMelFilters + 2];
        
        cbin[0] = (int)Math.round(lowerFilterFreq / samplingRate * fftSize);
        cbin[cbin.length - 1] = (int)(fftSize / 2);
        
        for (int i = 1; i <= numMelFilters; i++){
            double fc = centerFreq(i);

            cbin[i] = (int)Math.round(fc / samplingRate * fftSize);
        }
        
        return cbin;
    }
    /**
     * Calcula la salida del Filtro<br>
     */
    private static double[] melFilter(double bin[], int cbin[]){
        double temp[] = new double[numMelFilters + 2];

        for (int k = 1; k <= numMelFilters; k++){
            double num1 = 0, num2 = 0;

            for (int i = cbin[k - 1]; i <= cbin[k]; i++){
                num1 += ((i - cbin[k - 1] + 1) / (cbin[k] - cbin[k-1] + 1)) * bin[i];
            }

            for (int i = cbin[k] + 1; i <= cbin[k + 1]; i++){
                num2 += (1 - ((i - cbin[k]) / (cbin[k + 1] - cbin[k] + 1))) * bin[i];
            }

            temp[k] = num1 + num2;
        }

        double fbank[] = new double[numMelFilters];
        for (int i = 0; i < numMelFilters; i++){
            fbank[i] = temp[i + 1];
        }

        return fbank;
    }
    /**
     * Los Coheficientes son calculados en base a la salida de el metodo de Transformacion Linear<br>
     * @param f Salida del Metodo de Transformacion no Lineal
     * @return Coheficientes
     */
    private static double[] cepCoefficients(double f[]){
        double cepc[] = new double[numCepstra];
        
        for (int i = 0; i < cepc.length; i++){
            for (int j = 1; j <= numMelFilters; j++){
                cepc[i] += f[j - 1] * Math.cos(Math.PI * i / numMelFilters * (j - 0.5));
            }
        }
        
        return cepc;
    }
    /**
     * Ejecucion de la funcion logaritmica(natural logarithm)<br>
     * @param fbank Salida Filtrada
     * @return Logaritmo Natural del Filtro
     */
    private static double[] nonLinearTransformation(double fbank[]){
        double f[] = new double[fbank.length];
        final double FLOOR = -50;
        
        for (int i = 0; i < fbank.length; i++){
            f[i] = Math.log(fbank[i]);
            
            // Chequea si ln() retorna un valor menor que el FLOOR
            if (f[i] < FLOOR) f[i] = FLOOR;
        }
        
        return f;
    }
    /**
     * Calcula el Logaritmo en Base 10<br>
     * @param value Numero a Calcular
     * @return Logaritm en base 10 del valor de entrada
     */
    protected static double log10(double value){
        return Math.log(value) / Math.log(10);
    }
    /**
     * Calcula el Centro de Frecuencia<br>
     * @param i Indice de Filtro
     * @return Centro de Frecuencia
     */
    private static double centerFreq(int i){
        double mel[] = new double[2];
        mel[0] = freqToMel(lowerFilterFreq);
        mel[1] = freqToMel(samplingRate / 2);
        
        // Toma el Inverso:
        double temp = mel[0] + ((mel[1] - mel[0]) / (numMelFilters + 1)) * i;
        return inverseMel(temp);
    }
    /**
     * Calcula el Inverso de la Frecuencia<br>
     */
    private static double inverseMel(double x){
        double temp = Math.pow(10, x / 2595) - 1;
        return 700 * (temp);
    }
    /**
     * Convierte la frecuencia a mel-frequency<br>
     * @param freq Frecuencia
     * @return Mel-Frequency
     */
    protected static double freqToMel(double freq){
        return 2595 * log10(1 + freq / 700);
    }
    /**
     * Procesa la Magnitud de Espectro y el Cuadro de Entrada<br>
     * @param frame Cuadro de Entrada
     * @return Magnitude array de Espectros
     */
    protected static double[] magnitudeSpectrum(double frame[]){
        double magSpectrum[] = new double[frame.length];
        
        // Calcula FFT para el Cuadro Actual
        FFT.computeFFT( frame );
        
        // Calcula la Magnitud del Espectro
        for (int k = 0; k < frame.length; k++){
            magSpectrum[k] = Math.pow(FFT.real[k] * FFT.real[k] + FFT.imag[k] * FFT.imag[k], 0.5);
        }

        return magSpectrum;
    }
    /**
     * Hamming Window<br>
     * @param frame Cuadro a Procesar
     * @return Cuadro Procesado con el Algoritmo deHamming
     */
    private static void hammingWindow(){
        double w[] = new double[frameLength];
        for (int n = 0; n < frameLength; n++){
            w[n] = 0.54 - 0.46 * Math.cos( (2 * Math.PI * n) / (frameLength - 1) );
        }

        for (int m = 0; m < frames.length; m++){
            for (int n = 0; n < frameLength; n++){
                frames[m][n] *= w[n];
            }
        }
    }
    /**
     * Cuadro Bloqueado para reducir la Señal de Audio en el Cuadro<br>
     * @param Señal de Audio (16 bit integer data)
     */
    protected static void framing(double inputSignal[]){
        double numFrames = (double)inputSignal.length / (double)(frameLength - shiftInterval);
        
        // Redondeo Incondicional
        if ((numFrames / (int)numFrames) != 1){
            numFrames = (int)numFrames + 1;
        }
        
        // Se rellena de 0 los Cuadros sin Muestras
        double paddedSignal[] = new double[(int)numFrames * frameLength];
        for (int n = 0; n < inputSignal.length; n++){
            paddedSignal[n] = inputSignal[n];
        }

        frames = new double[(int)numFrames][frameLength];

        // Se reduce la señal en el cuadro con el intervalo especificado
        for (int m = 0; m < numFrames; m++){
            for (int n = 0; n < frameLength; n++){
                frames[m][n] = paddedSignal[m * (frameLength - shiftInterval) + n];
            }
        }
    }
    /**
     * pre-emphasis a equalizar amplitud por frecuencia alta y baja<br>
     * @param inputSignal Señal de Audio (16 bit integer data)
     * @return Señal de Audio despues de pre-emphasis (16 bit integer data)
     */
    protected static double[] preEmphasis(short inputSignal[]){
        double outputSignal[] = new double[inputSignal.length];
        
        // Se Aplica el pre-emphasis a cada muestra
        for (int n = 1; n < inputSignal.length; n++){
            outputSignal[n] = inputSignal[n] - preEmphasisAlpha * inputSignal[n - 1];
        }
        
        return outputSignal;
    }
}