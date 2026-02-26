package com.example.restaurant.service;

import com.example.restaurant.model.Reservation;
import com.example.restaurant.model.ReservationStatus;
import com.example.restaurant.model.RestaurantTable;
import com.example.restaurant.repository.ReservationRepository;
import com.example.restaurant.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepo;
    
    @Autowired
    private TableRepository tableRepo;
    
    @Autowired
    private TableService tableService;
    

    public List<Reservation> createReservation(List<Long> tableIds, String customerName,
                                               String customerEmail, int numberOfGuests,
                                               LocalDateTime startTime) {
        if (tableIds == null || tableIds.isEmpty()) {
            throw new RuntimeException("At least one table must be selected");
        }

        LocalDateTime endTime = startTime.plusHours(2);
        int totalCapacity = 0;
        for (Long tableId : tableIds) {
            Optional<RestaurantTable> tableOpt = tableRepo.findById(tableId);
            if (tableOpt.isEmpty()) {
                throw new RuntimeException("Table not found: " + tableId);
            }
            RestaurantTable table = tableOpt.get();
            totalCapacity += table.getCapacity();
            if (!tableService.isTableAvailable(tableId, startTime, endTime)) {
                throw new RuntimeException("Table " + tableId + " is not available at this time");
            }
        }

        if (totalCapacity < numberOfGuests) {
            throw new RuntimeException("Selected tables do not provide enough capacity");
        }

        List<Reservation> results = new ArrayList<>();
        for (Long tableId : tableIds) {
            RestaurantTable table = tableRepo.findById(tableId).get();
            Reservation reservation = new Reservation();
            reservation.setTable(table);
            reservation.setCustomerName(customerName);
            reservation.setCustomerEmail(customerEmail);
            reservation.setNumberOfGuests(numberOfGuests);
            reservation.setStartTime(startTime);
            reservation.setEndTime(endTime);
            reservation.setStatus(ReservationStatus.CONFIRMED);
            results.add(reservationRepo.save(reservation));
        }
        return results;
    }
    
    public void cancelReservation(Long reservationId) {
        Optional<Reservation> opt = reservationRepo.findById(reservationId);
        if (opt.isPresent()) {
            Reservation res = opt.get();
            res.setStatus(ReservationStatus.CANCELLED);
            reservationRepo.save(res);
        }
    }
}