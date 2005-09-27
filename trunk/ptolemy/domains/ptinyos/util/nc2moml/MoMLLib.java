package ptolemy.domains.ptinyos.util.nc2moml;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;
import java.util.*;

/** Searches tree files with <input suffix> (e.g., *.moml).  Creates <output filename> (e.g., index.moml) in each dir that contains those files and all parent directories.
  
  Usage:
    MoMLLib <input suffix> <output filename> <root dir of input files>
    
  Example: MoMLLib .moml index.moml /home/celaine/trash/todayoutput2
    
*/
public class MoMLLib {
    /** components is in short path format relative to root
     *    Example: tos/lib/Counters/Counter
     *  indexFiles is in short path format relative to dir of outputFile
     *    Example: subdir/index.moml
     *  outputFile is in long path format
     *    Example: /home/celaine/trash/todayoutput2/tos/lib/Counters/Counter/index.moml
         
<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
     "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="Counters" class="ptolemy.moml.EntityLibrary">
  <configure>
  <?moml
  <group>
  <entity name="Counter" class="tos.lib.Counters.Counter"/>
  <entity name="IntToLeds" class="tos.lib.Counters.IntToLeds"/>
  <entity name="IntToLedsM" class="tos.lib.Counters.IntToLedsM"/>
  <entity name="IntToRfm" class="tos.lib.Counters.IntToRfm"/>
  <entity name="IntToRfmM" class="tos.lib.Counters.IntToRfmM"/>
  <entity name="RfmToInt" class="tos.lib.Counters.RfmToInt"/>
  <entity name="RfmToIntM" class="tos.lib.Counters.RfmToIntM"/>
  <entity name="SenseToInt" class="tos.lib.Counters.SenseToInt"/>
  </group>
  ?>
  </configure>
</entity>
         
     */
    public static void generateIndex(String[] components, String[] indexFiles, String libraryName, String outputFile) {
        
        Element root = new Element("entity");
        root.setAttribute("name", libraryName);
        root.setAttribute("class", "ptolemy.moml.EntityLibrary");

        Element configure = new Element("configure");
        root.addContent(configure);

        DocType plot = new DocType("plot",
                "-//UC Berkeley//DTD MoML 1//EN",
                "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd");
        Document doc = new Document(root, plot);

        Element group = new Element("group");

        // Make entries for index files.
        // Example:
        //     <input source="Counters/index.moml"/>
        for (int i = 0; i< indexFiles.length; i++) {
            Element input = new Element("input");
            input.setAttribute("source", indexFiles[i]);
            group.addContent(input);

        }

        // Make entries for component files.
        for (int i = 0; i < components.length; i++) {
            String c = components[i];
            // FIXME FILESEPARATOR
            String[] subNames = c.split("/");
            String componentName = subNames[subNames.length - 1];

            // FIXME FILESEPARATOR
            String className = c.replaceAll("/", ".");

            Element entity = new Element("entity");
            entity.setAttribute("name", componentName);
            entity.setAttribute("class", className);
            group.addContent(entity);
        }

        
        // Setup format for xml serializer.
        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        Format format = serializer.getFormat();
        format.setOmitEncoding(true);
        format.setLineSeparator("\n");
        serializer.setFormat(format);

        // Create ?moml processing instruction.
        ProcessingInstruction moml = new ProcessingInstruction("moml", "\n" + serializer.outputString(group) + "\n");
        configure.addContent(moml);

        // Generate index file. 
        try {
            FileOutputStream out = null;
            if (outputFile != null) {
                out = new FileOutputStream(outputFile);
            }

            if (out != null) {
                serializer.output(doc, out);
                out.flush();
                out.close();
            } else {
                serializer.output(doc, System.out);
            }
        }
        catch (IOException e) {
            System.err.println(e);
        }
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java MoMLLib <input suffix> <output filename> <root dir of input files>");
            return;
        }

        String inputSuffix = args[0].trim();
        String outputFilename = args[1].trim();
        String rootDir = args[2].trim();

        try {
            MoMLLib.proc(inputSuffix, outputFilename, rootDir, rootDir);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void proc(
            final String inputSuffix,
            final String outputFilename,
            String root,
            String currentDir) throws Exception {
        File dir = new File(currentDir);
    
        // Filter for directories only.
        FileFilter filterForDirs = new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            };
        File[] children = dir.listFiles(filterForDirs);

        // Recursive call.
         for (int i = 0; i < children.length; i++) {
             proc(inputSuffix, outputFilename, root, children[i].toString());
         }

        // Filter for files with name == outputFilename.
        FilenameFilter filterForOutputFilename = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.equals(outputFilename);
                }
            };

        ArrayList indexFiles = new ArrayList();
            
        // Look for outputFilename in children directories.
        for (int i = 0; i < children.length; i++) {
            File[] grandchildren = children[i].listFiles(filterForOutputFilename);
            if (grandchildren.length == 1) {
                String indexFile = grandchildren[0].toString();
                indexFile = indexFile.replaceFirst("^" + currentDir, "");
                indexFile = indexFile.replaceFirst("^" + File.separator, "");
                indexFiles.add(indexFile);
            } else {
                if (grandchildren.length > 1) {
                    throw new Exception("Duplicate file "
                            + outputFilename
                            + " in "
                            + children[i]);
                }
            }
        }

        // Filter for files with name ending in inputSuffix and not outputFilename.
        FilenameFilter filterForInputSuffix = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(inputSuffix) && !name.equals(outputFilename);
                }
            };
        
        String[] ncFiles = dir.list(filterForInputSuffix);
        String[] components = {};
        if (ncFiles.length > 0) {
            components = new String[ncFiles.length];

            for (int i = 0; i < ncFiles.length; i++) {
                // Make short path format for component name, w/o suffix
                String shortpath = currentDir.replaceFirst("^" + root, "");
                shortpath = shortpath.replaceFirst("^" + File.separator, "");
                shortpath = shortpath + File.separator + ncFiles[i];
                shortpath = shortpath.replaceFirst(inputSuffix + "$", "");
                components[i] = shortpath;
            }
        }

        // Create libraryName
        String[] currentDirSubnames = currentDir.split(File.separator);
        if (currentDirSubnames.length < 1)
            throw new Exception("Problem with currentDir name: " + currentDir);
        String libraryName = currentDirSubnames[currentDirSubnames.length - 1];

        // Create full output file name.
        String fullOutputFilename = currentDir + File.separator + outputFilename;

        // Create index file.
        String[] stringArrayType = {};
        MoMLLib.generateIndex(
                components,
                (String[]) indexFiles.toArray(stringArrayType),
                libraryName,
                fullOutputFilename);
    }
}

