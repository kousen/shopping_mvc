package com.kousenit.shopping_mvc.controllers;

import com.kousenit.shopping_mvc.entities.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"GrazieInspection", "SqlResolve", "SqlNoDataSourceInspection"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Profile("test")
class ProductRestControllerTest {
    @LocalServerPort
    private int randomServerPort;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<Integer> getIds() {
        return jdbcTemplate.query("select id from products",
                (rs, rowNum) -> rs.getInt("id"));
    }

    // Odd contortions you need to go through to get a List<Product>
    private List<Product> getProducts(Double minPrice) {
        String url = "http://localhost:" + randomServerPort + "/rest";
        if (minPrice != null) {
            url += "?minimumPrice=" + minPrice;
        }
        ResponseEntity<List<Product>> productsEntity =
                template.exchange(url, HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {});
        return productsEntity.getBody();
    }

    @Test
    void getAll() {
        List<Product> products = getProducts(null);
        assertNotNull(products);
        assertEquals(4, products.size());
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
                () -> assertNotNull(savedProduct),
                () -> {
                    assert savedProduct != null;
                    assertEquals(product.getName(), savedProduct.getName());
                },
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
        double min = 12.0;
        List<Product> products = getProducts(min);
        assertNotNull(products);
        assertEquals(2, products.size());
        products.forEach(product -> assertTrue(product.getPrice() > min));
    }

    @Test
    void deleteSingleProduct() {
        List<Integer> ids = getIds();
        // delete them all
        ids.forEach(id -> template.delete("/rest/{id}", id));
        // check that they're gone
        List<Product> products = getProducts(null);
        assertEquals(0, products.size());
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