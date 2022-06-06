package pt.ulisboa.tecnico.cmov.cmovproject.chat;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import pt.ulisboa.tecnico.cmov.cmovproject.chat.message.Message;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.message.TextMessage;

// encapsulates all the info on a chat entry
public class ChatEntry implements Serializable {
    private String username;
    private Message msg;

    // TODO: create builder for other types of message
    public ChatEntry(String username, String textMsg){
        this.username = username;
        this.msg = new TextMessage(textMsg);
    }

    public String getUsername() {
        return username;
    }

    public Message getMsg() {
        return msg;
    }

    public int getByteCount(){
        int userByes = username.getBytes().length;
        int msgBytes = msg.getByteCount();
        return userByes + msgBytes;
    }
}
