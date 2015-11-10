// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2015 The Regents of the University of California.
// All rights reserved.
//
// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.
//
// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.
//
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.
/**
 * Module to access audio hardware on the host.
 * NOTE: This is very incomplete! Just a placeholder for now.
 * @module audio
 * @authors Edward A. Lee
 */
 
// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, exports */
/*jshint globalstrict: true*/
"use strict";

// Reference to the Java class documented at:
//    http://terra.eecs.berkeley.edu:8080/job/ptII/javadoc/ptolemy/media/javasound/LiveSound.html
var LiveSound = Java.type('ptolemy.media.javasound.LiveSound');

/** Construct an instance of an Player object type. This should be instantiated in your
 *  JavaScript code as
 *  <pre>
 *     var Audio = require("audio");
 *     var player = new Audio.Player();
 *  </pre>
 *  An instance of this object type implements the following functions:
 *  (FIXME: replace with your design)
 *  <ul>
 *  <li> play(data): Play the specified array.
 *  <li> stop(): Stop playback and free the audio resources.
 *  </ul>
 *  @param options A JSON object with fields 'FIXME' and 'FIXME' that give the
 *   FIXME properties of the audio such as sample rate, etc. Provide reasonable
 *   defaults.
 */
exports.Player = function(options) {
    // Provide default values for options using the following common JavaScript idiom.
    options = options || {};
    this.foo = options.foo || 80;
    
    LiveSound.setSampleRate(8000);
    // Start playback.
    LiveSound.startPlayback(this);
};

/** Play audio data.
 *  @param data An array of numbers in the range -1 to 1 to be played.
 */
exports.Player.prototype.play = function(data) {
    // NOTE: Convert array into 2-D array required by LiveSound.
    LiveSound.putSamples(this, [data]);
};

/** Stop the player and free audio resources. */
exports.Player.prototype.stop = function() {
    LiveSound.stopPlayback(this);
};

// Below is code to be added by students.

/** Construct an instance of a Capture object type. This should be instantiated in your
 *  JavaScript code as
 *  <pre>
 *     var audio = require("audio");
 *     var capture = new audio.Capture();
 *  </pre>
 *  An instance of this object type implements the following functions:
 *  (FIXME: replace with your design)
 *  <ul>
 *  <li> get(): Return an array of audio data.
 *  <li> stop(): Stop capture and free the audio resources.
 *  </ul>
 *  @param options A JSON object with fields 'FIXME' and 'FIXME' that give the
 *   FIXME properties of the audio such as sample rate, etc. Provide reasonable
 *   defaults.
 */
exports.Capture = function(options) {
    LiveSound.setSampleRate(8000);
    // Start playback.
    LiveSound.startCapture(this);
};

/** Capture audio data.
 *  @return An array of numbers in the range -1 to 1 captured from the audio.
 */
exports.Capture.prototype.get = function(data) {
    // NOTE: 2-D double[][] array returned by LiveSound.
    var inputData = LiveSound.getSamples(this);
    // Could use Nashorn-specific conversion to convert to a JavaScript array,
    // as follows:
    // var channels = Java.from(data);
    // var sound = Java.from(channels[0]);

    // Note that we return only channel 0.
    if (inputData.length >= 1) {
        var channels = inputData[0];
        return channels;
    }
    throw("No audio data returned.");
};

/** Stop the capture and free audio resources. */
exports.Capture.prototype.stop = function() {
    LiveSound.stopCapture(this);
};
