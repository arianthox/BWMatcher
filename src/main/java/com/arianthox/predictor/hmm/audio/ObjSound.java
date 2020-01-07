
package com.arianthox.predictor.hmm.audio;

/**
 * <b>Descripcion:</b> Convina los Cuadros<br>
 * <b>calls:</b> none<br>
 * <b>called by:</b> micInput<br>
 * <b>input:</b> speech signal<br>
 * <b>output:</b> speech signal
 * @author Keith Fung
 */

public class ObjSound{

    private short sound[];
    private int curPos = 0;

    /**
     * Constructor que crea un nuevo short array
     * @param totalSample Tamaño del Array
     */
    public ObjSound(int totalSample){
        sound = new short[totalSample];
    }

    /**
     * Combinacion de muestras
     * @param Muestra de señal de audio
     * @param sampleLength Tamaño de la muestra
     */
    public void addSound(short sample[], int sampleLength){
        for (int c = 0; c < sampleLength; c++){
            sound[c + curPos] = sample[c];
        }
        curPos += sampleLength;
    }
    /**
     * Retorna la señal de audio con el tiempo dado
     * @param IndexNum Tiempo/Indice
     * @return Amplitud para el tiempo especificado
     */
    public short getSoundAt(int IndexNum){
        return sound[IndexNum];
    }
    /**
     * Retorna la Señal
     * @return Señal
     */
    public int getSoundSize(){
        return sound.length;
    }
    
    /**
     * Retorna la Señal de Audio
     * @return Señal de Audio
     */
    public short[] getSound(){
        return sound;
    }
}