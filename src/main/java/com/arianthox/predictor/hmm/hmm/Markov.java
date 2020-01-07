
package com.arianthox.predictor.hmm.hmm;

import java.io.*;
import java.util.*;

/**
 * <b>Descripcion:</b> Esta clase representa los Modelos Ocultos de Markov y los metodos esenciales para el Reconocimiento del Habla.
 * La Coleccion de los Metodos Incluye  el Algoritmo Forward-Backward,Algoritmo Baum-Welch, Scaling, Viterbi, etc.<br>
 * <b>Entrada:</b> Secuencia de Enteros<br>
 * <b>Salida:</b> Probabilidad
 * @author Ricardo Sanchez
 */
public class Markov{
    /**
     * Minimo de Probabilidad
     */
    final double MIN_PROBABILITY = 0.00000000001;
    /**
     * Tamaño de la secuencia de Observacion
     */
    protected int len_obSeq;
    /**
     * Numero de Estados del Modelo
     */
    protected int num_states;
    /**
     * Numero de simbolos de Observacion por Estado
     */
    protected int num_symbols;
    /**
     * Numero de Estados en el modelo por Salto
     */
    protected final int delta = 2;
    /**
     * Configuracion discreta de simbolos de Observacion
     */
    protected int obSeq[][];
    /**
     * Secuencia de Observacion Actual
     */
    protected int currentSeq[];
    /**
     * Numero de Secuencia de Observacion
     */
    protected int num_obSeq;
    /**
     * Probabilidad de Estado de Transicion
     */
    protected double transition[][];
    /**
     * Probabilidad de Salida Discreta
     */
    protected double output[][];
    /**
     * Estado Inicial de Distribucion
     */
    protected double pi[];
    /**
     * Variable Alfa de Seguimiento
     */
    protected double alpha[][];
    /**
     * Variable Beta de Retroceso
     */
    protected double beta[][];
    /**
     * Coheficiente de Escala
     */
    protected double scaleFactor[];
    /**
     * Variable para el Algoritmo de Viterbi
     */
    private int psi[][];
    /**
     * Mejor estado de Secuencia
     */
    public int q[];
    /**
     * Algortimo de Viterbi usado para obtener el mejor estado de Secuencia y Probabilidad<br>
     * @param testSeq Secuencia de Prueba
     * @return Probabilidad
     */
    public double viterbi(int testSeq[]){
        setObSeq(testSeq);
        double phi[][] = new double[len_obSeq][num_states];
        psi = new int[len_obSeq][num_states];
        q = new int[len_obSeq];
        
        for (int i = 0; i < num_states; i++){
            double temp = pi[i];
            if (temp == 0){
                temp = MIN_PROBABILITY;
            }
            
            phi[0][i] = Math.log(temp) + Math.log(output[i][currentSeq[0]]);
            psi[0][i] = 0;
        }
        
        for (int t = 1; t < len_obSeq; t++){
            for (int j = 0; j < num_states; j++){
                double max = phi[t - 1][0] + Math.log(transition[0][j]);
                double temp = 0;
                int index = 0;
                
                for (int i = 1; i < num_states; i++){
                    
                    temp = phi[t - 1][i] + Math.log(transition[i][j]);
                    if (temp > max){
                        max = temp;
                        index = i;
                    }
                    
                }
                
                phi[t][j] = max + Math.log( output[j][currentSeq[t]] );
                psi[t][j] = index;
            }
        }
        
        double max = phi[len_obSeq - 1][0];
        double temp = 0;
        int index = 0;
        for (int i = 1; i < num_states; i++){
            temp = phi[len_obSeq - 1][i];
            
            if (temp > max){
                max = temp;
                index = i;
            }
        }
        
        q[len_obSeq - 1] = index;
        
        for (int t = len_obSeq - 2; t >= 0; t--){
            q[t] = psi[t + 1][q[t + 1]];
        }
        
        return max;
    }
    /**
     * Reescala la Variable Beta para prevenir el Desbordamiento<br>
     * @param t Indice de la Variable Beta
     */
    private void rescaleBeta(int t){
        for (int i = 0; i < num_states; i++){
            beta[t][i] *= scaleFactor[t];
        }
    }
    /**
     * Reescala la Variable Alpha para prevenir el Desbordamiento<br>
     * @param t Indice de la Variable Alpha
     */
    private void rescaleAlpha(int t){
        // Calcula la Escala de Coheficientes
        for (int i = 0; i < num_states; i++){
            scaleFactor[t] += alpha[t][i];
        }
        
        scaleFactor[t] = 1 / scaleFactor[t];
        
        // Aplica la Escala de Coheficientes
        for (int i = 0; i < num_states; i++){
            alpha[t][i] *= scaleFactor[t];
        }
    }
    /**
     * Retorna la Probabilidad calculada de la secuencia de Prueba<br>
     * @param testSeq Secuencia de Prueba
     * @return Probabilidad de la Secuencia dada por el Modelo
     */
    public double getProbability(int testSeq[]){
        setObSeq(testSeq);
        double temp = computeAlpha();
        
        return temp;
    }
    /**
     * Calcula la Variable Alpha<br>
     * @return Probabilidad
     */
    protected double computeAlpha(){
        /**
         * Probabilidad de Observacion de la secuencia dada por el Modelo HMM
         */
        double probability = 0;
        
        // Resetea el scaleFactor[]
        for (int t = 0; t < len_obSeq; t++){
            scaleFactor[t] = 0;
        }
        
        /**
         * Inicializacion: Calcula las Variables Alpha en Tiempo 0
         */
        for (int i = 0; i < num_states; i++){
            alpha[0][i] = pi[i] * output[i][currentSeq[0]];
        }
        rescaleAlpha(0);
        
        /**
         * Induccion:
         */
        for (int t = 0; t < len_obSeq - 1; t++){
            for (int j = 0; j < num_states; j++){
                
                /**
                 * Suma todas alpha[t][i] * transition[i][j]
                 */
                double sum = 0;
                
                /**
                 * Calcula la suma de todas alpha[t][i] * transition[i][j], 0 <= i < num_states
                 */
                for (int i = 0; i < num_states; i++){
                    sum += alpha[t][i] * transition[i][j];
                }
                
                alpha[t + 1][j] = sum * output[j][currentSeq[t + 1]];
            }
            rescaleAlpha(t + 1);
        }
        
        
        for (int i = 0; i < num_states; i++){
            probability += alpha[len_obSeq - 1][i];
        }
        
        probability = 0;
        for (int t = 0; t < len_obSeq; t++){
            probability += Math.log( scaleFactor[t] );

        }
        
        return -probability;

    }
    /**
     * Calcula la variable Beta de acuerdo al metodo de Estimacion<br>
     */
    protected void computeBeta(){
        /**
         * Inicializacion: Establece a todas las variables time = len_obSeq - 1
         */
        for (int i = 0; i < num_states; i++){
            beta[len_obSeq - 1][i] = 1;
        }
        rescaleBeta(len_obSeq - 1);
        
        /**
         * Induccion:
         */
        for (int t = len_obSeq - 2; t >= 0; t--){
            for (int i = 0; i < num_states; i++){
                for (int j = 0; j < num_states; j++){
                    beta[t][i] += transition[i][j] * output[j][currentSeq[t + 1]] * beta[t + 1][j];
                }
            }
            rescaleBeta(t);
        }
    }
    /**
     * Establece el numero de Intentos para la secuencia<br>
     * @param k Numero de Intentos por Secuencia
     */
    public void setNumObSeq(int k){
        num_obSeq = k;
        obSeq = new int[k][];
    }
    /**
     * Establece una Secuencia de Pruba<br>
     * @param k Indice Representa kth secuencia de prueba
     * @param trainSeq Secuencia de Prueba
     */
    public void setTrainSeq(int k, int trainSeq[]){
        obSeq[k] = trainSeq;
    }
    /**
     * Establece una secuencia de Pruba<br>
     * @param trainSeq Secuencia de Prueba
     */
    public void setTrainSeq(int trainSeq[][]){
        num_obSeq = trainSeq.length;
        
        for (int k = 0; k < num_obSeq; k++){
            obSeq[k] = trainSeq[k];
        }
    }
    /**
     * Prueba el hmm Model<br>
     */
    public void train(){
        // Re estima 25 Veces
        for (int i = 0; i < 25; i++){
            reestimate();
        }
    }
    
    /**
     * Algoritmo Baum-Welch - Re-Estima (Itera Actualiza) de los parametros HMM<br>
     */
    private void reestimate(){
        
        double newTransition[][] = new double[num_states][num_states];
        double newOutput[][] = new double[num_states][num_symbols];
        double numerator[] = new double[num_obSeq];
        double denominator[] = new double[num_obSeq];
        
        // calcula la matriz de Probabilidades
        double sumP = 0;
        
        for (int i = 0; i < num_states; i++){
            for (int j = 0; j < num_states; j++){
                
                if (j < i || j > i + delta){
                    newTransition[i][j] = 0;
                }
                else{
                    for (int k = 0; k < num_obSeq; k++){
                        numerator[k] = denominator[k] = 0;
                        setObSeq(obSeq[k]);
                        
                        sumP += computeAlpha();
                        computeBeta();
                        
                        for (int t = 0; t < len_obSeq - 1; t++){
                            numerator[k] += alpha[t][i] * transition[i][j] * output[j][currentSeq[t + 1]] * beta[t + 1][j];
                            denominator[k] += alpha[t][i] * beta[t][i];
                        }
                    }
                    
                    double denom = 0;
                    for (int k = 0; k < num_obSeq; k++){
                        newTransition[i][j] += (1 / sumP) * numerator[k];
                        denom += (1 / sumP) * denominator[k];
                    }
                    
                    newTransition[i][j] /= denom;
                    newTransition[i][j] += MIN_PROBABILITY;
                }
            }
        }
        
        // Calcula la salida de la nueva matrix de probabilidades
        sumP = 0;
        for (int i = 0; i < num_states; i++){
            for (int j = 0; j < num_symbols; j++){
                for (int k = 0; k < num_obSeq; k++){
                    numerator[k] = denominator[k] = 0;
                    setObSeq(obSeq[k]);
                    
                    sumP += computeAlpha();
                    computeBeta();
                    
                    for (int t = 0; t < len_obSeq - 1; t++){
                        if ( currentSeq[t] == j ){
                            numerator[k] += alpha[t][i] * beta[t][i];
                        }
                        denominator[k] += alpha[t][i] * beta[t][i];
                    }
                }
                
                double denom = 0;
                for (int k = 0; k < num_obSeq; k++){
                    newOutput[i][j] += (1 / sumP) * numerator[k];
                    denom += (1 / sumP) * denominator[k];
                }
                
                newOutput[i][j] /= denom;
                newOutput[i][j] += MIN_PROBABILITY;
            }
        }
        
        // reemplaza la antigua matrix
        transition = newTransition;
        output = newOutput;
    }
    
    /**
     * Establece la sequencia de Observacion<br>
     * @param observationSeq Secuencia de Observacion
     */
    public void setObSeq(int observationSeq[]){
        currentSeq = observationSeq;
        len_obSeq = observationSeq.length;
        
        alpha = new double[len_obSeq][num_states];
        beta = new double[len_obSeq][num_states];
        scaleFactor = new double[len_obSeq];
    }
    
    /**
     * Constructor de Clase - Usado para crear el modelo en base al archivo<br>
     * @param Direccion del Archivo a cargar
     */
    public Markov(String filepath){
        try{
            FileReader fr = new FileReader(filepath);
            BufferedReader br = new BufferedReader(fr);
            
            String temp = "";
            StringTokenizer st;
            
            while( (temp = br.readLine()) != null ){
                if ( temp.equals("Number of States:") ){
                    num_states = Integer.parseInt( br.readLine() );
                }
                else if ( temp.equals("Observation Symbols:") ){
                    num_symbols = Integer.parseInt( br.readLine() );
                }
                else if ( temp.equals("Number of Samples:") ){
                    num_obSeq = Integer.parseInt( br.readLine() );
                }
                else if ( temp.equals("Output Matrix:") ){
                    output = new double[num_states][num_symbols];
                    for (int i = 0; i < num_states; i++){
                        st = new StringTokenizer( br.readLine() );
                        for (int j = 0; j < num_symbols; j++){
                            output[i][j] = Double.parseDouble(st.nextToken());
                        }
                    }
                }
                else if ( temp.equals("Transition Matrix:") ){
                    transition = new double[num_states][num_states];
                    for (int i = 0; i < num_states; i++){
                        st = new StringTokenizer( br.readLine() );
                        for (int j = 0; j < num_states; j++){
                            transition[i][j] = Double.parseDouble(st.nextToken());
                        }
                    }
                }
                else if ( temp.equals("pi:") ){
                    pi = new double[num_states];
                    st = new StringTokenizer( br.readLine() );
                    
                    for (int i = 0; i < num_states; i++){
                        pi[i] = Double.parseDouble(st.nextToken());
                    }
                }
            }
            
            br.close();
        }
        catch(IOException e){
        }
        catch(Exception e){
        }
    }
    
    /**
     * Constructor de Clase<br>
     * @param num_states Numero de Estados en el Modelo
     * @param num_symbols Numero de Simbolos por Modelo
     */
    public Markov(int num_states, int num_symbols){
        this.num_states = num_states;
        this.num_symbols = num_symbols;
        transition = new double[num_states][num_states];
        output = new double[num_states][num_symbols];
        pi = new double[num_states];
        
        /**
         * Modelo HMM, El Primer Estado es inicializado en 1
         */
        pi[0] = 1;
        for (int i = 1; i < num_states; i++){
            pi[i] = 0;
        }
        
        // Genera aleatoriamente la probabilidad para todas las otras matrices de probabilidad
        randomProb();
    }
    
    /**
     * Genera las probabilidades Aleatorias<br>
     */
    private void randomProb(){
        double remainder = 1;
        for (int i = 0; i < num_states; i++){
            for (int j = 0; j < num_states - 1; j++){
                if (j < i || j > i + delta){
                    transition[i][j] = 0;
                }
                else{
                    double randNum = Math.random() * remainder;
                    transition[i][j] = randNum;
                    remainder -= randNum;
                }
            }
            transition[i][num_states - 1] = remainder;
            
            remainder = 1;
            for (int j = 0; j < num_symbols - 1; j++){
                double randNum = Math.random() * remainder;
                output[i][j] = randNum;
                remainder -= output[i][j];
            }
            output[i][num_symbols - 1] = remainder;
        }
    }
    
    /**
     * Almacena el Modelo HMM en un Archivo<br>
     * @param filepath Archivo Destino
     */
    public void save(String filepath){
        try{
            FileWriter fw = new FileWriter(filepath);
            BufferedWriter bw = new BufferedWriter(fw);
            
            bw.write("Number of States:");
            bw.newLine();
            bw.write(num_states + "");
            bw.newLine();
            bw.write("Observation Symbols:");
            bw.newLine();
            bw.write(num_symbols + "");
            bw.newLine();
            bw.write("Number of Samples:");
            bw.newLine();
            bw.write(num_obSeq + "");
            bw.newLine();
            bw.write("Output Matrix:");
            bw.newLine();
            
            for (int i = 0; i < num_states; i++){
                for (int j = 0; j < num_symbols; j++){
                    bw.write(output[i][j] + "");
                    if (j != num_symbols - 1){
                        bw.write(" ");
                    }
                }
                bw.newLine();
            }
            
            bw.write("Transition Matrix:");
            bw.newLine();
            
            for (int i = 0; i < num_states; i++){
                for (int j = 0; j < num_states; j++){
                    bw.write(transition[i][j] + "");
                    if (j != num_states - 1){
                        bw.write(" ");
                    }
                }
                bw.newLine();
            }
            
            bw.write("pi:");
            bw.newLine();
            
            for (int i = 0; i < num_states; i++){
                bw.write(pi[i] + "");
                if (i != num_states - 1){
                    bw.write(" ");
                }
            }
            
            bw.close();
        }
        catch(IOException e){
        }
        catch(Exception e){
        }
    }
}