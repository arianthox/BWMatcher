
package com.arianthox.predictor.hmm.database;

import java.sql.*;

/**
 * <b>Descripcion:</b> Clase Usada para manipular la Base de Datos<br>
 * <b>Entrada:</b> Datos a ser Almacenados<br>
 * <b>Salida:</b> Datos leidos de la Base de Datos
 * @author Ricardo Sanchez
 */
public class database{
    /**
     * Tipo de Datos DOUBLE
     */
    public static final String DOUBLE = "DOUBLE";
    /**
     * statement - Usado para enviar los Comandos SQL a la Base de Datos
     */
    private Statement stmt;
    /**
     * Conexion a Base de Datos
     */
    private Connection con;
    /**
     * Devuelve el Numero de Filas en una Tabla<br>
     * @param tableName Nombre de la Tabla
     * @return Numero de Filas
     */
    public int getRowCount(String tableName){
        int rows = 0;

        try{
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
            if ( rs != null ){
                rs.next();
                rows = rs.getInt(1);
            }
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
        return rows;
    }
    /**
     * Devuelve los Datos de la Tabla<br>
     * @param tableName Nombre de la Tabla
     * @return result set
     */
    public ResultSet retrieve(String tableName){
        tableName = tableName.replace(' ', '_');
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery("SELECT * FROM " + tableName);
        }
        catch(SQLException SQLE) {
            System.out.println(SQLE.toString());
        }
        
        return rs;
    }
    
    /**
     * Inserta un registro en la Fila espeficicada<br>
     * @param tableName Nombre de la Tabla a Agregar la Fila
     * @param values Valores para todas las columnas en la Tabla
     */
    public void insertRow(String tableName, double values[]){
        tableName = tableName.replace(' ', '_');
        String sqlcmd = "INSERT INTO " + tableName + " VALUES(";
        for (int i = 0; i < values.length; i++){
            sqlcmd += values[i] + "";
            if (i != values.length - 1){
                sqlcmd += ", ";
            }
        }
        sqlcmd += ")";
        
        System.out.println(sqlcmd);
        
        try{
            stmt.execute(sqlcmd);
        }
        catch(SQLException SQLE) {
            System.out.println(SQLE.toString());
        }
    }
    
    /**
     * Crea la Tabla en la Base de Datos<br>
     * @param tableName Nombre de la Tabla a crear
     * @param columns Nombre de las Columnas
     * @param dataTypes Tipo de Datos para las Columnas
     */
    public void createTable(String tableName, String columns[], String dataTypes[]){
        tableName = tableName.replace(' ', '_');
        String sqlcmd = "CREATE TABLE " + tableName + " ( ";
        for (int i = 0; i < columns.length; i++){
            sqlcmd += columns[i] + " " + dataTypes[i];
            if (i != columns.length - 1){
                sqlcmd += ", ";
            }
        }
        sqlcmd += " )";
        
        System.out.println(sqlcmd);
        
        try{
            stmt.execute(sqlcmd);
        }
        catch(SQLException SQLE) {
            System.out.println(SQLE.toString());
        }
    }
    
    /**
     * Cerrar la Conexion a la Base de Datos<br>
     */
    public void close(){
        try{
            stmt.close();
            con.close();
        }
        catch(Exception e){
        }
    }
    
    /**
     * Construye la Base de Datos<br>
     * @param dataSourceName Nombre del Datasource
     */
    public database(String dataSourceName){
        try{           
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            con = DriverManager.getConnection("jdbc:odbc:" + dataSourceName);
            
            stmt = con.createStatement();
        }
        catch(SQLException SQLE) {
            System.out.println(SQLE.toString());
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
    }
}