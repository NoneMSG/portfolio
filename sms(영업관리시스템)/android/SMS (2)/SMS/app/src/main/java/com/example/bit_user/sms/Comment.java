package com.example.bit_user.sms;

/**
 * Created by bit-user on 2017-08-28.
 */

public class Comment {
    private int no;
    private int userNo;
    private String name;
    private String comment;
    private String date;


    public Comment(int no, int userNo,String name,String comment, String date){

        this.no = no;
        this.name=name;
        this.userNo=userNo;
        this.comment=comment;
        this.date=date;
    }

    public int getUserNo() {
        return userNo;
    }

    public void setUserNo(int userNo) {
        this.userNo = userNo;
    }
    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
