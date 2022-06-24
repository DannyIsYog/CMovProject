package pt.ulisboa.tecnico.cmov.cmovproject.chat;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import pt.ulisboa.tecnico.cmov.cmovproject.chat.message.Message;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.message.TextMessage;

// encapsulates all the info on a chat entry
public class ChatEntry implements Serializable {
    private String username;
    private Message msg;
    private String ts;

    // TODO: create builder for other types of message
    public ChatEntry(String username, String textMsg, String ts){
        this.username = username;
        this.msg = new TextMessage(textMsg);
        this.ts = ts;
    }

    public String getUsername() {
        return username;
    }

    public Message getMsg() {
        return msg;
    }

    public String getTs() {
        return this.ts;
    }

    public int getByteCount(){
        int userByes = username.getBytes().length;
        int msgBytes = msg.getByteCount();
        return userByes + msgBytes;
    }

    public String toString(){
        return "ENTRY: { username : "+username+" msg : "+msg.getText().toString()+" }";
    }

    public static ChatEntry getEmptyEntry() {
        return new ChatEntry("downloading...", "downloading...", "");
    }
}
