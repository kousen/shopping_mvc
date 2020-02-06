package com.kousenit.shopping_mvc.config;

import com.kousenit.shopping_mvc.dao.ProductRepository;
import com.kousenit.shopping_mvc.entities.Product;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Profile({"prod", "dev"})
public class AppInit implements CommandLineRunner {
    private final ProductRepository repository;

    public AppInit(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() == 0) {
            repository.saveAll(Arrays.asList(
                    new Product("baseball", 9.99),
                    new Product("football", 14.95),
                    new Product("basketball", 11.99)));
        }
    }
}
