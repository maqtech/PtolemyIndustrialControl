/* Class providing additional functions in the Ptolemy II expression language.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

import ptolemy.data.StringToken;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;


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
     *  and make one long one line string? Also this currently
     *  only looks in the working directory.
     *
     *  @param filename The file we want to read the text from.
     *  @return StringToken containing the text contained in
     *  the specified file.
     *  @exception IllegalActionException If for the given filename
     *  a file cannot be opened.
     */
    public static StringToken readFile(String filename)
            throws IllegalActionException {

        // temporary hack, need to work out way to obtain the path.
        String curDir = System.getProperty("user.dir");

        //System.out.println("Directory is " + curDir);
        File fileT = new File(curDir, filename);
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

        // Matlab Matrices always start at 1 instead of 0.
        int posRow = 1;
        int posColumn = 1;
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
                    mtr = new double[column+1][row+1];
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
                posRow=1;
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
