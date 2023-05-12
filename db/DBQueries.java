package DigitalVault_INF1416.db;
import java.sql.*; 

public class DBQueries {
    private static final String dbName = "cofredigital.db";
    public static final int adminGID = 0;
    public static final int userGID = 1;

    private static Connection conn;

    public static void main(String[] args) {  
        start();
        try{
            // insertKeys(123, "crt123.crt", "key123.key");
            // insertGroup(424, "admin");
            // insertUser(0, "user@email.com", "1415125125", "253124124", 123, 424);
            // insertKeys(001, "crt001.crt", "key001.key");
            // insertUser(1, "user2@email.com", "123124124124", "353535235232", 001, 424);
            // selectAll();
            selectAllUsers();
            // System.out.println(hasUsers());
        }catch(SQLException e){
            System.err.println(e.getMessage());
        }
    }

    public static void start(){
        if (!DBManager.exists(dbName)){ //does not exist
            try {
                createDB();
                insertGroup(adminGID, "admin");
                insertGroup(userGID, "user");
            }
            catch (SQLException e){
                System.err.println(e.getMessage()); 
                return;
            }
            System.out.println("DB Created!");
        }
        System.out.println("DB Started!");
    }   

    private static Connection getCurrentConnection() throws SQLException{
        if (conn == null)
            conn = DBManager.connect(dbName);
        return conn;
    }

    private static void createDB() throws SQLException{
        Connection conn = getCurrentConnection();
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

    public static void insertKeys(int kid, String crt, String privatekey) throws SQLException{
        String sql = "INSERT INTO chaveiro(kid, crt, privatekey) VALUES(?,?,?)";  
        Connection conn = getCurrentConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);  
        pstmt.setInt(1, kid);  
        pstmt.setString(2, crt);
        pstmt.setString(3, privatekey); 
        pstmt.executeUpdate();  
    }

    public static void insertGroup(int gid, String groupname) throws SQLException{
        String sql = "INSERT INTO grupo(gid, groupname) VALUES(?,?)";  
        Connection conn = getCurrentConnection(); 
        PreparedStatement pstmt = conn.prepareStatement(sql);  
        pstmt.setInt(1, gid);  
        pstmt.setString(2, groupname);  
        pstmt.executeUpdate();  
    }
    
    public static void insertUser(int uid, String email, String hash, String token, int kid, int gid) throws SQLException{
        String sql = "INSERT INTO usuario(uid, email, hash, token, kid, gid) VALUES(?,?,?,?,?,?)";  
        Connection conn = getCurrentConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);  
        pstmt.setInt(1, uid);  
        pstmt.setString(2, email); 
        pstmt.setString(3, hash);  
        pstmt.setString(4, token);
        pstmt.setInt(5, kid);  
        pstmt.setInt(6, gid); 
        pstmt.executeUpdate();  
    }

    public static void selectAll() throws SQLException{
        System.out.println("\nGRUPOS:");
        selectAllGroups();
        System.out.println("\nCHAVEIROS:");
        selectAllKeys();
        System.out.println("\nUSUARIOS:");
        selectAllUsers();
    }

    public static void selectAllUsers() throws SQLException{
        String sql = "SELECT * FROM usuario"; 
        Connection conn = getCurrentConnection();
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

    public static void selectAllKeys() throws SQLException{
        String sql = "SELECT * FROM chaveiro"; 
        Connection conn = getCurrentConnection();
        Statement stmt = conn.createStatement();  
        ResultSet rs = stmt.executeQuery(sql);
        System.out.println("KID\tCERTIFICATE\tPRIVATE_KEY");  
        while (rs.next()) {  
            System.out.println(
                rs.getInt("kid") + "\t" +   
                rs.getString("crt") + "\t" +  
                rs.getString("privatekey") + "\t"
            );  
        }  
    }

    public static void selectAllGroups() throws SQLException{
        String sql = "SELECT * FROM grupo"; 
        Connection conn = getCurrentConnection();
        Statement stmt = conn.createStatement();  
        ResultSet rs = stmt.executeQuery(sql);
        System.out.println("GID\tGROUPNAME");  
        while (rs.next()) {  
            System.out.println(
                rs.getInt("gid") + "\t" +   
                rs.getString("groupname") + "\t"
            );  
        }  
    }

    public static boolean hasUsers() throws SQLException{
        Connection conn = getCurrentConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM usuario");

        if (rs.next()) {
            int count = rs.getInt(1);
            return count > 0;
        }
        return false;
    }

    public static boolean emailAlreadyTaken(String email) throws SQLException{
        Connection conn = getCurrentConnection();

        String query = "SELECT COUNT(*) FROM usuario WHERE email = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, email);
        
        ResultSet rs = stmt.executeQuery();
        int count = rs.getInt(1);
        
        return (count > 0);
    }


}
