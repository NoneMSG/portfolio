package com.example.bit_user.sms;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by bit-user on 2017-08-18.
 */
//작성날짜 구하기 / 작성날짜기준 7일후 날짜 구하기 /7일후 해당 주 구하기=오늘날짜의 주 구하기
public class GetDayAndWeek {
    private String today;
    private String afterDay;
    private String thisMonday;
    private String[] nextWeek;

    Calendar calendar = Calendar.getInstance();
    Date date = calendar.getTime();


    //#### 오늘 날짜 구하기
    public String getToday(){

        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.KOREA);
        Date currentTime = new Date();
        today = mSimpleDateFormat.format(currentTime);

        return today;
    }

    //####오늘 날짜로부터 7일 후 구하기
    public  String get7DayAgoDate(String date,int n) {

        String stryear = date.substring(0,4);
        String strmonth = date.substring(5,7);
        String strday = date.substring(8);

        int year=Integer.parseInt(stryear);
        int month=Integer.parseInt(strmonth);
        int day=Integer.parseInt(strday);

        calendar.set(year, month-1, day);

        calendar.add(Calendar.DATE, +n);

        java.util.Date weekago = calendar.getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());

        String afterDay=formatter.format(weekago);
        System.out.println(afterDay);

        return afterDay;

    }

    //###############이번주 월요일 구하기
    public String getCurMonday(){
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        thisMonday=formatter.format(calendar.getTime());
        return thisMonday;
    }

}
