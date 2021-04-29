package com.store.inventory.models;

public class Product {

    public String product_id;
    public String product_name;
    public String product_category;
    public String product_image;
    public String product_qauntity;
    public String product_selling_price;
    public String product_buying_price;
    public String product_specs;
    public String supplier_name;
    public String supplier_phone;
    public String supplier_id;

    public Product() {

    }

    public Product(String product_name, String product_qauntity, String product_selling_price,String product_buying_price,
                   String product_specs,String product_category,
                   String supplier_id) {
        this.product_name = product_name;
        this.product_qauntity = product_qauntity;
        this.product_selling_price = product_selling_price;
        this.product_buying_price = product_buying_price;
        this.product_specs = product_specs;
        this.product_category = product_category;
        this.supplier_id = supplier_id;
    }

    public Product(String product_name, String product_qauntity, String product_selling_price, String product_buying_price,
                   String product_specs, String product_image,
                   String product_category, String supplier_id) {
        this.product_name = product_name;
        this.product_qauntity = product_qauntity;
        this.product_selling_price = product_selling_price;
        this.product_buying_price = product_buying_price;
        this.product_specs = product_specs;
        this.product_image = product_image;
        this.product_category = product_category;
        this.supplier_id = supplier_id;
    }

    public String getProduct_id() {
        return product_id;
    }
    public void setProduct_id(String id) {
        this.product_id = id;
    }

    public String getProduct_category() {
        return product_category;
    }
    public void setProduct_category(String name) {
        this.product_category = name;
    }

    public String getProduct_name() {
        return product_name;
    }
    public void setProduct_name(String name) {
        this.product_name = name;
    }

    public String getProduct_image() {
        return product_image;
    }
    public void setProduct_image(String name) {
        this.product_image = name;
    }

    public String getProduct_qauntity() {return product_qauntity;}
    public void setProduct_qauntity(String name) {
        this.product_qauntity = name;
    }

    public String getProduct_specs() {
        return product_specs;
    }
    public void setProduct_specs(String name) {
        this.product_specs = name;
    }

    public String getProduct_selling_price() {
        return product_selling_price;
    }
    public void setProduct_selling_price(String name) {
        this.product_selling_price = name;
    }

    public String getProduct_buying_price() {
        return product_buying_price;
    }
    public void setProduct_buying_price(String name) {
        this.product_buying_price = name;
    }

    public String getSupplier_id() {
        return supplier_id;
    }
    public void setSupplier_id(String name) {
        this.supplier_id = name;
    }

    public String getSupplier_name() {
        return supplier_name;
    }
    public void setSupplier_name(String name) {
        this.supplier_name = name;
    }

    public String getSupplier_phone() {
        return supplier_phone;
    }
    public void setSupplier_phone(String name) {
        this.supplier_phone = name;
    }


}
