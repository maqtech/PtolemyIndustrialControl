/* An applet that uses Ptolemy II FSM and DE domains.

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

@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.demo.ABP;

// FIXME: Trim this.
import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.gui.*;
import ptolemy.domains.de.lib.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.plot.*;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import java.util.Enumeration;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import javax.swing.BoxLayout;


//////////////////////////////////////////////////////////////////////////
//// ABPApplet
/**
An applet that uses Ptolemy II FSM and DE domains to model communication
using the alternating bit protocol.

@author Xiaojun Liu, Steve Neuendorffer
@version $Id$
*/
public class ABP extends TypedCompositeActor {

    public ABP(Workspace workspace)
	    throws IllegalActionException, NameDuplicationException {

        // Create the model.
        super(workspace);
	setName("ABP");
        Manager manager = new Manager(workspace, "Manager");
        setManager(manager);
        
        DEDirector director = new DEDirector(this, "DEDirector");

        Parameter forwardRate = new Parameter(this, "forwardRate");
        forwardRate.setExpression("0.3");

        Parameter backwardRate = new Parameter(this, "backwardRate");
        backwardRate.setExpression("0.2");

        Parameter stopTime = new Parameter(this, "stopTime");
        stopTime.setExpression("100.0");
        director.stopTime.setExpression("stopTime");

        DEMessageSource msgSrc = new DEMessageSource(this, "MessageSource");
        DETimer timer = new DETimer(this, "Timer");

        // forward packet channel
        DEChannel forward = new DEChannel(this, "ForwardChannel");
        forward.dropRate.setExpression("forwardRate");
        forward.maxDelay.setToken(new DoubleToken(5.0));
        forward.minDelay.setToken(new DoubleToken(0.5));

        // backward packet channel
        DEChannel backward = new DEChannel(this, "BackwardChannel");
        backward.dropRate.setExpression("backwardRate");
        backward.maxDelay.setToken(new DoubleToken(2.0));
        backward.minDelay.setToken(new DoubleToken(0.2));

        // the plot
        TimedPlotter plot = new TimedPlotter(this, "Plot");
        plot.plot = new Plot();
        plot.plot.setGrid(false);
        plot.plot.setTitle("Events");
        plot.plot.addLegend(0, "Received");
        plot.plot.addLegend(1, "Sent");
        plot.plot.addLegend(2, "AltBit");
        plot.plot.setXLabel("Time");
        plot.plot.setYLabel("Events");
        plot.plot.setXRange(0.0, 100.0);
        plot.plot.setYRange(0.0, 12.0);
        plot.plot.setConnected(false);
        plot.plot.setImpulses(true);
        plot.plot.setMarksStyle("dots");
        plot.fillOnWrapup.setToken(new BooleanToken(false));

        // sender - a hierarchical FSM
        TypedCompositeActor sender = new TypedCompositeActor(this, "Sender");
        // create ports
        TypedIOPort sdrRequest = (TypedIOPort)sender.newPort("request");
        sdrRequest.setInput(true);
        sdrRequest.setTypeEquals(BaseType.GENERAL);
        TypedIOPort sdrMsgIn = (TypedIOPort)sender.newPort("msgIn");
        sdrMsgIn.setInput(true);
        sdrMsgIn.setTypeEquals(BaseType.INT);
        TypedIOPort sdrNext = (TypedIOPort)sender.newPort("next");
        sdrNext.setOutput(true);
        sdrNext.setTypeEquals(BaseType.GENERAL);
        TypedIOPort sdrError = (TypedIOPort)sender.newPort("error");
        sdrError.setOutput(true);
        sdrError.setTypeEquals(BaseType.GENERAL);
        TypedIOPort sdrAck = (TypedIOPort)sender.newPort("ack");
        sdrAck.setInput(true);
        sdrAck.setTypeEquals(BaseType.INT);
        TypedIOPort sdrPktOut = (TypedIOPort)sender.newPort("pktOut");
        sdrPktOut.setOutput(true);
        sdrPktOut.setTypeEquals(BaseType.INT);
        TypedIOPort sdrSetTimer = (TypedIOPort)sender.newPort("setTimer");
        sdrSetTimer.setOutput(true);
        sdrSetTimer.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort sdrExpired = (TypedIOPort)sender.newPort("expired");
        sdrExpired.setInput(true);
        sdrExpired.setTypeEquals(BaseType.GENERAL);
        TypedIOPort sdrMonitor = (TypedIOPort)sender.newPort("monitor");
        sdrMonitor.setOutput(true);
        sdrMonitor.setTypeEquals(BaseType.INT);

        // sender's mode controller
        FSMActor ctrl = new FSMActor(sender, "Controller");

        TypedIOPort ctrlNext = (TypedIOPort)ctrl.newPort("next");
        ctrlNext.setInput(true);
        ctrlNext.setTypeEquals(BaseType.GENERAL);
        TypedIOPort ctrlError = (TypedIOPort)ctrl.newPort("error");
        ctrlError.setInput(true);
        ctrlError.setTypeEquals(BaseType.GENERAL);

        State ctrlConnecting = new State(ctrl, "Connecting");
        State ctrlDead = new State(ctrl, "Dead");
        State ctrlSending = new State(ctrl, "Sending");
        ctrl.initialStateName.setExpression("Connecting");
        Transition ctrlTr1 = new Transition(ctrl, "ctrlTr1");
        ctrlConnecting.outgoingPort.link(ctrlTr1);
        ctrlSending.incomingPort.link(ctrlTr1);
        ctrlTr1.setGuardExpression("next_S");
        Transition ctrlTr2 = new Transition(ctrl, "ctrlTr2");
        ctrlConnecting.outgoingPort.link(ctrlTr2);
        ctrlDead.incomingPort.link(ctrlTr2);
        ctrlTr2.setGuardExpression("error_S");

        // sender's director
        FSMDirector sdrDir = new FSMDirector(sender, "SenderDirector");
        sdrDir.controllerName.setExpression("Controller");

        // submachine refining sender's connecting state
        FSMActor connect = new FSMActor(sender, "Connect");
        // ports
        TypedIOPort conRequest = (TypedIOPort)connect.newPort("request");
        conRequest.setInput(true);
        conRequest.setTypeEquals(BaseType.GENERAL);
        TypedIOPort conNext = (TypedIOPort)connect.newPort("next");
        conNext.setOutput(true);
        conNext.setTypeEquals(BaseType.GENERAL);
        TypedIOPort conError = (TypedIOPort)connect.newPort("error");
        conError.setOutput(true);
        conError.setTypeEquals(BaseType.GENERAL);
        TypedIOPort conAck = (TypedIOPort)connect.newPort("ack");
        conAck.setInput(true);
        conAck.setTypeEquals(BaseType.INT);
        TypedIOPort conPktOut = (TypedIOPort)connect.newPort("pktOut");
        conPktOut.setOutput(true);
        conPktOut.setTypeEquals(BaseType.INT);
        TypedIOPort conSetTimer = (TypedIOPort)connect.newPort("setTimer");
        conSetTimer.setOutput(true);
        conSetTimer.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort conExpired = (TypedIOPort)connect.newPort("expired");
        conExpired.setInput(true);
        conExpired.setTypeEquals(BaseType.GENERAL);
        // connect's states and transitions
        State conInit = new State(connect, "Init");
        State conWait = new State(connect, "Wait");
        State conSucc = new State(connect, "Success");
        State conFail = new State(connect, "Fail");
        connect.initialStateName.setExpression("Init");
        Transition conTr1 = new Transition(connect, "conTr1");
        conInit.outgoingPort.link(conTr1);
        conWait.incomingPort.link(conTr1);
        conTr1.setGuardExpression("request_S");
        BroadcastOutput conTr1Act1 = new BroadcastOutput(conTr1, "conTr1Act1");
        conTr1Act1.portName.setExpression("pktOut");
        conTr1Act1.expression.setExpression("-1");
        BroadcastOutput conTr1Act2 = new BroadcastOutput(conTr1, "conTr1Act2");
        conTr1Act2.portName.setExpression("setTimer");
        conTr1Act2.expression.setExpression(TIME_OUT);
        SetVariable conTr1Act3 = new SetVariable(conTr1, "conTr1Act3");
        conTr1Act3.variableName.setExpression("count");
        conTr1Act3.expression.setExpression("5");
        Transition conTr2 = new Transition(connect, "conTr2");
        conWait.outgoingPort.link(conTr2);
        conSucc.incomingPort.link(conTr2);
        conTr2.setGuardExpression("(ack_S ? ack_V : 0) == -1");
        BroadcastOutput conTr2Act1 = new BroadcastOutput(conTr2, "conTr2Act1");
        conTr2Act1.portName.setExpression("next");
        conTr2Act1.expression.setExpression("true");
        BroadcastOutput conTr2Act2 = new BroadcastOutput(conTr2, "conTr2Act2");
        conTr2Act2.portName.setExpression("setTimer");
        conTr2Act2.expression.setExpression(RESET);
        Transition conTr3 = new Transition(connect, "conTr3");
        conWait.outgoingPort.link(conTr3);
        conFail.incomingPort.link(conTr3);
        conTr3.setGuardExpression("!ack_S && expired_S && count == 0");
        BroadcastOutput conTr3Act1 = new BroadcastOutput(conTr3, "conTr3Act1");
        conTr3Act1.portName.setExpression("error");
        conTr3Act1.expression.setExpression("true");
        Transition conTr4 = new Transition(connect, "conTr4");
        conWait.outgoingPort.link(conTr4);
        conFail.incomingPort.link(conTr4);
        conTr4.setGuardExpression("(ack_S ? ack_V : -1) != -1 && "
                 + "expired_S && count == 0");
        BroadcastOutput conTr4Act1 = new BroadcastOutput(conTr4, "conTr4Act1");
        conTr4Act1.portName.setExpression("error");
        conTr4Act1.expression.setExpression("true");
        Transition conTr5 = new Transition(connect, "conTr5");
        conWait.outgoingPort.link(conTr5);
        conWait.incomingPort.link(conTr5);
        conTr5.setGuardExpression("!ack_S && expired_S && count != 0");
        BroadcastOutput conTr5Act1 = new BroadcastOutput(conTr5, "conTr5Act1");
        conTr5Act1.portName.setExpression("pktOut");
        conTr5Act1.expression.setExpression("-1");
        BroadcastOutput conTr5Act2 = new BroadcastOutput(conTr5, "conTr5Act2");
        conTr5Act2.portName.setExpression("setTimer");
        conTr5Act2.expression.setExpression(TIME_OUT);
        SetVariable conTr5Act3 = new SetVariable(conTr5, "conTr5Act3");
        conTr5Act3.variableName.setExpression("count");
        conTr5Act3.expression.setExpression("count - 1");
        Transition conTr6 = new Transition(connect, "conTr6");
        conWait.outgoingPort.link(conTr6);
        conWait.incomingPort.link(conTr6);
        conTr6.setGuardExpression("(ack_S ? ack_V : -1) != -1 "
                + "&& expired_S && count != 0");
        BroadcastOutput conTr6Act1 = new BroadcastOutput(conTr6, "conTr6Act1");
        conTr6Act1.portName.setExpression("pktOut");
        conTr6Act1.expression.setExpression("-1");
        BroadcastOutput conTr6Act2 = new BroadcastOutput(conTr6, "conTr6Act2");
        conTr6Act2.portName.setExpression("setTimer");
        conTr6Act2.expression.setExpression(TIME_OUT);
        SetVariable conTr6Act3 = new SetVariable(conTr6, "conTr6Act3");
        conTr6Act3.variableName.setExpression("count");
        conTr6Act3.expression.setExpression("count - 1");
        // create the local variable
        Variable conCount = new Variable(connect, "count");
        conCount.setTypeEquals(BaseType.INT);
        conCount.setToken(new IntToken(0));
        // set connect to be ctrlConnecting's refinement
        ctrlConnecting.refinementName.setExpression("Connect");

        // the submachine refining sender's sending state
        FSMActor send = new FSMActor(sender, "Send");
        // create ports
        TypedIOPort sendMsgIn = (TypedIOPort)send.newPort("msgIn");
        sendMsgIn.setInput(true);
        sendMsgIn.setTypeEquals(BaseType.INT);
        TypedIOPort sendNext = (TypedIOPort)send.newPort("next");
        sendNext.setOutput(true);
        sendNext.setTypeEquals(BaseType.GENERAL);
        TypedIOPort sendAck = (TypedIOPort)send.newPort("ack");
        sendAck.setInput(true);
        sendAck.setTypeEquals(BaseType.INT);
        TypedIOPort sendPktOut = (TypedIOPort)send.newPort("pktOut");
        sendPktOut.setOutput(true);
        sendPktOut.setTypeEquals(BaseType.INT);
        TypedIOPort sendSetTimer = (TypedIOPort)send.newPort("setTimer");
        sendSetTimer.setOutput(true);
        sendSetTimer.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort sendExpired = (TypedIOPort)send.newPort("expired");
        sendExpired.setInput(true);
        sendExpired.setTypeEquals(BaseType.GENERAL);
        TypedIOPort sendMonitor = (TypedIOPort)send.newPort("monitor");
        sendMonitor.setOutput(true);
        sendMonitor.setTypeEquals(BaseType.INT);
        // the states and transitions
        State s0 = new State(send, "0");
        State s1 = new State(send, "1");
        send.initialStateName.setExpression("0");
        Transition sendTr1 = new Transition(send, "sendTr1");
        s0.outgoingPort.link(sendTr1);
        s0.incomingPort.link(sendTr1);
        sendTr1.setGuardExpression("msgIn_S");
        BroadcastOutput sendTr1Act1 =
                new BroadcastOutput(sendTr1, "sendTr1Act1");
        sendTr1Act1.portName.setExpression("pktOut");
        sendTr1Act1.expression.setExpression("msgIn_V*2");
        BroadcastOutput sendTr1Act2 =
                new BroadcastOutput(sendTr1, "sendTr1Act2");
        sendTr1Act2.portName.setExpression("monitor");
        sendTr1Act2.expression.setExpression("0");
        BroadcastOutput sendTr1Act3 =
                new BroadcastOutput(sendTr1, "sendTr1Act3");
        sendTr1Act3.portName.setExpression("setTimer");
        sendTr1Act3.expression.setExpression(TIME_OUT);
        SetVariable sendTr1Act4 =
                new SetVariable(sendTr1, "sendTr1Act4");
        sendTr1Act4.variableName.setExpression("trying");
        sendTr1Act4.expression.setExpression("true");
        SetVariable sendTr1Act5 =
                new SetVariable(sendTr1, "sendTr1Act5");
        sendTr1Act5.variableName.setExpression("msg");
        sendTr1Act5.expression.setExpression("msgIn_V");
        Transition sendTr2 = new Transition(send, "sendTr2");
        s0.outgoingPort.link(sendTr2);
        s0.incomingPort.link(sendTr2);
        sendTr2.setGuardExpression("!ack_S && expired_S && trying");
        BroadcastOutput sendTr2Act1 =
                new BroadcastOutput(sendTr2, "sendTr2Act1");
        sendTr2Act1.portName.setExpression("pktOut");
        sendTr2Act1.expression.setExpression("msgIn_V*2");
        BroadcastOutput sendTr2Act2 =
                new BroadcastOutput(sendTr2, "sendTr2Act2");
        sendTr2Act2.portName.setExpression("monitor");
        sendTr2Act2.expression.setExpression("0");
        BroadcastOutput sendTr2Act3 =
                new BroadcastOutput(sendTr2, "sendTr2Act3");
        sendTr2Act3.portName.setExpression("setTimer");
        sendTr2Act3.expression.setExpression(TIME_OUT);
        Transition sendTr3 = new Transition(send, "sendTr3");
        s0.outgoingPort.link(sendTr3);
        s0.incomingPort.link(sendTr3);
        sendTr3.setGuardExpression("(ack_S ? ack_V : 0) != 0 "
                + "&& expired_S && trying");
        BroadcastOutput sendTr3Act1 =
                new BroadcastOutput(sendTr3, "sendTr3Act1");
        sendTr3Act1.portName.setExpression("pktOut");
        sendTr3Act1.expression.setExpression("msgIn_V*2");
        BroadcastOutput sendTr3Act2 =
                new BroadcastOutput(sendTr3, "sendTr3Act2");
        sendTr3Act2.portName.setExpression("monitor");
        sendTr3Act2.expression.setExpression("0");
        BroadcastOutput sendTr3Act3 =
                new BroadcastOutput(sendTr3, "sendTr3Act3");
        sendTr3Act3.portName.setExpression("setTimer");
        sendTr3Act3.expression.setExpression(TIME_OUT);
        Transition sendTr4 = new Transition(send, "sendTr4");
        s0.outgoingPort.link(sendTr4);
        s1.incomingPort.link(sendTr4);
        sendTr4.setGuardExpression("(ack_S ? ack_V : -1) == 0 "
                + "&& trying");
        BroadcastOutput sendTr4Act1 =
                new BroadcastOutput(sendTr4, "sendTr4Act1");
        sendTr4Act1.portName.setExpression("setTimer");
        sendTr4Act1.expression.setExpression(RESET);
        BroadcastOutput sendTr4Act2 =
                new BroadcastOutput(sendTr4, "sendTr4Act2");
        sendTr4Act2.portName.setExpression("next");
        sendTr4Act2.expression.setExpression("true");
        SetVariable sendTr4Act3 =
                new SetVariable(sendTr4, "sendTr4Act3");
        sendTr4Act3.variableName.setExpression("trying");
        sendTr4Act3.expression.setExpression("false");
        Transition sendTr5 = new Transition(send, "sendTr5");
        s1.outgoingPort.link(sendTr5);
        s1.incomingPort.link(sendTr5);
        sendTr5.setGuardExpression("msgIn_S");
        BroadcastOutput sendTr5Act1 =
                new BroadcastOutput(sendTr5, "sendTr5Act1");
        sendTr5Act1.portName.setExpression("pktOut");
        sendTr5Act1.expression.setExpression("msgIn_V*2+1");
        BroadcastOutput sendTr5Act2 =
                new BroadcastOutput(sendTr5, "sendTr5Act2");
        sendTr5Act2.portName.setExpression("monitor");
        sendTr5Act2.expression.setExpression("1");
        BroadcastOutput sendTr5Act3 =
                new BroadcastOutput(sendTr5, "sendTr5Act3");
        sendTr5Act3.portName.setExpression("setTimer");
        sendTr5Act3.expression.setExpression(TIME_OUT);
        SetVariable sendTr5Act4 =
                new SetVariable(sendTr5, "sendTr5Act4");
        sendTr5Act4.variableName.setExpression("trying");
        sendTr5Act4.expression.setExpression("true");
        SetVariable sendTr5Act5 =
                new SetVariable(sendTr5, "sendTr5Act5");
        sendTr5Act5.variableName.setExpression("msg");
        sendTr5Act5.expression.setExpression("msgIn_V");
        Transition sendTr6 = new Transition(send, "sendTr6");
        s1.outgoingPort.link(sendTr6);
        s1.incomingPort.link(sendTr6);
        sendTr6.setGuardExpression("!ack_S && expired_S && trying");
        BroadcastOutput sendTr6Act1 =
                new BroadcastOutput(sendTr6, "sendTr6Act1");
        sendTr6Act1.portName.setExpression("pktOut");
        sendTr6Act1.expression.setExpression("msgIn_V*2+1");
        BroadcastOutput sendTr6Act2 =
                new BroadcastOutput(sendTr6, "sendTr6Act2");
        sendTr6Act2.portName.setExpression("monitor");
        sendTr6Act2.expression.setExpression("1");
        BroadcastOutput sendTr6Act3 =
                new BroadcastOutput(sendTr6, "sendTr6Act3");
        sendTr6Act3.portName.setExpression("setTimer");
        sendTr6Act3.expression.setExpression(TIME_OUT);
        Transition sendTr7 = new Transition(send, "sendTr7");
        s1.outgoingPort.link(sendTr7);
        s1.incomingPort.link(sendTr7);
        sendTr7.setGuardExpression("(ack_S ? ack_V : 1) != 1 "
                + "&& expired_S && trying");
        BroadcastOutput sendTr7Act1 =
                new BroadcastOutput(sendTr7, "sendTr7Act1");
        sendTr7Act1.portName.setExpression("pktOut");
        sendTr7Act1.expression.setExpression("msgIn_V*2+1");
        BroadcastOutput sendTr7Act2 =
                new BroadcastOutput(sendTr7, "sendTr7Act2");
        sendTr7Act2.portName.setExpression("monitor");
        sendTr7Act2.expression.setExpression("1");
        BroadcastOutput sendTr7Act3 =
                new BroadcastOutput(sendTr7, "sendTr7Act3");
        sendTr7Act3.portName.setExpression("setTimer");
        sendTr7Act3.expression.setExpression(TIME_OUT);
        Transition sendTr8 = new Transition(send, "sendTr8");
        s1.outgoingPort.link(sendTr8);
        s0.incomingPort.link(sendTr8);
        sendTr8.setGuardExpression("(ack_S ? ack_V : -1) == 1 "
                + "&& trying");
        BroadcastOutput sendTr8Act1 =
                new BroadcastOutput(sendTr8, "sendTr8Act1");
        sendTr8Act1.portName.setExpression("setTimer");
        sendTr8Act1.expression.setExpression(RESET);
        BroadcastOutput sendTr8Act2 =
                new BroadcastOutput(sendTr8, "sendTr8Act2");
        sendTr8Act2.portName.setExpression("next");
        sendTr8Act2.expression.setExpression("true");
        SetVariable sendTr8Act3 =
                new SetVariable(sendTr8, "sendTr8Act3");
        sendTr8Act3.variableName.setExpression("trying");
        sendTr8Act3.expression.setExpression("false");
        // create the local variables
        Variable sendFlag = new Variable(send, "trying");
        sendFlag.setTypeEquals(BaseType.BOOLEAN);
        sendFlag.setToken(BooleanToken.FALSE);
        Variable msgCount = new Variable(send, "msg");
        msgCount.setTypeEquals(BaseType.INT);
        msgCount.setToken(new IntToken(0));
        // set to be ctrlSending's refinement
        ctrlSending.refinementName.setExpression("Send");
        
        // connect sender's components
        TypedIORelation sdrR1 = (TypedIORelation)sender.newRelation("request");
        sdrRequest.link(sdrR1);
        conRequest.link(sdrR1);
        TypedIORelation sdrR2 = (TypedIORelation)sender.newRelation("setTimer");
        sdrSetTimer.link(sdrR2);
        conSetTimer.link(sdrR2);
        sendSetTimer.link(sdrR2);
        TypedIORelation sdrR3 = (TypedIORelation)sender.newRelation("ack");
        sdrAck.link(sdrR3);
        conAck.link(sdrR3);
        sendAck.link(sdrR3);
        TypedIORelation sdrR4 = (TypedIORelation)sender.newRelation("pktOut");
        sdrPktOut.link(sdrR4);
        conPktOut.link(sdrR4);
        sendPktOut.link(sdrR4);
        TypedIORelation sdrR5 = (TypedIORelation)sender.newRelation("expired");
        sdrExpired.link(sdrR5);
        conExpired.link(sdrR5);
        sendExpired.link(sdrR5);
        TypedIORelation sdrR6 = (TypedIORelation)sender.newRelation("next");
        sdrNext.link(sdrR6);
        conNext.link(sdrR6);
        sendNext.link(sdrR6);
        ctrlNext.link(sdrR6);
        TypedIORelation sdrR7 = (TypedIORelation)sender.newRelation("msgIn");
        sdrMsgIn.link(sdrR7);
        sendMsgIn.link(sdrR7);
        TypedIORelation sdrR8 = (TypedIORelation)sender.newRelation("monitor");
        sdrMonitor.link(sdrR8);
        sendMonitor.link(sdrR8);
        TypedIORelation sdrR9 = (TypedIORelation)sender.newRelation("error");
        conError.link(sdrR9);
        ctrlError.link(sdrR9);
        
        // the receiver FSM
        FSMActor receiver = new FSMActor(this, "Receiver");
        // ports
        TypedIOPort recPktIn = (TypedIOPort)receiver.newPort("pktIn");
        recPktIn.setInput(true);
        recPktIn.setTypeEquals(BaseType.INT);
        TypedIOPort recAck = (TypedIOPort)receiver.newPort("ack");
        recAck.setOutput(true);
        recAck.setTypeEquals(BaseType.INT);
        TypedIOPort recMsgOut = (TypedIOPort)receiver.newPort("msgOut");
        recMsgOut.setOutput(true);
        recMsgOut.setTypeEquals(BaseType.INT);
        // states and transitions
        State recInit = new State(receiver, "Init");
        State recS0 = new State(receiver, "S0");
        State recS1 = new State(receiver, "S1");
        receiver.initialStateName.setExpression("Init");
        Transition recTr1 = new Transition(receiver, "recTr1");
        recInit.outgoingPort.link(recTr1);
        recInit.incomingPort.link(recTr1);
        recTr1.setGuardExpression("(pktIn_S ? pktIn_V : 0) == -1");
        BroadcastOutput recTr1Act1 = new BroadcastOutput(recTr1, "recTr1Act1");
        recTr1Act1.portName.setExpression("ack");
        recTr1Act1.expression.setExpression("-1");
        Transition recTr2 = new Transition(receiver, "recTr2");
        recInit.outgoingPort.link(recTr2);
        recS1.incomingPort.link(recTr2);
        recTr2.setGuardExpression("(pktIn_S ? pktIn_V%2 : 1) == 0");
        BroadcastOutput recTr2Act1 = new BroadcastOutput(recTr2, "recTr2Act1");
        recTr2Act1.portName.setExpression("ack");
        recTr2Act1.expression.setExpression("0");
        BroadcastOutput recTr2Act2 =
        new BroadcastOutput(recTr2, "recTr2Act2");
        recTr2Act2.portName.setExpression("msgOut");
        recTr2Act2.expression.setExpression("pktIn_V/2");
        Transition recTr3 = new Transition(receiver, "recTr3");
        recS1.outgoingPort.link(recTr3);
        recS1.incomingPort.link(recTr3);
        recTr3.setGuardExpression("(pktIn_S ? pktIn_V%2 : 1) == 0");
        BroadcastOutput recTr3Act1 = new BroadcastOutput(recTr3, "recTr3Act1");
        recTr3Act1.portName.setExpression("ack");
        recTr3Act1.expression.setExpression("0");
        Transition recTr4 = new Transition(receiver, "recTr4");
        recS1.outgoingPort.link(recTr4);
        recS0.incomingPort.link(recTr4);
        recTr4.setGuardExpression("(pktIn_S ? pktIn_V%2 : 0) == 1");
        BroadcastOutput recTr4Act1 = new BroadcastOutput(recTr4, "recTr4Act1");
        recTr4Act1.portName.setExpression("ack");
        recTr4Act1.expression.setExpression("1");
        BroadcastOutput recTr4Act2 = new BroadcastOutput(recTr4, "recTr4Act2");
        recTr4Act2.portName.setExpression("msgOut");
        recTr4Act2.expression.setExpression("pktIn_V/2");
        Transition recTr5 = new Transition(receiver, "recTr5");
        recS0.outgoingPort.link(recTr5);
        recS0.incomingPort.link(recTr5);
        recTr5.setGuardExpression("(pktIn_S ? pktIn_V%2 : 0) == 1");
        BroadcastOutput recTr5Act1 = new BroadcastOutput(recTr5, "recTr5Act1");
        recTr5Act1.portName.setExpression("ack");
        recTr5Act1.expression.setExpression("1");
        Transition recTr6 = new Transition(receiver, "recTr6");
        recS0.outgoingPort.link(recTr6);
        recS1.incomingPort.link(recTr6);
        recTr6.setGuardExpression("(pktIn_S ? pktIn_V%2 : 1) == 0");
        BroadcastOutput recTr6Act1 = new BroadcastOutput(recTr6, "recTr6Act1");
        recTr6Act1.portName.setExpression("ack");
        recTr6Act1.expression.setExpression("0");
        BroadcastOutput recTr6Act2 = new BroadcastOutput(recTr6, "recTr6Act2");
        recTr6Act2.portName.setExpression("msgOut");
        recTr6Act2.expression.setExpression("pktIn_V/2");
        
        // connect the top level system
        TypedIORelation sysR1 = (TypedIORelation)this.newRelation("request");
        msgSrc.request.link(sysR1);
        sdrRequest.link(sysR1);
        TypedIORelation sysR2 = (TypedIORelation)this.newRelation("msgIn");
        msgSrc.output.link(sysR2);
        sdrMsgIn.link(sysR2);
        TypedIORelation sysR3 = (TypedIORelation)this.newRelation("pktOut");
        forward.input.link(sysR3);
        sdrPktOut.link(sysR3);
        TypedIORelation sysR4 = (TypedIORelation)this.newRelation("sdrAck");
        backward.output.link(sysR4);
        sdrAck.link(sysR4);
        TypedIORelation sysR5 = (TypedIORelation)this.newRelation("recAck");
        backward.input.link(sysR5);
        recAck.link(sysR5);
        TypedIORelation sysR6 = (TypedIORelation)this.newRelation("msgOut");
        recMsgOut.link(sysR6);
        plot.input.link(sysR6);
        TypedIORelation sysR7 = (TypedIORelation)this.newRelation("setTimer");
        timer.set.link(sysR7);
        sdrSetTimer.link(sysR7);
        TypedIORelation sysR8 = (TypedIORelation)this.newRelation("expired");
        timer.expired.link(sysR8);
        sdrExpired.link(sysR8);
        TypedIORelation sysR9 = (TypedIORelation)this.newRelation("pktIn");
        forward.output.link(sysR9);
        recPktIn.link(sysR9);
        TypedIORelation sysR10 = (TypedIORelation)this.newRelation("next");
        msgSrc.next.link(sysR10);
        sdrNext.link(sysR10);
        
        plot.input.link(sysR2);
        TypedIORelation sysR11 = (TypedIORelation)this.newRelation("monitor");
        sdrMonitor.link(sysR11);
        plot.input.link(sysR11);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // FIXME: Should these be parameters of the model?
    public static final String TIME_OUT = "5.0";
    public static final String RESET = "-1.0";
}
