package model;

public class Transaction {
    private String id;
    private String userName;
    private String description;
    private String date;
    private String time;

    public Transaction(String id, String userName, String description, String date, String time) {
        this.id = id;
        this.userName = userName;
        this.description = description;
        this.date = date;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}


