package com.Message;

import java.io.Serializable;

public class SnapMsg extends Message implements Serializable {
    public int Snap;
    public SnapMsg(int num,int EndID){
        Snap=num;
        ID=EndID;
    }
}
