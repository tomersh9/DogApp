package com.example.dogapp.Models;

public class ModelPost {

    private String pId, uName, uId, pDesc, pLikes, pComments, pTime, uLoc, uPic;

    public ModelPost() {
    }

    public ModelPost(String pId, String uName, String uId, String pDesc, String pLikes, String pComments, String pTime, String uLoc, String uPic) {
        this.pId = pId;
        this.uName = uName;
        this.uId = uId;
        this.pDesc = pDesc;
        this.pLikes = pLikes;
        this.pComments = pComments;
        this.pTime = pTime;
        this.uLoc = uLoc;
        this.uPic = uPic;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getpDesc() {
        return pDesc;
    }

    public void setpDesc(String pDesc) {
        this.pDesc = pDesc;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getuLoc() {
        return uLoc;
    }

    public void setuLoc(String uLoc) {
        this.uLoc = uLoc;
    }

    public String getuPic() {
        return uPic;
    }

    public void setuPic(String uPic) {
        this.uPic = uPic;
    }
}