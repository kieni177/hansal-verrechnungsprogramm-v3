package com.hansal.verrechnungsprogramm.repository;

import com.hansal.verrechnungsprogramm.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Product p WHERE p.id NOT IN (SELECT oi.product.id FROM OrderItem oi WHERE oi.product IS NOT NULL) AND p.id NOT IN (SELECT mc.product.id FROM MeatCut mc)")
    List<Product> findUnreferencedProducts();
}
