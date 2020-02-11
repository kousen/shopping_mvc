package com.kousenit.shopping_mvc.controllers;

import com.kousenit.shopping_mvc.dao.ProductRepository;
import com.kousenit.shopping_mvc.entities.Product;
import com.kousenit.shopping_mvc.entities.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rest")
public class ProductRestController {
    private ProductRepository repository;

    public ProductRestController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Product> getAllProducts(
            @RequestParam(required = false) Double minimumPrice) {
        if (minimumPrice != null) {
            return repository.findAllByPriceGreaterThanEqual(minimumPrice);
        }
        return repository.findAll();
    }

    @GetMapping("{id}")
    public Product getProduct(@PathVariable("id") Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Product> insertProduct(@RequestBody Product product) {
        product = repository.save(product);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.getId())
                .toUri();
        return ResponseEntity.created(uri).body(product);
    }

    @PutMapping("{id}")
    public Product updateOrInsertProduct(@PathVariable Integer id,
                                 @RequestBody Product newProduct) {
        return repository.findById(id).map(product -> {
                    product.setName(newProduct.getName());
                    product.setPrice(newProduct.getPrice());
                    return repository.save(product);
                }).orElseGet(() -> {
                   newProduct.setId(id);
                   return repository.save(newProduct);
                });
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        Optional<Product> existingProduct = repository.findById(id);
        if (existingProduct.isPresent()) {
            repository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllProducts() {
        repository.deleteAll();
    }
}
