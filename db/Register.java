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

public class Register {
    public int rid;
    public Date date;
    public String log;

    public Register(ResultSet rs) throws SQLException{
        int rid = rs.getInt("rid");
        int mid = rs.getInt("mid");
        String email = rs.getString("email");
        String filename = rs.getString("filename");
        String date = rs.getString("date");

        this.rid = rid;
        this.date = stringToDate(date);
        this.log = createLog(mid, email, filename);
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

    public static String createLog(int mid, String email, String filename) throws SQLException{
        String description = DBQueries.selectDescription(mid);
        String log = description.replace("<arq_name>", filename);
        log = log.replace("<login_name>", email);
        return log;
    }
}
