package com.Message;

import java.io.Serializable;

public class TokenMsg extends Message implements Serializable {
    public int TokenNum;
    public TokenMsg(int num){
        this.TokenNum=num;
    }
}
