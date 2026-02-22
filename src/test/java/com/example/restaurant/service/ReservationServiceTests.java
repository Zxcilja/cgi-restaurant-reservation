package com.example.restaurant.service;

import com.example.restaurant.model.Reservation;
import com.example.restaurant.model.RestaurantTable;
import com.example.restaurant.repository.ReservationRepository;
import com.example.restaurant.repository.TableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTests {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TableRepository tableRepository;

    @Mock
    private TableService tableService;

    @InjectMocks
    private ReservationService reservationService;

    private RestaurantTable t1;
    private RestaurantTable t2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        t1 = new RestaurantTable();
        t1.setId(1L);
        t1.setCapacity(4);

        t2 = new RestaurantTable();
        t2.setId(2L);
        t2.setCapacity(6);

        when(tableRepository.findById(1L)).thenReturn(Optional.of(t1));
        when(tableRepository.findById(2L)).thenReturn(Optional.of(t2));
        when(tableService.isTableAvailable(anyLong(), any(), any())).thenReturn(true);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createSingleReservation() {
        LocalDateTime start = LocalDateTime.now();
        List<Reservation> res = reservationService.createReservation(
                Collections.singletonList(1L),
                "Alice", "a@example.com", 3, start);
        assertEquals(1, res.size());
        assertEquals(t1, res.get(0).getTable());
        assertEquals(3, res.get(0).getNumberOfGuests());
    }

    @Test
    void createMultipleReservations() {
        LocalDateTime start = LocalDateTime.now();
        List<Reservation> res = reservationService.createReservation(
                Arrays.asList(1L, 2L),
                "Bob", "b@example.com", 8, start);
        assertEquals(2, res.size());
        assertEquals(t1, res.get(0).getTable());
        assertEquals(t2, res.get(1).getTable());
    }

    @Test
    void failsWhenNotEnoughCapacity() {
        LocalDateTime start = LocalDateTime.now();
        Exception ex = assertThrows(RuntimeException.class, () ->
                reservationService.createReservation(Arrays.asList(1L, 2L),
                        "Bob", "b@example.com", 12, start));
        assertTrue(ex.getMessage().contains("enough capacity"));
    }
}
