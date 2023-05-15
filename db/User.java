/* INF1416 2023.1 - Trabalho 4
 * Theo Caldas - 1911078    
 * Matheus Kulick - 1911090
 */
package DigitalVault_INF1416.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class User {
    public int uid; 
    public String email;
    public String hash;
    public String token;
    public Date blocked;
    public int kid; 
    public int gid;
    public int nLogins;

    public User(ResultSet rs) throws SQLException{
        int uid = rs.getInt("uid");
        String email = rs.getString("email");
        String hash = rs.getString("hash");
        String token = rs.getString("token");
        String blocked = rs.getString("blocked");
        int kid = rs.getInt("kid");
        int gid = rs.getInt("gid");
        int nLogins = rs.getInt("nlogins");

        this.uid = uid;
        this.email = email;
        this.hash = hash;
        this.token = token;
        this.blocked = stringToDate(blocked);
        this.kid = kid;
        this.gid = gid;
        this.nLogins = nLogins;
    }

    public static String dateToString(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String strDate = formatter.format(date);
        return strDate;
    }

    public static Date stringToDate(String string){
        if (string.equals(DBQueries.NOT_BLOCKED))
            return null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            Date date = formatter.parse(string);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }
}
