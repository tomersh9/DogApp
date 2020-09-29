package com.example.dogapp.Enteties;

public class Chat {

    private String sender;
    private String receiver;
    private String message;
    private String time;
    private String timeStamp;
    private String isSeen;

    public Chat() {
    }

    public Chat(String sender, String receiver, String message, String time, String timeStamp, String isSeen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.time = time;
        this.timeStamp = timeStamp;
        this.isSeen = isSeen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getIsSeen() {
        return isSeen;
    }

    public void setIsSeen(String isSeen) {
        this.isSeen = isSeen;
    }
}
