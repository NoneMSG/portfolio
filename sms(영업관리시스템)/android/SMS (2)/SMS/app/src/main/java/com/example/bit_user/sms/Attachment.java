package com.example.bit_user.sms;

import java.io.Serializable;

/**
 * Created by bit-user on 2017-09-06.
 */

public class Attachment implements Serializable {
    private int no;
    private int dayNo;
    private String path;
    private String originalName;
    private String extName;
    private String md5;

    public Attachment(int no,int dayNo,String path, String originalName,String extName,String md5){
        this.no = no;
        this.dayNo=dayNo;
        this.path = path;
        this.originalName=originalName;
        this.extName=extName;
        this.md5=md5;
    }
    public Attachment(int no, String originalName){
        this.no = no;
        this.originalName=originalName;
    }

    public int getNo() {
        return no;
    }

    public int getDayNo() {
        return dayNo;
    }

    public String getPath() {
        return path;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getExtName() {
        return extName;
    }

    public String getMd5() {
        return md5;
    }
}
