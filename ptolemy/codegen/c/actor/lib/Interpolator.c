/***hermiteBlock***/
    double $actorClass(_hermite)(int index, int iStart, double vStart, double tanStart, int iEnd, double vEnd, double tanEnd) {
        // forming the Hermite matrix M
        double M[4][4];
        double Gh[4];
        double coef[4];
        double indexSqr;
        int i;
        
        double iStartSqr = iStart * iStart;
        double iEndSqr = iEnd * iEnd;
        M[0][0] = iStartSqr * iStart;
        M[0][1] = iStartSqr;
        M[0][2] = iStart;
        M[0][3] = 1;

        M[1][0] = iEndSqr * iEnd;
        M[1][1] = iEndSqr;
        M[1][2] = iEnd;
        M[1][3] = 1;

        M[2][0] = 3 * iStartSqr;
        M[2][1] = 2 * iStart;
        M[2][2] = 1;
        M[2][3] = 0;

        M[3][0] = 3 * iEndSqr;
        M[3][1] = 2 * iEnd;
        M[3][2] = 1;
        M[3][3] = 0;

        //double[][] MInverse = DoubleMatrixMath.inverse(M);

        // forming the column vector of values and tangents
        Gh[0] = vStart;
        Gh[1] = vEnd;
        Gh[2] = tanStart;
        Gh[3] = tanEnd;

        // compute the coefficients vector coef[a, b, c, d] or the 3rd order
        // curve.
        //coef = DoubleMatrixMath.multiply(Gh, MInverse);
        for (i = 0; i < 4; i++) {
            //coef[i] = (Gh[0] * M[0][i]) + (Gh[1] * M[1][i]) + (Gh[2] * M[2][i]) + (Gh[3] * M[3][i]);
            coef[i] = (Gh[0] * M[i][0]) + (Gh[1] * M[i][1]) + (Gh[2] * M[i][2]) + (Gh[3] * M[i][3]);
        }

        // compute the interpolated value
        indexSqr = index * index;
        return (coef[0] * indexSqr * index) + (coef[1] * indexSqr) + (coef[2] * index) + coef[3];
    }
/**/

/***sharedInterpolateBlock***/
    $this.hermiteBlock()
    
    inline double $actorClass(interpolate)(int index, Token indexes, Token values, int period, int order, int numRefPoints, int largestIndex) {
        //int numRefPoints = indexes.length;
        //int largestIndex = Array_get(indexes, numRefPoints - 1);
        int i;
        int indexIndexStart;
        
        if ((index < 0) || (index > largestIndex)) {
            if (period == 0) {
                return 0.0;
            } else {
                // convert index to a value within [0, period-1]
                if (index < 0) {
                    index += (((-index / period) + 1) * period);
                }

                index %= period;
            }
        }

        // index is now within [0, period-1]. If it is outside the range of
        // the smallest and the largest index, values must be periodic.
        // Handle a special case where the number of reference points is
        // 1. The code for order 3 later won't work for this case.
        if (numRefPoints == 1) {
            return Array_get(values, 0).payload.Double;
        }

        // indexIndexStart is the index to indexes whose entry is the
        // index to the left of the interpolation point.
        indexIndexStart = -1;

        // search though all indexes to find iStart.
        for (i = 0; i < numRefPoints; i++) {
            if (Array_get(indexes, i).payload.Int == index) {
                return Array_get(values, i).payload.Double;
            } else if (Array_get(indexes, i).payload.Int < index) {
                indexIndexStart = i;
            } else {
                break;
            }
        }

        // Perform interpolation
        if (order == 0) {
            if (indexIndexStart != -1) {
                return Array_get(values, indexIndexStart).payload.Double;
            } else {
                return Array_get(values, numRefPoints - 1).payload.Double;
            }
        }

        // order must be 1 or 3, need at least the two points surrounding
        // the interpolation point.
        int iStart;

        // order must be 1 or 3, need at least the two points surrounding
        // the interpolation point.
        int iEnd;
        double vStart;
        double vEnd;

        if (indexIndexStart == -1) {
            iStart = Array_get(indexes, numRefPoints - 1).payload.Int - period;
            vStart = Array_get(values, numRefPoints - 1).payload.Double;
        } else {
            iStart = Array_get(indexes, indexIndexStart).payload.Int;
            vStart = Array_get(values, indexIndexStart).payload.Double;
        }

        if (indexIndexStart == (numRefPoints - 1)) {
            iEnd = Array_get(indexes, 0).payload.Int + period;
            vEnd = Array_get(values, 0).payload.Double;
        } else {
            iEnd = Array_get(indexes, indexIndexStart + 1).payload.Int;
            vEnd = Array_get(values, indexIndexStart + 1).payload.Double;
        }

        if (order == 1) {
            return vStart + (((index - iStart) * (vEnd - vStart)) / (iEnd - iStart));
        }

        // order is 3. Need the points before Start and the point after End
        // to compute the tangent at Start and End.
        int iBeforeStart;

        // order is 3. Need the points before Start and the point after End
        // to compute the tangent at Start and End.
        int iAfterEnd;
        double vBeforeStart;
        double vAfterEnd;

        if (indexIndexStart == -1) {
            iBeforeStart = Array_get(indexes, numRefPoints - 2).payload.Int - period;
            vBeforeStart = Array_get(values, numRefPoints - 2).payload.Double;
        } else if (indexIndexStart == 0) {
            if (period > 0) {
                iBeforeStart = Array_get(indexes, numRefPoints - 1).payload.Int - period;
                vBeforeStart = Array_get(values, numRefPoints - 1).payload.Double;
            } else {
                // Not periodic
                iBeforeStart = Array_get(indexes, 0).payload.Int - 1;
                vBeforeStart = 0.0;
            }
        } else {
            iBeforeStart = Array_get(indexes, indexIndexStart - 1).payload.Int;
            vBeforeStart = Array_get(values, indexIndexStart - 1).payload.Double;
        }

        if (indexIndexStart == (numRefPoints - 1)) {
            iAfterEnd = Array_get(indexes, 1).payload.Int + period;
            vAfterEnd = Array_get(values, 1).payload.Double;
        } else if (indexIndexStart == (numRefPoints - 2)) {
            if (period > 0) {
                iAfterEnd = Array_get(indexes, 0).payload.Int + period;
                vAfterEnd = Array_get(values, 0).payload.Double;
            } else {
                // Not periodic
                iAfterEnd = Array_get(indexes, numRefPoints - 1).payload.Int + 1;
                vAfterEnd = 0.0;
            }
        } else {
            iAfterEnd = Array_get(indexes, indexIndexStart + 2).payload.Int;
            vAfterEnd = Array_get(values, indexIndexStart + 2).payload.Double;
        }

        // computer the tangent at Start and End.
        double tanBefore2Start = (vStart - vBeforeStart) / (iStart - iBeforeStart);
        double tanStart2End = (vEnd - vStart) / (iEnd - iStart);
        double tanEnd2After = (vAfterEnd - vEnd) / (iAfterEnd - iEnd);

        double tanStart = 0.5 * (tanBefore2Start + tanStart2End);
        double tanEnd = 0.5 * (tanStart2End + tanEnd2After);

        return $actorClass(_hermite)(index, iStart, vStart, tanStart, iEnd, vEnd, tanEnd);
    }
/**/


/*** preinitBlock ***/
    int $actorSymbol(_iterationCount) = 0;
/**/

/*** initBlock ***/
/**/

/*** fireBlock($numRefPoints, $largestIndex) ***/
    $ref(output) = $actorClass(interpolate)($actorSymbol(_iterationCount), $ref(indexes), $ref(values), $val(period), $val(order), $numRefPoints, $largestIndex);
/**/

/***postfireBlock***/
    $actorSymbol(_iterationCount)++;
/**/