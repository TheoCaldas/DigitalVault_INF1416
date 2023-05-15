/* INF1416 2023.1 - Trabalho 4
 * Theo Caldas - 1911078    
 * Matheus Kulick - 1911090
 */
package DigitalVault_INF1416.db;
import java.security.cert.*;
import java.sql.*;

public class DBQueries {
    private static final String dbName = "cofredigital.db";
    public static final int ADMIN_GID = 0;
    public static final int USER_GID = 1;
    public static final String NOT_BLOCKED = "no";

    private static Connection conn;

    public static void main(String[] args) {  
        start();
        try{
            selectAll();
            // selectAllUsers();
        }catch(SQLException e){
            System.err.println(e.getMessage());
        }
    }

    public static void start(){
        if (!DBManager.exists(dbName)){ //does not exist
            try {
                createDB();
                insertGroup(ADMIN_GID, "admin");
                insertGroup(USER_GID, "user");
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
                + " blocked text,\n"
                + " nlogins integer,\n" 
                + " nqueries integer,\n" 
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
        String sql = "INSERT INTO usuario(uid, email, hash, token, blocked, nlogins, nqueries, kid, gid) VALUES(?,?,?,?,?,0,0,?,?)";  
        Connection conn = getCurrentConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);  
        pstmt.setInt(1, uid);  
        pstmt.setString(2, email); 
        pstmt.setString(3, hash);  
        pstmt.setString(4, token);
        pstmt.setString(5, NOT_BLOCKED);
        pstmt.setInt(6, kid);  
        pstmt.setInt(7, gid); 
        pstmt.executeUpdate();  
    }

    public static void selectAll() throws SQLException{
        System.out.println("\n=======GRUPOS=======\n");
        selectAllGroups();
        System.out.println("\n=======CHAVEIROS=======\n");
        selectAllKeys();
        System.out.println("\n=======USUARIOS=======\n");
        selectAllUsers();
    }

    public static void selectAllUsers() throws SQLException{
        String sql = "SELECT * FROM usuario"; 
        Connection conn = getCurrentConnection();
        Statement stmt = conn.createStatement();  
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {  
            System.out.println(
                "UID: " + rs.getInt("uid") + "\n" +   
                "EMAIL: " + rs.getString("email") + "\n" +  
                "HASH: " + rs.getString("hash") + "\n" +
                "TOKEN: " + rs.getString("token") + "\n" +
                "BLOCKED: " + rs.getString("blocked") + "\n" +
                "KID: " + rs.getInt("kid") + "\n" +
                "GID: " + rs.getInt("gid") + "\n"
            );  
            System.out.println("---------------------------"); 
        }  
    }

    public static void selectAllKeys() throws SQLException{
        String sql = "SELECT * FROM chaveiro"; 
        Connection conn = getCurrentConnection();
        Statement stmt = conn.createStatement();  
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {  
            System.out.println(
                "KID: " + rs.getInt("kid") + "\n" +   
                "CERTIFICATE: " + rs.getString("crt") + "\n" +  
                "PRIVATE_KEY: " + rs.getString("privatekey") + "\n"
            );  
            System.out.println("---------------------------"); 
        }  
    }

    public static void selectAllGroups() throws SQLException{
        String sql = "SELECT * FROM grupo"; 
        Connection conn = getCurrentConnection();
        Statement stmt = conn.createStatement();  
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {  
            System.out.println(
                "GID: " + rs.getInt("gid") + "\n" +   
                "GROUP_NAME: " + rs.getString("groupname") + "\n"
            );  
            System.out.println("---------------------------"); 
        }  
    }

    public static int getUsersCount() throws SQLException {
        Connection conn = getCurrentConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM usuario");

        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }

        return count;
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

    private static ResultSet selectUserByEmail(String email) throws SQLException{
        Connection conn = getCurrentConnection();

        String query = "SELECT * FROM usuario WHERE email = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, email);
        
        return stmt.executeQuery();
    }

    private static boolean isEmptyResult(ResultSet rs) throws SQLException{
        return !rs.next();
    }

    public static boolean emailAlreadyTaken(String email) throws SQLException{
        ResultSet rs = selectUserByEmail(email);
        return !isEmptyResult(rs);
    }

    //returns user if exists, null otherwise
    public static User selectUser(String email) throws SQLException{
        ResultSet rs = selectUserByEmail(email);
        if (isEmptyResult(rs))
            return null;
        return new User(rs);
    }

    public static void updateUserLoginCount(User user) throws SQLException {
        Connection conn = getCurrentConnection();
        String sql = "UPDATE usuario SET nlogins = nlogins + 1 WHERE uid = ?";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, user.uid);

        pstmt.executeUpdate();
    }

    public static void updateUserQueriesCount(User user) throws SQLException {
        Connection conn = getCurrentConnection();
        String sql = "UPDATE usuario SET nqueries = nqueries + 1 WHERE uid = ?";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, user.uid);

        pstmt.executeUpdate();
    }

    //return if user was blocked
    public static boolean blockUser(User user, java.util.Date nowDate) throws SQLException{
        Connection conn = getCurrentConnection();
        String sql = "UPDATE usuario SET blocked = ? WHERE uid = ?";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, User.dateToString(nowDate));
        pstmt.setInt(2, user.uid);

        int rowsUpdated = pstmt.executeUpdate();
        return (rowsUpdated > 0);
    }

    public static KeyChain getKeyChain(int kid) throws SQLException, CertificateException {
        Connection conn = getCurrentConnection();

        String query = "SELECT * FROM chaveiro where kid = ?"; 

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, kid);

        ResultSet rs = stmt.executeQuery();
        if (isEmptyResult(rs))
            return null;

        KeyChain crt = new KeyChain(rs);
        return crt;
    }

    public static User selectAdmin() throws SQLException{
        Connection conn = getCurrentConnection();

        String query = "SELECT * FROM usuario WHERE gid = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, ADMIN_GID);
        ResultSet rs = stmt.executeQuery();

        if (isEmptyResult(rs))
            return null;
            
        return new User(rs);
    }
}
