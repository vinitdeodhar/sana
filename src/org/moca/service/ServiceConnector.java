package org.moca.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class ServiceConnector {
	private static final String TAG = ServiceConnector.class.toString();
	
	ServiceListener<BackgroundUploader> mListener = null;
	private BackgroundUploader mUploadService = null;
    
    private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name,
				IBinder service) {
			Log.i(TAG, "onServiceConnected");
			mUploadService = ((BackgroundUploader.LocalBinder)service).getService();
			if (mListener != null)
				mListener.onConnect(mUploadService);
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "onServiceDisconnected");
			if (mListener != null)
				mListener.onDisconnect(mUploadService);
			mUploadService = null;
		}
		
	};

	public void connect(Context c) {
		if (mUploadService == null) {
			Intent serviceIntent = new Intent(c, BackgroundUploader.class);
			c.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		}
	}
	
	public void disconnect(Context c) {
		if (mUploadService != null) {
			c.unbindService(serviceConnection);
		}
	}
	
	public void setServiceListener(ServiceListener<BackgroundUploader> listener) {
		this.mListener = listener;
		
		if (listener instanceof Context) {
			Log.w(TAG, "Provided ServiceListener is a Context. You may be leaking a Context.");
		}
	}
}
