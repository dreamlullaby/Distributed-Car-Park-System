package com.Thread;

import com.Gateway;
import com.Message.SnapMsg;
import com.Message.TokenMsg;

import java.util.ArrayList;
import java.util.Date;

public class ServerThread extends Thread {
    Gateway gate;
    int ID;
    int RestNum;
    private ArrayList<String> WaitingForPark;
    private ArrayList<String> WaitingForLeave;
    boolean RcvToken;
    int EndID;
    Date lastDate;/**对比同一目的地消息之时标，未超时表示重复接收*/
    static long TimeBtwRcvSameEndID=500;
    boolean RcvTrans;
    Date lastDateOfTrans;/**发送常规转移消息后一段时间内收到转移消息不再发送*/
    static long TimeBtwNormalTransOfToken=5000;
    Date lastDateOfUpdate;
    static long TimeToUpdateInfo=4000;
    public ServerThread(Gateway gate){
        this.gate=gate;
        this.ID=gate.ID;
        RestNum=-1;
        RcvToken=false;
        EndID=ID;
        lastDate=new Date();
        RcvTrans=false;
        lastDateOfTrans=new Date();
        lastDateOfUpdate=new Date();
        WaitingForPark=new ArrayList<>();
        WaitingForLeave=new ArrayList<>();
    }

    public synchronized void ReceiveTokenMsg(int ID) {
        if(EndID==-1){
            RcvTrans=true;
        }
        if (EndID!=ID||new Date().getTime() - lastDate.getTime() > TimeBtwRcvSameEndID) {
            RcvToken = true;
            EndID = ID;
            lastDate=new Date();
        }
    }

    public void ReceiveServerMsg(boolean parking,String name){
        if(parking){
            WaitingForPark.add(name);
        }
        else{
            WaitingForLeave.add(name);
        }
    }

    public void ReceiveSnapMsg(int EndID,int Num){
        int N=Num+gate.token.curNum();
        if(EndID==ID){
            RestNum=N;
        }
        else{
            SnapMsg SM=new SnapMsg(N,EndID);
            gate.emitSingleMessage(gate.next,SM);
        }
    }

    public void run(){
        long Ta=new Date().getTime();
        long T0=new Date().getTime();
        long Ts=new Date().getTime();
        while(true){
            /**一定时间输出当前状态*/
            if(new Date().getTime()-Ta>TimeBtwNormalTransOfToken/2) {
                Ta = new Date().getTime();
                System.out.print("Rest Space in Parking Lot : " + RestNum + " Current Number in Token :" + gate.token.curNum() + "\n");
            }

            if(new Date().getTime()-lastDateOfUpdate.getTime()>1000&&new Date().getTime()-Ts>TimeToUpdateInfo){
                Ts=new Date().getTime();
                SnapMsg SM=new SnapMsg(0,ID);
                gate.emitSingleMessage(gate.next,SM);
            }

            if(RcvToken==true){
                /**如收到TokenMsg检查其EndID是否为自己，如果是的话则将标志位RcvToken置否，否则传递Token并将EndID放入TokenMsg的ID位*/
                if(EndID==ID){
                    RcvToken=false;
                }
                else synchronized (gate){
                    //TokenMsg TM=new TokenMsg(gate.token.SingleTokenTransmit());
                    TokenMsg TM=new TokenMsg(gate.token.Transmit(),EndID);
                    gate.emitSingleMessage(gate.next,TM);
                    String str=new String("Send TokenMsg To Gateway "+gate.next+" with "+TM.TokenNum+"\n");
                    System.out.print(str);
                    RcvToken=false;
                }
            }
            /**如果发现token中的数目为0，则向下一家发送TokenMsg以请求传递非空Token；每两次请求间间隔一定时间*/
            if(gate.token.curNum()==0&&new Date().getTime()-T0>1000) {
                T0 = new Date().getTime();
                EndID = ID;
                TokenMsg TM = new TokenMsg(gate.token.Transmit(), ID);
                gate.emitSingleMessage(gate.next, TM);
            }
            else if(new Date().getTime()-lastDate.getTime()>TimeBtwNormalTransOfToken){
                lastDate=new Date();
                lastDateOfTrans=lastDate;
                EndID=-1;
                TokenMsg TM = new TokenMsg(gate.token.Transmit(), ID);
                gate.emitSingleMessage(gate.next, TM);
            }
            else if(RcvTrans&&new Date().getTime()-lastDateOfTrans.getTime()>TimeBtwNormalTransOfToken){
                RcvTrans=false;
                lastDate=new Date();
                lastDateOfTrans=lastDate;
                EndID=-1;
                TokenMsg TM = new TokenMsg(gate.token.Transmit(), ID);
                gate.emitSingleMessage(gate.next, TM);
            }
            if(WaitingForPark.size()>0){
                if(gate.token.Occupy()){
                    String str=new String(WaitingForPark.get(0)+" come into parking lot at "+new Date().toString()+" through Gateway "+ID+"\n");
                    System.out.print(str);
                    gate.RecordStr(str);
                    RestNum--;
                    WaitingForPark.remove(0);
                }
            }
            if(WaitingForLeave.size()>0){
                gate.token.Release();
                String str=new String(WaitingForLeave.get(0)+" leave parking lot at "+new Date().toString()+" out of Gateway "+ID+"\n");
                System.out.print(str);
                gate.RecordStr(str);
                RestNum++;
                WaitingForLeave.remove(0);
            }
        }
    }
}
