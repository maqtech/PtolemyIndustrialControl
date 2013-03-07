/* Class representing an audio parameter change of LiveSound.

 Copyright (c) 1998-2013 The Regents of the University of California.
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
package ptolemy.media.javasound;

///////////////////////////////////////////////////////////////////
//// LiveSoundEvent

/**
 A LiveSoundEvent represents a change in an audio parameter of
 LiveSound.  This event will be generated by LiveSound when an
 audio parameter change (e.g., a change in the sample rate)
 occurs, and is passed to the live sound event listeners to
 notify them about the change.

 @author Brian K. Vogel
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (vogel)
 @Pt.AcceptedRating Red
 @see LiveSoundListener
 */
public class LiveSoundEvent {
    /** Construct a LiveSoundEvent, with the specified parameter.
     *
     *  @param parameter The audio parameter of LiveSound that
     *   has changed. The value of parameter should be one of
     *   LiveSoundEvent.SAMPLE_RATE, LiveSoundEvent.CHANNELS,
     *   LiveSoundEvent.BUFFER_SIZE, or
     *   LiveSoundEvent.BITS_PER_SAMPLE.
     */
    public LiveSoundEvent(int parameter) {
        // FIXME: Should check that the value is parameter is legal.
        _parameter = parameter;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the parameter of LiveSound that has changed. The
     *  corresponding method of LiveSound may then be invoked do
     *  discover the new value of the parameter. For example, if
     *  a sample rate change occurs, then this method will return
     *  LiveSoundEvent.SAMPLE_RATE. The getSampleRate() method of
     *  LiveSound may then be invoked to discover the new value
     *  of the sample rate.
     *
     *  @return SAMPLE_RATE, CHANNELS, BUFFER_SIZE, or BITS_PER_SAMPLE.
     */
    public int getSoundParameter() {
        return _parameter;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** The value indicates a sample rate change event.
     */
    public static final int SAMPLE_RATE = 0;

    /** The value indicates a channel number change event.
     */
    public static final int CHANNELS = 1;

    /** The value indicates a buffer size change event.
     */
    public static final int BUFFER_SIZE = 2;

    /** The value indicates a bits per channel change event.
     */
    public static final int BITS_PER_SAMPLE = 3;

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                    ////
    private int _parameter;
}
