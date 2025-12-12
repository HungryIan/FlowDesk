public class Reservation {
    private String name;
    private String contactNumber;
    private int age;
    private String room;
    private String timeSlot;
    private int queueNumber;
    private String status; // "WAITING", "APPROVED", "CANCELLED"
    private String reservationId;

    public Reservation(String name, String contactNumber, int age, String room, String timeSlot, int queueNumber) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.age = age;
        this.room = room;
        this.timeSlot = timeSlot;
        this.queueNumber = queueNumber;
        this.status = "WAITING";
        this.reservationId = "R" + String.format("%04d", queueNumber);
    }

    public String getName() {
        return name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public int getAge() {
        return age;
    }

    public String getRoom() {
        return room;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public int getQueueNumber() {
        return queueNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReservationId() {
        return reservationId;
    }
}


