/* Generated By:JavaCC: Do not edit this line. TokenMgrError.java Version 0.7pre2 */
/* 
   Copyright (c) 1998-1999 The Regents of the University of California.
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
   @AcceptedRating Yellow (cxh@eecs.berkeley.edu)

   Created : May 1998

*/

package ptolemy.data.expr;

public class TokenMgrError extends Error
{
    /*
     * Ordinals for various reasons why an Error of this type can be thrown.
    */

   /**
    * Lexical error occured.
    */
    static final int LEXICAL_ERROR = 0;

    /**
    * An attempt was made to create a second instance of a static
    * token manager.
    */
    static final int STATIC_LEXER_ERROR = 1;

    /**
    * Tried to change to an invalid lexical state.
    */
    static final int INVALID_LEXICAL_STATE = 2;

    /**
    * Detected (and bailed out of) an infinite loop in the token manager.
    */
    static final int LOOP_DETECTED = 3;

    /**
    * @serial Indicates the reason why the exception is thrown. It will have
    * one of the above 4 values.
    */
    int errorCode;

    /**
    * Replaces unprintable characters by their espaced (or unicode escaped)
    * equivalents in the given string
    */
    protected static final String addEscapes(String str) {
        StringBuffer retval = new StringBuffer();
        char ch;
        for (int i = 0; i < str.length(); i++) {
            switch (str.charAt(i))
                {
                case 0 :
                    continue;
                case '\b':
                    retval.append("\\b");
                    continue;
                case '\t':
                    retval.append("\\t");
                    continue;
                case '\n':
                    retval.append("\\n");
                    continue;
                case '\f':
                    retval.append("\\f");
                    continue;
                case '\r':
                    retval.append("\\r");
                    continue;
                case '\"':
                    retval.append("\\\"");
                    continue;
                case '\'':
                    retval.append("\\\'");
                    continue;
                case '\\':
                    retval.append("\\\\");
                    continue;
                default:
                    if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
                        String s = "0000" + Integer.toString(ch, 16);
                        retval.append("\\u" + s.substring(s.length() - 4,
                                s.length()));
                    } else {
                        retval.append(ch);
                    }
                    continue;
                }
        }
        return retval.toString();
    }

    /**
    * Returns a detailed message for the Error when it is thrown by the
    * token manager to indicate a lexical error.
    * Parameters : 
    *    EOFSeen     : indicates if EOF caused the lexicl error
    *    curLexState : lexical state in which this error occured
    *    errorLine   : line number when the error occured
    *    errorColumn : column number when the error occured
    *    errorAfter  : prefix that was seen before this error occured
    *    curchar     : the offending character
    * Note: You can customize the lexical error message by modifying
    * this method.
    */
    private static final String LexicalError(boolean EOFSeen, int lexState,
            int errorLine, int errorColumn, String errorAfter, char curChar) {
        return("Lexical error at line " +
                errorLine + ", column " +
                errorColumn + ".  Encountered: " +
                (EOFSeen ? "<EOF> " :
                        ("\"" + addEscapes(String.valueOf(curChar)) + "\"") +
                        " (" + (int)curChar + "), ") +
                "after : \"" + addEscapes(errorAfter) + "\"");
    }

    /**
    * You can also modify the body of this method to customize your
    * error messages.
    * For example, cases like LOOP_DETECTED and INVALID_LEXICAL_STATE are not
    * of end-users concern, so you can return something like : 
    *
    *     "Internal Error : Please file a bug report .... "
    *
    * from this method for such cases in the release version of your parser.
    */
    public String getMessage() {
        return super.getMessage();
    }

    /*
    * Constructors of various flavors follow.
    */

    public TokenMgrError() {
    }

    public TokenMgrError(String message, int reason) {
        super(message);
        errorCode = reason;
    }

    public TokenMgrError(boolean EOFSeen, int lexState, int errorLine,
            int errorColumn, String errorAfter, char curChar, int reason) {
        this(LexicalError(EOFSeen, lexState, errorLine,
                errorColumn, errorAfter, curChar), reason);
    }
}
