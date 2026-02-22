package com.example.restaurant.model;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int capacity;
    private String zone;
    private double x;
    private double y;
    private boolean nearWindow;
    private boolean privateArea;
    private boolean accessible;
    private boolean nearKidsArea;


    @ElementCollection
    @CollectionTable(name = "table_combinable", joinColumns = @JoinColumn(name = "table_id"))
    @Column(name = "other_table_id")
    private java.util.Set<Long> combinableWith = new java.util.HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isNearWindow() {
        return nearWindow;
    }

    public void setNearWindow(boolean nearWindow) {
        this.nearWindow = nearWindow;
    }

    public boolean isPrivateArea() {
        return privateArea;
    }

    public void setPrivateArea(boolean privateArea) {
        this.privateArea = privateArea;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    public boolean isNearKidsArea() {
        return nearKidsArea;
    }

    public void setNearKidsArea(boolean nearKidsArea) {
        this.nearKidsArea = nearKidsArea;
    }

    public java.util.Set<Long> getCombinableWith() {
        return combinableWith;
    }

    public void setCombinableWith(java.util.Set<Long> combinableWith) {
        this.combinableWith = combinableWith;
    }
}
