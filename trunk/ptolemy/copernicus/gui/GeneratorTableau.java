/* A tableau for controlling code generation.

 Copyright (c) 2000-2002 The Regents of the University of California.
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

package ptolemy.copernicus.gui;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.copernicus.kernel.Copernicus;
import ptolemy.copernicus.kernel.GeneratorAttribute;
//import ptolemy.copernicus.c.Main;
//import ptolemy.copernicus.java.Main;
//import ptolemy.copernicus.jhdl.Main;

import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.gui.JTextAreaExec;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.SwingWorker;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// GeneratorTableau
/**
A tableau that creates a new control panel for code generation.

@author Shuvra Bhattacharyya, Edward A. Lee, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class GeneratorTableau extends Tableau {

    /** Create a new control panel for code generation.
     *  The tableau is itself an entity contained by the effigy
     *  and having the specified name.  The frame is not made visible
     *  automatically.  You must call show() to make it visible.
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public GeneratorTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
        NamedObj model = container.getModel();

        if (model instanceof CompositeEntity) {
            GeneratorFrame frame = new GeneratorFrame(
                    (CompositeEntity)model, this);
	    setFrame(frame);
            frame.setBackground(BACKGROUND_COLOR);
        } else {
            throw
		new IllegalActionException(model,
                        "Can only generate code for "
                        + "instances of CompositeEntity.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Default background color is a light grey.
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    // The .java file should be created in this package.
    private static String _packageName = new String("");

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of GeneratorTableau.
     */
    public class GeneratorFrame extends PtolemyFrame {

	/** Construct a frame to control code generation for
         *  the specified Ptolemy II model.
	 *  After constructing this, it is necessary
	 *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.
	 *  @param model The model to put in this frame, or null if none.
         *  @param tableau The tableau responsible for this frame.
         *  @exception IllegalActionException If the model rejects the
         *   configuration attribute.
         *  @exception NameDuplicationException If a name collision occurs.
	 */
	public GeneratorFrame(final CompositeEntity model, Tableau tableau)
                throws IllegalActionException, NameDuplicationException {
	    super(model, tableau);
            JPanel component = new JPanel();
            component.setLayout(new BoxLayout(component, BoxLayout.X_AXIS));

	    
            // Caveats panel.
            JPanel caveatsPanel = new JPanel();
            caveatsPanel.setBorder(
                    BorderFactory.createEmptyBorder(5, 0, 0, 0));
            caveatsPanel.setLayout(new BoxLayout(caveatsPanel, BoxLayout.X_AXIS));
            JTextArea messageArea = new JTextArea(
                    "NOTE: This is a highly preliminary "
                    + "code generator facility, with many "
                    + "limitations.  It is best viewed as "
                    + "a concept demonstration.", 2, 10);
            messageArea.setEditable(false);
            messageArea.setBorder(BorderFactory.createEtchedBorder());
	    messageArea.setLineWrap(true);
	    messageArea.setWrapStyleWord(true);
            caveatsPanel.add(messageArea);

            JButton moreInfoButton = new JButton("More Info");
            moreInfoButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        Configuration configuration = getConfiguration();
                        URL infoURL =
                            getClass().getResource("../../../doc/codegen.htm");
                        try {
                            configuration.openModel(
                                    null, infoURL, infoURL.toExternalForm());
                        } catch (Exception ex) {
                            throw new InternalErrorException(model, ex,
                                    "Failed to open doc/codegen.htm: ");

                        }
                    }
                });
            caveatsPanel.add(moreInfoButton);

            JPanel left = new JPanel();
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

            //component.add(caveatsPanel);
            left.add(caveatsPanel);

            // Panel for push buttons.
            JPanel buttonPanel = new JPanel();
            //buttonPanel.setLayout(new GridLayout(7, 1));
            buttonPanel.setLayout(new GridLayout(1, 4));
            //buttonPanel.setBorder(
            //        BorderFactory.createEmptyBorder(2, 0, 2, 0));

            // Button panel first.
            JButton parametersButton = new JButton("Parameters");
            parametersButton
		.setToolTipText("Display a summary of the Parameters");
            buttonPanel.add(parametersButton);

            //buttonPanel.add(Box.createVerticalStrut(5));

            JButton goButton = new JButton("Generate");
            goButton.setToolTipText("Generate code");
            buttonPanel.add(goButton);

            //buttonPanel.add(Box.createVerticalStrut(5));

            JButton stopButton = new JButton("Cancel");
            stopButton.setToolTipText("Terminate executing processes");
            buttonPanel.add(stopButton);

            //buttonPanel.add(Box.createVerticalStrut(5));
            JButton clearButton = new JButton("Clear");
            clearButton.setToolTipText("Clear Log");
            buttonPanel.add(clearButton);

            //controlPanel.add(buttonPanel);
	    left.add(buttonPanel);

            // Add space right of the buttons
            //controlPanel.add(Box.createHorizontalStrut(20));

            // Next, put in a panel to configure the code generator.
            // If the model contains an attribute with tableau
            // configuration information, use that.  Otherwise, make one.
            GeneratorAttribute attribute =
                (GeneratorAttribute)
                model.getAttribute("_generator",
                        GeneratorAttribute.class);
            if (attribute == null) {
                attribute = new GeneratorAttribute(
                        model, "_generator");
            }

	    // Adjust parameters accordingly.
	    //Parameter modelName = (Parameter)attribute.getAttribute("model");
	    //modelName.setExpression(StringUtilities
	    //			    .sanitizeName(model.getName()));


            Configurer configurer = new Configurer(attribute);
            final GeneratorAttribute options = attribute;

            JPanel controlPanel = new JPanel();
            controlPanel.add(configurer);
	    JScrollPane scrollPane = new JScrollPane(controlPanel);
	    left.add(scrollPane, BorderLayout.CENTER);
            //component.add(controlPanel);
	    //left.add(controlPanel);

            // Add space under the control panel.
	    //component.add(Box.createVerticalStrut(10));
	    component.add(left);

	    // Create a JTextAreaExec without Start and Cancel buttons.
	    final JTextAreaExec exec =
		new JTextAreaExec("Code Generator Commands", false);
	    component.add(exec);

            getContentPane().add(component, BorderLayout.CENTER);

            parametersButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
			exec.appendJTextArea(options.toString());
		    }
		});

            stopButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
			exec.cancel();
                    }
                });

            clearButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
			exec.clear();
                    }
                });

            goButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        try {
			    // True if we should run jode, jad or javap.
			    boolean decompile = false;
			    boolean compile =
				((BooleanToken)
				 ((Parameter)options.getAttribute("compile"))
				 .getToken())
				.booleanValue();
			    boolean show =
				((BooleanToken)
				 ((Parameter)options.getAttribute("show"))
				 .getToken())
				.booleanValue();
			    boolean run =
				((BooleanToken)
				 ((Parameter)options.getAttribute("run"))
				 .getToken())
				.booleanValue();

			    // The code generator to run.  The value of this
			    // parameter should name a subdirectory of
			    // ptolemy/copernnicus such as "java" or "shallow".
			    String codeGenerator = 
				((StringToken)
				 ((Parameter)options
				  .getAttribute("codeGenerator"))
				 .getToken()).stringValue();

			    // Convert "java" to java.
			    //codeGenerator =
			    //	codeGenerator.substring(1, codeGenerator
			    //				.length() - 1);
								    
			    String targetPath =
				((StringToken)
				 ((Parameter)options
				  .getAttribute("targetPath"))
				 .getToken()).stringValue();

			    String root =
				((StringToken)
				 ((Parameter)options
				  .getAttribute("root"))
				 .getToken()).stringValue();

			    //targetPath =
				//targetPath.substring(1, targetPath
			    // .length() - 1);

			    // Check that we will be able to write
			    File directory = new File(root, targetPath);
			    if (!directory.isDirectory()) {
				throw new IllegalActionException(model,
						 "Not a directory: "
						 + root + "/" + targetPath);
			    }
			    if (!directory.canWrite()) {
				throw new IllegalActionException(model,
						 "Can't write: "
						 + root + "/" + targetPath);
			    }

			    // Commands that we will eventually execute,
			    // depending on compile, show and run.
			    List execCommands = new LinkedList();

			    // Commands that are generated by Copernicus
			    // that may or not be executed.  For example,
			    // we may end up not compile, but using an
			    // earlier compilation and running.
			    List commands = null;

			    if (codeGenerator.equals("applet")) {
				// Soot is a memory pig, so we run
				// it in a separate process.
				try {
				    commands =
					_generateCodeGeneratorCommands(model,
								       options,
								       "applet"
								       );
				} catch (Exception ex) {
				    throw new IllegalActionException(model, ex, null);
				}
			    } else if (codeGenerator.equals("c")
				       && compile) {
				exec.updateStatusBar("Starting c "
                                        + "code generation");
				// FIXME: How come the status bar
				// does not get updated?
				ptolemy.copernicus.c
				    .Main.generate((CompositeActor)model,
                                            targetPath);
				exec.updateStatusBar("C Code generation "
                                        + "complete.");
				decompile = true;

			    } else if (codeGenerator.equals("java")) {
				// Soot is a memory pig, so we run
				// it in a separate process.
				try {
				    commands =
					_generateCodeGeneratorCommands(model,
								       options,
								       "java");
                                    decompile = true;
				} catch (Exception ex) {
				    throw new IllegalActionException(model,
								     ex, null);
				}
			    } else if (codeGenerator.equals("jhdl")
				       && compile) {
				exec.updateStatusBar("Starting jhdl "
                                        + "code generation");
				// FIXME: How come the status bar
				// does not get updated?
				ptolemy.copernicus.jhdl
				    .Main.generate((CompositeActor)model,
                                            targetPath);
				exec.updateStatusBar("JHDL Code generation "
                                        + "complete.");


			    } else if (codeGenerator.equals("shallow")) {
				// Soot is a memory pig, so we run
				// it in a separate process.
				try {
				    commands =
					_generateCodeGeneratorCommands(model,
								       options,
							              "shallow"
								       );
				} catch (Exception ex) {
				    throw new IllegalActionException(model,
								     ex, null);
				}
				decompile = true;
			    } else {
				throw new IllegalActionException(model,
					 "Don't know about '"
					 + codeGenerator + "'.  Try 'java'");
			    }


			    if (compile && commands != null) {
				execCommands.add(commands.get(0));
			    } 

			    if (show && decompile) {
				String targetPackage =
				    ((StringToken)
				     ((Parameter)options
				      .getAttribute("targetPackage"))
				     .getToken()).stringValue();

				//targetPackage =
				//    targetPackage.substring(1, targetPackage
				// .length() - 1);

				String className = targetPackage;
				if (codeGenerator.equals("java")) {
				    className = className + ".Main";
				} else {
				    if (className.length() > 0
                                        && ! className.endsWith(".") ) {
					className = className + '.'
					    + "CG" + model.getName();
				    } else {
					className = "CG" + model.getName();
				    }
				}

				String classPath =
				    ((StringToken)
				     ((Parameter)options
				      .getAttribute("classPath"))
				     .getToken()).stringValue();

				execCommands.add("javap "
                                        + "-classpath \"" 
					+ classPath	 
                                        + "\" "
                                        + className);
			    }

                            if (run && commands != null) {
				System.out.println("GeneratorTableau: run"
					       + commands.get(1));
				execCommands.add(commands.get(1));
                            }

                            if (execCommands.size() > 0) {
				System.out.println("GeneratorTableau: execCommand"
					       + execCommands.size());

				exec.setCommands(execCommands);
				exec.start();
                            }
                        } catch (Exception ex) {
                            MessageHandler.error("Code generation failed.",
                                    ex);
                        }
                    }
                });
	}
    }

    /** A factory that creates a control panel for code generation.
     */
    public static class Factory extends TableauFactory {

	/** Create an factory with the given name and container.
	 *  @param container The container entity.
	 *  @param name The name of the entity.
	 *  @exception IllegalActionException If the container is incompatible
	 *   with this attribute.
	 *  @exception NameDuplicationException If the name coincides with
	 *   an attribute already in the container.
	 */
	public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
	    super(container, name);
	}

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

	/** Create a new instance of GeneratorTableau in the specified
         *  effigy. If the specified effigy is not an
         *  instance of PtolemyEffigy, then do not create a tableau
         *  and return null. It is the responsibility of callers of
         *  this method to check the return value and call show().
         *
	 *  @param effigy The model effigy.
	 *  @return A new control panel tableau if the effigy is
         *    a PtolemyEffigy, or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
	 */
	public Tableau createTableau(Effigy effigy) throws Exception {
	    if (effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a tableau
                GeneratorTableau tableau =
                    (GeneratorTableau)effigy.getEntity("generatorTableau");
                if (tableau == null) {
                    tableau = new GeneratorTableau(
                            (PtolemyEffigy)effigy, "generatorTableau");
                }
		// Don't call show() here, it is called for us in
		// TableauFrame.ViewMenuListener.actionPerformed()
                return tableau;
	    } else {
		return null;
	    }
	}
    }

    // Return a List consisting of the command string that will
    // generate code Java for model and the command string that will
    // run the generated code.
    //
    // @param model The model to generate code for.
    // @param generatorAttribute The GeneratorAttribute that
    // controls the compilation of the model.
    // @param copernicusSubdirectory The directory that contains
    // the generator we are running.  Usually, something like
    // "applet" or "java" or "shallow".
    private List _generateCodeGeneratorCommands(CompositeEntity model,
	    GeneratorAttribute generatorAttribute,
            String copernicusSubdirectory)
            throws IllegalArgumentException, InternalErrorException {
	// Create a temporary file in c:/temp or /tmp.
	File temporaryMoMLFile = null;

	try {
	    temporaryMoMLFile = File.createTempFile("CGTmp", ".xml");
	    temporaryMoMLFile.deleteOnExit();

	    // We write out the model so that we can run the code generator
	    // in a separate process.
	    java.io.FileWriter fileWriter =
		new java.io.FileWriter(temporaryMoMLFile);
	    model.exportMoML(fileWriter);
	    fileWriter.close();
	} catch (IOException io) {
	    throw new InternalErrorException(model, null,
	            "Failed to write model to '"
                    + temporaryMoMLFile + "': " + io);
	}


	try {
	    Copernicus.updateModelPathAndModel(generatorAttribute,
					       temporaryMoMLFile.getPath());
	} catch (IOException ex) {
	    throw new InternalErrorException(model, ex,
					     "Failed to update modelPath "
					     + " or model");
	}

	Parameter codeGenerator =
	    (Parameter)generatorAttribute.getAttribute("codeGenerator");
	codeGenerator.setExpression("\"" + copernicusSubdirectory + "\"");


	List results = new LinkedList();
	try { 
	    results.add(Copernicus.commandToCompile(generatorAttribute));
	    results.add(Copernicus.commandToRun(generatorAttribute));
	} catch (Exception ex) {
	    throw new InternalErrorException(model, ex,
					     "Failed generate command strings");
	}
	return results;
    }
}

