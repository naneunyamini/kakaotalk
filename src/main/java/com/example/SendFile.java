package com.example;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class SendFile extends JFrame implements ActionListener{
    private JTextField tf_filename;
    private JButton bt_dialog, bt_send, bt_close;
    private JLabel lb_status;
    private static final String SEPARATOR = "|";
    private String address;
    public SendFile(String address) {
        super("파일전송");
        this.address = address;

        setLayout(null);

        JLabel lbl = new JLabel("파일이름");
        lbl.setFont(new Font("SanSerif", Font.PLAIN, 12));
        lbl.setBounds(10, 30, 60, 20);
        add(lbl);

        tf_filename = new JTextField();
        tf_filename.setBounds(80, 30, 160, 20);
        add(tf_filename);

        bt_dialog = new JButton("찾아보기");
        bt_dialog.setFont(new Font("SanSerif", Font.PLAIN, 12));
        bt_dialog.setBounds(20, 60, 80, 20);
        bt_dialog.addActionListener(this);
        add(bt_dialog);

        bt_send = new JButton("전송");
        bt_send.setFont(new Font("SanSerif", Font.PLAIN, 12));
        bt_send.setBounds(120, 60, 80, 20);
        bt_send.addActionListener(this);
        add(bt_send);

        bt_close = new JButton("종료");
        bt_close.setFont(new Font("SanSerif", Font.PLAIN, 12));
        bt_close.setBounds(220, 60, 80, 20);
        bt_close.addActionListener(this);
        add(bt_close);

        lb_status = new JLabel("파일전송 대기중...");
        lb_status.setFont(new Font("SanSerif", Font.PLAIN, 12));

        lb_status.setBounds(10, 90, 230, 20); 
        lb_status.setBackground(Color.gray);
        lb_status.setForeground(Color.BLACK);

        add(lb_status);

        addWindowListener(new WinListener());

        setSize(320, 230);
        setVisible(true);
    }
    public void actionPerformed (ActionEvent e){
        if (e.getSource() == bt_dialog){
        FileDialog fd = new FileDialog(this, "파일 찾기", FileDialog.LOAD);
        fd.setFont(new Font("SanSerif", Font.PLAIN, 12));
        fd.setVisible(true);
        tf_filename.setText(fd.getDirectory() + fd.getFile());
        if (tf_filename.getText().startsWith("null"))
        tf_filename.setText("");
        } else if(e.getSource() == bt_send){
            String filename = tf_filename.getText();
            if(filename.equals("")){
                lb_status.setName("파일이름을 입력하세요.");
                return;
            }

        lb_status.setText("파일검색중..");
        File file = new File(filename);

        if (!file.exists()) {
            lb_status.setText("해당파일을 찾을 수 없습니다.");
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int fileLength = (int)file.length();

        buffer.append(file.getName());
        buffer.append(SEPARATOR);
        buffer.append(fileLength);
        lb_status.setText("연결설정중.....");

        try{
            Socket sock = new Socket(address, 3777);
            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream bin = new BufferedInputStream(fin, fileLength);
            byte data[] = new byte[fileLength];
            try{
                lb_status.setText(" ");
                bin.read(data, 0, fileLength);
                bin.close();
            }catch (IOException err){
                lb_status.setText("파일읽기 오류.");
                return;
            }
            
            for(int i = 0; i < data.length; i++) {
            System.out.print(data[i]);
            }

            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            out.writeUTF(buffer.toString());

            tf_filename.setText("");
            lb_status.setText("파일전송중......( 0 Byte)");
            BufferedOutputStream bout = new BufferedOutputStream(out, 2048);
            DataInputStream din = new DataInputStream(sock.getInputStream());
            sendFile(bout, din, data, fileLength);
            bout.close();
            din.close();

            lb_status.setText(file.getName() + " 파일전송이 완료되었습니다.");
            sock.close();
            } catch (IOException e1){
                System.out.println(e1);
                lb_status.setText(address + "로의 연결에 실패하였습니다.");
            }
        } else if(e.getSource() == bt_close){
            dispose();
        }
    }
    private void sendFile(BufferedOutputStream bout, DataInputStream din, byte[] data, int fileLength) throws IOException{
        int size = 2048;
        int count = fileLength/size;
        int rest = fileLength%size;
        int flag = 1;
        if(count == 0) flag = 0;
        for(int i=0; i<=count; i++){
            if(i == count && flag == 0){
                bout.write(data, 0, rest);
                bout.flush();
                return;
            } else if(i == count){
                bout.write(data, i*size, rest);
                bout.flush();
                return;
            } else {
                bout.write(data, i*size, size);
                bout.flush();
                lb_status.setText("파일전송중......(" + ((i+1)*size) + "/" + fileLength + " Byte)");
                din.readUTF();
            }
        }
    }
    class WinListener extends WindowAdapter{
            public void windowClosing (WindowEvent we){ 
                System.exit(0);
        }
            
    }
}