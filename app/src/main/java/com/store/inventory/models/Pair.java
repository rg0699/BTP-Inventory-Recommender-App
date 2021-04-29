package com.store.inventory.models;

public class Pair {

    public Double key;
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
