package DigitalVault_INF1416.db;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

public class LogManager {
    static public class RegisterComparator implements Comparator<Register> {
        @Override
        public int compare(Register o1, Register o2) {
            return o1.date.compareTo(o2.date);
        }
    }
    public static void main(String[] args) {
        try {
            readAllRegiters();
        } catch (Exception e) {
            System.out.println("Falha ao ler registros");
        }
    }

    public static void addRegister(int mid, String email, String filename){
        try {
            Date now = new Date();
            String nowString = Register.dateToString(now);
            Random rand = new Random();
            int rid = rand.nextInt();
            DBQueries.insertRegister(rid, nowString, mid, email, filename);
        } catch (Exception e) {
            System.out.println("Falha ao resgistrar evento!");
        }
    }

    private static void readAllRegiters() throws SQLException, IOException{
        ArrayList<Register> registers = DBQueries.selectAllRegisters();
        Collections.sort(registers, new RegisterComparator());
        FileWriter writer = new FileWriter("log.txt");
        for (Register register: registers){
            writer.write(Register.dateToString(register.date) + " - " + register.log + "\n");
        }
        writer.close();
    }
}


