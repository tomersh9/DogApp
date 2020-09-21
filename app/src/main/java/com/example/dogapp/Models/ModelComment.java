package com.example.dogapp.Models;

public class ModelComment {

    String cId, comment, timeStamp, uId, uPic, uName;

    public ModelComment() {
    }

    public ModelComment(String cId, String comment, String timeStamp, String uId, String uPic, String uName) {
        this.cId = cId;
        this.comment = comment;
        this.timeStamp = timeStamp;
        this.uId = uId;
        this.uPic = uPic;
        this.uName = uName;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuPic() {
        return uPic;
    }

    public void setuPic(String uPic) {
        this.uPic = uPic;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }
}
