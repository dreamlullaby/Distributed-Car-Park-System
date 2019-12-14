package com;

import com.Message.ChangeTopoMsg;
import com.Message.Message;
import com.Message.TokenMsg;
import com.Thread.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;


public class Gateway {

    public SignUpThread SUT;
    public ServerThread ST;

    public Writer Record;

    public Token token;
    private static int TotalNum = 200;
    public static int MaxNumOfGateway = 5;
    int RestNum;/**在注册时从其他节点获得*/

    /**本节点信息*/
    public int ID;
    String selfHostname;
    int selfPort;
    public int former;
    public int next;
    /**此五者查询拓扑表TopoTable获得*/
    public HashMap<Integer,Node> Content;/**所有可能连接的节点信息*/
    public HashMap<Integer,Node> NodeTable;/**已连接的节点信息*/
    public HashMap<Integer,ObjectOutputStream> outputStreamHashMap = new HashMap<>();

    public boolean register;/**是否进入环状节点*/
    public boolean AdjMatrix[];/**邻接矩阵（线性表）表明与何节点生成全连接*/
    public boolean TopoTable[];/**拓扑表（线性表）储存进入环状拓扑的节点*/
    public int NumInTopo;

    /**读取文件填写系统全节点信息*/
    void ReadInfo(String confPath){
        String conf;
        try{
            InputStream is = new FileInputStream(confPath);
            int iAvail = is.available();
            int count=0;
            byte[] bytes = new byte[iAvail];
            is.read(bytes);
            conf=new String(bytes);
            is.close();
            String[] IDpart=conf.split(" >>>\r\n");
            ID=Integer.parseInt(IDpart[0]);
            String[] configuration=IDpart[1].split("\r\n");
            this.Content=new HashMap<>();
            for(String str:configuration){
                String[] components=str.split(" ");
                if(ID==count++){
                    ID=Integer.parseInt(components[0]);
                    selfHostname=components[1];
                    selfPort=Integer.parseInt(components[2]);
                }
                Node node=new Node(Integer.parseInt(components[0]),components[1],Integer.parseInt(components[2]));
                this.Content.put(node.id,node);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /**文件流写入函数*/
    private void InitRecorder()throws IOException{
        String filepath="Gateway"+String.valueOf(ID)+"RecordFile.txt";
        File outfile=new File(filepath);
        outfile.createNewFile();
        Record=new FileWriter(outfile);
        Record.write("TITLE  :  Node "+String.valueOf(ID)+" 's Recording File\r\n");
        Record.flush();
    }

    public void RecordStr(String str) {
        try {
            Record.write(str);
            Record.flush();
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    /**与SUT相关函数*/
    public void ReceiveAcceptMsg(int ID,boolean OK,boolean Register,boolean[]TopoTable){
        this.SUT.ReceiveAcceptMsg(ID,OK,Register,TopoTable);
    }

    
    /**初始化函数*/
    Gateway(/*boolean IfFirstNode,int restNum,HashMap<Integer,Socket> InitSocket,Socket socket*/) {
        register=false;
        TopoTable=new boolean[MaxNumOfGateway];
        AdjMatrix=new boolean[MaxNumOfGateway];
        NodeTable=new HashMap<>();
        for(int i=0;i<MaxNumOfGateway;i++){
            AdjMatrix[i]=false;
            TopoTable[i]=false;
        }
        this.ReadInfo("config.txt");
        try{
            InitRecorder();
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }
        System.out.print("Current id : "+String.valueOf(ID)+"\nServer Socket : "+selfHostname+":"+String.valueOf(selfPort)+"\n");
    }

    public synchronized void InitGateway(boolean IsFirstNode,boolean[]TopoTable,int Num){
        if(IsFirstNode==true){
            this.token=new Token(TotalNum);
        }
        else{
            this.token=new Token(0);
        }
        this.NumInTopo=Num;
        this.TopoTable=TopoTable.clone();
        this.register=true;
        for(int i=0;i<this.TopoTable.length;i++){
            System.out.print(String.valueOf(this.TopoTable[i])+"\t");
            if(this.TopoTable[i]&&i!=ID){
                ChangeTopoMsg CTM=new ChangeTopoMsg(ID,1);
                emitSingleMessage(i,CTM);
            }
        }
        System.out.print("...成功进入拓扑;获得Token : "+String.valueOf(token.curNum())+"\n");
        TrimTopo();
    }

    public synchronized void ChangeTopo(int ID,int AOR) {/**ClientThread接收到ChangeTopoMsg之后调用*/
        System.out.print("Receiving ChangTopoMsg from Gateway " + ID + " : " + AOR + "\n");
        if (AOR == 1) {
            TopoTable[ID] = true;
        } else if (AOR == -1) {
            TopoTable[ID] = false;
        } else {
            System.out.print("Error in Changing TopoTable\n");
            return;
        }
        TrimTopo();
    }

    public synchronized void TrimTopo(){
        int count=0;
        for(int i=0;i<MaxNumOfGateway;i++){
            if(TopoTable[i]) count++;
        }
        this.NumInTopo=count;
        if(count==0){
            if(ST==null) {
                ST = new ServerThread(this);
                ST.start();
            }
            return;
        }
        count=ID;
        while(true) {
            count--;
            count = (count + MaxNumOfGateway) % MaxNumOfGateway;
            if (TopoTable[count] == true) {
                this.former = count;
                break;
            }
        }
        count=ID;
        while(true){
            count++;
            count%=MaxNumOfGateway;
            if(TopoTable[count]==true){
                this.next=count;
                break;
            }
        }
        if(ST==null) {
            ST = new ServerThread(this);
            ST.start();
        }
        System.out.print("Trimming and former is "+String.valueOf(former)+" and next is "+String.valueOf(next)+"\n");
    }
    /**消息发送或接收函数*/
    public synchronized void emitSingleMessage(int id, Message msg) {
        try {
            ObjectOutputStream oos = this.outputStreamHashMap.get(id);
            oos.writeObject(msg);
            oos.flush();
            if(msg instanceof TokenMsg){
                //System.out.print("Send Token "+((TokenMsg) msg).TokenNum+" to Gateway "+id+"\n");
            }
        } catch (IOException e) {
            System.out.print("error in emit single message\n");
            //e.printStackTrace();
        }
    }

    public synchronized void ReceiveTokenMsg(int num,int ID){
        token.Receive(num);
        ST.ReceiveTokenMsg(ID);
    }

    public static void main(String[] args) throws IOException {
        Gateway gate = new Gateway();/**初始化进程*/

        /**Start Server Socket*/
        ServerSocket listener = new ServerSocket(gate.selfPort);
        System.out.print(listener.toString() + "\n");
        /**Start Client Socket*/
        Set locations = gate.Content.keySet();
        for (Object location : locations) {
            if (gate.Content.get(location).id == gate.ID) continue;
            new RegisterThread(gate, gate.Content.get(location)).start();
        }

        System.out.print("accepting\n");
        new EmitMsgThread(gate).start();
        gate.SUT=new SignUpThread(gate);
        gate.SUT.start();
        while (true) {
            Socket socket = listener.accept();
            new ClientThread(gate,socket).start();
            System.out.print(socket.toString() + " has been opened\n");
        }
    }
}