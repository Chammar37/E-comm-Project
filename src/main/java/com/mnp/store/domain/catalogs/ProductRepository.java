package com.mnp.store.domain.catalogs;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findOneByISBNIgnoreCase(String isbn);

    Optional<Product> findOneByCategoryIgnoreCase(String category);



}
