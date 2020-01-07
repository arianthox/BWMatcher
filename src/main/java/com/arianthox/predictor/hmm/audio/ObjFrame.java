
package com.arianthox.predictor.hmm.audio;

/**
 * <b>Descripcion:</b> almacena 100ms de muestra (1600 muestras), y las procesa<br>
 * <b>Entrada:</b> 100ms Señal<br>
 * <b>Salida:</b> 100ms Señal
 * @author Keith Fung
 */

public class ObjFrame{
    final int BUFFER_SIZE = 3200;
    private short sample[] = new short[(int)(BUFFER_SIZE / 2)];
    private double avgEnergy = 0;

    /**
     * Construcor<br>
     * @param bufferRead 100ms Señal de Habla
     */
    public ObjFrame(short bufferRead[]){

        double runningSum = 0;
        for (int c = 0; c < sample.length; c++){
            sample[c] = bufferRead[c];
            runningSum += bufferRead[c] * bufferRead[c];
        }
        avgEnergy = runningSum / (BUFFER_SIZE / 2);
    }

    /**
     * @return Energia Procesada
     */
    public double getAvgEnergy(){
        return avgEnergy;
    }

    /**
     * Retorna la energia de 100ms<br>
     * @return 100ms Señal de Audio
     */
    public short[] getSample(){
        return sample;
    }
}
