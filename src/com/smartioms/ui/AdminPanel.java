package com.smartioms.ui;

import com.smartioms.model.*;
import com.smartioms.service.*;
import com.smartioms.util.ConsoleUtil;
import java.util.List;
import java.util.Scanner;

public class AdminPanel {
    private final User admin;
    private final UserService userService;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final Scanner sc;

    public AdminPanel(User admin, UserService us, ProductService ps, Scanner sc) {
        this.admin = admin; this.userService = us;
        this.productService = ps; this.supplierService = new SupplierService(ps);
        this.sc = sc;
    }

    public void showMenu() {
        int choice;
        do {
            ConsoleMenu.clearScreen();
            ConsoleMenu.drawHeader("\u001B[1;36mADMIN DASHBOARD - Welcome, " + admin.getName() + "\u001B[0m");
            System.out.println("1. Add New Product");
            System.out.println("2. View Inventory & Financial Report");
            System.out.println("3. Supplier Stock Request (Restock)");
            System.out.println("4. Warehouse Management");
            System.out.println("5. User Management (Staff/Admins)");
            System.out.println("6. View Customer Data");
            System.out.println("7. Repay Debts");
            System.out.println("8. Low Stock Alerts");
            System.out.println("\u001B[31m9. Logout\u001B[0m");
            System.out.print("\nSelect Option: ");

            while (!sc.hasNextInt()) { sc.next(); }
            choice = sc.nextInt(); sc.nextLine();

            switch (choice) {
                case 1 -> addNewProduct();
                case 2 -> viewInventoryReport();
                case 3 -> { ConsoleMenu.clearScreen(); supplierService.handleStockRequest(sc); }
                case 4 -> warehouseManagement();
                case 5 -> userManagement();
                case 6 -> viewCustomerData();
                case 7 -> repayDebts();
                case 8 -> viewLowStockAlerts();
                case 9 -> { ConsoleMenu.clearScreen(); System.out.println("Logging out..."); }
            }
            if (choice != 9) { System.out.println("\nPress Enter..."); sc.nextLine(); }
        } while (choice != 9);
    }

    //  VIEW customer DATa
    private void viewCustomerData() {
        ConsoleMenu.clearScreen();
        ConsoleMenu.drawHeader("CUSTOMER DATA");
        List<User> customers = userService.getAllCustomers();
        if (customers.isEmpty()) { System.out.println("No customers registered."); return; }

        System.out.printf("%-4s %-15s %-8s %-10s %-10s %-10s%n",
            "#", "Name", "ID", "Visits", "Orders", "Status");
        System.out.println("-".repeat(65));
        int num = 1;
        for (User c : customers) {
            String status = c.isTrusted() ? "\u001B[32mTRUSTED\u001B[0m" : "Regular";
            System.out.printf("%-4d %-15s %-8d %-10d %-10d %-10s%n",
                num++, c.getName(), c.getUserId(), c.getVisitCount(), c.getPurchaseCount(), status);
        }

        System.out.println("\nView session purchases for a customer?");
        System.out.println("Enter Customer ID (0 to go back): ");
        int cid = sc.nextInt(); sc.nextLine();
        if (cid == 0) return;

        User target = null;
        for (User c : customers) if (c.getUserId() == cid) { target = c; break; }
        if (target == null) { System.out.println("\u001B[31mCustomer not found!\u001B[0m"); return; }

        System.out.printf("\n--- Session Purchases for '%s' ---\n", target.getName());
        List<String> purchases = target.getSessionPurchases();
        if (purchases.isEmpty()) {
            System.out.println("No purchases in current session (or not logged in yet).");
        } else if (purchases.size() == 1 && purchases.get(0).equals("0")) {
            System.out.println("Customer visited but bought nothing this session.");
        } else {
            for (int i = 0; i < purchases.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, purchases.get(i));
            }
        }
    }

    // user mnagement
    private void userManagement() {
        ConsoleMenu.clearScreen();
        ConsoleMenu.drawHeader("USER MANAGEMENT");
        System.out.println("1. View All Users\n2. Add Admin/Staff\n3. Delete User\n4. Back");
        int uChoice = sc.nextInt(); sc.nextLine();
        switch (uChoice) {
            case 1 -> {
                System.out.printf("%-5s %-15s %-10s %-8s %-8s%n", "ID", "Username", "Role", "Visits", "Orders");
                for (User u : userService.getAllUsers())
                    System.out.printf("%-5d %-15s %-10s %-8d %-8d%n",
                        u.getUserId(), u.getName(), u.getRole(), u.getVisitCount(), u.getPurchaseCount());
            }
            case 2 -> {
                System.out.print("Username: "); String name = sc.nextLine();
                String pass = ConsoleUtil.readAndValidatePassword("Password: ", sc);
                String role;
                while (true) {
                    System.out.print("Role (ADMIN/STAFF): "); role = sc.nextLine().toUpperCase().trim();
                    if (role.equals("ADMIN") || role.equals("STAFF")) break;
                    System.out.println("\u001B[31mInvalid role!\u001B[0m");
                }
                if (userService.signup(name, role, pass))
                    System.out.println("\u001B[32mUser '" + name + "' added as " + role + "!\u001B[0m");
                else System.out.println("\u001B[31mFailed!\u001B[0m");
            }
            case 3 -> {
                System.out.print("User ID to delete: "); int id = sc.nextInt(); sc.nextLine();
                if (userService.deleteUser(id)) System.out.println("\u001B[32mDeleted!\u001B[0m");
                else System.out.println("\u001B[31mFailed!\u001B[0m");
            }
        }
    }

    private void addNewProduct() {
        ConsoleMenu.clearScreen();
        ConsoleMenu.drawHeader("ADD NEW PRODUCT");
        System.out.print("Product Name: "); String n = sc.nextLine();
        System.out.print("Category: "); String cat = sc.nextLine();
        System.out.print("Supplier Name: "); String sup = sc.nextLine();
        System.out.print("Unit Cost Price: "); double c = sc.nextDouble();
        System.out.print("Unit Selling Price: "); double sp = sc.nextDouble(); sc.nextLine();
        System.out.println("\n--- Distribute stock across warehouses ---");
        System.out.print("Warehouse 1 qty: "); int w1 = sc.nextInt();
        System.out.print("Warehouse 2 qty: "); int w2 = sc.nextInt();
        System.out.print("Warehouse 3 qty: "); int w3 = sc.nextInt(); sc.nextLine();
        System.out.printf("Total: %d (W1=%d W2=%d W3=%d)%n", w1+w2+w3, w1, w2, w3);
        System.out.print("Payment (C=Cash/L=Loan): "); String t = sc.nextLine().toUpperCase();
        String pt = t.startsWith("L") ? "LOAN" : "CASH";
        productService.addProduct(n, cat, w1, w2, w3, c, sp, sup, pt);
        System.out.println("\u001B[32mProduct added as " + pt + "!\u001B[0m");
    }

    private void warehouseManagement() {
        ConsoleMenu.clearScreen();
        ConsoleMenu.drawHeader("WAREHOUSE MANAGEMENT");
        System.out.println("1. View Stock\n2. Transfer\n3. Update\n4. Back");
        int ch = sc.nextInt(); sc.nextLine();
        switch (ch) { case 1 -> viewWarehouseStock(); case 2 -> transferStock(); case 3 -> updateWarehouseStock(); }
    }

    private void viewWarehouseStock() {
        ConsoleMenu.clearScreen();
        System.out.printf("%-4s %-15s %-8s %-8s %-8s %-8s%n", "ID", "Product", "W1", "W2", "W3", "TOTAL");
        System.out.println("-".repeat(55));
        int t1=0,t2=0,t3=0;
        for (Product p : productService.getAllProducts()) {
            System.out.printf("%-4d %-15s %-8d %-8d %-8d %-8d%n", p.getId(), p.getName(), p.getW1Stock(), p.getW2Stock(), p.getW3Stock(), p.getStock());
            t1+=p.getW1Stock(); t2+=p.getW2Stock(); t3+=p.getW3Stock();
        }
        System.out.println("-".repeat(55));
        System.out.printf("%-4s %-15s %-8d %-8d %-8d %-8d%n", "", "TOTALS", t1, t2, t3, t1+t2+t3);
    }

    private void transferStock() {
        viewWarehouseStock();
        System.out.print("\nProduct ID: "); int pid = sc.nextInt();
        System.out.print("From (1/2/3): "); int f = sc.nextInt();
        System.out.print("To (1/2/3): "); int t = sc.nextInt();
        System.out.print("Qty: "); int q = sc.nextInt(); sc.nextLine();
        if (f==t||f<1||f>3||t<1||t>3) { System.out.println("\u001B[31mInvalid!\u001B[0m"); return; }
        if (productService.transferStock(pid,f,t,q)) System.out.println("\u001B[32mTransferred!\u001B[0m");
        else System.out.println("\u001B[31mFailed!\u001B[0m");
    }

    private void updateWarehouseStock() {
        System.out.print("Product ID: "); int pid = sc.nextInt(); sc.nextLine();
        Product p = productService.getProductById(pid);
        if (p==null) { System.out.println("\u001B[31mNot found!\u001B[0m"); return; }
        System.out.printf("W1=%d W2=%d W3=%d%n", p.getW1Stock(), p.getW2Stock(), p.getW3Stock());
        System.out.print("Warehouse (1/2/3): "); int w = sc.nextInt();
        System.out.print("New qty: "); int q = sc.nextInt(); sc.nextLine();
        if (w<1||w>3) { System.out.println("\u001B[31mInvalid!\u001B[0m"); return; }
        productService.updateWarehouseStock(pid, w, q);
        System.out.println("\u001B[32mUpdated!\u001B[0m");
    }

    private void viewInventoryReport() {
        ConsoleMenu.clearScreen();
        ConsoleMenu.drawHeader("FINANCIAL & INVENTORY REPORT");
        System.out.printf("%-3s %-12s %-10s %-5s %-5s %-5s %-6s %-9s %-9s %-10s %-5s %-10s%n",
            "ID","Name","Cat","W1","W2","W3","Tot","Cost","Sale","Supplier","Type","Loan");
        System.out.println("-".repeat(100));
        double tc=0,tv=0,td=0,tp=0;
        for (Product p : productService.getAllProducts()) {
            double ic=p.getCostPrice()*p.getStock(), iv=p.getSellingPrice()*p.getStock();
            tc+=ic; tv+=iv;
            if (p.getPaymentType().equals("LOAN")) td+=p.getLoanAmount(); else tp+=ic;
            System.out.printf("%-3d %-12s %-10s %-5d %-5d %-5d %-6d %-9.2f %-9.2f %-10s %-5s %-10.2f%n",
                p.getId(),p.getName(),p.getCategory(),p.getW1Stock(),p.getW2Stock(),p.getW3Stock(),p.getStock(),
                p.getCostPrice(),p.getSellingPrice(),p.getSupplierName(),p.getPaymentType(),p.getLoanAmount());
        }
        double sr=productService.getTotalSalesRevenue(), of=productService.getOwnerFundsInjected();
        System.out.println("\n"+"=".repeat(55));
        System.out.println("       ACCOUNTING SUMMARY");
        System.out.println("=".repeat(55));
        System.out.printf("  Sales Revenue : $%.2f%n  Owner Funds   : $%.2f%n  Inventory Cost: $%.2f%n  Inventory Value: $%.2f%n", sr,of,tc,tv);
        System.out.printf("  Cash Paid     : \u001B[32m$%.2f\u001B[0m%n  Debt          : \u001B[31m$%.2f\u001B[0m%n", tp, td);
        double g=sr-tc;
        if (g>=0) System.out.printf("  \u001B[32mGROSS PROFIT: $%.2f\u001B[0m%n", g);
        else System.out.printf("  \u001B[31mGROSS LOSS: $%.2f\u001B[0m%n", g);
    }

    private void repayDebts() {
        ConsoleMenu.clearScreen();
        double td = productService.getTotalOutstandingDebt();
        if (td<=0) { System.out.println("\u001B[32mNo debts!\u001B[0m"); return; }
        for (Product p : productService.getAllProducts())
            if (p.getLoanAmount()>0) System.out.printf("%-5d %-15s $%.2f%n", p.getId(), p.getName(), p.getLoanAmount());
        System.out.println("\n1. From Sales\n2. From Owner\n3. Cancel");
        int s = sc.nextInt(); sc.nextLine(); if (s==3) return;
        System.out.print("Product ID: "); int pid = sc.nextInt(); sc.nextLine();
        Product t = productService.getProductById(pid);
        if (t==null||t.getLoanAmount()<=0) { System.out.println("\u001B[31mNo loan!\u001B[0m"); return; }
        System.out.print("Amount: $"); double a = sc.nextDouble(); sc.nextLine();
        if (a<=0) return;
        if (s==1) {
            if (a>productService.getTotalSalesRevenue()) { System.out.println("\u001B[31mInsufficient!\u001B[0m"); return; }
            double r=productService.repayLoan(pid,a); productService.recordSale(-r);
            System.out.printf("\u001B[32mRepaid $%.2f\u001B[0m%n", r);
        } else {
            productService.addOwnerFunds(a); double r=productService.repayLoan(pid,a);
            System.out.printf("\u001B[32mInjected $%.2f, repaid $%.2f\u001B[0m%n", a, r);
        }
    }

    private void viewLowStockAlerts() {
        ConsoleMenu.clearScreen();
        String[][] al = productService.getLowStockAlerts();
        if (al.length==0) { System.out.println("\u001B[32mAll stocked!\u001B[0m"); return; }
        for (int i=0; i<al.length; i++)
            System.out.printf("\u001B[31m%d. %-15s Total:%-4s %s\u001B[0m%n", i+1, al[i][0], al[i][1], al[i][2]);
    }
}
