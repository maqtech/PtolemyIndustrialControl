/* Interface for embedded director for CT sub-systems.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)

*/
package ptolemy.domains.ct.kernel;

//////////////////////////////////////////////////////////////////////////
//// CTEmbeddedDirector
/** 
Interface for CT embedded directors. It defines three methods to support the
outside CTDirector. The methods are about step size control. After the internal
CT subsystem finishes one integration step, its step size control information
should be accessible from the outside CT director.
<P>
Note: This class is under significant redesign, please do not use it if
possible.
@author  Jie Liu
@version $Id$

*/
public interface CTEmbeddedDirector {
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the current integration step is successful.
     *  @return True if the current step is successful
     */	
    public boolean isThisStepSuccessful();

    /** Return the predicted next step size if this step is successful.
     */
    public double predictedStepSize();

    /** Return the refined step size if this step is not successful.
     */
    public double refinedStepSize();

}
