package org.moca.task;

import org.moca.net.MDSInterface;
import org.moca.util.MocaUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class CheckCredentialsTask extends AsyncTask<Context, Void, Integer> {
	public static final String TAG = CheckCredentialsTask.class.toString();
	public static final Integer CREDENTIALS_NO_CONNECTION = 0;
	public static final Integer CREDENTIALS_INVALID = 1;
	public static final Integer CREDENTIALS_VALID = 2;
	
	private ValidationListener validationListener = null;
	
	public void setValidationListener(ValidationListener listener) {
		this.validationListener = listener;
	}

	@Override
	protected Integer doInBackground(Context... params) {
		Log.i(TAG, "Executing CheckCredentialsTask");
		Context c = params[0];
		Integer result = CREDENTIALS_NO_CONNECTION;
		
		if (MocaUtil.checkConnection(c)) {
			try {
				boolean credentialsValid = MDSInterface.validateCredentials(c);
				result = credentialsValid ? CREDENTIALS_VALID : CREDENTIALS_INVALID;
			} catch (Exception e) {
				Log.e(TAG, "Got exception while validating credentials: " + e);
				e.printStackTrace();
				result = CREDENTIALS_NO_CONNECTION;
			}
		}
		return result;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		Log.i(TAG, "Completed CheckCredentialsTask");
		if (validationListener != null) {
			validationListener.onValidationComplete(result);
			// Free the reference to help prevent leaks.
			validationListener = null;
		}
    }
}
