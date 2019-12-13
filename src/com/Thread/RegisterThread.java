package com.Thread;

import com.Gateway;
import com.Message.Message;
import com.Message.TokenMsg;
import com.Node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;
/**用于一直启动保持检测其他节点上线*/
public class RegisterThread extends Thread {
    Gateway gate;
    Node node;

    public RegisterThread(Gateway gate, Node node) {
        this.gate = gate;
        this.node = node;
    }

    public void run() {
        Socket clientSocket;
        System.out.print(String.valueOf(node.id) + "'s RegisterThread opening\n");
        boolean flag=false;
        while (true)
        {
            if (flag == true) break;
            try {
                clientSocket = new Socket(node.hostname, node.port);
                System.out.print(clientSocket.toString()+"\n");
                node.cSocket = clientSocket;
                gate.NodeTable.put(node.id,node);
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                gate.outputStreamHashMap.put(node.id, oos);
                gate.AdjMatrix[node.id] = true;
                flag=true;
            } catch (SocketTimeoutException e) {
                //e.printStackTrace();
                System.out.print("e1\t" + "from " + String.valueOf(node.id) + " \n");
            } catch (UnknownHostException e) {
                //e.printStackTrace();
                System.out.print("e2\t" + "from " + String.valueOf(node.id) + " \n");
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.print("e3\t" + "from " + String.valueOf(node.id) + " \n");
            }
        }
    }
}
