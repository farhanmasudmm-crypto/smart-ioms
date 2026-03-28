

package com.smartioms.ui;

public class ConsoleMenu {
    public static void clearScreen() {
        // Flushes the console and moves cursor to home position
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void drawHeader(String title) {
        System.out.println("\u001B[1;35m\n===============================\u001B[0m");
        System.out.println("    <<<< " + title + " >>>>");
        System.out.println("\u001B[1;35m===============================\u001B[0m");
    }
}
