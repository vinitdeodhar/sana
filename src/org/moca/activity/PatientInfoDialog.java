package org.moca.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;

public class PatientInfoDialog extends Activity {

	
	private static String errormessage;
	
     @Override
     public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);

          Builder builder = new AlertDialog.Builder(this);
          builder.setTitle("Patient information does not match database");
          builder.setMessage(errormessage);
          builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				setResult(RESULT_OK);
				dialog.cancel();
			}
          });
          builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				setResult(RESULT_CANCELED);
				dialog.cancel();
			}
          });
          builder.show();

     }
     
     public void setErrorMessage(String msg) {
    	 
    	 errormessage = msg;
    	 
     }
} 