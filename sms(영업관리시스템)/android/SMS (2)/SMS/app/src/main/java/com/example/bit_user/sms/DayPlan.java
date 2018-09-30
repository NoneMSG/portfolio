package com.example.bit_user.sms;

/**
 * Created by bit-user on 2017-08-17.
 */

public class DayPlan {
    private String no;
    private String day;
    private String date;
    private String plan;
    private String saleGoal;
    private int holiday;

    public DayPlan(String no,String day,String date, String plan,String total,int holiday){
        this.no=no;
        this.day=day;
        this.date=date;
        this.plan=plan;
        this.saleGoal=total;
        this.holiday=holiday;
    }
    public DayPlan(String day,String date, String plan,String total,int holiday){
        this.day=day;
        this.date=date;
        this.plan=plan;
        this.saleGoal=total;
        this.holiday=holiday;
    }
    @Override
    public String toString() {
        return "DayPlan{" +
                "no='" + no + '\'' +
                ", day='" + day + '\'' +
                ", date='" + date + '\'' +
                ", plan='" + plan + '\'' +
                ", saleGoal='" + saleGoal + '\'' +
                ", holiday=" + holiday +
                '}';
    }
}
