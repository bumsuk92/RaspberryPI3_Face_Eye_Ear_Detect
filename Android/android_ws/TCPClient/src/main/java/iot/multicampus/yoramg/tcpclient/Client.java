package iot.multicampus.yoramg.tcpclient;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public interface Client {
    public BufferedReader in = null;
    public PrintWriter out  = null;
    public Socket socket = null;

    public void sendMsg(String data);
    public void connectToServer(String ipadress, int port);
    public String[] receiveMsg();
}
