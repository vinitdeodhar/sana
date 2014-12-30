package org.moca.procedure;

import java.util.ArrayList;
import java.util.List;

import org.moca.util.MocaUtil;
import org.w3c.dom.Node;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * SelectElement is a ProcedureElement that creates a question and roller-type selection
 * drop box so that a user can answer the question. This element type is well suited for 
 * questions that may have one response out of many possible responses.
 */
public class SelectElement extends ProcedureElement {
    private Spinner spin;
    private List<String> choicelist;
    private String[] choices;
    private ArrayAdapter<String> adapter;
    
    @Override
    public ElementType getType() {
        return ElementType.SELECT;
    }

    @Override
    protected View createView(Context c) {
        spin = new Spinner(c);
    
        if(choices == null) 
            choicelist = new ArrayList<String>();
        else
            choicelist = java.util.Arrays.asList(choices);

        adapter = new ArrayAdapter<String>(c,
                android.R.layout.simple_spinner_item,
                choicelist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        int selected =  choicelist.indexOf(answer);
        if(selected != -1)
            spin.setSelection(selected);
        return encapsulateQuestion(c, spin);
    }
    
    /**
     * Set the spinner drop box selected answer to match <param>answer</param>, 
     * if it exists.
     */
    public void setAnswer(String answer) {
    	if(!isViewActive()) {
    		this.answer = answer;
    	} else {
    		this.answer = answer;
    		// TODO: Fix this so that the adapter has the correct selected item.
    		int index = choicelist.indexOf(answer);
    		spin.setSelection(index);
    		spin.refreshDrawableState();
    	}
    }

    /**
     * Return the selected answer as a string.
     */
    public String getAnswer() {
        if(!isViewActive())
            return answer;
        return adapter.getItem(spin.getSelectedItemPosition()).toString(); 
    }
    
    /**
     * Make question and response into an XML string for storing or transmission.
     */
    public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" question=\"" + question);
        sb.append("\" choices=\"" + TextUtils.join(",", choices));
        sb.append("\" answer=\"" + getAnswer()); 
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }
    
    private SelectElement(String id, String question, String answer, String concept, String figure, String audio, String[] choices) {
        super(id,question,answer, concept, figure, audio);
        this.choices = choices;
    }
    
    /**
     * Create a SelectElement from an XML procedure definition.
     */
    public static SelectElement fromXML(String id, String question, String answer, String concept, String figure, String audio, Node node) {
        String choicesStr = MocaUtil.getNodeAttributeOrDefault(node, "choices", "");
        return new SelectElement(id, question, answer, concept, figure, audio, choicesStr.split(","));
    }

}
