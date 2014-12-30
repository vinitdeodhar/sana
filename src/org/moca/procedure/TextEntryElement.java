package org.moca.procedure;

import org.moca.util.MocaUtil;
import org.w3c.dom.Node;

import android.content.Context;
import android.text.InputType;
import android.text.method.DialerKeyListener;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;

/**
 * TextEntryElement is a ProcedureElement that contains a question and a text box for user response.
 */
public class TextEntryElement extends ProcedureElement {
    private EditText et;
    private NumericType numericType = NumericType.NONE;
    
    private enum NumericType {
    	NONE,
    	DIALPAD,
    	INTEGER,
    	SIGNED,
    	DECIMAL
    }
    
    @Override
    public ElementType getType() {
        return ElementType.ENTRY;
    }
    
    private static KeyListener getKeyListenerForType(NumericType type) {
    	switch (type) {
    	case DIALPAD:
    		return new DialerKeyListener();
    	case INTEGER:
    		return new DigitsKeyListener();
    	case SIGNED:
    		return new DigitsKeyListener(true, false);
    	case DECIMAL:
    		return new DigitsKeyListener(true, true);
    	case NONE:
    	default:
    		return null;
    	}
    }
   
    public static final int TYPE_TEXT_FLAG_NO_SUGGESTIONS = 0x00080000;

    @Override
    protected View createView(Context c) {
        et = new EditText(c);
        et.setText(answer);
        et.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
        if (!NumericType.NONE.equals(numericType)) {
        	KeyListener listener = getKeyListenerForType(numericType);
        	if (listener != null)
        		et.setKeyListener(listener);
        } else {
        	
        	et.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }
        	
        return encapsulateQuestion(c, et);
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
    
    private TextEntryElement(String id, String question, String answer, String concept, String figure, String audio, NumericType numericType) {
        super(id, question, answer, concept, figure, audio);
        this.numericType = numericType;
    }
    
    /**
     * Create a TextEntryElement from an XML procedure definition.
     */
    public static TextEntryElement fromXML(String id, String question, String answer, String concept, String figure, String audio, Node n) throws ProcedureParseException {
    	String numericStr = MocaUtil.getNodeAttributeOrDefault(n, "numeric", "NONE");
    	
    	NumericType numericType = NumericType.NONE;
    	try {
    		numericType = NumericType.valueOf(numericStr);
    	} catch (Exception e) {
    		Log.e(TAG, "Could not parse numeric type: " + e.toString());
    		e.printStackTrace();
    	}
    	
        return new TextEntryElement(id, question, answer, concept, figure, audio, numericType);
    }

}
