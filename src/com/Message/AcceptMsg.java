package com.Message;

import com.Gateway;

import java.io.Serializable;

public class AcceptMsg extends Message implements Serializable {
    public long random;
    public boolean register;
    public boolean OK;
    public boolean TopoTable[];

    public AcceptMsg(Gateway gate, boolean OK, long random) {
        this.random = random;
        this.ID = gate.ID;
        this.OK = OK;
        this.register = gate.register;
        TopoTable = gate.TopoTable.clone();
    }
}
