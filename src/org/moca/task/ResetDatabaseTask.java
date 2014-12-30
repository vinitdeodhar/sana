package org.moca.task;

import org.moca.util.MocaUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class ResetDatabaseTask extends AsyncTask<Context, Void, Integer> {
	private static final String TAG = ResetDatabaseTask.class.toString();
	
	private ProgressDialog progressDialog;
	private Context mContext = null; // TODO context leak?
	
	public ResetDatabaseTask(Context c) {
		this.mContext = c;
	}
	
	@Override
	protected Integer doInBackground(Context... params) {
		Log.i(TAG, "Executing ResetDatabaseTask");
		Context c = params[0];
		MocaUtil.clearDatabase(c);
		MocaUtil.loadDefaultDatabase(c);		
		return 0;
	}
	
	@Override
	protected void onPreExecute() {
		Log.i(TAG, "About to execute ResetDatabaseTask");
		if (progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
    	progressDialog = new ProgressDialog(mContext);
    	progressDialog.setMessage("Clearing Database"); // TODO i18n
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	progressDialog.show();
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		Log.i(TAG, "Completed ResetDatabaseTask");
		if (progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
	}

}
