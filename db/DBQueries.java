package DigitalVault_INF1416.db;
import java.sql.*; 

public class DBQueries {
    private static String dbName = "cofredigital.db";
    public static void main(String[] args) {  
        start();
        try{
            insertKeys(123, "crt123.crt", "key123.key");
            insertGroup(424, "admin");
            insertUser(0, "user@email.com", "1415125125", "253124124", 123, 424);
            selectAllUsers();
        }catch(SQLException e){
            System.err.println(e.getMessage());
        }
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

    private static void createDB() throws SQLException{
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

    public static void insertKeys(int kid, String crtPath, String privatekeyPath) throws SQLException{
        String sql = "INSERT INTO chaveiro(kid, crt, privatekey) VALUES(?,?,?)";  
        Connection conn = DBManager.connect(dbName); 
        PreparedStatement pstmt = conn.prepareStatement(sql);  
        pstmt.setInt(1, kid);  
        pstmt.setString(2, crtPath);
        pstmt.setString(3, privatekeyPath); 
        pstmt.executeUpdate();  
    }

    public static void insertGroup(int gid, String groupname) throws SQLException{
        String sql = "INSERT INTO grupo(gid, groupname) VALUES(?,?)";  
        Connection conn = DBManager.connect(dbName); 
        PreparedStatement pstmt = conn.prepareStatement(sql);  
        pstmt.setInt(1, gid);  
        pstmt.setString(2, groupname);  
        pstmt.executeUpdate();  
    }
    
    public static void insertUser(int uid, String email, String hash, String token, int kid, int gid) throws SQLException{
        String sql = "INSERT INTO usuario(uid, email, hash, token, kid, gid) VALUES(?,?,?,?,?,?)";  
        Connection conn = DBManager.connect(dbName); 
        PreparedStatement pstmt = conn.prepareStatement(sql);  
        pstmt.setInt(1, uid);  
        pstmt.setString(2, email); 
        pstmt.setString(3, hash);  
        pstmt.setString(4, token);
        pstmt.setInt(5, kid);  
        pstmt.setInt(6, gid); 
        pstmt.executeUpdate();  
    }

    public static void selectAllUsers() throws SQLException{
        String sql = "SELECT * FROM usuario"; 
        Connection conn = DBManager.connect(dbName); 
        Statement stmt = conn.createStatement();  
        ResultSet rs = stmt.executeQuery(sql);
        System.out.println("UID\tEMAIL\tHASH\tTOKEN\tKID\tGID\t");  
        while (rs.next()) {  
            System.out.println(
                rs.getInt("uid") + "\t" +   
                rs.getString("email") + "\t" +  
                rs.getString("hash") + "\t" +
                rs.getString("token") + "\t" +
                rs.getInt("kid") + "\t" +
                rs.getInt("gid") + "\t"
            );  
        }  
    }
}
