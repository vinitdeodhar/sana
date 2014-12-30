package org.moca.task;

import org.moca.db.PatientInfo;
import org.moca.net.MDSInterface;
import org.moca.util.MocaUtil;
import org.moca.util.UserDatabase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PatientLookupTask extends AsyncTask<String, Void, PatientInfo> {
	private static final String TAG = PatientLookupTask.class.toString();
	
	private Context mContext;
	private PatientLookupListener listener = null;

	public PatientLookupTask(Context c) {
		mContext = c;
	}
	
	public void setPatientLookupListener(PatientLookupListener listener) {
		this.listener = listener;
	}
	
	@Override
	protected PatientInfo doInBackground(String... params) {
		String patientId = params[0];
		
		Log.i(TAG, "Looking up patient record for " + patientId);
		
		PatientInfo pi = null;
		try {
			if (MocaUtil.checkConnection(mContext)) {
				String mdsPatientInfo = MDSInterface.getUserInfo(mContext, patientId);
				pi = UserDatabase.getPatientFromMDSRecord(patientId, mdsPatientInfo);
				Log.i(TAG, "Acquired patient record from MDS");
			}
		} catch (Exception e) {
			Log.e(TAG, "Could not get patient record from MDS: " + e.toString());
			e.printStackTrace();
		}
		
		try {
			if (pi == null) {
				pi = UserDatabase.getPatientFromLocalDatabase(mContext, patientId);
				Log.i(TAG, "Acquired patient record from local Patient cache.");
			}
		} catch (Exception e) {
			Log.e(TAG, "Could not get patient record from local database: " + e.toString());
			e.printStackTrace();
		}
		
		if (pi == null) {
			pi = new PatientInfo();
			pi.setPatientIdentifier(patientId);
			pi.setConfirmed(false);
		}
		
		return pi;
	}
	
	@Override
	protected void onPostExecute(PatientInfo pi) {
		if (listener != null && pi != null) {
			if (!pi.isConfirmed()) {
				listener.onPatientLookupFailure(pi.getPatientIdentifier());
			} else {
				listener.onPatientLookupSuccess(pi);
			}
		}
	}

}
