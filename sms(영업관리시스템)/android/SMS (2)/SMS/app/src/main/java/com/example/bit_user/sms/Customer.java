package com.example.bit_user.sms;

/**
 * Created by bit-user on 2017-08-16.
 */

public class Customer {
    private int no;
    private String name;
    private String ownerName;
    private String address;
    private String secCstName;

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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSecCstName() {
        return secCstName;
    }

    public void setSecCstName(String secCstName) {
        this.secCstName = secCstName;
    }
}
