package org.moca.task;

import java.io.File;

import android.content.Context;
import android.content.Intent;

public class ImageProcessingTaskRequest {
	public Context c;
	public Intent intent;
	public File tempImageFile;
	public String savedProcedureId, elementId;
}
