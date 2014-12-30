package org.moca.net;

public class MDSNotification {
	// The MDS's ID for this notification.
	public String n;
	// The saved procedure GUID to which this notification refers.
	public String c;
	// The patient identifier 
	public String p;
	// This notification's count -- formatted like this: "(?P<this_message>\d+)/(?P<total_messages>\d+)". 
	public String d;
}
