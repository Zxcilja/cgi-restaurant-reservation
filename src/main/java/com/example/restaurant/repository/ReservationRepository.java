package com.example.restaurant.repository;

import com.example.restaurant.model.Reservation;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    @Query("SELECT r FROM Reservation r WHERE r.table.id = :tableId " +
           "AND r.startTime < :endTime AND r.endTime > :startTime " +
           "AND r.status != 'CANCELLED'")
    List<Reservation> findOverlappingReservations(
        @Param("tableId") Long tableId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    List<Reservation> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}