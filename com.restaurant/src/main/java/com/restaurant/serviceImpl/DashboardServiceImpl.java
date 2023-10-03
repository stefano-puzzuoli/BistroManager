package com.restaurant.serviceImpl;

import com.restaurant.dao.BillDao;
import com.restaurant.dao.CategoryDao;
import com.restaurant.dao.ProductDao;
import com.restaurant.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    ProductDao productDao;

    @Autowired
    BillDao billDao;

    @Override
    public ResponseEntity<Map<String, Object>> getDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("category", categoryDao.count());
        details.put("product", productDao.count());
        details.put("bill", billDao.count());
        return new ResponseEntity<>(details, HttpStatus.OK);
    }
}
