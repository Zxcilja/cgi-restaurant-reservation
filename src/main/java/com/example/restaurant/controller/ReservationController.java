package com.example.restaurant.controller;

import com.example.restaurant.model.Reservation;
import com.example.restaurant.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    
    @PostMapping
    public List<Reservation> createReservation(@RequestBody ReservationRequest request) {
        return reservationService.createReservation(
            request.getTableIds(),
            request.getCustomerName(),
            request.getCustomerEmail(),
            request.getNumberOfGuests(),
            request.getStartTime()
        );
    }
    
    @PostMapping("/{id}/cancel")
    public void cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
    }
    
    public static class ReservationRequest {
        private List<Long> tableIds;
        private String customerName;
        private String customerEmail;
        private int numberOfGuests;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime startTime;
        
        public List<Long> getTableIds() { return tableIds; }
        public void setTableIds(List<Long> tableIds) { this.tableIds = tableIds; }
        
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        
        public int getNumberOfGuests() { return numberOfGuests; }
        public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    }
}