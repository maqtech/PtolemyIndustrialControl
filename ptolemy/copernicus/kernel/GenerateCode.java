/* Standalone application that generates code

 Copyright (c) 2002 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.kernel;

import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.gui.MessageHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// GenerateCode
/** 

A Standalone application that generates code using the Ptolemy II code
generation system.  This class acts a wrapper for the copernicus Main
class by providing defaults arguments for the various backends.  The
default arguments are read in from a compileCommandTemplate.in file,
variables are substituted and the command executed.

<p>For example:
<pre>
java -classpath $PTII ptolemy.copernicus.kernel.GenerateCode foo.xml
</pre>
will read in the $PTII/ptolemy/copernicus/java/compileCommandTemplate.in,
substitute in the appropriate variables and then generate code for foo.xml

<p>The default code generator is the deep code generator.
<p>The argument that names the xml file containing the model to generate
code for should be a relative pathname.  If no argument is specified,
then code is generated for $PTII/ptolemy/domains/sdf/demo/Orthocomm/Orthocomm.xml

<p>Generating code is fairly complex, so there are many other parameters
that can be set as the other arguments.  

<p>The general format is
<code>-<i>ParameterName</i> <i>ParameterValue</i></code>, for example:
<code>-codeGenerator "shallow"</code>
<p>For example:
<pre>
java -classpath $PTII ptolemy.copernicus.kernel.GenerateCode -codeGenerator "shallow" foo.xml
</pre>

<p>The initial parameters, their values and any documentation can be
printed with

<pre>
java -classpath $PTII ptolemy.copernicus.kernel.GenerateCode -help
</pre>


@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0 */
public class GenerateCode {
    /** Parse the specified command-line arguments and then execute
     *  any specified commands.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public GenerateCode(String args[]) throws Exception {
	NamedObj namedObj = new NamedObj();
	_generatorAttribute =
	    new GeneratorAttribute(namedObj, "_helpGeneratorAttribute");

	_parseArgs(args);
	if (_verbose) {
	    System.out.println(_generatorAttribute.toString());
	}
	String command = commandToExecute(_generatorAttribute);
	System.out.println("About to execute:\n" + command);
	try {
	    // Need to deal with stdout and stderr??
	    Process process = Runtime.getRuntime().exec(command);

	    // Set up a Thread to read in any error messages
	    _StreamReaderThread errorGobbler = new
		_StreamReaderThread(process.getErrorStream(), System.err);

	    // Set up a Thread to read in any output messages
	    _StreamReaderThread outputGobbler = new
		_StreamReaderThread(process.getInputStream(), System.out);

	    // Start up the Threads
	    errorGobbler.start();
	    outputGobbler.start();
	    try {
		int processReturnCode = process.waitFor();
		synchronized(this) {
		    process = null;
		}
	    } catch (InterruptedException interrupted) {
		System.out.println("InterruptedException: "
				   + interrupted);
		throw interrupted;
	    }
	    System.out.println("All Done.");
	} catch (final IOException io) {
	    System.err.println("IOException: " + io);
	} 
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public String commandToExecute(GeneratorAttribute generatorAttribute)
	throws Exception {
	String compileCommandTemplateFile =
	    ((StringToken)
	      ((Parameter)generatorAttribute
	       .getAttribute("compileCommandTemplateFile"))
	     .getToken()).stringValue();
	return substitute(compileCommandTemplateFile, generatorAttribute);
    }

    /** Create a new instance of this application, passing it the
     *  command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
	try {
	    new GenerateCode(args);
        } catch (Exception ex) {
            MessageHandler.error("Command failed", ex);
            System.exit(0);
        }
        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }

    }

    /** Given a string and a Map containing String key/value pairs,
     *  substitute any keys found in the input with the corresponding
     *  values.
     *
     *  @param input The input string that contains substrings
     *  like "@codeBase@".
     *  @param substituteMap The Map of String keys like "@codeBase@"
     *  and String values like "../../..".
     *  @return  A string with the keys properly substituted with
     *  their corresponding values.
     */
    public static String substitute(String input,
            Map substituteMap) {

	// At first glance it would appear that we could use StringTokenizer
	// however, the token is really the String @codeBase@, not
	// the @ character.  StringTokenizer has problems with
	// "@codebase", which reports as having one token, but
	// should not be substituted since it is not "@codebase@"

	Iterator keys = substituteMap.keySet().iterator();

	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    input = StringUtilities.substitute(input, key,
                    (String)substituteMap.get(key));
	}
	return input;
    }

    /** Read in the contents of inputFileName, and for each Parameter
     *  in namedObj, search for strings like
     *  <code>@<i>ParameterName</i></code>, substitute in the value of the
     *  Parameter and return the results.
     *  @param inputFileName  The name of the file to read from.
     *  @param namedObj The NamedObj that conains Parameters to
     *  be searched for in inputFileName.
     *  @return The contents of inputFileName after doing the substitutions
     */
    public static String substitute(String inputFileName,
				    NamedObj namedObj)
	throws FileNotFoundException, IOException {

	Map substituteMap = new HashMap(); 
	Iterator attributes = namedObj.attributeList().iterator();
	while(attributes.hasNext()) {
	    Attribute attribute = (Attribute)attributes.next();
	    if (attribute instanceof Parameter) {
		Parameter parameter = (Parameter)attribute;
		try {
		    substituteMap.put("@" + parameter.getName() + "@",
					  parameter.getToken().toString());
		} catch (Exception ex) {
		    throw new IOException("Problem with '" 
					  + parameter.getName() + "': '"
					  + parameter.getExpression() + "': "
					  + ex);
		}
	    }
	}

	URL inputFileURL =
	    Thread.currentThread().getContextClassLoader()
	    .getResource(inputFileName);
	if (inputFileURL == null) {
	    throw new FileNotFoundException("Failed to find '"
					    + inputFileName
					    + "' as a resource");
	}
	BufferedReader inputReader =
	    new BufferedReader(new InputStreamReader(
			     inputFileURL.openStream()));
	String inputLine;
	StringBuffer output = new StringBuffer();
	while ( (inputLine = inputReader.readLine()) != null) {
	    output.append(substitute(inputLine, substituteMap));
 	}
	inputReader.close();
	return output.toString();
    }

    /** Read in the contents of inputFileName, and replace each matching
     *	String key found in substituteMap with the corresponding String value
     *  and write the results to outputFileName.
     *  @param inputFileName  The name of the file to read from.
     *  @param substituteMap The Map of String keys like "@codeBase@"
     *  and String values like "../../..".
     *  @param outputFileName The name of the file to write to.
     */
    public static void substitute(String inputFileName,
            Map substituteMap,
            String outputFileName)
            throws FileNotFoundException, IOException {
	BufferedReader inputFile =
	    new BufferedReader(new FileReader(inputFileName));
	PrintWriter outputFile =
	    new PrintWriter(new BufferedWriter(new FileWriter(outputFileName)));
	String inputLine;
	while ( (inputLine = inputFile.readLine()) != null) {
	    outputFile.println(substitute(inputLine, substituteMap));
 	}
	inputFile.close();
	outputFile.close();
    }



    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Parse a command-line argument.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    protected boolean _parseArg(String arg) throws Exception {
        if (arg.equals("-help")) {
            System.out.println(_usage());
            System.out.println(_help());
            // NOTE: This means the test suites cannot test -help
            System.exit(0);
        } else if (arg.equals("-test")) {
            _test = true;
        } else if (arg.equals("-verbose")) {
	    _verbose = true;
        } else if (arg.equals("-version")) {
            System.out.println("Version 1.0, Build $Id$");
            // NOTE: This means the test suites cannot test -version
            System.exit(0);
        } else if (arg.equals("")) {
            // Ignore blank argument.
        } else if (!arg.startsWith("-")) {
	    // Assume the argument is a file name or URL.
	    Parameter sourceClass =
		(Parameter)_generatorAttribute.getAttribute("sourceClass");
	    sourceClass.setExpression("\"" + arg + "\"");

	    // Get the model name from the path.
	    // I suppose we could parse the model and get it that way?
	    String modelName = arg.substring(arg.lastIndexOf('/')+1);

	    if (modelName.endsWith(".xml")) {
		modelName = modelName.substring(0, modelName.length() - 4);
	    }
	    if (modelName.endsWith(".moml")) {
		modelName = modelName.substring(0, modelName.length() - 5);
	    }

	    Parameter model =
		(Parameter)_generatorAttribute.getAttribute("model");
	    model.setExpression("\"" + modelName + "\"");
        }
        return true;
    }

    /** Parse the command-line arguments.
     *  @exception Exception If an argument is not understood or triggers
     *   an error.
     */
    protected void _parseArgs(String args[]) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (_parseArg(arg) == false) {
                if (arg.trim().startsWith("-")) {
                    if (i >= args.length - 1) {
                        throw new IllegalActionException("Cannot set " +
                                "parameter " + arg + " when no value is " +
                                "given.");
                    }
                    // Save in case this is a parameter name and value.
                    _parameterNames.add(arg.substring(1));
                    _parameterValues.add(args[i + 1]);
                    i++;
                } else {
                    // Unrecognized option.
                    throw new IllegalActionException("Unrecognized option: "
                            + arg);
                }
            }
        }

        // Check saved options to see whether any is setting an attribute.
        Iterator names = _parameterNames.iterator();
        Iterator values = _parameterValues.iterator();
        while (names.hasNext() && values.hasNext()) {
            String name = (String)names.next();
            String value = (String)values.next();

            boolean match = false;

	    Attribute attribute = _generatorAttribute.getAttribute(name);
	    if (attribute instanceof Settable) {
		match = true;
		((Settable)attribute).setExpression(value);
		if (attribute instanceof Variable) {
		    // Force evaluation so that listeners
		    // are notified.
		    ((Variable)attribute).getToken();
		}
	    }

            if (!match) {
                // Unrecognized option.
                throw new IllegalActionException("Unrecognized option: " +
                        "No parameter exists with name " + name);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a string containing all the Parameters */
    private static String _help() {
	NamedObj namedObj = new NamedObj();
	try {
	    GeneratorAttribute generatorAttribute =
		new GeneratorAttribute(namedObj, "_helpGeneratorAttribute");
	    return generatorAttribute.toString();
	} catch (Exception ex) {
	    return ex.toString();
	}
    }

    /** Return a string containing the usage */
    private static String _usage() {
	return "Usage:\n"
		     + "  java -classpath $PTII ptolemy.copernicus.kernel.GenerateCode -help\n"
		     + "  java -classpath $PTII ptolemy.copernicus.kernel.GenerateCode [java|applet|c|jhdl|shallow] foo.xml\n"
		     + "  For example:\n"
		     + "  java -classpath $PTII ptolemy.copernicus.kernel.GenerateCode java foo.xml -java c:/jdk1.3.1/bin/java\n";
    } 

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The command-line options that are either present or not. */
    protected String _commandFlags[] = {
        "-help",
        "-test",
	"-verbose",
        "-version",
    };

    /** The command-line options that take arguments. */
    protected String _commandOptions[][] = {
        {"-class",  "<classname>"},
        {"-<parameter name>", "<parameter value>"},
    };

    /** The form of the command line. */
    protected String _commandTemplate =
	"generate [options . . .] [relative xml filename] ";

    /** If true, then auto exit after a few seconds. */
    protected static boolean _test = false;

    /** If true, then print debugging information. */
    protected boolean _verbose = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Private class that reads a stream in a thread and prints
    // to another stream
    private class _StreamReaderThread extends Thread {

	_StreamReaderThread(InputStream inputStream, PrintStream stream) {
	    _inputStream = inputStream;
	    _stream = stream;
	}

	// Read lines from the _inputStream and output them to the
	// _stream
	public void run() {
	    try {
		InputStreamReader inputStreamReader =
		    new InputStreamReader(_inputStream);
                BufferedReader bufferedReader =
                    new BufferedReader(inputStreamReader);
                String line = null;
                while ( (line = bufferedReader.readLine()) != null)
		    _stream.println(line);
	    } catch (IOException ioe) {
		System.err.println("IOException: " + ioe);
	    }
	}

	// Stream to read from.
	private InputStream _inputStream;
	// Stream to write to. Usually System.out or System.err
	private PrintStream _stream;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // GeneratorAttribute that contains Parameters that
    // control code generation.
    private GeneratorAttribute _generatorAttribute;

    // List of parameter names seen on the command line.
    private List _parameterNames = new LinkedList();

    // List of parameter values seen on the command line.
    private List _parameterValues = new LinkedList();

    // The value of the ptolemy.ptII.dir property.
    private String _ptIIDirectory;


}
