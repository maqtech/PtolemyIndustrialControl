/* Creates and sends a key to a SymmetricDecryption and encrypts incoming data
   based on a given symmetric algorithm.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Yellow (rnreddy@andrew.cmu.edu)
@AcceptedRating Red (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import ptolemy.actor.NoRoomException;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SymmetricEncryption
/**
This actor takes an unsigned byte array at the input and encrypts the message.
The resulting output is an unsigned byte array. Various ciphers that are
implemented by "providers" and installed maybe used by specifying the algorithm
in the <i>algorithm</i> parameter.  The specified algorithm must be symmetric.
The mode and padding can also be specified in the mode and padding parameters.
In case a provider specific instance of an algorithm is needed the provider may
also be specified in the provider parameter. This actor sends its secret key on
the <i>keyOut</i> port to a decryption actor as an unsigned byte array.  This
key should be protected in some manner as the security of the encrypted message
relies on the secrecy of this key. Key creation is done in pre-initialization
and is put on the <i>keyOut</i> port during initialization so the decryption
actor has a key to use when its first fired.

The following actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).


TODO: include sources of information on JCE cipher and algorithms

TODO: Use cipher streaming to allow for easier file input reading.
@author Rakesh Reddy
@version $Id$
@since Ptolemy II 3.1
*/

public class SymmetricEncryption extends CipherActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SymmetricEncryption(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        keyOut = new SDFIOPort(this, "keyOut", false, true);
        keyOut.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        parameters = new SDFIOPort(this, "parameters", false, true);
        parameters.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** This port outputs the key to be used by the SymmetricDecryption actor
     *  as an unsigned byte array.
     */
    public SDFIOPort keyOut;

    public SDFIOPort parameters;



    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token on the <i>input</i> port, this method takes the
     *  data from the <i>input</i> and encrypts the data based on the
     *  <i>algorithm</i>, <i>provider</i>, <i>mode</i> and <i>padding</i>
     *  using the created secret key.  This is then sent on the
     *  <i>output</i>.  The public key is also sent out on the <i>keyOut</i>
     *  port.  All parameters should be the same as the corresponding
     *  decryption actor.  The call for encryption is done in the base class.
     *
     *  @exception IllegalActionException if thrown by base class.
     */
    public void fire() throws IllegalActionException {

        keyOut.send(0, _unsignedByteArrayToArrayToken(_keyToBytes(_secretKey)));
        if(_algParams != null){
            try {

                parameters.send(0, _unsignedByteArrayToArrayToken(_algParams.getEncoded()));

            } catch (NoRoomException e) {
                e.printStackTrace();
                throw new IllegalActionException(this.getName()+e.getMessage());
            } catch (IllegalActionException e) {
                e.printStackTrace();
                throw new IllegalActionException(this.getName()+e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalActionException(this.getName()+e.getMessage());
            }
        }

        //            if(FIRST_RUN == true){
        //                _baos = new ByteArrayOutputStream();
        //                try {
        //
        //                    _cipher.init(Cipher.ENCRYPT_MODE, _secretKey, _algParams);
        //
        //                } catch (InvalidKeyException e) {
        //                    // TODO Auto-generated catch block
        //                    e.printStackTrace();
        //                } catch (InvalidAlgorithmParameterException e) {
        //                    // TODO Auto-generated catch block
        //                    e.printStackTrace();
        //                }
        //                _cos = new CipherOutputStream(_baos, _cipher);
        //                FIRST_RUN = false;
        //            }
        //        //} catch (NoRoomException e) {
        //       e.printStackTrace();
        //  } catch (IllegalActionException e) {
        //    e.printStackTrace();
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        } catch (InvalidKeyException e) {
        //            e.printStackTrace();
        //        } catch (InvalidAlgorithmParameterException e) {
        //            e.printStackTrace();
        //        }
        //        super.fire();
    }

    /** Get an instance of the cipher and outputs the key required for
     *  decryption.
     *  @exception IllegalActionException if thrown by base class.
     *  @exception NoSuchAlgorihmException if the algorithm is not found.
     *  @exception NoSuchPaddingException if the padding scheme is illegal
     *      for the given algorithm.
     *  @exception NoSuchProviderException if the specified provider does not
     *      exist.
     */
    public void initialize() throws IllegalActionException {
        try{
            super.initialize();
            _secretKey = (SecretKey)_createSymmetricKey();
            //keyOut.send(0, _unsignedByteArrayToArrayToken(_keyToBytes(_secretKey)));

            _cipher.init(Cipher.ENCRYPT_MODE, _secretKey);

            _algParams = _cipher.getParameters();
            //if(_algParams != null){
            //    parameters.send(0, _unsignedByteArrayToArrayToken(_algParams.getEncoded()));
            //}

            FIRST_RUN=true;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new IllegalActionException (this.getName()+e.getMessage());
        } catch (NoRoomException e) {
            e.printStackTrace();
            throw new IllegalActionException (this.getName()+e.getMessage());
        } catch (IllegalActionException e) {
            e.printStackTrace();
            throw new IllegalActionException (this.getName()+e.getMessage());
        }
    }

    /** Sets token production for initialize to one and resolves scheduling.
     *
     * @throws IllegalActionException if thrown by base class.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        keyOut.setTokenInitProduction(1);
        parameters.setTokenInitProduction(1);
        getDirector().invalidateResolvedTypes();
    }



    /** Encrypt the data with the specified key.  Receives the data to be
     *  encrypted as a byte array and returns a byte array.  Also creates
     *  and sends an initialization vector if necessary.
     *
     * @param dataBytes the data to be encrypted.
     * @return byte[] the encrypted data.
     * @throws IllegalActionException if exception below it thrown.
     * @exception IOException if error occurs in ByteArrayOutputStream.
     * @exception InvalideKeyException if key is invalid.
     * @exception BadPaddingException if padding is bad.
     * @exception IllegalBockSizeException if illegal block size.
     */
    protected byte[] _process(byte[] dataBytes)throws IllegalActionException{
        //      ByteArrayOutputStream baos = new ByteArrayOutputStream();
        _baos.reset();
        //        bais = new ByteArrayInputStream(initialData);
        //        int length = 0;
        //        byte [] buffer = new byte [BUFFER_SIZE];
        //        try{
        //            while ((length = bais.read(buffer)) != -1) {
        //                _cos.write(buffer, 0, length);
        //            }
        //            _cos.flush();
        //        } catch (IOException e){
        //            e.printStackTrace();
        //            throw new IllegalActionException(this.getName()+e.getMessage());
        //        }

        try {

            _baos.write(_cipher.doFinal(dataBytes));

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return _baos.toByteArray();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /* The public key to be used for asymmetric encryption. This key is null
     * for symmetric decryption.
     */
    private SecretKey _secretKey = null;

    //The initilization parameter used in a block ciphering mode.
    private IvParameterSpec _spec;

    private AlgorithmParameters _algParams;

    private static int BUFFER_SIZE = 8192;

    private boolean FIRST_RUN;

    //    private CipherOutputStream _cos;

    private ByteArrayOutputStream _baos;

    private ByteArrayInputStream bais;
}
