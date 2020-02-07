package com.kousenit.shopping_mvc.controllers;

import com.kousenit.shopping_mvc.entities.Product;
import com.kousenit.shopping_mvc.entities.ProductNotFoundException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("GrazieInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Profile("test")
class ProductRestControllerTest {
    @Autowired
    private TestRestTemplate template;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<Integer> getIds() {
        return jdbcTemplate.query("select id from products",
                (rs, rowNum) -> rs.getInt("id"));
    }

    @Test
    void getAll() {

    }

    @Test
    void getSingleProductThatExists() {
        List<Integer> ids = getIds();
        ids.forEach(id -> {
                    Product product = template.getForObject("/rest/{id}", Product.class, id);
                    assertAll(
                            () -> assertNotNull(product.getId()),
                            () -> assertTrue(product.getName().length() > 0),
                            () -> assertTrue(product.getPrice() >= 0.0)
                    );
                }
        );
    }

    @Test
    void getSingleProductThatDoesNotExist() {
        List<Integer> ids = getIds();
        assertFalse(ids.contains(999));

        assertThrows(RestClientException.class,
                () -> template.getForEntity("/rest/{id}", Product.class, 999));
    }

    @Test
    void insertProduct() {
        Product product = new Product();
        product.setName("baseball bat");
        product.setPrice(20.97);

        ResponseEntity<Product> response = template.postForEntity("/rest", product, Product.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Product savedProduct = response.getBody();
        assertAll(
                () -> assertEquals(product.getName(), savedProduct.getName()),
                () -> assertEquals(product.getPrice(), savedProduct.getPrice(), 0.01),
                () -> assertNotNull(savedProduct.getId()));
    }

    @Test
    void updateProduct() {
        List<Integer> ids = getIds();
        Product product = template.getForObject("/rest/{id}", Product.class, ids.get(0));
        product.setPrice(product.getPrice() * 1.10);
        template.put("/rest/{id}", product, product.getId());
    }


    @Test
    void getProductsWithMinimumPrice() {

    }

    @Test @Disabled
    void deleteSingleProduct() {
        List<Integer> ids = getIds();
        // delete them all
        ids.forEach(id -> template.delete("/rest/{id}", id));
        // check that they're gone
        ResponseEntity<Product[]> entity = template.getForEntity(
                "/products", Product[].class);
        assertEquals(0, entity.getBody().length);
        // rely on automatic rollback to restore them
    }

    @Test
    void deleteAllProducts() {
        List<Integer> ids = getIds();
        assertTrue(ids.size() > 0);
        template.delete("/rest");
        ids = getIds();
        assertEquals(0, ids.size());
    }
}