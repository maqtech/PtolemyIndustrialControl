/* An actor that gets stock prices from the web.

 Copyright (c) 1997-2002 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.actor.lib.jspaces.demo.Stock;

import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Source;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.StreamTokenizer;

//////////////////////////////////////////////////////////////////////////
//// StockQuote
/**

This actor gets the stock price for http://quote.yahoo.com and
sends it to the output port. The ticker symbol is specified in
the ticker parameter.

@author Yuhong Xiong, Jie Liu
@version $Id$
@since Ptolemy II 1.0
*/
public class StockQuote extends Source {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StockQuote(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

	ticker = new Parameter(this, "ticker", new StringToken("YHOO"));
	ticker.setTypeEquals(BaseType.STRING);

	// set the type constraints.
	output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Stock ticker. The default ticker is "YHOO".
     */
    public Parameter ticker;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the stock price from the web and send it to the output.
     *  @exception IllegalActionException If the URL or ticker is invalid;
     *   or if an I/O exception occurs when accessing the URL.
     */
    public void fire() throws IllegalActionException {
        try {
            super.fire();
	    String tickerString =
                ((StringToken)ticker.getToken()).stringValue();
	    String spec = (_urlString + tickerString);
	    URL url = new URL(spec);
	    InputStream stream = url.openStream();

	    // The part of the returned HTML file from Yahoo that contains
	    // the stock quote has the form:
	    //
	    // <td nowrap align=left><a href="/q?s=FDX&d=t">FDX</a></td> ...
	    // <td nowrap><b>49.06</b>
	    //
	    // or
	    //
	    // <td nowrap align=left><a href="/q?s=YHOO&d=t">YHOO</a></td> ...
	    // <td nowrap><b>37 <sup>1</sup>/<sub>8</sub></b>
	    //
	    // To get the quote, a StreamTokenizer is used to extract each
	    // line as a token, find the line that contains "/q?s=YHOO&d=t",
	    // and extract number between <b> and </b>.
	    // Note that the quote may be in either the decimal or fractional
	    // form.
	    // This code is fragile. If Yahoo changes the quote format, it
	    // will break.

	    Reader r = new BufferedReader(new InputStreamReader(stream));
	    StreamTokenizer st = new StreamTokenizer(r);
	    // set newline(10) to be the only white space.
	    st.resetSyntax();
	    st.whitespaceChars(10, 10);
	    // every char from space(32) up is a word constituents.
	    st.wordChars(32, 127);

	    while (st.nextToken() != StreamTokenizer.TT_EOF
                    && st.sval != null) {
                //System.out.println(st);
		if (st.sval.indexOf("No such ticker symbol") != -1) {
		    // bad ticker
		    throw new IllegalActionException(this, "StockQuote.fire:"
                            + " bad ticker: " + tickerString);
		}
		if (st.sval.startsWith(_matchString + tickerString)) {
		    // found the line
		    break;
		}
	    }

            if (st.sval == null) {
		    throw new IllegalActionException(this, "StockQuote.fire:"
                            + " Did not find tickerString: " + tickerString
                            + " in " + spec + ".\n" 
                            + " Perhaps the format changed,"
                            + " Try changing _matchString");

            }

	    // st.sval contains the quote now.
	    // the quote is between <b> and </b>
	    int index1 = st.sval.indexOf("<b>");
	    index1 += 3;
	    // index1 is now the index of the first digit of the quote.
	    int index2 = st.sval.indexOf("<", index1);
	    String stringQuote = st.sval.substring(index1, index2);

	    // If the quote is in decimal, stringQuote contains the whole
	    // quote string; If the quote is in fractional, stringQuote
	    // may have a trailing white space.
	    double price = (Double.valueOf(stringQuote)).doubleValue();

	    int index = st.sval.indexOf("sup");
	    if (index != -1) {
		// price has fractional part.
		double sup = _getNumber(st.sval, index+4);
		index = st.sval.indexOf("sub");
		double sub = _getNumber(st.sval, index+4);

		price += sup/sub;
	    }
            output.send(0, new DoubleToken(price));

        } catch (MalformedURLException mfurl) {
	    throw new IllegalActionException(this, mfurl.getMessage());
	} catch (IOException io) {
	    throw new IllegalActionException(this, io.getMessage());
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Get the number in the string from the start index. Cast the
    // number to double before return.
    private double _getNumber(String str, int index) {
	int num = 0;
	char c = str.charAt(index);
	while (Character.isDigit(c)) {
	    num = num*10 + Character.digit(c, 10);
	    index++;
	    c=str.charAt(index);
	}
	return (double)num;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _urlString = "http://quote.yahoo.com/q?s=";
    private String _matchString = 
        "<td nowrap align=left><font face=arial size=-1><a href=\"/q?s=";
}

