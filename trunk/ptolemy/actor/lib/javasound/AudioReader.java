/* An actor that outputs the sequence of sample values from a
   sound file specified as a URL.

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

@ProposedRating Yellow (vogel@eecs.berkeley.edu)
@AcceptedRating Yellow (chf@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.javasound;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import javax.sound.sampled.*;

import ptolemy.media.javasound.*;

/////////////////////////////////////////////////////////////////
//// AudioReader
/**
This actor sequentially outputs the samples from an sound file,
specified as a URL. Although the sound file must be specified
as a URL, it is still possible to specify files on the local
file system. The audio samples that are read from the file are
converted to DoubleTokens that may range from [-1.0, 1.0].
Thus, the output type of this actor is DoubleToken.
<p>
<b>Usage</b>
<p>
The <i>sourceURL</i> parameter should be set to the name of the file,
specified as a fully qualified URL. It is possible to load a file
from the local file system by using the prefix "file://" instead of
"http://". Relative file paths are allowed. To specify a file
relative to the current directory, use "../" or "./". For example,
if the current directory contains a file called "test.wav", then
<i>sourceURL</i> should be set to "file:./test.wav". If the parent
directory contains a file called "test.wav", then <i>sourceURL</i>
should be set to "file:../test.wav". To reference the file
test.wav, located at "/tmp/test.wav", <i>sourceURL</i>
should be set to "file:///tmp/test.wav" The default value is
"file:///tmp/test.wav".
<p>
The sound file is not periodically repeated by this actor, so
postfire() will return false when the end of the sound
file is reached.
<p>
There are security issues involved with accessing files and audio
resources in applets. Applets are only allowed access to files
specified by a URL and located on the machine from which the
applet is loaded. The .java.policy file may be modified to grant
applets more privileges.
<p>
Note: Requires Java 2 v1.3.0 or later.
@author Brian K. Vogel
@version $Id$
@see ptolemy.media.javasound.LiveSound
@see SoundWriter
@see SoundCapture
@see SoundPlayback
*/
public class AudioReader extends Source {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the parameters and initialize them to their default values.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AudioReader(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output.setTypeEquals(BaseType.DOUBLE);
	output.setMultiport(true);
	sourceURL = new StringAttribute(this, "sourceURL");
	sourceURL.setExpression("file:///tmp/test.wav");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** The URL of the file to read from. The default value of this
     *  parameter is the URL "file:///tmp/test.wav".
     *  Supported file formats are  WAV, AU, and AIFF. The sound
     *  file format is determined from the file extension.
     *  <p>
     *  An exception will occur if the path references a
     *  non-existent or unsupported sound file.
     */
    public StringAttribute sourceURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle change requests for all parameters. An exception is
     *  thrown if the requested change is not allowed.
     *  @exception IllegalActionException If the change is not
     *   allowed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
	if(_debugging) _debug("AudioReader: attributeChanged() invoked on: " +
                attribute.getName());
	if (attribute == sourceURL) {
	    if (_safeToInitialize == true) {
		try {
		    _initializeReader();
		} catch (IOException ex) {
		    throw new IllegalActionException(this,
                            "Cannot read audio:\n" +
                            ex);
		}
	    }
	} else {
	    super.attributeChanged(attribute);
	    return;
	}
    }

    /** Open the sound file specified by the URL for reading.
     *  @exception IllegalActionException If there is a problem opening
     *   the specified URL, or if the file has an unsupported audio
     *   format.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	if(_debugging) _debug("AudioReader: initialize(): invoked");
	try {
	    _initializeReader();
	} catch (IOException ex) {
	    throw new IllegalActionException(this,
                    "Cannot open the specified URL: " +
                    ex);
	}
	_safeToInitialize = true;
	_haveASample = false;
    }

    /** Invoke <i>count</i> iterations of this actor. This method
     *  causes one audio sample per channel per iteration to be
     *  read from the specified file. Each sample is converted to
     *  a double token, with a maximum range of -1.0 to 1.0.
     *  One double token per channel is written to the output port
     *  in an iteration.
     *  <p>
     *  This method should be called instead of the prefire(),
     *  fire(), and postfire() methods when this actor is used in a
     *  domain that supports vectorized actors.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Return STOP_ITERATING if the
     *   end of the sound file is reached.
     *  @see ptolemy.actor.Executable
     *  @exception IllegalActionException If there is a problem reading
     *   from the specified sound file.
     */
    public int iterate(int count) throws IllegalActionException {
	// Check if we need to reallocate the output token array.
	if (count > _audioSendArray.length) {
	    _audioSendArray = new DoubleToken[count];
	}
	// For each sample.
	for (int i = 0; i < count; i++) {
	    if (_haveASample == false) {
		// Need to read more data.
		try {
		    // Read in audio data.
		    _audioInDoubleArray = _soundReader.getSamples();
		} catch (Exception ex) {
		    throw new IllegalActionException(this,
                            "Unable to open the sound file for reading: " +
                            ex);
		}
		_getSamplesArrayPointer = 0;
		// Check that the read was successful
		if (_audioInDoubleArray != null) {
		    _haveASample = true;
		}
	    }
	    // Note: we cannot use an if..then..else here because the
	    // above block may set _haveASample = true. Thus, we may
	    // sometimes need to execute both blocks.
	    if (_haveASample == true) {
		// Copy a sample to the output array.
		// For each channel.
		for (int j = 0; j < _channels; j++) {
		    _audioSendArray[i] =
			new DoubleToken(_audioInDoubleArray[j][_getSamplesArrayPointer]);
		}
		_getSamplesArrayPointer++;
		// Check if we still have at least one sample left.
		if ((_audioInDoubleArray[0].length - _getSamplesArrayPointer) <= 0) {
		    // We just ran out of samples.
		    _haveASample = false;
		}
	    }
	}
	// Check that the read was successful
	if (_audioInDoubleArray != null) {
	    // Send.
	    for (int j = 0; j < _channels; j++) {
		output.send(j, _audioSendArray, count);
	    }
	    return COMPLETED;
	} else {
	    // Read was unsuccessful, so output an array of zeros.
	    // This generally means that the end of the sound file
	    // has been reached.
	    // Convert to DoubleToken[].
	    for (int i = 0; i < count; i++) {
		_audioSendArray[i] = new DoubleToken(0);
	    }
	    // Output an array of zeros on each channel.
	    for (int j = 0; j < _channels; j++) {
		output.send(j, _audioSendArray, count);
	    }
	    return STOP_ITERATING;
	}
    }

    /** This method causes one audio sample per channel to be
     *  read from the specified file. Each sample is converted to
     *  a double token, with a maximum range of -1.0 to 1.0.
     *  One double token per channel is written to the output port.
     *  @return True if there are samples available from the
     *  audio source. False if there are no more samples (end
     *  of sound file reached).
     *  @exception IllegalActionException If there is a problem reading
     *   from the specified sound file.
     */
    public boolean postfire() throws IllegalActionException {
	int returnVal = iterate(1);
	if (returnVal == COMPLETED) {
	    return true;
	} else if (returnVal == NOT_READY) {
	    // This should never happen.
	    throw new IllegalActionException(this, "Actor " +
                    "is not ready to fire.");
	} else if (returnVal == STOP_ITERATING) {
	    return false;
	}
	return false;
    }

    /** Free up any system resources involved in the audio
     *  reading process and close any open sound files.
     *
     *  @exception IllegalActionException If there is a
     *   problem closing the file.
     */
    public void wrapup() throws IllegalActionException {
	if(_debugging) _debug("AudioReader: wrapup(): invoked");
	// Stop capturing audio.
	if (_soundReader != null) {
	    try {
		_soundReader.closeFile();
	    } catch (IOException ex) {
		throw new IllegalActionException(this,
                        "Problem closing sound file: \n" +
                        ex.getMessage());
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize/Reinitialize audio reading. First close any
     *  open files. Then read the <i>sourceURL</i> parameter and
     *  open the file specified by this parameter.
     *  <p>
     *  This method is synchronized since it is not safe to call
     *  other SoundReader methods while this method is executing.
     *  @exception IllegalActionException If there is a problem initializing
     *   the audio reader.
     */
    private synchronized void _initializeReader()
            throws IOException, IllegalActionException {
	if (_soundReader != null) {
            _soundReader.closeFile();
	}
	// Load audio from a URL.
	String theURL = sourceURL.getExpression();
	// Each read this many samples per channel when
	// _soundReader.getSamples() is called.
	// This value was chosen somewhat arbitrarily.
	int getSamplesArraySize = 64;
	_soundReader = new SoundReader(theURL,
                getSamplesArraySize);
	// Read the number of audio channels and set
	// parameter accordingly.
	_channels = _soundReader.getChannels();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private SoundReader _soundReader;
    private int _channels;
    private double[][] _audioInDoubleArray;
    private boolean _haveASample;
    private int _getSamplesArrayPointer;
    private DoubleToken[] _audioSendArray = new DoubleToken[1];
    private boolean _safeToInitialize = false;
}
