package com.restaurant.serviceImpl;

import com.restaurant.JWT.JwtFilter;
import com.restaurant.POJO.Category;
import com.restaurant.POJO.Product;
import com.restaurant.constants.RestaurantConstants;
import com.restaurant.dao.ProductDao;
import com.restaurant.service.ProductService;
import com.restaurant.utils.RestaurantUtils;
import com.restaurant.wrapper.ProductWrapper;
import org.hibernate.mapping.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductDao productDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateProductMap(requestMap, false)) {
                    productDao.save(getProductFromMap(requestMap, false));
                    return RestaurantUtils.getResponseEntity("Product added successfully.", HttpStatus.OK);
                }
                return RestaurantUtils.getResponseEntity(RestaurantConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            } else {
                return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProducts() {
        try {
            return new ResponseEntity<>(productDao.getAllProducts(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(Collections.EMPTY_LIST, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateProductMap(requestMap, true)) {
                    Optional<Product> product = productDao.findById(Integer.parseInt(requestMap.get("id")));
                    if (product.isPresent()) {
                        Product productRetrieved = getProductFromMap(requestMap, true);
                        productRetrieved.setStatus(product.get().getStatus());
                        productDao.save(productRetrieved);
                        return RestaurantUtils.getResponseEntity("Product updated successfully.", HttpStatus.OK);
                    } else {
                        return RestaurantUtils.getResponseEntity("Product id does not exist.", HttpStatus.OK);
                    }
                }
                return RestaurantUtils.getResponseEntity(RestaurantConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            } else {
                return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Product> product = productDao.findById(id);
                if (product.isPresent()) {
                    productDao.deleteById(id);
                    return RestaurantUtils.getResponseEntity("Product deleted successfully.", HttpStatus.OK);
                }
                return RestaurantUtils.getResponseEntity("Product id does not exist.", HttpStatus.OK);
            } else {
                return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Product> product = productDao.findById(Integer.parseInt(requestMap.get("id")));
                if (product.isPresent()) {
                    Product productRetrieved = product.get();
                    productRetrieved.setStatus(requestMap.get("status"));
                    productDao.save(productRetrieved);
                    return RestaurantUtils.getResponseEntity("Product status updated successfully.", HttpStatus.OK);
                } else {
                    return RestaurantUtils.getResponseEntity("Product id does not exist.", HttpStatus.OK);
                }
            } else {
                return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer categoryId) {
        try {
//            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(productDao.getByCategory(categoryId), HttpStatus.OK);
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(Collections.EMPTY_LIST, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ProductWrapper> getProductById(Integer id) {
        try {
//            if (jwtFilter.isAdmin()) {
                Optional<Product> product = productDao.findById(id);
                if (product.isPresent()) {
                    return new ResponseEntity<>(productDao.getByProductId(id), HttpStatus.OK);
                }
                return new ResponseEntity<>(new ProductWrapper(), HttpStatus.OK);
//            }
//            return new ResponseEntity<>(new ProductWrapper(), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ProductWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Product getProductFromMap(Map<String, String> requestMap, boolean isUpdate) {
        Category category = new Category();
        category.setId(Integer.parseInt(requestMap.get("categoryId")));
        Product product = new Product();
        if (isUpdate) {
            product.setId(Integer.parseInt(requestMap.get("id")));
        } else {
            product.setStatus("true");
        }
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Integer.parseInt(requestMap.get("price")));
        product.setCategory(category);
        return product;
    }

    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if (requestMap.containsKey("name")) {
            if (requestMap.containsKey("id") && validateId) {
                return true;
            } else if (!validateId) {
                return true;
            }
        }
        return false;
    }
}
