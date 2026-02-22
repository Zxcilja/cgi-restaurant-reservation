package com.example.restaurant.service;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TableServiceTests {

    @Mock
    private TableRepository tableRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private TableService tableService;

    private RestaurantTable table1;
    private RestaurantTable table2;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        table1 = new RestaurantTable();
        table1.setId(1L);
        table1.setCapacity(4);
        table1.setX(0);
        table1.setY(0);
        table1.setZone("A");

        table2 = new RestaurantTable();
        table2.setId(2L);
        table2.setCapacity(6);
        table2.setX(1);
        table2.setY(0);
        table2.setZone("A");

        when(tableRepository.findAll()).thenReturn(Arrays.asList(table1, table2));
        // no overlapping reservations for simplicity
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void returnsSingleTableWhenEnoughCapacity() {
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        List<TableService.TableRecommendation> recs = tableService.getRecommendations(
                4, time, null, false, false, false);

        assertFalse(recs.isEmpty());
        assertEquals(1, recs.get(0).getTables().size());
        assertEquals(table1, recs.get(0).getTables().get(0));
    }

    @Test
    void returnsPairWhenNoSingleCanFit() {
        LocalDateTime time = LocalDateTime.now().plusDays(1);

        List<TableService.TableRecommendation> recs = tableService.getRecommendations(
                8, time, null, false, false, false);

        assertFalse(recs.isEmpty());
        assertEquals(2, recs.get(0).getTables().size());
        List<RestaurantTable> pair = recs.get(0).getTables();
        assertTrue(pair.contains(table1) && pair.contains(table2));
    }

    @Test
    void returnsPairExactFivePlusFive() {
        
        RestaurantTable a = new RestaurantTable();
        a.setId(3L);
        a.setCapacity(5);
        a.setX(0);
        a.setY(1);
        a.setZone("A");

        RestaurantTable b = new RestaurantTable();
        b.setId(4L);
        b.setCapacity(5);
        b.setX(1);
        b.setY(1);
        b.setZone("A");

        when(tableRepository.findAll()).thenReturn(Arrays.asList(a, b));

        LocalDateTime time = LocalDateTime.now().plusDays(1);
        List<TableService.TableRecommendation> recs = tableService.getRecommendations(
                10, time, null, false, false, false);

        assertFalse(recs.isEmpty());
        assertEquals(2, recs.get(0).getTables().size());
        List<RestaurantTable> pair = recs.get(0).getTables();
        assertTrue(pair.contains(a) && pair.contains(b));
    }

    @Test
    void pairMustBeAdjacent() {

        table2.setX(10);
        table2.setY(10);
        when(tableRepository.findAll()).thenReturn(Arrays.asList(table1, table2));

        LocalDateTime time = LocalDateTime.now().plusDays(1);
        List<TableService.TableRecommendation> recs = tableService.getRecommendations(
                8, time, null, false, false, false);
        assertTrue(recs.isEmpty());
    }

    @Test
    void getTableByIdAndSavePosition() {
        when(tableRepository.findById(1L)).thenReturn(java.util.Optional.of(table1));
        table1.setX(5.5);
        table1.setY(7.7);
        when(tableRepository.save(table1)).thenReturn(table1);

        RestaurantTable found = tableService.getTableById(1L);
        assertNotNull(found);
        found.setX(5.5);
        found.setY(7.7);
        RestaurantTable saved = tableService.saveTable(found);
        assertEquals(5.5, saved.getX());
        assertEquals(7.7, saved.getY());
        verify(tableRepository).save(table1);
    }
}
