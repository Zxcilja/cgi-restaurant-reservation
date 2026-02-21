package com.example.restaurant.repository;

import com.example.restaurant.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByZone(String zone);
    List<RestaurantTable> findByCapacityGreaterThanEqual(int capacity);
}