package com.smartioms.model;

public class Product {
    private int id;
    private String name;
    private String category;
    private int w1Stock;
    private int w2Stock;
    private int w3Stock;
    private double costPrice;
    private double sellingPrice;
    private String supplierName;
    private String paymentType;
    private double loanAmount;

    public Product(int id, String name, String category,
                   int w1, int w2, int w3,
                   double cost, double sale, String supplier,
                   String paymentType, double loanAmount) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.w1Stock = w1;
        this.w2Stock = w2;
        this.w3Stock = w3;
        this.costPrice = cost;
        this.sellingPrice = sale;
        this.supplierName = supplier;
        this.paymentType = paymentType;
        this.loanAmount = loanAmount;
    }

    public int getStock() { return w1Stock + w2Stock + w3Stock; }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getW1Stock() { return w1Stock; }
    public int getW2Stock() { return w2Stock; }
    public int getW3Stock() { return w3Stock; }
    public double getCostPrice() { return costPrice; }
    public double getSellingPrice() { return sellingPrice; }
    public String getSupplierName() { return supplierName; }
    public String getPaymentType() { return paymentType; }
    public double getLoanAmount() { return loanAmount; }

    public void setW1Stock(int s) { this.w1Stock = s; }
    public void setW2Stock(int s) { this.w2Stock = s; }
    public void setW3Stock(int s) { this.w3Stock = s; }
    public void setCostPrice(double c) { this.costPrice = c; }
    public void setPaymentType(String t) { this.paymentType = t; }
    public void setLoanAmount(double a) { this.loanAmount = a; }

    public void setWarehouseStock(int wNum, int qty) {
        switch (wNum) {
            case 1 -> w1Stock = qty;
            case 2 -> w2Stock = qty;
            case 3 -> w3Stock = qty;
        }
    }

    public int getWarehouseStock(int wNum) {
        return switch (wNum) {
            case 1 -> w1Stock;
            case 2 -> w2Stock;
            case 3 -> w3Stock;
            default -> 0;
        };
    }

    public boolean deductStock(int qty) {
        if (qty > getStock()) return false;
        int remaining = qty;
        while (remaining > 0) {
            if (w1Stock >= w2Stock && w1Stock >= w3Stock && w1Stock > 0) {
                int take = Math.min(remaining, w1Stock);
                w1Stock -= take; remaining -= take;
            } else if (w2Stock >= w1Stock && w2Stock >= w3Stock && w2Stock > 0) {
                int take = Math.min(remaining, w2Stock);
                w2Stock -= take; remaining -= take;
            } else if (w3Stock > 0) {
                int take = Math.min(remaining, w3Stock);
                w3Stock -= take; remaining -= take;
            } else { break; }
        }
        return remaining == 0;
    }
}
