package com.Message;

import java.io.Serializable;
import java.util.Date;

public class RegisterMsg extends Message implements Serializable {
    public long random;
    public Date time;
    public RegisterMsg(int num,Date time,long random) {
        this.random = random;
        this.ID = num;
        this.time = time;
    }
}
