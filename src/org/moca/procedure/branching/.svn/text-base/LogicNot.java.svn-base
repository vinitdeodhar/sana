package org.moca.procedure.branching;

import java.util.HashMap;

import org.moca.procedure.ProcedureElement;
import org.moca.procedure.ProcedureParseException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * LogicNot is a Criteria subclass that serves as a container to hold a single
 * Criteria object. It's criteriaMet() method evaluates the Criteria it holds 
 * and returns the opposite (not).
 */
public class LogicNot extends Criteria {
    Criteria criteria;
    
    public LogicNot(Criteria crit) {
        this.criteria = crit;
    }
    
    public boolean criteriaMet() {
        return !criteria.criteriaMet();
    }
    
    public static LogicNot fromXML(Node node, HashMap<String, ProcedureElement> elts) throws ProcedureParseException {
        if(!node.getNodeName().equals("not"))
            throw new ProcedureParseException("LogicNot got NodeName " + node.getNodeName());
        NodeList children = node.getChildNodes();
        if (children.getLength() != 3)
            throw new ProcedureParseException("LogicNot wrong number of elements: expects 1");
        Node child = children.item(1);
        return new LogicNot(Criteria.switchOnCriteria(child, elts));
    }
}