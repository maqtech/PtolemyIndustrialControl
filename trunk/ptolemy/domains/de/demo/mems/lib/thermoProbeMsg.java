/*
A data object that represents a temperature [environmental] value.  
Whenever an temperature change occur inside the MEMSEnvir object, an 
instance of thermoProbe will be created with the new temperature value 
generated by by the Environmental Value Generator (MEMSEVG).  The 
thermoProbe object will then be wrapped inside an ObjectToken, which 
will be transported to the sensor Actors inside the MEMSProc.  

;;;;;;; OLD
 An probe object that invokes the getTemp() method in the MEMSEnvir
   actor and keeps a copy of the value returned by getTemp().
;;;;;;; OLD

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.de.demo.mems.lib;

// import ptolemy.actor.*;
// import ptolemy.domains.de.kernel.*;
// import ptolemy.kernel.*;
// import ptolemy.kernel.util.*;
// import ptolemy.data.*;
// import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// thermoProbeMsg
/**

A data object that represents a temperature value.  Whenever an
temperature change occur inside the MEMSEnvir object, an instance of
thermoProbe will be created with the new temperature value generated by
by the Environmental Value Generator (MEMSEVG).  The thermoProbe object 
will then be wrapped inside an ObjectToken, which will be transported 
to the sensor Actors inside the MEMSProc.  

;;;;;;;; OLD
An probe object that rides inside an ObjectToken.  When the token reaches a 
MEMSEnvir Actor, the actor will call its "probe" method, passing a reference
of the MEMSEnvir itself as argument.  The "probe" method in turn calls the
getTemp() in MEMSEnvir and keeps a copy of the value it returns.
;;;;;;;; OLD

@author Allen Miu
@version $Id$
*/
public class thermoProbeMsg extends ProbeMsg {

  ///////////////////////////////////////////////////////////////////
  ////                         public methods                    ////

  /** Calls MEMSEnvir's getTemp() and keeps a copy of the return
   *  value in _value
   */
  /* OLD */
  /*
  public void probe(MEMSEnvir envir) {
    value = envir.getTemp();
  }
  */

  /** Creates a thermoProbe object that stores the temperature value
   */
  public thermoProbeMsg(double temperature) {
    _temperature = temperature;
    thermoProbe = true;
  }

  /** Returns the temperature of this Probe instance.
   */
  public double getTemperature() {
    return _temperature;
  }

  ///////////////////////////////////////////////////////////////////
  ////                         private methods                   ////
  private double _temperature;
}






