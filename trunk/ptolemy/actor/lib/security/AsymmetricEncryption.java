/* Receives a key from an AsymmetricDecryption actor and uses it to encrypt a
   data input based on a given asymmetric algorithm.

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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import ptolemy.data.ArrayToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// AsymmetricEncryption
/**
This actor takes an unsigned byte array at the input and encrypts the message
using the public key retrieved from the AsymmetricDecyption actor.
The resulting output is an unsigned byte array. Various ciphers that are
implemented by "providers" and installed on the system maybe used by specifying
the algorithm in the <i>algorithm</i> parameter. The algorithm specified must be
asymmetric. The mode and padding can also be specified in the <i>mode</i> and
<i>padding</i> parameters. In case a provider specific instance of an algorithm is
needed the provider may also be specified in the <i>provider</i> parameter.

The following actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).


TODO: include sources of information on JCE cipher and algorithms

TODO: Use cipher streaming to allow for easier file input reading.
@author Rakesh Reddy
@version $Id$
*/

public class AsymmetricEncryption extends CipherActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AsymmetricEncryption(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        keyIn = new SDFIOPort(this, "keyIn", true, false);
        keyIn.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** This port receives the public key to be used from the
     *  AsymmetricDecryption actor in the form of an unsigned byte array.
     *  This key is used to encrypt data from the <i>input</i> port.
     */
    public SDFIOPort keyIn;



    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there are tokens on the <i>input</i> and <i>keyIn</i> ports, they
     *  are consumed.  This method takes the data from the <i>input</i> and
     *  encrypts the data based on the <i>algorithm</i>, <i>provider</i>,
     *  <i>mode</i> and <i>padding</i> using the public key from the decryption
     *  actor.  This is then sent on the <i>output</i>.  All parameters should
     *  be the same as the corresponding decryption actor.
     *
     */
    public void fire() throws IllegalActionException {
        if(keyIn.hasToken(0)){
            _publicKey = (PublicKey)_bytesToKey(_arrayTokenToUnsignedByteArray((ArrayToken)keyIn.get(0)));
        }
        if(_publicKey != null){
            super.fire();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////

    /** Receives the data to be encrypted as a byte array and returns
     *  a byte array.
     *
     * @param dataBytes the data to be encrypted.
     * @return byte[] the encrypted data.
     * @throws IllegalActionException if an exception below is thrown.
     * @exception IOException when error occurs in ByteArrayOutputStream.
     * @exception InvalideKeyException when key is invalid.
     * @exception BadPaddingException when padding is bad.
     * @exception IllegalBockSizeException for illegal block sizes.
     */
    protected byte[] _process(byte[] dataBytes)throws IllegalActionException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try{
            _cipher.init(Cipher.ENCRYPT_MODE, _publicKey);
            int blockSize = _cipher.getBlockSize();
            int length = 0;
            for(int i = 0; i<dataBytes.length; i+=blockSize){
                if(dataBytes.length-i <= blockSize){
                    length = dataBytes.length-i;
                } else{
                    length = blockSize;
                }
                baos.write(_cipher.doFinal(dataBytes, i, length));
            }
            baos.flush();
            baos.close();


        } catch (IOException e){
            e.printStackTrace();
            throw new IllegalActionException(this.getName()+e.getMessage());
        } catch (InvalidKeyException e){
            e.printStackTrace();
            throw new IllegalActionException(this.getName()+e.getMessage());
        } catch (BadPaddingException e){
            e.printStackTrace();
            throw new IllegalActionException(this.getName()+e.getMessage());
        } catch (IllegalBlockSizeException e){
            e.printStackTrace();
            throw new IllegalActionException(this.getName()+e.getMessage());
        }
        return baos.toByteArray();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //The public key to be used for asymmetric encryption.
    private PublicKey _publicKey = null;
}
