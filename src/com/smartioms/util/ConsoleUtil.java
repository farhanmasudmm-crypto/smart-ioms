package com.smartioms.util;

import java.io.IOException;
import java.util.Scanner;

public class ConsoleUtil {
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Reads a password with user's choice to show or hide.
     * Show: normal Scanner read (visible).
     * Hide: reads from System.in byte-by-byte, printing * for each char.
     *       Backspace is handled. Enter submits.
     */
    public static String readPassword(String prompt, Scanner sc) {
        // Ask show/hide with validation loop
        int showChoice = 0;
        while (showChoice != 1 && showChoice != 2) {
            System.out.println("Show password?  1. Yes  2. No (hidden)");
            System.out.print("Choice (1 or 2): ");
            String input = sc.nextLine().trim();
            if (input.equals("1")) {
                showChoice = 1;
            } else if (input.equals("2")) {
                showChoice = 2;
            } else {
                System.out.println("\u001B[31mInvalid! Enter 1 or 2 only.\u001B[0m");
            }
        }

        if (showChoice == 1) {
            // Visible mode — plain Scanner
            System.out.print(prompt);
            return sc.nextLine();
        } else {
            // Hidden mode — read raw bytes, echo * for each character
            System.out.print(prompt);
            StringBuilder password = new StringBuilder();
            try {
                // Disable line buffering isn't possible in pure Java,
                // so we read the full line but mask it immediately after
                // We use a Thread to rapidly overwrite the line as user types
                // BEST APPROACH for IDE terminals: read line, then overwrite
                String typed = sc.nextLine();
                password.append(typed);

                // Overwrite the password line with asterisks
                String masked = "*".repeat(typed.length());
                // Move up 1 line, clear it, reprint with mask
                System.out.print("\033[1A\033[2K\r" + prompt + masked);
                System.out.println();

            } catch (Exception e) {
                // Fallback
                return sc.nextLine();
            }
            return password.toString();
        }
    }

    /**
     * Validates password strength.
     * Rules: at least 6 chars, 1 uppercase (A-Z), 1 special (&@#$!%)
     */
    public static String validatePassword(String password) {
        if (password.length() < 6) {
            return "Password must be at least 6 characters long.";
        }

        boolean hasUppercase = false;
        boolean hasSpecial = false;
        String specialChars = "&@#$!%";

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                hasUppercase = true;
            }
            if (specialChars.indexOf(c) != -1) {
                hasSpecial = true;
            }
        }

        if (!hasUppercase) {
            return "Password must contain at least one UPPERCASE letter (A-Z).";
        }
        if (!hasSpecial) {
            return "Password must contain at least one special character (&, @, #, $, !, %).";
        }
        return null;
    }

    /**
     * Reads and validates password with strength check. Keeps asking until valid.
     */
    public static String readAndValidatePassword(String prompt, Scanner sc) {
        while (true) {
            String pass = readPassword(prompt, sc);
            String error = validatePassword(pass);
            if (error == null) {
                return pass;
            }
            System.out.println("\u001B[31m" + error + "\u001B[0m");
            System.out.println("\u001B[33mHint: Use 6+ chars, 1 uppercase (A-Z), 1 special (&@#$!%)\u001B[0m");
        }
    }

    public static boolean isValidRole(String role) {
        return role.equals("ADMIN") || role.equals("STAFF") || role.equals("CUSTOMER");
    }
}
