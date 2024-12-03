package com.example;

import java.io.IOException;

import java.util.ArrayList;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;


public class ChatServer {
    private static final String TAG = "ChatServer : "; // 태그 생성.
    private ArrayList<ServerThread> vcClient;
    private ServerSocket serverSocket;
    private Socket socket;
    private InetSocketAddress isa;

    public static final int cs_port = 2777;
    public static final int cs_maxclient = 50;

    public ChatServer() {
       String strServerListenIp = "0.0.0.0";
        int nServerListenPort = cs_port;
        
        try {
            serverSocket = new ServerSocket();
            
            isa = new InetSocketAddress(strServerListenIp, nServerListenPort);
            serverSocket.bind(isa);
            vcClient = new ArrayList<>();
            //serverSocket.bind(isa);
            
            while(true) {
                socket = null;
                ServerThread ci = null;
                System.out.println(TAG + "클라이언트 요청 대기중.....");
                try {
                    socket = serverSocket.accept();
                    isa=(InetSocketAddress)socket.getRemoteSocketAddress();
                    ci = new ServerThread(this, socket);
                    System.out.println(TAG + "요청이 성공함");
                    System.out.printf("요청한 연결을 확인함 :  [%s] [%d]\n " , isa.getHostName(), nServerListenPort);
                    ci.start();
                    vcClient.add(ci);
                } catch(IOException e) {
                    System.out.println(TAG + e);
                    try {
                        if (socket != null) socket.close();
                    } catch(IOException el) {
                        System.out.println(TAG + el);
                    } finally {
                        socket = null; 
                    }
                }
                
            }
        } catch (Exception e) {
             System.out.println(TAG + "연결안됨");
        }
    }
    public ArrayList<ServerThread> getvcClient() {
        return vcClient;
    }
    public static void main(String[] args) {
        new ChatServer();
    }
    
}