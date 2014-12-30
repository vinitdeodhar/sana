package org.moca.service;

import android.app.Service;

public interface ServiceListener<T extends Service> {
	void onConnect(T service);
	void onDisconnect(T service);
}
