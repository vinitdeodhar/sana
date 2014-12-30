package org.moca.procedure;

import org.moca.R;
import org.w3c.dom.Node;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.method.DialerKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 
 */
public class PatientIdElement extends ProcedureElement implements OnClickListener {
    private EditText et;
    private Button barcodeButton;

    private static final int BARCODE_INTENT_REQUEST_CODE = 2;
    
    @Override
    public ElementType getType() {
        return ElementType.PATIENT_ID;
    }

    @Override
    protected View createView(Context c) {
    	
        et = new EditText(c);
        et.setText(answer);
        et.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        et.setKeyListener(new DialerKeyListener());
        
    	LinearLayout ll = new LinearLayout(c);
    	ll.setOrientation(LinearLayout.VERTICAL);

    	ll.addView(et, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    	ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    	
    	//SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
    	boolean barcodeEnable = true; //sp.getBoolean(Constants.PREFERENCE_BARCODE_ENABLED, false);
    	
    	if (barcodeEnable) {
	    	barcodeButton = new Button(c);
	        barcodeButton.setText(c.getResources().getString(R.string.procedurerunner_scan_id));
	        barcodeButton.setOnClickListener(this);
	        barcodeButton.setGravity(Gravity.CENTER_HORIZONTAL);
	    	ll.addView(barcodeButton, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    	}

        return encapsulateQuestion(c, ll);
    }
    
    /**
     * Set the text in the text box.
     */
    public void setAnswer(String answer) {
    	this.answer = answer;
    	if(isViewActive()) {
    		et.setText(answer);
    	}
    }
    
    public void setAndRefreshAnswer(String answer) {
    	this.answer = answer;
    	if (et != null) {
    		et.setText(answer);
    		et.refreshDrawableState();
    	}
    }
    /**
     * Return the user's typed-in response.
     */
    public String getAnswer() {
        if(!isViewActive())
            return answer;
        else if(et.getText().length() == 0)
            return "";
        return et.getText().toString();
    }
    
    /**
     * Make question and response into an XML string for storing or transmission.
     */
    public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" question=\"" + question);
        sb.append("\" answer=\"" + getAnswer());
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }
    
    private PatientIdElement(String id, String question, String answer, String concept, String figure, String audio) {
        super(id, question, answer, concept, figure, audio);
    }
    
    /**
     * Create a PatientIdElement from an XML procedure definition.
     */
    public static PatientIdElement fromXML(String id, String question, String answer, String concept, String figure, String audio, Node n) {
        return new PatientIdElement(id, question, answer, concept, figure, audio);
    }
    
    
	 public void onClick(View v) {
		 if (v == barcodeButton) {
			 String procedureId = getProcedure().getInstanceUri().getPathSegments().get(1); //which procedure its part of
			 String[] params = {procedureId, id};
	    	 Intent intent = new Intent("com.google.zxing.client.android.SCAN");
	    	 try {
	    		 ((Activity)this.getContext()).startActivityForResult(intent, BARCODE_INTENT_REQUEST_CODE);
	    	 } catch (Exception e) {
	    		 Log.e(TAG, "Exception opening barcode reader, probably not installed, " + e.toString());
	    		 new AlertDialog.Builder(getContext())
	    		  .setTitle("Error")
	    	      .setMessage("Barcode reader not installed. install \"ZXing Barcode Scanner\" from the Android Market.")
	    	      .setPositiveButton("Ok", null)
	    	      .show();
	    	 }
	  
		 }
	 }
    

}
