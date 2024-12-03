package com.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

class ChatRoomDisplay extends JFrame implements ActionListener, KeyListener, ListSelectionListener, ChangeListener {
    private ClientThread cr_thread;
    private String idTo;
    private boolean isSelected;
    public boolean isAdmin;

    private JLabel roomer;
    public JList roomerInfo;
    private JButton coerceOut, sendWord, sendFile, quitRoom;


    //-------------------------------------------
    //캔버스 구현

    private JButton penBlack;
    private JButton penWhite;
    private JButton penRed;
    private JButton penOrange;
    private JButton penYellow;
    private JButton penGreen;
    private JButton penBlue;
    private JButton penMagenta;
    private JButton penGray;
    private JButton EraseAll;
    private BufferedImage imgBuff;
    private JLabel drawLabel;
    // private JPanel drawPanel;
    private Brush brush;
    String sendDraw = null;
    String sendColor = null;
    boolean drawPPAP = true;

    

    // -----------------------------------------------


    private Font font;
    private JViewport view;
    private JScrollPane jsp3;
    public JTextArea messages;
    public JTextField message;

    public ChatRoomDisplay(ClientThread thread) {
        super("Chat-Application-대화방");

        cr_thread = thread;
        isSelected = false;
        isAdmin = false;
        font = new Font("SanSerif", Font.PLAIN, 12);
        Container c = getContentPane();
        c.setLayout(null);

        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBounds(425, 10, 140, 175);
        p.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "참여자"));
        roomerInfo = new JList();
        roomerInfo.setFont(font);
        JScrollPane jsp2 = new JScrollPane (roomerInfo);
        roomerInfo.addListSelectionListener(this);
        jsp2.setBounds(15, 25, 110, 135);
        p.add(jsp2);

        c.add(p);

        p = new JPanel();
        p.setLayout(null);
        p.setBounds(10, 10, 410, 540);
        p.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "채팅창"));

        view = new JViewport();
        messages = new JTextArea();
        messages.setFont(font);
        messages.setEditable(false);
        view.add(messages);
        view.addChangeListener(this);
        jsp3 = new JScrollPane(view);
        jsp3.setBounds(15, 25, 380, 470);
        p.add(jsp3);

        message = new JTextField();
        message.setFont(font);
        message.addKeyListener(this);
        message.setBounds(15, 505, 380, 20);
        message.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
        p.add(message);

        c.add(p);

        coerceOut = new JButton("강 제 퇴 장");
        coerceOut.setFont(font);
        coerceOut.addActionListener(this);
        coerceOut.setBounds(445, 195, 100, 30);
        coerceOut.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
        c.add(coerceOut);

        sendWord = new JButton("귓말보내기");
        sendWord.setFont(font);
        sendWord.addActionListener(this);
        sendWord.setBounds(445, 235, 100, 30);
        sendWord.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
        c.add(sendWord);

        sendFile = new JButton("파 일 전 송");
        sendFile.setFont(font);
        sendFile.addActionListener(this);
        sendFile.setBounds(445, 275, 100, 30);
        sendFile.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
        c.add(sendFile);

        quitRoom = new JButton("퇴 실 하 기");
        quitRoom.setFont(font);
        quitRoom.addActionListener(this);
        quitRoom.setBounds(445, 315, 100, 30);
        quitRoom.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
        c.add(quitRoom);

        // -------------------------------------------------
        // 캔버스 구현

        p = new JPanel();
        p.setLayout(null);
        p.setBounds(590, 10, 540, 480);
        p.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "캔버스"));
        

        imgBuff = new BufferedImage(530, 460, BufferedImage.TYPE_INT_ARGB);
        drawLabel = new JLabel(new ImageIcon(imgBuff));
        // drawPanel = new JPanel();
        brush = new Brush();

        drawLabel.setBounds(600, 20, 530, 460);
        drawLabel.setBackground(Color.WHITE);
        brush.setBounds(600, 20, 530, 460);



        c.add(drawLabel);
        c.add(brush);
        c.add(p);
        drawLabel.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (isAdmin == true) {
					System.out.println("그리는중...");
					brush.xx = e.getX();
					brush.yy = e.getY();
                    cr_thread.requestDraw(brush.xx, brush.yy);
					brush.repaint();
					brush.printAll(imgBuff.getGraphics());
				} else {
					System.out.println("방장만이 그릴 수 있습니다.");
				 }
			}
		});

        penBlack = new JButton();
        penBlack.setBackground(Color.black);
        penBlack.setBounds(610, 500, 40, 40);
        penBlack.addActionListener(this);
        c.add(penBlack);

        penWhite = new JButton();
        penWhite.setBackground(Color.white);
        penWhite.setBounds(660, 500, 40, 40);
        penWhite.addActionListener(this);
        c.add(penWhite);

        penRed = new JButton();
        penRed.setBackground(Color.red);
        penRed.setBounds(710, 500, 40, 40);
        penRed.addActionListener(this);
        c.add(penRed);

        penOrange = new JButton();
        penOrange.setBackground(Color.orange);
        penOrange.setBounds(760, 500, 40, 40);
        penOrange.addActionListener(this);
        c.add(penOrange);

        penYellow = new JButton();
        penYellow.setBackground(Color.yellow);
        penYellow.setBounds(810, 500, 40, 40);
        penYellow.addActionListener(this);
        c.add(penYellow);

        penGreen = new JButton();
        penGreen.setBackground(Color.green);
        penGreen.setBounds(860, 500, 40, 40);
        penGreen.addActionListener(this);
        c.add(penGreen);

        penBlue = new JButton();
        penBlue.setBackground(Color.blue);
        penBlue.setBounds(910, 500, 40, 40);
        penBlue.addActionListener(this);
        c.add(penBlue);

        penMagenta = new JButton();
        penMagenta.setBackground(Color.magenta);
        penMagenta.setBounds(960, 500, 40, 40);
        penMagenta.addActionListener(this);
        c.add(penMagenta);

        penGray = new JButton();
        penGray.setBackground(Color.lightGray);
        penGray.setBounds(1010, 500, 40, 40);
        penGray.addActionListener(this);
        c.add(penGray);

        EraseAll = new JButton();
        EraseAll.setBackground(Color.DARK_GRAY);
        EraseAll.setBounds(1060, 500, 40, 40);
        EraseAll.addActionListener(this);
        c.add(EraseAll);

        // -------------------------------------------------

        Dimension dim = getToolkit().getScreenSize();
        setSize(1160, 600);
        setLocation(dim.width/2 - getWidth()/2, dim.height/2 - getHeight()/2);
        setVisible(true);

        addWindowListener(
            new WindowAdapter() {
                public void windowActivated(WindowEvent e) {
                    message.requestFocusInWindow();
                }
            }
        );
        addWindowListener(
            new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    cr_thread.requestQuitRoom();
                }
            }
        );
    }

    public void resetComponents(){
        messages.setText("");
        message.setText("");
        message.requestFocusInWindow();
    }

    public void keyPressed(KeyEvent ke){
        if (ke.getKeyChar() == KeyEvent.VK_ENTER){
            String words = message.getText();
            String data;
            String idTo;
            if(words.startsWith("/w")){
                StringTokenizer st = new StringTokenizer(words, " ");
                String command = st.nextToken();
                idTo = st.nextToken();
                data = st.nextToken();
                cr_thread.requestSendWordTo(data, idTo);
                message.setText("");
            } else {
                cr_thread.requestSendWord (words);
                message.requestFocusInWindow();
            }
        }
    }
    public void valueChanged(ListSelectionEvent e){
        isSelected = true;
        idTo = String.valueOf(((JList)e.getSource()).getSelectedValue());
    }
    public synchronized void actionPerformed (ActionEvent ae){
        if (ae.getSource() == coerceOut) {
            if (!isAdmin) {
                JOptionPane.showMessageDialog(this,"당신은 방장이 아닙니다.", "강제퇴장.", JOptionPane.ERROR_MESSAGE);
            } else if (!isSelected) {
                JOptionPane.showMessageDialog(this, "강제퇴장 ID를 선택하세요.", "강제퇴장", JOptionPane.ERROR_MESSAGE);
            } else {
                cr_thread.requestCoerceOut(idTo);
                isSelected = false;
            }
        } else if (ae.getSource() == quitRoom) {
            cr_thread.requestQuitRoom();
        } else if (ae.getSource() == sendWord) {
            String idTo, data;
            if ((idTo = JOptionPane.showInputDialog("아이디를 입력하세요.")) != null){
            if ((data = JOptionPane.showInputDialog("메시지를 입력하세요.")) != null) {   
                    cr_thread.requestSendWordTo(data, idTo);
                }
            }
        } else if (ae.getSource() == sendFile) {
            String idTo;
            if ((idTo = JOptionPane.showInputDialog("상대방 아이디를 입력하세요.")) != null){ 
                cr_thread.requestSendFile(idTo);
            }
        } else if (ae.getSource() == penBlack) {
            brush.setColor(Color.black);
            cr_thread.requestChangePen("black");
        } else if (ae.getSource() == penWhite) {
            brush.setColor(Color.white);
            cr_thread.requestChangePen("white");
        } else if (ae.getSource() == penRed) {
            brush.setColor(Color.red);
            cr_thread.requestChangePen("red");
        } else if (ae.getSource() == penOrange) {
            brush.setColor(Color.orange);
            cr_thread.requestChangePen("orange");
        } else if (ae.getSource() == penYellow) {
            brush.setColor(Color.yellow);
            cr_thread.requestChangePen("yellow");
        } else if (ae.getSource() == penGreen) {
            brush.setColor(Color.green);
            cr_thread.requestChangePen("green");
        } else if (ae.getSource() == penBlue) {
            brush.setColor(Color.blue);
            cr_thread.requestChangePen("blue");
        } else if (ae.getSource() == penMagenta) {
            brush.setColor(Color.magenta);
            cr_thread.requestChangePen("magenta");
        } else if (ae.getSource() == penGray) {
            brush.setColor(Color.lightGray);
            cr_thread.requestChangePen("lightGray");
        } else if (ae.getSource() == EraseAll) {
            brush.setClearC(false);
            cr_thread.requestChangePen("erase");
            brush.repaint();
        }
    }

    public void stateChanged(ChangeEvent e){
        jsp3.getVerticalScrollBar().setValue((jsp3.getVerticalScrollBar().getValue() + 20 ));
    }
    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    public synchronized Brush getBrush() {
        return this.brush;
    }
    public synchronized void brushBuff() {
        brush.printAll(imgBuff.getGraphics());
    }
}

