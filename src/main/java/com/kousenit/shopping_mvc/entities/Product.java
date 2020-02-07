package com.kousenit.shopping_mvc.entities;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "products")
@Data
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "A name is required")
    private String name;

    @DecimalMin(value = "0.0", message = "Price must be greater than zero")
    private double price;

    public Product() {}

    public Product(String name, double price) {
        this(null, name, price);
    }

    public Product(Integer id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
