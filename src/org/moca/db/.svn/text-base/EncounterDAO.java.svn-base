package org.moca.db;

import org.moca.db.MocaDB.SavedProcedureSQLFormat;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class EncounterDAO {
	public static String getEncounterGuid(Context context, Uri encounterUri) {
		Cursor cursor = null;
		String guid = "";
		try {
			cursor = context.getContentResolver().query(encounterUri, new String [] { SavedProcedureSQLFormat.GUID }, null, null, null);
			if (cursor.moveToFirst()) {
				guid = cursor.getString(cursor.getColumnIndex(SavedProcedureSQLFormat.GUID));
			}
		} catch (Exception e) {
			e.printStackTrace();
			EventDAO.logException(context, e);
		} finally {
			if (cursor != null)
				cursor.deactivate();
		}
		
		return guid;
	}
}
