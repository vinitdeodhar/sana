package org.moca.procedure;

import org.moca.media.AudioPlayer;
import org.moca.util.MocaUtil;
import org.w3c.dom.Node;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A ProcedureElement is an item that can be placed on a page in a Moca procedure.
 * Typically there will only be one ProcedureElement per page, but this style suggestion
 * is not enforced, and users can make XML procedure definitions that contain several 
 * ProcedureElements per page. 
 * 
 * A ProcedureElement, generally speaking, asks a question and may allow for an answer.
 * For example, a RadioElement poses a question and allows a user to chose among response 
 * buttons.
 * 
 * ProcedureElements are defined in XML and dynamically created from the XML in Moca.
 */
public abstract class ProcedureElement {
    public static String TAG = ProcedureElement.class.toString();
    
    public static enum ElementType {
    	TEXT(""),
        ENTRY(""), 
        SELECT(""), 
        PATIENT_ID(""),
        MULTI_SELECT(""), 
        RADIO(""), 
        PICTURE("image.jpg"), 
        SOUND("sound.3gp"), 
        BINARYFILE("binary.bin"), 
        INVALID(""), 
        GPS(""),
        DATE("");
    	
        private String filename;
        private ElementType(String filename) {
        	this.filename = filename;
        }
        
        /**
         * Returns the default filename for a given ElementType
         * @return
         */
        public String getFilename() {
        	return filename;
        }
    }

    protected String id;
    protected String question;
    protected String answer;
    protected String concept;
    
    // Resource of a corresponding figure for this element.
    protected String figure;
    // Resource of a corresponding audio prompt for this element.
    protected String audioPrompt;
    
    private Procedure procedure;
    
    private Context cachedContext;
    private View cachedView;
    private AudioPlayer mAudioPlayer;
    private boolean bRequired = false;
    private String helpText;
    
    protected abstract View createView(Context c);
    
    void clearCachedView() {
    	cachedView = null;
    }
    
    protected ProcedureElement(String id, String question, String answer, String concept, String figure, String audioPrompt) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.concept = concept;
        this.figure = figure;
        this.audioPrompt = audioPrompt;
    }
    
    protected Procedure getProcedure() {
        return procedure;
    }
    
    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }
    
    protected boolean isViewActive() {
        return !(cachedView == null);
    }
    
    protected Context getContext() {
        return cachedContext;
    }
    
    public View toView(Context c) {
        if(cachedView == null || cachedContext != c) {
            cachedView = createView(c);
            cachedContext = c;
        }
        return cachedView;
    }
    
    public abstract ElementType getType(); 
    
    /** 
     * Returns the value(s) a user has selected in the element as a String.
     * Depending on the type of element this is, different formats may apply.
     * 
     * The only strange return formatting is for a MULTI_SELECT element. In this
     * case, the selected values are returned as a single string, delimited by TOKEN_DELIMITER.
     */
    public abstract String getAnswer(); 
    
    /** 
     * Set the value of the ProcedureElement's answer. This may involve interacting with previously made Views
     * if they exist.
     */
    public abstract void setAnswer(String answer);
    
    
    public boolean isRequired() {
    	return bRequired;
    }
    
    public void setRequired(boolean required) {
    	this.bRequired = required;
    }
    
    public String getHelpText() {
    	return helpText;
    }
    
    public void setHelpText(String helpText) {
    	this.helpText = helpText;
    }
    
    public boolean validate() throws ValidationError {
    	if (bRequired && "".equals(getAnswer().trim())) {
    		throw new ValidationError(helpText);
    	}
    	return true;
    }
    
    /**
     * Tell the element's widget to refresh itself. 
     */
    public void refreshWidget() {
    
    }
    
    public abstract void buildXML(StringBuilder sb);

    /**
	 * Build the XML representation of this ProcedureElement. Should only use
	 * this if you intend to use only the XML for this element. If you are
	 * building the XML for this Procedure, then prefer buildXML with a
	 * StringBuilder since String operations are slow.
	 */
    public String toXML() {
    	StringBuilder sb = new StringBuilder();
    	buildXML(sb);
    	return sb.toString();
    }
    
    /**
     * Create an element from an XML element node of a procedure definition.
     */
    public static ProcedureElement createElementfromXML(Node node) throws ProcedureParseException {
        //Log.i(TAG, "fromXML(" + node.getNodeName() + ")");
        
        if(!node.getNodeName().equals("Element")) {
            throw new ProcedureParseException("Element got NodeName " + node.getNodeName());
        }

        String questionStr = MocaUtil.getNodeAttributeOrDefault(node, "question", "");
        String answerStr = MocaUtil.getNodeAttributeOrDefault(node, "answer", null);
        String typeStr = MocaUtil.getNodeAttributeOrDefault(node, "type", "INVALID");
        String conceptStr = MocaUtil.getNodeAttributeOrDefault(node, "concept", "");
        String idStr = MocaUtil.getNodeAttributeOrFail(node, "id", new ProcedureParseException("Element doesn't have id number"));
        String figureStr = MocaUtil.getNodeAttributeOrDefault(node, "figure", "");
        String audioStr = MocaUtil.getNodeAttributeOrDefault(node, "audio", "");
        
        ElementType etype = ElementType.valueOf(typeStr);

        ProcedureElement el = null;
        switch(etype) {
        case TEXT:
            el = TextElement.fromXML(idStr, questionStr, answerStr, conceptStr, figureStr, audioStr, node);
            break;
        case ENTRY:
            el = TextEntryElement.fromXML(idStr, questionStr, answerStr, conceptStr, figureStr, audioStr, node);
            break;
        case SELECT:
            el = SelectElement.fromXML(idStr, questionStr, answerStr, conceptStr, figureStr, audioStr, node);
            break;
        case MULTI_SELECT:
            el = MultiSelectElement.fromXML(idStr, questionStr, answerStr, conceptStr, figureStr, audioStr, node);
            break;
        case RADIO:
            el = RadioElement.fromXML(idStr, questionStr, answerStr, conceptStr, figureStr, audioStr, node);
            break;
        case PICTURE:
            el = PictureElement.fromXML(idStr, questionStr, answerStr, conceptStr, figureStr, audioStr, node);
            break;
        case SOUND:
            el = SoundElement.fromXML(idStr, questionStr, answerStr, conceptStr, figureStr, audioStr, node);
            break;
        case GPS:
        	el = GpsElement.fromXML(idStr, questionStr, answerStr, conceptStr, figureStr, audioStr, node);
        	break;
        case BINARYFILE:
            el = BinaryUploadElement.fromXML(idStr, questionStr, answerStr, conceptStr, figureStr, audioStr, node);
            break;
        case PATIENT_ID:
        	el = PatientIdElement.fromXML(idStr, questionStr, answerStr, conceptStr, figureStr, audioStr, node);
        	break;
        case DATE:
        	el = DateElement.fromXML(idStr, questionStr, answerStr, conceptStr, figureStr, audioStr, node);
        	break;
        case INVALID:
        default:
            throw new ProcedureParseException("Got invalid node type : " + etype);
        }
        
        if (el == null) {
        	throw new ProcedureParseException("Failed to parse node with id " + idStr);
        }
        
        String helpStr = MocaUtil.getNodeAttributeOrDefault(node, "helpText", "");
        el.setHelpText(helpStr);
        
        String requiredStr = MocaUtil.getNodeAttributeOrDefault(node, "required", "false");
        if ("true".equals(requiredStr)) {
        	el.setRequired(true);
        } else if ("false".equals(requiredStr)) {
        	el.setRequired(false);
        } else {
        	throw new ProcedureParseException("Argument to \'required\' attribute invalid for id " + idStr + ". Must be \'true\' or \'false\'");
        }
        	

        
        return el;
    }
    
    public String getId() {
        return id;
    }
    
    /**
     * @return the question string originally defined in the XML procedure definition.
     */
    public String getQuestion() {
        return question;
    }
    
    /**
     * @return the medical concept associated with this ProcedureElement
     */
    public String getConcept() {
    	return concept;
    }
    
    /**
     * @return the figure URL associated with this ProcedureElement
     */
    public String getFigure() {
    	return figure;
    }
    
    boolean hasAudioPrompt() {
    	return !"".equals(audioPrompt);
    }
    
    void playAudioPrompt() {
    	if (mAudioPlayer != null)
    		mAudioPlayer.play();
    }
    
    public String getAudioPrompt() {
    	return audioPrompt;
    }
    
    public View encapsulateQuestion(Context c, View v) {
    	TextView textView = new TextView(c);
    	textView.setText(this.question);
    	textView.setGravity(Gravity.CENTER_HORIZONTAL);
    	textView.setTextAppearance(c, android.R.style.TextAppearance_Large);
        
    	View questionView = textView;
    	
        ImageView imageView = null;
        
        //Set accompanying figure
        
        if(!"".equals(figure)) {    
	        try{
	        	String imagePath = c.getPackageName() + ":" + figure;
	        	int resID = c.getResources().getIdentifier(imagePath, null, null);
	        	imageView = new ImageView(c);
	        	imageView.setImageResource(resID);
	        	imageView.setAdjustViewBounds(true); // set the ImageView bounds to match the Drawable's dimensions
	        	imageView.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	        	imageView.setPadding(10,10,10,10);
	        }
	        catch(Exception e){
	        	Log.e(TAG, "Couldn't find resource figure " + e.toString());
	        }
        }
        
        if (hasAudioPrompt()) {
        	try {
        		String resourcePath = c.getPackageName() + ":" + audioPrompt;
        		int resID = c.getResources().getIdentifier(resourcePath, null, null);
        		Log.i(TAG, "Looking up ID for resource: " + resourcePath + ", got " + resID);
        		
        		if (resID != 0) {
	        		mAudioPlayer = new AudioPlayer(resID);
	        		View playerView = mAudioPlayer.createView(c);
	        		playerView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	        		
	        		LinearLayout audioPromptView = new LinearLayout(c);
	                audioPromptView.setOrientation(LinearLayout.HORIZONTAL);
	                audioPromptView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	                audioPromptView.setGravity(Gravity.CENTER);
	                
	                // Insert the play button to the left of the current question view.
	                audioPromptView.addView(playerView);
	                audioPromptView.addView(questionView);
	                
	                questionView = audioPromptView;
        		}
        	} catch (Exception e) {
        		Log.e(TAG, "Couldn't find resource for audio prompt: " + e.toString());
        	}
        }
        
    	LinearLayout ll = new LinearLayout(c);
    	ll.setOrientation(LinearLayout.VERTICAL);
    	
    	LinearLayout viewHolder = new LinearLayout(c);
    	viewHolder.addView(v);
    	viewHolder.setGravity(Gravity.CENTER_HORIZONTAL);
    	
    	//Add to layout
    	ll.addView(questionView);
    	if (imageView != null)
    		ll.addView(imageView);
    	ll.addView(viewHolder);
    	ll.setGravity(Gravity.CENTER);
    	ll.setPadding(10, 0, 10, 0);
    	ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
        return ll;
    }
}
