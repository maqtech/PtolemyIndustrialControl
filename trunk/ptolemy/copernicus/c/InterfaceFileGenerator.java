/*
A C code generator for generating "interface files" (.i.h files)
that implement Java classes.

Copyright (c) 2001-2002 The University of Maryland.
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

@ProposedRating Red (ankush@eng.umd.edu)
@AcceptedRating Red (ankush@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/*
  A C code generator for generating "interface files" (.i.h files)
  that implement Java classes.

  @author Ankush Varma
  @version $Id$

*/

// FIXME: Handle (ignore?) phantom methods and fields.

public class InterfaceFileGenerator extends CodeGenerator {

    /** Construct a header file generator.
     */
    public InterfaceFileGenerator() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate code for a C header file that implements declarations
     *  associated with a class.
     *  Code for two struct-based type definitions is generated here.
     *  One type corresponds to the class itself (class variables,
     *  function pointers to methods, etc.), and the other type
     *  is for instances of the class.
     *  @param source the class.
     *  @return header code for the class.
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
        // use for the class
        String className = source.getName();
        String typeName = CNames.instanceNameOf(source);
        String objectName = CNames.classNameOf(source);

        // Avoid multiple inclusions of the generated header file
        headerCode.append("\n#ifndef _" + typeName + "_i_h\n");
        headerCode.append("#define _" + typeName + "_i_h\n\n");
        footerCode.append("\n#endif\n\n");

        // FIXME: generate header code for inner classes (probably here)

        // Generate typedef for instance-specific structure. The actual
        // definition of the structure will be placed after the definition
        // of the class-specific structure;


        bodyCode.append("struct " + typeName + ";\n");
        bodyCode.append("typedef struct " + typeName + " *" +
                        typeName + ";\n\n");

        bodyCode.append("/* Structure that implements " + className + " */\n");
        bodyCode.append("struct " + objectName +";\n\n");
        bodyCode.append("/* Pointer to structure that implements " +
                        className + " */\n");
        bodyCode.append("typedef struct " +objectName+ " *" + objectName
                        +";\n");




         // Return an appropriate concatenation of the code strings.
        return (headerCode.append(bodyCode.append(footerCode))).toString();
    }

}
