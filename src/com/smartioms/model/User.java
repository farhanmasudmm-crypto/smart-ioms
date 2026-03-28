package com.smartioms.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String name;
    private String role;
    private String password;
    private int visitCount;       // total logins
    private int purchaseCount;    // total sessions where user bought something

    // Current session purchases: each entry = "productName x qty = $total"
    private List<String> sessionPurchases = new ArrayList<>();

    public User(int id, String name, String role, String password, int visitCount, int purchaseCount) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.password = password;
        this.visitCount = visitCount;
        this.purchaseCount = purchaseCount;
    }

    // Backward-compatible constructor
    public User(int id, String name, String role, String password) {
        this(id, name, role, password, 0, 0);
    }

    public int getUserId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getPassword() { return password; }
    public int getVisitCount() { return visitCount; }
    public int getPurchaseCount() { return purchaseCount; }
    public List<String> getSessionPurchases() { return sessionPurchases; }

    public void incrementVisit() { visitCount++; }
    public void incrementPurchase() { purchaseCount++; }

    public void addSessionPurchase(String entry) { sessionPurchases.add(entry); }
    public void clearSessionPurchases() { sessionPurchases.clear(); }

    /** Customer is "trusted" if they have ordered more than 5 times */
    public boolean isTrusted() { return purchaseCount > 5; }

    /** Loyal discount: 5% if ordered more than 5 times */
    public double getLoyaltyDiscount() { return isTrusted() ? 0.05 : 0.0; }
}
