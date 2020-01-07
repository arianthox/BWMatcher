
package com.arianthox.predictor.hmm.dsp;

/**
 * <b>Descripcion:</b> Detecta el Ruido y busca la Ocurrencia de la Palabra actual,
 * para remover el Ruido.<br>
 * <b>Entrada:</b>Señal de Habla<br>
 * <b>Salida:</b>Señal de Habla Modificada
 * @author Ricardo Sanchez Delgado
 */
public class EndPt{

    final static int frameSize = 80;


    public static short[] absCut(short sample[]){
        if (sample.length > 3200){
            boolean crossingBoolean[] = zeroCrossingBoolean(sample);
            int energy[] = avgEnergy(sample);
            int crossing[] = zeroCrossing(sample,energy,crossingBoolean);
            short chopped[] = chopping (sample, crossing);
            return chopped;
        }    
        else{
            return sample;
        }
    }

    /**
     * Corta la muestra en pequeñas Partes<br>
     * @param sample Muestra para ser cortada
     * @param cut
     * @return Muestra Cortada
     */
    public static short[] chopping(short sample[], int cut[]){

        cut[0] *= frameSize;
        cut[1] *= frameSize;
        short chopFile[] = new short[cut[1] - cut[0]];
        for (int c = 0; c < chopFile.length; c++){
            chopFile[c] = sample[cut[0] + c];
        }
        
        return chopFile;
    }

    /**
     * 5ms Cuadro (80 Muestras / Cuadro)<br>
     * Determina el Ruido<br>
     * @param sample Muestra para ser Analizada
     * @return Energia
     */
    public static int[] avgEnergy(short sample[]){
        final double energyConst = 1.95;
        
        //[0] = Inicio del Corte, [1] = Fin del Corte
        int energyCut[] = new int[2];

        //Elimina el Fin del Cuadro, Alta probabilidad de ser Ruido
        double energyFrame[] = new double[(int)(sample.length / frameSize)];

        //temp Variable de Suma
        double runningSum = 0;

        //avg Energia de los Primeros 100ms
        double noiseEnergy = 0;

        //Indice para la Deteccion de la Fuerza de Energia
        double noiseEnergyThreshold = 0;

        int location = 0;
        int backwardLocation = 0;
        //Bandera para Determinar el Nivel de Energia
        boolean belowThreshold = true;
        boolean valleyFound = true;

        for (int c = 0; c < energyFrame.length; c++){
            runningSum = 0;
            for(int d = c * frameSize; d < (c + 1) * frameSize; d++){
                runningSum += (sample[d] * sample[d]);
            }
            energyFrame[c] = runningSum / frameSize;
        }
        runningSum = 0;
        for (int c = 0; c < 20; c++){
            runningSum += energyFrame[c];
        }
        noiseEnergy = runningSum / 20;
        noiseEnergyThreshold = noiseEnergy * energyConst;
        energyCut[1] = energyFrame.length - 22;  
        energyCut[0] = 20;



        //Busca el Inicio del Corte
        location = 20;    //Primer Cuadro
        belowThreshold = true;
        while((location < (energyFrame.length - 36)) && (belowThreshold)){

            if (energyFrame[location] > noiseEnergyThreshold){

                runningSum = 0;    //Porcentaje de los Siguientes 16 Cuadros(80ms) de ser mas altos que la Energia
                for (int c = 1; c < 17; c++){
                    if (energyFrame[location + c] > noiseEnergyThreshold){
                        runningSum++;
                    }
                }

                if (runningSum >= 13){
                    belowThreshold = false;
                    energyCut[0] = location;

                    //Bandera de Busqueda
                    valleyFound = true;
                    backwardLocation = 0;
                    while((valleyFound) && (backwardLocation < 16) && ((location - backwardLocation) > 20)){

                        if (energyFrame[location - backwardLocation - 1] < energyFrame[location - backwardLocation]){
                            //Establece el Nuevo punto de Corte
                            energyCut[0] = location - backwardLocation - 1;
                        }
                        else{
                            valleyFound = false;
                        }
                        backwardLocation++;

                    }

                }

            }
            location++;
        }


        //Busca el Fin del Corte

        //Procesamiento de los 100ms finales de la Muestra
        runningSum = 0;
        for (int c = energyFrame.length - 21; c < energyFrame.length; c++){
            runningSum += energyFrame[c];
        }
        noiseEnergy = runningSum / 20;
        noiseEnergyThreshold = noiseEnergy * energyConst;
        energyCut[1] = energyFrame.length - 22; 


        location = energyFrame.length - 22;    //Ultimo Cuadro
        belowThreshold = true;
        while((location > 35) && (belowThreshold)){
            if (energyFrame[location] > noiseEnergyThreshold){
                runningSum = 0;        //Porcentaje de los Siguientes 16 Cuadros(80ms) de ser mas altos que la Energia
                for (int c = 1; c < 17; c++){
                    if (energyFrame[location - c] > noiseEnergyThreshold){
                        runningSum++;
                    }
                }
                if (runningSum >= 13){
                    belowThreshold = false;
                    energyCut[1] = location;

                    //Bandera de Busqueda
                    valleyFound = true;
                    backwardLocation = 0;
                    while((valleyFound) && (backwardLocation < 16) && ((location + backwardLocation) < (energyFrame.length - 22))){

                        if (energyFrame[location + backwardLocation + 1] < energyFrame[location + backwardLocation]){
                            //Establece el Nuevo punto de Corte
                            energyCut[1] = location + backwardLocation + 1;
                        }
                        else{
                            valleyFound = false;
                        }
                        backwardLocation++;
                    }
                    energyCut[1]++;    //Incluye todo el Ultimo Cuadro

                }
            }
            location--;
        }
        return energyCut;
    }
    
    /**
     * Calcula el avg de la Energia para 5ms Cuadros<br>
     * @param sample Señal de Habla
     * @return array cuadrantes de energia para 5ms
     */
    private static double[] energyGraph(short sample[]){
        double runningSum = 0;
        //Elimina el Cuadro, Alta probabilidad de ser Ruido
        double energyFrame[] = new double[(int)(sample.length / frameSize)];

        for (int c = 0; c < energyFrame.length; c++){
            runningSum = 0;
            for(int d = c * frameSize; d < (c + 1) * frameSize; d++){
                runningSum += (sample[d] * sample[d]);
            }
            energyFrame[c] = runningSum / frameSize;
        }
        return energyFrame;
    }

    /**
     * Desactiva la localizacion de corte<br>
     * @return boolean array con el mismo tamaño de la muestra, true = a zero-crossing, false = no zero-crossing
     * @param sample señal de habla
     */
    public static boolean[] zeroCrossingBoolean(short sample[]){
        boolean crossingBoolean[] = new boolean[sample.length];
        int trues=0;
        for (int c = 0; c < sample.length - 1; c++){
            if (((sample[c] > 0) && (sample[c + 1] < 0)) || ((sample[c] < 0) && (sample[c + 1] > 0))){
                crossingBoolean[c] = true;
                //System.out.println("Cambio:"+crossingBoolean[c]+"-"+sample[c]+"-"+sample[c+1]);
                trues++;
            }
        }
        //System.out.println("Trues:"+trues);
        return crossingBoolean;
    }

    /**
     * Busca el punto final basado en zero-crossing y el resultado del avgEnergy<br>
     * @param sample Señal de Habla
     * @param energy resultado del avgEnergy, el cual es usado como punto inicial
     * @param crossing resultado del zeroCrossingBoolean
     * @return Inicii y Fin de la señal de Voz
     */
    public static int[] zeroCrossing(short sample[], int energy[], boolean crossing[]){
        final double crossingConst = 12.5;

        int crossingCut[] = new int[2];
        crossingCut[0] = energy[0];
        crossingCut[1] = energy[1] - 1;
        
        double crossingFrame[] = new double [(int)(sample.length/frameSize)];

        double crossingSD = 0;    //Estandar de Desviacion
        double IZC = 0;    //average zero-crossing
        double IZCT = 0.15625;    //zero-crossing fuerza

        int crossingPeak = 0;    //Punto maximo de localizacion por 80ms

        double runningSum = 0;

        int location = 0;
        
        for (int c = 0; c < crossingFrame.length; c++){
            runningSum = 0;
            for (int d = c * frameSize; d < (c + 1) * frameSize; d++){
                if (crossing[d]){
                    runningSum++;
                }
            }
            crossingFrame[c] = runningSum / frameSize;
        }

        //Avg
        runningSum = 0;
        for (int c = 0; c < 20; c++){
            runningSum += crossingFrame[c];
        }
        IZC = runningSum / 20;
        
        //Estandar de Desviacion
        runningSum = 0;
        for (int c = 0; c < 20; c++){
            runningSum += crossingFrame[c] * crossingFrame[c];
        }
        crossingSD = Math.sqrt((runningSum / 20) - (IZC * IZC));

        //Chekea si 25/10ms < SD o vice versa
        if ((0.15625) > (IZC * 2 * crossingSD)){
            IZCT = IZC * 2 * crossingSD;
        }
                
        IZCT *= crossingConst;    //Fuerza

//////////////////////////////////
        //Busca un Pico en 80 ms y conpara con la fuerza en 16 Cuadros
        //Inicio de la busqueda de el Pico
        location = crossingCut[0] - 16;
        if (location < 20){
            location = 20;
        }

        crossingPeak = location;
        while(location != crossingCut[0]){
            if (crossingFrame[crossingPeak] < crossingFrame[location]){
                crossingPeak = location;
            }
            location++;
        }     

        //Chequea si el pico es un Intervalo Muerto
        if (IZCT < crossingFrame[crossingPeak]){
            crossingCut[0] = crossingPeak;
            //Busqueda Global del  minimo para los proximos 50ms
            location = crossingCut[0] - 10;
            if (location < 20){
                location = 20;
            }

            crossingPeak = location;
            while(location != crossingCut[0]){
                if (crossingFrame[crossingPeak] >= crossingFrame[location]){
                    crossingPeak = location;
                }
                location++;
            }
            crossingCut[0] = crossingPeak;

        }

///////////////////////////////////
        //Usa los ultimos 100ms como ruido and y ejecuta el zero-crossing
        //cal the avg
        runningSum = 0;
        for (int c = crossingFrame.length - 21; c < crossingFrame.length; c++){
            runningSum += crossingFrame[c];

        }

        IZC = runningSum / 20;
        
        //Desviacion Estandar
        runningSum = 0;
        for (int c = crossingFrame.length - 21; c < crossingFrame.length; c++){
            runningSum += crossingFrame[c] * crossingFrame[c];

        }
        crossingSD = Math.sqrt((runningSum / 20) - (IZC * IZC));

        IZCT = 0.15625;    //25 / 160
        //Chequea si 25/10ms < SD o Vice versa
        if ((0.15625) > (IZC * 2 * crossingSD)){
            IZCT = IZC * 2 * crossingSD;
        }
                
        IZCT *= crossingConst;    //Fuerza

////////////////////////////////////////
        //Fin de la Busqueda del Pico para 80ms
        location = crossingCut[1] + 16;

        if (location > crossingFrame.length - 22){
            location = crossingFrame.length - 22;
        }

        crossingPeak = location;
        while(location != crossingCut[1]){
            if (crossingFrame[crossingPeak] < crossingFrame[location]){
                crossingPeak = location;
            }
            location--;
        }     

        //Verifica el Cuadro
        if (IZCT < crossingFrame[crossingPeak]){
            crossingCut[1] = crossingPeak;
            //Busqueda Global por el minimo de 50ms
            location = crossingCut[1] + 10;
            if (location > crossingFrame.length - 22){
                location = crossingFrame.length - 22;
            }
            crossingPeak = location;
            while(location != crossingCut[1]){
                if (crossingFrame[crossingPeak] >= crossingFrame[location]){
                    crossingPeak = location;
                }
                location--;
            }     
            crossingCut[1] = crossingPeak;

        }
        crossingCut[1]++;

        return crossingCut;
    }

    /**
     * Calcula el zero-crossing por 5ms Cuadros<br>
     * @param sample Señal de Habla
     * @return array de zero-crossing para cada 5ms
     */
    private static double[] crossingGraph(short sample[]){

        boolean crossing[] = zeroCrossingBoolean(sample);
        double crossingFrame[] = new double[(int)(sample.length/ frameSize)];
        double runningSum = 0;
        
        for (int c = 0; c < crossingFrame.length; c++){
            runningSum = 0;
            for (int d = c * frameSize; d < (c + 1) * frameSize; d++){
                if (crossing[d]){
                    runningSum++;
                }
            }
            crossingFrame[c] = runningSum / frameSize;
        }
        return crossingFrame;
    }
}