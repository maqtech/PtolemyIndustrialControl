/* OMCCommand invokes OpenModelica Compiler(OMC) by sending command to the OMC server.
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

/**    
    <p> It invokes OpenModelica Compiler(OMC) by sending command to the 
    OMC server with different purposes such as building the Modelica model by 
    sending buildModel(), loading the Modelica model by sending loadModel() and 
    loading file by sending loadFile().
    It fetches the string result of invocation as well. </p>

    @author Mana Mirzaei
    @version $Id$
    @since Ptolemy II 9.1
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
*/
public class OMCCommand implements IOMCCommand {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                   ////

    /**  Build the Modelica model by sending buildModel(className) to the OMC.
     *   @param modelName The Name of the model which should be built.
     *   @return CompilerResult The result of sending buildModel(className) command to the OMC.
     *   @exception ConnectException If buildModel command couldn't
     *   be sent to the OMC.
     */
    public CompilerResult buildModel(String modelName) throws ConnectException {
        // Create a unique instance of OMCProxy.
        _omcProxy = OMCProxy.getInstance();

        CompilerResult buildModelResult = _omcProxy.sendCommand("buildModel("
                + modelName + ")");
        return buildModelResult;
    }

    /** Create an instance of OMCCommand object in order to provide a global point of access to the instance.
     *  It provides a unique source of OMCCommand instance.
     *  @return An OMCCommand object representing the instance value.
     */
    public static OMCCommand getInstance() {
        if (_omcCommandInstance == null) {
            _omcCommandInstance = new OMCCommand();
        }
        return _omcCommandInstance;
    }

    /** Load the Modelica model from the file.
     *  @param fileName The name of the file which is loaded.
     *  @return CompilerResult The result of sending loadFileInteractiveQualified("fileName") command to the OMC.
     *  @exception ConnectException If loadFileInteractiveQualified command couldn't
     *  be sent to the OMC.
     */
    public CompilerResult loadFile(String fileName) throws ConnectException {
        // Create a unique instance of OMCProxy.
        _omcProxy = OMCProxy.getInstance();
        CompilerResult loadFileInteractiveQualifiedResult = _omcProxy
                .sendCommand("loadFileInteractiveQualified(\"" + fileName
                        + "\")");
        return loadFileInteractiveQualifiedResult;
    }

    /** Load the Modelica model.
     *  @param modelName The Name of the model which should be loaded from the Modelica library.
     *  @return CompilerResult The result of sending loadModel(modelName) command to the OMC.
     *  @exception ConnectException If the command couldn't
     *  be sent to the OMC.
     */
    public CompilerResult loadModel(String modelName) throws ConnectException {
        // Create a unique instance of OMCProxy.
        _omcProxy = OMCProxy.getInstance();
        CompilerResult loadModelResult = _omcProxy.sendCommand("loadModel("
                + modelName + ")");
        return loadModelResult;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // OMCProxy Object for accessing a unique source of instance. 
    private static OMCCommand _omcCommandInstance = null;

    // OMCProxy Object for accessing a unique source of instance.
    private OMCProxy _omcProxy;
}
