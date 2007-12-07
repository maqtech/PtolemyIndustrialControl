/***declareBlock***/
typedef double DoubleToken;
/**/

/***funcDeclareBlock***/
Token Double_new(double d);
/**/

/***Double_new***/
// make a new integer token from the given value.
Token Double_new(double d) {
    Token result;
    result.type = TYPE_Double;
    result.payload.Double = d;
    return result;
}
/**/

/***Double_delete***/
/* Instead of Double_delete(), we call scalarDelete(). */
/**/

/***Double_equals***/
Token Double_equals(Token thisToken, ...) {
    va_list argp;
    Token otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    if (otherToken.type != TYPE_Double) {
        otherToken = Double_convert(otherToken);
    }

    va_end(argp);

    // Give tolerance for testing.
    return Boolean_new(1.0E-6 > thisToken.payload.Double - otherToken.payload.Double);
}
/**/

/***Double_isCloseTo***/
Token Double_isCloseTo(Token thisToken, ...) {
    va_list argp;
    Token otherToken;
    Token tolerance;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);
    tolerance = va_arg(argp, Token);

    va_end(argp);
    return Boolean_new(fabs(thisToken.payload.Double - otherToken.payload.Double) < tolerance.payload.Double);
}
/**/

/***Double_print***/
Token Double_print(Token thisToken, ...) {
    printf("%g", thisToken.payload.Double);
}
/**/

/***Double_toString***/
Token Double_toString(Token thisToken, ...) {
    return String_new(DoubletoString(thisToken.payload.Double));
}
/**/

/***Double_add***/
Token Double_add(Token thisToken, ...) {
    va_list argp;
    Token otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    va_end(argp);
    return Double_new(thisToken.payload.Double + otherToken.payload.Double);
}
/**/

/***Double_subtract***/
Token Double_subtract(Token thisToken, ...) {
    va_list argp;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    va_end(argp);
    return Double_new(thisToken.payload.Double - otherToken.payload.Double);
}
/**/

/***Double_multiply***/
Token Double_multiply(Token thisToken, ...) {
    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    switch (otherToken.type) {
    case TYPE_Double:
        result = Double_new(thisToken.payload.Double * otherToken.payload.Double);
        break;
#ifdef TYPE_Int
    case TYPE_Int:
        result = Double_new(thisToken.payload.Double * otherToken.payload.Int);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Double_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/***Double_divide***/
Token Double_divide(Token thisToken, ...) {
    va_list argp;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    va_end(argp);
    return Double_new(thisToken.payload.Double / otherToken.payload.Double);
}
/**/

/***Double_negate***/
Token Double_negate(Token thisToken, ...) {
    thisToken.payload.Double = -thisToken.payload.Double;
    return thisToken;
}
/**/

/***Double_zero***/
Token Double_zero(Token token, ...) {
    return Double_new(0.0);
}
/**/

/***Double_one***/
Token Double_one(Token token, ...) {
    return Double_new(1.0);
}
/**/


/***Double_clone***/
Token Double_clone(Token thisToken, ...) {
    return thisToken;
}
/**/




--------------------- static functions --------------------------
/***Double_convert***/
Token Double_convert(Token token, ...) {
    switch (token.type) {
#ifdef TYPE_String
    case TYPE_String:
        // FIXME: Is this safe?
        token.type = TYPE_Double;
        if (sscanf(token.payload.String, "%lg", &token.payload.Double) != 1) {
            fprintf(stderr, "Double_convert(): failed to convert \"%s\" to a Double\n", token.payload.String);
            exit(-1);
        }
        break;
#endif
#ifdef TYPE_Int
    case TYPE_Int:
        token.type = TYPE_Double;
        token.payload.Double = InttoDouble(token.payload.Int);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Double_convert(): Conversion from an unsupported type. (%d)\n", token.type);
        exit(-1);
        break;
    }
    token.type = TYPE_Double;
    return token;
}
/**/

