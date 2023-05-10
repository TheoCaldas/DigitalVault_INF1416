package DigitalVault_INF1416.db;
import java.sql.*; 
import java.io.File;

public class DBManager{
    public static boolean exists(String databaseFileName) {  
        File file = new File(databaseFileName);
        return file.exists();
    } 

    public static Connection connect(String databaseFileName) throws SQLException {  
        String url = "jdbc:sqlite:" + databaseFileName;
        Connection conn = null;  
        conn = DriverManager.getConnection(url);  
        conn.createStatement().execute("PRAGMA foreign_keys = ON");
        return conn;  
    }  

    public static Connection createNewDatabase(String fileName) throws SQLException{  
        Connection conn = connect(fileName);
        if (conn != null) {  
            DatabaseMetaData meta = conn.getMetaData();  
            System.out.println("The driver name is " + meta.getDriverName());  
            System.out.println("A new database has been created.");  
        }  
        return conn;
    }
    
    public static void createNewTable(Connection conn, String databaseFileName, String sqlQuery) throws SQLException {  
        Statement stmt = conn.createStatement();  
        stmt.execute(sqlQuery);  
    } 
}