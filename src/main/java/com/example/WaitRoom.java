package com.example;

import java.util.*;

class WaitRoom {
	private static final int MAX_ROOM = 10;
	private static final int MAX_USER = 100;
	private static final String SEPARATOR = "|";
	private static final String DELIMETER = ".";
	private static final String DELIMETER1 = "=";
	
	private static final int ERR_ALREADYUSER = 3001;
	private static final int ERR_SERVERFULL = 3002;
	private static final int ERR_ROOMSFULL = 3011;
	private static final int ERR_ROOMERFULL = 3021;
	private static final int ERR_PASSWORD = 3022;
	
	private static ArrayList<String> userList;
	private static ArrayList<ChatRoom> roomList; 
	private static HashMap<String, ServerThread> userHash;
	private static HashMap<Integer, ChatRoom> roomHash;
	
	private static int userCount;
	private static int roomCount;
	
	static{
		userList = new ArrayList<>(MAX_USER);
		roomList = new ArrayList<>(MAX_ROOM);
		userHash = new HashMap<>(MAX_USER);
		roomHash = new HashMap<>(MAX_ROOM);
		userCount = 0;
		roomCount = 0;
	}
	
	public WaitRoom() {}
	
	public synchronized int addUser(String id, ServerThread client) {
		if(userCount == MAX_USER) return ERR_SERVERFULL;
		
		Iterator<String> ids = userList.iterator();
		while(ids.hasNext()){
			String tempID = (String)ids.next();
			if(tempID.equals(id)) return ERR_ALREADYUSER;
		}
		Iterator<ChatRoom> rooms = roomList.iterator();
		while(rooms.hasNext()) {
			ChatRoom tempRoom = (ChatRoom)rooms.next();
			if(tempRoom.checkUserIDs(id)) return ERR_ALREADYUSER;
		}
		
		userList.add(id);
		userHash.put(id, client);
		client.st_ID = id;
		client.st_roomNumber = 0;
		userCount++;
		return 0;
		
	}
	
	public synchronized void delUser(String id) {
		userList.remove(id);
		userHash.remove(id); 
		userCount--;
	}
	
	public synchronized String getRooms() {
		StringBuffer room = new StringBuffer();
		String rooms;
		Integer roomNum;
		Iterator<Integer> enu = roomHash.keySet().iterator();
		while(enu.hasNext()) {
			roomNum = (Integer) enu.next();
			ChatRoom tempRoom = (ChatRoom) roomHash.get(roomNum);
			room.append(String.valueOf(roomNum));
			room.append(DELIMETER1);
			room.append(tempRoom.toString());
			room.append(DELIMETER);
		}
		try {
			rooms = new String(room);
			rooms = rooms.substring(0, rooms.length() - 1);
		}catch(StringIndexOutOfBoundsException e) {
			return "empty";
		}
		return rooms;
	}
	
	
	public synchronized String getUsers() {
		StringBuffer id = new StringBuffer();
		String ids;
		Iterator<String> enu = userList.iterator();
		while(enu.hasNext()){
			id.append(enu.next());
			id.append(DELIMETER);
		}
		try {
			ids = new String(id);
			ids = ids.substring(0, ids.length() - 1);
		}catch(StringIndexOutOfBoundsException e) {
			return "";
		}
		return ids;
	}
	
	public synchronized int addRoom(ChatRoom room) {
		if(roomCount == MAX_ROOM) return ERR_ROOMSFULL;
		
		roomList.add(room);
		roomHash.put(ChatRoom.roomNumber, room);
		roomCount++;
		return 0;
	}
	
	public String getWaitRoomInfo() {
		StringBuffer roomInfo = new StringBuffer();
		roomInfo.append(getRooms());
		roomInfo.append(SEPARATOR);
		roomInfo.append(getUsers());
		return roomInfo.toString();
	}
	
	public synchronized int joinRoom(String id, ServerThread client, int roomNumber, String password){
		Integer roomNum = roomNumber;
		ChatRoom room = (ChatRoom)roomHash.get(roomNum);
		if(room.isRocked()) {
			if(room.checkPassword(password)) {
				if(!room.addUser(id, client)) {
					return ERR_ROOMERFULL;
				}
			}else {
				return ERR_PASSWORD;
			}
		}else if(!room.addUser(id, client)) {
			return ERR_ROOMERFULL;
		}
		userList.remove(id);
		userHash.remove(id);
		
		return 0;
	}
	
	public String getRoomInfo(int roomNumber) {
		Integer roomNum = roomNumber;
		ChatRoom room = (ChatRoom) roomHash.get(roomNum);
		return room.getUsers();
	}
	
	public synchronized boolean quitRoom(String id, int roomNumber, ServerThread client){
		boolean returnValue = false;
		Integer roomNum = roomNumber;
		ChatRoom room = (ChatRoom) roomHash.get(roomNum);
		if(room.delUser(id)) {
			roomList.remove(room); 
			roomHash.remove(roomNum); 
			roomCount--;
			returnValue = true;
		}
		userList.add(id);
		userHash.put(id, client);
		return returnValue;
	}	
			
	public synchronized HashMap<String, ServerThread> getClients(int roomNumber) {
		if (roomNumber == 0) return userHash;
		
		Integer roomNum = roomNumber;
		ChatRoom room = (ChatRoom) roomHash.get(roomNum);
		return room.getClients();
	}	
}