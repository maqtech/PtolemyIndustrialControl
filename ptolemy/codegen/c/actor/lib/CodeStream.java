/*
  @Copyright (c) 2005 The Regents of the University of California.
  All rights reserved.

  Permission is hereby granted, without written agreement and without
  license or royalty fees, to use, copy, modify, and distribute this
  software and its documentation for any purpose, provided that the
  above copyright notice and the following two paragraphs appear in all
  copies of this software.

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
/*
 * Created on Feb 22, 2005
 *
 */
package ptolemy.codegen.c.actor.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.util.FileUtilities;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author Jackie
 *
 */
public class CodeStream {


    /**
     * CodeStream constructor associated with each actor helper.
     * Therefore, each actor should have its own codestream during code generation.
     * @param helper the actor helper associated with this code stream
     */
        public CodeStream (CCodeGeneratorHelper helper) {
        _actorHelper = helper;
    }

        /** To append the given string to this code stream
         * @param codeBlock the code block to be appended to this code stream
         */
    public void append(String codeBlock) {
        _stream.append(codeBlock);
    }

    /** To append the content of the StringBuffer to this code stream
     * @param codeBlock the code block to be appended to this code stream
     */
    public void append(StringBuffer codeBlock) {
        _stream.append(codeBlock);
    }

    /** To append the content of the CodeStream to this code stream
     * @param codeBlock the code block to be appended to this code stream
     */
    public void append(CodeStream codeBlock) {
        _stream.append(codeBlock.toString());
    }
   
    /** To append an specific code block. 
     * First, it checks if the code file is parsed already.
     * If so, it gets the code block from the well-constructed code block table.
     * If not, it has to construct the table. 
     * @param blockName the name of the code block
     * @throws IllegalActionException Thrown if an error occurs during parsing. 
     */
    public void appendCodeBlock(String blockName) throws IllegalActionException {
        if (_codeBlockTable == null) {
                _constructCodeBlockTable();
        }
        StringBuffer codeBlock = (StringBuffer) _codeBlockTable.get(blockName);
        if (codeBlock == null) {
            throw new IllegalActionException ("Cannot find code block: " + blockName);        
        }
        
        _stream.append(codeBlock);
    }

    /** 
     * Return the string representation of the code stream
     */
    public String toString() {
        return _stream.toString();
    }

    
    /**
     * This method reads the .c file associate with the particular actor 
     * identified by the className and constructs the code block table. 
     * 
     * @throws IllegalActionException Thrown if an error occurs during parsing. 
     */
    private void _constructCodeBlockTable() throws IllegalActionException {
        String className; 
        if (_actorHelper == null) {
                className = _testingClassName;
        } else {
                className = _actorHelper.getComponent().getClassName();
            className = className.substring(className.lastIndexOf(".") + 1);
        }
        
        _codeBlockTable = new Hashtable();
        BufferedReader reader = null;
        try {
            // open the .c file for reading
            reader = FileUtilities.openForReading(_filePath + className + ".c", null, null);
            StringBuffer codeInFile = new StringBuffer();

            // create a string of all code in the file
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                codeInFile.append(line+"\n");
            }
            
            // recursively parse the file
            while (_parseCodeBlock(codeInFile) != null); 
            
        } catch (IOException e) {
            if (reader == null) {
                throw new IllegalActionException (null, e, "Cannot open file: " + className + ".c");
            } else { 
                throw new IllegalActionException (null, e, "Error reading file: " + className + ".c");
            }
        }
    }
 
    /** This method parse code from the given StringBuffer. It is
     * responsible for putting the first single (nested or non-nested) 
     * code block along with the block name (key) into the code block table.
     * It calls sub-parsing functions parseHeader() and parseBody().
     * It returns the name of the code block (key). If null is returned, it means
     * there is no more code block to be parsed in the .c file.
         * 
     * @param codeInFile all the text within the .c file
         * @return the name of the code block as key to _CodeBlockTable
     * @throws IllegalActionException Thrown if an error occurs during parsing. 
         */
        private String _parseCodeBlock(StringBuffer codeInFile) throws IllegalActionException {
        String name = _parseHeader(codeInFile);
        if (name != null) {
            StringBuffer body = _parseBody(codeInFile);
            _codeBlockTable.put(name, body);
        }
                return name;
        }

        /** 
     * A sub-parsing method which return a well-formed string 
     * representing the code block name.
     * 
         * @param codeInFile all the text within the .c file
         * @return the name of the code block as key to _CodeBlockTable
         * @throws IllegalActionException Thrown if an error occurs when parsing
     * the header. 
         */
        private String _parseHeader(StringBuffer codeInFile) throws IllegalActionException {
        
        _parseIndex = codeInFile.indexOf(_BLOCKSTART, _parseIndex);

        // Check to see if there are no more code block start headers.
        if (_parseIndex == -1) {
            return null;
        }
        
        _parseIndex += _BLOCKSTART.length();        
        int endIndex = codeInFile.indexOf(_HEADEREND, _parseIndex);
        
        if (endIndex == -1) {
            throw new IllegalActionException("Missing code block close header");
        }
        
        String name = _checkCodeHeader(codeInFile.substring(_parseIndex, endIndex));
        _parseIndex = _HEADEREND.length() + endIndex; 
                return name;
        }

    /**
     * Type-checks the format of a given code block name.
     * Assume the code block name is short (that is the reason why String
     * is used instead of StringBuffer).
     * This method returns a well-formed code block name,
     * otherwise, it throws an exception.
     * 
         * @param name the header string
     * @return the well-formed header string after lexical checking
         */
        private static String _checkCodeHeader(String name) {
        // FIXME: extra lexical checking 
        // e.g. Do we allow nested code block within code block name??
        // e.g. ...spaces??
        // e.g. ...special symbols??
                return name.trim();
        }

        /**
     * A Sub-parsing method which returns the code block body.
     * It recursively parses within the code body for nested code blocks.
     * 
     * @param codeInFile all the text within the .c file
     * @return the code body within the current code block
     * @throws IllegalActionException Thrown if an error occurs when parsing 
     * the code body. 
     */
    private StringBuffer _parseBody(StringBuffer codeInFile) throws IllegalActionException {
        int openBlock = 1;
        int scanIndex = _parseIndex;

        int startIndex, endIndex = -1;        
        while (openBlock > 0) {
            endIndex = codeInFile.indexOf(_BLOCKEND, scanIndex);
                startIndex = codeInFile.indexOf(_BLOCKSTART, scanIndex);
            if (startIndex < endIndex && startIndex != -1) {
                    openBlock++;
                scanIndex = startIndex+1;
            }
            else {
                openBlock--;                
                scanIndex = endIndex+1;
            }
        }
        
        if (endIndex == -1) 
            throw new IllegalActionException("Missing close block");

        StringBuffer body = new StringBuffer(codeInFile.substring(_parseIndex, endIndex));

        // Recursively parsing for nested code blocks
        for (String subBlockKey = _parseCodeBlock(codeInFile); subBlockKey != null; ) {       
            // FIXME: do we include the nested code block into the current block??
            //body.append((StringBuffer) _codeBlockTable.get(subBlockKey));
            
            // FIXME: take away the nested code block from the current code block
            subBlockKey = _parseCodeBlock(codeInFile);
        }
        _parseIndex = _BLOCKEND.length() + endIndex; 
        return body;
    }

    ///////////////////////////////////////////////////////////////////////
    // private variables declarations
    ///////////////////////////////////////////////////////////////////////

    
    /**
     * Index pointer that indicates the current location
     * within the .c file to be parsed.
     */
    private int _parseIndex = 0;
    
        /**
     * The content of this CodeStream.
     */
    private StringBuffer _stream = new StringBuffer();
   
    /**
     * The code block table that stores the code blocks with
     * code block names (String) as keys.
     */
    private Hashtable _codeBlockTable = null;
    
    /**
     * The associated actor helper object.
     */
    private CCodeGeneratorHelper _actorHelper;

    /** 
     * File path to the .c files
     */
    private static String _filePath = "$CLASSPATH" + "/ptolemy/codegen/c/actor/lib/";

    /**
     * String pattern which represents the start of a code block.
     */
    private static String _BLOCKSTART = "/***";
    
    /**
     * String pattern which represents the end of a code block header.
     */
    private static String _HEADEREND = "*/";

    /**
     * String pattern which represents the end of a code block.
     * Both _BLOCKSTART and _BLOCKEND cannot be the prefix of the other.
     */
    private static String _BLOCKEND = "/**/";
    
    
    
    
    
    //============================================================================
 
    /**
     * The class name used during testing. 
     */
    private static String _testingClassName;
    
    /**
     * Private test method which returns a StringBuffer that contains all 
     * the code block names and bodies, given the name of an actor class.
     * 
     * @throws IllegalActionException Throw if an error occurs during parsing.
     */
    private static StringBuffer _testing() throws IllegalActionException {
        StringBuffer buffer = new StringBuffer();
            CodeStream stream = new CodeStream(null);
        if (stream._codeBlockTable == null) {
            stream._constructCodeBlockTable();
        }
        
        for (Iterator keys = stream._codeBlockTable.keySet().iterator(); keys.hasNext();) {
                String key = (String) keys.next();
            buffer.append(key + ": \n");
            buffer.append((StringBuffer) stream._codeBlockTable.get(key));
            buffer.append("\n-------------------------------\n\n");
        }
        
        return buffer;
    }
    
    /**
     * Testing main
     * 
     * @param arg command-line arguments
     * @throws IOException Thrown if an error occurs when reading user inputs. 
     * @throws IllegalActionException Throw if an error occurs during parsing.
     */
    public static void main(String[] arg) throws IOException, IllegalActionException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("----------Testing--------------------------------");
        System.out.print("please input class name: ");
        _testingClassName = in.readLine();
        System.out.println("\n----------Result----------------------------------");

        System.out.println(CodeStream._testing());
    }
}
