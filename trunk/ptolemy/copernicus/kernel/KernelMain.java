/* Abstract base class that provides common main() functionality
to be used by various backends.

 Copyright (c) 2001 The Regents of the University of California.
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

import soot.ConsoleCompilationListener;
import soot.Main;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.Transform;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLParser;

import com.microstar.xml.XmlException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// Main
/** Abstract base class that provides common main() functionality
to be used by various backends.

The backends should extend this class and create a constructor that
looks like:
<pre>
public class Main extends KernelMain {
    public Main(String [] args) {
	super(args[0]);
    }

    public static void main(String[] args)
	throws IllegalActionException, NameDuplicationException {

	Main main = new Main(args);

	// Parse the model, initialize it and create instance classes
	// for the actors.
	main.initialize();

	// Add Transforms to the Scene.
	main.addTransforms();
	    
	main.generateCode(args);
    }
}
</pre>

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
*/
public class KernelMain {

    /** Set up code generation arguments.
     *  @param momlClassName The name of the top level model or the
     *  .xml file that we are to generate code for.  For example:
     *  "ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom".
     */
    public KernelMain(String momlClassName) {
	_momlClassName = momlClassName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add transforms to the Scene.  Derived classes should do most
     *  of their added functionality in this method.
     */
    public void addTransforms() {

        // A Hack to ignore the class we specify on the command
	// line. This is a soot problem that requires this hack.
	// We will provide soot with java.lang.Object as its
	// only application class in Main. The first transformation
	// will ignore all application classes (i.e. set them to
	// library classes)
        Scene.v().getPack("wjtp").add(new Transform("wjtp.hack",
                new _IgnoreAllApplicationClasses(), ""));
    }

    /** Call soot.Main.main(), which does command line argument
     *  processing and then starts the transformation.  This method
     *  should be called after calling initialize() and addTransforms().
     *
     *  @param args Soot command line arguments to be passed
     *  to soot.Main.main().
     */
    public void generateCode(String [] args) {
        // This is rather ugly.  The moml Class is not a Java class, so
        // soot won't recognize it.  However, if we give soot nothing, then
        // it won't run.  Note that later we will call setLibraryClass() on
        // this class so that we don't actually generate code for it.
        args[0] = "java.lang.Object";

	// Rather than calling soot.Main.main() here directly, which
	// spawns a separate thread, we run this in the same thread
        //soot.Main.main(args);
	soot.Main.setReservedNames();
	soot.Main.setCmdLineArgs(args);
	soot.Main main = new soot.Main();
	soot.ConsoleCompilationListener consoleCompilationListener =
	    new soot.ConsoleCompilationListener();
	soot.Main.addCompilationListener(consoleCompilationListener);
	// Thread thread = new Thread(main);
	// thread.start();
	main.run();
    }

    /** Read in a MoML class, sanitize the top level name,
     *  initialize the model.  Usually initialize() is called after
     *  calling readInModel().
     *  @param toplevel The model we are generating code for.
     */
    public void initialize(CompositeActor toplevel)
            throws IllegalActionException, NameDuplicationException {
	_toplevel = toplevel;
	// If the name of the model is the empty string, change it to
	// the basename of the file.
	if (_toplevel.getName().length() == 0) {
	    String baseName = (new File(_momlClassName)).getName();
	    if (baseName.lastIndexOf('.') != -1) {
		baseName = baseName.substring(0,
					      baseName.lastIndexOf('.'));
	    }
	    _toplevel.setName(baseName);
	}

	// Make the name follow Java initializer naming conventions.
	_toplevel.setName(SootUtilities.sanitizeName(_toplevel.getName()));


        // Temporary hack because cloning doesn't properly clone
        // type constraints.  In some ways, it would make sense
	// to do this in readInModel(), where we already have a MoMLParser
	// object, but we want to be sure the type constraints are cloned
	// if we are passed in a model directly without running readInModel().
        CompositeActor modelClass = null;
	try {
	    modelClass = (CompositeActor)
                _parser._searchForClass(_momlClassName,
                        _toplevel.getMoMLInfo().source);
	} catch (XmlException xml) {
            throw new
                IllegalActionException("Failed to find class '"
                        + _momlClassName + "' in '"
                        + _toplevel.getMoMLInfo().source
                        + "': " + xml);
	}

        if(modelClass != null) {
            _toplevel = modelClass;
        }

        // FIXME: insert code to parse parameters like
        // CompositeActorApplication does.  i.e. --iterations=50
        // These should get parsed and affect the model that was loaded.
        // They will be folded into the generated code during the code 
        // generation process.

        // Initialize the model to ensure type resolution and scheduling
        // are done.
        try {
            Manager manager = new Manager(_toplevel.workspace(), "manager");
            _toplevel.setManager(manager);
            manager.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not initialize "
                    + "composite actor: " + e);
        }
    }


    /** Generate a .class file associated with the top level Ptolemy II
     *  object and all of its descendants in a specific directory.
     *  @param toplevel The root object of the topology to be saved.
     *  @param directoryName The name of the directory to where the .class
     *  file will be created.
     *  @return The generated java code.
     */
    public static void generate(CompositeActor toplevel, String directoryName)
	throws IllegalActionException, NameDuplicationException {
	// FIXME: This name is awfully close to generateCode(), yet
	// this method is a superset of the generateCode functionality.
	String [] args = {
	    toplevel.getName(),
	    "-d", directoryName,
	    "-p", "wjtp.at", "targetPackage:ptolemy.copernicus.java.test.cg",
	    "-p" ,"wjtp.mt", "targetPackage:ptolemy.copernicus.java.test.cg",
	    "-p" ,"wjtp.umr", "disabled"
	};

	KernelMain main = new KernelMain(args[0]);
	main.initialize(toplevel);
	main.addTransforms();
	main.generateCode(args);
    }

    /** Sample main() method that parses a MoML class, initializes
     *  the model and creates actor instances.  In this class,
     *  this method does not do much, it is only a sample.
     *
     *  @param args The first element of the array is the MoML class
     *  name or file name, subsequent optional arguments are Soot
     *  command line options.
     *  <p>The most common option is <code>-d ../../..</code>, which
     *  will store the generated files in ../../..
     *  <p>Another common option is
     *  <code> -p <i>phase-name</i> <i>key1[</i>:<i>value1]</i>,<i>key2[</i>:<i>value2]</i>,<i>...</i>,<i>keyn[</i>:<i>valuen]</i></code>
     *  which will set the run time option <i>key</i> to <i>value</i> for
     *  <i>phase-name</i> (default for <i>value</i> is true)
     *  <p>An example is:<br>
     *  <code>-p wjtp.at deep,targetPackage:ptolemy.copernicus.jhdl.cg</code>
     *  <p>For a complete list of Soot Options, pass in "-h", or run
     *  <code>$PTII/bin/soot -h<code>, or see
     *  <a href="http://www.sable.mcgill.ca/soot/tutorial/usage">http://www.sable.mcgill.ca/soot/tutorial/usage</a>
     *
     *  @exception IllegalActionException If the model cannot be parsed.
     *  @exception NameDuplicationException If the name of the
     *  model cannot be changed to a Java identifier String.
     */
    public static void main(String[] args)
	throws IllegalActionException, NameDuplicationException {
	KernelMain kernelMain = new KernelMain(args[0]);
	CompositeActor toplevel = kernelMain.readInModel(args[0]);
	kernelMain.initialize(toplevel);
	kernelMain.addTransforms();
	kernelMain.generateCode(args);
    }

    /** Read in a MoML class, either as a top level model or
     *  a file, initialize the model, then create instance classes for actors.
     *  <p> The MoML class name is processed as follows:
     *  <ol>
     *  <li> The momlClassName argument is assumed to be a dot
     *  separated top level model name such as
     *  <code>ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom</code>
     *  and inserted into a MoML fragment:
     *  <p><code>
     *  &lt;entity name="ToplevelModel" class=" + momlClassName + "/&gt;
     *  </code>
     *  and then passed to MoMLParser.parse().
     *  <li>If the parse fails, then the name is tried as a
     *  relative MoML file name and passed to MoMLParser.parseFile().
     *  </ol>
     *  @exception IllegalActionException If the model cannot be parsed.
     */
    public CompositeActor readInModel(String momlClassName)
            throws IllegalActionException, NameDuplicationException {

	// readInModel() is a separate method so that we can read
	// in the model and then get its name so that we can 
	// determine the name of the class that will be generated.
	
        // Call the MOML parser on the test file to generate a Ptolemy II
        // model.
	_momlClassName = momlClassName;
       	CompositeActor toplevel = null;
	// First, try it as a top level model
	String source = "<entity name=\"ToplevelModel\""
	    + "class=\"" + momlClassName + "\"/>\n";
        try {
            toplevel = (CompositeActor)_parser.parse(source);

        } catch (Exception exception) {
	    StringBuffer errorMessage = new StringBuffer();
	    errorMessage.append("1. Failed to parse '" + momlClassName
				+ "' as a top level model in\n"
				+ source + "\nException was: "
				+ exception); 
	    try {
		// Then try it as an xml file
		toplevel = (CompositeActor)_parser.parseFile(momlClassName);
	    } catch (Exception exceptionTwo) {
		errorMessage.append("2. Failed to parse '" + momlClassName
				    + "' as an xml file: "
				    + exceptionTwo); 
		try {
		    URL momlURL = new URL(momlClassName); 
		    try {
			// Then try it as a URL file
			toplevel = (CompositeActor)_parser.parse(null,
								 momlURL);
		    } catch (Exception exceptionThree) {
			errorMessage.append("3. Failed to parse '"
					    + momlClassName
					    + "' as a URL '"
					    + momlURL + "': "
					    + exceptionThree
					    + momlClassName
					    + "': " + exceptionThree);
			throw new IllegalActionException(errorMessage
							 .toString());
		    
		    }
		} catch (MalformedURLException malformed) {
		    throw new IllegalActionException(errorMessage + ": "
						     + malformed);
		}
	    }
	    if (toplevel == null) {
		throw new IllegalActionException(errorMessage.toString());
	    }
        }
	return toplevel;
    }


    /** Return the model that we are generating code for.
     */
    public CompositeActor toplevel() {
        return _toplevel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The name of the MoML class, either as a top level model or
     *  as an xml file that we are generating code for.
     */
    protected String _momlClassName;

    /** The CompositeActor we are generating code for.
     */
    protected CompositeActor _toplevel;

    /** The MoMLParser for parsing models.
     */
    protected MoMLParser _parser = new MoMLParser();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    public static class _IgnoreAllApplicationClasses
	extends SceneTransformer {
        /** Transform the Scene according to the information specified
         *  in the model for this transform.
         *  @param phaseName The phase this transform is operating under.
         *  @param options The options to apply.
         */
        protected void internalTransform(String phaseName, Map options) {
            for(Iterator classes =
		    Scene.v().getApplicationClasses().snapshotIterator();
                classes.hasNext();) {
                ((SootClass)classes.next()).setLibraryClass();
            }
        }
    }
}
