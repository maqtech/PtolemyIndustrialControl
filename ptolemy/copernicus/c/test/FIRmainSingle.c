
/* Main program that accompanies the FIR.java test 

   @author Shuvra S. Bhattacharyya 
   @version $Id$
*/

#include "FIRsingle.h"
#include <stdio.h>

#include "FIRconfigSingle.h"

int main(void) {

    float input[10] = INPUT_VALUES;
    float output[10] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    float data[20];
    float taps[NUM_TAPS] = TAP_VALUES;
    int i;

    /* An instance of an FIR filter */
    struct i0438065929_FIRsingle fir;

    /* Configure the filter with the desired parameters */
    f0973424432_initialize(&fir, taps, NUM_TAPS, data, INTERP, DEC, DECPHASE);

    /* Execute one (SDF) invocation of the filter */
    f130289078_fire(&fir, input, output);

    /* Print the contents of the output buffer */
    for (i=0; i<10; i++) {
        printf("%f\n", output[i]);
    }

    return 0;
}
