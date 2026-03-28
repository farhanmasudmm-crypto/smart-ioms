package com.smartioms.service;

import com.smartioms.model.Product;
import java.io.*;
import java.util.*;

public class ProductService {
    private List<Product> products = new ArrayList<>();
    private final String folderPath;
    private final String fileName = "products.txt";

    private String[][] lowStockAlerts = new String[0][2];
    private static final int LOW_STOCK_THRESHOLD = 5;

    private double totalSalesRevenue = 0.0;
    private double ownerFundsInjected = 0.0;

    public ProductService() {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();
        this.folderPath = dir.getAbsolutePath();
        System.out.println("\u001B[36m[INIT] Data directory: " + folderPath + "\u001B[0m");
        loadProducts();
        loadFinanceData();
        rebuildLowStockAlerts();
    }

    private void loadProducts() {
        File file = new File(folderPath, fileName);
        if (!file.exists()) {
            System.out.println("\u001B[33m[INIT] No products.txt found\u001B[0m");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            products.clear();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length == 11) {
                    // Warehouse : id,name,cat,w1,w2,w3,cost,sale,supplier,payType,loan
                    products.add(new Product(
                        Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(),
                        Integer.parseInt(p[3].trim()), Integer.parseInt(p[4].trim()), Integer.parseInt(p[5].trim()),
                        Double.parseDouble(p[6].trim()), Double.parseDouble(p[7].trim()), p[8].trim(),
                        p[9].trim(), Double.parseDouble(p[10].trim())
                    ));
                } else if (p.length == 9) {
                    // Old format: id,name,cat,totalStock,cost,sale,supplier,payType,loan
                    int total = Integer.parseInt(p[3].trim());
                    products.add(new Product(
                        Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(),
                        total, 0, 0,
                        Double.parseDouble(p[4].trim()), Double.parseDouble(p[5].trim()), p[6].trim(),
                        p[7].trim(), Double.parseDouble(p[8].trim())
                    ));
                } else if (p.length == 7) {
                    int total = Integer.parseInt(p[3].trim());
                    products.add(new Product(
                        Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(),
                        total, 0, 0,
                        Double.parseDouble(p[4].trim()), Double.parseDouble(p[5].trim()), p[6].trim(),
                        "CASH", 0.0
                    ));
                } else if (p.length == 5) {
                    int total = Integer.parseInt(p[3].trim());
                    products.add(new Product(
                        Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(),
                        total, 0, 0,
                        Double.parseDouble(p[4].trim()), Double.parseDouble(p[4].trim()), "Unknown",
                        "CASH", 0.0
                    ));
                }
            }
            System.out.println("\u001B[36m[INIT] Loaded " + products.size() + " products\u001B[0m");
        } catch (IOException e) {
            System.out.println("Read Error: " + e.getMessage());
        }
    }

    public synchronized void saveProducts() {
        File file = new File(folderPath, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (Product p : products) {
                String record = String.format("%d,%s,%s,%d,%d,%d,%.2f,%.2f,%s,%s,%.2f",
                    p.getId(), p.getName(), p.getCategory(),
                    p.getW1Stock(), p.getW2Stock(), p.getW3Stock(),
                    p.getCostPrice(), p.getSellingPrice(), p.getSupplierName(),
                    p.getPaymentType(), p.getLoanAmount());
                writer.write(record);
                writer.newLine();
            }
            writer.flush();
            System.out.println("\u001B[32m[SYSTEM]: Products saved\u001B[0m");
        } catch (IOException e) {
            System.out.println("\u001B[31mWRITE ERROR: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void loadFinanceData() {
        File file = new File(folderPath, "finance.txt");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    totalSalesRevenue = Double.parseDouble(parts[0].trim());
                    ownerFundsInjected = Double.parseDouble(parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Finance Read Error: " + e.getMessage());
        }
    }

    private void saveFinanceData() {
        File file = new File(folderPath, "finance.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(String.format("%.2f,%.2f", totalSalesRevenue, ownerFundsInjected));
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.out.println("\u001B[31mFinance Write Error: " + e.getMessage() + "\u001B[0m");
        }
    }

    // =================== PRODUCT MANAGEMENT ===================

    public void addProduct(String name, String cat, int w1, int w2, int w3,
                           double cost, double sale, String supplier, String paymentType) {
        int id = products.isEmpty() ? 1 : products.get(products.size() - 1).getId() + 1;
        int totalStock = w1 + w2 + w3;
        double loanAmount = paymentType.equals("LOAN") ? cost * totalStock : 0.0;
        products.add(new Product(id, name, cat, w1, w2, w3, cost, sale, supplier, paymentType, loanAmount));
        saveProducts();
        rebuildLowStockAlerts();
    }

    // Backward compatible: all stock goes to W1
    public void addProduct(String name, String cat, int stock, double cost, double sale, String supplier) {
        addProduct(name, cat, stock, 0, 0, cost, sale, supplier, "CASH");
    }

    public void updateStock(int id, int newTotalStock) {
        for (Product p : products) {
            if (p.getId() == id) {
                p.setW1Stock(newTotalStock);
                p.setW2Stock(0);
                p.setW3Stock(0);
                saveProducts();
                rebuildLowStockAlerts();
                return;
            }
        }
    }

    public void updateWarehouseStock(int productId, int warehouseNum, int newQty) {
        for (Product p : products) {
            if (p.getId() == productId) {
                p.setWarehouseStock(warehouseNum, newQty);
                saveProducts();
                rebuildLowStockAlerts();
                return;
            }
        }
    }

    public boolean transferStock(int productId, int fromW, int toW, int qty) {
        Product p = getProductById(productId);
        if (p == null) return false;
        int available = p.getWarehouseStock(fromW);
        if (qty > available || qty <= 0) return false;
        p.setWarehouseStock(fromW, available - qty);
        p.setWarehouseStock(toW, p.getWarehouseStock(toW) + qty);
        saveProducts();
        rebuildLowStockAlerts();
        return true;
    }

    public boolean deductStockForSale(int productId, int qty) {
        Product p = getProductById(productId);
        if (p == null) return false;
        boolean success = p.deductStock(qty);
        if (success) {
            saveProducts();
            rebuildLowStockAlerts();
        }
        return success;
    }

    // =================== SALES & FINANCE ===================

    public void recordSale(double amount) {
        totalSalesRevenue += amount;
        saveFinanceData();
        System.out.println("\u001B[32m[SALE] $" + String.format("%.2f", amount) +
            " | Total revenue: $" + String.format("%.2f", totalSalesRevenue) + "\u001B[0m");
    }

    public double getTotalSalesRevenue() { return totalSalesRevenue; }
    public void addOwnerFunds(double amount) { ownerFundsInjected += amount; saveFinanceData(); }
    public double getOwnerFundsInjected() { return ownerFundsInjected; }

    public double repayLoan(int productId, double amount) {
        for (Product p : products) {
            if (p.getId() == productId) {
                if (p.getLoanAmount() <= 0) return 0;
                double repay = Math.min(amount, p.getLoanAmount());
                p.setLoanAmount(p.getLoanAmount() - repay);
                if (p.getLoanAmount() <= 0) { p.setPaymentType("CASH"); p.setLoanAmount(0); }
                saveProducts();
                return repay;
            }
        }
        return 0;
    }

    public double getTotalOutstandingDebt() {
        double d = 0; for (Product p : products) d += p.getLoanAmount(); return d;
    }

    public double getAvailableCash() { return totalSalesRevenue + ownerFundsInjected; }

    // =================== LOW STOCK ALERTS ===================

    public void rebuildLowStockAlerts() {
        List<String[]> alerts = new ArrayList<>();
        for (Product p : products) {
            if (p.getStock() < LOW_STOCK_THRESHOLD) {
                alerts.add(new String[]{ p.getName(),
                    String.valueOf(p.getStock()),
                    "W1=" + p.getW1Stock() + " W2=" + p.getW2Stock() + " W3=" + p.getW3Stock() });
            }
        }
        lowStockAlerts = alerts.toArray(new String[0][3]);
    }

    public String[][] getLowStockAlerts() { return lowStockAlerts; }
    public List<Product> getAllProducts() { return products; }

    public Product getProductById(int id) {
        return products.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }
}
