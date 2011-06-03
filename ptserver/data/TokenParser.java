/*
 Convert a token to a byte stream and back

 Copyright (c) 2011 The Regents of the University of California.
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
package ptserver.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptserver.data.handler.TokenHandler;

///////////////////////////////////////////////////////////////////
////TokenParser
/**
* <p>This class is a singleton and is a central point for converting
* a token from a byte stream and back.</p>
*
* <p>When the instances is created, a mapping from Token to its TokenHandlers
* is loaded from ptserver.data.TokenHandlers.properties ResourceBundle.
* This class fill parse a token from an input stream by figuring out
* its token identifier (based on the position of the TokenHandler in the TokenHandlers.properties)
* and then selecting appropriate TokenHandler to do the parsing</p>
*
* <p>Similarly, the token would be converted to a byte stream by selecting
* a TokenHandler mapped to its class and using it to do the conversion.</p>
*
* @author ahuseyno
* @version $Id$
* @since Ptolemy II 8.0
* @Pt.ProposedRating Red (ahuseyno)
* @Pt.AcceptedRating Red (ahuseyno)
*/
public class TokenParser {

    /**
     * <p>This private constructor loads mappings from a token class to
     * its TokenHandler from TokenHandlers.properties file which is located in ptserver/data directory.</p>
     *
     * <p>This constructor is private because the TokenParser is singleton which makes it easy to locate the instance without passing it around.</p>
     * @exception IllegalActionException if there is a problem loading the mapping from TokenHandlers.properties file.
     */
    private TokenParser() throws IllegalActionException {
        //key is the token class name
            //We have to use ResourceBundle.getKeys() method because Android does not support .keySet() method.
        Enumeration<String> keys = _tokenHandlersBundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            //value is the token handler class name
            String value = _tokenHandlersBundle.getString(key);
            try {
                ClassLoader classLoader = this.getClass().getClassLoader();
                Class<Token> tokenClass = (Class<Token>) classLoader
                        .loadClass(key);
                TokenHandler<Token> tokenHandler = (TokenHandler<Token>) classLoader
                        .loadClass(value).newInstance();
                HandlerData<Token> data = new HandlerData<Token>(tokenHandler,
                        tokenClass, (short) _handlerList.size());
                _handlerMap.put(tokenClass, data);
                _handlerList.add(data);
            } catch (ClassNotFoundException e) {
                throw new IllegalActionException(null, e,
                        "Class was not found for " + key + " or " + value);
            } catch (InstantiationException e) {
                throw new IllegalActionException(null, e,
                        "System could not instantiate instance " + value);
            } catch (IllegalAccessException e) {
                throw new IllegalActionException(null, e,
                        "System could not instantiate instance " + value);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return singleton instance of the TokenParser.  The TokenParser is singleton to simplify operations with it since
     * there is no need to pass the instance around.  Also instantiation of the instance is costly since it relies on reflection.
     * @return instance of the TokenParser
     * @exception IllegalActionException if there is a problem loading the mapping from TokenHandlers.properties file.
     */
    public static TokenParser getInstance() throws IllegalActionException {
        if (_instance == null) {
            _instance = new TokenParser();
        }
        return _instance;
    }

    /**
     * Convert the token to a byte stream by first finding its TokenHandler and
     * writing its position to the stream (first 2 bytes) followed by the conversion produced TokenHandler.
     * @param <T> Type of the target token
     * @param token Token to be converted
     * @param outputStream outputStream used for the resulting byte stream
     * @exception IOException if there is a problem with the outputStream
     * @exception IllegalActionException if the state becomes inconsistent
     */
    public <T extends Token> void convertToBytes(T token,
            DataOutputStream outputStream) throws IOException,
            IllegalActionException {
        HandlerData<T> handlerData = (HandlerData<T>) _handlerMap.get(token
                .getClass());
        if (handlerData == null) {
            throw new NullPointerException("No handler found for type "
                    + token.getClass());
        }
        outputStream.writeShort(handlerData._position);
        handlerData._tokenHandler.convertToBytes(token, outputStream);
    }

    /**
     * Convert the token to a byte stream by first finding its TokenHandler and
     * writing its position to the stream (first 2 bytes) followed by the conversion produced TokenHandler.
     * @param <T> Type of the target token
     * @param token Token to be converted
     * @param outputStream outputStream used for the resulting byte stream
     * @exception IOException if there is a problem with the outputStream
     * @exception IllegalActionException if the state becomes inconsistent
     */
    public <T extends Token> void convertToBytes(T token,
            OutputStream outputStream) throws IOException,
            IllegalActionException {
        convertToBytes(token, new DataOutputStream(outputStream));
    }

    /**
     * Read and parse the inputStream in order to recreate the Token.
     * @param <T> Type of Token
     * @param inputStream InputStream containing byteStream of the token data
     * where first 2 bytes indicated the token type (defined as TokenHandlers position)
     * @return Token read from the inputStream
     * @exception IOException is thrown in case of a problem with the outputStream
     * @exception IllegalActionException is thrown if the state becomes inconsistent
     */
    public <T extends Token> T convertToToken(DataInputStream inputStream)
            throws IOException, IllegalActionException {
        short position = inputStream.readShort();
        HandlerData<T> data = (HandlerData<T>) _handlerList.get(position);
        if (data == null) {
            throw new NullPointerException("No handler found for position "
                    + position);
        }
        return data._tokenHandler.convertToToken(inputStream, data._tokenType);
    }

    /**
     * Read and parse the inputStream in order to recreate the Token.
     * @param <T> Type of Token
     * @param inputStream InputStream containing byteStream of the token data
     * where first 2 bytes indicated the token type (defined as TokenHandlers position)
     * @return Token read from the inputStream
     * @exception IOException is thrown in case of a problem with the outputStream
     * @exception IllegalActionException is thrown if the state becomes inconsistent
     */
    public <T extends Token> T convertToToken(InputStream inputStream)
            throws IOException, IllegalActionException {
        return this.<T> convertToToken(new DataInputStream(inputStream));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Singleton instance of the parser.
     */
    private static TokenParser _instance;

    /**
     * ResourceBundle containing mapping from token type to its handler.
     */
    private static final ResourceBundle _tokenHandlersBundle = ResourceBundle
            .getBundle("ptserver.data.TokenHandlers");

    /**
     * Mapping from token type to its handler instance
     */
    private final HashMap<Class<? extends Token>, HandlerData<?>> _handlerMap = new HashMap<Class<? extends Token>, HandlerData<?>>();

    /**
     * List of HandlerData files ordered according to their position.
     * Position is needed to figure out token data type on a byte stream.
     */
    private final ArrayList<HandlerData<?>> _handlerList = new ArrayList<HandlerData<?>>();

    /**
     * Data structure that stores handler, token type and position tuple
     */
    private class HandlerData<T extends Token> {

        /**
         * Create new instance of HandlerData
         * @param tokenHandler the tokenHandler for the tokenType
         * @param tokenType the tokenType to convert
         * @param position the identifier of the token type used in the byte stream
         */
        public HandlerData(TokenHandler<T> tokenHandler, Class<T> tokenType,
                short position) {
            this._tokenHandler = tokenHandler;
            this._tokenType = tokenType;
            this._position = position;
        }

        /**
         * Token Handler for the tokenType
         */
        private final TokenHandler<T> _tokenHandler;
        /**
         * The token type handled by the tokenHandler
         */
        private final Class<T> _tokenType;
        /**
         * Identifier of the token type
         */
        private final int _position;
    }
}
