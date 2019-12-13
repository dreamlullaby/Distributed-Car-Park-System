package com;

public class Token {
    int currentNum;

    Token(int num){
        currentNum=num;
    }
    public boolean Release(){
        currentNum++;
        return true;
    }
    public boolean Occupy(){
        if(currentNum>0){
            currentNum--;
            return true;
        }
        return false;
    }
    public int Transmit(){
        int num=currentNum/2;
        currentNum-=num;
        return num;
    }
    public boolean Receive(int num){
        try {
            this.currentNum += num;
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public int curNum(){
        return currentNum;
    }
}
