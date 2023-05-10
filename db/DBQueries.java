package DigitalVault_INF1416.db;
import java.sql.*; 

public class DBQueries {
    private static String dbName = "cofredigital.db";
    public static void main(String[] args) {  
        start();
    }

    public static void start(){
        if (!DBManager.exists(dbName)){ //does not exist
            try {createDB();}
            catch (SQLException e){
                System.err.println(e.getMessage()); 
                return;
            }
            System.out.println("DB Created!");
        }
        System.out.println("DB Started!");
    }   

    public static void createDB() throws SQLException{
        Connection conn = DBManager.createNewDatabase(dbName);
        String sql;

        //chaveiro
        sql = "CREATE TABLE IF NOT EXISTS chaveiro (\n"  
                + " kid integer PRIMARY KEY,\n"  
                + " crt text NOT NULL,\n"  
                + " privatekey text NOT NULL\n"  
                + ");";  
        DBManager.createNewTable(conn, dbName, sql);

        //grupo
        sql = "CREATE TABLE IF NOT EXISTS grupo (\n"  
                + " gid integer PRIMARY KEY,\n"  
                + " groupname text\n"  
                + ");";  
        DBManager.createNewTable(conn, dbName, sql);

        //usuario
        sql = "CREATE TABLE IF NOT EXISTS usuario (\n"  
                + " uid integer PRIMARY KEY,\n"  
                + " email text,\n"  
                + " hash text,\n"  
                + " token text,\n" 
                + " kid integer,\n"  
                + " gid integer,\n"  
                + " FOREIGN KEY (kid) REFERENCES chaveiro(kid),\n" 
                + " FOREIGN KEY (gid) REFERENCES grupo(gid)\n" 
                + ");";  
        DBManager.createNewTable(conn, dbName, sql);
    }
}
