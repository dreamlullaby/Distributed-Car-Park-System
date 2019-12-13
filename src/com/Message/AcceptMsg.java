package com.Message;

import com.Gateway;

public class AcceptMsg extends Message {
    public long random;
    public boolean register;
    public boolean OK;
    public boolean TopoMap[];

    public AcceptMsg(Gateway gate, boolean OK, long random) {
        this.random = random;
        this.ID = gate.ID;
        this.OK = OK;
        this.register = gate.register;
        TopoMap = new boolean[gate.TopoMap.length];
        for (int i = 0; i < gate.TopoMap.length; i++) {
            this.TopoMap[i] = gate.TopoMap[i];
        }
    }
}
