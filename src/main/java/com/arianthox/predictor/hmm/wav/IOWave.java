package com.arianthox.predictor.hmm.wav;

import javax.sound.sampled.*;
import java.io.*;

/**
 * <b>Descripcion:</b> Entrada y Salida de Archivos de Audio<br>
 * <b>Entrada:</b> Señal de Voz, Archivo<br>
 * <b>Salida:</b> ninguna<br>
 * @author Ricardo Sanchez Delgado
 */
public class IOWave{
    /**
     * Metodo de codificacion "pulse-code modulation"  codificacion Predefinida
     */
    final static AudioFormat.Encoding SAMPLE_ENCODING = AudioFormat.Encoding.PCM_SIGNED;
    /**
     * Captura una muestra de 16KHz, puede ser modificado por el Constructor
     */
    final static float SAMPLE_RATE = 16000.0F;
    /**
     * muestra de 16 bits
     */
    final static int SAMPLE_BITS = 16;
    /**
     * Monofonico
     */
    final static int SAMPLE_CHANNELS = 1;
    /**
     * Rata = 16000
     */
    final static float SAMPLE_FRAME_RATE = 16000.0F;
    /**
     * Tamaño = 2
     */
    final static int SAMPLE_FRAME_SIZE = 2;
    /**
     * 00000100 00000001<br>
     * Primer byte 00000001<br>
     * Segundo byte 00000100
     */
    final static boolean SAMPLE_BIG_ENDIAN = false;    
    /**
     * Formato de Audio
     */
    final static AudioFormat FORMAT = new AudioFormat(SAMPLE_ENCODING, SAMPLE_RATE,SAMPLE_BITS,SAMPLE_CHANNELS,SAMPLE_FRAME_SIZE,SAMPLE_FRAME_RATE,SAMPLE_BIG_ENDIAN);

    /**
     * Escribe el Archivo de Audio<br>
     * @param sample datos en formato de 16 bits
     * @param path localizacion destino
     */
    public static void writeWave(short sample[], String path){
        //Entrada de datos al archivo de audio

        byte sampleByte[] = new byte[sample.length * 2];
        
        for (int c=0; c < sample.length; c++){
            sampleByte[2 * c] = (byte)sample[c];
            sampleByte[2 * c + 1] = (byte)(sample[c]>>8);
        }


        try {
            ByteArrayInputStream sampleByteArrayInputStream = new ByteArrayInputStream(sampleByte);
            AudioInputStream sampleAudioInputStream = new AudioInputStream (sampleByteArrayInputStream, FORMAT, sampleByte.length / SAMPLE_FRAME_SIZE);
            if (AudioSystem.write(sampleAudioInputStream,AudioFileFormat.Type.WAVE, new File(path + ".wav")) == -1){
                System.out.println("Unable to write to file");
            }
        }
        catch (Exception e){
            e.printStackTrace();               
        }
    }

///////////////////////////////////////////////////////////////

    /**
     * Lectura del Archivo de Audio<br>
     * @param Archivo de Audio
     * @return short array de un tamaño indeterminado que contiene la amplitud del archivo wave
     */
    public static short[] readWave(String path){


        /**
         * Define el Objeto File con la Localizacion dada
         */
        File fileRead = new File(path);

        /**
         * Tamaño inicial del Buffer
         */
        int byteRead = 16000 * 2;

        /**
         * array usado como almacenamiento temporal de los datos leidos
         */
        byte waveByte[] = new byte[byteRead];

        /**
         * array usado temporalmente para los datos retornados
         */
        short waveShort[];

        /**
         * almacena el numero de bytes leidos del archivo de audio
         */
        int numByteRead;

        try {
            /**
             * Byte array usado para almacenar los datos leidos del archivo de audio
             */
            ByteArrayOutputStream readByteArrayOutputStream = new ByteArrayOutputStream();

            /**
             * Abre el archivo de audio
             */
            AudioInputStream readAudioInputStream = AudioSystem.getAudioInputStream(fileRead);

            while ((numByteRead = readAudioInputStream.read(waveByte, 0, waveByte.length)) != -1){
                readByteArrayOutputStream.write(waveByte,0,numByteRead);
            }

            /**
             * el array almacena temporalmente los datos leidos
             */
            byte tempWaveByte[] = readByteArrayOutputStream.toByteArray();

            waveShort = new short[tempWaveByte.length / 2];

            //se Convierte a 2 Bytes dentro de un Short
            for (int c = 0 ; c < waveShort.length ; c++){
                waveShort[c] = (short)((tempWaveByte[2 * c + 1] << 8) + (tempWaveByte[2 * c] >= 0 ? tempWaveByte[2 * c] : tempWaveByte[2 * c] + 256));
            }
            return waveShort;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        waveShort = new short[1];
        return waveShort;
    }
}