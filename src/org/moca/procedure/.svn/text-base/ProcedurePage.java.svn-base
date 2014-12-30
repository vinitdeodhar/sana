package org.moca.procedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moca.db.PatientValidator;
import org.moca.procedure.ProcedureElement.ElementType;
import org.moca.procedure.branching.Criteria;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * ProcedurePage the the object corresponding to a single "page" in a Moca procedure.
 * Each ProcedurePage can contain several elements, although the Moca style recommendation
 * is to use just one element per page. Each page is defined as an XML node in a procedure 
 * description.
 * 
 * ProcedurePages may have criteria that must be true in order for the procedure runner to 
 * display the page. This is stored in the ProcedurePage object. See documentation on Criteria 
 * for more info about how Criteria work.
 */
public class ProcedurePage {
	public static final String TAG = "ProcedurePage"; 

	private View cachedView;
	private Context cachedContext;

	List<ProcedureElement> elements;
	
	Procedure procedure;
	Criteria criteria;

	/**
	 * Constructor for ProcedurePage if no entry criteria are desired for the
	 * page (the page will always display).
	 */
	public ProcedurePage(List<ProcedureElement> elements) {
		this.elements = elements;
		this.criteria = new Criteria();
	}

	public void listElements() {
		Log.i(TAG, "listing all element types on this page");
		for (int i=0; i<elements.size(); i++) {
			Log.i(TAG, elements.get(i).getId());
		}
	}
	
	void clearCachedView() {
		for (ProcedureElement pe : elements) {
			pe.clearCachedView();
		}
	}
	
	/**
	 * Standard constructor for ProcedurePage.
	 */
	public ProcedurePage(List<ProcedureElement> elements, Criteria criteria) {
		this.elements = elements;
		this.criteria = criteria;
	}

	public String getElementValue(String key) {

		String value = "";
		List<ProcedureElement> els = getSpecialElements();
		for (int i=0; i<els.size(); i++) {
			if (els.get(i).getId().equals(key)) {
				value = els.get(i).getAnswer();
			}
		}

		return value;

	}
	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
		for(ProcedureElement pe : elements) {
			pe.setProcedure(procedure);
		}
	}


	// tests whether page has special elements that need further action

	private String[] specialElements = {"patientId", 
			"patientFirstName", "patientLastName", "patientBirthdateDay", "patientBirthdateMonth", "patientBirthdateYear", "patientGender"};

	public boolean hasSpecialElement() {
		for (int i=0; i<elements.size(); i++) {
			for (int j=0; j<specialElements.length; j++) {
				if (elements.get(i).id.equals(specialElements[j])) {
					return true;
				}
			}
		}
		return false;
	}
	
	public PatientIdElement getPatientIdElement() {
		PatientIdElement patientid = null;
		for (int i=0; i<elements.size(); i++) {
			if (elements.get(i).getType().equals(ElementType.PATIENT_ID)) {
				patientid = (PatientIdElement)elements.get(i);
				break;
			}
		}
		return patientid;
	}
	
	/**
	 * Returns a list of all the special elements on the page. TODO rename to getElementById
	 */

	public ProcedureElement getElementByType(String type) {
		ProcedureElement p = null;
		List<ProcedureElement> els = elements;
		for (int i=0; i<els.size(); i++) {
			if (els.get(i).getId().toString().equals(type)) {
				p = els.get(i);
			}
		}
		return p;
	}

	public List<ProcedureElement> getSpecialElements() {
		List<ProcedureElement> els = new ArrayList<ProcedureElement>();
		for (ProcedureElement el : elements) {
			els.add(el);
		}
		return els;
	}
	
	public void setElementValue(String key, String value) {
		for (int i=0; i<elements.size(); i++) {
			ProcedureElement e = elements.get(i);
			if (e.getType().equals(key)) {
				e.setAnswer(value);
			}
		}
	}

	public boolean isSpecialElement(ProcedureElement e) {
		for (int i=0; i<specialElements.length; i++) {
			if (e.id.equals(specialElements[i])) {
				return true;
			}
		}
		return false;
	}
	
	public boolean validate() throws ValidationError {
		
		for (ProcedureElement el : elements) {
			if (!el.validate()) {
				return false;
			}
		}
		
		if (!PatientValidator.validate(procedure, procedure.getPatientInfo())) {
			return false;
		}
		
		return true;
	}

	/**
	 * Tests whether the criteria are currently met to display this page, given
	 * user selections thus far.
	 */
	public boolean shouldDisplay() {
		return criteria.criteriaMet();
	}

	public void populateElements(HashMap<String, ProcedureElement> elementMap) {
		for (ProcedureElement e : elements) {
			elementMap.put(e.getId(), e);
		}
	}

	public HashMap<String, ProcedureElement> getElementMap() {
		HashMap<String, ProcedureElement> ret = new HashMap<String, ProcedureElement>();
		populateElements(ret);
		return ret;
	}
	
	public void playFirstPrompt() {
		for (ProcedureElement e : elements) { 
			if (e.hasAudioPrompt()) {
				e.playAudioPrompt();
				return;
			}
		}
	}

	public String getSummary() {
		if(!elements.isEmpty())
			return elements.get(0).getQuestion();
		return "";
	}

	public View toView(Context c) {
		if(cachedView == null || cachedContext != c) {
			cachedContext = c;
			cachedView = createView(c);
		}
		return cachedView;        
	}


	private View createView(Context c) {
		// ll contains scroll contains ill
		ScrollView scroll = new ScrollView(c);
		LinearLayout ll = new LinearLayout(c);
		LinearLayout ill = new LinearLayout(c);

		ll.setOrientation(LinearLayout.VERTICAL);
		ill.setOrientation(LinearLayout.VERTICAL);

		float weight = 1.0f / elements.size();
		for (ProcedureElement e : elements) {
			View v = e.toView(c);
			LinearLayout subll = new LinearLayout(c);
			// subll.addView(v, new LinearLayout.LayoutParams(-1,-1));
			subll.addView(v);
			subll.setGravity(Gravity.CENTER);
			ill.addView(subll, new LinearLayout.LayoutParams(-1, -1, weight));
		}
		ill.setWeightSum(1.0f);
		scroll.addView(ill);

		ll.addView(scroll);
		ll.setGravity(Gravity.CENTER);
		return ll;
	}

	/**
	 * Creates an XML description of the page and its elements.
	 */
	public String toXML() {
		StringBuilder sb = new StringBuilder();
		buildXML(sb);
		return sb.toString();
	}

	public void buildXML(StringBuilder sb) {
		Log.i(TAG, "ProcedurePage.toXML()");
		sb.append("<Page>\n");

		for (ProcedureElement e : elements) {
			e.buildXML(sb);
		}

		sb.append("</Page>\n");
	}

	public void restoreAnswers(Map<String,String> answersMap) {
		for(ProcedureElement s : elements) {
			if(answersMap.containsKey(s.getId())) {
				Log.i(TAG, "restoreAnswers : " + s.getId() + " " + answersMap.get(s.getId()));
				s.setAnswer(answersMap.get(s.getId()));
			}
		}
	}

	public Map<String,String> toAnswers() {
		HashMap<String,String> answers = new HashMap<String,String>();
		populateAnswers(answers);
		return answers;
	}

	public void populateAnswers(Map<String,String> answers) {
		for(ProcedureElement s : elements) {
			answers.put(s.getId(), s.getAnswer());
		}
	}

	public Map<String,Map<String,String>> toElementMap() {
		HashMap<String,Map<String,String>> elementMap = new HashMap<String,Map<String,String>>();
		populateElementMap(elementMap);
		return elementMap;
	}

	public void populateElementMap(Map<String,Map<String,String>> elementMap) {
		for(ProcedureElement s : elements) {
			Map<String,String> submap = new HashMap<String,String>();
			submap.put("question", s.getQuestion());
			submap.put("answer", s.getAnswer());
			submap.put("type", s.getType().toString());
			submap.put("concept", s.getConcept());
			elementMap.put(s.getId(), submap);
		}
	}

	/**
	 * Create a ProcedurePage from a node in an XML procedure description.
	 */
	public static ProcedurePage fromXML(Node node,
			HashMap<String, ProcedureElement> elts) throws ProcedureParseException {
		//Log.i(TAG, "ProcedurePage.fromXML(" + node.toString() + ")");
		if (!node.getNodeName().equals("Page")) {
			throw new ProcedureParseException("ProcedurePage got NodeName "
					+ node.getNodeName());
		}
		List<ProcedureElement> elements = new ArrayList<ProcedureElement>();
		Criteria criteria = new Criteria();
		NodeList children = node.getChildNodes();
		boolean showIfAlreadyExists = false;
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeName().equals("Element")) {
				elements.add(ProcedureElement.createElementfromXML(child));
			} else if (child.getNodeName().equals("ShowIf")) {
				//Log.i(TAG, "Page has ShowIf - creating Criteria");
				if (showIfAlreadyExists)
					throw new ProcedureParseException(
					"More than one ShowIf statement!");
				criteria = Criteria.fromXML(child, elts);
				showIfAlreadyExists = true;
			}
		}
		ProcedurePage pp = new ProcedurePage(elements, criteria);
		return pp;
	}

}
