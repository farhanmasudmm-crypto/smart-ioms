package com.smartioms.service;

import com.smartioms.model.User;
import com.smartioms.util.ConsoleUtil;
import java.io.*;
import java.util.*;

public class UserService {
    private List<User> users = new ArrayList<>();
    private final String folderPath;

    public UserService() {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();
        this.folderPath = dir.getAbsolutePath();
        loadUsers();
    }

    public void addDefaultAdmin() {
        // Only add if no admin exists
        for (User u : users) {
            if (u.getRole().equals("ADMIN")) return;
        }
        users.add(new User(getNextId(), "atik", "ADMIN", "atik123", 0, 0));
        saveUsers();
    }

    private int getNextId() {
        int max = 0;
        for (User u : users) {
            if (u.getUserId() > max) max = u.getUserId();
        }
        return max + 1;
    }

    // =================== FILE I/O ===================
    // Format: id,name,role,password,visitCount,purchaseCount
    private void loadUsers() {
        File file = new File(folderPath, "users.txt");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            users.clear();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length == 6) {
                    users.add(new User(
                        Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(), p[3].trim(),
                        Integer.parseInt(p[4].trim()), Integer.parseInt(p[5].trim())
                    ));
                } else if (p.length == 4) {
                    // Legacy format without visit/purchase counts
                    users.add(new User(
                        Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(), p[3].trim(), 0, 0
                    ));
                }
            }
            System.out.println("\u001B[36m[INIT] Loaded " + users.size() + " users\u001B[0m");
        } catch (IOException e) {
            System.out.println("User Read Error: " + e.getMessage());
        }
    }

    public void saveUsers() {
        File file = new File(folderPath, "users.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (User u : users) {
                writer.write(String.format("%d,%s,%s,%s,%d,%d",
                    u.getUserId(), u.getName(), u.getRole(), u.getPassword(),
                    u.getVisitCount(), u.getPurchaseCount()));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("\u001B[31mUser Write Error: " + e.getMessage() + "\u001B[0m");
        }
    }

    // =================== AUTH ===================
    public User login(String name, String pass) {
        for (User u : users) {
            if (u.getName().equals(name) && u.getPassword().equals(pass)) {
                u.incrementVisit();
                saveUsers();
                return u;
            }
        }
        return null;
    }

    public boolean signup(String name, String role, String password) {
        String upperRole = role.toUpperCase().trim();
        if (!ConsoleUtil.isValidRole(upperRole)) return false;
        users.add(new User(getNextId(), name, upperRole, password, 0, 0));
        saveUsers();
        return true;
    }

    public List<User> getAllUsers() { return users; }

    public List<User> getAllCustomers() {
        List<User> customers = new ArrayList<>();
        for (User u : users) {
            if (u.getRole().equals("CUSTOMER")) customers.add(u);
        }
        return customers;
    }

    public boolean deleteUser(int userId) {
        if (userId == 1) {
            System.out.println("\u001B[31mError: Cannot delete the primary System Administrator!\u001B[0m");
            return false;
        }
        boolean removed = users.removeIf(u -> u.getUserId() == userId);
        if (removed) saveUsers();
        return removed;
    }

    /** Call after a customer completes a purchase */
    public void recordPurchase(User customer) {
        customer.incrementPurchase();
        saveUsers();
    }
}
