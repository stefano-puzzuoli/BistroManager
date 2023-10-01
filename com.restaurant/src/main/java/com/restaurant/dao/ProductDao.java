package com.restaurant.dao;

import com.restaurant.POJO.Product;
import com.restaurant.wrapper.ProductWrapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductDao extends JpaRepository<Product, Integer> {


    List<ProductWrapper> getAllProducts();

}
