package com.Thread;

import com.Gateway;
import com.Message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Objects;

public class ClientThread extends Thread {
    private int ID;
    private Socket cSocket;
    private Gateway gate;

    public ClientThread(Gateway gate,Socket socket) {
        this.ID = gate.ID;
        this.gate = gate;
        this.cSocket = socket;
    }

    public void run() {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(cSocket.getInputStream());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        while (true) {
            try {
                Message msg;
                msg = (Message) Objects.requireNonNull(ois).readObject();
                // Synchronizing mainObj so that multiple threads access mainObj in a synchronized way
                synchronized (gate) {
                    if (msg instanceof TokenMsg) {
                        gate.ReceiveTokenMsg(((TokenMsg) msg).TokenNum,msg.ID);
                    }
                    else if(msg instanceof AcceptMsg) {
                        if (gate.SUT.Getrandom() == ((AcceptMsg) msg).random) {
                            gate.ReceiveAcceptMsg(msg.ID, ((AcceptMsg) msg).OK, ((AcceptMsg) msg).register,((AcceptMsg) msg).TopoTable);
                            String str = new String("Receive AcceptMsg from Gateway " + String.valueOf(msg.ID) + " : " + String.valueOf(((AcceptMsg) msg).OK) + " random : " + String.valueOf(((AcceptMsg) msg).random) + "\n");
                            System.out.print(str);
                        }
                        else{
                            /**随机数不对则丢弃*/
                        }
                    }
                    else if(msg instanceof RegisterMsg){
                        System.out.print("Receive RegisterMsg from Gateway "+String.valueOf(msg.ID)+"\n");
                        synchronized (gate){
                            if(gate.register==true||gate.SUT==null){
                                AcceptMsg AM=new AcceptMsg(gate,true,((RegisterMsg) msg).random);
                                gate.emitSingleMessage(msg.ID,AM);
                            }
                            else{
                                /**处理多个节点共同进行注册*/
                                if(msg.ID<ID){/**ID小者优先*/
                                    AcceptMsg AM=new AcceptMsg(gate,true,((RegisterMsg) msg).random);
                                    gate.emitSingleMessage(msg.ID,AM);
                                }
                                else{
                                    AcceptMsg AM=new AcceptMsg(gate,false,((RegisterMsg) msg).random);
                                    gate.emitSingleMessage(msg.ID,AM);
                                }
                            }
                        }
                    }
                    else if(msg instanceof ChangeTopoMsg){
                        gate.ChangeTopo(msg.ID,((ChangeTopoMsg) msg).AddOrRemove);
                    }
                    else if(msg instanceof ServerMsg) {
                        if (gate.ST != null) {
                            gate.ST.ReceiveServerMsg(((ServerMsg) msg).SearchForSpace, ((ServerMsg) msg).name);
                        }
                    }
                    else if(msg instanceof SnapMsg){
                        gate.ST.ReceiveSnapMsg(msg.ID,((SnapMsg) msg).Snap);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
