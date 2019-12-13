package com;

import com.Message.Message;
import com.Message.TokenMsg;
import com.Thread.ClientThread;
import com.Thread.EmitMsgThread;
import com.Thread.RegisterThread;
import com.Thread.SignUpThread;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;


public class Gateway {

    public SignUpThread SUT;

    public Token token;
    private static int TotalNum = 200;
    public static int MaxNumOfGateway = 5;
    int WaitNum;
    int RestNum;/**在注册时从其他节点获得*/

    public int ID;/**通过它找到Content中的自己*/
    int selfPort;
    String selfHostname;
    String former;
    String next;/**此五者查询拓扑表获得*/
    public HashMap<Integer,Node> Content;
    public HashMap<Integer,Node> NodeTable;
    public HashMap<Integer,ObjectOutputStream> outputStreamHashMap = new HashMap<>();

    public boolean register;
    public boolean AdjMatrix[];
    public boolean TopoMap[];

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
    /**与SUT相关函数*/
    public void ReceiveAcceptMsg(int ID,boolean OK,boolean Register){
        this.SUT.ReceiveMsg(ID,OK,Register);
    }

    public Date GetRegisterTime() {
        if (SUT == null) return null;
        return this.SUT.time;
    }

    Gateway(/*boolean IfFirstNode,int restNum,HashMap<Integer,Socket> InitSocket,Socket socket*/) {
        register=false;
        WaitNum=0;
        this.token=new Token(TotalNum);
        TopoMap=new boolean[MaxNumOfGateway];
        AdjMatrix=new boolean[MaxNumOfGateway];
        NodeTable=new HashMap<>();
        for(int i=0;i<MaxNumOfGateway;i++){
            AdjMatrix[i]=false;
            TopoMap[i]=false;
        }
        this.ReadInfo("config.txt");//C:\\Users\\LENOVO\\Desktop\\Stop1210\\config.txt
        System.out.print("Current id : "+String.valueOf(ID)+"\nServer Socket : "+selfHostname+":"+String.valueOf(selfPort)+"\n");
    }

    /**消息发送函数*/
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
        /*if(IfFirstNode==true) {
            token = new com.Token(TotalNum);
            RestNum=restNum;
        }
        else{
            token=new com.Token(0);
            RestNum = TotalNum;
        }
        Sockets.putAll(InitSocket);
        self=socket;
        //acquire former and next
        Set keys=Sockets.keySet();
        Socket head=null;
        Socket end=null;
        Socket temp=null;
        for(Object key:keys){
            end=Sockets.get(key);
            if(head==null){
                head=end;
            }
        }
        boolean i=false;
        for(Object key:keys){
            if(next==null&&former!=null){
                next=temp;
            }
            if(i==true){
                next=temp;
            }
            if(self.equals(temp)){
                former=temp;
                i=true;
            }
            temp=Sockets.get(key);
        }
        if(temp==null){
            next=head;
        }
        if(former==null){
            former=end;
        }*/
//acquire former and next