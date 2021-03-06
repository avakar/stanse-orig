/**
 * @file XMLPattern.java
 * @brief 
 *
 * Copyright (c) 2008-2009 Marek Trtik
 *
 * Licensed under GPLv2.
 */
package cz.muni.stanse.utils.xmlpatterns;

import cz.muni.stanse.utils.Pair;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Attribute;

/**
 * @brief
 *
 * @see
 */
public final class XMLPattern {

    // public section

    public XMLPattern(final String xml) {
        this(XMLAlgo.toElement(xml));
    }

    public XMLPattern(final String patternName, final String xml) {
        this(XMLAlgo.toElement("<pattern name=\"" + patternName + "\">" +
                               xml + "</pattern>"));
    }

    public XMLPattern(final Element XMLelement) {
        patternXMLelement = XMLelement;
        name = patternXMLelement.attributeValue("name");
        final String conAttr = patternXMLelement.attributeValue("constructive");
        constructive = (conAttr == null) ? true : !conAttr.equals("false");
    }

    @Override
    public boolean equals(Object obj) {
        return (obj == null || getClass() != obj.getClass()) ?
                    false : isEqualWith((XMLPattern)obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.patternXMLelement != null ?
                                         this.patternXMLelement.hashCode() : 0);
        hash = 43 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 43 * hash + (this.constructive ? 1 : 0);
        return hash;
    }

    boolean isEqualWith(XMLPattern other) {
        return isConstructive() == other.isConstructive() &&
               getName().equals(other.getName()) &&
               getPatternXMLelement().equals(other.getPatternXMLelement());
    }

    public String getName() {
        return name;
    }

    public boolean isConstructive() {
        return constructive;
    }

    public Pair<Boolean,XMLPatternVariablesAssignment>
    matchesXMLElement(final Element XMLelement) {
        final XMLPatternVariablesAssignment varsAssignment =
                new XMLPatternVariablesAssignment();
        return new Pair<Boolean,XMLPatternVariablesAssignment>(
                          matchingElements(getPatternXMLelement(),XMLelement,
                                           varsAssignment),
                          varsAssignment);
    }

    // private section

    private static boolean matchingElements(final Element XMLpivot,
                           final Element XMLelement,
                           final XMLPatternVariablesAssignment varsAssignment) {
        if (XMLpivot.getName().equals("nested")) {
            final String elementName = XMLelement.getName();
            for (final Iterator<Attribute> j = XMLpivot.attributeIterator();
			j.hasNext(); )
                if (elementName.equals(j.next().getValue()))
                    return false;
            return onNested(XMLpivot,XMLelement,varsAssignment);
        }
        if (XMLpivot.getName().equals("ignore"))
            return true;
        if (XMLpivot.getName().equals("var")) {
            if (!satisfyVarConstraints(XMLelement.getName(),
                                       XMLpivot.attribute("match"),
                                       XMLpivot.attribute("except")))
                return false;
            varsAssignment.put(XMLpivot.attribute("name").getValue(),
                               XMLelement);
            return true;
        }
        
        if (!XMLpivot.getName().equals(XMLelement.getName()))
            return false;
        if (!matchingAttributes(XMLpivot.attributes(),XMLelement))
            return false;
        if (XMLpivot.isTextOnly() != XMLelement.isTextOnly())
            return false;
        if (XMLpivot.isTextOnly() &&
            !XMLpivot.getText().equals(XMLelement.getText()))
            return false;

        final Iterator<Element> i = XMLpivot.elementIterator();
        final Iterator<Element> j = XMLelement.elementIterator();
	while (i.hasNext() && j.hasNext()) {
            final Element pivotNext = i.next();
            if (pivotNext.getName().equals("any"))
                return true;
            if (!matchingElements(pivotNext, j.next(), varsAssignment))
                return false;
        }
        if (i.hasNext() || j.hasNext())
            return false;

        return true;
    }

    private static boolean onNested(final Element XMLpivot,
                           final Element XMLelement,
                           final XMLPatternVariablesAssignment varsAssignment) {
        if (matchingElements((Element)XMLpivot.elementIterator().next(),
		XMLelement, varsAssignment))
            return true;
        
        for (final Iterator<Element> j = XMLelement.elementIterator();
		j.hasNext(); )
            if (matchingElements(XMLpivot, j.next(), varsAssignment))
                return true;

        return false;
    }

    private Element getPatternXMLelement() {
        return (Element)patternXMLelement.elementIterator().next();
    }
    
    private static boolean matchingAttributes(final List<Attribute> pivotATTRs,
                                              final Element XMLelement) {
        for (final Attribute pivotAttr: pivotATTRs) {
            final String elem = XMLelement.attributeValue(pivotAttr.getName());
            if (elem == null || !matchingAttribute(pivotAttr.getValue(), elem))
                return false;
        }
        return true;
    }

    private static boolean matchingAttribute(final String pivotAttr,
                                             final String elemAttr) {
        final Pair<Boolean,String[]> parsedData = splitAttrSymbols(pivotAttr);
        return retvalWhenItemInSet(!parsedData.getFirst(),elemAttr,
                                   parsedData.getSecond());
    }

    private static boolean satisfyVarConstraints(final String elemName,
                                                 final Attribute matchAttr,
                                                 final Attribute exceptAttr) {
        return (matchAttr != null) ?
                   retvalWhenItemInSet(true,elemName,
                           splitAttrSymbols(matchAttr.getValue()).getSecond()) :
               (exceptAttr != null) ?
                   retvalWhenItemInSet(false,elemName,
                           splitAttrSymbols(exceptAttr.getValue()).getSecond()):
                   true;
    }

    private static boolean retvalWhenItemInSet(final boolean retval,
                                               final String item,
                                               final String[] set) {
        for (final String setItem : set)
            if (setItem.equals(item))
                return retval;
        return !retval;
    }

    private static Pair<Boolean,String[]> splitAttrSymbols(String attrString){
        attrString = attrString.replace(" ","")
                               .replace("\t","");
        return (attrString.charAt(0) != '-' && attrString.charAt(0) != '{') ?
                    Pair.make(false,attrString.split(" ")) :
                    Pair.make(attrString.charAt(0) == '-' ? true : false,
                              attrString.substring(attrString.indexOf('{') + 1,
                                                   attrString.lastIndexOf('}'))
                                        .split("\\}\\{"));
    }

    private final Element patternXMLelement;
    private final String name;
    private final boolean constructive;
}
