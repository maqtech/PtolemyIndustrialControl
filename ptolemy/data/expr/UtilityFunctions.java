/* Class providing additional functions in the Ptolemy II expression language.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.StringToken;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.data.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.Properties;
import java.util.StringTokenizer;


//////////////////////////////////////////////////////////////////////////
//// UtilityFunctions
/**
This class providess additional functions for use in the Ptolemy II
expression language.  All of the methods in this class are static
and return an instance of Token.  The expression language identifies
the appropriate method to use by using reflection, matching the
types of the arguments.

@author  Neil Smyth, Christopher Hyland, Bart Kienhuis, Edward A. Lee
@version $Id$
*/
public class UtilityFunctions {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a Gaussian random number.
     *  @param mean The mean.
     *  @param standardDeviation The standard deviation.
     *  @return An observation of a Gaussian random variable.
     */
    public static DoubleToken gaussian(double mean, double standardDeviation) {
        if(_random == null) _random = new Random();
        double raw = _random.nextGaussian();
        double result = (raw*standardDeviation) + mean;
        return new DoubleToken(result);
    }

    /** Return a matrix of Gaussian random numbers.
     *  @param mean The mean.
     *  @param standardDeviation The standard deviation.
     *  @param rows The number of rows.
     *  @param columns The number of columns.
     *  @return A matrix of observations of a Gaussian random variable.
     */
    public static DoubleMatrixToken gaussian(
            double mean, double standardDeviation, int rows, int columns) {
        if(_random == null) _random = new Random();
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                double raw = _random.nextGaussian();
                result[i][j] = (raw*standardDeviation) + mean;
            }
        }
        return new DoubleMatrixToken(result);
    }

    /** Return the approximate number of bytes available for future 
     *  object allocation.  Note that requesting a garbage collection
     *  may change this value.
     *  @return The approximate number of bytes available.
     *  @see #totalMemory
     */
    public static LongToken freeMemory() {
	return new LongToken(Runtime.getRuntime().freeMemory());
    }

    /** FIXME. Placeholder for a function that will return a model.
     */
    public static ObjectToken model(String classname)
            throws IllegalActionException {
        return new ObjectToken(classname);
    }

    /** Get the specified property from the environment. An empty string
     *  is returned if the argument environment variable does not exist.
     *  See the javadoc page for java.util.System.getProperties() for
     *  a list of system properties.  An example property is
     *  "java.version", which returns the version of the JDK.
     *
     *  @param propertyName The name of property.
     *  @return A token containing the string value of the property.
     */
    public static StringToken property(String envName) {
        return new StringToken(System.getProperty(envName));
    }

    /** Get the string text contained in the specified file. For
     *  now this just looks in the directory where the parser
     *  is located, but will eventually (hopefully!) be able
     *  to use environment variables, user names etc. in
     *  creating a file path. An empty string
     *  is returned if the specified file could not be located.
     *  FIXME: what do with format of file?, e.g. if file is
     *  spread over many lines should we remove the newlines
     *  and make one long one line string?<p>
     *  Use readFile({@link #findFile}) to specify files relative to the
     *  current user directory or classpath.<p>
     *
     *  @param filename The file we want to read the text from.
     *  @return StringToken containing the text contained in
     *  the specified file.
     *  @exception IllegalActionException If for the given filename
     *  a file cannot be opened.
     */
    public static StringToken readFile(String filename)
            throws IllegalActionException {

        File fileT = new File(filename);
        //System.out.println("Trying to open file: " + fileT.toString());
        BufferedReader fin = null;
        String line;
        String result = "";
        String newline = System.getProperty("line.separator");
        try {
            if (fileT.exists()) {
                fin = new BufferedReader(new FileReader(fileT));
                while (true) {
                    try {
                        line = fin.readLine();
                    } catch (IOException e) {
                        break;
                    }

                    if (line == null) break;
                    result += line + newline;
                    //System.out.println("read in line: \"" +
                    //   line + newline + "\"");
                }
            }
        } catch (FileNotFoundException e) {
            // what should we do here?
            throw new IllegalActionException("File not found:\n" +
                    e.toString() );
        }
        //System.out.println("Contents of file are: " + result);
        return new StringToken(result);
    }

    /** Read a file that contains a matrix of reals in Matlab notation.
     *
     *  @param filename The filename.
     *  @return The matrix defined in the file.
     *  @exception IllegalActionException If the file cannot be opened.
     */
    public static DoubleMatrixToken readMatrix(String filename)
            throws IllegalActionException {

        DoubleMatrixToken returnMatrix = null;

        File fileT = new File(filename);
        FileReader fin = null;

        // Vector containing the matrix
        Vector k = null;

        // Parameters for the Matrix
        int row = -1;
        int column = -1;

        int posRow = 0;
        int posColumn = 0;
        double[][] mtr = null;

        if (fileT.exists()) {

            try {
                // Open the matrix file
                fin = new FileReader(fileT);
            } catch (FileNotFoundException e) {
                throw new IllegalActionException("FIle Not FOUND");
            }


            // Read the file and convert it into a matrix
            if(_matrixParser == null) {
                _matrixParser = new MatrixParser( System.in );
            }

            _matrixParser.ReInit( fin );
            k = _matrixParser.readMatrix( );

            if ( column == -1 ) {
                // The column size of the matrix
                column = k.size();
            }

            Iterator i = k.iterator();
            while( i.hasNext() ) {
                Vector l = (Vector) i.next();
                if ( row == -1 ) {
                    // the row size.
                    row = l.size();
                    // create a new matrix definition
                    mtr = new double[column][row];
                } else {
                    if ( row != l.size() ) {
                        throw new  IllegalActionException(" The Row" +
                                " size needs to be the same for all" +
                                " rows");
                    }
                }
                Iterator j = l.iterator();
                while( j.hasNext() ) {
                    Double s = (Double) j.next();
                    mtr[posColumn][posRow++] = s.doubleValue();
                }
                posRow=0;
                posColumn++;
            }

            // Vectors have now become obsolete, data is stored
            // in double[][].
            k.removeAll(k);
            returnMatrix =  new DoubleMatrixToken(mtr);
        } else {
            throw new IllegalActionException("ReadMatrix: File " +
                    filename + " not Found");
        }

        return returnMatrix;
    }

    /** Create an array that contains the specified element
     *  repeated the specified number of times.
     *  @param numberOfTimes The number of times to repeat the element.
     *  @param element The element to repeat.
     *  @return A new array containing the specified element repeated the
     *   specified number of times.
     */
    public static ArrayToken repeat(IntToken numberOfTimes, Token element) {
        int length = numberOfTimes.intValue();
        Token[] result = new Token[length];
        for(int i = 0; i < length; i++) {
            result[i] = element;
        }

	ArrayToken arrayToken;
	try {
	    arrayToken = new ArrayToken(result);
	} catch (IllegalArgumentException illegalAction) {
	    // This should not happen since the elements of the array always
	    // have the same type.
	    throw new InternalErrorException("UtilityFunctions.repeat: "
	        + "Cannot construct ArrayToken. " + illegalAction.getMessage());
        }
        return arrayToken;
    }

    /** Return the approximate number of bytes used by current objects
     *	and available for future object allocation.
     *  @return The total number of bytes used by the JVM.
     *  @see #freeMemory
     */
    public static LongToken totalMemory() {
	return new LongToken(Runtime.getRuntime().totalMemory());
    }

    /** Find a file. Uses the supplied name and if it does not exist as is,
     * searches the user directory followed by the current system
     * java.class.path list and returns the first match or name unchanged.
     * @param name Relative pathname of file/directory to find.
     * @return Canonical absolute path if file/directory was found, otherwise
     * returns unchanged name. */
    public static String findFile(String name) {
        File fileT = new File(name);
        if (!fileT.exists()) {
            String curDir = System.getProperty("user.dir");
            fileT = new File(curDir, name);
        }
        if (!fileT.exists()) {
            String cp = System.getProperty("java.class.path");
            StringTokenizer tokens = new StringTokenizer(cp, System.getProperty("path.separator"));
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                fileT = new File(token,name);
                if (fileT.exists()) break;
            }
        }
        if (fileT.exists()) {
            try {
                return fileT.getCanonicalPath();
            } catch (java.io.IOException ex) {
                return fileT.getAbsolutePath();
            }
        }
        else
            return name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Matrix Parser. The Matrix parser is recreated for the standard
     *  in. However, we use ReInit for the specific matrix files.
     */
    private static MatrixParser _matrixParser;

    /** The random number generator.
     */
    private static Random _random;
}
