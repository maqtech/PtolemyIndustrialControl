/* ExampleSystem.java constructs a test hierachical graph using the pt.kernel classes.

 Copyright (c) 1997 The Regents of the University of California.
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

package pt.kernel.demo;
import pt.kernel.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// ExampleSystem
/** 
ExapmleSystem constructs a hierachical graph as shown in 
Ptolemy 2 design document, Figure 8 
The graph has 10 entities, 14 ports, and 12 relations.
The main function also returns the results of some key functions of
ComponentRelation and ComponentPort.
See Ptolemy 2 design document, Figure 11 
@author Jie Liu
@version @(#)ExampleSystem.java	1.2    01/27/98
*/
public class ExampleSystem extends ExampleApplet 
{
    /** ExampleSystem
     * construct the graph
     */	
    public ExampleSystem() throws IllegalActionException, NameDuplicationException {
        super();

        // Create composite entities
        e0 = new CompositeEntity();
        e0.setName("E0");
        e3 = new CompositeEntity(e0, "E3");
        e4 = new CompositeEntity(e3, "E4");
        e7 = new CompositeEntity(e0, "E7");
        e10 = new CompositeEntity(e0, "E10");
        
        // Create component entities
        e1 = new ComponentEntity(e4, "E1");
        e2 = new ComponentEntity(e4, "E2");
        e5 = new ComponentEntity(e3, "E5");
        e6 = new ComponentEntity(e3, "E6");
        e8 = new ComponentEntity(e7, "E8");
        e9 = new ComponentEntity(e10, "E9");
        
        // Create ports
        p0 = (ComponentPort) e4.newPort("P0");
        p1 = (ComponentPort) e1.newPort("P1");
        p2 = (ComponentPort) e2.newPort("P2");
        p3 = (ComponentPort) e2.newPort("P3");
        p4 = (ComponentPort) e4.newPort("P4");
        p5 = (ComponentPort) e5.newPort("P5");
        p6 = (ComponentPort) e5.newPort("P6");
        p7 = (ComponentPort) e3.newPort("P7");
        p8 = (ComponentPort) e7.newPort("P8");
        p9 = (ComponentPort) e8.newPort("P9");
        p10 = (ComponentPort) e8.newPort("P10");
        p11 = (ComponentPort) e7.newPort("P11");
        p12 = (ComponentPort) e10.newPort("P12");
        p13 = (ComponentPort) e10.newPort("P13");
        p14 = (ComponentPort) e9.newPort("P14");
        
        // Create links
        r1 = e4.connect(p1, p0, "R1");
        r2 = e4.connect(p1, p4, "R2");
        p3.link(r2);
        r3 = e4.connect(p1, p2, "R3");
        r4 = e3.connect(p4, p7, "R4");
        r5 = e3.connect(p4, p5, "R5");
        e3.allowLevelCrossingConnect(true);
        r6 = e3.connect(p3, p6, "R6");
        r7 = e0.connect(p7, p13, "R7");
        r8 = e7.connect(p9, p8, "R8");
        r9 = e7.connect(p10, p11, "R9");
        r10 = e0.connect(p8, p12, "R10");
        r11 = e10.connect(p12, p13, "R11");
        r12 = e10.connect(p14, p13, "R12");
        p11.link(r7);
       
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Action when click on the button
    *  Print all results in the text area. Override ExampleApplet.buttonAction
    *  @see pt.kernel.demo.Figure8.ExampleApplet#buttonAction()
    *  @see java.awt.buttton
    *  @param java.awt.event
    */	
    public boolean buttonAction(java.awt.Event event) {
        clearTextArea();
        printInTextArea("----Methods of ComponentRelation----\n");
        printInTextArea("getLinkedPorts:\n");
        printLinkedPorts(r1);
        printLinkedPorts(r2);
        printLinkedPorts(r3);
        printLinkedPorts(r4);
        printLinkedPorts(r5);
        printLinkedPorts(r6);
        printLinkedPorts(r7);
        printLinkedPorts(r8);
        printLinkedPorts(r9);
        printLinkedPorts(r10);
        printLinkedPorts(r11);
        printLinkedPorts(r12);
        
        printInTextArea("\ndeepGetLinkedPorts:\n");
        printDeepLinkedPorts(r1);
        printDeepLinkedPorts(r2);
        printDeepLinkedPorts(r3);
        printDeepLinkedPorts(r4);
        printDeepLinkedPorts(r5);
        printDeepLinkedPorts(r6);
        printDeepLinkedPorts(r7);
        printDeepLinkedPorts(r8);
        printDeepLinkedPorts(r9);
        printDeepLinkedPorts(r10);
        printDeepLinkedPorts(r11);
        printDeepLinkedPorts(r12);
        
        printInTextArea("\n----Methods of ComponentPort----\n");
        printInTextArea("getConnectedPorts:\n");
        printConnectedPorts(p0);
        printConnectedPorts(p1);
        printConnectedPorts(p2);
        printConnectedPorts(p3);
        printConnectedPorts(p4);
        printConnectedPorts(p5);
        printConnectedPorts(p6);
        printConnectedPorts(p7);
        printConnectedPorts(p8);
        printConnectedPorts(p9);
        printConnectedPorts(p10);
        printConnectedPorts(p11);
        printConnectedPorts(p12);
        printConnectedPorts(p13);
        printConnectedPorts(p14);
        
        printInTextArea("\ndeepGetConnectedPorts:\n");    
        printDeepConnectedPorts(p0);
        printDeepConnectedPorts(p1);
        printDeepConnectedPorts(p2);
        printDeepConnectedPorts(p3);
        printDeepConnectedPorts(p4);
        printDeepConnectedPorts(p5);
        printDeepConnectedPorts(p6);
        printDeepConnectedPorts(p7);
        printDeepConnectedPorts(p8);
        printDeepConnectedPorts(p9);
        printDeepConnectedPorts(p10);
        printDeepConnectedPorts(p11);
        printDeepConnectedPorts(p12);
        printDeepConnectedPorts(p13);                
        printDeepConnectedPorts(p14);
        return true;
    }

    /**init
    *  Initialize the applet
    */
    public void init() {
        super.init();
        addNotify();
        createAppletForm();
    }
    
    /**createAppletForm
    *  
    */
    public void createAppletForm() {
        try {
            create();
        } catch ( java.lang.Exception ex ) {}
    }

    /** Print the linked ports for a given ComponetRelation. The ports are
    *  restricted in the same level of hierachy
    *  @see pt.kernel.Relation#getLinkedPorts()
    *  @param ComponentRelation
    */	
    public void printLinkedPorts(ComponentRelation r) {
        String st = r.getName() + ": ";
        ComponentPort po;
        Enumeration ports = r.getLinkedPorts();
        while (ports.hasMoreElements()) {
            po = (ComponentPort) ports.nextElement();
            st += po.getName() + " ";
        }
        printInTextArea(st+"\n");
    }
    
    /** Print the deeply linked ports for a given ComponetRelation. Look through
    *  all transparent ports and return only non transparent ports (those
    *  with no inside links).
    *  @see pt.kernel.ComponentRelation#deepGetLinkedPorts()
    *  @param ComponentRelation
    */	
    public void printDeepLinkedPorts(ComponentRelation r) {
        String st = r.getName() + ": ";
        ComponentPort po;
        Enumeration ports = r.deepGetLinkedPorts();
        while (ports.hasMoreElements()) {
            po = (ComponentPort) ports.nextElement();
            st += po.getName() + " ";
        }
        printInTextArea(st+"\n");
    }
    
    /** Print the connected ports for a given ComponetPort.  Restricted
    *  to the same level of hierachy.
    *  @see pt.kernel.Port#getConnectedPorts()
    *  @param ComponentPort
    */	
    public void printConnectedPorts(ComponentPort p) {
        String st = p.getName() + ": ";
        ComponentPort po;
        Enumeration ports = p.getConnectedPorts();
        while (ports.hasMoreElements()) {
            po = (ComponentPort) ports.nextElement();
            st += po.getName() + " ";
        }
        printInTextArea(st+"\n");
    }
    
    /** Print the deeply connected ports for a given ComponetPort. Look through
    *  all transparent ports and return only non transparent ports (those
    *  with no inside links).
    *  @see pt.kernel.ComponentPort#deepGetConnectedPorts()
    *  @param ComponentPort
    */	
    public void printDeepConnectedPorts(ComponentPort p) {
        String st = p.getName() + ": ";
        ComponentPort po;
        Enumeration ports = p.deepGetConnectedPorts();
        while (ports.hasMoreElements()) {
            po = (ComponentPort) ports.nextElement();
            st += po.getName() + " ";
        }
        printInTextArea(st+"\n");
    }
        
    public static void main(String args[]) throws pt.kernel.NameDuplicationException, pt.kernel.IllegalActionException {
        ExampleSystem exsys = new ExampleSystem();
        ExampleFrame f = new ExampleFrame();
        f.setResizable(true);
        f.add(exsys);
        f.addNotify();
        java.awt.Insets insets = f.insets(); 
        exsys.init();
        exsys.move(insets.left,insets.top);
        f.resize(exsys.preferredSize());
        exsys.resize(exsys.preferredSize());
        f.show();
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////
    
    /** Components of the system
    */
    
    private CompositeEntity e0;
    private CompositeEntity e3;
    private CompositeEntity e4;
    private CompositeEntity e7;
    private CompositeEntity e10;
    
    private ComponentEntity e1;
    private ComponentEntity e2;
    private ComponentEntity e5;
    private ComponentEntity e6;
    private ComponentEntity e8;
    private ComponentEntity e9;
    
    private ComponentPort p0;
    private ComponentPort p1;
    private ComponentPort p2;
    private ComponentPort p3;
    private ComponentPort p4;
    private ComponentPort p5;
    private ComponentPort p6;
    private ComponentPort p7;
    private ComponentPort p8;
    private ComponentPort p9;
    private ComponentPort p10;
    private ComponentPort p11;
    private ComponentPort p12;
    private ComponentPort p13;
    private ComponentPort p14;
    
    private ComponentRelation r1;
    private ComponentRelation r2;
    private ComponentRelation r3;
    private ComponentRelation r4;
    private ComponentRelation r5;
    private ComponentRelation r6;
    private ComponentRelation r7;
    private ComponentRelation r8;
    private ComponentRelation r9;
    private ComponentRelation r10;
    private ComponentRelation r11;
    private ComponentRelation r12;    
}

