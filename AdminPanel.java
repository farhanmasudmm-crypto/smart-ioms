import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;

public class AdminPanel {
    private ArrayList<String[]> products = new ArrayList<>(Arrays.asList(
        new String[]{"Gaming Laptop", "45", "12", "1200"},
        new String[]{"Mechanical Keyboard", "120", "85", "75"},
        new String[]{"UltraWide Monitor", "30", "15", "450"}
    ));

    private double totalSalesRevenue = 5400.00;
    private Scanner sc = new Scanner(System.in);

    public void showMenu(String adminName) {
        boolean sessionActive = true;
        while (sessionActive) {
            System.out.println("\n--- ADMIN COMMAND CENTER | User: " + adminName.toUpperCase() + " ---");
            System.out.println("1. View Inventory & Sales");
            System.out.println("2. Add New Product");
            System.out.println("3. Delete Product");
            System.out.println("4. Update Price");
            System.out.println("5. Request Supply (Restock)");
            System.out.println("6. Logout");
            System.out.print("Action: ");
            
            int choice = sc.nextInt();
            sc.nextLine(); 

            switch (choice) {
                case 1: displayDashboard(); break;
                case 2: addProduct(); break;
                case 3: deleteProduct(); break;
                case 4: changePrice(); break;
                case 5: requestSupply(); break;
                case 6: sessionActive = false; break;
                default: System.out.println("Invalid Command.");
            }
        }
    }

    private void displayDashboard() {
        System.out.println("\n--- GLOBAL INVENTORY & SALES ---");
        System.out.printf("%-3s | %-20s | %-7s | %-7s | %-7s\n", "ID", "Product", "Wh_A", "Wh_B", "Price");
        for (int i = 0; i < products.size(); i++) {
            String[] p = products.get(i);
            System.out.printf("%-3d | %-20s | %-7s | %-7s | $%-7s\n", i, p[0], p[1], p[2], p[3]);
        }
        System.out.println("---------------------------------------------------------");
        System.out.println("TOTAL SYSTEM REVENUE: $" + totalSalesRevenue);
    }

    private void addProduct() {
        System.out.print("Enter Name: "); String name = sc.nextLine();
        System.out.print("Wh_A Qty: "); String qA = sc.next();
        System.out.print("Wh_B Qty: "); String qB = sc.next();
        System.out.print("Price: "); String pr = sc.next();
        products.add(new String[]{name, qA, qB, pr});
        System.out.println("Product Added successfully.");
    }

    private void deleteProduct() {
        System.out.print("Enter Product ID to delete: ");
        int id = sc.nextInt();
        if (id >= 0 && id < products.size()) {
            products.remove(id);
            System.out.println("Product Deleted.");
        } else {
            System.out.println("Invalid ID.");
        }
    }

    private void changePrice() {
        System.out.print("Enter Product ID: ");
        int id = sc.nextInt();
        System.out.print("Enter New Price: ");
        String newPrice = sc.next();
        if (id >= 0 && id < products.size()) {
            products.get(id)[3] = newPrice;
            System.out.println("Price Updated.");
        }
    }

    private void requestSupply() {
        System.out.print("Enter Product ID to Restock: ");
        int id = sc.nextInt();
        System.out.print("Enter Quantity to Order from Supplier: ");
        int orderQty = sc.nextInt();
        
        if (id >= 0 && id < products.size()) {
            System.out.println("Sending Request to Supplier for " + products.get(id)[0] + "...");
            int currentQty = Integer.parseInt(products.get(id)[1]);
            products.get(id)[1] = String.valueOf(currentQty + orderQty);
            System.out.println("Supply Received! Warehouse A updated.");
        }
    }
}
