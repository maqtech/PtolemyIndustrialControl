/* A library supporting the playback of audio data and the creation of
   sound files from audio samples.

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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating
*/

package ptolemy.media.javasound;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.sound.sampled.*;

//////////////////////////////////////////////////////////////////////////
//// SoundPlayback
/**
A library supporting the playback of audio. This class supports the
real-time capture of audio from the audio input port (mic or line-in)
as well as the capture of audio from a sound file.
<p>
 Depending on available
system resorces, it may be possible to run multiple instances of this
class and an instance of SoundPlayback concurrently. It should
at least be possible to run one instance of this class and one
instance of SoundPlayback concurrently, thus enabling the real-time
capturing, processing, and playback of audio.
<p>
<i>Security issues</i>: Applications have no restrictions on the
capturing or playback of audio. Applets, however, may only capture
audio from a file specified as a URL on the same machine as the
one the applet was loaded from. The .java.policy file must be
modified to grant applets more privliiges.
<p>
Note: Requires Java 2 v1.3.0 RC1 or later.

@author Brian K. Vogel
@version $Id$
@see ptolemy.media.javasound.SoundCapture
*/

public class SoundPlayback {

    /** Construct a sound playback object. This constructor creates an
     *  object that plays audio through the computer's speaker. Note
     *  that putSamples() should be
     *  called often enough to prevent underflow of the internal audio
     *  input buffer.
     *  @param sampleRate Sample rate in Hz. Must be in the range (8000
     *  to 48000).
     *  @param sampleSizeInBits Number of bits per sample (valid choices are
     *  8 or 16).
     *  @param channels Number of audio channels. FIXME: must be 1
     *   for now.
     *  @param bufferSize Requested size of the internal audio input
     *   buffer in samples. This controls the latency. Ideally, the
     *   smallest value that gives acceptable performance (no overflow)
     *   should be used.
     *  @param putSamplesSize Size of the array parameter of
     *   <i>putSamples()</i>. For performance reasons, the size should
     *   be chosen smaller than <i>bufferSize</i>. Typical values
     *   might be 1/2 to 1/16th of <i>bufferSize</i>.
     */
    // FIXME: channels must be set = 1.
    public SoundPlayback(float sampleRate, int sampleSizeInBits,
			int channels, int bufferSize,
			int putSamplesSize) {
	System.out.println("SoundPlayback: constructor 1: invoked");
	// Set mode to real-time.
	this._playbackMode = "speaker";
	this._sampleSizeInBits = sampleSizeInBits;
	this._sampleRate = sampleRate;
	this._channels = channels;
	this._bufferSize = bufferSize;
	this._putSamplesSize = putSamplesSize;

	System.out.println("SoundPlayback: constructor 1: sampleSizeInBits = "
			   + sampleSizeInBits);
	System.out.println("SoundPlayback: constructor 1: sampleRate = "
			   + sampleRate);
	System.out.println("SoundPlayback: constructor 1: channels = "
			   + channels);
	System.out.println("SoundPlayback: constructor 1: bufferSize = "
			   + bufferSize);
	System.out.println("SoundPlayback: constructor 1: putSamplesSize = "
			   + putSamplesSize);
    }

    /** Construct a sound playback object. This constructor creates an
     *  object that plays audio to a sound file. To create a new
     *  sound file, call the <i>startPlayback()</i> method.
     *  Thereafter, each call to <i>putSamples()</i> will add
     *  <i>putSamplesSize</i> many samples to the sound file. To
     *  close and save the sound file, call method <i>stopPlayback</i>.
     *  @param fileName The file name to create. If the file already
     *  exists, overwrite it.
     *  @param sampleRate Sample rate in Hz. Must be in the range (8000
     *  to 48000).
     *  @param sampleSizeInBits Number of bits per sample (valid choices are
     *  8 or 16).
     *  @param channels Number of audio channels. FIXME: must be 1
     *   for now.
     *  @param putSamplesSize Size of the array parameter of
     *   <i>putSamples()</i>. For performance reasons, the size should
     *   be chosen smaller than <i>bufferSize</i>. Typical values
     *   might be 1/2 to 1/16th of <i>bufferSize</i>.
     */
    // FIXME: channels must be set = 1.
    public SoundPlayback(String fileName, float sampleRate, int sampleSizeInBits,
			int channels, int bufferSize,
			int putSamplesSize) {
	System.out.println("SoundPlayback: constructor 2: invoked");

	this._playbackMode = "file";
	this._fileName = fileName;
	this._sampleSizeInBits = sampleSizeInBits;
	this._sampleRate = sampleRate;
	this._channels = channels;
	this._productionRate = putSamplesSize;

	System.out.println("SoundPlayback: constructor 1: sampleSizeInBits = "
			   + sampleSizeInBits);
	System.out.println("SoundPlayback: constructor 1: sampleRate = "
			   + sampleRate);
	System.out.println("SoundPlayback: constructor 1: channels = "
			   + channels);
	System.out.println("SoundPlayback: constructor 1: bufferSize = "
			   + bufferSize);
	System.out.println("SoundPlayback: constructor 1: putSamplesSize = "
			   + putSamplesSize);
    }

    ///////////////////////////////////////////////////////////////
    ///  Public Methods                                         ///

    /** Perform initialization for the playback of audio data.
     *  After this method is called, 
     *   This method must be invoked prior
     *  to the first invocation of <i>putSamples</i>. This method
     *  must not be called more than once between invocations of
     *  <i>stopPlayback()</i>.
     */
    public void startPlayback() {
	System.out.println("SoundPLayback: startPlayback(): invoked");
	if (_playbackMode == "speaker") {
	    // Real time playback to speaker.
	    _startPlaybackRealTime();
	} else if (_playbackMode == "file") {
	    // Record data to sound file.
	    _startPlaybackToFile();
	} else if (_playbackMode == "both") {
	    // Real time playback to speaker.
	    // **AND**
	    // Record data to sound file.
	    _startPlaybackRealTime();
	    _startPlaybackToFile();
	}
    }

    /** Stop playing/writing audio. This method should be called when
     *  no more calls to <i>putSamples()</i>. are required, so
     *  that the system resources involved in the audio playback
     *  may be freed.
     *  <p>
     *  If the "play audio to file" constructor was used, then
     *  the sound file specified by the constructor will be closed.
     */
    public void stopPlayback() {
	if (_playbackMode == "speaker") {
	    // Stop real-time playback to speaker.
	    _sourceLine.stop();
	    _sourceLine.close();
	    _sourceLine = null;
	} else if (_playbackMode == "file") {
	    // Record data to sound file.
	    _stopPlaybackToFile();   
	}
    }

    /** If the "play audio to speaker" constructor was called,
     *  then playback the array of audio samples in
     *  <i>putSamplesArray</i>. There will be a latency before
     *  the audio data is actually heard, however, since the
     *  audio data in <i>putSamplesArray</i> is added to an
     *  internal audio buffer, the size of which is set by
     *  the constructor. If the "play audio to speaker" mode is
     *  used, then this method should be called frequently
     *  enough to prevent underflow of the internal audio buffer.
     *  <p>
     *  If the "play audio to file" constructor was used,
     *  then append the audio data contained in <i>putSamplesArray</i>
     *  to the soundfile specified in the constructor.
     */
    public void putSamples(double[] putSamplesArray) {
	System.out.println("SoundPlayback: putSamples(): invoked");
	if (_playbackMode == "speaker") {
	    
	    // Convert array of double valued samples into
	    // the proper byte array format.
	    _data = _doubleArrayToByteArray(putSamplesArray,
					    _frameSizeInBytes);

	    // Note: _data is a byte array containing data to
	    // be written to the output device.
	    // Note: consumptionRate is amount of data to write, in bytes.

	    // Now write the array to output device.
	    _sourceLine.write(_data, 0, _putSamplesSize*_frameSizeInBytes);
	} else if (_playbackMode == "file") {
	    System.out.println("SoundPlayback: putSamples(): file");

	    // Convert array of double valued samples into
	    // the proper byte array format.
	    _data = _doubleArrayToByteArray(putSamplesArray,
					    _frameSizeInBytes);
	    System.out.println("SoundPlayback: putSamples(): _data.length = " + _data.length);
	    System.out.println("SoundPlayback: putSamples(): putSamplesArray.length = " + putSamplesArray.length);
	    System.out.println("SoundPlayback: putSamples(): _frameSizeInBytes = " + _frameSizeInBytes);
	    // Add new audio data to the file buffer array.
	    for (int i = 0; i < _data.length; i++) {
		_toFileBuffer.add(new Byte(_data[i]));
	    }
	} else if (_playbackMode == "both") {

	} else {
	    // Should not happen since caught by constructor.
	}
	//System.out.println("SoundPlayback: putSamples(): return");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methodes                  ////

    private void _startPlaybackRealTime() {
        boolean signed = true;
        boolean bigEndian = true;

        AudioFormat format = new AudioFormat((float)_sampleRate,
                _sampleSizeInBits,
                _channels, signed, bigEndian);
        _frameSizeInBytes = format.getFrameSize();

	System.out.println("SoundPLayback: _startPlaybackRealTime(): sampling rate = " + _sampleRate);
	System.out.println("SoundPLayback: _startPlaybackRealTime(): sample size in bits = " +
			   _sampleSizeInBits);

        DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class,
					 format,
					 AudioSystem.NOT_SPECIFIED);

	System.out.println("SoundPLayback: Dataline.Info : " +
			   sourceInfo.toString());

        // get and open the source data line for playback.
	try {
	    // Source DataLine is a stupid name. It is really a target for
	    // audio data, not a source.
	    _sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            // Open line and suggest a buffersize (in bytes) to use or
	    // the internal audio buffer.
	    _sourceLine.open(format, _bufferSize*_frameSizeInBytes);
	    System.out.println("SoundPlayback: internal audio buffer size = " +
			       _sourceLine.getBufferSize()/_frameSizeInBytes + 
			       " samples.");
	    
	} catch (LineUnavailableException ex) {
            System.err.println("LineUnavailableException " + ex);
	    return;
	}

	// Array of audio samples in byte format.
	_data = new byte[_productionRate*_frameSizeInBytes];

        // Start the source data line
	_sourceLine.start();
    }

    private void _startPlaybackToFile() {
	// Array to hold all data to be saved to file. Grows
	// as new data are added (via putSamples()).
	// Each element is a byte of audio data.
	_toFileBuffer = new ArrayList();
	
	 boolean signed = true;
        boolean bigEndian = true;

        _playToFileFormat = new AudioFormat((float)_sampleRate,
                _sampleSizeInBits,
                _channels, signed, bigEndian);
        _frameSizeInBytes = _playToFileFormat.getFrameSize();
    }


    private void _stopPlaybackToFile() {
	
	int size =  _toFileBuffer.size();
	byte[] audioBytes = new byte[size];
	for (int i = 0; i < size; i++) {
	    Byte j = (Byte)_toFileBuffer.get(i);
	    audioBytes[i] = j.byteValue();
	}
	ByteArrayInputStream byteInputArrayStream = 
	    new ByteArrayInputStream(audioBytes);

	AudioInputStream audioInputStream = 
	    new AudioInputStream(byteInputArrayStream, 
				 _playToFileFormat, 
				 audioBytes.length /  _frameSizeInBytes);

	File outFile = new File(_fileName);

	try {

	    StringTokenizer st = new StringTokenizer(_fileName, ".");

	    // Do error checking:
	    if (st.countTokens() != 2) {
		System.err.println("Error: Incorrect file name format. Format: filname.extension");
	    }
	    
	    st.nextToken(); // Advance to the file extension.
	    
	    String fileExtension = st.nextToken();
	    
	    if (fileExtension.equalsIgnoreCase("au")) {
		// Save the file.
		AudioSystem.write(audioInputStream, 
				  AudioFileFormat.Type.AU, outFile);
	    } else if (fileExtension.equalsIgnoreCase("aiff")) {
		// Save the file.
		AudioSystem.write(audioInputStream, 
				  AudioFileFormat.Type.AIFF, outFile);
	    } else if (fileExtension.equalsIgnoreCase("wave")) {
		// Save the file.
		AudioSystem.write(audioInputStream, 
				  AudioFileFormat.Type.WAVE, outFile);
	    } else if (fileExtension.equalsIgnoreCase("wav")) {
		// Save the file.
		AudioSystem.write(audioInputStream, 
				  AudioFileFormat.Type.WAVE, outFile);
	    } else if (fileExtension.equalsIgnoreCase("aifc")) {
		// Save the file.
		AudioSystem.write(audioInputStream, 
				  AudioFileFormat.Type.AIFC, outFile);
	    } else {
		System.err.println("Error saving file: Unknown file format: "
				   + fileExtension);
	    }
	} catch (IOException e) {
	  System.err.println("SoundPlayback: error saving" +
          " file: " + e);
	}
    }

    /* Convert a double array of audio samples in linear signed pcm big endian
     * format into a byte array of audio samples (-1,1) range.
     * FIXME: This method only works for mono (single channel) audio.
     */
    private byte[] _doubleArrayToByteArray(double[] doubleArray,
            int _bytesPerSample) {

	//System.out.println("_bytesPerSample = " + _bytesPerSample);
	int lengthInSamples = doubleArray.length;
	double mathDotPow = Math.pow(2, 8 * _bytesPerSample - 1);
	byte[] byteArray = new byte[lengthInSamples * _bytesPerSample];
	for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {
	    long l = Math.round((doubleArray[currSamp] * mathDotPow));
	    byte[] b = new byte[_bytesPerSample];
	    for (int i = 0; i < _bytesPerSample; i += 1, l >>= 8)
		b[_bytesPerSample - i - 1] = (byte) l;
	    for (int i = 0; i < _bytesPerSample; i += 1) {
		//if (_isBigEndian)
                byteArray[currSamp*_bytesPerSample + i] = b[i];
                //else put(b[_bytesPerSample - i - 1]);
	    }
	}
	return byteArray;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _productionRate;

    private String _fileName;

    private String _playbackMode;

    private int _sampleSizeInBits;

    private int _putSamplesSize;
    
    private float _sampleRate;
    
    private int _channels;

    private int _bufferSize;

    // This is a stupid name, but it is consistant with
    // the Java Sound API naming conventions. It is
    // really a "target."
    private SourceDataLine _sourceLine;

    // Array of audio samples in byte format.
    private byte[] _data;

    private int _frameSizeInBytes;

    private ArrayList _toFileBuffer;

    // This is the format of _toFileBuffer.
    private AudioFormat _playToFileFormat;
}
