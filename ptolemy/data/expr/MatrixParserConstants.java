/* Generated By:JJTree&JavaCC: Do not edit this line. MatrixParserConstants.java */
/* Parser for matrices written in matlab format.

 Copyright (c) 1998-2008 The Regents of the University of California.
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
package ptolemy.data.expr;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface MatrixParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int CONSTANT = 5;
  /** RegularExpression Id. */
  int FLOAT = 6;
  /** RegularExpression Id. */
  int INTEGER = 7;
  /** RegularExpression Id. */
  int DIGIT = 8;
  /** RegularExpression Id. */
  int NAME = 9;
  /** RegularExpression Id. */
  int NATURAL = 10;
  /** RegularExpression Id. */
  int SIGN = 11;
  /** RegularExpression Id. */
  int COMMENT = 12;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "<CONSTANT>",
    "<FLOAT>",
    "<INTEGER>",
    "<DIGIT>",
    "<NAME>",
    "<NATURAL>",
    "<SIGN>",
    "<COMMENT>",
    "\"[\"",
    "\";\"",
    "\"]\"",
    "\",\"",
  };

}
