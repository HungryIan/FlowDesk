package model;

public class Seat {
    private String roomCode;
    private String building;
    private String timeSlot;
    private int capacity;
    private int availableSeats;
    private String features;

    public Seat(String roomCode, String building, String timeSlot, int capacity, int availableSeats, String features) {
        this.roomCode = roomCode;
        this.building = building;
        this.timeSlot = timeSlot;
        this.capacity = capacity;
        this.availableSeats = availableSeats;
        this.features = features;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getBuilding() {
        return building;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public String getFeatures() {
        return features;
    }
    
    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
    
    public void decreaseAvailableSeats() {
        if (this.availableSeats > 0) {
            this.availableSeats--;
        }
    }
}

