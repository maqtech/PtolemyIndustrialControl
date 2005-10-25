/*

 Copyright (c) 2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.backtrack.xmlparser;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author tfeng
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConfigXmlTree {
    public ConfigXmlTree(String elementName) {
        this.elementName = elementName;
    }

    public void addChild(ConfigXmlTree child) {
        tree.add(child);
        child.setParent(this);
    }

    protected void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public void setParent(ConfigXmlTree parent) {
        this.parent = parent;
    }

    public String getAttribute(String name) {
        return (String) attributes.get(name);
    }

    public boolean hasAttribute(String name) {
        return attributes.get(name) != null;
    }

    public Enumeration getAttributeNames() {
        return attributes.keys();
    }

    public boolean isLeaf() {
        return tree.isEmpty();
    }

    public void setAttribute(String name, String value) {
        if (value != null) {
            attributes.put(name, value);
        }
    }

    public String getElementName() {
        return elementName;
    }

    public ConfigXmlTree getParent() {
        return parent;
    }

    public void startTraverseChildren() {
        iterator = tree.iterator();
    }

    public boolean hasMoreChildren() {
        return iterator.hasNext();
    }

    public ConfigXmlTree nextChild() {
        return (ConfigXmlTree) iterator.next();
    }

    public void dump() {
        dump(0);
    }

    public Object clone() {
        ConfigXmlTree newTree = new ConfigXmlTree(getElementName());
        newTree.attributes = (Hashtable) attributes.clone();
        startTraverseChildren();

        while (hasMoreChildren()) {
            newTree.addChild((ConfigXmlTree) nextChild().clone());
        }

        return newTree;
    }

    protected void dump(int indent) {
        dumpString(indent, getElementName());

        Enumeration attrenu = getAttributeNames();

        while (attrenu.hasMoreElements()) {
            String attr = (String) attrenu.nextElement();
            dumpString(indent + 2, "+A " + attr + " = " + getAttribute(attr));
        }

        startTraverseChildren();

        while (hasMoreChildren()) {
            ConfigXmlTree child = nextChild();
            child.dump(indent + 2);
        }
    }

    private void dumpString(int indent, String s) {
        StringBuffer buf = new StringBuffer(indent
                + ((s == null) ? 0 : s.length()));

        for (int i = 0; i < indent; i++) {
            buf.append(" ");
        }

        buf.append(s);
        System.out.println(buf);
    }

    private Iterator iterator;

    private String elementName;

    private Vector tree = new Vector();

    private ConfigXmlTree parent;

    private Hashtable attributes = new Hashtable();
}
