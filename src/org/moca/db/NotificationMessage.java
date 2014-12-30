package org.moca.db;

import java.util.HashMap;
import java.util.Map;

public class NotificationMessage {
	public NotificationMessage() {
		receivedMessages = 0;
		totalMessages = 0;
		messages = new HashMap<Integer,String>();
	}
	public int receivedMessages;
	public int totalMessages;
	public Map<Integer, String> messages; 
}
