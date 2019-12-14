package com.Message;

import java.io.Serializable;

public class TokenMsg extends Message implements Serializable {
    public int TokenNum;

    public TokenMsg(int num, int ID) {
        this.TokenNum = num;
        this.ID = ID;
    }
}
