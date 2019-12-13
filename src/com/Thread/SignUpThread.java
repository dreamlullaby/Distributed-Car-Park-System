package com.Thread;

import com.Gateway;
import com.Message.RegisterMsg;

import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

enum Response {
    Wait,Accept, Reject, AcceptFromInMap
}
public class SignUpThread extends Thread {
    Gateway gate;
    private static long MaxOfRandomNum = 100000000;
    private static long SignUpTimeOut = 8000;/**每个循环等待AcceptMsg的最大时间*/
    private long random;
    public Date time;
    public HashMap<Integer,Response> Mark;
    public SignUpThread(Gateway gate){
        this.gate=gate;
        this.time=new Date();
    }

    public long Getrandom(){
        /**返回当前循环的随机数以供ClientThread进行对比*/
        return this.random;
    }

    private long GetRandom(){
        /**生成一个随机数初始化RegisterMsg，在收到AcceptMsg时对照this.random是否匹配，不匹配表示不是同一个循环发送的消息，丢弃；每一个循环生成一个随机数*/
        System.out.print(Math.random()+"\n");
        return (long)(MaxOfRandomNum*Math.random());
    }

    public synchronized void ReceiveMsg(int ID,boolean OK,boolean Register) {
        if (OK == false) Mark.put(ID, Response.Reject);
        else if (OK == true && Register == true) Mark.put(ID, Response.Accept);
        else Mark.put(ID,Response.AcceptFromInMap);
        Set keys=Mark.keySet();
        for(Object key : keys){
            System.out.print(String.valueOf(key)+"\t"+Mark.get(key)+"\t");
        }
        System.out.print("\n");
    }

/**启动之后向所有在线节点发送RegisterMsg等待回复，如所有接收AcceptMsg均为OK则将gate的注册状态置为true，否则重新开始发送*/
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            long loop_start=new Date().getTime();
            Mark = new HashMap<>();
            int count_Ack = 0;
            time = new Date();
            random=GetRandom();
            for (int i = 0; i < gate.AdjMatrix.length; i++) {
                if (gate.AdjMatrix[i]&&i!=gate.ID) {
                    RegisterMsg RM = new RegisterMsg(gate.ID, time,random);
                    gate.emitSingleMessage(i, RM);
                    Mark.put(i, Response.Wait);
                    System.out.print("Send RegisterMsg to Gateway "+String.valueOf(i)+"\n");
                }
            }
            Set keys = Mark.keySet();
            boolean flag = false;
            while (!flag) {
                for (Object key : keys) {
                    if (Mark.get(key).equals(Response.Reject)) {
                        flag = true;
                    } else if (Mark.get(key).equals(Response.Accept) || Mark.get(key).equals(Response.AcceptFromInMap)) {
                        count_Ack++;
                        loop_start=new Date().getTime();
                        if (count_Ack == Mark.size()) {
                            synchronized (gate) {
                                gate.register = true;/**进入临界区改变gate状态为已进入拓扑*/
                                System.out.print("成功进入拓扑\n");
                                return;
                            }
                        }
                    }
                }
                if(flag==true){
                    System.out.print("加入拓扑被拒绝\n");
                    break;
                }
                if(new Date().getTime()-loop_start>=SignUpTimeOut) {
                    System.out.print("等待AcceptMsg超时 : from " + String.valueOf(loop_start) + " to " + String.valueOf(new Date().getTime()) + "\n");
                    break;
                }
            }
            try {
                Thread.sleep(1000 + (int) (2000 * Math.random()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.print("SignUpThread Reopened\n");
        }
    }

}
