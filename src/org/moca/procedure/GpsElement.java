package org.moca.procedure;

import org.w3c.dom.Node;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * GpsElement is a ProcedureElement that is created when a "GPS" element is put into 
 * an XML procedure description. It allows the user to click on a button to grab the 
 * current GPS coordinates. 
 * 
 * For field workers, this is often a useful feature to track patients and where they 
 * are seen by the health workers.
 */
public class GpsElement extends ProcedureElement implements OnClickListener {
    private Button getLocationButton;
    private TextView gpsTextView;    
    private LocationManager locationManager; 
    private LocationListener locationListener;
    private boolean gotCoordinates = false;
    private Handler handler = null;
    
    protected void finalize() throws Throwable {
    	// We need to make sure that we are not leaving the GPS on.
    	if (locationManager != null && locationListener != null) {
    		locationManager.removeUpdates(locationListener);
    	}
    	
        super.finalize(); //not necessary if extending Object.
    } 
    
    @Override
    public ElementType getType() {
        return ElementType.GPS;
    }

    protected View createView(Context c) {
        LinearLayout gpsContainer = new LinearLayout(c);
        gpsContainer.setOrientation(LinearLayout.VERTICAL);
        locationManager = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE);
        
    	if (locationListener == null) {
    		locationListener = new MocaGPSListener();
    	}
        
        if(question == null) {
            question = "Please enter GPS coordinates:";
        }
        gpsTextView = new TextView(c);
        gpsTextView.setText(question);
        gpsTextView.setGravity(Gravity.CENTER);
        gpsTextView.setTextAppearance(c, android.R.style.TextAppearance_Medium);
        gpsContainer.addView(gpsTextView, new LinearLayout.LayoutParams(-1,-1,0.1f));
        getLocationButton = new Button(c);
        getLocationButton.setText("Grab GPS Location");
        getLocationButton.setOnClickListener(this);
        gpsContainer.addView(getLocationButton, new LinearLayout.LayoutParams(-1,-1,0.1f));
        return gpsContainer;
    }
    
    public void onClick(View v) {
        if (v == getLocationButton) {
        	Log.i(TAG, "Requesting GPS updates to get the current location. ");
        	gotCoordinates = false;
        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        	getLocationButton.setEnabled(false);
        	getLocationButton.setText("Waiting for GPS...");
        	Thread gpsWatchdog = new Thread() {
        		public void run() {
        			Log.i(TAG, "GPS watchdog turning off GPS.");
        			locationManager.removeUpdates(locationListener);
        			if (!gotCoordinates) {
        				Log.i(TAG, "GPS coordinates were not acquired.");
        				getLocationButton.setEnabled(true);
        	        	getLocationButton.setText("Couldn't acquire location. Try again.");
        			}
        		}
        	};
        	if (handler == null)
        		handler = new Handler();
        	handler.postDelayed(gpsWatchdog, 10000);
        	
        }
    }
    
    public void setAnswer(String answer) {
      this.answer = answer;
    }
    
    /**
     * Get the acquired GPS coordinates.
     */
    public String getAnswer() {
    	return answer;
    }
    
    /**
     * Make question and response into an XML string for storing or transmission.
     */
    public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" answer=\"" + getAnswer());
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }
    
    private GpsElement(String id, String question, String answer, String concept, String figure, String audio) {
        super(id, question, answer, concept, figure, audio);
        setAnswer("Coordinates not acquired.");
    }
    
    /**
     * Create a GpsElement from an XML procedure definition.
     */
    public static GpsElement fromXML(String id, String question, String answer, String concept, String figure, String audio, Node node) {
        return new GpsElement(id, question, answer, concept, figure, audio);
    }
    
    private class MocaGPSListener implements LocationListener {
    	public void onLocationChanged(Location location) {
            // Called when the location has changed.
			Log.d(TAG, "Got location update :" + location.toString() + ". Disabling GPS");
			getLocationButton.setEnabled(false);
			getLocationButton.setText("Coordinates acquired.");
			gotCoordinates = true;
			setAnswer("Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
			locationManager.removeUpdates(locationListener);
        }
        public void onProviderDisabled(String provider) {
            // Called when the provider is disabled by the user.
        	Log.d(TAG, "Provider disabled: " + provider);
        	getLocationButton.setEnabled(true);
			getLocationButton.setText("GPS turned off -- check settings.");
			locationManager.removeUpdates(locationListener);
        }
        public void onProviderEnabled(String provider) {
            // Called when the provider is enabled by the user.
        	Log.d(TAG, "Provider enabled: " + provider);
        	// Do nothing, we should get a location update soon which will disable the listener.
        }
        public void onStatusChanged (String provider, int status, Bundle extras) {
            // Called when the provider status changes.
        	Log.d(TAG, "Provider status changed: " + provider + " status: " + status);
        	if (status == LocationProvider.AVAILABLE) {
        		// Do nothing, we should get a location update soon which will disable the listener.
        	} else if (status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
        		getLocationButton.setEnabled(true);
    			getLocationButton.setText("GPS service not available. Try again.");
            	locationManager.removeUpdates(locationListener);
        	}
        }    	
    }
}
