package com.example;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ReceiveFile extends JFrame implements ActionListener {
    public static final int port = 3777;
    public JLabel lbl;
    public JTextArea txt;
    public JButton btn;

    public ReceiveFile(){
        super("파일전송");
        setLayout(null);
        lbl = new JLabel("파일 전송을 기다립니다.");
        lbl.setBounds(10, 30, 230, 20);
        lbl.setBackground(Color.gray);
        lbl.setForeground(Color.BLACK);
        add(lbl);
        txt = new JTextArea("", 0, 0);
        txt.setBounds(10, 60, 230, 100);
        txt.setEditable(false);
        add(txt);
        btn = new JButton("닫기");
        btn.setBounds(105, 170, 40, 20);
        btn.setVisible(false);
        btn.addActionListener(this);
        add(btn);
        addWindowListener(new WinListener());
        setSize(250, 220);
        setVisible(true);

        try{
            ServerSocket socket = new ServerSocket(port);
            Socket sock = null;
            FileThread client = null;
            try{
                sock = socket.accept();
                client = new FileThread(this, sock);
                client.start();
            } catch (IOException e){
                System.out.println(e);
                try{
                    if(sock != null) sock.close();
                }catch (IOException el){
                    System.out.println(el);
                }finally{
                    sock = null;
                }
            }
        }catch(IOException e){}
    }
    public void actionPerformed (ActionEvent e){ 
        dispose();
    }
    class WinListener extends WindowAdapter{
        public void windowClosing(WindowEvent we){ 
            dispose();
        }
                    
    }
}
