/* Generated By:JJTree&JavaCC: Do not edit this line. PtParserConstants.java */
/*
 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

public interface PtParserConstants {

  int EOF = 0;
  int SINGLE_LINE_COMMENT = 3;
  int MULTI_LINE_COMMENT = 4;
  int PLUS = 10;
  int MINUS = 11;
  int MULTIPLY = 12;
  int DIVIDE = 13;
  int MODULO = 14;
  int POWER = 15;
  int GT = 16;
  int LT = 17;
  int GTE = 18;
  int LTE = 19;
  int NOTEQUALS = 20;
  int EQUALS = 21;
  int COND_AND = 22;
  int COND_OR = 23;
  int BOOL_NOT = 24;
  int BITWISE_NOT = 25;
  int AND = 26;
  int OR = 27;
  int XOR = 28;
  int SHL = 29;
  int SHR = 30;
  int LSHR = 31;
  int INTEGER = 32;
  int INTEGER_FORMAT_SPEC = 33;
  int DECIMAL_LITERAL = 34;
  int HEX_LITERAL = 35;
  int OCTAL_LITERAL = 36;
  int EXPONENT = 37;
  int DOUBLE = 38;
  int COMPLEX = 39;
  int BOOLEAN = 40;
  int FUNCTION = 41;
  int ID = 42;
  int LETTER = 43;
  int STRING = 44;

  int DEFAULT = 0;
  int IN_SINGLE_LINE_COMMENT = 1;
  int IN_MULTI_LINE_COMMENT = 2;
  int IN_FORMAL_COMMENT = 3;

  String[] tokenImage = {
    "<EOF>",
    "\"//\"",
    "\"/*\"",
    "<SINGLE_LINE_COMMENT>",
    "\"*/\"",
    "<token of kind 5>",
    "\" \"",
    "\"\\r\"",
    "\"\\t\"",
    "\"\\n\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"%\"",
    "\"^\"",
    "\">\"",
    "\"<\"",
    "\">=\"",
    "\"<=\"",
    "\"!=\"",
    "\"==\"",
    "\"&&\"",
    "\"||\"",
    "\"!\"",
    "\"~\"",
    "\"&\"",
    "\"|\"",
    "\"#\"",
    "\"<<\"",
    "\">>\"",
    "\">>>\"",
    "<INTEGER>",
    "<INTEGER_FORMAT_SPEC>",
    "<DECIMAL_LITERAL>",
    "<HEX_LITERAL>",
    "<OCTAL_LITERAL>",
    "<EXPONENT>",
    "<DOUBLE>",
    "<COMPLEX>",
    "<BOOLEAN>",
    "\"function\"",
    "<ID>",
    "<LETTER>",
    "<STRING>",
    "\"(\"",
    "\"?\"",
    "\":\"",
    "\".\"",
    "\",\"",
    "\")\"",
    "\"[\"",
    "\"{\"",
    "\"=\"",
    "\";\"",
    "\"]\"",
    "\"}\"",
  };

}
