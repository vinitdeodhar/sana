package org.moca.procedure;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.moca.Constants;
import org.w3c.dom.Node;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * BinaryUploadElement is a ProcedureElement that is created when a "BINARYFILE"
 * element is inserted into an XML procedure description. It allows a user to select
 * a binary file stored on the phone for upload. The path of where the selectable files
 * reside is set in the Moca settings dialog. By default, the most recent file modified
 * in the folder is selected. 
 * 
 * A refresh button allows the following example interaction to occur: 1) page with 
 * binaryuploadelement comes up on phone, 2) healthworker uses external ultrasound
 * which automatically stores an MPG file on the SD card, 3) with Moca still on the screen,
 * the healthworker hits the refresh file list button to automatically re-source the files 
 * on the SD card and automatically select the file that was just created.
 */
public class BinaryUploadElement extends ProcedureElement implements OnClickListener, OnItemSelectedListener {
    private Button refresh;
    private TextView tvBinary;
    private TextView result;
    private Spinner spin;
    private List<String> sdfiles = new ArrayList<String>();
    File[] filelist;
    private ArrayAdapter<String> adapter;
    private Context context;
    
    @Override
    public ElementType getType() {
        return ElementType.BINARYFILE;
    }

    protected View createView(Context c) {
        LinearLayout binaryContainer = new LinearLayout(c);
        binaryContainer.setOrientation(LinearLayout.VERTICAL);
        context = c;
               
        if(question == null) {
            question = "Load external device file:";
        }
        tvBinary = new TextView(c);
        tvBinary.setText(question);
        tvBinary.setGravity(Gravity.CENTER);
        tvBinary.setTextAppearance(c, android.R.style.TextAppearance_Medium);
        binaryContainer.addView(tvBinary, new LinearLayout.LayoutParams(-1,-1,0.1f));
        refresh = new Button(c);
        refresh.setText("Refresh file list");
        refresh.setOnClickListener(this);
        spin = new Spinner(c);
        spin.setOnItemSelectedListener(this);
        updateSdList();
        binaryContainer.addView(spin, new LinearLayout.LayoutParams(-1,-1,0.1f));
        binaryContainer.addView(refresh, new LinearLayout.LayoutParams(-1,-1,0.1f));
        result = new TextView(c);
        result.setText("Folder is empty!");
        result.setGravity(Gravity.CENTER);
        result.setTextAppearance(c, android.R.style.TextAppearance_Small);
        binaryContainer.addView(result, new LinearLayout.LayoutParams(-1,-1,0.1f));
        return binaryContainer;
    }
    
    private void updateSdList() {
    	File folder = new File(
    			PreferenceManager.getDefaultSharedPreferences(context).getString("s_binary_file_path", Constants.DEFAULT_BINARY_FILE_FOLDER));
    	// we may want to add a filename filter here if we want to restrict to certain types
    	// i.e. mpg files
    	FileFilter nofolders = new FileFilter() {
			public boolean accept(File f) {
				return !f.isDirectory();
			}
    };
    	filelist = folder.listFiles(nofolders);
    	int lastModifiedFileIndex = -1;
    	sdfiles = new ArrayList<String>();
    	if (filelist.length > 0) {
    		lastModifiedFileIndex = 0;
    	        for (int i=0; i < filelist.length; i++) {
    	            sdfiles.add(filelist[i].getName());
    	            if (filelist[i].lastModified() > filelist[lastModifiedFileIndex].lastModified()) {
    	            	lastModifiedFileIndex = i;
    	            }
    	        }
    	}
    	adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item,
                sdfiles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        
        if (lastModifiedFileIndex != -1) {
        	spin.setSelection(lastModifiedFileIndex);
        } 
        
    }
    
    public void onClick(View v) {
        if (v == refresh) {
        	updateSdList();
        	if (spin.getCount() == 0)
        		result.setText("Folder is empty!");
        } 
    }
    
    public void setAnswer(String answer) {
    	this.answer = answer;
    }
    
    /**
     * Returns the path of the selected file for upload, or empty string if no file was selected.
     */
    public String getAnswer() {
    	if(!isViewActive())
    		return answer;
    	
    	if (spin.getCount() > 0)
    		return filelist[spin.getSelectedItemPosition()].getAbsolutePath();
    	else
    		return "";
    }
    
    /**
     * Generates XML for storing or upload.
     * Here we make question equal binaryfile and the answer the filename 
     * (not path) of the selected file for upload.
     */
    public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" answer=\"" + getAnswer());
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }
    
    private BinaryUploadElement(String id, String question, String answer, String concept, String figure, String audio) {
        super(id, question, answer, concept, figure, audio);
    }
    
    /**
     * Create a BinaryUploadElement from an XML procedure definition.
     */
    public static BinaryUploadElement fromXML(String id, String question, String answer, String concept, String figure, String audio, Node node) {
        return new BinaryUploadElement(id, question, answer, concept, figure, audio);
    }

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		String loadedFileResult = "\nloaded successfully";
		loadedFileResult = spin.getSelectedItem() + loadedFileResult;
    	result.setText(loadedFileResult);
		
	}

	public void onNothingSelected(AdapterView<?> arg0) {		
	}
}
