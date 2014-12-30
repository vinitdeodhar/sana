package org.moca.procedure;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Node;

import android.content.Context;
import android.view.View;
import android.widget.DatePicker;

public class DateElement extends ProcedureElement {

	DatePicker dp = null;
	Date dateAnswer = new Date();
	
	@Override
	public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" question=\"" + question);
        sb.append("\" answer=\"" + getAnswer());
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }

	@Override
	protected View createView(Context c) {
		dp = new DatePicker(c);
		if (dateAnswer != null) {
			dp.init(dateAnswer.getYear() + 1900, dateAnswer.getMonth(), dateAnswer.getDate(), null);
		}
		return encapsulateQuestion(c, dp);
	}

	@Override
	public String getAnswer() {
		 if(!isViewActive())
			 return answer;
		 else {
			 dateAnswer = new Date(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
			 SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			 return sdf.format(dateAnswer);
		 }
	}

	@Override
	public ElementType getType() {
		return ElementType.DATE;
	}

	@Override
	public void setAnswer(String answer) {
		dateAnswer = new Date(answer);
		if (isViewActive()) {
			dp.updateDate(dateAnswer.getYear(), dateAnswer.getMonth(), dateAnswer.getDay());
		}
	}
	
	private DateElement(String id, String question, String answer, String concept, String figure, String audio) {
        super(id, question, answer, concept, figure, audio);
    }
    
	
	public static DateElement fromXML(String id, String question, String answer, String concept, String figure, String audio, Node n) throws ProcedureParseException {
		return new DateElement(id, question, answer, concept, figure, audio);
    }

}
