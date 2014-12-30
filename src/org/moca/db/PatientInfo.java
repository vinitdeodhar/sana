package org.moca.db;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class PatientInfo implements Parcelable {
	private static final String TAG = PatientInfo.class.toString();
	
	boolean isConfirmed = false;
	private String patientIdentifier = "";
	private String patientFirstName = "";
	private String patientLastName = "";
	private String patientGender = "";
	private Date patientBirthdate = new Date();
	
	public PatientInfo() {
		Log.v(TAG, "PatientInfo()");
	}
	
	private PatientInfo(Parcel p) {
		Log.v(TAG, "PatientInfo(Parcel)");
		readFromParcel(p);
	}
	
	public String getAnswerForId(String id) {
		if ("patientGender".equals(id))
			return patientGender;
		else if ("patientFirstName".equals(id))
			return patientFirstName;
		else if ("patientLastName".equals(id))
			return patientLastName;
		else if ("patientBirthdateYear".equals(id) && patientBirthdate != null)
			return Integer.toString(patientBirthdate.getYear());
		else if ("patientBirthdateMonth".equals(id) && patientBirthdate != null)
			return Integer.toString(patientBirthdate.getMonth());
		else if ("patientBirthdateDay".equals(id) && patientBirthdate != null)
			return Integer.toString(patientBirthdate.getDay());
		return "";
	}
	
	public boolean isConfirmed() {
		return isConfirmed;
	}
	
	public void setConfirmed(boolean confirmed) {
		isConfirmed = confirmed;
	}

	
	public String getPatientIdentifier() {
		return patientIdentifier;
	}
	
	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}
	
	public String getPatientGender() {
		return patientGender;
	}
	
	public void setPatientGender(String patientGender) {
		this.patientGender = patientGender;
	}
	
	public String getPatientFirstName() {
		return patientFirstName;
	}
	
	public void setPatientFirstName(String patientFirstName) {
		this.patientFirstName = patientFirstName;
	}
	
	public String getPatientLastName() {
		return patientLastName;
	}
	
	public void setPatientLastName(String patientLastName) {
		this.patientLastName = patientLastName;
	}
	
	public Date getPatientBirthdate() {
		return patientBirthdate;
	}
	
	public void setPatientBirthdate(Date patientBirthdate) {
		this.patientBirthdate = patientBirthdate;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<PatientInfo> CREATOR = new Parcelable.Creator<PatientInfo>() {

		@Override
		public PatientInfo createFromParcel(Parcel source) {
			return new PatientInfo(source);
		}

		@Override
		public PatientInfo[] newArray(int size) {
			return new PatientInfo[size];
		}
	};
	
	private void readFromParcel(Parcel p) {
		Log.v(TAG, "readFromParcel");

		try {
			boolean[] confirmedArray = p.createBooleanArray();
			isConfirmed = confirmedArray[0];
			patientIdentifier = p.readString();
			patientFirstName = p.readString();
			patientLastName = p.readString();
			patientGender = p.readString();
			patientBirthdate = new Date(p.readString());
		} catch (Exception e) {
			Log.e(TAG, "While reading PatientInfo from Parcel, got exception: " + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Log.v(TAG, "writeToParcel");
		dest.writeBooleanArray(new boolean[] {isConfirmed});
		dest.writeString(patientIdentifier);
		dest.writeString(patientFirstName);
		dest.writeString(patientLastName);
		dest.writeString(patientGender);
		dest.writeString(patientBirthdate.toString());
	}
	
}
