/*
 * Copyright (c) 2008 Marek Trtik
 * Copyright (c) 2008-2009 Jiri Slaby <jirislaby@gmail.com>
 *
 * Licensed under GPLv2.
 */
package cz.muni.stanse.utils.xmlpatterns;

import java.io.IOException;
import java.io.OutputStream;

import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public final class XMLAlgo {

    /**
     * Check if two elements are (recursively) equal
     *
     * TODO: remove and use equality tester from dom4j pkg
     *
     * @param e1 first element
     * @param e2 second element
     */
    public static boolean equalElements(final Element e1, final Element e2) {
	if (!e1.getName().equals(e2.getName()))
	    return false;
	if (!e1.getText().equals(e2.getText()))
	    return false;

    for (final Object attrObj : e1.attributes()) {
        final Attribute attr = (Attribute)attrObj;
        if (!attr.getName().equals("bl") &&
            !attr.getName().equals("bc") &&
            !attr.getValue().equals(e2.attributeValue(attr.getName())))
            return false;
    }


	final Iterator i = e1.elementIterator();
	final Iterator j = e2.elementIterator();
	while (i.hasNext() && j.hasNext())
	    if (!equalElements((Element)i.next(),(Element)j.next()))
		return false;
	if (i.hasNext() || j.hasNext())
	    return false;

	return true;
    }

    /**
     * Pretty-print XML node to a stream
     *
     * @param n node to dump
     * @param o stream to dump to
     */
    public static void outputXML(Node n, OutputStream o) {
	OutputFormat format = OutputFormat.createPrettyPrint();
	try {
	    XMLWriter writer = new XMLWriter(o, format);
	    writer.write(n);
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }

    /**
     * Pretty-print XML node to the System.err
     *
     * @param n node to dump
     */
    public static void outputXML(Node n) {
	outputXML(n, System.err);
    }

    public static Document toDocument(final String xml) {
        try {
            return DocumentHelper.parseText(xml);
        } catch (final DocumentException e) {
            return null;
        }
    }

    public static Element toElement(final String xml) {
        final Document doc = toDocument(xml);
        return doc != null ? (Element)doc.getRootElement().detach() : null;
    }

    private XMLAlgo() {
    }
}
