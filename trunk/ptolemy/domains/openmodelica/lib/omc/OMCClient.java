/* The simulation program that is launched by the System Command actor.
 *
 * Copyright (c) 2012-2013,
 * Programming Environment Laboratory (PELAB),
 * Department of Computer and getInformation Science (IDA),
 * Linkoping University (LiU).
 *
 * All rights reserved.
 *
 * (The new BSD license, see also
 *  http://www.opensource.org/licenses/bsd-license.php)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of Authors nor the name of Linkopings University nor
 *   the names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * 
 */

package ptolemy.domains.openmodelica.lib.omc;

import java.io.IOException;

import lbnl.lib.openmodelica.UtilSocket;
import ptolemy.kernel.util.IllegalActionException;

/**    
   <p> OMCClient is the simulation program that is launched by the System Command actor.
    System Command actor in <i>OpenModelica.xml</i> model in <i>lbnl/demo/OpenModelica</i> 
    causes Ptolemy II to call the command java at each time step, with the values 
    <i>-cp ../../.. ptolemy.domains.openmodelica.lib.omc.OMCClient</i>. 
   </p>       

    @author Mana Mirzaei 
    @version $Id$
    @since Ptolemy II 9.1
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
 */
public class OMCClient {

    /** OpenModelica Compiler(OMC) is called to initialize the OMC server and simulate the Modelica model afterward. 
     *  The generated simulation runtime file in <i>$TMPDIR/$USERNAME/OpenModelica</i> runs by <i>-interactive</i> flag. 
     *  Upon running the executable file the keyboard entries and the results are displayed in the same console as you are typing the command. 
     *  The simulation runtime waits until a Control Client has been connected to its port through calling the method 
     *  <i>establishSocket()</i>. Afterwards, <i>exchangeData()</i> handles communications between Ptolemy II and OMI. 
     *  @param args
     *  @throws IllegalActionException
     *  @throws IOException If an I/O error occurs while establishing connection or exchanging data over network.
     */
    public static void main(String[] args) throws IllegalActionException,
            IOException {

        try {
            // Initialize the OpenModelica compiler(OMC) server.
            _omcProxy.initServer();

            // Load the Modelica file - BouncingBall.mo and the Modelica library.
            _omcProxy.loadFile("BouncingBall.mo", "BouncingBall");

            // Build the Modelica model and run the executable result file in an interactive processing mode. [run by -interactive flag]
            _omcProxy.simulateModel("BouncingBall.mo", "BouncingBall",
                    "BouncingBall", "0.0", "0.1", 500, "0.0001", "dassl",
                    "csv", ".*", "", "", "interactive");
        } catch (ConnectException e) {
            e.printStackTrace();
            throw new IllegalActionException(e.getMessage());
        }

        // Create a unique instance of UtilSocket.
        _utilSocket = UtilSocket.getInstance();

        // Establish the client socket and initiate the connection.
        _utilSocket.establishSocket();

        // Use the BSD socket to exchange data with the OMC server.          
        _utilSocket.exchangeData();

        // Stop the OMC server.
        try {
            _omcProxy.quitServer();
            System.out.println("OMC server stopped!");
        } catch (ConnectException e) {
            e.printStackTrace();
            throw new IllegalActionException(
                    "SeverError: unable to stop server.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ////
    // OMCProxy object for accessing a unique source of instance.
    private static OMCProxy _omcProxy = OMCProxy.getInstance();

    // UtilSocket object for accessing a unique source of instance.
    private static UtilSocket _utilSocket = UtilSocket.getInstance();
}
