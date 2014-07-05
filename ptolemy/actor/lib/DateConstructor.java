/* Construct date token by parsing all date elements (year, day, month, ...).

   @Copyright (c) 2008-2014 The Regents of the University of California.
   All rights reserved.

   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the
   above copyright notice and the following two paragraphs appear in all
   copies of this software.

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
package ptolemy.actor.lib;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DateToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


/**
  Construct date token by parsing all date elements (year, day, month, ...).

 @author Patricia Derler
 @version $Id: AbsoluteValue.java 65768 2013-03-07 03:33:00Z cxh $
 @since Ptolemy II 10.
 @Pt.ProposedRating Red (pd)
 @Pt.AcceptedRating Red (pd)
 */
public class DateConstructor extends TypedAtomicActor {
    
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DateConstructor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output = new TypedIOPort(this, "input", false, true);
        output.setTypeEquals(BaseType.DATE);
        
        year = new TypedIOPort(this, "year", true, false);
        year.setTypeEquals(BaseType.INT);
        
        month = new TypedIOPort(this, "month", true, false);
        month.setTypeEquals(BaseType.INT);
        
        day = new TypedIOPort(this, "day", true, false);
        day.setTypeEquals(BaseType.INT);
        
        hour = new TypedIOPort(this, "hour", true, false);
        hour.setTypeEquals(BaseType.INT);
        
        minute = new TypedIOPort(this, "minute", true, false);
        minute.setTypeEquals(BaseType.INT);
        
        second = new TypedIOPort(this, "second", true, false);
        second.setTypeEquals(BaseType.INT);
        
        millisecond = new TypedIOPort(this, "millisecond", true, false);
        millisecond.setTypeEquals(BaseType.INT);
        
        microsecond = new TypedIOPort(this, "microsecond", true, false);
        microsecond.setTypeEquals(BaseType.INT);
        
        nanosecond = new TypedIOPort(this, "nanosecond", true, false);
        nanosecond.setTypeEquals(BaseType.INT);
        
        timeZone = new TypedIOPort(this, "timeZone", true, false);
        timeZone.setTypeEquals(BaseType.STRING);
        
        timeAsLong = new TypedIOPort(this, "timeAsLong", true, false);
        timeAsLong.setTypeEquals(BaseType.LONG);
        
        precision = new StringParameter(this, "precision");
        precision.addChoice("second");
        precision.addChoice("millisecond");
        precision.addChoice("microsecond");
        precision.addChoice("nanosecond");
    }
    
    public TypedIOPort output;
    public TypedIOPort year;
    public TypedIOPort month;
    public TypedIOPort day;
    public TypedIOPort hour;
    public TypedIOPort minute;
    public TypedIOPort second;
    public TypedIOPort millisecond;
    public TypedIOPort microsecond;
    public TypedIOPort nanosecond;
    public TypedIOPort timeZone;
    public TypedIOPort timeAsLong;
    public Parameter precision;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct a date token with all tokens present. If a token
     *  for the long value is present, use this token and time zone
     *  as well as precision. Otherwise use tokens on other inputs to 
     *  create ports.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        DateToken dateToken = null;
        int datePrecision = DateToken.PRECISION_MILLISECOND;
        
        if (timeAsLong.connectedPortList().size() > 0 && timeAsLong.hasToken(0)) {
            long timeAsLongValue = 1l;
            timeAsLongValue = ((LongToken)timeAsLong.get(0)).longValue();
            String precisionValue = "second";
            precisionValue = ((StringToken)precision.getToken()).stringValue();
            switch (precisionValue) {
                case "second":
                    datePrecision = DateToken.PRECISION_SECOND; 
                    break;
                case "millisecond":
                    datePrecision = DateToken.PRECISION_MILLISECOND;
                    break;
                case "microsecond":
                    datePrecision = DateToken.PRECISION_MICROSECOND;
                    break;
                case "nanosecond":
                    datePrecision = DateToken.PRECISION_NANOSECOND;
                    break;
                default:
                    datePrecision = DateToken.PRECISION_MILLISECOND;     
                    break;
            }
            String timeZoneValue = TimeZone.getDefault().getID();
            if ((timeZone.connectedPortList().size() > 0) && timeZone.hasToken(0)) {
                timeZoneValue = ((StringToken)timeZone.get(0)).stringValue();
            }
            dateToken = new DateToken(timeAsLongValue, datePrecision, TimeZone.getTimeZone(timeZoneValue));
        } else {
            int yearValue = 1970;
            if (year.connectedPortList().size() > 0 && year.hasToken(0)) {
                yearValue = ((IntToken)year.get(0)).intValue();
            }
            int monthValue = 1;
            if (month.connectedPortList().size() > 0 && month.hasToken(0)) {
                monthValue = ((IntToken)month.get(0)).intValue();
            }
            int dayValue = 1;
            if (day.connectedPortList().size() > 0 && day.hasToken(0)) {
                dayValue = ((IntToken)day.get(0)).intValue();
            }
            int hourValue = 0;
            if (hour.connectedPortList().size() > 0 && hour.hasToken(0)) {
                hourValue = ((IntToken)hour.get(0)).intValue();
            }
            int minuteValue = 0;
            if (minute.connectedPortList().size() > 0 && minute.hasToken(0)) {
                minuteValue = ((IntToken)minute.get(0)).intValue();
            }
            int secondValue = 0;
            if (second.connectedPortList().size() > 0 && second.hasToken(0)) {
                secondValue = ((IntToken)second.get(0)).intValue();
                datePrecision = DateToken.PRECISION_SECOND;
            }
            int millisecondValue = 0;
            if (millisecond.connectedPortList().size() > 0 && millisecond.hasToken(0)) {
                millisecondValue = ((IntToken)millisecond.get(0)).intValue();
                datePrecision = DateToken.PRECISION_MILLISECOND;
            }
            int microsecondValue = 0;
            if (microsecond.connectedPortList().size() > 0 && microsecond.hasToken(0)) {
                microsecondValue = ((IntToken)microsecond.get(0)).intValue();
                datePrecision = DateToken.PRECISION_MICROSECOND;
            }
            int nanosecondValue = 0;
            if (nanosecond.connectedPortList().size() > 0 && nanosecond.hasToken(0)) {
                nanosecondValue = ((IntToken)nanosecond.get(0)).intValue();
                datePrecision = DateToken.PRECISION_NANOSECOND;
            }
            String timeZoneValue = TimeZone.getDefault().getID();
            if (timeZone.connectedPortList().size() > 0 && timeZone.hasToken(0)) {
                timeZoneValue = ((StringToken)timeZone.get(0)).stringValue();
            }
            dateToken = new DateToken();
            dateToken.getCalendarInstance().setTimeZone(TimeZone.getTimeZone(timeZoneValue));
            dateToken.getCalendarInstance().set(Calendar.YEAR, yearValue);
            dateToken.getCalendarInstance().set(Calendar.MONTH, monthValue);
            dateToken.getCalendarInstance().set(Calendar.DAY_OF_MONTH, dayValue);
            dateToken.getCalendarInstance().set(Calendar.HOUR, hourValue);
            dateToken.getCalendarInstance().set(Calendar.MINUTE, minuteValue);
            dateToken.getCalendarInstance().set(Calendar.SECOND, secondValue);
            dateToken.getCalendarInstance().set(Calendar.MILLISECOND, millisecondValue);
            dateToken.addMicroseconds(microsecondValue);
            dateToken.addNanoseconds(nanosecondValue);
        }
        
        
        output.send(0, dateToken);
    }
}
