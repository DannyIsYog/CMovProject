package pt.ulisboa.tecnico.cmov.cmovproject;

import java.util.List;

public class Room {
    enum RoomType
    {
        PUBLIC,
        PRIVATE,
        GEO_CASED
    }

    private String name;
    private int roomType;
    private List<String> messages;
    private List<String> users;
    private double latitude;
    private double longitude;
    private double radius;

    public Room(String name, int roomType, List<String> messages, List<String> users, double latitude, double longitude, double radius) {
        this.name = name;
        this.roomType = roomType;
        this.messages = messages;
        this.users = users;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
