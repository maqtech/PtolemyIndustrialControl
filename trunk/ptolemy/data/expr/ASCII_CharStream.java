/* Generated By:JavaCC: Do not edit this line. ASCII_CharStream.java Version 0.7pre6 */
/* A Ptolemy application specified as an instance of CompositeActor.

   Copyright (c) 1999-2001 The Regents of the University of California.
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
   @AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.data.expr;

/**
An implementation of interface CharStream, where the stream is assumed to
contain only ASCII characters (without unicode processing).

@author Neil Smyth
@version $Id$
*/

public final class ASCII_CharStream
{
    public static final boolean staticFlag = false;
    int bufsize;
    int available;
    int tokenBegin;
    public int bufpos = -1;
    public final char BeginToken() throws java.io.IOException
        {
            tokenBegin = -1;
            char c = readChar();
            tokenBegin = bufpos;

            return c;
        }

    public final char readChar() throws java.io.IOException
        {
            if (inBuf > 0)
                {
                    --inBuf;
                    return (char)((char)0xff &
                            buffer[(bufpos == bufsize - 1) ?
                                    (bufpos = 0) : ++bufpos]);
                }

            if (++bufpos >= maxNextCharInd)
                FillBuff();

            char c = (char)((char)0xff & buffer[bufpos]);

            UpdateLineColumn(c);
            return (c);
        }

    /**
     * @deprecated
     * @see #getEndColumn
     */

    public final int getColumn() {
        return bufcolumn[bufpos];
    }

    /**
     * @deprecated
     * @see #getEndLine
     */

    public final int getLine() {
        return bufline[bufpos];
    }

    public final int getEndColumn() {
        return bufcolumn[bufpos];
    }

    public final int getEndLine() {
        return bufline[bufpos];
    }

    public final int getBeginColumn() {
        return bufcolumn[tokenBegin];
    }

    public final int getBeginLine() {
        return bufline[tokenBegin];
    }

    public final void backup(int amount) {

        inBuf += amount;
        if ((bufpos -= amount) < 0)
            bufpos += bufsize;
    }

    public ASCII_CharStream(java.io.Reader dstream, int startline,
            int startcolumn, int buffersize)
        {
            inputStream = dstream;
            line = startline;
            column = startcolumn - 1;

            available = bufsize = buffersize;
            buffer = new char[buffersize];
            bufline = new int[buffersize];
            bufcolumn = new int[buffersize];
        }

    // This was 4096;
    private static final int INIT_BUFFER_SIZE = 32;

    public ASCII_CharStream(java.io.Reader dstream, int startline,
            int startcolumn)
        {
            this(dstream, startline, startcolumn, INIT_BUFFER_SIZE);
        }
    public void ReInit(java.io.Reader dstream, int startline,
            int startcolumn, int buffersize)
        {
	    // System.out.println("ASCI_CharStream.ReInit(): buffersize="
	    //                    + buffersize);
	    // leak?
	    try {
		// System.out.println("ASCI_CharStream.ReInit(): closing "
		//		   + inputStream);
		inputStream.close();
	    } catch (Exception ex) {
		throw new RuntimeException("ASCII_CharStream: close failed? " 
					   + ex);
	    }
            inputStream = dstream;
            line = startline;
            column = startcolumn - 1;

            if (buffer == null || buffersize != buffer.length)
                {
                    available = bufsize = buffersize;
                    buffer = new char[buffersize];
                    bufline = new int[buffersize];
                    bufcolumn = new int[buffersize];
                }
            prevCharIsLF = prevCharIsCR = false;
            tokenBegin = inBuf = maxNextCharInd = 0;
            bufpos = -1;
        }

    public void ReInit(java.io.Reader dstream, int startline,
            int startcolumn)
        {
            ReInit(dstream, startline, startcolumn, INIT_BUFFER_SIZE);
        }
    public ASCII_CharStream(java.io.InputStream dstream, int startline,
            int startcolumn, int buffersize)
        {
	    
            this(new java.io.InputStreamReader(dstream), startline,
                    startcolumn, INIT_BUFFER_SIZE);
        }

    public ASCII_CharStream(java.io.InputStream dstream, int startline,
            int startcolumn)
        {
            this(dstream, startline, startcolumn, INIT_BUFFER_SIZE);
        }

    public void ReInit(java.io.InputStream dstream, int startline,
            int startcolumn, int buffersize)
        {
            ReInit(new java.io.InputStreamReader(dstream), startline,
                    startcolumn, INIT_BUFFER_SIZE);
        }
    public void ReInit(java.io.InputStream dstream, int startline,
            int startcolumn)
        {
            ReInit(dstream, startline, startcolumn, INIT_BUFFER_SIZE);
        }
    public final String GetImage()
        {
            if (bufpos >= tokenBegin)
                return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);
            else
                return new String(buffer, tokenBegin, bufsize - tokenBegin) +
                    new String(buffer, 0, bufpos + 1);
        }

    public final char[] GetSuffix(int len)
        {
            char[] ret = new char[len];

            if ((bufpos + 1) >= len)
                System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);
            else
                {
                    System.arraycopy(buffer, bufsize - (len - bufpos - 1),
                            ret, 0, len - bufpos - 1);
                    System.arraycopy(buffer, 0,
                            ret, len - bufpos - 1, bufpos + 1);
                }

            return ret;
        }

    public void Done()
        {
            buffer = null;
            bufline = null;
            bufcolumn = null;
        }

    /**
     * Method to adjust line and column numbers for the start of a token.<BR>
     */
    public void adjustBeginLineColumn(int newLine, int newCol)
        {
            int start = tokenBegin;
            int len;

            if (bufpos >= tokenBegin)
                {
                    len = bufpos - tokenBegin + inBuf + 1;
                }
            else
                {
                    len = bufsize - tokenBegin + bufpos + 1 + inBuf;
                }

            int i = 0, j = 0, k = 0;
            int nextColDiff = 0, columnDiff = 0;

            while (i < len &&
                    bufline[j = start % bufsize] ==
                    bufline[k = ++start % bufsize])
                {
                    bufline[j] = newLine;
                    nextColDiff = columnDiff + bufcolumn[k] - bufcolumn[j];
                    bufcolumn[j] = newCol + columnDiff;
                    columnDiff = nextColDiff;
                    i++;
                }

            if (i < len)
                {
                    bufline[j] = newLine++;
                    bufcolumn[j] = newCol + columnDiff;

                    while (i++ < len)
                        {
                            if (bufline[j = start % bufsize] !=
                                    bufline[++start % bufsize])
                                bufline[j] = newLine++;
                            else
                                bufline[j] = newLine;
                        }
                }

            line = bufline[j];
            column = bufcolumn[j];
        }

    private final void ExpandBuff(boolean wrapAround)
        {
	    // System.out.println("ASCII_CharStream.ExpandBuff(): "
	    //                    + "expanding by 2x " + (bufsize*2) );
	    /*
            char[] newbuffer = new char[bufsize + 2048];
            int newbufline[] = new int[bufsize + 2048];
            int newbufcolumn[] = new int[bufsize + 2048];
	    */
            char[] newbuffer = new char[bufsize * 2];
            int newbufline[] = new int[bufsize * 2];
            int newbufcolumn[] = new int[bufsize * 2];

            try
                {
                    if (wrapAround)
                        {
                            System.arraycopy(buffer, tokenBegin,
                                    newbuffer, 0, bufsize - tokenBegin);
                            System.arraycopy(buffer, 0, newbuffer,
                                    bufsize - tokenBegin, bufpos);
                            buffer = newbuffer;

                            System.arraycopy(bufline, tokenBegin,
                                    newbufline, 0, bufsize - tokenBegin);
                            System.arraycopy(bufline, 0,
                                    newbufline, bufsize - tokenBegin, bufpos);
                            bufline = newbufline;

                            System.arraycopy(bufcolumn, tokenBegin,
                                    newbufcolumn, 0, bufsize - tokenBegin);
                            System.arraycopy(bufcolumn, 0, newbufcolumn,
                                    bufsize - tokenBegin, bufpos);
                            bufcolumn = newbufcolumn;

                            maxNextCharInd = (bufpos += (bufsize - tokenBegin));
                        }
                    else
                        {
                            System.arraycopy(buffer, tokenBegin,
                                    newbuffer, 0, bufsize - tokenBegin);
                            buffer = newbuffer;

                            System.arraycopy(bufline, tokenBegin,
                                    newbufline, 0, bufsize - tokenBegin);
                            bufline = newbufline;

                            System.arraycopy(bufcolumn, tokenBegin,
                                    newbufcolumn, 0, bufsize - tokenBegin);
                            bufcolumn = newbufcolumn;

                            maxNextCharInd = (bufpos -= tokenBegin);
                        }
                }
            catch (Throwable t)
                {
                    throw new Error(t.getMessage());
                }


            //bufsize += 2048;
	    // I am so cool!
            bufsize += bufsize;
            available = bufsize;
            tokenBegin = 0;
        }


    private final void FillBuff() throws java.io.IOException
        {
            if (maxNextCharInd == available)
                {
                    if (available == bufsize)
                        {
                            if (tokenBegin > /*2048*/ (bufsize*2))
                                {
                                    bufpos = maxNextCharInd = 0;
                                    available = tokenBegin;
                                }
                            else if (tokenBegin < 0)
                                bufpos = maxNextCharInd = 0;
                            else
                                ExpandBuff(false);
                        }
                    else if (available > tokenBegin)
                        available = bufsize;
                    else if ((tokenBegin - available) < /*2048*/ (bufsize*2))
                        ExpandBuff(true);
                    else
                        available = tokenBegin;
                }

            int i;
            try {
                if ((i = inputStream.read(buffer, maxNextCharInd,
                        available - maxNextCharInd)) == -1)
                    {
                        inputStream.close();
                        throw new java.io.IOException();
                    }
                else
                    maxNextCharInd += i;
                return;
            }
            catch(java.io.IOException e) {
                --bufpos;
                backup(0);
                if (tokenBegin == -1)
                    tokenBegin = bufpos;
                throw e;
            }
        }

    private final void UpdateLineColumn(char c)
        {
            column++;

            if (prevCharIsLF)
                {
                    prevCharIsLF = false;
                    line += (column = 1);
                }
            else if (prevCharIsCR)
                {
                    prevCharIsCR = false;
                    if (c == '\n')
                        {
                            prevCharIsLF = true;
                        }
                    else
                        line += (column = 1);
                }

            switch (c)
                {
                case '\r' :
                    prevCharIsCR = true;
                    break;
                case '\n' :
                    prevCharIsLF = true;
                    break;
                case '\t' :
                    column--;
                    column += (8 - (column & 07));
                    break;
                default :
                    break;
                }

            bufline[bufpos] = line;
            bufcolumn[bufpos] = column;
        }

    private int bufline[];
    private int bufcolumn[];

    private int column = 0;
    private int line = 1;

    private boolean prevCharIsCR = false;
    private boolean prevCharIsLF = false;

    private java.io.Reader inputStream;

    private char[] buffer;
    private int maxNextCharInd = 0;
    private int inBuf = 0;


}
