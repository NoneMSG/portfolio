package com.example.bit_user.sms;

/**
 * Created by bit-user on 2017-09-18.
 */

public class CalendarList {
    private String title;
    private String confirm;
    private String name;
    private String content;
    private int no;
    private int userNo;

    public CalendarList(int no, String title, String name, String content, String confirm){
        this.no = no;
        this.title=title;
        this.name=name;
        this.content=content;
        this.confirm=confirm;
    }

    public String getTitle() {
        return title;
    }

    public String getConfirm() {
        return confirm;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public int getNo() {
        return no;
    }

    public int getUserNo() {
        return userNo;
    }
}
