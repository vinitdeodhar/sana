package org.moca.procedure;

import org.w3c.dom.Node;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

/**
 * TextElement is an answer-less ProcedureElement that represents a block of text
 * on a procedure page.
 */
public class TextElement extends ProcedureElement {
    @Override
    protected View createView(Context c) {
        TextView tv = new TextView(c);
        tv.setText(question);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setTextAppearance(c, android.R.style.TextAppearance_Large);
        return tv;
    }
    
    @Override
    public ElementType getType() {
        return ElementType.TEXT;
    }
    
    public void setAnswer(String answer) {
    	this.answer = answer;
    }
    
    public String getAnswer() {
        if(!isViewActive())
            return answer;
        return "";
    }
    
    /**
     * Make TextElement text into an XML string for storing or transmission.
     */
    public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" value=\"" + getAnswer());
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }
    
    private TextElement(String id, String question, String answer, String concept, String figure, String audio) {
        super(id, question, answer, concept, figure, audio);
    }
    
    /**
     * Create a TextElement from an XML procedure definition.
     */
    public static TextElement fromXML(String id, String question, String answer, String concept, String figure, String audio, Node node) {
        return new TextElement(id, question, answer, concept, figure, audio);
    }
}
