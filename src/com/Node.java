package com;

import java.net.Socket;

public class Node {
    public int id;/**字符串标识符，如：出入口A*/
    public String hostname;
    public int port;
    public Socket cSocket;
    public Node(int id,String hostname,int port){
        this.id=id;
        this.hostname=hostname;
        this.port=port;
    }
}
