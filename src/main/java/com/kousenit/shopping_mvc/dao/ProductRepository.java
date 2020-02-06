package com.kousenit.shopping_mvc.dao;

import com.kousenit.shopping_mvc.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ProductRepository extends JpaRepository<Product,Integer> {
    List<Product> findAllByPriceGreaterThanEqual(double amount);
}
