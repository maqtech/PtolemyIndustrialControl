/* An attribute that stores the configuration of a code generator

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.copernicus.kernel;

import ptolemy.actor.gui.MoMLApplication;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.*;
import ptolemy.moml.Documentation;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;



//////////////////////////////////////////////////////////////////////////
//// GeneratorAttribute
/**
This is an attribute that stores the configuration of a code generator.

<p>The initial default parameters, their values and their documentation
are read in from a MoML file specified by the <i>initialParametersURL</i>.
Having the parameters defined in a MoML file allows us to easily
add and modify parameters without lots of bookkeeping.

<p>To view the initial default parameters, either call toString(), or
run:
<pre>
java -classpath $PTII ptolemy.copernicus.kernel.Copernicus -help
</pre>

@author Edward A. Lee, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class GeneratorAttribute extends SingletonAttribute implements ChangeListener{

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public GeneratorAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

	_attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>"
                + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");

	System.out.println("GeneratorAttribute(" + container + " " + name);
	// Read in the initialParameters file.
        initialParametersURL =
	    new Parameter(this, "initialParametersURL",
			  new StringToken("ptolemy/copernicus/kernel/Generator.xml"));
        Documentation doc = new Documentation(initialParametersURL, "tooltip");
        doc.setValue("MoML File that contains the initial parameter values.");

	URL initialParameters = 
	    getClass().getClassLoader()
	    .getResource(((StringToken)initialParametersURL.getToken())
			 .stringValue());
	if (initialParameters == null) {
	    throw new IllegalActionException(this, "Failed to find the " 
					     + "value of the "
					     + "initialParametersURL: '"
					     + initialParametersURL
					     .getExpression()
					     + "'");
	}
		    

	try {
	    BufferedReader inputReader = new BufferedReader(
                                          new InputStreamReader(
                                          initialParameters.openStream()));

	    String inputLine;
	    StringBuffer buffer = new StringBuffer();
	    while ((inputLine = inputReader.readLine()) != null) {
		buffer.append(inputLine + "\n" );
	    }
	    inputReader.close();
	    addChangeListener(this);
  	    try {
  		requestChange(new MoMLChangeRequest(this, this,
  						buffer.toString()));
  	    } catch (Exception ex) {
  		throw new IllegalActionException(this, ex, "Failed to parse " 
  						 + buffer.toString());
  	    }
	} catch (Exception ex) {
	    throw new IllegalActionException(this, ex, "Failed to parse '" 
					     + initialParametersURL
					     .getExpression()
					     + "'");
	}
	sanityCheckAndUpdateParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Parameters                        ////


    /** MoML file that contains other parameters.  The default value
     *  is the string "ptolemy/copernicus/kernel/Generator.xml".
     */
    public Parameter initialParametersURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void changeExecuted(ChangeRequest change) {
	System.out.println("changeExecuted: " + change);
    }

    public void changeFailed(ChangeRequest change, final Exception exception) {
	System.out.println("changeFailed: " + change + " " + exception);
    }

    /** Return a String representation of this object. */
    public String toString() {
	// We use reflection here so that we don't have to edit
	// this method every time we add a field.
	StringBuffer results = new StringBuffer();
	Iterator attributes = attributeList().iterator();
	while(attributes.hasNext()) {
	    Attribute attribute = (Attribute)attributes.next();
	    if (attribute instanceof Parameter) {
		StringBuffer value = new StringBuffer("\n Value:         ");
		try {
		    value.append(((Parameter)attribute).getToken());
		} catch (Exception ex) {
		    value.append(ex);
		}
		
		results.append("Parameter:      " + attribute.getName()
			       + "\n Expression:    "
			       + ((Parameter)attribute).getExpression()
			       + value.toString()
			       );
	    } else {
		results.append("Attribute:      " + attribute.getName());
	    }
            Attribute tooltipAttribute =
                ((NamedObj)attribute).getAttribute("tooltip");
            if (tooltipAttribute != null
                    && tooltipAttribute instanceof Documentation) {
		results.append("\n Documentation: " 
			       + ((Documentation)tooltipAttribute).getValue());
            } else {
                String tip = Documentation.consolidate((NamedObj)attribute);
                if (tip != null) {
                    results.append("\n Documentation: " + tip);
                }
	    }
	    results.append("\n\n");
	    
	}
	return results.toString();
    }

    public void sanityCheckAndUpdateParameters()
	throws IllegalActionException {
	return;
    }
    /** Sanity check the parameters and update them as necessary */
    public void foosanityCheckAndUpdateParameters()
	throws IllegalActionException {

	// Get the modelPath and update modelPath and model.
	String modelPath = 
	    ((StringToken)
	     ((Parameter)getAttribute("modelPath"))
	     .getToken()).stringValue();

	// FIXME: This means we are always parsing the default model
	// modelPath, every time we construct a GeneratorAttribute, 
	// even if we then go and change it immeadiately.
	updateModelPathAndModel(modelPath);

	String root =
	    ((StringToken)
	     ((Parameter)getAttribute("root"))
	     .getToken()).stringValue();

	// Check that we will be able to write to the value of
	// the root Parameter
	File rootDirectory = new File(root);
	if (!rootDirectory.isDirectory() 
	    || !rootDirectory.canWrite()) {
	    // It would be nice to tell the user we are changing the root
	    // directory of the build.  Usually root is $PTII or
	    // ptolemy.ptII.dir 

	    // Get user.dir and create a ptII/cg subdir if necessary
            String userDirectory = 
		UtilityFunctions.getProperty("user.dir");
	    if (userDirectory != null) {
		rootDirectory =
		    new File(userDirectory + "/ptII/cg");
		if (!rootDirectory.isDirectory()) {
		    // No need to check the return value here,
		    // we do it later anyway
		    rootDirectory.mkdirs();
		}
		if (!rootDirectory.isDirectory() 
		    || !rootDirectory.canWrite()) {
		    throw new IllegalActionException("'" + root
						     + "' was not a "
						     + "writable directory, "
						     + "so we tried '"
						     + rootDirectory
						     + "', but we failed to "
						     + "make a writable"
						     + "directory?");
		} else {
		    root = rootDirectory.getPath();
		    ((Parameter)getAttribute("root"))
			.setExpression("property(\"user.dir\") + "
				       + "\"/ptII/cg\"");
		}
	    }
	}

	// targetPath depends on the value of targetPackage
	// FIXME: Variables should be visible in the UI, but not editable.
	String targetPackage =
	    ((StringToken)
	     ((Parameter)getAttribute("targetPackage"))
	     .getToken()).stringValue();

	String targetPath = StringUtilities.substitute(targetPackage,
						       ".", "/");

	((Variable)getAttribute("targetPath"))
	     .setExpression("\"" + targetPath + "\"");

	// Check that root + targetPath is writable.
	// targetPath depends on root, so we should mess with root first.

	File targetPathFile = new File(root, targetPath);
	if (!targetPathFile.isDirectory() 
	    || !targetPathFile.canWrite()) {
	    // Make any directories
	    if (!targetPathFile.mkdirs()) {
		    throw new
			IllegalActionException("'" + targetPathFile
					       + "' was not a "
					       + "writable directory, and "
					       + "mkdirs() failed");
	    }
	}
    }

    /** Update the modelPath and model parameters in the GeneratorAttribute.
     *  @param modelPathOrURL The file pathname or URL to the model.
     */
    public void updateModelPathAndModel(String modelPathOrURL)
    throws IllegalActionException {
	URL modelURL;
	try {
	    modelURL = MoMLApplication.specToURL(modelPathOrURL);
	} catch (IOException ex) {
	    throw new IllegalActionException(this, ex,
					     "Failed to parse '"
					     + modelPathOrURL + "'");
	}

	// Parse the model and get the name of the model.
	try {
	    MoMLParser parser = new MoMLParser();
	    NamedObj topLevel = null;
	    try {
		topLevel = parser.parse(null, modelURL);
		// FIXME: 1st arg of parse() could be $PTII as a URL.
		modelPathOrURL = modelURL.toExternalForm();
	    } catch (FileNotFoundException ex) {
		try {
		    // Might be under Web Start, try it this way.
		    URL anotherURL =
			MoMLApplication.jarURLEntryResource(modelPathOrURL);
		    if (anotherURL != null) {
			topLevel = parser.parse(null, anotherURL);
			modelPathOrURL = anotherURL.toExternalForm();
		    } else {
			throw new Exception("1. Failed to find '"
					    + modelURL.toExternalForm()
					    + "'\n"
					    + "2. Failed to find '"
					    + anotherURL
					    + "'");
		    }
		} catch (Exception ex1) {
		    throw new IllegalActionException(this, ex1, 
					"Failed to parse '"
					+ modelPathOrURL + "'"
					+ " Tried loading as a resource, too!"
					);
		}
	    }

	    Parameter modelPath = (Parameter)getAttribute("modelPath");
	    modelPath.setExpression("\"" + modelPathOrURL + "\"");

	    // Strip off the leading '.' and then sanitize.
	    String modelName = 
		StringUtilities
		.sanitizeName(topLevel.getFullName().substring(1));

	    Parameter model = (Parameter)getAttribute("model");
	    model.setExpression("\"" + modelName + "\"");
	} catch (Exception ex) {
	    throw new IllegalActionException(this, ex,
					     "Failed to parse '"
					     + modelPathOrURL + "'");
	}
    }
}
