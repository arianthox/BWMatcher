
package com.arianthox.predictor.hmm.vq;

/**
 * <b>Descripcion:</b> Clase que almacena las Coordenadas<br>
 * <b>Entrada:</b> Configuracion de Coordenadas<br>
 * <b>Salida:</b> Ninguna
 * @author Ricardo Sanchez Delgado
 */
public class Point{
    /**
     * Array de Coordenadas
     */
    protected double coordinates[];
    /**
     * Dimensiones
     */
    protected int dimension;
    /**
     * Constructor que crea un punto en base a las coordenadas del array<br>
     * @param co array de Coordenadas
     */
    public Point(double co[]){
        dimension = co.length;
        coordinates = co;
    }
    /**
     * Retorna el array de coordenadas<br>
     * @return coordenadas como un array Double
     */
    public double[] getAllCo(){
        return coordinates;
    }
    /**
     * Devuelve las Coordenadas de un Index especifico<br>
     * @param i Indice
     * @return Coordenadas en el Indice i
     */
    public double getCo(int i){
        return coordinates[i];
    }
    /**
     * Especifica las coordenadas para un Indice<br>
     * @param i Indice
     * @param value Valor de la Coordenada
     */
    public void setCo(int i, double value){
        coordinates[i] = value;
    }
    /**
     * Reemplaza las coordenadas por una proporcionada<br>
     * @param tCo Nuevo array de coordenadas
     */
    public void changeCo(double tCo[]){
        coordinates = tCo;
    }
    /**
     * Devuelve las Dimensiones<br>
     * @return dimension
     */
    public int getDimension(){
        return dimension;
    }
    /**
     * Compara 2 puntos dados<br>
     * @param p1 Primer Punto
     * @param p2 Segundo Punto
     * @return true/false indicando si los puntos son identicos o no.
     */
    public static boolean equals(Point p1, Point p2){
        boolean equal = true;
        int d = p1.getDimension();
        
        // la dimension de los 2 puntos tiene que ser igual
        if (d == p2.getDimension()){
            // compara todas las coordenadas
            for (int k = 0; k < d && equal; k++){
                // si alguna coordenada es diferente los puntos no son identicos
                if ( p1.getCo(k) != p2.getCo(k)){
                    equal = false;
                }
            }
        }
        else{
            equal = false;
        }
        return equal;
    }
}