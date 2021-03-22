package com.store.inventory.models;

public class Pair
{
    private final Double key;
    private Product product;

    public Pair(Double aKey, Product aproduct)
    {
        key   = aKey;
        product = aproduct;
    }

    public Double getKey()   { return key; }
    public Product getProduct() { return product; }
}
