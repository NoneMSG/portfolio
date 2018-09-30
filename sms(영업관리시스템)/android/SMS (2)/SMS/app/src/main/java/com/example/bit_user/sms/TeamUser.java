package com.example.bit_user.sms;

/**
 * Created by bit-user on 2017-09-11.
 */

public class TeamUser {
    private int no;
    private String name;

    public TeamUser(int no, String name){
        this.no=no;
        this.name=name;
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
}
