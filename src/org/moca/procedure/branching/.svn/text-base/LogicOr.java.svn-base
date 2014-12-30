package org.moca.procedure.branching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.moca.procedure.ProcedureElement;
import org.moca.procedure.ProcedureParseException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * LogicOr is a Criteria subclass that serves as a container to hold multiple
 * Criteria. The key difference between LogicOr and LogicAnd is the criteriaMet()
 * method. As the name implies, LogicOr's criteriaMet() method evaluates if ANY
 * of the Criteria it holds are met.
 */
public class LogicOr extends Criteria {
    private List<Criteria> criteria;
    
    public LogicOr(List<Criteria> crit) {
        this.criteria = crit;
    }
    
    public boolean criteriaMet() {
        for(Criteria c : criteria) {
            if (c.criteriaMet())
                return true;
        }
        return false;
    }
    
    public static LogicOr fromXML(Node node, HashMap<String, ProcedureElement> elts) throws ProcedureParseException {
        if(!node.getNodeName().equals("or"))
            throw new ProcedureParseException("LogicOr got NodeName " + node.getNodeName());
        NodeList children = node.getChildNodes();
        List<Criteria> crits = new ArrayList<Criteria>();
        for(int i=0; i<children.getLength();i++) {
            Node child = children.item(i);
            if(child.getNodeName().equals("Criteria") || 
                    child.getNodeName().equals("and") || 
                    child.getNodeName().equals("or") || 
                    child.getNodeName().equals("not")) {
                crits.add(Criteria.switchOnCriteria(child, elts));
            }
        }
        if (crits.size() == 0)
            throw new ProcedureParseException("LogicOr no arguments to <or>");
        else
            return new LogicOr(crits);
    }
}