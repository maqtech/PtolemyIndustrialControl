# Tests for the ASTReflect class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#


######################################################################
####
#
test ASTReflect-2.1 {check out constructor in Object} {
    set class [ java::call Class forName "java.lang.Object"]
    set astList [java::call ptolemy.lang.java.ASTReflect constructorsASTList $class]
    lcompare [listToStrings $astList] \
{{ {ConstructorDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident Object} 
         {Qualifier {NameNode { 
                     {Ident lang} 
                     {Qualifier {NameNode { 
                                 {Ident java} 
                                 {Qualifier {AbsentTreeNode {leaf}}} 
                               }}} 
                   }}} 
       }}} 
  {Params  {}} 
  {ThrowsList  {}} 
  {Body {BlockNode { 
         {Stmts  {}} 
       }}} 
  {ConstructorCall {SuperConstructorCallNode { 
                    {Args  {}} 
                  }}} 
}}}}
} {1}

######################################################################
####
#
test ASTReflect-2.2 {check out constructors} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect constructorsASTList $class]
    lcompare [listToStrings $astList] \
{{ {ConstructorDeclNode { 
  {Modifiers 0} 
  {Name {NameNode { 
         {Ident ReflectTest} 
         {Qualifier {NameNode { 
                     {Ident test} 
                     {Qualifier {NameNode { 
                                 {Ident java} 
                                 {Qualifier {NameNode { 
                                             {Ident lang} 
                                             {Qualifier {NameNode { 
                                                         {Ident ptolemy} 
                                                         {Qualifier {AbsentTreeNode {leaf}}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {Params  {}} 
  {ThrowsList  {}} 
  {Body {BlockNode { 
         {Stmts  {}} 
       }}} 
  {ConstructorCall {SuperConstructorCallNode { 
                    {Args  {}} 
                  }}} 
}}} { {ConstructorDeclNode { 
  {Modifiers 0} 
  {Name {NameNode { 
         {Ident ReflectTest} 
         {Qualifier {NameNode { 
                     {Ident test} 
                     {Qualifier {NameNode { 
                                 {Ident java} 
                                 {Qualifier {NameNode { 
                                             {Ident lang} 
                                             {Qualifier {NameNode { 
                                                         {Ident ptolemy} 
                                                         {Qualifier {AbsentTreeNode {leaf}}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {IntTypeNode {leaf}}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {BlockNode { 
         {Stmts  {}} 
       }}} 
  {ConstructorCall {SuperConstructorCallNode { 
                    {Args  {}} 
                  }}} 
}}} { {ConstructorDeclNode { 
  {Modifiers 0} 
  {Name {NameNode { 
         {Ident ReflectTest} 
         {Qualifier {NameNode { 
                     {Ident test} 
                     {Qualifier {NameNode { 
                                 {Ident java} 
                                 {Qualifier {NameNode { 
                                             {Ident lang} 
                                             {Qualifier {NameNode { 
                                                         {Ident ptolemy} 
                                                         {Qualifier {AbsentTreeNode {leaf}}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {ArrayTypeNode { 
              {BaseType {ArrayTypeNode { 
                         {BaseType {IntTypeNode {leaf}}} 
                       }}} 
            }}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {BlockNode { 
         {Stmts  {}} 
       }}} 
  {ConstructorCall {SuperConstructorCallNode { 
                    {Args  {}} 
                  }}} 
}}}}
} {1}

######################################################################
####
#
test ASTReflect-3.1 {check out fields} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTestFields"]
    set astList [java::call ptolemy.lang.java.ASTReflect fieldsASTList $class]
    lcompare [listToStrings $astList] \
{{ {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Boolean} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myBoolean} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident Boolean ptolemy} 
                                                                     {Qualifier {NameNode { 
                                                                                 {Ident lang} 
                                                                                 {Qualifier {NameNode { 
                                                                                             {Ident public java} 
                                                                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                                                                           }}} 
                                                                               }}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Character} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myCharacter} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident Character ptolemy} 
                                                                     {Qualifier {NameNode { 
                                                                                 {Ident lang} 
                                                                                 {Qualifier {NameNode { 
                                                                                             {Ident public java} 
                                                                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                                                                           }}} 
                                                                               }}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Byte} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myByte} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident Byte ptolemy} 
                                                                     {Qualifier {NameNode { 
                                                                                 {Ident lang} 
                                                                                 {Qualifier {NameNode { 
                                                                                             {Ident public java} 
                                                                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                                                                           }}} 
                                                                               }}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Short} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myShort} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident Short ptolemy} 
                                                                     {Qualifier {NameNode { 
                                                                                 {Ident lang} 
                                                                                 {Qualifier {NameNode { 
                                                                                             {Ident public java} 
                                                                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                                                                           }}} 
                                                                               }}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Integer} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myInteger} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident Integer ptolemy} 
                                                                     {Qualifier {NameNode { 
                                                                                 {Ident lang} 
                                                                                 {Qualifier {NameNode { 
                                                                                             {Ident public java} 
                                                                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                                                                           }}} 
                                                                               }}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Long} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myLong} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident Long ptolemy} 
                                                                     {Qualifier {NameNode { 
                                                                                 {Ident lang} 
                                                                                 {Qualifier {NameNode { 
                                                                                             {Ident public java} 
                                                                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                                                                           }}} 
                                                                               }}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Float} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myFloat} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident Float ptolemy} 
                                                                     {Qualifier {NameNode { 
                                                                                 {Ident lang} 
                                                                                 {Qualifier {NameNode { 
                                                                                             {Ident public java} 
                                                                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                                                                           }}} 
                                                                               }}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Double} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myDouble} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident Double ptolemy} 
                                                                     {Qualifier {NameNode { 
                                                                                 {Ident lang} 
                                                                                 {Qualifier {NameNode { 
                                                                                             {Ident public java} 
                                                                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                                                                           }}} 
                                                                               }}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {BoolTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myBoolean} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident protected boolean ptolemy} 
                                                                     {Qualifier {AbsentTreeNode {leaf}}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {CharTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myCharacter} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident protected char ptolemy} 
                                                                     {Qualifier {AbsentTreeNode {leaf}}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {ByteTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myByte} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident protected byte ptolemy} 
                                                                     {Qualifier {AbsentTreeNode {leaf}}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {ShortTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myShort} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident protected short ptolemy} 
                                                                     {Qualifier {AbsentTreeNode {leaf}}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {IntTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myInteger} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident protected int ptolemy} 
                                                                     {Qualifier {AbsentTreeNode {leaf}}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {LongTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myLong} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident protected long ptolemy} 
                                                                     {Qualifier {AbsentTreeNode {leaf}}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {FloatTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myFloat} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident protected float ptolemy} 
                                                                     {Qualifier {AbsentTreeNode {leaf}}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {DoubleTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myDouble} 
         {Qualifier {NameNode { 
                     {Ident ReflectTestFields} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident protected double ptolemy} 
                                                                     {Qualifier {AbsentTreeNode {leaf}}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}}}   
} {1}

######################################################################
####
#
test ASTReflect-4.1 {check out innerclasses} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect innerClassesASTList $class]
    lcompare [listToStrings $astList] \
{{ {ClassDeclNode { 
  {Interfaces  {}} 
  {Members { 
   {ConstructorDeclNode { 
    {Modifiers 0} 
    {Name {NameNode { 
           {Ident ReflectTest$innerPublicClass} 
           {Qualifier {NameNode { 
                       {Ident test} 
                       {Qualifier {NameNode { 
                                   {Ident java} 
                                   {Qualifier {NameNode { 
                                               {Ident lang} 
                                               {Qualifier {NameNode { 
                                                           {Ident ptolemy} 
                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
         }}} 
    {Params { 
     {ParameterNode { 
      {DefType {TypeNameNode { 
                {Name {NameNode { 
                       {Ident ReflectTest} 
                       {Qualifier {NameNode { 
                                   {Ident test} 
                                   {Qualifier {NameNode { 
                                               {Ident java} 
                                               {Qualifier {NameNode { 
                                                           {Ident lang} 
                                                           {Qualifier {NameNode { 
                                                                       {Ident ptolemy} 
                                                                       {Qualifier {AbsentTreeNode {leaf}}} 
                                                                     }}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
              }}} 
      {Modifiers 1} 
      {Name {NameNode { 
             {Ident } 
             {Qualifier {AbsentTreeNode {leaf}}} 
           }}} 
    }}}} 
    {ThrowsList  {}} 
    {Body {BlockNode { 
           {Stmts  {}} 
         }}} 
    {ConstructorCall {SuperConstructorCallNode { 
                      {Args  {}} 
                    }}} 
  }}   {MethodDeclNode { 
    {Modifiers 1} 
    {Name {NameNode { 
           {Ident innerPublicClassPublicMethod} 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
    {Params { 
     {ParameterNode { 
      {DefType {TypeNameNode { 
                {Name {NameNode { 
                       {Ident NamedObj} 
                       {Qualifier {NameNode { 
                                   {Ident util} 
                                   {Qualifier {NameNode { 
                                               {Ident kernel} 
                                               {Qualifier {NameNode { 
                                                           {Ident ptolemy} 
                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
              }}} 
      {Modifiers 1} 
      {Name {NameNode { 
             {Ident } 
             {Qualifier {AbsentTreeNode {leaf}}} 
           }}} 
    }}}} 
    {ThrowsList  {}} 
    {Body {AbsentTreeNode {leaf}}} 
    {ReturnType {ArrayTypeNode { 
                 {BaseType {ArrayTypeNode { 
                            {BaseType {ArrayTypeNode { 
                                       {BaseType {IntTypeNode {leaf}}} 
                                     }}} 
                          }}} 
               }}} 
  }}   {FieldDeclNode { 
    {DefType {TypeNameNode { 
              {Name {NameNode { 
                     {Ident ReflectTest} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident ptolemy} 
                                                                     {Qualifier {AbsentTreeNode {leaf}}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
            }}} 
    {Modifiers 20} 
    {Name {NameNode { 
           {Ident this$0} 
           {Qualifier {NameNode { 
                       {Ident ReflectTest$innerPublicClass} 
                       {Qualifier {NameNode { 
                                   {Ident test} 
                                   {Qualifier {NameNode { 
                                               {Ident java} 
                                               {Qualifier {NameNode { 
                                                           {Ident lang} 
                                                           {Qualifier {NameNode { 
                                                                       {Ident ReflectTest ptolemy} 
                                                                       {Qualifier {NameNode { 
                                                                                   {Ident test} 
                                                                                   {Qualifier {NameNode { 
                                                                                               {Ident java} 
                                                                                               {Qualifier {NameNode { 
                                                                                                           {Ident lang} 
                                                                                                           {Qualifier {NameNode { 
                                                                                                                       {Ident private final ptolemy} 
                                                                                                                       {Qualifier {AbsentTreeNode {leaf}}} 
                                                                                                                     }}} 
                                                                                                         }}} 
                                                                                             }}} 
                                                                                 }}} 
                                                                     }}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
         }}} 
    {InitExpr {AbsentTreeNode {leaf}}} 
  }}}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident ReflectTest$innerPublicClass} 
         {Qualifier {NameNode { 
                     {Ident test} 
                     {Qualifier {NameNode { 
                                 {Ident java} 
                                 {Qualifier {NameNode { 
                                             {Ident lang} 
                                             {Qualifier {NameNode { 
                                                         {Ident ptolemy} 
                                                         {Qualifier {AbsentTreeNode {leaf}}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {SuperClass {TypeNameNode { 
               {Name {NameNode { 
                      {Ident Object} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}}}
} {1} 

######################################################################
####
#
test ASTReflect-5.1 {check out methods} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    lcompare [listToStrings $astList] \
{{ {MethodDeclNode { 
  {Modifiers 513} 
  {Name {NameNode { 
         {Ident publicMethod1} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params  {}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {VoidTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident publicMethod2} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {IntTypeNode {leaf}}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList { 
   {TypeNameNode { 
    {Name {NameNode { 
           {Ident Exception} 
           {Qualifier {NameNode { 
                       {Ident lang} 
                       {Qualifier {NameNode { 
                                   {Ident java} 
                                   {Qualifier {AbsentTreeNode {leaf}}} 
                                 }}} 
                     }}} 
         }}} 
  }}}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {VoidTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident publicMethod2} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {ArrayTypeNode { 
              {BaseType {TypeNameNode { 
                         {Name {NameNode { 
                                {Ident NamedObj} 
                                {Qualifier {NameNode { 
                                            {Ident util} 
                                            {Qualifier {NameNode { 
                                                        {Ident kernel} 
                                                        {Qualifier {NameNode { 
                                                                    {Ident ptolemy} 
                                                                    {Qualifier {AbsentTreeNode {leaf}}} 
                                                                  }}} 
                                                      }}} 
                                          }}} 
                              }}} 
                       }}} 
            }}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {VoidTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident publicMethod2} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {ArrayTypeNode { 
              {BaseType {ArrayTypeNode { 
                         {BaseType {TypeNameNode { 
                                    {Name {NameNode { 
                                           {Ident NamedObj} 
                                           {Qualifier {NameNode { 
                                                       {Ident util} 
                                                       {Qualifier {NameNode { 
                                                                   {Ident kernel} 
                                                                   {Qualifier {NameNode { 
                                                                               {Ident ptolemy} 
                                                                               {Qualifier {AbsentTreeNode {leaf}}} 
                                                                             }}} 
                                                                 }}} 
                                                     }}} 
                                         }}} 
                                  }}} 
                       }}} 
            }}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {VoidTypeNode {leaf}}} 
}}}}
} {1}

######################################################################
####
#
test ASTReflect-6.1 {check out Anonymous classes} {
    set class [ java::call Class forName "ptolemy.lang.java.test.Anon"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    lcompare [listToStrings $astList] \
{{ {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident foo} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params  {}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {IntTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident ugh} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params  {}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {IntTypeNode {leaf}}} 
}}}}
} {1}

######################################################################
####
#
test ASTReflect-7.1 {check out Array Length} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ArrayLength"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    lcompare [listToStrings $astList] \
{{ {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident ack} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {TypeNameNode { 
              {Name {NameNode { 
                     {Ident Cloneable} 
                     {Qualifier {NameNode { 
                                 {Ident lang} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                           }}} 
                               }}} 
                   }}} 
            }}} 
    {Modifiers 9} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {IntTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident oof} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {DoubleTypeNode {leaf}}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {TypeNameNode { 
               {Name {NameNode { 
                      {Ident String} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident oof} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {IntTypeNode {leaf}}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {TypeNameNode { 
               {Name {NameNode { 
                      {Ident String} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident ugh} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {TypeNameNode { 
              {Name {NameNode { 
                     {Ident Object} 
                     {Qualifier {NameNode { 
                                 {Ident lang} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                           }}} 
                               }}} 
                   }}} 
            }}} 
    {Modifiers 1} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {DoubleTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident yo} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {ArrayTypeNode { 
              {BaseType {DoubleTypeNode {leaf}}} 
            }}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {IntTypeNode {leaf}}} 
}}}}
} {1}

######################################################################
####
#
#test ASTReflect-8.1 {check out characters - ignored - signature is unchanged } {
#    set class [ java::call Class forName "ptolemy.lang.java.test.CharTest"]
#    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
#    list [listToStrings $astList]
#} {1}

######################################################################
####
#
test ASTReflect-9.1 {check out class access} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ClassAccess"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    lcompare [listToStrings $astList] \
{{ {MethodDeclNode { 
  {Modifiers 512} 
  {Name {NameNode { 
         {Ident class$} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {TypeNameNode { 
              {Name {NameNode { 
                     {Ident String} 
                     {Qualifier {NameNode { 
                                 {Ident lang} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                           }}} 
                               }}} 
                   }}} 
            }}} 
    {Modifiers 17} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {TypeNameNode { 
               {Name {NameNode { 
                      {Ident Class} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}}}
} {1}

######################################################################
####
#
#test ASTReflect-10.1 {check out exceptions - ignored - signature is unchanged } {
#    set class [ java::call Class forName "ptolemy.lang.java.test.ExceptionTest"]
#    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
#    list [listToStrings $astList]
#} {}

######################################################################
####
#
#test ASTReflect-11.1 {check out for loops - ignored - signature is unchanged } {
#    set class [ java::call Class forName "ptolemy.lang.java.test.For"]
#    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
#    list [listToStrings $astList]
#} {}
######################################################################
####
#

test ASTReflect-12.1 {check out interfaces} {
    set class [ java::call Class forName "ptolemy.lang.java.test.IFace"]
    set ast [java::call ptolemy.lang.java.ASTReflect ASTCompileUnitNode $class]
    lcompare [$ast toString] \
{ {CompileUnitNode { 
  {DefTypes { 
   {InterfaceDeclNode { 
    {Interfaces  {}} 
    {Members  {}} 
    {Modifiers 8} 
    {Name {NameNode { 
           {Ident IFace} 
           {Qualifier {NameNode { 
                       {Ident test} 
                       {Qualifier {NameNode { 
                                   {Ident java} 
                                   {Qualifier {NameNode { 
                                               {Ident lang} 
                                               {Qualifier {NameNode { 
                                                           {Ident ptolemy} 
                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
         }}} 
  }}}} 
  {Imports  {}} 
  {Pkg {NameNode { 
        {Ident test} 
        {Qualifier {NameNode { 
                    {Ident java} 
                    {Qualifier {NameNode { 
                                {Ident lang} 
                                {Qualifier {NameNode { 
                                            {Ident ptolemy} 
                                            {Qualifier {AbsentTreeNode {leaf}}} 
                                          }}} 
                              }}} 
                  }}} 
      }}} 
}}}
} {1}


######################################################################
####
#
test ASTReflect-13.1 {check out inner classes} {
    set class [ java::call Class forName "ptolemy.lang.java.test.InnerClass"]
    set ast [java::call ptolemy.lang.java.ASTReflect ASTCompileUnitNode $class]
    lcompare [$ast toString] \
{ {CompileUnitNode { 
  {DefTypes { 
   {ClassDeclNode { 
    {Interfaces  {}} 
    {Members { 
     {ConstructorDeclNode { 
      {Modifiers 1} 
      {Name {NameNode { 
             {Ident InnerClass} 
             {Qualifier {NameNode { 
                         {Ident test} 
                         {Qualifier {NameNode { 
                                     {Ident java} 
                                     {Qualifier {NameNode { 
                                                 {Ident lang} 
                                                 {Qualifier {NameNode { 
                                                             {Ident ptolemy} 
                                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                                           }}} 
                                               }}} 
                                   }}} 
                       }}} 
           }}} 
      {Params  {}} 
      {ThrowsList  {}} 
      {Body {BlockNode { 
             {Stmts  {}} 
           }}} 
      {ConstructorCall {SuperConstructorCallNode { 
                        {Args  {}} 
                      }}} 
    }}     {FieldDeclNode { 
      {DefType {IntTypeNode {leaf}}} 
      {Modifiers 1} 
      {Name {NameNode { 
             {Ident x} 
             {Qualifier {NameNode { 
                         {Ident InnerClass} 
                         {Qualifier {NameNode { 
                                     {Ident test} 
                                     {Qualifier {NameNode { 
                                                 {Ident java} 
                                                 {Qualifier {NameNode { 
                                                             {Ident lang} 
                                                             {Qualifier {NameNode { 
                                                                         {Ident public int ptolemy} 
                                                                         {Qualifier {AbsentTreeNode {leaf}}} 
                                                                       }}} 
                                                           }}} 
                                               }}} 
                                   }}} 
                       }}} 
           }}} 
      {InitExpr {AbsentTreeNode {leaf}}} 
    }}     {ClassDeclNode { 
      {Interfaces  {}} 
      {Members { 
       {ConstructorDeclNode { 
        {Modifiers 1} 
        {Name {NameNode { 
               {Ident InnerClass$InnerInnerClass} 
               {Qualifier {NameNode { 
                           {Ident test} 
                           {Qualifier {NameNode { 
                                       {Ident java} 
                                       {Qualifier {NameNode { 
                                                   {Ident lang} 
                                                   {Qualifier {NameNode { 
                                                               {Ident ptolemy} 
                                                               {Qualifier {AbsentTreeNode {leaf}}} 
                                                             }}} 
                                                 }}} 
                                     }}} 
                         }}} 
             }}} 
        {Params { 
         {ParameterNode { 
          {DefType {TypeNameNode { 
                    {Name {NameNode { 
                           {Ident InnerClass} 
                           {Qualifier {NameNode { 
                                       {Ident test} 
                                       {Qualifier {NameNode { 
                                                   {Ident java} 
                                                   {Qualifier {NameNode { 
                                                               {Ident lang} 
                                                               {Qualifier {NameNode { 
                                                                           {Ident ptolemy} 
                                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                                         }}} 
                                                             }}} 
                                                 }}} 
                                     }}} 
                         }}} 
                  }}} 
          {Modifiers 0} 
          {Name {NameNode { 
                 {Ident } 
                 {Qualifier {AbsentTreeNode {leaf}}} 
               }}} 
        }}}} 
        {ThrowsList  {}} 
        {Body {BlockNode { 
               {Stmts  {}} 
             }}} 
        {ConstructorCall {SuperConstructorCallNode { 
                          {Args  {}} 
                        }}} 
      }}       {MethodDeclNode { 
        {Modifiers 1} 
        {Name {NameNode { 
               {Ident meth} 
               {Qualifier {AbsentTreeNode {leaf}}} 
             }}} 
        {Params  {}} 
        {ThrowsList  {}} 
        {Body {AbsentTreeNode {leaf}}} 
        {ReturnType {VoidTypeNode {leaf}}} 
      }}       {FieldDeclNode { 
        {DefType {TypeNameNode { 
                  {Name {NameNode { 
                         {Ident InnerClass} 
                         {Qualifier {NameNode { 
                                     {Ident test} 
                                     {Qualifier {NameNode { 
                                                 {Ident java} 
                                                 {Qualifier {NameNode { 
                                                             {Ident lang} 
                                                             {Qualifier {NameNode { 
                                                                         {Ident ptolemy} 
                                                                         {Qualifier {AbsentTreeNode {leaf}}} 
                                                                       }}} 
                                                           }}} 
                                               }}} 
                                   }}} 
                       }}} 
                }}} 
        {Modifiers 20} 
        {Name {NameNode { 
               {Ident this$0} 
               {Qualifier {NameNode { 
                           {Ident InnerClass$InnerInnerClass} 
                           {Qualifier {NameNode { 
                                       {Ident test} 
                                       {Qualifier {NameNode { 
                                                   {Ident java} 
                                                   {Qualifier {NameNode { 
                                                               {Ident lang} 
                                                               {Qualifier {NameNode { 
                                                                           {Ident InnerClass ptolemy} 
                                                                           {Qualifier {NameNode { 
                                                                                       {Ident test} 
                                                                                       {Qualifier {NameNode { 
                                                                                                   {Ident java} 
                                                                                                   {Qualifier {NameNode { 
                                                                                                               {Ident lang} 
                                                                                                               {Qualifier {NameNode { 
                                                                                                                           {Ident private final ptolemy} 
                                                                                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                                                                                         }}} 
                                                                                                             }}} 
                                                                                                 }}} 
                                                                                     }}} 
                                                                         }}} 
                                                             }}} 
                                                 }}} 
                                     }}} 
                         }}} 
             }}} 
        {InitExpr {AbsentTreeNode {leaf}}} 
      }}       {FieldDeclNode { 
        {DefType {IntTypeNode {leaf}}} 
        {Modifiers 2} 
        {Name {NameNode { 
               {Ident _y} 
               {Qualifier {NameNode { 
                           {Ident InnerClass$InnerInnerClass} 
                           {Qualifier {NameNode { 
                                       {Ident test} 
                                       {Qualifier {NameNode { 
                                                   {Ident java} 
                                                   {Qualifier {NameNode { 
                                                               {Ident lang} 
                                                               {Qualifier {NameNode { 
                                                                           {Ident protected int ptolemy} 
                                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                                         }}} 
                                                             }}} 
                                                 }}} 
                                     }}} 
                         }}} 
             }}} 
        {InitExpr {AbsentTreeNode {leaf}}} 
      }}}} 
      {Modifiers 1} 
      {Name {NameNode { 
             {Ident InnerClass$InnerInnerClass} 
             {Qualifier {NameNode { 
                         {Ident test} 
                         {Qualifier {NameNode { 
                                     {Ident java} 
                                     {Qualifier {NameNode { 
                                                 {Ident lang} 
                                                 {Qualifier {NameNode { 
                                                             {Ident ptolemy} 
                                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                                           }}} 
                                               }}} 
                                   }}} 
                       }}} 
           }}} 
      {SuperClass {TypeNameNode { 
                   {Name {NameNode { 
                          {Ident Object} 
                          {Qualifier {NameNode { 
                                      {Ident lang} 
                                      {Qualifier {NameNode { 
                                                  {Ident java} 
                                                  {Qualifier {AbsentTreeNode {leaf}}} 
                                                }}} 
                                    }}} 
                        }}} 
                 }}} 
    }}}} 
    {Modifiers 0} 
    {Name {NameNode { 
           {Ident InnerClass} 
           {Qualifier {NameNode { 
                       {Ident test} 
                       {Qualifier {NameNode { 
                                   {Ident java} 
                                   {Qualifier {NameNode { 
                                               {Ident lang} 
                                               {Qualifier {NameNode { 
                                                           {Ident ptolemy} 
                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
         }}} 
    {SuperClass {TypeNameNode { 
                 {Name {NameNode { 
                        {Ident Object} 
                        {Qualifier {NameNode { 
                                    {Ident lang} 
                                    {Qualifier {NameNode { 
                                                {Ident java} 
                                                {Qualifier {AbsentTreeNode {leaf}}} 
                                              }}} 
                                  }}} 
                      }}} 
               }}} 
  }}}} 
  {Imports  {}} 
  {Pkg {NameNode { 
        {Ident test} 
        {Qualifier {NameNode { 
                    {Ident java} 
                    {Qualifier {NameNode { 
                                {Ident lang} 
                                {Qualifier {NameNode { 
                                            {Ident ptolemy} 
                                            {Qualifier {AbsentTreeNode {leaf}}} 
                                          }}} 
                              }}} 
                  }}} 
      }}} 
}}}
} {1}

######################################################################
####
#
test ASTReflect-14.1 {check out } {
    set class [ java::call Class forName "ptolemy.lang.java.test.InnerIFace"]
    set ast [java::call ptolemy.lang.java.ASTReflect ASTCompileUnitNode $class]
    lcompare [$ast toString] \
{ {CompileUnitNode { 
  {DefTypes { 
   {ClassDeclNode { 
    {Interfaces  {}} 
    {Members { 
     {ConstructorDeclNode { 
      {Modifiers 0} 
      {Name {NameNode { 
             {Ident InnerIFace} 
             {Qualifier {NameNode { 
                         {Ident test} 
                         {Qualifier {NameNode { 
                                     {Ident java} 
                                     {Qualifier {NameNode { 
                                                 {Ident lang} 
                                                 {Qualifier {NameNode { 
                                                             {Ident ptolemy} 
                                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                                           }}} 
                                               }}} 
                                   }}} 
                       }}} 
           }}} 
      {Params  {}} 
      {ThrowsList  {}} 
      {Body {BlockNode { 
             {Stmts  {}} 
           }}} 
      {ConstructorCall {SuperConstructorCallNode { 
                        {Args  {}} 
                      }}} 
    }}     {ClassDeclNode { 
      {Interfaces  {}} 
      {Members { 
       {MethodDeclNode { 
        {Modifiers 9} 
        {Name {NameNode { 
               {Ident ack} 
               {Qualifier {AbsentTreeNode {leaf}}} 
             }}} 
        {Params  {}} 
        {ThrowsList  {}} 
        {Body {AbsentTreeNode {leaf}}} 
        {ReturnType {IntTypeNode {leaf}}} 
      }}       {MethodDeclNode { 
        {Modifiers 9} 
        {Name {NameNode { 
               {Ident ugh} 
               {Qualifier {AbsentTreeNode {leaf}}} 
             }}} 
        {Params { 
         {ParameterNode { 
          {DefType {CharTypeNode {leaf}}} 
          {Modifiers 25} 
          {Name {NameNode { 
                 {Ident } 
                 {Qualifier {AbsentTreeNode {leaf}}} 
               }}} 
        }}}} 
        {ThrowsList  {}} 
        {Body {AbsentTreeNode {leaf}}} 
        {ReturnType {CharTypeNode {leaf}}} 
      }}}} 
      {Modifiers 520} 
      {Name {NameNode { 
             {Ident InnerIFace$IFace} 
             {Qualifier {NameNode { 
                         {Ident test} 
                         {Qualifier {NameNode { 
                                     {Ident java} 
                                     {Qualifier {NameNode { 
                                                 {Ident lang} 
                                                 {Qualifier {NameNode { 
                                                             {Ident ptolemy} 
                                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                                           }}} 
                                               }}} 
                                   }}} 
                       }}} 
           }}} 
      {SuperClass {TypeNameNode { 
                   {Name {NameNode { 
                          {Ident Object} 
                          {Qualifier {NameNode { 
                                      {Ident lang} 
                                      {Qualifier {NameNode { 
                                                  {Ident java} 
                                                  {Qualifier {AbsentTreeNode {leaf}}} 
                                                }}} 
                                    }}} 
                        }}} 
                 }}} 
    }}}} 
    {Modifiers 0} 
    {Name {NameNode { 
           {Ident InnerIFace} 
           {Qualifier {NameNode { 
                       {Ident test} 
                       {Qualifier {NameNode { 
                                   {Ident java} 
                                   {Qualifier {NameNode { 
                                               {Ident lang} 
                                               {Qualifier {NameNode { 
                                                           {Ident ptolemy} 
                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
         }}} 
    {SuperClass {TypeNameNode { 
                 {Name {NameNode { 
                        {Ident Object} 
                        {Qualifier {NameNode { 
                                    {Ident lang} 
                                    {Qualifier {NameNode { 
                                                {Ident java} 
                                                {Qualifier {AbsentTreeNode {leaf}}} 
                                              }}} 
                                  }}} 
                      }}} 
               }}} 
  }}}} 
  {Imports  {}} 
  {Pkg {NameNode { 
        {Ident test} 
        {Qualifier {NameNode { 
                    {Ident java} 
                    {Qualifier {NameNode { 
                                {Ident lang} 
                                {Qualifier {NameNode { 
                                            {Ident ptolemy} 
                                            {Qualifier {AbsentTreeNode {leaf}}} 
                                          }}} 
                              }}} 
                  }}} 
      }}} 
}}}
} {1}

######################################################################
####
#
test ASTReflect-15.1 {check out one field, one method classes } {
    set class [ java::call Class forName "ptolemy.lang.java.test.OneFM"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    lcompare [listToStrings $astList] \
{{ {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident get} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params  {}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {IntTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident set} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {IntTypeNode {leaf}}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {VoidTypeNode {leaf}}} 
}}}}
} {1}

######################################################################
####
#
test ASTReflect-16.1 {check out one field classes} {
    set class [ java::call Class forName "ptolemy.lang.java.test.OneField"]
    set astList [java::call ptolemy.lang.java.ASTReflect fieldsASTList $class]
    lcompare [listToStrings $astList] \
{{ {FieldDeclNode { 
  {DefType {IntTypeNode {leaf}}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident x} 
         {Qualifier {NameNode { 
                     {Ident OneField} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident public int ptolemy} 
                                                                     {Qualifier {AbsentTreeNode {leaf}}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}}}
} {1}

######################################################################
####
#
test ASTReflect-17.1 {check out a simple class} {
    set classDeclNode [java::call ptolemy.lang.java.ASTReflect  lookupClassDeclNode "ptolemy.lang.java.test.Simple"]
    lcompare [$classDeclNode toString] \
{ {ClassDeclNode { 
  {Interfaces  {}} 
  {Members { 
   {ConstructorDeclNode { 
    {Modifiers 1} 
    {Name {NameNode { 
           {Ident Simple} 
           {Qualifier {NameNode { 
                       {Ident test} 
                       {Qualifier {NameNode { 
                                   {Ident java} 
                                   {Qualifier {NameNode { 
                                               {Ident lang} 
                                               {Qualifier {NameNode { 
                                                           {Ident ptolemy} 
                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
         }}} 
    {Params  {}} 
    {ThrowsList  {}} 
    {Body {BlockNode { 
           {Stmts  {}} 
         }}} 
    {ConstructorCall {SuperConstructorCallNode { 
                      {Args  {}} 
                    }}} 
  }}}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident Simple} 
         {Qualifier {NameNode { 
                     {Ident test} 
                     {Qualifier {NameNode { 
                                 {Ident java} 
                                 {Qualifier {NameNode { 
                                             {Ident lang} 
                                             {Qualifier {NameNode { 
                                                         {Ident ptolemy} 
                                                         {Qualifier {AbsentTreeNode {leaf}}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {SuperClass {TypeNameNode { 
               {Name {NameNode { 
                      {Ident Object} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}}
} {1}

######################################################################
####
#
test ASTReflect-18.1 {check out a simple class with an import} {
    set classDeclNode [java::call ptolemy.lang.java.ASTReflect  lookupClassDeclNode "ptolemy.lang.java.test.Simple2"]
    lcompare [$classDeclNode toString] \
{ {ClassDeclNode { 
  {Interfaces  {}} 
  {Members { 
   {ConstructorDeclNode { 
    {Modifiers 1} 
    {Name {NameNode { 
           {Ident Simple2} 
           {Qualifier {NameNode { 
                       {Ident test} 
                       {Qualifier {NameNode { 
                                   {Ident java} 
                                   {Qualifier {NameNode { 
                                               {Ident lang} 
                                               {Qualifier {NameNode { 
                                                           {Ident ptolemy} 
                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
         }}} 
    {Params  {}} 
    {ThrowsList  {}} 
    {Body {BlockNode { 
           {Stmts  {}} 
         }}} 
    {ConstructorCall {SuperConstructorCallNode { 
                      {Args  {}} 
                    }}} 
  }}}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident Simple2} 
         {Qualifier {NameNode { 
                     {Ident test} 
                     {Qualifier {NameNode { 
                                 {Ident java} 
                                 {Qualifier {NameNode { 
                                             {Ident lang} 
                                             {Qualifier {NameNode { 
                                                         {Ident ptolemy} 
                                                         {Qualifier {AbsentTreeNode {leaf}}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {SuperClass {TypeNameNode { 
               {Name {NameNode { 
                      {Ident Object} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}}
} {1}

######################################################################
####
#
#test ASTReflect-19.1 {check out switches - ignored - signature is unchanged } {} {
#    set class [ java::call Class forName "ptolemy.lang.java.test.Switch"]
#    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
#    lcompare [listToStrings $astList] \
#} {} 

######################################################################
####
#
test ASTReflect-20.1 {check out superclasses} {
    set classDeclNode [java::call ptolemy.lang.java.ASTReflect  lookupClassDeclNode "ptolemy.lang.java.test.SuperChild"]
    lcompare [$classDeclNode toString] \
{ {ClassDeclNode { 
  {Interfaces  {}} 
  {Members { 
   {ConstructorDeclNode { 
    {Modifiers 1} 
    {Name {NameNode { 
           {Ident SuperChild} 
           {Qualifier {NameNode { 
                       {Ident test} 
                       {Qualifier {NameNode { 
                                   {Ident java} 
                                   {Qualifier {NameNode { 
                                               {Ident lang} 
                                               {Qualifier {NameNode { 
                                                           {Ident ptolemy} 
                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
         }}} 
    {Params  {}} 
    {ThrowsList  {}} 
    {Body {BlockNode { 
           {Stmts  {}} 
         }}} 
    {ConstructorCall {SuperConstructorCallNode { 
                      {Args  {}} 
                    }}} 
  }}   {MethodDeclNode { 
    {Modifiers 513} 
    {Name {NameNode { 
           {Ident test} 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
    {Params  {}} 
    {ThrowsList  {}} 
    {Body {AbsentTreeNode {leaf}}} 
    {ReturnType {VoidTypeNode {leaf}}} 
  }}}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident SuperChild} 
         {Qualifier {NameNode { 
                     {Ident test} 
                     {Qualifier {NameNode { 
                                 {Ident java} 
                                 {Qualifier {NameNode { 
                                             {Ident lang} 
                                             {Qualifier {NameNode { 
                                                         {Ident ptolemy} 
                                                         {Qualifier {AbsentTreeNode {leaf}}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
       }}} 
  {SuperClass {TypeNameNode { 
               {Name {NameNode { 
                      {Ident Object} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}}
} {1}

######################################################################
####
#
test ASTReflect-21.1 {check out field use} {
    set class [ java::call Class forName "ptolemy.lang.java.test.UseFields"]
    set astList [java::call ptolemy.lang.java.ASTReflect fieldsASTList $class]
    lcompare [listToStrings $astList] {}
} {1}

######################################################################
####
#
test ASTReflect-22.1 {check out method Use} {
    set class [ java::call Class forName "ptolemy.lang.java.test.UseMethods"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    lcompare [listToStrings $astList] \
{{ {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident use} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {IntTypeNode {leaf}}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {TypeNameNode { 
               {Name {NameNode { 
                      {Ident Object} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}}}
} {1}

######################################################################
####
#
test ASTReflect-23.1 {pathNameToClass} {
    set fileSeparator [java::call System getProperty file.separator]
    set fileList [list java lang Object.java]
    set path [join $fileList $fileSeparator]
    set myclass [java::call ptolemy.lang.java.ASTReflect pathNameToClass $path]
    list [$myclass getName]
} {java.lang.Object}


######################################################################
####
#
test ASTReflect-23.2 {pathNameToClass} {
    set fileSeparator [java::call System getProperty file.separator]
    set fileList [list ptII ptolemy lang java ASTReflect]
    set path [join $fileList $fileSeparator]
    set myclass [java::call ptolemy.lang.java.ASTReflect pathNameToClass $path]
    list [$myclass getName]
} {ptolemy.lang.java.ASTReflect}


######################################################################
####
#
test ASTReflect-23.3 {pathNameToClass on a non-existant class} {
    set myclass [java::call ptolemy.lang.java.ASTReflect pathNameToClass "ptolemy/lang/java/NOTAClass"]
    list [java::isnull $myclass]
} {1}

######################################################################
####
#
test ASTReflect-23.4 {pathNameToClass} {
    set myclass [java::call ptolemy.lang.java.ASTReflect pathNameToClass "NOTAClass"]
    list [java::isnull $myclass]
} {1}

