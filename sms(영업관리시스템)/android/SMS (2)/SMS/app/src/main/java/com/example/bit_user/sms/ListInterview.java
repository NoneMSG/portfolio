package com.example.bit_user.sms;

import java.io.Serializable;

/**
 * Created by bit-user on 2017-08-22.
 */

public class ListInterview implements Serializable{
    private String no;
    private String title;
    private String name1;
    private String name2;

    public ListInterview(String no,String title,String name1, String name2){
        this.no = no;
        this.title = title;
        this.name1 = name1;
        this.name2 = name2;
    }

    public String getNo() {
        return no;
    }

    public String getTitle() {
        return title;
    }

    public String getName1() {
        return name1;
    }

    public String getName2() {
        return name2;
    }

    @Override
    public String toString() {
        return  title +"\n"+
                " 거래처 : " + name1 +
                " 2거래처 : " + name2+"\n";
    }
}
