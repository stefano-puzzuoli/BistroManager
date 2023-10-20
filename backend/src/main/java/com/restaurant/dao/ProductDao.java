package com.restaurant.dao;

import com.restaurant.model.Product;
import com.restaurant.wrapper.ProductWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductDao extends JpaRepository<Product, Integer> {


    List<ProductWrapper> getAllProducts();

    List<ProductWrapper> getByCategory(@Param("categoryId") Integer categoryId);

    ProductWrapper getByProductId(@Param("id") Integer id);


}
