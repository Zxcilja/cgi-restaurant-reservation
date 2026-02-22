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
        
        List<TableService.TableRecommendation> recs = tableService.getRecommendations(guests, time, zone, window, privateArea, accessible);
        boolean usedFallback = false;
        if (recs.isEmpty()) {
            recs = tableService.getRecommendations(guests, time, zone, window, privateArea, accessible, true);
            usedFallback = !recs.isEmpty();
        }

        final boolean fallback = usedFallback;
        return recs.stream()
                .map(r -> {
                    boolean avail = true;
                    for (RestaurantTable tbl : r.getTables()) {
                        if (!tableService.isTableAvailable(tbl.getId(), time, time.plusHours(2))) {
                            avail = false;
                            break;
                        }
                    }
                    if (fallback) {
                        avail = false;
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
    
    public static class TablePositionDTO {
        private Long id;
        private double x;
        private double y;
        private java.util.List<Long> combinableWith; 

        public TablePositionDTO() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        public java.util.List<Long> getCombinableWith() { return combinableWith; }
        public void setCombinableWith(java.util.List<Long> combinableWith) { this.combinableWith = combinableWith; }
    }

    @PutMapping("/positions")
    public void updatePositions(@RequestBody List<TablePositionDTO> positions) {
        System.out.println("Updating positions: " + positions.size());
        for (TablePositionDTO p : positions) {
            System.out.println("pos -> id=" + p.getId() + " x=" + p.getX() + " y=" + p.getY() + " comb=" + p.getCombinableWith());
            RestaurantTable t = tableService.getTableById(p.getId());
            if (t != null) {
                t.setX(p.getX());
                t.setY(p.getY());
                if (p.getCombinableWith() != null) {
                    t.getCombinableWith().clear();
                    t.getCombinableWith().addAll(p.getCombinableWith());
                }
                tableService.saveTable(t);
            }
        }
    }
}