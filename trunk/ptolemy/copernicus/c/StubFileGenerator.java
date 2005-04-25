/*
  A C code generator for generating "stub header files"
  that implement Java classes.

  Copyright (c) 2003-2005 The University of Maryland.
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

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY

*/
package ptolemy.copernicus.c;

import soot.SootClass;

import java.util.Iterator;


/** A C code generator for generating "stub header files"
    that implement Java classes.

    @author Ankush Varma
    @version $Id$
    @since Ptolemy II 4.0
    @Pt.ProposedRating Red (ankush)
    @Pt.AcceptedRating Red (ankush)
*/
public class StubFileGenerator extends CodeGenerator {
    /** Construct a stub file generator.
     */
    public StubFileGenerator() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate code for a stub header file that implements
     *  declarations associated with a class.
     *  @param source The class.
     *  @return Stub header code for the class.
     */
    public String generate(SootClass source) {
        StringBuffer bodyCode = new StringBuffer();
        StringBuffer headerCode = new StringBuffer();
        StringBuffer footerCode = new StringBuffer();

        // An iterator over all different member declarations
        // (declarations for fields, methods, constructors, etc.) of the
        // given Java class declaration.
        Iterator membersIter;

        // Extract the unique class name and instance-specific type name to
        // use for the class.
        String className = source.getName();
        String typeName = CNames.instanceNameOf(source);
        String objectName = CNames.classNameOf(source);

        headerCode.append("/* Automatically generated by the Ptolemy "
            + "C Code Generator. */\n\n");

        // Avoid multiple inclusions of the generated header file.
        headerCode.append("\n#ifndef _" + typeName + "_i_h\n");
        headerCode.append("#define _" + typeName + "_i_h\n\n");
        footerCode.append("\n#endif\n\n");

        // Generate typedef for instance-specific structure. The actual
        // definition of the structure will be placed after the definition
        // of the class-specific structure.
        bodyCode.append("struct " + typeName + ";\n");
        bodyCode.append("typedef struct " + typeName + " *" + typeName
            + ";\n\n");

        // Generate the type declaration header for the class
        // structure. This structure represents the class as a whole.
        // A second structure will be defined to represent each instance of the
        // class.
        bodyCode.append("/* Structure that implements " + className + " */\n");
        bodyCode.append("struct " + objectName + ";\n\n");
        bodyCode.append("/* Pointer to structure that implements " + className
            + " */\n");
        bodyCode.append("typedef struct " + objectName + " *" + objectName
            + ";\n");

        // Return an appropriate concatenation of the code strings.
        return headerCode.toString() + bodyCode.toString()
        + footerCode.toString();
    }

    /** Return the appropriate suffix for the stub header files.
     *  @return The suffix.
     */
    public static String stubFileNameSuffix() {
        return ("_i.h");
    }
}
