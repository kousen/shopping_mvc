package com.kousenit.shopping_mvc.entities;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Integer id) {
        super("Product not found with id " + id);
    }
}
