package com.example;

import java.util.*;


class ChatRoom{
	private static final String DELIMETER = ".";
	private static final String DELIMETER1 = "=";
	public static int roomNumber = 0;
	private ArrayList<String> userList;
	private HashMap<String, ServerThread> userHash;
	private String roomName;
	private int roomMaxUser;
	private int roomUser;
	private boolean isRock;
	private String password;
	private String admin;
	
	public ChatRoom(String roomName, int roomMaxUser, boolean isRock, String password, String admin) {
		roomNumber++;
		this.roomName = roomName;
		this.roomMaxUser = roomMaxUser;
		this.roomUser = 0;
		this.isRock = isRock;
		this.password = password;
		this.admin = admin;
		this.userList = new ArrayList<>(roomMaxUser);
		this.userHash = new HashMap<>(roomMaxUser);
	}
	
	public boolean addUser(String id, ServerThread client) {
		if (roomUser == roomMaxUser){
			return false;
		}
		userList.add(id);
		userHash.put(id, client);
		roomUser++;
		return true;
	}
	
	public boolean checkPassword(String passwd) {
		return password.equals(passwd);
	}
	
	public boolean checkUserIDs(String id) {
		Iterator<String> ids = userList.iterator();
		while(ids.hasNext()) {
			String tempId = (String) ids.next();
			if (tempId.equals(id)) return true;
		}
		return false;
	}
	
	public boolean isRocked() {
		return isRock;
	}
	
	public boolean delUser(String id) {
		userList.remove(id);
		userHash.remove(id);
		roomUser--;
		return userList.isEmpty();
	}
	
	public synchronized String getUsers() {
		StringBuffer id = new StringBuffer();
		String ids;
		Iterator<String> enu = userList.iterator();
		while(enu.hasNext()) {
			id.append(enu.next());
			id.append(DELIMETER);
		}
		try {
			ids = new String(id);
			ids = ids.substring(0, ids.length()-1);
		}catch(StringIndexOutOfBoundsException e) {
			return "";
		}
		return ids;
	}
	
	public ServerThread getUser(String id) {
		ServerThread client = null;
		client = (ServerThread) userHash.get(id);
		return client;
	}
	
	public HashMap<String, ServerThread> getClients() {
		return userHash;
	}
	
	public String toString() {
		StringBuffer room = new StringBuffer();
		room.append(roomName);
		room.append(DELIMETER1);
		room.append(String.valueOf(roomUser));
		room.append(DELIMETER1);
		room.append(String.valueOf(roomMaxUser));
		room.append(DELIMETER1);
		if(isRock) {
			room.append("비공개");
		}else {
			room.append("공개");
		}
		room.append(DELIMETER1);
		room.append(admin);
		return room.toString();
	}
	
	public static synchronized int getRoomNumber() {
		return roomNumber;
	}
}
