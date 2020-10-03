package com.example.dogapp.Enteties;

public class StarterSliderPagerItem {

    private int icon;
    private String title;
    private String body;

    public StarterSliderPagerItem(int icon, String title, String body) {
        this.icon = icon;
        this.title = title;
        this.body = body;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
