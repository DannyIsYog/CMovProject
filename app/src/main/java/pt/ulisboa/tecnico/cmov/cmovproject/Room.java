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
    private  List<String> users;

    public Room(String name, int roomType, List<String> messages, List<String> users) {
        this.name = name;
        this.roomType = roomType;
        this.messages = messages;
        this.users = users;
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

    public void setRoomType(int type) {
        this.roomType = type;
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

}
