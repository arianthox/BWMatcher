
package com.arianthox.predictor.hmm.vq;

import java.io.*;
import java.sql.*;
import java.util.StringTokenizer;
import com.arianthox.predictor.hmm.database.*;

/**
 * <b>Descripcion:</b> codebook componente para el Vector de Quantizacion<br>
 * <b>Entrada:</b> Señal de Habla<br>
 * <b>Salida:</b> Centros e indices configurados
 * @author Ricardo Sanchez Delgado
 */
public class CodeBook{
    /**
     * Factor de Separacion (Deberia estar en un rango de 0.01 <= SPLIT <= 0.05)
     */
    protected final double SPLIT = 0.01;
    /**
     * Distorsion Minima
     */
    protected final double MIN_DISTORTION = 0.1;
    /**
     * tamaño del Codebook<br>
     * por defecto es: 256
     */
    protected int codebook_size = 256;
    /**
     * array de Centros
     */
    protected CentroId centroids[];
    /**
     * puntos de ensayo
     */
    protected Point pt[];
    /**
     * dimension
     */
    protected int dimension;
    /**
     * Contructor que ejecuta un Codebook de acuerdo a los puntos y el Tamaño del Codebook<br>
     * @param tmpPt vector de Prueba
     * @param size Tamaño del Codebook
     */
    public CodeBook(Point tmpPt[], int size){
        // Actualizacion de Variables
        pt = tmpPt;
        codebook_size = size;
        
        // se asegura de que el espacio reservado sea el adecuado para el Vector
        if (pt.length >= codebook_size){
            dimension = pt[0].getDimension();
            initialize();
        }
        else{
            //System.out.println("err: No hay Espacio para los Puntos");
        }
    }
    /**
     * Constructor que ejecuta un Codebook de acuerdo a los puntos dados y tamaño por defecto (256)<br>
     * @param tmpPt Vector de Prueba
     */
    public CodeBook(Point tmpPt[]){
        pt = tmpPt;
        
        // se asegura de que el espacio reservado sea el adecuado para el Vector
        if (pt.length >= codebook_size){
            dimension = pt[0].getDimension();
            initialize();
        }
        else{
            //System.out.println("err: No hay Espacio para los Puntos");
        }
    }
    /**
     * Constructor que carga un codebook en base a un Result Set<br>
     * @param rs es el Result Set de la base de Datos
     * @param d dimension de los puntos en el Codebook
     * @param cbk_size size of codebook
     */
    public CodeBook(ResultSet rs, int d, int cbk_size){
        codebook_size = cbk_size;
        dimension = d;
        
        centroids = new CentroId[codebook_size];
        
        try{
            if (rs != null){
                int ctr = -1;
                while( rs.next() ){
                    ctr++;
                    double p[] = new double[dimension];                    
                    for (int k = 0; k < dimension; k++){
                        p[k] = rs.getDouble(k + 1);
                    }

                    centroids[ctr] = new CentroId(p);
                }
            }
        }
        catch(Exception e){
            //System.out.println(e.toString());
        }
    }
    /**
     * Constructor que carga un Codebook de un Archivo Externo<br>
     * @param inputFile direccion del Archivo
     */
    public CodeBook(String inputFile){
        try{
            FileReader fr = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(fr);
            
            String temp = br.readLine();
            StringTokenizer st = new StringTokenizer(temp);
            
            codebook_size = Integer.parseInt(st.nextToken());
            dimension = Integer.parseInt(st.nextToken());
            
            centroids = new CentroId[codebook_size];
            int ctr = -1;
            while( (temp = br.readLine()) != null ){
                st = new StringTokenizer(temp);
                
                ctr++;
                double p[] = new double[dimension];
                for (int k = 0; k < dimension; k++){
                    p[k] = Double.parseDouble(st.nextToken());
                }
                centroids[ctr] = new CentroId(p);
            }
            
            br.close();
        }
        catch(FileNotFoundException e){
            //System.out.println("err: Archivo no Encontrado\n" + e.toString());
        }
        catch(IOException e){
            //System.out.println("err: Error de Entrada/Salida \n" + e.toString());
        }
        catch(Exception e){
            //System.out.println("err: Exception\n" + e.toString());
        }
    }
    /**
     * Crea un Codebook usando un Algortimo LBG<br>
     */
    protected void initialize(){
        double distortion_before_update = 0; // Distorsion Antes del Centro
        double distortion_after_update = 0; // Distorsion Despues del Centro
        
        // 1-vector codebook
        centroids = new CentroId[1];
        
        // Inicializacion con (0, 0) Coordenadas
        double origin[] = new double[dimension];
        centroids[0] = new CentroId(origin);
        
        // Inicializacion
        for(int i = 0; i < pt.length; i++){
            centroids[0].add(pt[i], 0);
        }
        
        // Actualizacion de los puntos
        centroids[0].update();
        
        // Iteracion 1: repite la division paso a paso
        while( centroids.length < codebook_size ){
            // Divide el codevectors por metodo de division binario
            split();
            
            // Agrupa los puntos
            groupPtoC();
            
            // Iteration 2: aplicacion  del algoritmo
            do{
                for (int i = 0; i < centroids.length; i++){
                    distortion_before_update += centroids[i].getDistortion();
                    centroids[i].update();
                }
                
                // Regrupacion de Puntos
                groupPtoC();
                
                for (int i = 0; i < centroids.length; i++){
                    distortion_after_update += centroids[i].getDistortion();
                }
                
            }while( Math.abs(distortion_after_update - distortion_before_update) < MIN_DISTORTION );
        }
    }
    /**
     * Almacena el Codebook en la Base de Datos<br>
     * @param word palabra de prueba del Codebook
     */
    public void saveToDB(String word){
        database db = new database("codebooks");
        
        String columns[] = new String[dimension];
        String dataTypes[] = new String[dimension];
        
        for (int k = 0; k < dimension; k++){
            columns[k] = "c" + k;
            dataTypes[k] = database.DOUBLE;
        }
        
        db.createTable(word + "_cbk", columns, dataTypes);
        
        for (int c = 0; c < centroids.length; c++){
            db.insertRow(word + "_cbk", centroids[c].getAllCo());
        }
        
        db.close();
    }
    /**
     * Almacena el Codebook en un Archivo de Texto<br>
     * @param filepath Direccion del Archivo Destino
     */
    public void saveToFile(String filepath){
        try{
            FileWriter fw = new FileWriter(filepath);
            BufferedWriter bw = new BufferedWriter(fw);
            
            bw.write(codebook_size + " " + dimension);
            bw.newLine();
            for (int c = 0; c < centroids.length; c++){
                for (int k = 0; k < dimension; k++){
                    bw.write( centroids[c].getCo(k) + "" );                    
                    if (k != dimension - 1){
                        bw.write(" ");
                    }
                }
                bw.newLine();
            }            
            bw.close();
        }
        catch(FileNotFoundException e){
            //System.out.println("err: Archivo no Encontrado\n" + e.toString());
        }
        catch(IOException e){
            //System.out.println("err: Exepcion de Entrada/Salida\n" + e.toString());
        }
        catch(Exception e){
            //System.out.println("err: Exepcion\n" + e.toString());
        }
    }
    public void saveToFileUVQ(String filepath,short[][] samples){
        try{
            FileWriter fw = new FileWriter(filepath);
            BufferedWriter bw = new BufferedWriter(fw);
            for(short row[]:samples){
                for(short col:row){
                    bw.write(col + " ");
                }
                bw.newLine();
            }
            bw.close();
        }
        catch(FileNotFoundException e){
            //System.out.println("err: Archivo no Encontrado\n" + e.toString());
        }
        catch(IOException e){
            //System.out.println("err: Exepcion de Entrada/Salida\n" + e.toString());
        }
        catch(Exception e){
            //System.out.println("err: Exepcion\n" + e.toString());
        }
    }
    
    /**
     * Algoritmo de Division para implementar el numero de centros multiplos de 2<br>
     */
    protected void split(){
        CentroId temp[] = new CentroId[centroids.length * 2];
        double tCo[][];
        
        for (int i = 0; i < temp.length; i += 2){
            
            tCo = new double[2][dimension];
            
            for (int j = 0; j < dimension; j++){
                tCo[0][j] = centroids[i/2].getCo(j) * (1 + SPLIT);
            }
            
            temp[i] = new CentroId(tCo[0]);
            
            for (int j = 0; j < dimension; j++){
                tCo[1][j] = centroids[i/2].getCo(j) * (1 - SPLIT);
            }
            
            temp[i+1] = new CentroId(tCo[1]);
        }
        
        // Reemplaza los antiguos Centros por uno nuevo
        centroids = new CentroId[temp.length];
        centroids = temp;
    }
    /**
     * quantize Array de entrada de puntos<br>
     * @param pts puntos para ser Quantizados
     * @return quantized array indexado
     */
    public int[] quantize(Point pts[]){
        int output[] = new int[pts.length];
        
        for (int i = 0; i < pts.length; i++){
            output[i] = closestCentroidToPoint(pts[i]);
        }
        
        return output;
    }
    /**
     * Calcula la Distorsion<br>
     * @param pts puntos para calcular la distorsion
     * @return distorsion
     */
    public double getDistortion(Point pts[]){
        double dist = 0;
        
        for (int i = 0; i < pts.length; i++){
            int index = closestCentroidToPoint(pts[i]);
            
            double d = getDistance(pts[i], centroids[index]);
            dist += d;
        }
        
        return dist;
    }
    /**
     * Busca los puntos cerrados para un Punto Dado<br>
     * @param pt Punto
     * @return Indice del Centro Encontrado
     */
    private int closestCentroidToPoint(Point pt){
        double tmp_dist = 0;
        double lowest_dist = 0; 
        int lowest_index = 0;
        
        for (int i = 0; i < centroids.length; i++){
            tmp_dist = getDistance(pt, centroids[i]);
            if (tmp_dist < lowest_dist || i == 0){
                lowest_dist = tmp_dist;
                lowest_index = i;
            }
        }
        
        return lowest_index;
    }
    /**
     * Busca los puntos cerrados para un Punto Dado<br>
     * @param pt Punto
     * @return Indice del Centro Encontrado
     */

    private int closestCentroidToCentroid(CentroId c){
        double tmp_dist = 0;
        double lowest_dist = Double.MAX_VALUE;
        int lowest_index = 0;
        
        for (int i = 0; i < centroids.length; i++){
            tmp_dist = getDistance(c, centroids[i]);
            if (tmp_dist < lowest_dist && centroids[i].getNumPts() > 1){
                lowest_dist = tmp_dist;
                lowest_index = i;
            }
        }
        
        return lowest_index;
    }
    /**
     * Encuentra el Punto para 2 Centros Dados<br>
     * @param c1 Primer Centro
     * @param c2 Segundo Centro
     * @return indice del punto
     */
    private int closestPoint(CentroId c1, CentroId c2){
        double tmp_dist = 0;
        double lowest_dist = getDistance(c2.getPoint(0), c1);
        int lowest_index = 0;
        
        for (int i = 1; i < c2.getNumPts(); i++){
            tmp_dist = getDistance(c2.getPoint(i), c1);
            if (tmp_dist < lowest_dist){
                lowest_dist = tmp_dist;
                lowest_index = i;
            }
        }
        
        return lowest_index;
    }
    /**
     * Agrupa los Puntos en Celdas<br>
     */
    private void groupPtoC(){
        // Busca el centro y asigna lo asigna para el punto dado
        for(int i = 0; i < pt.length; i++){
            int index = closestCentroidToPoint(pt[i]);
            
            centroids[index].add(pt[i], getDistance(pt[i], centroids[index]));
        }
        
        for (int i = 0; i < centroids.length; i++){
            if (centroids[i].getNumPts() == 0){

                int index = closestCentroidToCentroid(centroids[i]);
                

                int closestIndex = closestPoint(centroids[i], centroids[index]);
                Point closestPt = centroids[index].getPoint(closestIndex);
                
                centroids[index].remove(closestPt, getDistance(closestPt, centroids[index]));
                centroids[i].add(closestPt, getDistance(closestPt, centroids[i]));
            }
        }
    }
    /**
     * Calcula la distancia del punto al Centro<br>
     * @param tPt Punto
     * @param tC Centro
     */
    private double getDistance(Point tPt, CentroId tC){
        double distance = 0;
        double temp = 0;
        
        for (int i = 0 ; i < dimension; i++) {
            temp = tPt.getCo(i) - tC.getCo(i);
            distance += temp * temp;
            
        }
        
        distance = Math.sqrt(distance);
        
        return distance;
    }
}