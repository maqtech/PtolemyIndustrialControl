/* NamedObj is the baseclass for most of the common Ptolemy objects.

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

package pt.kernel;

//////////////////////////////////////////////////////////////////////////
//// NamedObj
/** 
NamedObj is the baseclass for most of the common Ptolemy objects.  A
NamedObj is, simply put, a named object; in addition to a name, a
NamedObj has a reference to a parent object, which is always a Block
(a type of NamedObj). This reference can be null. A NamedObj also has
a descriptor.

@author Richard Stevens
<P>  Richard Stevens is an employee of the U.S. Government, whose
  written work is not subject to copyright.  His contributions to 
  this work fall within the scope of 17 U.S.C. A7 105. <P>

@version $Id$
@see classname
@see full-classname */
public abstract class NamedObj {
    /** no-arg Constructor - Construct a blank NamedObj
     * @see full-classname/method-name
     * @return Reference to created named object
     * @exception full-classname description
     */	
    public NamedObj() {
        this("", null, "");
    }

    /** Constructor with 3 arguments - Set the name, parent, 
     * and descriptor to the respective arguments
     * @see full-classname/method-name
     * @param n name
     * @param p parent
     * @param d descriptor
     * @return Reference to created named object
     */	
    public NamedObj(String n, Block p, String d) {
        super();
        setName(n);
        setParent(p);
        setDescriptor(d);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Get the name of the object
     * @see full-classname/method-name
     * @return The object name
     * @exception full-classname description
     */	
    public String getName() {
        return nm;
    }

    /** Get the descriptor
     * @see full-classname/method-name
     * @return The descriptor
     * @exception full-classname description
     */	
    public String getDescriptor() {
        return dscrptr;
    }

    /** Get the parent block
     * @see full-classname/method-name
     * @return The parent block
     * @exception full-classname description
     */	
    public Block getParent() {
        return prnt;
    }

    /** Specify the specific instance's place in the
     * universe-galaxy-star hierarchy. The default implementation
     * returns names that might look like
     * 
     * <P> universe.galaxy.star.port <P>
     * 
     * for a porthole; the output is the fullName of the parent, plus
     * a period, plus the name of the NamedObj it is called on.
     * This has no relation to the class name.
     *
     * @see full-classname/method-name
     * @return The full object name
     * @exception full-classname description 
     */
    public String getFullName() {
        if(prnt == null) { return nm; }
        else { return new String(prnt.getFullName() + "." + nm); }
    }

    /** Set the object name
     * @see full-classname/method-name
     * @param myName A String to be the new object name
     * @return void
     * @exception full-classname description
     */	
    public void setName (String myName) {
        nm = myName;
    }

    /** Description Set the parent block
     * @see full-classname/method-name
     * @param myParent A Block to be the new parent
     * @return void
     * @exception full-classname description
     */	
    public void setParent(Block myParent) {
        prnt = myParent;
    }

    /** Set the name and parent
     * @see full-classname/method-name
     * @param myName A String to be the new object name
     * @param myParent A Block to be the new parent
     * @return void
     * @exception full-classname description
     */	
    public void setNameParent(String myName, Block myParent) {
        setName(myName);
        setParent(myParent);
    }

    /** Prepare the object for system execution 
     * (abstract - must be defined in derived class)
     * @see full-classname/method-name
     * @return void
     * @exception full-classname description
     */	
    abstract public void initialize();

    /** Print a description of the object
     * @see full-classname/method-name
     * @param verbose If true, verbose description, else less verbose
     * @return A String describing the object
     * @exception full-classname description
     */	
    public String print(boolean verbose) {
        return new String(getFullName() + ": " + getClassName() + "\n");
    }

    /** Print the object's class name
     * @see full-classname/method-name
     * @return A String giving the class name of the object
     * @exception full-classname description
     */	
    public String getClassName() {
        return getClass().getName();
    }

    /** clear all flags
     * @see full-classname/method-name
     * @return void
     * @exception full-classname description
     */	
    public void clearAllFlags() {
        status.clearAllFlags();
    }

    /** set all flags
     * @see full-classname/method-name
     * @return void
     * @exception full-classname description
     */	
    public void setAllFlags() {
        status.setAllFlags();
    }

    /** clear a flag
     * @see full-classname/method-name
     * @param index identifying the flag
     * @return void
     * @exception full-classname description
     */	
    public void clearFlag (int index) {
        status.clearFlag(index);
    }

    /** set a flag
     * @see full-classname/method-name
     * @param index identifying the flag
     * @return void
     * @exception full-classname description
     */	
    public void setFlag (int index) {
        status.setFlag(index);
    }

    /** get a flag
     * @see full-classname/method-name
     * @param index identifying the flag
     * @return the flag (true or false)
     * @exception full-classname description
     */	
    public boolean getFlag (int index) {
        return status.getFlag(index);
    }

    /** set all counters to zero
     * @see full-classname/method-name
     * @return void
     * @exception full-classname description
     */	
    public void zeroAllCounters() {
        status.zeroAllCounters();
    }

    /** set all counters to a fixed fill value
     * @see full-classname/method-name
     * @param fillValue value to be assigned
     * @return void
     * @exception full-classname description
     */	
    public void setAllCounters(int fillValue) {
        status.setAllCounters(fillValue);
    }

    /** get a counter
     * @see full-classname/method-name
     * @param index identifying the counter
     * @return the counter
     * @exception full-classname description
     */	
    public int getCounter(int index) {
        return status.getCounter(index);
    }

    /** set a counter to a given value
     * @see full-classname/method-name
     * @param index identifying the counter
     * @param value to be set
     * @return void
     * @exception full-classname description
     */	
    public void setCounter(int index, int value) {
        status.setCounter(index, value);
    }

    /** set a counter to zero
     * @see full-classname/method-name
     * @param index identifying the counter
     * @return void
     * @exception full-classname description
     */	
    public void zeroCounter(int index) {
        status.zeroCounter(index);
    }

    /** increment a counter
     * @see full-classname/method-name
     * @param index identifying the counter
     * @return the counter after increment
     * @exception full-classname description
     */	
    public int incrementCounter(int index) {
        return status.incrementCounter(index);
    }

    /** decrement a counter
     * @see full-classname/method-name
     * @param index identifying the counter
     * @return true if new value is zero, else false
     * @exception full-classname description
     */	
    public boolean decrementCounter(int index) {
        return status.decrementCounter(index);
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Set the descriptor
     * @see full-classname/method-name
     * @param myDescriptor A String giving the new descriptor
     * @return void
     * @exception full-classname description
     */	
     protected void setDescriptor(String myDescriptor) {
         dscrptr = myDescriptor;
     }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////


    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////


    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* Private variables should not have doc comments, they should
     * have regular comments.
     */
     private String nm;            // name
     private Block prnt;           // parent
     private String dscrptr;       // descriptor
     private StatusArray status;   // flags and counters
}
