package com.Thread;

import com.Gateway;
import com.Message.AcceptMsg;
import com.Message.Message;
import com.Message.RegisterMsg;
import com.Message.TokenMsg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Objects;

public class ClientThread extends Thread {
    private int ID;
    private Socket cSocket;
    private Gateway gate;

    public ClientThread(int id, Gateway gate) {
        this.ID=id;
        this.gate = gate;
    }

    public ClientThread(Gateway gate,Socket socket){
        this.gate=gate;
        this.cSocket=socket;
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
                        gate.token.Receive(((TokenMsg) msg).TokenNum);
                        System.out.print("Received " + String.valueOf(((TokenMsg) msg).TokenNum) + " from Gateway " + String.valueOf(msg.ID) + " and current number is" + gate.token.curNum() + "\n");
                    }
                    if(msg instanceof AcceptMsg) {
                        if (gate.SUT.Getrandom() == ((AcceptMsg) msg).random)
                            if (gate.SUT != null) {
                                gate.ReceiveAcceptMsg(msg.ID, ((AcceptMsg) msg).OK, ((AcceptMsg) msg).register);
                                System.out.print("Receive AcceptMsg from Gateway " + String.valueOf(msg.ID) + " : " + String.valueOf(((AcceptMsg) msg).OK) + " random : "+String.valueOf(((AcceptMsg) msg).random)+" \n");
                            }
                        //else System.out.print("Receive AcceptMsg from Gateway " + String.valueOf(msg.ID) + " : " + String.valueOf(((AcceptMsg) msg).OK) + " but SignUpThread doesn't know\n");
                    }
                    if(msg instanceof RegisterMsg){
                        System.out.print("Receive RegisterMsg from Gateway "+String.valueOf(msg.ID)+" with a register time : "+((RegisterMsg) msg).time.toString()+"\n");
                        synchronized (gate){
                            if(gate.register==true||gate.SUT==null){
                                AcceptMsg AM=new AcceptMsg(gate,true,((RegisterMsg) msg).random);
                                gate.emitSingleMessage(msg.ID,AM);
                            }
                            else{
                                /**处理多个节点共同进行注册*/
                                if(gate.SUT!=null) System.out.print("Our Register Time : "+gate.GetRegisterTime().toString()+" and Received Register Time : "+((RegisterMsg) msg).time.toString()+"\n");
                                if(((RegisterMsg) msg).time.before(gate.GetRegisterTime())){
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
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
