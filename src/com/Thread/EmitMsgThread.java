package com.Thread;

import com.Gateway;
import com.Message.ServerMsg;

public class EmitMsgThread extends Thread{
    Gateway gate;
    public EmitMsgThread(Gateway gate){
        this.gate=gate;
    }
    public void run(){
        try{
            Thread.sleep(10000);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        while(true){
            double r=Math.random();
            int target= (int)(gate.MaxNumOfGateway*r);
            if(gate.AdjMatrix[target]==true){
                //System.out.print("Send to "+String.valueOf(target)+"\n");
                ServerMsg SM;
                //gate.emitSingleMessage(target,SM);
                try{
                    Thread.sleep(2000);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
