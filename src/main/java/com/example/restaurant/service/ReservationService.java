package com.example.restaurant.service;

import com.example.restaurant.model.Reservation;
import com.example.restaurant.model.ReservationStatus;
import com.example.restaurant.model.RestaurantTable;
import com.example.restaurant.repository.ReservationRepository;
import com.example.restaurant.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private TableRepository tableRepository;
    
    @Autowired
    private TableService tableService;
    
    public Reservation createReservation(Long tableId, String customerName, 
                                        String customerEmail, int numberOfGuests,
                                        LocalDateTime startTime) {
        Optional<RestaurantTable> tableOpt = tableRepository.findById(tableId);
        if (tableOpt.isEmpty()) {
            throw new RuntimeException("Table not found");
        }
        
        RestaurantTable table = tableOpt.get();
        
        if (table.getCapacity() < numberOfGuests) {
            throw new RuntimeException("Table capacity exceeded");
        }
        
        LocalDateTime endTime = startTime.plusHours(2);
        if (!tableService.isTableAvailable(tableId, startTime, endTime)) {
            throw new RuntimeException("Table is not available at this time");
        }
        
        Reservation reservation = new Reservation();
        reservation.setTable(table);
        reservation.setCustomerName(customerName);
        reservation.setCustomerEmail(customerEmail);
        reservation.setNumberOfGuests(numberOfGuests);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        
        return reservationRepository.save(reservation);
    }
    
    public void cancelReservation(Long reservationId) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isPresent()) {
            Reservation res = opt.get();
            res.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(res);
        }
    }
}