/* Create and send a Key.

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


import ptolemy.actor.lib.Source;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

//////////////////////////////////////////////////////////////////////////
//// Key
/**

This actor creates a key and sends it on the output.

The key can be used by the {@link SymmetricEncryption} and 
{@link SymmetricDecryption} actors.

<p>This key should be not be visible to users as the security of
the encrypted message relies on the secrecy of this key.

<p>This actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).

<br>Information about JCA can be found at
<a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html" target="_top">http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html">.
<br>Information about JCE can be found at
<a href="http://java.sun.com/products/jce/" target="_top">http://java.sun.com/products/jce/">.

@author Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class Key extends Source {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Key(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        algorithm = new StringParameter(this, "algorithm");
        Set algorithms = Security.getAlgorithms("KeyGenerator");
        Iterator algorithmsIterator = algorithms.iterator();
        for(int i = 0; algorithmsIterator.hasNext(); i++) {
            String algorithmName = (String)algorithmsIterator.next();

            algorithm.addChoice(algorithmName);
        }
        algorithm.setExpression("DES");


        output.setTypeEquals(BaseType.OBJECT);

        provider = new StringParameter(this, "provider");
        provider.setExpression("SystemDefault");
        provider.addChoice("SystemDefault");
        Provider [] providers = Security.getProviders();
        for (int i = 0; i < providers.length; i++) {
            provider.addChoice(providers[i].getName());
        }

        keySize = new Parameter(this, "keySize", new IntToken(56));

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** Specify the algorithm to be used to process data.  The algorithm is
     *  specified as a string. The algorithms are limited to those
     *  implemented by providers using the Java JCE which are found on the
     *  system.
     *  The initial default is the first value returned by
     *  Security.getAlgorithms();
     */
    public StringParameter algorithm;

    /** Specify a provider for the given algorithm.  Takes the algorithm name
     *  as a string. The default value is "SystemDefault" which allows the
     *  system chooses the provider based on the JCE architecture.
     */
    public StringParameter provider;

    /** Specify the size of the key to be created.  This is an integer value
     *  representing the number of bits in the key.  The initial default
     *  depends on the algorithm that is selected, not all algorithms use
     *  keySize.
     *  <p>DSA is the most common algorithm that uses keySize, the Sun
     *  documentation says:
     *  "The length, in bits, of the modulus p. This must range from
     *  512 to 1024, and must be a multiple of 64. The default keysize
     *  is 1024."
     *  Refer to
     *  <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppB"><code>http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppB</code></a>
     *  for a list of possible key sizes for certain algorithms.
     *  The initial default is 1024.
     */
    public Parameter keySize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reinitialize the state if
     *  the <i>algorithm</i>, <i>provider</i>, or <i>keysize</i>
     *  parameter is changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == algorithm) {
            _algorithm = ((StringToken)algorithm.getToken()).stringValue();
        } else if (attribute == provider) {
            _provider = ((StringToken)provider.getToken()).stringValue();
        } else if (attribute == keySize) {
            _keySize = ((IntToken)keySize.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** If there is a token on the <i>input</i> port, this method takes the
     *  data from the <i>input</i> and encrypts the data based on the
     *  <i>algorithm</i>, <i>provider</i>, <i>mode</i> and <i>padding</i>
     *  using the created secret key.  This is then sent on the
     *  <i>output</i>.  The public key is also sent out on the <i>keyOut</i>
     *  port.  All parameters should be the same as the corresponding
     *  decryption actor.  The call for encryption is done in the base class.
     *
     *  @exception IllegalActionException If thrown by base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, _secretKeyObjectToken);
    }

    /** Get an instance of the cipher and outputs the key required for
     *  decryption.
     *  @exception IllegalActionException If thrown by base class or
     *  if the algorithm is not found, or if the padding scheme is illegal,
     *  or if the specified provider does not exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _algorithm = ((StringToken)algorithm.getToken()).stringValue();
        _provider = ((StringToken)provider.getToken()).stringValue();
        _keySize = ((IntToken)keySize.getToken()).intValue();
        try {
            KeyGenerator keyGen;
            if (_provider.equalsIgnoreCase("SystemDefault")) {
                keyGen = KeyGenerator.getInstance(_algorithm);
            } else {
                keyGen = KeyGenerator.getInstance(_algorithm, _provider);
            }
            keyGen.init(_keySize, new SecureRandom());
            _secretKey = keyGen.generateKey();
            _secretKeyObjectToken = new ObjectToken(_secretKey);

        } catch (Exception ex) {
            throw new IllegalActionException (this, ex,
                    "Failed to initialize Key.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The name of the algorithm to be used. */
    protected String _algorithm;

    /** The key size to be used when processing information. */
    protected int _keySize;

    /** The provider to be used for a provider specific implementation. */
    protected String _provider;

    /* The public key to be used for asymmetric encryption. This key is null
     * for symmetric decryption.
     */
    private SecretKey _secretKey = null;

    private ObjectToken _secretKeyObjectToken;
}
