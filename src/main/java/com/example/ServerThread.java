package com.example;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.util.NoSuchElementException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import java.net.ServerSocket;
import java.net.Socket;

class ServerThread extends Thread {
    private static final String TAG = "SererThread : "; // 태그 생성.
    private ChatServer st_server;
    private Socket socket;
    private DataInputStream st_in;
    private DataOutputStream st_out;
    private StringBuffer st_buffer;
    private WaitRoom st_waitRoom;
    public String st_ID;
    public int st_roomNumber;
    
    private static final String SEPARATOR = "|";
    private static final String DELIMETER = ".";
    private static final int WAITROOM = 0;
    
    private static final int REQ_LOGON = 1001;
    private static final int REQ_CREATEROOM = 1011;
    private static final int REQ_ENTERROOM = 1021;
    private static final int REQ_QUITROOM = 1031;
    private static final int REQ_LOGOUT = 1041;
    private static final int REQ_SENDWORD = 1051;
    private static final int REQ_SENDWORDTO = 1052;
    private static final int REQ_COERCEOUT = 1053;
    private static final int REQ_SENDFILE = 1061;
    
    private static final int YES_LOGON = 2001;
    private static final int NO_LOGON = 2002;
    private static final int YES_CREATEROOM = 2011;
    private static final int NO_CREATEROOM = 2012;
    private static final int YES_ENTERROOM = 2021;
    private static final int NO_ENTERROOM = 2022;
    private static final int YES_QUITROOM = 2031;
    private static final int YES_LOGOUT = 2041;
    private static final int YES_SENDWORD = 2051;
    private static final int YES_SENDWORDTO = 2052;
    private static final int NO_SENDWORDTO = 2053;
    private static final int YES_COERCEOUT = 2054;
    private static final int YES_SENDFILE = 2061;
    private static final int NO_SENDFILE = 2062;
    private static final int MDY_WAITUSER = 2003;
    private static final int MDY_WAITINFO = 2013;
    private static final int MDY_ROOMUSER = 2023;

    // 캔버스
    private static final int DRAW_CANVAS = 4001;
    private static final int CHANGE_PEN = 4002;
    
    private static final int ERR_ALREADYUSER = 3001;
    private static final int ERR_SERVERFULL = 3002;
    private static final int ERR_ROOMSFULL = 3011;
    private static final int ERR_ROOMERFULL = 3021;
    private static final int ERR_PASSWORD = 3022;
    private static final int ERR_REJECTION = 3031;
    private static final int ERR_NOUSER = 3032;

    public ServerThread(ChatServer st_server, Socket socket) {
        try {
            this.st_server = st_server;
            this.socket = socket;
            st_in = new DataInputStream(socket.getInputStream());
            st_out = new DataOutputStream(socket.getOutputStream());
            st_buffer = new StringBuffer(2048);
            st_waitRoom = new WaitRoom();
        } catch (IOException e) {
            System.out.println(TAG + e);
        }
    }

    private void pt_sendErrCode(int message, int errCode) throws IOException {
        st_buffer.setLength(0);
        st_buffer.append(message);
        st_buffer.append(SEPARATOR);
        st_buffer.append(errCode);
        pt_send(st_buffer.toString());
    }

    private void pt_modifyWaitRoom() throws IOException{
        st_buffer.setLength(0);
        st_buffer.append(MDY_WAITINFO);
        st_buffer.append(SEPARATOR);
        st_buffer.append(st_waitRoom.getWaitRoomInfo());
        pt_broadcast(st_buffer.toString(), WAITROOM);
    }
    
    private void pt_modifyWaitUser() throws IOException{
        String ids = st_waitRoom.getUsers();
        st_buffer.setLength(0);
        st_buffer.append(MDY_WAITUSER);
        st_buffer.append (SEPARATOR);
        st_buffer.append(ids);
        pt_broadcast(st_buffer.toString(), WAITROOM);
    }
    
    private void pt_modifyRoomUser(int roomNumber, String id, int code) throws IOException{
        String ids = st_waitRoom.getRoomInfo(roomNumber);
        st_buffer.setLength(0);
        st_buffer.append(MDY_ROOMUSER);
        st_buffer.append(SEPARATOR);
        st_buffer.append(id);
        st_buffer.append(SEPARATOR);
        st_buffer.append(code);
        st_buffer.append (SEPARATOR);
        st_buffer.append(ids);
        pt_broadcast(st_buffer.toString(), roomNumber);
    }
    
    private void pt_send(String sendData) throws IOException{
        synchronized(st_out){
            System.out.println(sendData);
            st_out.writeUTF(sendData);
            st_out.flush();
        }
    }
    
    private synchronized void pt_broadcast(String sendData, int roomNumber) throws IOException{
        ServerThread client;
        HashMap<String, ServerThread> clients = st_waitRoom.getClients(roomNumber);
        Iterator<String> enu = clients.keySet().iterator();
        while(enu.hasNext()){
            client = (ServerThread)clients.get(enu.next());
            client.pt_send(sendData);
        }
    }

    private synchronized void pt_drawCanvas(int roomNumber, int x, int y) {
        try {
            String ids = st_waitRoom.getRoomInfo(roomNumber);
            st_buffer.setLength(0);
            st_buffer.append(DRAW_CANVAS);
            st_buffer.append(SEPARATOR);
            st_buffer.append(x);
            st_buffer.append(SEPARATOR);
            st_buffer.append(y);
            pt_broadcast(st_buffer.toString(), roomNumber);
        } catch (IOException e) {
            System.out.println(TAG + e);
        }
    }
    

    @Override
    public void run() {
        try {
            while(true) {
                String recvData = st_in.readUTF();
                System.out.println(recvData);
            
                StringTokenizer st = new StringTokenizer(recvData, SEPARATOR);
                int command = Integer.parseInt(st.nextToken());
                switch(command){
                    case REQ_LOGON : {
                        st_roomNumber = WAITROOM;
                        int result;
                        st_ID = st.nextToken();
                        result = st_waitRoom.addUser(st_ID, this);
                        st_buffer.setLength(0);
                        if(result == 0){
                            st_buffer.append(YES_LOGON); 
                            st_buffer.append(SEPARATOR);
                            st_buffer.append(st_waitRoom.getRooms());
                            pt_send(st_buffer.toString());
                            pt_modifyWaitUser();
                            System.out.println(st_ID + "의 연결요청 승인");
                    } else {
                        pt_sendErrCode(NO_LOGON, result);
                    }
                    break;
                }
                case REQ_CREATEROOM : {
                    String id, roomName, password; 
                    int roomMaxUser, result;
                    boolean isRock;
                    id = st.nextToken();
                    String roomInfo = st.nextToken();
                    StringTokenizer room = new StringTokenizer(roomInfo, DELIMETER);
                    roomName = room.nextToken();
                    roomMaxUser = Integer.parseInt(room.nextToken());
                    isRock = (Integer.parseInt(room.nextToken()) == 0) ? false : true;
                    password = room.nextToken();
                    
                    ChatRoom chatRoom = new ChatRoom(roomName, roomMaxUser,isRock, password, id);

                    result = st_waitRoom.addRoom(chatRoom);
                    if (result == 0) {
                        st_roomNumber = ChatRoom.getRoomNumber();
                        boolean temp = chatRoom.addUser(st_ID, this);
                        st_waitRoom.delUser(st_ID);
                        
                        st_buffer.setLength(0);
                        st_buffer.append(YES_CREATEROOM);
                        st_buffer.append(SEPARATOR);
                        st_buffer.append(st_roomNumber);
                        pt_send(st_buffer.toString());
                        pt_modifyWaitRoom();
                        pt_modifyRoomUser(st_roomNumber, id, 1);
                    }
                    else {
                        pt_sendErrCode(NO_CREATEROOM, result);
                    }
                    break;
                }
                case REQ_ENTERROOM : {
                    String id, password;
                    int roomNumber, result;
                    id = st.nextToken();
                    roomNumber = Integer.parseInt(st.nextToken());
                    try {
                        password = st.nextToken();
                    }
                    catch(NoSuchElementException e) {
                        password = "0";
                    }
                    result = st_waitRoom.joinRoom(id, this, roomNumber, password);
                    
                    if (result == 0) {
                        st_buffer.setLength(0);
                        st_buffer.append(YES_ENTERROOM);
                        st_buffer.append(SEPARATOR); 
                        st_buffer.append(roomNumber);
                        st_buffer.append(SEPARATOR);
                        st_buffer.append(id);
                        st_roomNumber = roomNumber;
                        pt_send(st_buffer.toString());
                        pt_modifyRoomUser(roomNumber, id, 1);
                        pt_modifyWaitRoom();
                    } else {
                        pt_sendErrCode(NO_ENTERROOM, result);
                    }
                    break;
                }
                case REQ_QUITROOM : {
                    String id;
                    int roomNumber;
                    boolean updateWaitInfo;
                    id = st.nextToken();
                    roomNumber = Integer.parseInt(st.nextToken());
                    updateWaitInfo = st_waitRoom.quitRoom(id, roomNumber, this);
                    st_buffer.setLength(0);
                    st_buffer.append(YES_QUITROOM);
                    st_buffer.append(SEPARATOR);
                    st_buffer.append(id);
                    pt_send(st_buffer.toString());
                    st_roomNumber = WAITROOM;
                    
                    if(updateWaitInfo) {
                        pt_modifyWaitRoom();
                    }else {
                        pt_modifyWaitRoom();
                        pt_modifyRoomUser(roomNumber, id, 0);
                    }
                    break;
                }
                case REQ_LOGOUT : {
                    String id = st.nextToken();
                    st_waitRoom.delUser(id);
                    st_buffer.setLength(0);
                    st_buffer.append(YES_LOGOUT);
                    pt_send(st_buffer.toString());
                    pt_modifyWaitUser();
                    release();
                    break;
                }
                case REQ_SENDWORD: {
                    String id = st.nextToken();
                    int roomNumber = Integer.parseInt(st.nextToken());
                    st_buffer.setLength(0);
                    st_buffer.append(YES_SENDWORD);
                    st_buffer.append(SEPARATOR);
                    st_buffer.append(id);
                    st_buffer.append(SEPARATOR);
                    st_buffer.append(st_roomNumber);
                    st_buffer.append (SEPARATOR);
                    try {
                        String data = st.nextToken();
                        st_buffer.append(data);
                    }catch (NoSuchElementException e) {}
                    
                    pt_broadcast(st_buffer.toString(), roomNumber);
                    break;
                }
                
                case REQ_SENDWORDTO : {
                    String id = st.nextToken();
                    int roomNumber = Integer.parseInt(st.nextToken());
                    String idTo = st.nextToken();
                    
                    HashMap<String, ServerThread> room = st_waitRoom.getClients(roomNumber);
                    ServerThread client = null;
                    if ((client = (ServerThread) room.get(idTo)) != null) {
                        st_buffer.setLength(0);
                        st_buffer.append(YES_SENDWORDTO);
                        st_buffer.append(SEPARATOR);
                        st_buffer.append(id);
                        st_buffer.append(SEPARATOR);
                        st_buffer.append(idTo);
                        st_buffer.append(SEPARATOR);
                        st_buffer.append(st_roomNumber);
                        st_buffer.append(SEPARATOR);
                        try {
                            String data = st.nextToken();
                            st_buffer.append(data);
                        }catch(NoSuchElementException e) {}
                            client.pt_send(st_buffer.toString());
                            pt_send(st_buffer.toString());
                        break;
                    }else {
                        st_buffer.setLength(0);
                        st_buffer.append(NO_SENDWORDTO);
                        st_buffer.append (SEPARATOR);
                        st_buffer.append(idTo);
                        st_buffer.append (SEPARATOR);
                        st_buffer.append(st_roomNumber);
                        pt_send(st_buffer.toString());
                        break;
                    }
                }
                case REQ_SENDFILE: {
                    String id = st.nextToken();
                    int roomNumber = Integer.parseInt(st.nextToken());
                    String idTo = st.nextToken();
                    
                    HashMap<String, ServerThread> room = st_waitRoom.getClients(roomNumber);
                    ServerThread client = null;
                    if ((client = (ServerThread) room.get(idTo)) != null) {
                        st_buffer.setLength(0);
                        st_buffer.append (REQ_SENDFILE);
                        st_buffer.append(SEPARATOR);
                        st_buffer.append(id);
                        st_buffer.append (SEPARATOR);
                        st_buffer.append(st_roomNumber);
                        client.pt_send(st_buffer.toString());
                        break;
                    } else {
                        st_buffer.setLength(0);
                        st_buffer.append(NO_SENDFILE);
                        st_buffer.append (SEPARATOR); st_buffer.append(ERR_NOUSER);
                        st_buffer.append(SEPARATOR);
                        st_buffer.append(idTo);
                        pt_send(st_buffer.toString());
                        break;
                    }
                }
                case NO_SENDFILE : {
                    String id = st.nextToken();
                    int roomNumber = Integer.parseInt(st.nextToken());
                    String idTo = st.nextToken();
                    
                    HashMap<String, ServerThread> room = st_waitRoom.getClients(roomNumber);
                    ServerThread client = null;
                    client = (ServerThread) room.get(idTo);
                    
                    st_buffer.setLength(0);
                    st_buffer.append(NO_SENDFILE);
                    st_buffer.append(SEPARATOR);
                    st_buffer.append(ERR_REJECTION);
                    st_buffer.append(SEPARATOR);
                    st_buffer.append(id);
                    client.pt_send(st_buffer.toString());
                    break;
                }
                
                case YES_SENDFILE: {
                    String id = st.nextToken();
                    int roomNumber = Integer.parseInt(st.nextToken());
                    String idTo = st.nextToken();
                    String hostaddr = st.nextToken();
                    
                    HashMap<String, ServerThread> room = st_waitRoom.getClients(roomNumber);
                    ServerThread client = null;
                    client = (ServerThread) room.get(idTo);
                    st_buffer.setLength(0);
                    st_buffer.append(YES_SENDFILE);
                    st_buffer.append(SEPARATOR);
                    st_buffer.append(id);
                    st_buffer.append(SEPARATOR);
                    st_buffer.append(hostaddr);
                    
                    client.pt_send(st_buffer.toString());
                    break;
                }
                
                case REQ_COERCEOUT : {
                    int roomNumber = Integer.parseInt(st.nextToken());
                    String idTo = st.nextToken();
                    boolean updateWaitInfo;
                    HashMap<String, ServerThread> room = st_waitRoom.getClients(roomNumber);
                    ServerThread client = null;
                    client = (ServerThread) room.get(idTo);
                    updateWaitInfo = st_waitRoom.quitRoom(idTo, roomNumber, client);
                    
                    st_buffer.setLength(0);
                    st_buffer.append(YES_COERCEOUT);
                    client.pt_send(st_buffer.toString());
                    client.st_roomNumber = 0;
                    
                    if (updateWaitInfo) {
                        pt_modifyWaitRoom();
                    }else {
                        pt_modifyWaitRoom();
                        pt_modifyRoomUser(roomNumber, idTo, 2);
                    }
                    break;
                }
           
                case DRAW_CANVAS : {
                    int roomNumber = Integer.parseInt(st.nextToken());
                    int x = Integer.parseInt(st.nextToken());
                    int y = Integer.parseInt(st.nextToken());

                    st_buffer.setLength(0);
                    st_buffer.append(DRAW_CANVAS);
                    st_buffer.append(SEPARATOR);
                    st_buffer.append(x);
                    st_buffer.append(SEPARATOR);
                    st_buffer.append(y);
                    
                    pt_broadcast(st_buffer.toString(), roomNumber);
                    break;
                }
                case CHANGE_PEN : {
                    int roomNumber = Integer.parseInt(st.nextToken());
                    String color = st.nextToken();

                    st_buffer.setLength(0);
                    st_buffer.append(CHANGE_PEN);
                    st_buffer.append(SEPARATOR);
                    st_buffer.append(color);
                    
                    pt_broadcast(st_buffer.toString(), roomNumber);
                    break;
                }
            }
            Thread.sleep(1);
        }
    }catch(NullPointerException e) {
    }catch(InterruptedException e) {
        System.out.println(e);
        
        if(st_roomNumber ==0) {
            st_waitRoom.delUser(st_ID);
        }else {
            boolean temp = st_waitRoom.quitRoom(st_ID, st_roomNumber, this);
            st_waitRoom.delUser(st_ID);
        }
        release();
    }catch(IOException e) {
        System.out.println(e);
        
        if(st_roomNumber ==0) {
            st_waitRoom.delUser(st_ID);
        }else {
            boolean temp = st_waitRoom.quitRoom(st_ID, st_roomNumber, this);
            st_waitRoom.delUser(st_ID);
        }
        release();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void release() {
        try {
        if(st_in != null) st_in.close();
    }catch(IOException e1) {}
    finally {
        st_in = null;
    }
    try {
        if(st_out != null) st_out.close();
    }catch(IOException e1) {}
    finally {
        st_out = null;
    }
    try {
        if(socket != null) socket.close();
    }catch(IOException e1) {}
    finally {
        socket = null;
    }
    
    if(st_ID != null) {
        System.out.println(st_ID+"와 연결을 종료합니다.");
        st_ID = null;
    }
}
}
