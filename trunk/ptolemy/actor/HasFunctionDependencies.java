/* An interface provides access to the FunctionDependency object
   associated with an entitiy.

   Copyright (c) 2003-2004 The Regents of the University of California.
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

   @ProposedRating Red (hyzheng@eecs.berkeley.edu)
   @AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.actor;

//////////////////////////////////////////////////////////////////////////
//// HasFunctionDependencies
/** An interface provides access to the FunctionDependency object
    ssociated with an entity. This interface is extended by Actor interface.

    @see Actor
    @see FunctionDependency
    @author Haiyang Zheng
    @version $Id$
    @since Ptolemy II 3.1
*/
public interface HasFunctionDependencies {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an instance of FunctionDependency.
     *  @return the FunctionDependency object.
     */
    public FunctionDependency getFunctionDependencies();

}
