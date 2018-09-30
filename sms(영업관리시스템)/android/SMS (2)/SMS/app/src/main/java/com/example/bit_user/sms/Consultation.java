package com.example.bit_user.sms;

import java.io.Serializable;

/**
 * Created by bit-user on 2017-09-01.
 */

public class Consultation implements Serializable {
    private int no;
    private String title;
    private String regDate;

    public Consultation(int no, String title , String regDate){
        this.no = no;
        this.title=title;
        this.regDate=regDate;
    }

    public Consultation(int no, String title ){
        this.no = no;
        this.title=title;
    }

    public int getNo() {
        return no;
    }

    public String getTitle() {
        return title;
    }

    public String getRegDate() {
        return regDate;
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "no=" + no +
                ", title='" + title + '\'' +
                ", regDate='" + regDate + '\'' +
                '}';
    }
}
