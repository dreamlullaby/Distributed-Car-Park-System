package com.Message;

import java.io.Serializable;
import java.util.Date;

public class RegisterMsg extends Message implements Serializable {
    public long random;
    public RegisterMsg(int num,long random) {
        this.random = random;
        this.ID = num;
    }
}
