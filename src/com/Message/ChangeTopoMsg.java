package com.Message;

import java.io.Serializable;

public class ChangeTopoMsg extends Message implements Serializable {
    public int AddOrRemove;/** -1 for Remove 1 for Add*/
    public ChangeTopoMsg(int ID,int AddOrRemove){
        this.ID=ID;
        this.AddOrRemove=AddOrRemove;
    }
}
