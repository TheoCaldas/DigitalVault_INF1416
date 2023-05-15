/* INF1416 2023.1 - Trabalho 4
 * Theo Caldas - 1911078    
 * Matheus Kulick - 1911090
 */
package DigitalVault_INF1416.db;
import java.security.cert.*;
import java.sql.*;
import java.util.ArrayList;

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
                insertAllMessages();
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

        //mensagens
        sql = "CREATE TABLE IF NOT EXISTS mensagem (\n"  
                + " mid integer PRIMARY KEY,\n"  
                + " description text\n"  
                + ");";  
        DBManager.createNewTable(conn, dbName, sql);

        //registro
        sql = "CREATE TABLE IF NOT EXISTS registro (\n"  
                + " rid integer PRIMARY KEY,\n"  
                + " date text\n"  
                + " email text\n"  
                + " filename text\n" 
                + " mid integer\n"
                + " FOREIGN KEY (uid) REFERENCES usuario(uid),\n" 
                + " FOREIGN KEY (mid) REFERENCES mensagem(mid),\n" 
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

    public static void insertMessage(int mid, String description) throws SQLException{
        String sql = "INSERT INTO mensagem(mid, description) VALUES(?,?)";  
        Connection conn = getCurrentConnection(); 
        PreparedStatement pstmt = conn.prepareStatement(sql);  
        pstmt.setInt(1, mid);  
        pstmt.setString(2, description);  
        pstmt.executeUpdate();  
    }

    public static void insertRegister(int rid, String date, int mid, String email, String filname) throws SQLException{
        String sql = "INSERT INTO registro(rid, date, mid, email, filename) VALUES(?,?,?,?,?)";  
        Connection conn = getCurrentConnection(); 
        PreparedStatement pstmt = conn.prepareStatement(sql);  
        pstmt.setInt(1, rid);  
        pstmt.setString(2, date);
        pstmt.setInt(3, mid);
        pstmt.setString(4, (email == null) ? "" : email);
        pstmt.setString(5, (filname == null) ? "" : filname);
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

    public static String selectDescription(int mid) throws SQLException{
        Connection conn = getCurrentConnection();

        String query = "SELECT * FROM mensagem WHERE mid = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, mid);
        
        ResultSet rs = stmt.executeQuery();
        if (isEmptyResult(rs))
            return null;

        return rs.getString("description");
    }

    public static ArrayList<Register> selectAllRegisters() throws SQLException{
        String sql = "SELECT * FROM registro"; 
        Connection conn = getCurrentConnection();
        Statement stmt = conn.createStatement();  
        ResultSet rs = stmt.executeQuery(sql);
        ArrayList<Register> registers = new ArrayList<Register>();
        while (rs.next()) {  
            Register reg = new Register(rs);
            registers.add(reg);
        }  
        return registers;
    }

    private static void insertAllMessages() throws SQLException{
        insertMessage(1001, "Sistema iniciado.");
        insertMessage(1002, "Sistema encerrado.");
        insertMessage(1003, "Sessão iniciada para <login_name>.");
        insertMessage(1004, "Sessão encerrada para <login_name>.");
        insertMessage(2001, "Autenticação etapa 1 iniciada.");
        insertMessage(2002, "Autenticação etapa 1 encerrada.");
        insertMessage(2003, "Login name <login_name> identificado com acesso liberado.");
        insertMessage(2004, "Login name <login_name> identificado com acesso bloqueado.");
        insertMessage(2005, "Login name <login_name> não identificado.");
        insertMessage(3001, "Autenticação etapa 2 iniciada para <login_name>.");
        insertMessage(3002, "Autenticação etapa 2 encerrada para <login_name>.");
        insertMessage(3003, "Senha pessoal verificada positivamente para <login_name>.");
        insertMessage(3004, "Primeiro erro da senha pessoal contabilizado para <login_name>.");
        insertMessage(3005, "Segundo erro da senha pessoal contabilizado para <login_name>.");
        insertMessage(3006, "Terceiro erro da senha pessoal contabilizado para <login_name>.");
        insertMessage(3007, "Acesso do usuário <login_name> bloqueado pela autenticação etapa 2.");
        insertMessage(4001, "Autenticação etapa 3 iniciada para <login_name>.");
        insertMessage(4002, "Autenticação etapa 3 encerrada para <login_name>.");
        insertMessage(4003, "Token verificado positivamente para <login_name>.");
        insertMessage(4004, "Primeiro erro de token contabilizado para <login_name>.");
        insertMessage(4005, "Segundo erro de token contabilizado para <login_name>.");
        insertMessage(4006, "Terceiro erro de token contabilizado para <login_name>.");
        insertMessage(4007, "Acesso do usuário <login_name> bloqueado pela autenticação etapa 3.");
        insertMessage(5001, "Tela principal apresentada para <login_name>.");
        insertMessage(5002, "Opção 1 do menu principal selecionada por <login_name>.");
        insertMessage(5003, "Opção 2 do menu principal selecionada por <login_name>.");
        insertMessage(5004, "Opção 3 do menu principal selecionada por <login_name>.");
        insertMessage(6001, "Tela de cadastro apresentada para <login_name>.");
        insertMessage(6002, "Botão cadastrar pressionado por <login_name>.");
        insertMessage(6003, "Senha pessoal inválida fornecida por <login_name>.");
        insertMessage(6004, "Caminho do certificado digital inválido fornecido por <login_name>.");
        insertMessage(6005, "Chave privada verificada negativamente para <login_name> (caminho inválido).");
        insertMessage(6006, "Chave privada verificada negativamente para <login_name> (frase secreta inválida).");
        insertMessage(6007, "Chave privada verificada negativamente para <login_name> (assinatura digital inválida).");
        insertMessage(6008, "Confirmação de dados aceita por <login_name>.");
        insertMessage(6009, "Confirmação de dados rejeitada por <login_name>.");
        insertMessage(6010, "Botão voltar de cadastro para o menu principal pressionado por <login_name>.");
        insertMessage(7001, "Tela de consulta de arquivos secretos apresentada para <login_name>.");
        insertMessage(7002, "Botão voltar de consulta para o menu principal pressionado por <login_name>.");
        insertMessage(7003, "Botão Listar de consulta pressionado por <login_name>.");
        insertMessage(7004, "Caminho de pasta inválido fornecido por <login_name>.");
        insertMessage(7005, "Arquivo de índice decriptado com sucesso para <login_name>.");
        insertMessage(7006, "Arquivo de índice verificado (integridade e autenticidade) com sucesso para <login_name>.");
        insertMessage(7007, "Falha na decriptação do arquivo de índice para <login_name>.");
        insertMessage(7008, "Falha na verificação (integridade e autenticidade) do arquivo de índice para <login_name>.");
        insertMessage(7009, "Lista de arquivos presentes no índice apresentada para <login_name>.");
        insertMessage(7010, "Arquivo <arq_name> selecionado por <login_name> para decriptação.");
        insertMessage(7011, "Acesso permitido ao arquivo <arq_name> para <login_name>.");
        insertMessage(7012, "Acesso negado ao arquivo <arq_name> para <login_name>.");
        insertMessage(7013, "Arquivo <arq_name> decriptado com sucesso para <login_name>.");
        insertMessage(7014, "Arquivo <arq_name> verificado (integridade e autenticidade) com sucesso para <login_name>.");
        insertMessage(7015, "Falha na decriptação do arquivo <arq_name> para <login_name>.");
        insertMessage(7016, "Falha na verificação (integridade e autenticidade) do arquivo <arq_name> para <login_name>.");
        insertMessage(8001, "Tela de saída apresentada para <login_name>.");
        insertMessage(8002, "Botão encerrar sessão pressionado por <login_name>.");
        insertMessage(8003, "Botão encerrar sistema pressionado por <login_name>.");
        insertMessage(8004, "Botão voltar de sair para o menu principal pressionado por <login_name>.");        
    }
}
