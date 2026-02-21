package com.example.restaurant.service;

import com.example.restaurant.model.Reservation;
import com.example.restaurant.model.RestaurantTable;
import com.example.restaurant.repository.ReservationRepository;
import com.example.restaurant.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TableService {

    @Autowired
    private TableRepository tableRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }
    
    public boolean isTableAvailable(Long tableId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
            tableId, startTime, endTime
        );
        return overlapping.isEmpty();
    }
    
    public List<TableRecommendation> getRecommendations(
            int numberOfGuests,
            LocalDateTime startTime,
            String preferredZone,
            boolean preferWindow,
            boolean preferPrivate,
            boolean preferAccessible) {
        
        LocalDateTime endTime = startTime.plusHours(2); 
        
        List<RestaurantTable> allTables = tableRepository.findAll();
        List<TableRecommendation> recommendations = new ArrayList<>();
        
        for (RestaurantTable table : allTables) {
            if (table.getCapacity() < numberOfGuests) {
                continue; 
            }

            if (preferredZone != null && !preferredZone.isEmpty() &&
                !table.getZone().equalsIgnoreCase(preferredZone)) {
                continue;
            }

            if (preferWindow && !table.isNearWindow()) {
                continue;
            }

            if (preferPrivate && !table.isPrivateArea()) {
                continue;
            }

            if (preferAccessible && !table.isAccessible()) {
                continue;
            }
            
            if (!isTableAvailable(table.getId(), startTime, endTime)) {
                continue; 
            }
            
            double score = calculateScore(table, numberOfGuests, preferredZone, 
                                        preferWindow, preferPrivate, preferAccessible);
            
            recommendations.add(new TableRecommendation(table, score));
        }
        
        recommendations.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        return recommendations;
    }
    
    private double calculateScore(RestaurantTable table, int guests, String preferredZone,
                                  boolean preferWindow, boolean preferPrivate, boolean preferAccessible) {
        double score = 100.0;
        
        int emptySeats = table.getCapacity() - guests;
        score -= emptySeats * 10; 
        
        if (preferredZone != null && !preferredZone.isEmpty()) {
            if (table.getZone().equalsIgnoreCase(preferredZone)) {
                score += 20;
            } else {
                score -= 10;
            }
        }
        
        if (preferWindow && table.isNearWindow()) score += 15;
        if (preferPrivate && table.isPrivateArea()) score += 15;
        if (preferAccessible && table.isAccessible()) score += 10;
        
        return score;
    }
    
    public static class TableRecommendation {
        private RestaurantTable table;
        private double score;
        
        public TableRecommendation(RestaurantTable table, double score) {
            this.table = table;
            this.score = score;
        }
        
        public RestaurantTable getTable() { return table; }
        public double getScore() { return score; }
    }
}