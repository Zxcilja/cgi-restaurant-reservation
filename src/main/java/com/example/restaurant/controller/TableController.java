package com.example.restaurant.controller;

import com.example.restaurant.model.RestaurantTable;
import com.example.restaurant.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "*") 
public class TableController {

    @Autowired
    private TableService tableService;
    
    @GetMapping
    public List<RestaurantTable> getAllTables() {
        return tableService.getAllTables();
    }
    
    @GetMapping("/recommendations")
    public List<TableRecommendationDTO> getRecommendations(
            @RequestParam int guests,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false, defaultValue = "false") boolean window,
            @RequestParam(required = false, defaultValue = "false") boolean privateArea,
            @RequestParam(required = false, defaultValue = "false") boolean accessible) {
        
        return tableService.getRecommendations(guests, time, zone, window, privateArea, accessible)
                .stream()
                .map(r -> new TableRecommendationDTO(
                    r.getTable(),
                    r.getScore(),
                    tableService.isTableAvailable(r.getTable().getId(), time, time.plusHours(2))
                ))
                .collect(Collectors.toList());
    }
    
    public static class TableRecommendationDTO {
        private RestaurantTable table;
        private double score;
        private boolean available;
        
        public TableRecommendationDTO(RestaurantTable table, double score, boolean available) {
            this.table = table;
            this.score = score;
            this.available = available;
        }
        
        public RestaurantTable getTable() { return table; }
        public double getScore() { return score; }
        public boolean isAvailable() { return available; }
    }
}