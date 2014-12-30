package org.moca.procedure;

import java.util.ArrayList;
import java.util.List;

import org.moca.util.MocaUtil;
import org.w3c.dom.Node;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

/**
 * RadioElement is a ProcedureElement that can display a question along with multiple-choice 
 * radio box answers. Unlike a MultiSelectElement, only one answer can be selected at a time.
 */
public class RadioElement extends ProcedureElement {
    private List<String> choicelist;
    private String[] choices;
    ArrayList<RadioButton> rblist;
    
    @Override
    public ElementType getType() {
        return ElementType.RADIO;
    }

    @Override
    protected View createView(Context c) {
        ScrollView radioView = new ScrollView(c);
        RadioGroup rg = new RadioGroup(c);
        rg.setOrientation(LinearLayout.VERTICAL);
        choicelist = java.util.Arrays.asList(choices);
        rblist = new ArrayList<RadioButton>();

        if(answer == null)
        	answer = "";
        RadioButton checked = null;
        for(Object choice : choicelist) {
            
            RadioButton rb = new RadioButton(c);
            rb.setText((String)choice);
            rg.addView(rb);
            if(answer.equals(choice)) {
                checked = rb;
            }
            rblist.add(rb);
        }
        if(checked != null)
            checked.setChecked(true);
        radioView.addView(rg, new ViewGroup.LayoutParams(-1,-1));
        return encapsulateQuestion(c, radioView);
    }
    
    /**
     * Set the selected radio button to be the one containing <param>answer</param>.
     */
    public void setAnswer(String answer) {
    	if(!isViewActive()) {
    		this.answer = answer;
    	} else { 
	    	for(RadioButton r : rblist) {
	    		if(r.getText().toString().equals(answer))
	    			r.setChecked(true);
	    		else
	    			r.setChecked(false);
	    	}
    	}
    }
    
    /**
     * Generate a string contained the user selection (the answer to the question).
     */
    public String getAnswer() {
        if(!isViewActive())
            return answer;
        String s = "";
        for (RadioButton r : rblist) {
            if (r.isChecked())
                s += r.getText().toString();
        }
        return s;
    }
    
    /**
     * Make question and selected response into an XML string for storing or transmission.
     */
    public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" question=\"" + question);
        sb.append("\" choices=\"" + TextUtils.join(",", choices));
        sb.append("\" answer=\"" + getAnswer());
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }

    private RadioElement(String id, String question, String answer, String concept, String figure, String audio, String[] choices) {
        super(id,question,answer, concept, figure, audio);
        this.choices = choices;
    }
    
    /**
     * Create a RadioElement from an XML procedure definition.
     */
    public static RadioElement fromXML(String id, String question, String answer, String concept, String figure, String audio, Node node) {
        String choicesStr = MocaUtil.getNodeAttributeOrDefault(node, "choices", "");
        return new RadioElement(id, question, answer, concept, figure, audio, choicesStr.split(","));
    }
    
}
