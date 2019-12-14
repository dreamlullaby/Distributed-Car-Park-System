package com;

public class Token {
    int currentNum;

    Token(int num){
        currentNum=num;
    }
    public synchronized boolean Release(){
        currentNum++;
        return true;
    }
    public synchronized boolean Occupy(){
        if(currentNum>0){
            currentNum--;
            return true;
        }
        return false;
    }
    public synchronized int Transmit(){
        int num=currentNum/2;
        currentNum-=num;
        return num;
    }
    public synchronized int SingleTokenTransmit(){
        int num=currentNum;
        currentNum=0;
        return num;
    }
    public synchronized boolean Receive(int num){
        try {
            this.currentNum += num;
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public synchronized int curNum(){
        return currentNum;
    }
}
