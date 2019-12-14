package com.Message;

import java.io.Serializable;

public class ServerMsg extends Message implements Serializable {
    public boolean SearchForSpace;
    public String name;
    public ServerMsg(boolean Search,String str){
        SearchForSpace=Search;
        name=str;
    }
}
