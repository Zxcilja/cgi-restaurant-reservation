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
    
    private static final int AVERAGE_VISIT_HOURS = 3; 


    public boolean isTableAvailable(Long tableId, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime bufferStart = startTime.minusHours(AVERAGE_VISIT_HOURS);
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
            tableId, bufferStart, endTime
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
        return getRecommendations(numberOfGuests, startTime, preferredZone, preferWindow, preferPrivate, preferAccessible, false);
    }

    public List<TableRecommendation> getRecommendations(
            int numberOfGuests,
            LocalDateTime startTime,
            String preferredZone,
            boolean preferWindow,
            boolean preferPrivate,
            boolean preferAccessible,
            boolean ignoreAvailability) {
        
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
            
            if (!ignoreAvailability && !isTableAvailable(table.getId(), startTime, endTime)) {
                continue; 
            }
            
            double score = calculateScore(table, numberOfGuests, preferredZone, 
                                        preferWindow, preferPrivate, preferAccessible);
            
            recommendations.add(new TableRecommendation(Collections.singletonList(table), score));
        }
        

        if (!recommendations.isEmpty()) {
            recommendations.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
            return recommendations;
        }
        
        
        for (int i = 0; i < allTables.size(); i++) {
            RestaurantTable t1 = allTables.get(i);
            for (int j = i + 1; j < allTables.size(); j++) {
                RestaurantTable t2 = allTables.get(j);

                if (!isSuitablePair(t1, t2, numberOfGuests)) continue;

                if (preferredZone != null && !preferredZone.isEmpty()) {
                    if (!(t1.getZone().equalsIgnoreCase(preferredZone) ||
                          t2.getZone().equalsIgnoreCase(preferredZone))) {
                        continue;
                    }
                }

                if (preferWindow && !(t1.isNearWindow() || t2.isNearWindow())) {
                    continue;
                }

                if (preferPrivate && !(t1.isPrivateArea() || t2.isPrivateArea())) {
                    continue;
                }

                if (preferAccessible && !(t1.isAccessible() || t2.isAccessible())) {
                    continue;
                }

                if (!ignoreAvailability && (!isTableAvailable(t1.getId(), startTime, endTime) ||
                    !isTableAvailable(t2.getId(), startTime, endTime))) {
                    continue;
                }

                double score = calculatePairScore(t1, t2, numberOfGuests,
                        preferredZone, preferWindow, preferPrivate, preferAccessible);
                recommendations.add(new TableRecommendation(Arrays.asList(t1, t2), score));
            }
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
    
    

    private boolean isSuitablePair(RestaurantTable t1, RestaurantTable t2, int requiredGuests) {
        
        if (t1.getCombinableWith().contains(t2.getId()) ||
            t2.getCombinableWith().contains(t1.getId())) {
            return (t1.getCapacity() + t2.getCapacity()) >= requiredGuests;
        }

        double dx = t1.getX() - t2.getX();
        double dy = t1.getY() - t2.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance >= 150.0) {  
            return false;
        }

        int combinedCapacity = t1.getCapacity() + t2.getCapacity();
        return combinedCapacity >= requiredGuests;
    }
    
    private double calculatePairScore(RestaurantTable t1, RestaurantTable t2,
                                      int guests, String preferredZone,
                                      boolean preferWindow, boolean preferPrivate,
                                      boolean preferAccessible) {
        
        double score = (calculateScore(t1, guests, preferredZone, preferWindow, preferPrivate, preferAccessible) +
                        calculateScore(t2, guests, preferredZone, preferWindow, preferPrivate, preferAccessible)) / 2.0;
        
        int emptySeats = t1.getCapacity() + t2.getCapacity() - guests;
        
        score -= 20;
        score -= emptySeats * 5;
        return score;
    }
    
    public RestaurantTable getTableById(Long id) {
        return tableRepository.findById(id).orElse(null);
    }

    public RestaurantTable saveTable(RestaurantTable table) {
        return tableRepository.save(table);
    }

    public static class TableRecommendation {
        private List<RestaurantTable> tables;
        private double score;
        
        public TableRecommendation(List<RestaurantTable> tables, double score) {
            this.tables = tables;
            this.score = score;
        }
        
        public List<RestaurantTable> getTables() { return tables; }
        public double getScore() { return score; }
    }
}