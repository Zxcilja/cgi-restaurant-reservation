package com.example.restaurant.config;

import com.example.restaurant.model.Reservation;
import com.example.restaurant.model.ReservationStatus;
import com.example.restaurant.model.RestaurantTable;
import com.example.restaurant.repository.ReservationRepository;
import com.example.restaurant.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TableRepository tableRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    private Random random = new Random();
    
    @Override
    public void run(String... args) throws Exception {
        createTable("Table 1 (window)", 2, "Main Hall", 100, 100, true, false, false, false);
        createTable("Table 2 (window)", 2, "Main Hall", 200, 100, true, false, false, false);
        createTable("Table 3", 4, "Main Hall", 300, 100, false, false, false, false);
        createTable("Table 4", 4, "Main Hall", 400, 100, false, false, false, false);
        createTable("Table 5 (VIP)", 6, "VIP Hall", 150, 200, false, true, true, false);
        createTable("Table 6 (VIP)", 8, "VIP Hall", 300, 200, false, true, true, false);
        createTable("Table 7 (terrace)", 4, "Terrace", 100, 300, true, false, false, false);
        createTable("Table 8 (terrace)", 4, "Terrace", 250, 300, false, false, false, false);
        createTable("Table 9 (kids)", 4, "Main Hall", 500, 150, false, false, false, true);
        createTable("Table 10 (large)", 10, "Main Hall", 350, 250, false, false, true, false);

        createRandomReservations();
    }

    
    private void createTable(String name, int capacity, String zone, double x, double y,
                            boolean nearWindow, boolean privateArea, boolean accessible, boolean nearKids) {
        RestaurantTable table = new RestaurantTable();
        table.setName(name);
        table.setCapacity(capacity);
        table.setZone(zone);
        table.setX(x);
        table.setY(y);
        table.setNearWindow(nearWindow);
        table.setPrivateArea(privateArea);
        table.setAccessible(accessible);
        table.setNearKidsArea(nearKids);
        tableRepository.save(table);
    }
    
    private void createRandomReservations() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        
        tableRepository.findAll().forEach(table -> {
            if (random.nextDouble() < 0.3) { 
                Reservation res = new Reservation();
                res.setTable(table);
                res.setCustomerName("Гость " + random.nextInt(100));
                res.setCustomerEmail("guest" + random.nextInt(100) + "@example.com");
                res.setNumberOfGuests(random.nextInt(table.getCapacity()) + 1);
                res.setStartTime(tomorrow);
                res.setEndTime(tomorrow.plusHours(2));
                res.setStatus(ReservationStatus.CONFIRMED);
                reservationRepository.save(res);
            }
        });
    }
}