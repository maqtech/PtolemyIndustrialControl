ptolemy/matlab/README.txt
Version: $Id$

This directory contains the Ptolemy II Matlab Interface for Windows
written by Zoltan Kemenczy of Research in Motion Limited.

Christopher Hylands wrote the notes below, so any mistakes are his.

Below are instructions for building the interface at UC Berkeley:

1. Go to
   http://sources.redhat.com/cygwin/
   and install the Cygwin Setup tool

2. Start up the Cygwin Setup tool and install
    binutils:	Needed for dlltool
    gcc:	Needed for gcc
    w32api:	Needed for the user32 library

3. Install or mount the Matlab share.
   In the UC Berkeley EECS Department, the Matlab binary is at
   available as \\winsww\public\matlab\matlab-6.0\bin\win32\Matlab.exe
   To mount this, bring up the Run window with:
      Start -> Run 
   Type in \\winsww
   Right click on sww, select 'Map Network Drive'
   Click Finish

   For me, this ended up on the e: drive

4. Add the Matlab directory to your path.  You can either add it
   permanently to you path, or add it temporarily.
   If you are on a laptop, and the Matlab installation is a remote
   file system, you should mount it temporarily:

   bash-2.05$ PATH=/cygdrive/e/public/matlab/matlab-6.0/bin/win32:$PATH

5. Verify that matlab is in your path
   bash-2.05$ type matlab
   matlab is /cygdrive/e/public/matlab/matlab-6.0/bin/win32/matlab

6. Reconfigure Ptolemy II so that it knows that you now have matlab:
   cd $PTII
   rm config.*
   ./configure

7. Build the Matlab interface
   cd $PTII/ptolemy/matlab
   make

8. Run a test:
   vergil "$PTII/ptolemy/matlab/test/TestExpression.xml" 
   Note that during the preinitialize phase there may be a delay while
   the Matlab engine starts up over the network.
