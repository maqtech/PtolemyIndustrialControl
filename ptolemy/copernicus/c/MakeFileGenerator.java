/*

A class that generates a makefile for a given class.

Copyright (c) 2002-2003 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.


@ProposedRating Red (ankush@eng.umd.edu)
@AcceptedRating Red (ankush@eng.umd.edu)

*/

package ptolemy.copernicus.c;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashSet;

import soot.Scene;
import soot.SootClass;

/** A class that generates the makefile for the given class. The generated file
    has the name (class).make.

    @author Ankush Varma
    @version $Id$
    @since Ptolemy II 2.0
*/

public class MakeFileGenerator {
    /** Dummy constructor
     */
    public void MakeFileGenerator() {
    }

    /** Finds the filename corresponding to this class.
     *  @param className The name of the class.
     *  @return The corresponding filename as it should be written to the
     *  makeFile.
     */
    public static String classNameToMakeFileName(String className) {
        StringBuffer name = new StringBuffer(
            CNames.classNameToFileName(className));
        return name.toString();
    }

    /** Create the MakeFile.
     *  @param classPath The classPath.
     *  @param className The class for which the Makefile is to be generated.
     *  The makefile will have the name <i>className</i>.make.
     */
    public static void generateMakeFile(String classPath, String className) {
        // Garbage collection.
        boolean gc = Options.v().getBoolean("gc");

        StringBuffer code = new StringBuffer();

        code.append("#Standard variables\n");
        code.append("RUNTIME = ../runtime\n");
        code.append("NATIVE_BODIES ="
                + NativeMethodGenerator.nativeBodyLib + "\n");
        // Overridden bodies.
        code.append("OVER_BODIES = "
                + OverriddenMethodGenerator.overriddenBodyLib
                + "\n");
        code.append("LIB = " + Options.v().get("lib")
                + "\n");
        code.append("LIB_FILE = $(LIB)/j2c_lib.a\n");

        // The -g flag is for gdb debugging.
        //code.append("CFLAGS = -g -Wall -pedantic\n");
        code.append("CFLAGS = -g -Wall -pedantic -Wno-trigraphs\n");
        code.append("DEPEND = gcc -Wno-trigraphs -MM -I $(RUNTIME) -I $(LIB) "
                + "-I $(NATIVE_BODIES) -I $(OVER_BODIES)\n\n");

        code.append("THIS = " + className + ".make\n");

        // Get names of all .c files in the transitive closure.
        code.append("SOURCES = $(RUNTIME)/pccg_runtime.c "
                + "$(RUNTIME)/pccg_array.c $(RUNTIME)/strings.c\\\n"
                + "\t" + className + "_main.c\\\n");

        HashSet libSources = RequiredFileGenerator.generateUserClasses(code);

        // Generate all the source files for system(library) classes.
        code.append("\n\nLIB_SOURCES = ");
        Iterator i = libSources.iterator();
        while (i.hasNext()) {
            code.append("\t" + (String)i.next() + ".c\\\n");
        }

        code.append("\n");// Takes care of blank line for last "\".

        // Definitions for various kinds of files.
        code.append("\nOBJECTS = $(SOURCES:.c=.o)\n");
        code.append(  "HEADERS = $(SOURCES:.c=.h)\n");
        code.append( "IHEADERS = $(SOURCES:.c="
                + StubFileGenerator.stubFileNameSuffix() + ")\n");
        if (gc) {
            code.append( "GC_OBJECT = $(RUNTIME)/GC.o\n");
        }

        code.append("\nLIB_OBJECTS = $(LIB_SOURCES:.c=.o)\n");
        code.append(  "LIB_HEADERS = $(LIB_SOURCES:.c=.h)\n");
        code.append( "LIB_IHEADERS = $(LIB_SOURCES:.c="
                + StubFileGenerator.stubFileNameSuffix() + ")\n");

        // Main Target.
        code.append("\n"+ className + ".exe : $(OBJECTS) $(LIB_FILE)");

        if (gc) {
            code.append(" $(GC_OBJECT)");
        }

        code.append("\n");
        code.append("\tgcc -g $(OBJECTS) $(LIB_FILE) ");
        if (gc) {
            code.append("$(GC_OBJECT) ");
        }
        code.append("-o "+ className +".exe\n");

        // The garbage collector cannot be compiled with -pedantic.
        if (gc) {
            code.append("\n$(GC_OBJECT) : $(RUNTIME)/GC.c $(RUNTIME)/GC.h\n");
            code.append("\tgcc -g -c $(RUNTIME)/GC.c -o $(RUNTIME)/GC.o\n");
        }

        // Conversion from .c to .o
        code.append(".c.o:\n");
        code.append("\tgcc $(CFLAGS) -c  -I $(RUNTIME) -I $(LIB) "
                + "-I $(NATIVE_BODIES) $< -o $@ "
                + "\n\n");

        // Library generation.
        code.append("$(LIB_FILE): $(LIB_OBJECTS)\n");
        code.append("\tar r $(LIB_FILE) $(LIB_OBJECTS)\n");
        code.append("\tranlib $(LIB_FILE)\n");

        // Other targets.
        code.append("\n.PHONY:depend\n\n");
        code.append("depend:\n");
        code.append("\t$(DEPEND) $(SOURCES)>makefile.tmp;\\\n");
        code.append("\tcat $(THIS) makefile.tmp>"
                + className + ".mk;\\\n");
        code.append("\trm makefile.tmp;\n");
        code.append("\n");

        code.append("clean:\n");
        code.append("\trm $(OBJECTS) $(LIB_OBJECTS) $(LIB_FILE)");
        if (gc) {
            code.append(" $(GC_OBJECT)");
        }
        code.append(";\n");

        code.append("# DO NOT DELETE THIS LINE "
                    + " -- make depend depends on it.\n\n");

        FileHandler.write(className + ".make", code.toString());

    }

}


