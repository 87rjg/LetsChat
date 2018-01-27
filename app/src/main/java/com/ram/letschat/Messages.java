package com.ram.letschat;

/**
 * Created by RAMJEE on 19-01-2018.
 */

public class Messages {

    private String message;
    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }




    public Messages(){}

    public Messages(String message, String from) {
        this.message = message;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
