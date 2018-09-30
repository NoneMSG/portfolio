package com.example.bit_user.sms;

/**
 * Created by bit-user on 2017-08-25.
 */

public class Home_List {
    private int no;
    private String title;
    private String writeDate;
    private String fixed="";

    public String getFixed() {
        return fixed;
    }

    public Home_List(int no, String title, String writeDate){
        this.no=no;
        this.title=title;
        this.writeDate=writeDate;
        //this.fixed=fixed;
    }

    public Home_List(int no, String title, String writeDate,String fixed){
        this.no=no;
        this.title=title;
        this.writeDate=writeDate;
        this.fixed=fixed;
    }

    public int getNo() {
        return no;
    }

    public String getTitle() {
        return title;
    }

    public String getWriteDate() {
        return writeDate;
    }


    @Override
    public String toString() {
        return "Home_List{" +
                "no=" + no +
                ", title='" + title + '\'' +
                ", writeDate='" + writeDate + '\'' +
                ", fixed='" + fixed + '\'' +
                '}';
    }
}
