package com.store.inventory.models;

public class Pair extends Product {

    public Double key;
    //public Product product;
    public String productId;

    public Pair() {

    }

    public Pair(Double key, String productId)
    {
        this.key   = key;
        this.productId = productId;
    }

    public Double getKey()   { return key; }
    public String getProductId() { return productId; }
}
