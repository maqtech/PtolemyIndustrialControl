/* Sign a jar file.
 *
 * Copyright 2004 - 2007 University of Cardiff.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ptolemy.copernicus.applet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;

import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;
import sun.misc.BASE64Encoder;
import sun.security.util.ManifestDigester;

/**
 * Sign a Jar file.
 * <p>From <a href="https://svn.cs.cf.ac.uk/projects/whip/trunk/whip-core/src/main/java/org/whipplugin/data/bundle/JarSigner15.java">https://svn.cs.cf.ac.uk/projects/whip/trunk/whip-core/src/main/java/org/whipplugin/data/bundle/JarSigner15.java</a>.
 * See also <a href="http://www.onjava.com/pub/a/onjava/2001/04/12/signing_jar.html?page=1">http://www.onjava.com/pub/a/onjava/2001/04/12/signing_jar.html?page=1</a>.
 * @author Andrew Harrison, Contributor: Christopher Brooks.
 * @since Ptolemy II 8.0
 * @version $Id$
 */
public class JarSigner {

    /** Construct a jar signer.
     *  @param alias The alias for the signing key.
     *  @param privateKey The private key to sign with.
     *  @param certChain The certificate chain.
     */
    public JarSigner(String alias, PrivateKey privateKey,
            X509Certificate[] certChain) {
        _alias = alias;
        _privateKey = privateKey;
        // Findbugs: EI2 May expose internal representation by incorporating
        // reference to immutable object
        _certChain = new X509Certificate[certChain.length];
        System.arraycopy(certChain, 0, _certChain, 0, certChain.length);
    }

    /** JarSigner test driver.
     *
     *  <p>This method uses the <code>$PTII/ptKeystore</code> certificate.  To create that file:
     *  <pre>
     *  cd $PTII
     *  make ptKeystore
     *  make jnlp_list
     *  </pre>
     *  <p>Usage:
     *  <pre>
     *  java -classpath $PTII ptolemy.copernicus.applet.JarSigner JNLPApplication.jar JNLPSignedApplication.jar
     *  </pre>
     *  To verify the signed jar, run:
     *  <pre>
     *  jarsigner -verify -verbose -certs JNLPSignedApplication.jar
     *  </pre>
     *  @param args An array of two arguments, the first element is the name of the jar file
     *  to be read in, the second is the name of the signed jar file to be created.

     */
    public static void main(String args[]) {
        if (args.length != 2) {
            System.err
            .println("Usage: java -classpath $PTII ptolemy.copernicus.applet.JarSigner JNLPApplication.jar JNLPSignedApplication.jar");
        }
        // $PTII/ptKeystore is generated by running (cd $PTII; make ptKeystore)
        String keystoreFileName = /*System.getProperty("PTII")*/"/Users/cxh/ptII"
                + File.separator + "ptKeystore";
        String storePassword = "this.is.the.storePassword,change.it";
        String keyPassword = "this.is.the.keyPassword,change.it";
        String alias = "ptolemy";

        String keystorePropertiesFileName = StringUtilities
                .getProperty("ptolemy.ptII.dir")
                + File.separator
                + "ptKeystore.properties";

        Properties properties = new Properties();
        try {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(
                        keystorePropertiesFileName);
                properties.load(fileInputStream);
                String property = null;
                if ((property = properties.getProperty("keystoreFileName")) != null) {
                    keystoreFileName = property;
                }
                storePassword = properties.getProperty("storePassword");
                keyPassword = properties.getProperty("keyPassword");
                alias = properties.getProperty("alias");
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }
        } catch (IOException ex) {
            System.out
            .println("Warning: failed to read \""
                    + keystorePropertiesFileName
                    + "\", using default store password, key password and alias:"
                    + ex);
        }

        System.out.println("About to sign \"" + args[0] + "\" and create \""
                + args[1] + "\"" + " using keystore: \"" + keystoreFileName
                + "\"" + " and alias: \"" + alias + "\"");
        try {
            sign(args[0], args[1], keystoreFileName, alias,
                    storePassword.toCharArray(), keyPassword.toCharArray());
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    /** Sign a jar file.
     *  @param jarFileName  The name of the jar file to be signed.
     *  @param signedJarFileName  The name of the signed jar file to be created.
     *  @param keystoreFileName The name of the keystore file.  To create a keystore file, run
     *  <pre>
     *  cd $PTII
     *  make ptKeystore
     *  make jnlp_list
     *  </pre>
     *  @param alias The alias of the certificate.  This is the string used when the key is created.
     *  @param storePassword  The password of the key store.
     *  @param keyPassword  The password of the key store.
     *  @exception Exception  If there is a problem open or closing files, or a problem signing
     *  the jar file.
     */
    public static void sign(String jarFileName, String signedJarFileName,
            String keystoreFileName, String alias, char[] storePassword,
            char[] keyPassword) throws Exception {
        FileInputStream fileIn = null;
        OutputStream outStream = null;
        try {
            fileIn = new FileInputStream(keystoreFileName);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fileIn, storePassword);

            // Get the certificate chain
            Certificate[] chain = keyStore.getCertificateChain(alias);
            if (chain == null) {
                throw new Exception(
                        "Could not get certificate chain from alias \"" + alias
                        + "\" from keystore \"" + keystoreFileName
                        + "\"");
            }
            X509Certificate certChain[] = new X509Certificate[0];
            certChain = new X509Certificate[chain.length];

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (int count = 0; count < chain.length; count++) {
                ByteArrayInputStream certIn = new ByteArrayInputStream(
                        chain[0].getEncoded());
                X509Certificate cert = (X509Certificate) cf
                        .generateCertificate(certIn);
                certChain[count] = cert;
            }

            Key key = keyStore.getKey(alias, keyPassword);
            if (key == null) {
                throw new Exception("Could not get key from alias \"" + alias
                        + "\" from keystore \"" + keystoreFileName + "\"");
            }
            KeyFactory keyFactory = KeyFactory.getInstance(key.getAlgorithm());
            KeySpec keySpec = null;
            try {
                keySpec = keyFactory.getKeySpec(key, DSAPrivateKeySpec.class);
            } catch (java.security.spec.InvalidKeySpecException ex) {
                System.out.println("Using RSA");
                keySpec = keyFactory.getKeySpec(key, RSAPrivateKeySpec.class);
            }
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            JarSigner jarSigner = new JarSigner(alias, privateKey, certChain);

            JarFile jarFile = null;
            try {
                jarFile = new JarFile(jarFileName);
                outStream = new FileOutputStream(signedJarFileName);
                jarSigner._signJarFile(jarFile, outStream);
            } finally {
                if (jarFile != null) {
                    jarFile.close();
                }
            }
        } finally {
            if (fileIn != null) {
                try {
                    fileIn.close();
                } catch (IOException ex) {
                    if (outStream != null) {
                        outStream.close();
                    }
                    throw ex;
                }
            }
            if (outStream != null) {
                outStream.close();
            }
        }
        // FIXME: The problem here is that if we create jar files for Web Start,
        // then the cert chain is not included in the PTOLEMY.RSA file.
        // One can see this by running without the code below and creating
        // a jar file and then running
        // jarsigner -verify -certs -verbose signed_V2V.jar.bad
        // and then running with the code below
        // jarsigner -verify -certs -verbose signed_V2V.jar
        //
        // In the good file, we will have lines like
        // [certificate is valid from 2/27/09 4:00 PM to 4/9/12 4:59 PM]
        // X.509, CN=VeriSign Class 3 Code Signing 2004 CA, OU=Terms of use at https://www.verisign.com/rpa (c)04, OU=VeriSign Trust Network, O="VeriSign, Inc.", C=US
        // [certificate is valid from 7/15/04 5:00 PM to 7/15/14 4:59 PM]
        // X.509, OU=Class 3 Public Primary Certification Authority, O="VeriSign, Inc.", C=US
        // [certificate is valid from 1/28/96 4:00 PM to 8/1/28 4:59 PM]
        //
        // If we don't have those lines, then we get an error about how
        // the "JAR resources in JNLP file are not signed by same certificate"

        System.out.println("Working around bug where the chain of certs "
                + "is not included in the .RSA file");
        List commands = new LinkedList();
        commands.add("jarsigner -keystore \"" + keystoreFileName
                + "\" -keypass \"" + new String(keyPassword)
        + "\" -storepass \"" + new String(storePassword) + "\" \""
        + signedJarFileName + "\"  \"" + alias + "\"");

        final StreamExec exec = new StreamExec();
        exec.setCommands(commands);
        exec.start();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Make sure that the manifest entries are ready for the signed
     * JAR manifest file. if we already have a manifest, then we
     * make sure that all the elements are valid. if we do not
     * have a manifest, then we create a new signed JAR manifest
     * file by adding the appropriate headers
     */
    private static Map _createEntries(Manifest manifest, JarFile jarFile)
            throws IOException {
        Map entries = null;
        if (manifest.getEntries().size() > 0) {
            entries = _pruneManifest(manifest, jarFile);
        } else {
            // if there are no pre-existing entries in the manifest,
            // then we put a few default ones in
            Attributes attributes = manifest.getMainAttributes();
            attributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(),
                    "1.0");
            attributes.putValue(
                    "Created-By",
                    System.getProperty("java.version") + " ("
                            + System.getProperty("java.vendor") + ")");
            entries = manifest.getEntries();
        }
        return entries;
    }

    /** Create a signature file object out of the manifest and the
     * message digest.
     */
    private/*static*/SignatureFile _createSignatureFile(Manifest manifest,
            MessageDigest messageDigest) throws IOException,
            IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, InstantiationException,
            ClassNotFoundException {

        // Findbugs: SIC: Should be a static inner class.  However,
        // this method references this._alias and returns a new object.

        // construct the signature file and the signature block for
        // this manifest
        ManifestDigester manifestDigester = new ManifestDigester(
                _serializeManifest(manifest));
        return new SignatureFile(new MessageDigest[] { messageDigest },
                manifest, manifestDigester, this._alias, true);

    }

    private static Constructor _findConstructor(Class c, Class... argTypes)
            throws NoSuchMethodException {
        Constructor ct = c.getDeclaredConstructor(argTypes);
        if (ct == null) {
            throw new RuntimeException(c.getName());
        }
        ct.setAccessible(true);
        return ct;
    }

    private static Method _findMethod(Class c, String methodName,
            Class... argTypes) throws NoSuchMethodException {
        Method m = c.getDeclaredMethod(methodName, argTypes);
        if (m == null) {
            throw new RuntimeException(c.getName());
        }
        m.setAccessible(true);
        return m;
    }

    /** Retrieve the manifest from a jar file -- this will either
     * load a pre-existing META-INF/MANIFEST.MF, or create a new
     * one.
     */
    private static Manifest _getManifestFile(JarFile jarFile)
            throws IOException {
        JarEntry jarEntry = jarFile.getJarEntry("META-INF/MANIFEST.MF");
        if (jarEntry != null) {
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                jarEntry = (JarEntry) entries.nextElement();
                if ("META-INF/MANIFEST.MF".equalsIgnoreCase(jarEntry.getName())) {
                    break;
                } else {
                    jarEntry = null;
                }
            }
        }
        // create the manifest object
        Manifest manifest = new Manifest();
        if (jarEntry != null) {
            InputStream inputStream = null;
            try {
                inputStream = jarFile.getInputStream(jarEntry);
                manifest.read(inputStream);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        return manifest;

    }

    /** Given a manifest file and given a jar file, make sure that
     * the contents of the manifest file is correct and return a
     * map of all the valid entries from the manifest
     */
    private static Map _pruneManifest(Manifest manifest, JarFile jarFile)
            throws IOException {
        Map map = manifest.getEntries();
        Iterator elements = map.keySet().iterator();
        while (elements.hasNext()) {
            String element = (String) elements.next();
            if (jarFile.getEntry(element) == null) {
                elements.remove();
            }

        }
        return map;

    }

    /** A small helper function that will convert a manifest into an
     * array of bytes.
     */
    private static byte[] _serializeManifest(Manifest manifest)
            throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            manifest.write(baos);
            baos.flush();
        } finally {
            if (baos != null) {
                baos.close();
            }
        }
        return baos.toByteArray();
    }

    /** The actual JAR signing method. This is the method which
     * will be called by those wrapping the JARSigner class.
     * @param jarFile The jar file to be read in and signed.
     * @param outputStream  The stream to which the signed jar file should  be written.
     * @exception NoSuchAlgorithmException If the SHA1 algorithm cannot be found or there
     * is a problem generating the block.
     * @exception InvalidKeyException If the certificate key is not valid.
     * @exception SignatureException If there is a problem with the signature.
     * @exception IOException If there is a problem reading or writing a file.
     * @exception IllegalAccessException If there is a problem getting the metaname from the
     * the signature file.
     * @exception InvocationTargetException If there is a problem creating the signature file
     * or getting the metaname from the signature.
     * @exception NoSuchMethodException If thrown while creating the signature file.
     * @exception CertificateException If there is a problem generating the block.
     * @exception InstantiationException If thrown while creating the signature file.
     * @exception ClassNotFoundException If thrown while generating the signature block.
     */
    public void _signJarFile(JarFile jarFile, OutputStream outputStream)
            throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException, IOException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException,
            CertificateException, InstantiationException,
            ClassNotFoundException {

        // calculate the necessary files for the signed jAR

        // get the manifest out of the jar and verify that
        // all the entries in the manifest are correct
        Manifest manifest = _getManifestFile(jarFile);
        Map entries = _createEntries(manifest, jarFile);

        // create the message digest and start updating the
        // the attributes in the manifest to contain the SHA1
        // digests
        MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
        _updateManifestDigest(manifest, jarFile, messageDigest, entries);

        // construct the signature file object and the
        // signature block objects
        SignatureFile signatureFile = _createSignatureFile(manifest,
                messageDigest);
        SignatureFile.Block block = signatureFile.generateBlock(_privateKey,
                _certChain, true, jarFile);

        // start writing out the signed JAR file

        // write out the manifest to the output jar stream
        String manifestFileName = "META-INF/MANIFEST.MF";
        JarOutputStream jarOutputStream = null;
        try {
            jarOutputStream = new JarOutputStream(outputStream);
            JarEntry manifestFile = new JarEntry(manifestFileName);
            jarOutputStream.putNextEntry(manifestFile);
            byte manifestBytes[] = _serializeManifest(manifest);
            jarOutputStream.write(manifestBytes, 0, manifestBytes.length);
            jarOutputStream.closeEntry();

            // write out the signature file -- the signatureFile
            // object will name itself appropriately
            String signatureFileName = signatureFile.getMetaName();
            JarEntry signatureFileEntry = new JarEntry(signatureFileName);
            jarOutputStream.putNextEntry(signatureFileEntry);
            signatureFile.write(jarOutputStream);
            jarOutputStream.closeEntry();

            // write out the signature block file -- again, the block
            // will name itself appropriately
            String signatureBlockName = block.getMetaName();
            JarEntry signatureBlockEntry = new JarEntry(signatureBlockName);
            jarOutputStream.putNextEntry(signatureBlockEntry);
            block.write(jarOutputStream);
            jarOutputStream.closeEntry();

            // commit the rest of the original entries in the
            // META-INF directory. if any of their names conflict
            // with one that we created for the signed JAR file, then
            // we simply ignore it
            Enumeration metaEntries = jarFile.entries();
            while (metaEntries.hasMoreElements()) {
                JarEntry metaEntry = (JarEntry) metaEntries.nextElement();
                if (metaEntry.getName().startsWith("META-INF")
                        && !(manifestFileName.equalsIgnoreCase(metaEntry
                                .getName())
                                || signatureFileName.equalsIgnoreCase(metaEntry
                                        .getName()) || signatureBlockName
                                        .equalsIgnoreCase(metaEntry.getName()))) {
                    _writeJarEntry(metaEntry, jarFile, jarOutputStream);
                }
            }

            // now write out the rest of the files to the stream
            Enumeration allEntries = jarFile.entries();
            while (allEntries.hasMoreElements()) {
                JarEntry entry = (JarEntry) allEntries.nextElement();
                //System.out.println("JarSigner: entry: " + entry);
                if (!entry.getName().startsWith("META-INF")) {
                    _writeJarEntry(entry, jarFile, jarOutputStream);
                }
            }

        } finally {
            if (jarOutputStream != null) {
                // finish the stream that we have been writing to
                jarOutputStream.flush();
                jarOutputStream.finish();
                jarOutputStream.close();
            }
        }
    }

    /** Helper function to update the digest.
     *  The inputStream is always closed upon exit.
     */
    private static String _updateDigest(MessageDigest digest,
            InputStream inputStream) throws IOException {
        try {
            byte[] buffer = new byte[2048];
            int read = 0;
            while ((read = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        } finally {
            inputStream.close();
        }
        return _b64Encoder.encode(digest.digest());

    }

    /** Update the attributes in the manifest to have the
     * appropriate message digests. we store the new entries into
     * the entries Map and return it (we do not compute the digests
     * for those entries in the META-INF directory)
     */
    private static Map _updateManifestDigest(Manifest manifest,
            JarFile jarFile, MessageDigest messageDigest, Map entries)
                    throws IOException {
        Enumeration jarElements = jarFile.entries();
        while (jarElements.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) jarElements.nextElement();
            if (jarEntry.getName().startsWith("META-INF")) {
                continue;
            } else if (manifest.getAttributes(jarEntry.getName()) != null) {
                // update the digest and record the base 64 version of
                // it into the attribute list
                Attributes attributes = manifest.getAttributes(jarEntry
                        .getName());
                attributes.putValue(
                        "SHA1-Digest",
                        _updateDigest(messageDigest,
                                jarFile.getInputStream(jarEntry)));

            } else if (!jarEntry.isDirectory()) {
                // store away the digest into a new Attribute
                // because we don't already have an attribute list
                // for this entry. we do not store attributes for
                // directories within the JAR
                Attributes attributes = new Attributes();
                attributes.putValue(
                        "SHA1-Digest",
                        _updateDigest(messageDigest,
                                jarFile.getInputStream(jarEntry)));
                entries.put(jarEntry.getName(), attributes);

            }

        }
        return entries;
    }

    /** A helper function that can take entries from one jar file and
     *  write it to another jar stream.
     *
     * @param jarEntry The entry in the jar file to be added to the
     * jar output stream.
     * @param jarFile The jar file that contains the jarEntry.
     * @param jarOutputStream The output stream that the jarEntry from
     * the jarFile to which to write.
     * @exception IOException If there is a problem reading or writing
     * the jarEntry.
     */
    protected static void _writeJarEntry(JarEntry jarEntry, JarFile jarFile,
            JarOutputStream jarOutputStream) throws IOException {

        jarOutputStream.putNextEntry(jarEntry);
        byte[] buffer = new byte[2048];
        int read = 0;
        InputStream inputStream = null;
        try {
            inputStream = jarFile.getInputStream(jarEntry);
            while ((read = inputStream.read(buffer)) > 0) {
                jarOutputStream.write(buffer, 0, read);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private inner classes             ////

    private static class SignatureFile {
        // FindBugs indicates that this should be a static class.

        private Object sigFile;

        private Class JDKsfClass;

        private Method getMetaNameMethod;
        private Method writeMethod;

        private static String JDK_SIGNATURE_FILE = "sun.security.tools.SignatureFile";
        private static final String GETMETANAME_METHOD = "getMetaName";
        private static final String WRITE_METHOD = "write";

        public SignatureFile(MessageDigest digests[], Manifest mf,
                ManifestDigester md, String baseName, boolean signManifest)
                        throws ClassNotFoundException, NoSuchMethodException,
                        InstantiationException, IllegalAccessException,
                        InvocationTargetException {

            try {
                JDKsfClass = Class.forName(JDK_SIGNATURE_FILE);
            } catch (ClassNotFoundException ex) {
                // Java 1.8
                JDK_SIGNATURE_FILE = "sun.security.tools.jarsigner.SignatureFile";
                JDKsfClass = Class.forName(JDK_SIGNATURE_FILE);
            }
            Constructor constructor = _findConstructor(JDKsfClass,
                    MessageDigest[].class, Manifest.class,
                    ManifestDigester.class, String.class, Boolean.TYPE);

            sigFile = constructor.newInstance(digests, mf, md, baseName,
                    signManifest);

            getMetaNameMethod = _findMethod(JDKsfClass, GETMETANAME_METHOD);
            writeMethod = _findMethod(JDKsfClass, WRITE_METHOD,
                    OutputStream.class);
        }

        public Block generateBlock(PrivateKey privateKey,
                X509Certificate[] certChain, boolean externalSF, ZipFile zipFile)
                        throws NoSuchAlgorithmException, InvalidKeyException,
                        IOException, SignatureException, CertificateException,
                        ClassNotFoundException, NoSuchMethodException,
                        InstantiationException, IllegalAccessException,
                        InvocationTargetException {
            return new Block(this, privateKey, certChain, externalSF, zipFile);
        }

        public Class getJDKSignatureFileClass() {
            return JDKsfClass;
        }

        public Object getJDKSignatureFile() {
            return sigFile;
        }

        public String getMetaName() throws IllegalAccessException,
        InvocationTargetException {
            return (String) getMetaNameMethod.invoke(sigFile);
        }

        public void write(OutputStream os) throws IllegalAccessException,
        InvocationTargetException {
            writeMethod.invoke(sigFile, os);
        }

        private static class Block {

            private Object block;

            private static final String JDK_BLOCK = JDK_SIGNATURE_FILE
                    + "$Block";
            private static final String JDK_CONTENT_SIGNER = "com.sun.jarsigner.ContentSigner";

            private Method getMetaNameMethod;
            private Method writeMethod;

            public Block(SignatureFile sfg, PrivateKey privateKey,
                    X509Certificate[] certChain, boolean externalSF,
                    ZipFile zipFile) throws ClassNotFoundException,
                    NoSuchMethodException, InstantiationException,
                    IllegalAccessException, InvocationTargetException {

                Class blockClass = Class.forName(JDK_BLOCK);

                Class contentSignerClass = Class.forName(JDK_CONTENT_SIGNER);

                Constructor constructor = null;
                // Most recent JVM first for efficiency.
                try {
                        // Java 1.8
                        // javap -classpath /Library/Java/JavaVirtualMachines/jdk1.8.0_20.jdk/Contents/Home/jre/../lib/tools.jar sun.security.tools.jarsigner.SignatureFile\$Block

                        //sun.security.tools.jarsigner.SignatureFile$Block(sun.security.tools.jarsigner.SignatureFile, 
                        //        java.security.PrivateKey, 
                        //        java.lang.String, 
                        //        java.security.cert.X509Certificate[], boolean, 
                        //        java.lang.String, java.security.cert.X509Certificate,
                        //        java.lang.String, 
                        //        com.sun.jarsigner.ContentSigner, 
                        //        java.lang.String[], java.util.zip.ZipFile)

                        constructor = _findConstructor(blockClass,
                                sfg.getJDKSignatureFileClass(), PrivateKey.class,
                                String.class,
                                X509Certificate[].class, Boolean.TYPE,
                                String.class, X509Certificate.class,
                                // Is this the only difference betwee 1.6 and 1.8?
                                // Running jode indicates that this is tSAPolicyID
                                // and passed to
                                // sun.security.tools.jarsigner.JarSignerParameters.
                                // Presumably, this is at Time Stamp Authority Policy.
                                // See http://docs.oracle.com/javase/8/docs/technotes/guides/security/time-of-signing.html
                                String.class,
                                contentSignerClass, String[].class, ZipFile.class);

                        block = constructor.newInstance(sfg.getJDKSignatureFile(), /* explicit argument on the constructor */
                                privateKey,
                                /*signatureAlgorithm*/null, certChain, externalSF,
                                null, null, null, null, null, zipFile);
                } catch (NoSuchMethodException ex) {
                    // Java 1.6

                    // In Java 1.6, SignatureFile$Block() takes a new argument,
                    // String sigAlg, which can be null.  See the source code
                    // for JarSigner at
                    // http://www.java2s.com/Open-Source/Java-Document/6.0-JDK-Modules-sun/security/sun/security/tools/JarSigner.java.htm
                    // and see
                    // http://www.docjar.com/docs/api/sun/security/tools/SignatureFile.html

                    try {
                        constructor = _findConstructor(blockClass,
                                sfg.getJDKSignatureFileClass(), PrivateKey.class,
                                /* Is this the only difference between 1.5 and 1.6?*/
                                /* signatureAlgorithm */String.class,
                                X509Certificate[].class, Boolean.TYPE,
                                String.class, X509Certificate.class,
                                contentSignerClass, String[].class, ZipFile.class);

                        block = constructor.newInstance(sfg.getJDKSignatureFile(), /* explicit argument on the constructor */
                                privateKey,
                                /*signatureAlgorithm*/null, certChain, externalSF,
                                null, null, null, null, zipFile);

                    } catch (NoSuchMethodException ex2) {
                        try {
                            // Java 1.5
                            constructor = _findConstructor(blockClass,
                                    sfg.getJDKSignatureFileClass(), PrivateKey.class,
                                    X509Certificate[].class, Boolean.TYPE,
                                    String.class, X509Certificate.class,
                                    contentSignerClass, String[].class, ZipFile.class);

                            block = constructor.newInstance(sfg.getJDKSignatureFile(), /* explicit argument on the constructor */
                                    privateKey, certChain, externalSF, null, null,
                                    null, null, zipFile);
                        } catch (NoSuchMethodException ex3) {
                            throw new NoSuchMethodException("Failed to find the Block "
                                    + "constructor. Tried these constructors: "
                                    + "\nJava 1.8: " + ex
                                    + "\nJava 1.6: " + ex2
                                    + "\nJava 1.5: " + ex3);
                        }
                    }
                }
                getMetaNameMethod = _findMethod(blockClass, GETMETANAME_METHOD);
                writeMethod = _findMethod(blockClass, WRITE_METHOD,
                        OutputStream.class);
            }

            public String getMetaName() throws IllegalAccessException,
            InvocationTargetException {
                return (String) getMetaNameMethod.invoke(block);
            }

            public void write(OutputStream os) throws IllegalAccessException,
            InvocationTargetException {
                writeMethod.invoke(block, os);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The alias for the signing key. */
    private String _alias;

    private static BASE64Encoder _b64Encoder = new BASE64Encoder();

    /** The private key to sign with. */
    private PrivateKey _privateKey;

    /** The certificate chain. */
    private X509Certificate[] _certChain;
}
