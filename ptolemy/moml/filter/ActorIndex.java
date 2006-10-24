/* Generate an index of actors.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.moml.filter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ptolemy.kernel.util.InternalErrorException;
import ptolemy.moml.MoMLParser;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// ActorIndex

/** Generate actor/demo index files.
 For each actor that is listed in a file, generate a html file that
 lists the models in which the actor appears.

 <p>For details, see $PTII/vergil/actor/docViewerHelp.htm
 
 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ActorIndex {
    /** Generate the index files.
     *  @param classesFileName The name of the file that contains the
     *  dot separated class names - one class per line.
     *  This file is usually called allActors.txt, which is generated by
     *  running the $PTII/doc/doclets/PtDoc doclet.  For example, see
     *  $PTII/doc/codeDoc/allActors.txt
     *  @param modelsFileName The name of the file that contains the
     *  urls that point to the models to be parsed, one model
     *  per line.  This file is usually called models.txt and is generated by 
     *  running ptolemy.actor.gui.HTMLAbout.
     *  @param outputDirectory Directory in which to write index files.
     *  The files generated have the name <i>actorName<i>idx.htm, where
     *  <i>actorName</i> is the name of the actor.  Typically, the value
     *  for this parameter is doc/codeDoc.
     *  @exception Exception If there is a problem reading or writing
     *  a file.
     */
    public static void generateActorIndex(String classesFileName,
            String modelsFileName, String outputDirectory) throws Exception {
        // The class name is the key, a set of models is the value.
        HashMap classesToBeIndexed = new HashMap();
        BufferedReader classesReader = null;
        BufferedReader modelReader = null;
        try {
            // Read classesFileName and populate the classes Set
            classesReader = new BufferedReader(new FileReader(classesFileName));
            String className;
            while ((className = classesReader.readLine()) != null) {
                System.out.println("Going to index " + className);
                classesToBeIndexed.put(className, new HashSet());
            }

            // Read modelsFileName and parse each model, looking
            // for classes in which we are interested.
            modelReader = new BufferedReader(new FileReader(modelsFileName));
            String modelName;
            MoMLParser parser = new MoMLParser();

            // Handle backward compatibility issues
            MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());

            // Add a MoML filter that updates the values of classesToBeIndexed
            // with models that contain classes named by the key of
            // classesToBeIndexed
            NamedObjClassesSeen namedObjClassesSeen = new NamedObjClassesSeen(
                    classesToBeIndexed);
            MoMLParser.addMoMLFilter(namedObjClassesSeen);

            // Add a filter to remove the graphical classes
            // This is safe to do because our NamedObjClassesSeen filter
            // runs before we then remove the graphical classes.
            RemoveGraphicalClasses removeGraphicalClasses = new RemoveGraphicalClasses();
            removeGraphicalClasses.setRemoveGR(true);
            MoMLParser.addMoMLFilter(removeGraphicalClasses);

            while ((modelName = modelReader.readLine()) != null) {
                // Reset the list of classes seen, read the model
                // The filter updates the classesToBeIndexed hashMap
                namedObjClassesSeen.reset(modelName);
                //URL modelURL = new File(modelName).toURL();
                try {
                    URL modelURL = FileUtilities.nameToURL(modelName, null,
                            null);
                    System.out.println("Parsing: " + modelURL);
                    parser.reset();
                    parser.parse(null, modelURL);
                } catch (Exception ex) {
                    System.err.println("Warning, failed to parse " + modelName);
                    ex.printStackTrace();
                }
            }

        } finally {
            if (classesReader != null) {
                try {
                    classesReader.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
            if (modelReader != null) {
                try {
                    modelReader.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }

        // Write the output files.
        Iterator classes = classesToBeIndexed.entrySet().iterator();
        while (classes.hasNext()) {
            Map.Entry entry = (Map.Entry) classes.next();
            String actorClassName = (String) entry.getKey();
            if (((Set) entry.getValue()).size() == 0) {
                // Skip classes that are not used in a demo
                // ptolemy.vergil.actor.DocManager checks to see if 
                // the Idx.htm file exists before creating a link to it.
                continue;
            }
            BufferedWriter writer = null;
            try {
                String outputFileName = outputDirectory + File.separator
                        + actorClassName.replace('.', File.separatorChar)
                        + "Idx.htm";

                // Determine the relative path to $PTII from this
                // file.  We need this so that we can link to the models.
                String canonicalOutputFileName = new File(outputFileName)
                        .getCanonicalPath().replace('\\', '/');

                // Get PTII as C:/cxh/ptII
                String ptII = null;
                try {
                    ptII = new URI(StringUtilities
                            .getProperty("ptolemy.ptII.dirAsURL")).normalize()
                            .getPath();
                    // Under Windows, convert /C:/foo/bar to C:/foo/bar
                    ptII = new File(ptII).getCanonicalPath().replace('\\', '/');
                } catch (URISyntaxException ex) {
                    throw new InternalErrorException(null, ex,
                            "Failed to process PTII " + ptII);
                }
                if (ptII.length() == 0) {
                    throw new InternalErrorException("Failed to process "
                            + "ptolemy.ptII.dirAsURL property, ptII = null?");
                }

                String relativePath = "";
                if (canonicalOutputFileName.startsWith(ptII)) {
                    // If the canonical output file name starts with ptII
                    // we then generate a relative path
                    String relativeOutputFileName = StringUtilities.substitute(
                            canonicalOutputFileName, ptII, "");
                    StringBuffer relativePathBuffer = new StringBuffer();
                    int index = 0;
                    while (relativeOutputFileName.indexOf('/', index) != -1) {
                        index = relativeOutputFileName.indexOf('/', index) + 1;
                        relativePathBuffer.append("../");
                    }
                    relativePath = relativePathBuffer.toString();
                    // Strip off the last ../
                    relativePath = relativePath.substring(0, relativePath
                            .length() - 3);
                }

                // Make directories if necessary
                File outputDirectoryFile = new File(new File(outputFileName)
                        .getParent());
                if (!outputDirectoryFile.exists()) {
                    System.out.println("Creating " + outputDirectoryFile);
                    outputDirectoryFile.mkdirs();
                }

                System.out.println("Writing " + outputFileName);
                writer = new BufferedWriter(new FileWriter(outputFileName));

                writer.write("<html>\n<head>\n<title>Index for "
                        + actorClassName + "</title>\n" + "<link href=\""
                        + relativePath + "doc/default.css\""
                        + "rel=\"stylesheet\" type=\"text/css\">\n"
                        + "</head>\n<body>\n" + "<h2>" + actorClassName
                        + "</h2>\n"
                        + "Below are demonstration models that use "
                        + actorClassName + "\n<ul>\n");

                // Loop through all the models that use this actor
                Iterator models = ((Set) entry.getValue()).iterator();
                while (models.hasNext()) {
                    String model = (String) models.next();
                    if (model.startsWith("$CLASSPATH")) {
                        model = model.substring(11);
                    }
                    writer.write("<li><a href=\"" + relativePath + model
                            + "\">" + model + "</a>\n");
                }
                writer.write("</ul>\n</body>\n</html>\n");
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    /** Generate index documentation.  The three arguments are passed to
     *  {@link #generateActorIndex(String, String, String)}.   
     *  <pre>
     *  java -classpath "$PTII;$PTII/lib/diva.jar" ptolemy.moml.filter.ActorIndex allActors.txt models.txt doc/codeDoc
     *  </pre>
     *  @param args An array of three Strings
     *  <br> The name of the file that lists all the actors in which we are
     *  interested.
     *  <br> The name of the file that lists all the models to be indexed.
     *  <br> The directory in which to write the index files.
     *  @exception Exception If there is a problem reading or writing
     *  a file.
     */
    public static void main(String[] args) throws Exception {
        ActorIndex.generateActorIndex(args[0], args[1], args[2]);
    }
}
