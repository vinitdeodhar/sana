package org.moca.procedure;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.moca.R;
import org.moca.db.PatientInfo;
import org.moca.db.PatientValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ViewAnimator;

/**
 * A Procedure is, conceptually, a form that can be made up of a number of pages, each of which 
 * may contain several elements. Since pages may contain entry criteria (checks that allow the procedure
 * to branch if previous responses were made a certain way), the methods in the Procedure take care of 
 * checking these criteria.
 */
public class Procedure {
    public static final String TAG = Procedure.class.toString();
    
    private View cachedView;
    private Context cachedContext;
    
    private Uri instanceUri = null;
    private String title;
    private String author;
    private List<ProcedurePage> pages;
    public ListIterator<ProcedurePage> pagesIterator;
    private ProcedurePage currentPage;
    private ViewAnimator viewAnimator;
    private PatientInfo patientInfo = null;

    public Procedure(String title, String author, List<ProcedurePage> pages, HashMap<String, ProcedureElement> elements) {
        this.pages = new LinkedList<ProcedurePage>();
        //this.pages.addAll(pages);
        for(ProcedurePage pp : pages) {
            pp.setProcedure(this);
            this.pages.add(pp);
        }
        this.title = title;
        this.author = author;
        pagesIterator = pages.listIterator();
        
        next();
    }

    public void init() {
    }
    
    public void setCachedView(View v){
    	this.cachedView = v;
    }
    
    public View getCachedView(){
    	return this.cachedView;
    }
    
    public void setInstanceUri(Uri instanceUri) {
        this.instanceUri = instanceUri;
    }
    
    public Uri getInstanceUri() {
        return instanceUri;
    }

    public ProcedurePage current() {
        return currentPage;
    }
    
    public void setPatientInfo(PatientInfo pi) {
    	this.patientInfo = pi;
    }
    
    public PatientInfo getPatientInfo() {
    	return patientInfo;
    }

    /**
     * Determines whether there is a next page in the sequence.
     * It does NOT check whether that page should be viewed or not.
     */
    public boolean hasNext() {
        if(pagesIterator == null)
            return false;
        return pagesIterator.hasNext();
    }
    
    /**
     * Determines whether there is a previous page in the sequence.
     * It does NOT check whether that page should be viewed or not.
     */
    public boolean hasPrev() {
        if(pagesIterator == null)
            return false;
        if(pagesIterator.previousIndex() == 0) {
            return false;
        }
        return true;
    }

    /**
     * Advances to the next page in the sequence.
     * It does NOT check whether that page should be viewed or not given user choices.
     */
    public void next() {
        if (hasNext()) {
            currentPage = pagesIterator.next();
            if(viewAnimator != null && cachedContext != null) {
                viewAnimator.setInAnimation(cachedContext,R.anim.slide_from_right);
                viewAnimator.setOutAnimation(cachedContext,R.anim.slide_to_left);
                viewAnimator.showNext();
            }
        }
    }
    
    /**
     * Goes back to the previous page in the sequence.
     * It does NOT check whether that page should be viewed or not given user choices.
     */
    public void prev() {
        if (hasPrev()) {
            currentPage = pagesIterator.previous();
            if(viewAnimator != null && cachedContext != null) {
                viewAnimator.setInAnimation(cachedContext,R.anim.slide_from_left);
                viewAnimator.setOutAnimation(cachedContext,R.anim.slide_to_right);
                viewAnimator.showPrevious();
            }
        }
    }
    
    /**
     * Determines whether there is a next show-able page in the sequence, given
     * user selections thus far.
     */
    public boolean hasNextShowable() {
        if(pagesIterator == null)
            return false;
        if (!pagesIterator.hasNext())
            return false;
        for (int i = pagesIterator.nextIndex(); i < pages.size(); i++) {
            if (pages.get(i).shouldDisplay())
                return true;
        }
        return false;
    }
    
    /**
     * Determines whether there is a previous show-able page in the sequence, given
     * user selections thus far.
     */
    public boolean hasPrevShowable() {
        if (pagesIterator == null)
            return false;
        if (!pagesIterator.hasPrevious())
            return false;
        if (pagesIterator.previousIndex() == 0)
        	return false;
        for (int i = pagesIterator.previousIndex(); i >= 0; i--) {
            if (pages.get(i).shouldDisplay())
                return true;
        }
        return false;
    }
            
    /**
     * Advances the current page to the next show-able page in the sequence, skipping 
     * over non-show-able pages, given user selections thus far. 
     * It also updates the procedure view to advance by this same number of pages.
     */
    public void advance() {
        if (!hasNextShowable())
            return;
        ProcedurePage pp = pagesIterator.next();
        viewAnimator.showNext();
        while (hasNext() && !pp.shouldDisplay()) {
            pp = pagesIterator.next();
            viewAnimator.showNext();
        }
        currentPage = pp;
        
        // Fill in default values for data from patient in the database
		PatientValidator.populateSpecialElements(this, patientInfo);
    }
   
    /**
     * Regresses the current page to the previous show-able page in the sequence, skipping 
     * over non-show-able pages, given user selections thus far. 
     * It also updates the procedure view to regress by this same number of pages.
     */
    public void back() {
        if (!hasPrevShowable())
            return;
        ProcedurePage pp;
        // this will refer to the current page
        pagesIterator.previous();
        pp = pages.get(pagesIterator.previousIndex());
        viewAnimator.showPrevious();
        while (hasPrev() && !pp.shouldDisplay()) {
            pagesIterator.previous();
            pp = pages.get(pagesIterator.previousIndex());
            viewAnimator.showPrevious();
        }
        currentPage = pp;
    }
    
    public void jumpToPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= pages.size()) {
        	return;
        }
        pagesIterator = pages.listIterator();
        Log.i(TAG, "pageIndex value: " + pageIndex);
        while(pagesIterator.nextIndex() != pageIndex) {
            pagesIterator.next();
        }
        Log.i(TAG, "current index of page: " + getCurrentIndex());
        currentPage = pagesIterator.next();
        Log.i(TAG, "current index of page: " + getCurrentIndex());
        viewAnimator.setInAnimation(null);
        viewAnimator.setOutAnimation(null);
        viewAnimator.setDisplayedChild(pageIndex);
    }
    
    public void jumpToVisiblePage(int pageIndex) {
    	if (pageIndex < 0 || pageIndex >= pages.size())
    		return;
    	
    	pagesIterator = pages.listIterator();
    	int visibleIndex = 0;
    	int actualIndex = 0;
    	while (pagesIterator.hasNext()) {
    		ProcedurePage page = pagesIterator.next();
    		
    		if (visibleIndex == pageIndex) {
    			currentPage = page;
    	        viewAnimator.setInAnimation(null);
    	        viewAnimator.setOutAnimation(null);
    	        viewAnimator.setDisplayedChild(actualIndex);
    	        break;
    		}
    		
    		if (page.shouldDisplay()) {
    			visibleIndex++;
    		}
    		actualIndex++;
    	}
    }
    
    public int getCurrentIndex() {
        return pages.indexOf(currentPage);
    }
    
    public int getCurrentVisibleIndex() {
    	Iterator<ProcedurePage> pageIterator = pages.iterator();
    	int visibleIndex = 0;
    	while (pageIterator.hasNext()) {
    		ProcedurePage page = pageIterator.next();
    		if (page == currentPage) {
    			return visibleIndex;
    		}
    		if (page.shouldDisplay()) {
    			visibleIndex++;
    		}
    	}
    	return 0;
    }

    public int getTotalPageCount() {
        return pages.size();
    }
    
    public int getVisiblePageCount() {
    	Iterator<ProcedurePage> pageIterator = pages.iterator();
    	int visibleCount = 0;
    	while (pageIterator.hasNext()) {
    		ProcedurePage page = pageIterator.next();
    		if (page.shouldDisplay()) {
    			visibleCount++;
    		}
    	}
    	return visibleCount;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getAuthor() {
    	return author;
    }
    
    public String toXML() {
        Log.i(TAG,"Procedure.toXML()");
        StringBuilder sb = new StringBuilder();
        buildXML(sb);
        return sb.toString();
    }
    
    public void buildXML(StringBuilder sb) {
    	sb.append("<Procedure title =\"" + title + "\" author =\"" + author + "\">\n");
        
        for (ProcedurePage p : pages) {
            p.buildXML(sb);
        }
        
        sb.append("</Procedure>");
    }
    
    public Map<String, String> toAnswers() {
        HashMap<String,String> answers = new HashMap<String,String>();
        
        for(ProcedurePage pp : pages) {
        	pp.populateAnswers(answers);
        }
        
        return answers;
    }
    
    public void restoreAnswers(Map<String,String> answersMap) {
    	for (ProcedurePage pp : pages) {
    		pp.restoreAnswers(answersMap);
    	}
    }
    
    /**
     * @return a dictionary mapping Element ids to a dictionary containing the properties for each Element
     */
    public Map<String, Map<String,String>> toElementMap() {
        HashMap<String,Map<String,String>> answers = new HashMap<String,Map<String,String>>();
        
        for(ProcedurePage pp : pages) {
        	pp.populateElementMap(answers);
        }
        
        return answers;
    }
          
    private static Procedure fromXML(Node node) throws ProcedureParseException {
        
        if(!node.getNodeName().equals("Procedure")) {
            throw new ProcedureParseException("Procedure got NodeName" + node.getNodeName());
        }        
        
        List<ProcedurePage> pages = new ArrayList<ProcedurePage>();
        
        NodeList nl = node.getChildNodes();
        ProcedurePage page;
        HashMap<String, ProcedureElement> elts = new HashMap<String, ProcedureElement>();
        for(int i=0; i<nl.getLength(); i++) {
            Node child = nl.item(i);
            if(child.getNodeName().equals("Page")) {
                page = ProcedurePage.fromXML(child, elts);
                elts.putAll(page.getElementMap());
                pages.add(page);
            }
        }
        String title = "Untitled Procedure";
        Node titleNode = node.getAttributes().getNamedItem("title");
        
        if(titleNode != null) {
        	title = titleNode.getNodeValue();
            Log.i(TAG, "Loading Procedure from XML: " + title);
            
        }
        
        String author = "";
        Node authorNode = node.getAttributes().getNamedItem("author");
        if(authorNode != null) {
        	author = authorNode.getNodeValue();
            Log.i(TAG, "Author of this procedure: " + author);
            
        }
        
        Procedure procedure = new Procedure(title, author, pages, elts);
        return procedure;
    }
    
    
    public static Procedure fromRawResource(Context c, int id) throws IOException, ParserConfigurationException, SAXException, Exception {
        return fromXML(new InputSource(c.getResources().openRawResource(id)));       
    }
    
    public static Procedure fromXMLString(String xml) throws IOException, ParserConfigurationException, SAXException, ProcedureParseException {
    	return fromXML(new InputSource(new StringReader(xml)));
    }
    
    public static Procedure fromXML(InputSource xml) throws IOException, ParserConfigurationException, SAXException, ProcedureParseException {
    	
    	long processingTime = System.currentTimeMillis();
    	
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setNamespaceAware(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document d = db.parse(xml);
        
        NodeList children = d.getChildNodes();
        Node procedureNode = null;
        for(int i=0; i<children.getLength(); i++) {
            Node child = d.getChildNodes().item(i);
            if(child.getNodeName().equals("Procedure")) {
                procedureNode = child;
                break;
            }
        }
        if(procedureNode == null) {
            throw new ProcedureParseException("Can't get procedure");
        }
        Procedure result = fromXML(procedureNode);
        
        processingTime = System.currentTimeMillis() - processingTime;
        Log.i(TAG, "Parsing procedure XML took " + processingTime + " milliseconds.");
        
        return result;
    }
    
    private View createView(Context c) {
        viewAnimator = new ViewAnimator(c);
        //viewAnimator.setInAnimation(AnimationUtils.loadAnimation(c,R.anim.slide_from_right));
        //viewAnimator.setOutAnimation(AnimationUtils.loadAnimation(c,R.anim.slide_to_left));

        for(ProcedurePage page : pages) {
            viewAnimator.addView(page.toView(c));
        }

        return viewAnimator;
    }
    
    public void clearCachedViews() {
    	cachedView = null;
    	cachedContext = null;
    	
    	for (ProcedurePage pp : pages) {
    		pp.clearCachedView();
    	}
    }
    
    public View toView(Context c) {
        if(cachedView == null || cachedContext != c) {
            cachedView = createView(c);
            cachedContext = c;
        }
        
        return cachedView;
    }
    
    public ArrayList<String> toStringArray() {
        ArrayList<String> stringList= new ArrayList<String>();
        
        for (ProcedurePage cp : pages) {
        	if(cp.shouldDisplay()) {
        		stringList.add(cp.getSummary());
        	}
        }
        
        return stringList;
    }
}
