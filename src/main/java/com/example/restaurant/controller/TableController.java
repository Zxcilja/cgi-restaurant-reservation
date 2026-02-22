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
                .map(r -> {
                    boolean avail = true;
                    for (RestaurantTable tbl : r.getTables()) {
                        if (!tableService.isTableAvailable(tbl.getId(), time, time.plusHours(2))) {
                            avail = false;
                            break;
                        }
                    }
                    return new TableRecommendationDTO(
                        r.getTables(),
                        r.getScore(),
                        avail
                    );
                })
                .collect(Collectors.toList());
    }
    
    public static class TableRecommendationDTO {
        private List<RestaurantTable> tables;
        private double score;
        private boolean available;
        
        public TableRecommendationDTO(List<RestaurantTable> tables, double score, boolean available) {
            this.tables = tables;
            this.score = score;
            this.available = available;
        }
        
        public List<RestaurantTable> getTables() { return tables; }
        public double getScore() { return score; }
        public boolean isAvailable() { return available; }
    }
}