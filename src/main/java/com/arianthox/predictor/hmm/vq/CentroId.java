
package com.arianthox.predictor.hmm.vq;

import java.util.Vector;
import java.util.Enumeration;

/**
 * <b>description:</b> centroid de un Codebook
 * <b>Entrada:</b> Punto k-dimensional<br>
 * <b>Salida:</b> Distorsion
 * @author Ricardo Sanchez Delgado
 */
public class CentroId extends Point{
    /**
     * Distorsion - Suma total de los puntos distantes al centro
     */
    protected double distortion = 0;
    /**
     * Almacena los puntos distantes al centro
     */
    protected Vector<Point> pts = new Vector<Point>(0);
    /**
     * Numero total de puntos lejanos al centro
     */
    protected int total_pts;
    /**
     * Constructor que crea el Centro a partir de las cordenadas proporcionadas<br>
     * @param Co Array de Coordenadas
     */
    public CentroId(double Co[]){
        super(Co);
        total_pts = 0;
    }
    /**
     * Devuelve un punto especifico indexado<br>
     * @param index Numero Deseado
     * @return Punto correspondiente al Index Dado
     */
    public Point getPoint(int index){
        return (Point)pts.get(index);
    }
    /**
     * Retorna el Numero de Puntos en esta Celda
     * @return Numero de Puntos
     */
    public int getNumPts(){
        return total_pts;
    }
    /**
     * Remueve un punto dado de la Celda
     * @param pt Punto para ser removido
     * @param dist Distancia del Centro
     */
    public void remove(Point pt, double dist){
        Point tmpPoint = (Point)pts.get(0);
        int i = -1;
        
        Enumeration enume = pts.elements();
        boolean found = false;
        while( enume.hasMoreElements() && !found ){
            tmpPoint = (Point)enume.nextElement();
            i++;
            
            // Busca un punto identico en el Vector pts
            if ( Point.equals(pt, tmpPoint) ){
                found = true;
            }
        }
        
        if (found){
            // remueve el punto del Vector pts
            pts.remove(i);
            // Actualiza la distorsion
            distortion -= dist;
            // Actualiza el numero de puntos
            total_pts--;
        }
        else{
            //System.out.println("err: Punto no Encontrado");
        }
    }
    /**
     * Agrega el punto al Centro<br>
     * @param pt Punto alejado del centro
     * @param dist Distancia al Centro
     */
    public void add(Point pt, double dist){
        // Actualiza el Numero de Puntos
        total_pts++;
        // Agrega el punto al Vector pts
        pts.add(pt);
        // Actualiza la distorsion
        distortion += dist;
    }
    /**
     * Actualiza el Centroide Dado por los Puntos Agregados
     */
    public void update(){
        double sum_coordinates[] = new double[dimension];
        Point tmpPoint;
        Enumeration enume = pts.elements();
        
        while( enume.hasMoreElements() ){
            tmpPoint = (Point)enume.nextElement();
            
            // Calcula la suma de todas las coordenadas
            for (int k = 0; k < dimension; k++){
                sum_coordinates[k] += tmpPoint.getCo(k);
            }
        }
        
        // Divide la suma de las coordenadas por el total de numeros dados
        for( int k = 0 ; k < dimension; k++){
            setCo(k, sum_coordinates[k] / (double)total_pts);
            pts = new Vector<Point>(0);
        }
        
        // resetea el numero de puntos
        total_pts = 0;
        // resetea la distorsion
        distortion = 0;
    }
    /**
     * retorna la distorsion actual
     * @return Distorsion Actual
     */
    public double getDistortion(){
        return distortion;
    }
}