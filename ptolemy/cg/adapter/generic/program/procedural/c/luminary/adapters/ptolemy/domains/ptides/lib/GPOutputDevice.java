/* A code generation helper class for ptolemy.domains.ptides.lib.targets.luminary.GPOutputDevice

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.domains.ptides.lib;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib.OutputDevice;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A code generation helper class for ptolemy.domains.ptides.lib.targets.luminary.GPOutputDevice.
 * @author Jia Zou, Isaac Liu, Jeff C. Jensen
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Yellow (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 */

public class GPOutputDevice extends OutputDevice {
    /** Construct a helper with the given
     *  ptolemy.domains.ptides.lib.GPOutputDevice actor.
     *  @param actor The given ptolemy.domains.ptides.lib.targets.luminary.GPOutputDevice actor.
     *  @throws IllegalActionException 
     *  @throws NameDuplicationException 
     */
    public GPOutputDevice(ptolemy.domains.ptides.lib.targets.luminary.GPOutputDevice actor) throws IllegalActionException, NameDuplicationException {
        super(actor);
        
        Parameter pinParameter = actor.pin;
        StringParameter padParameter = actor.pad;
        _pinID = null;
        _padID = null;

        if (pinParameter != null) {
            _pinID = ((IntToken) pinParameter.getToken()).toString();
        } else {
            throw new IllegalActionException("does not know what pin this output device is associated to.");
        }
        if (padParameter != null) {
            _padID = padParameter.stringValue();
        } else {
            throw new IllegalActionException("does not know what pin this output device is associated to.");
        }
        
    }
    
    ////////////////////////////////////////////////////////////////////
    ////                     public methods                         ////


    public String generateActuatorActuationFuncCode() throws IllegalActionException {
        List args = new LinkedList();
        CodeStream _codeStream = getStrategy().getCodeStream();
        
        args.add(_padID);
        args.add(_pinID);

        _codeStream.clear();
        _codeStream.appendCodeBlock("actuationBlock", args);

        return processCode(_codeStream.toString());
    }

    /**
     * Generate the fire code. In this base class, add the name of the
     * associated component in the comment. It checks the inline parameter
     * of the code generator. If the value is true, it generates the actor
     * fire code and the necessary type conversion code. Otherwise, it
     * generate an invocation to the actor function that is generated by
     * generateFireFunctionCode.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateFireCode() throws IllegalActionException {
        List args = new LinkedList();
        CodeStream _codeStream = getStrategy().getCodeStream();
        
        ptolemy.domains.ptides.lib.targets.luminary.GPOutputDevice actor = (ptolemy.domains.ptides.lib.targets.luminary.GPOutputDevice) getComponent();
        PtidesBasicDirector adapter = (PtidesBasicDirector)getAdapter(actor.getDirector());

        args.add((adapter._actuators.get(actor)).toString());

        _codeStream.clear();
        _codeStream.appendCodeBlock("fireBlock", args);

        return processCode(_codeStream.toString());
    }
    
    public String generateHardwareInitializationCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        List args = new ArrayList();
        args.add(_padID);
        args.add(_pinID);
        code.append(processCode(getStrategy().getCodeStream().getCodeBlock("initializeGPOutput", args)));
        return code.toString();
    }
    
    private String _pinID;
    private String _padID;
}
