package org.moca.db;

import org.moca.db.MocaDB.ProcedureSQLFormat;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ProcedureDAO {
	public static String getXMLForProcedure(Context context, Uri procedure) {
		Cursor cursor = null;
		String procedureXml = "";
		try {
			cursor = context.getContentResolver().query(procedure, new String [] { ProcedureSQLFormat.PROCEDURE }, null, null, null);        
			cursor.moveToFirst();
			procedureXml = cursor.getString(cursor.getColumnIndex(ProcedureSQLFormat.PROCEDURE));
			cursor.deactivate();
		} catch (Exception e) {
			EventDAO.logException(context, e);
		} finally {
			if (cursor != null)
				cursor.deactivate();
		}
		
		return procedureXml;
	}
}
