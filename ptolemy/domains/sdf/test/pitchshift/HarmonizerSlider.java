/* Perform real-time pitch shifting of audio signals.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
package ptolemy.domains.sdf.test.pitchshift;

import ptolemy.domains.sdf.test.pitchshift.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.text.NumberFormat;
import javax.swing.border.*;
import java.io.File;
import java.util.Vector;
import javax.media.sound.sampled.*;
import javax.media.sound.midi.*;

//////////////////////////////////////////////////////////////////////////
//// HarmonizerSlider
/**
Perform real-time pitch shifting of audio signals. This only works
for audio signals that have either a unique pitch or no pitch at
any given time (pitched or unpitched, voiced or unvoiced). Examples
inlude human vocal sounds and sounds from musical instruments capable
of playing only one note at a times (e.g., horns, flute). The pitch
shifting algorithm is based on the algorithm proposed by Keith Lent
in his paper: "An efficient Method for Pitch Shifting Digitally
Sampled Sounds", published in the Computer Music Journal, Vol 13,
No. 4, Winter 1989. The algorithm is presented with more mathematical
rigore in the paper by Robert Bristow-Johnson:
"A Detailed Analysis of a Time-Domain Formant-Corrected Pitch-
Shifting Algorithm", in J. Audio Eng. Soc., Vol 43, No. 5, May 1995.
<p>
The pitch shifting algorithm uses a pitch-synchronous overlap-add (PSOLA)
based algorithm, and therefore requires the pitch of the input signal.
The pitch detector used in Keith Lent's algorithm consists of a
bandpass filter followed by a simple negative-slop zero-crossing detector.
I found such a simple pitch detector to be completely unusable for
vocal and musical instrument sounds. I therefore decided to implement
a more robust pitch detector. I am currently using a pitch detector
that uses cepstrum analysis. The (real) cepstrum is computed, and
then peak finding is performed on the high-time region of the cepstrum.
This cepstral technique works well for vocal sounds but does not
currently perform well for pitches above about 600 Hz.
<p>
Note: This application requires JDK 1.3. and at least a
Pentium II 400 MHz class processor (for 22050 Hz sample rate).
Also requires ptolemy.math (used by the pitch detector).
@author Brian K. Vogel
@version 1.0
 */
public class HarmonizerSlider extends JFrame {

    ProcessAudioHarmonizer processAudio;

    DecimalField textField;
    DecimalField textField2;
    DecimalField textField3;
    ConverterRangeModel sliderModel;
    ConverterRangeModel sliderModel2;
    ConverterRangeModel sliderModel3;

    boolean frozen = false;

    public HarmonizerSlider(String windowTitle) {
        super(windowTitle);

	////////////////////////////////////////////
            // 1st slider and textfield

            //Create the slider and its label
            JLabel sliderLabel = new JLabel("Pitch Scale Factor 1", JLabel.CENTER);
            sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            numberFormat.setMaximumFractionDigits(3);
            textField = new DecimalField(1, 10, numberFormat);
            sliderModel = new ConverterRangeModel();
            textField.setValue(0.001*sliderModel.getDoubleValue());
            textField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    sliderModel.setDoubleValue(1000*textField.getValue());

		}
            });

            JSlider pitchSlider = new JSlider(sliderModel);
            pitchSlider.addChangeListener(new SliderListener());
            sliderModel.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    //textField.setValue(sliderModel.getDoubleValue());
		    textField.setValue(0.001*sliderModel.getDoubleValue());
		}
	    });

            //Turn on labels at major tick marks.
            pitchSlider.setPaintTicks(false);
            pitchSlider.setPaintLabels(true);
            pitchSlider.setBorder(
                    BorderFactory.createEmptyBorder(0,0,10,0));

            ////////////////////////////////////////////
                // 2nd slider and textfield

                //Create the slider and its label
                JLabel sliderLabel2 = new JLabel("Pitch Scale Factor 2", JLabel.CENTER);
                sliderLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

                NumberFormat numberFormat2 = NumberFormat.getNumberInstance();
                numberFormat2.setMaximumFractionDigits(3);
                textField2 = new DecimalField(1, 10, numberFormat2);
                sliderModel2 = new ConverterRangeModel();
                textField2.setValue(0.001*sliderModel2.getDoubleValue());
                textField2.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sliderModel2.setDoubleValue(1000*textField2.getValue());

                    }
		});

                JSlider pitchSlider2 = new JSlider(sliderModel2);
                pitchSlider2.addChangeListener(new SliderListener2());
                sliderModel2.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        //textField.setValue(sliderModel.getDoubleValue());
                        textField2.setValue(0.001*sliderModel2.getDoubleValue());
                    }
                });

                //Turn on labels at major tick marks.
                pitchSlider2.setPaintTicks(false);
                pitchSlider2.setPaintLabels(true);
                pitchSlider2.setBorder(
                        BorderFactory.createEmptyBorder(0,0,10,0));


                ////////////////////////////////////////////
                    // 3rd slider and textfield

                    //Create the slider and its label
                    JLabel sliderLabel3 = new JLabel("Pitch Scale Factor 3", JLabel.CENTER);
                    sliderLabel3.setAlignmentX(Component.CENTER_ALIGNMENT);

                    NumberFormat numberFormat3 = NumberFormat.getNumberInstance();
                    numberFormat3.setMaximumFractionDigits(3);
                    textField3 = new DecimalField(1, 10, numberFormat3);
                    sliderModel3 = new ConverterRangeModel();
                    textField3.setValue(0.001*sliderModel3.getDoubleValue());
                    textField3.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            sliderModel3.setDoubleValue(1000*textField3.getValue());

                        }
                    });

                    JSlider pitchSlider3 = new JSlider(sliderModel3);
                    pitchSlider3.addChangeListener(new SliderListener3());
                    sliderModel3.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            //textField.setValue(sliderModel.getDoubleValue());
                            textField3.setValue(0.001*sliderModel3.getDoubleValue());
                        }
                    });

                    //Turn on labels at major tick marks.
                    pitchSlider3.setPaintTicks(false);
                    pitchSlider3.setPaintLabels(true);
                    pitchSlider3.setBorder(
                            BorderFactory.createEmptyBorder(0,0,10,0));




                    //Put everything in the content pane.
                    JPanel contentPane = new JPanel();
                    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
                    // group 1
                    contentPane.add(sliderLabel);
                    contentPane.add(pitchSlider);
                    contentPane.add(textField);
                    // group 2
                    contentPane.add(sliderLabel2);
                    contentPane.add(pitchSlider2);
                    contentPane.add(textField2);
                    // group 3
                    contentPane.add(sliderLabel3);
                    contentPane.add(pitchSlider3);
                    contentPane.add(textField3);
                    /////////
                    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
                    setContentPane(contentPane);



                    //Add a listener for window events
                    addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            System.exit(0);
                        }
                    });

    }

    /** Listens to the slider. */
    class SliderListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            int fps = (int)source.getValue();
            // Update pitch while user is sliding the slider.
            processAudio.updatePitchScaleFactor((double)fps/(double)1000);

        }
    }

    /** Listens to the slider. */
    class SliderListener2 implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            int fps = (int)source.getValue();
            // Update pitch while user is sliding the slider.
            processAudio.updatePitchScaleFactor2((double)fps/(double)1000);

        }
    }

    /** Listens to the slider. */
    class SliderListener3 implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            int fps = (int)source.getValue();
            // Update pitch while user is sliding the slider.
            processAudio.updatePitchScaleFactor3((double)fps/(double)1000);

        }
    }

    public void startPitchShifting(double sampRate) {

	processAudio = new ProcessAudioHarmonizer();
	processAudio.setSamplingRate(sampRate);
	processAudio.start();

	frozen = false;
    }

    public void stopPitchShifting() {
        frozen = true;
    }


    public static void main(String[] args) {
	double sampRate;
        HarmonizerSlider pitchSlider = new HarmonizerSlider("HarmonizerSlider");
        pitchSlider.pack();
        pitchSlider.setVisible(true);
	System.out.println("");
	System.out.println("HarmonizerSlider v1.0 by Brian K. Vogel, vogel@eecs.berkeley.edu");
	System.out.println("");
	System.out.println("Usage: java HarmonizerSlider <sample rate>");
	System.out.println("<sample rate> is optional.");
	System.out.println("");

	if (args.length == 1) {
	    // Optional argument is the sampling rate.
	    sampRate = (new Double(args[0])).doubleValue();
	} else {
	    sampRate = 22050;
	}
        pitchSlider.startPitchShifting(sampRate);
    }
}

