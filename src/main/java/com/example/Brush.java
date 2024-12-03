package com.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class Brush extends JLabel {
    public int xx, yy;
    public Color color = Color.BLACK;
    public boolean clearC = true;

    @Override
    public synchronized void paint(Graphics g) {
        g.setColor(color);
        g.fillOval(xx-10, yy-10, 10, 10);
        if (clearC == true) {
            g.setColor(color);
            g.fillOval(xx-10, yy-10, 10, 10);
        } else if (clearC == false) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 1000, 1000);
            clearC = true;
            System.out.println("캔버스 클리어 실행됨");
        }

    }

    public synchronized void setX(int x) {
        this.xx = x;
    }

    public synchronized void setY(int y) {
        this.yy = y;
    }

    public synchronized void setColor(Color color) {
        this.color = color;
    }

    public synchronized void setClearC(boolean clearC) {
        this.clearC = clearC;
    }
}