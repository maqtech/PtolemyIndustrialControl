/* An environment for declarations, which may be contained in another
environment.

Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.lang;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


//////////////////////////////////////////////////////////////////////////
//// Environ
/** An environment for declarations, which may be contained in another
environment.
<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@author Jeff Tsay
@version $Id$
 */
public class Environ {

    /** Construct an empty environment. */
    public Environ() {
        this(null, new LinkedList());
    }

    /** Constuct an environment nested inside the parent argument,
     *  without its own proper Decl's.
     */
    public Environ(Environ parent) {
        this(parent, new LinkedList());
    }

    /** Construct an environment nested inside the parent argument,
     *  with the given List of Decl's in this environment itself.
     */
    public Environ(Environ parent, List declList) {
        _parent = parent;
        _declList = declList;
    }

    public Environ parent() {
        return _parent;
    }

    /** Adds a mapping to the argument decl in this environment proper.
     *  This does not affect any Environs in which this is nested.
     */
    public void add(Decl decl) {
        _declList.add(decl);
    }

    /** copy the declList from env */
    public void copyDeclList(Environ env) {
	// FIXME: This is a little strange, but if two envs share a declList
	// then copyDeclList will effectively set them to null
        _declList.clear();
        _declList.addAll(env._declList);
    }

    /** Lookup a decl by name in the current environment, do not look
     *  in the parent environment, if any.	
     */
    public Decl lookup(String name) {
        return lookup(name, Decl.CG_ANY, new boolean[1], false);
    }

    /** Lookup a decl by name and mask in the current environment, do not look
     *  in the parent environment, if any.	
     */
    public Decl lookup(String name, int mask) {
        return lookup(name, mask, new boolean[1], false);
    }

    /** Lookup a decl by name in the current environment, do not look
     *  in the parent environment. Set more[0] to true if there are
     *  other decls with the same name in the environment.	
     */
    public Decl lookup(String name, boolean[] more) {
        return lookup(name, Decl.CG_ANY, more, false);
    }

    /** Lookup a decl by name and mask in the current environment, do not look
     *  in the parent environment, if any. Set more[0] to true if there 
     *  is one or more decls with the same name in the environment.
     *  Note that more[0] will be true if the other decls have the
     *  same name but different masks.
     */
    public Decl lookup(String name, int mask, boolean[] more) {
        return lookup(name, mask, more, false);
    }

    /** Lookup a decl by name in the current environment and
     *  in the parent environment, if any.
     */	
    public Decl lookupProper(String name) {
        return lookup(name, Decl.CG_ANY, new boolean[1], true);
    }

    /** Lookup a decl by name and mask in the current environment and
     *  in the parent environment, if any.
     */	
    public Decl lookupProper(String name, int mask) {
        return lookup(name, mask, new boolean[1], true);
    }

    /** Lookup a decl by name in the current environment and
     *  in the parent environment, if any. Set more[0] to true if there 
     *  is one or more decls with the same name in the environment.
     *  Note that more[0] will be true if the other decls have the
     *  same name but different masks.
     */	
    public Decl lookupProper(String name, boolean[] more) {
        return lookup(name, Decl.CG_ANY, more, true);
    }

    /** Lookup a decl by name and mask in the current environment and
     *  in the parent environment, if any. Set more[0] to true if there 
     *  is one or more decls with the same name in the environment.
     *  Note that more[0] will be true if the other decls have the
     *  same name but different masks.
     */	
    public Decl lookupProper(String name, int mask, boolean[] more) {
        return lookup(name, mask, more, true);
    }

    /** Lookup a decl by name and mask in the current environment or
     *  in the parent environment, if any. Set more[0] to true if there 
     *  is one or more decls with the same name in the environment.
     *  Note that more[0] will be true if the other decls have the
     *  same name but different masks.  If the proper argument is
     *  true, then look in the parent environment, if any.  If it
     *  is false, then do not look in the parent environment.
     */	
    public Decl lookup(String name, int mask, boolean[] more, boolean proper) {
        EnvironIter itr = lookupFirst(name, mask, proper);

        if (itr.hasNext()) {
            Decl retval = (Decl) itr.next();
            more[0] = itr.hasNext();
            return retval;
        }
        more[0] = false;
        return null;
    }

    public EnvironIter lookupFirst(String name) {
        return lookupFirst(name, Decl.CG_ANY, false);
    }

    public EnvironIter lookupFirst(String name, int mask) {
        return lookupFirst(name, mask, false);
    }

    public EnvironIter lookupFirstProper(String name) {
        return lookupFirst(name, Decl.CG_ANY, true);
    }

    public EnvironIter lookupFirstProper(String name, int mask) {
        return lookupFirst(name, mask, true);
    }

    public EnvironIter lookupFirst(String name, int mask, boolean proper) {
        Environ parent = proper ? null : _parent;

        return new EnvironIter(parent, _declList.listIterator(), name, mask);
    }

    public EnvironIter allDecls() {
        return lookupFirst(Decl.ANY_NAME, Decl.CG_ANY, false);
    }

    public EnvironIter allDecls(int mask) {
        return lookupFirst(Decl.ANY_NAME, mask, false);
    }

    public EnvironIter allDecls(String name) {
        return lookupFirst(name, Decl.CG_ANY, false);
    }

    public ListIterator allProperDecls() {
        return _declList.listIterator();
    }

    public EnvironIter allProperDecls(int mask) {
        return lookupFirst(Decl.ANY_NAME, mask, true);
    }

    public EnvironIter allProperDecls(String name) {
        return lookupFirst(name, Decl.CG_ANY, true);
    }

    /** Return true if there is more than one matching Decl only
     *  in this Environ.
     */
    public boolean moreThanOne(String name, int mask) {
        return moreThanOne(name, mask, false);
    }

    /** Return true if there is more than one matching Decl in this Environ,
     *  and if proper is true, in the enclosing Environs.
     */
    public boolean moreThanOne(String name, int mask, boolean proper) {
        boolean[] more = new boolean[1];

        lookup(name, mask, more, proper);

        return more[0];
    }

    public String toString() {
        return toString(true);
    }

    public String toString(boolean recursive) {
        ListIterator declItr = _declList.listIterator();

        StringBuffer retval = new StringBuffer("[");

        while (declItr.hasNext()) {
            Decl d = (Decl) declItr.next();
            retval.append(d.toString());
            if (declItr.hasNext()) {
                retval.append(", ");
            }
        }

        retval.append("] ");

        if (_parent != null) {
            retval.append("has parent\n");

            if (recursive) {
                retval.append(_parent.toString(true));
            }
        } else {
            retval.append("no parent\n");
        }
        return retval.toString();
    }

    protected Environ _parent;
    protected List _declList;
}
