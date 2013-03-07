#ifndef fmiPlatformTypes_h
#define fmiPlatformTypes_h

/* Standard header file to define the argument types of the
   functions of the Functional Mock-up Interface 2.0.
   This header file must be utilized both by the model and
   by the simulation engine.

   Revisions:
   - Nov.  14, 2011: Use the header file "fmiPlatformTypes.h" for FMI 2.0
                     both for "FMI for model exchange" and for "FMI for co-simulation"
                     New types "fmiComponentEnvironment", "fmiState", and "fmiByte".
                     The implementation of "fmiBoolean" is change from "char" to "int".
                     The #define "fmiPlatform" changed to "fmiTypesPlatform"
                     (in order that #define and function call are consistent)
   - Oct.   4, 2010: Renamed header file from "fmiModelTypes.h" to fmiPlatformTypes.h"
                     for the co-simulation interface
   - Jan.   4, 2010: Renamed meModelTypes_h to fmiModelTypes_h (by Mauss, QTronic)
   - Dec.  21, 2009: Changed "me" to "fmi" and "meModel" to "fmiComponent"
                     according to meeting on Dec. 18 (by Martin Otter, DLR)
   - Dec.   6, 2009: Added meUndefinedValueReference (by Martin Otter, DLR)
   - Sept.  9, 2009: Changes according to FMI-meeting on July 21:
                     Changed "version" to "platform", "standard" to "standard32",
                     Added a precise definition of "standard32" as comment
                     (by Martin Otter, DLR)
   - July  19, 2009: Added "me" as prefix to file names, added meTrue/meFalse,
                     and changed meValueReferenced from int to unsigned int
                     (by Martin Otter, DLR).
   - March  2, 2009: Moved enums and function pointer definitions to
                     ModelFunctions.h (by Martin Otter, DLR).
   - Dec.  3, 2008 : First version by Martin Otter (DLR) and
                     Hans Olsson (Dynasim).


   Copyright � 2008-2011, MODELISAR consortium. All rights reserved.
   This file is licensed by the copyright holders under the BSD License
   (http://www.opensource.org/licenses/bsd-license.html):

   ----------------------------------------------------------------------------
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are met:

   - Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.
   - Redistributions in binary form must reproduce the above copyright notice,
     this list of conditions and the following disclaimer in the documentation
     and/or other materials provided with the distribution.
   - Neither the name of the copyright holders nor the names of its
     contributors may be used to endorse or promote products derived
     from this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
   TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
   ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
   ----------------------------------------------------------------------------

   with the extension:

   You may distribute or publicly perform any modification only under the
   terms of this license.
   (Note, this means that if you distribute a modified file,
    the modified file must also be provided under this license).
*/

/* Platform (combination of machine, compiler, operating system) */
#define fmiTypesPlatform "standard32"

/* Type definitions of variables passed as arguments
   Version "standard32" means:

   fmiComponent           : 32 bit pointer
   fmiComponentEnvironment: 32 bit pointer
   fmiFMUstate            : 32 bit pointer
   fmiValueReference      : 32 bit
   fmiReal                : 64 bit
   fmiInteger             : 32 bit
   fmiBoolean             : 32 bit
   fmiString              : 32 bit pointer
   fmiByte                :  8 bit
*/
   typedef void*        fmiComponent;               /* Pointer to FMU instance       */
   typedef void*        fmiComponentEnvironment;    /* Pointer to FMU environment    */
   typedef void*        fmiFMUstate;                /* Pointer to internal FMU state */
   typedef unsigned int fmiValueReference;
   typedef double       fmiReal   ;
   typedef int          fmiInteger;
   typedef int          fmiBoolean;
   typedef const char*  fmiString ;
   typedef char         fmiByte   ;

/* Values for fmiBoolean  */
#define fmiTrue  1
#define fmiFalse 0

/* Undefined value for fmiValueReference (largest unsigned int value) */
#define fmiUndefinedValueReference ((fmiValueReference) (-1))

#endif
