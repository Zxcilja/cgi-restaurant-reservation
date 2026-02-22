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

        table2.setX(400);  
        table2.setY(400);
        when(tableRepository.findAll()).thenReturn(Arrays.asList(table1, table2));

        LocalDateTime time = LocalDateTime.now().plusDays(1);
        List<TableService.TableRecommendation> recs = tableService.getRecommendations(
                8, time, null, false, false, false);
        assertTrue(recs.isEmpty());
    }

    @Test
    void doesNotReturnInsufficientPair() {
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        List<TableService.TableRecommendation> recs = tableService.getRecommendations(
                12, time, null, false, false, false);
        assertTrue(recs.isEmpty());
    }

    @Test
    void doesNotReturnTooDistantPair() {
        table2.setX(200);
        table2.setY(0);
        when(tableRepository.findAll()).thenReturn(Arrays.asList(table1, table2));

        LocalDateTime time = LocalDateTime.now().plusDays(1);
        List<TableService.TableRecommendation> recs = tableService.getRecommendations(
                8, time, null, false, false, false);
        assertTrue(recs.isEmpty());
    }

    @Test
    void ignoresDistanceWhenCombinableFlagSet() {
        table2.setX(200);
        table2.setY(0);
        table1.getCombinableWith().add(table2.getId());
        table2.getCombinableWith().add(table1.getId());
        when(tableRepository.findAll()).thenReturn(Arrays.asList(table1, table2));

        LocalDateTime time = LocalDateTime.now().plusDays(1);
        List<TableService.TableRecommendation> recs = tableService.getRecommendations(
                8, time, null, false, false, false);
        assertFalse(recs.isEmpty(), "combinable flag should override distance");
        assertEquals(2, recs.get(0).getTables().size());
    }

    @Test
    void recommendsPairWhenLargeTableAlreadyBooked() {
        RestaurantTable big = new RestaurantTable();
        big.setId(10L);
        big.setCapacity(10);
        big.setX(0);
        big.setY(0);
        big.setZone("A");

        RestaurantTable small1 = new RestaurantTable();
        small1.setId(11L);
        small1.setCapacity(4);
        small1.setX(1);
        small1.setY(0);
        small1.setZone("A");

        RestaurantTable small2 = new RestaurantTable();
        small2.setId(12L);
        small2.setCapacity(6);
        small2.setX(2);
        small2.setY(0);
        small2.setZone("A");

        when(tableRepository.findAll()).thenReturn(Arrays.asList(big, small1, small2));

        LocalDateTime time = LocalDateTime.now().plusDays(1);
        when(reservationRepository.findOverlappingReservations(eq(big.getId()), any(), any()))
                .thenReturn(Collections.singletonList(new com.example.restaurant.model.Reservation()));
        when(reservationRepository.findOverlappingReservations(eq(small1.getId()), any(), any()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.findOverlappingReservations(eq(small2.getId()), any(), any()))
                .thenReturn(Collections.emptyList());

        List<TableService.TableRecommendation> recs = tableService.getRecommendations(
                10, time, null, false, false, false);

        assertFalse(recs.isEmpty(), "Expected a recommendation when big table is booked");
        assertEquals(2, recs.get(0).getTables().size());
        List<RestaurantTable> pair = recs.get(0).getTables();
        assertTrue(pair.contains(small1) && pair.contains(small2));
    }

    @Test
    void fallbackWhenAllBooked() {
        
        when(tableRepository.findAll()).thenReturn(Arrays.asList(table1, table2));
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(new com.example.restaurant.model.Reservation()));
        
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        List<TableService.TableRecommendation> recs = tableService.getRecommendations(
                4, time, null, false, false, false);
        assertTrue(recs.isEmpty());
        
        List<TableService.TableRecommendation> recs2 = tableService.getRecommendations(
                4, time, null, false, false, false, true);
        assertFalse(recs2.isEmpty());
        
        assertEquals(table1, recs2.get(0).getTables().get(0));
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
