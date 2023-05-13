// package DigitalVault_INF1416.main;a

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PasswordManager {

    public static void main(String[] args){
        passwordInput();
    }

    static class PasswordCombination {
        Character dig1;
        Character dig2;

        public PasswordCombination(Character dig1, Character dig2) {
            this.dig1 = dig1;
            this.dig2 = dig2;
        }
    }

    static class PasswordNode {
        Character value;
        PasswordNode dir;
        PasswordNode esq;

        public PasswordNode(Character value) {
            this.value = value;
        }

        public PasswordNode(Character value, PasswordNode dir, PasswordNode esq) {
            this.value = value;
            this.dir = dir;
            this.esq = esq;
        }

        private static void printTree(PasswordNode node, int h) {
            if (node == null) {
                return;
            }
    
            printTree(node.dir, h + 1);
    
            for (int i = 0; i < h; i++) {
                System.out.print("    ");
            }
    
            System.out.println(node.value);
    
            printTree(node.esq, h + 1);
        }
    }

    public static void passwordInput() {
        System.out.println("Insira sua senha: ( 'a', 'b', 'c', 'd', 'e' para escolher o campo e 'q' para submeter");
        Character[] possibleInputs = {'a', 'b', 'c', 'd', 'e', 'q'};
        Scanner scanner = new Scanner(System.in);
        Character input = ' ';
        ArrayList<PasswordCombination> userPassword = new ArrayList<PasswordCombination>();

        while(true) {
            Map<Character, PasswordCombination> keyboard = PasswordManager.createPasswordKeyboard();

            for (Map.Entry<Character, PasswordCombination> entry : keyboard.entrySet()) {
                Character key = entry.getKey();
                PasswordCombination value = entry.getValue();
                System.out.println("(" + key + ") -> " + "|" + value.dig1 + " " + value.dig2 +  "|");
            }

            input = scanner.next().charAt(0);
            boolean isValidInput = false;

            for (Character possibleInput : possibleInputs) {
                if(input.equals(possibleInput)) {
                    isValidInput = true;
                }
            }

            if(isValidInput) {
                if (input.equals('q')) break;
                PasswordCombination passComb = keyboard.get(input);
                userPassword.add(passComb);
            } else {
                System.out.println("Entrada Inválida!!! Somente 'a', 'b', 'c', 'd', 'e' ou 'q'");
            }
        };
    
        String[] passwords = PasswordManager.createAllPasswordsCombinations(userPassword);
        System.out.println(Arrays.toString(passwords));
    }

    public static Map<Character, PasswordCombination> createPasswordKeyboard() {
        ArrayList<Character> digits = new ArrayList<Character>();
        char[] charDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        for (int i = 0; i < 10; i++) {
            digits.add(charDigits[i]);
        }
        PasswordCombination[] passwordKeyboard = new PasswordCombination[5];
        Collections.shuffle(digits);

        for(int i = 0; i < 5; i++) {
            Character dig1 = digits.get(2 * i);
            Character dig2 = digits.get((2 * i) + 1);
            PasswordCombination pc = new PasswordCombination(dig1, dig2);
            passwordKeyboard[i] = pc;
        }

        Character[] keyboardCharcatersInput = {'a', 'b', 'c', 'd', 'e'};

        Map<Character, PasswordCombination> keyboard = new HashMap<Character, PasswordCombination>();
        for (int i = 0; i < keyboardCharcatersInput.length; i++) {
            Character input = keyboardCharcatersInput[i];
            PasswordCombination output = passwordKeyboard[i];
            keyboard.put(input, output);
        }


        return keyboard;
    }

    public static String[] createAllPasswordsCombinations(ArrayList<PasswordCombination> rawPassword) {
        PasswordNode passTree = new PasswordNode('@');
        addNodes(passTree, rawPassword, 0);
        // PasswordNode.printTree(passTree, rawPassword.size());

        ArrayList<ArrayList<Character>> paths = findAllPaths(passTree);
        
        String[] passwords = new String[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            ArrayList<Character> passwordArray = paths.get(i);
            passwordArray.remove(0);

            StringBuilder sb = new StringBuilder();
            for (Character c : passwordArray) {
                sb.append(c);
            }

            passwords[i] = sb.toString();

        }

        return passwords;
    }

    public static void addNodes(PasswordNode node, ArrayList<PasswordCombination> rawPassword, int h) {
        if(h >= rawPassword.size()) {
            return;
        } else {
            PasswordCombination passComb = rawPassword.get(h);
            PasswordNode dirNode = new PasswordNode(passComb.dig1);
            PasswordNode esqNode = new PasswordNode(passComb.dig2);
            
            node.dir = dirNode;
            addNodes(node.dir, rawPassword, h + 1);

            node.esq = esqNode;
            addNodes(node.esq, rawPassword, h + 1);
        }
    }

    public static ArrayList<ArrayList<Character>> findAllPaths(PasswordNode root) {
        ArrayList<ArrayList<Character>> paths = new ArrayList<ArrayList<Character>>();
        if (root == null) {
            return paths;
        }
        ArrayList<Character> currentPath = new ArrayList<Character>();
        dfs(root, currentPath, paths);
        return paths;
    }
    
    private static void dfs(PasswordNode node, ArrayList<Character> currentPath, ArrayList<ArrayList<Character>> paths) {
        if (node == null) return;

        currentPath.add(node.value);
        
        if (node.esq == null && node.dir == null) {
            paths.add(new ArrayList<Character>(currentPath));
        } else {
            dfs(node.esq, currentPath, paths);
            dfs(node.dir, currentPath, paths);
        }
        currentPath.remove(currentPath.size() - 1);
    }


}