/* Helper for the Global Data Plane (GDP) JavaScript module.

   Copyright (c) 2015-2016 The Regents of the University of California.
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

package ptolemy.actor.lib.jjs.modules.gdp;

import org.terraswarm.gdp.EP_TIME_SPEC;
import org.terraswarm.gdp.GDP_GCL;
import org.terraswarm.gdp.GDP_NAME;


import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

/** Helper for the GDP JavaScript module for use by accessors.
 *
 *  @author Nitesh Mor and Edward A. Lee, Contributor: Christopher Brooks.
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class GDPHelper {

    /** Create a GDP Helper.
     *  @param logName FIXME: What is the format?
     *  @param ioMode The i/o mode for the log (0: for internal use only, 1: read-only, 2: read-append, 3: append-only).
     */
    public GDPHelper(String logName, int ioMode) {
        _gcl = GDP_GCL.newGCL(new GDP_NAME(logName), ioMode);
        _logName = logName;    
    }

    /** Append a string to the log.
     *  @param data The string to be appended, which assumed to be UTF-8.
     */   
    public void append(String data) {
        byte [] bytes = data.getBytes(StandardCharsets.UTF_8);
        _gcl.append(bytes);
    }

    /** Get the next data.
     *  @param timeout The timeout in milliseconds.
     *  @return The next data.
     */
    public String getNextData(int timeout) {
        HashMap<String, Object> gdp_event = GDP_GCL.get_next_event(_gcl, timeout);
        return gdp_event.toString();
    }
    
    /** Read the indicated number of records.
     *  @param numberOfRecords The number of records to read.
     *  @return A string representing the records that were read or the empty
     *  string if no records were read.
     */
    public String read(long numberOfRecords) {
        HashMap<String,Object> data = _gcl.read(numberOfRecords);
        if (data != null) {
            return data.toString();
        } else {
            return "";
        }
    }

    /** Subscribe to a log.
     *  @param currentObj The handle   
     *  @param startRecord The index of the starting record.
     *  @param numberOfRecords The number of records to read.
     *  @param timeout The timeout in milliseconds.
     */
    public void subscribe(final ScriptObjectMirror currentObj, int startRecord,
            int numberOfRecords, int timeout) {
        
        EP_TIME_SPEC timeoutSpec = new EP_TIME_SPEC(timeout/1000,
                0, /* nanoseconds */
                0.001f /* accuracy in seconds */);
        // FIXME: We need to cast to a long here because it seems
        // like passing longs from JavaScript does not work for us.
        _gcl.subscribe((long)startRecord, numberOfRecords, timeoutSpec);
        Runnable blocking = new Runnable() {
            public void run() {
                while (_subscribed) {
                    // Zero arg means no timeout. Wait forever.
                    HashMap<String, Object> result = GDP_GCL.get_next_event(_gcl, 0);
                    if (result != null) {
                        currentObj.callMember("handleResponse", result.toString());
                    } else {
                        _subscribed = false;
                    }
                }
            }
        };
        Thread thread = new Thread(blocking, "GDP subscriber thread: " + _logName);
        // Start this as a deamon thread so that it doesn't block exiting the process.
        thread.setDaemon(true);
        thread.start();
    }
    
    /** Unsubscribe from a log.
     *  @param currentObj The handle   
     */
    public void unsubscribe(final ScriptObjectMirror currentObj) {
        // FIXME: Properly close the C side.
        _subscribed = false;
    }

    /** The log. */
    private GDP_GCL _gcl;

    /** The name of the log. */
    private String _logName;

    /** True if the log has been subscribed to. */
    private boolean _subscribed = false;
}
