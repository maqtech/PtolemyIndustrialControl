/* Generated By:JJTree&JavaCC: Do not edit this line. PtParserConstants.java */
/* 
 Copyright (c) 1998-2004 The Regents of the University of California.
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
  int OPENPAREN = 16;
  int CLOSEPAREN = 17;
  int OPENBRACE = 18;
  int CLOSEBRACE = 19;
  int OPENBRACKET = 20;
  int CLOSEBRACKET = 21;
  int COMMA = 22;
  int PERIOD = 23;
  int COLON = 24;
  int QUESTION = 25;
  int GT = 26;
  int LT = 27;
  int GTE = 28;
  int LTE = 29;
  int NOTEQUALS = 30;
  int EQUALS = 31;
  int COND_AND = 32;
  int COND_OR = 33;
  int BOOL_NOT = 34;
  int BITWISE_NOT = 35;
  int AND = 36;
  int OR = 37;
  int XOR = 38;
  int SHL = 39;
  int SHR = 40;
  int LSHR = 41;
  int INTEGER = 42;
  int INTEGER_FORMAT_SPEC = 43;
  int DECIMAL_LITERAL = 44;
  int HEX_LITERAL = 45;
  int OCTAL_LITERAL = 46;
  int EXPONENT = 47;
  int DOUBLE = 48;
  int COMPLEX = 49;
  int BOOLEAN = 50;
  int FUNCTION = 51;
  int ID = 52;
  int LETTER = 53;
  int STRING = 54;
  int SETEQUALS = 55;
  int SEPARATOR = 56;
  int SMSTRING = 57;
  int SMDOLLAR = 58;
  int SMDOLLARBRACE = 59;
  int SMDOLLARPAREN = 60;
  int SMID = 61;
  int SMLETTER = 62;
  int SMIDBRACE = 63;
  int SMBRACE = 64;
  int SMIDPAREN = 65;
  int SMPAREN = 66;
  int ERROR = 67;

  int DEFAULT = 0;
  int SingleLineCommentMode = 1;
  int MultiLineCommentMode = 2;
  int StringModeIDBrace = 3;
  int StringModeIDParen = 4;
  int StringMode = 5;
  int StringModeIDNone = 6;

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
    "\"(\"",
    "\")\"",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "\",\"",
    "\".\"",
    "\":\"",
    "\"?\"",
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
    "\"=\"",
    "\";\"",
    "<SMSTRING>",
    "\"$\"",
    "\"${\"",
    "\"$(\"",
    "<SMID>",
    "<SMLETTER>",
    "<SMIDBRACE>",
    "<SMBRACE>",
    "<SMIDPAREN>",
    "<SMPAREN>",
    "<ERROR>",
  };

}
