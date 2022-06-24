package pt.ulisboa.tecnico.cmov.cmovproject.chat.message;

import java.nio.charset.StandardCharsets;

public class TextMessage extends Message {
    private String msg;

    public TextMessage(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }

    public String getText() {
        return msg;
    }

    @Override
    public Integer getByteCount() {
        return msg.getBytes().length;
    }
}
