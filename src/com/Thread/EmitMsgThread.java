package com.Thread;

import com.Gateway;
import com.Message.TokenMsg;

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
                TokenMsg TM=new TokenMsg((int)(200*Math.random()));
                TM.ID=gate.ID;
                //gate.emitSingleMessage(target,TM);
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
