/***declareBlock***/
typedef int IntToken;
/**/

/***funcDeclareBlock***/
Token Int_new(int i);
/**/


/***newBlock***/
// make a new integer token from the given value.
Token Int_new(int i) {
    Token result;
    result.type = TYPE_Int;
    result.payload.Int = i;
    return result;
}
/**/

/***equalsBlock***/
Token Int_equals(Token thisToken, ...) {
    va_list argp; 
    Token otherToken; 
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    va_end(argp);
    return Boolean_new(thisToken.payload.Int == otherToken.payload.Int);
}
/**/

/***isCloseToBlock***/
Token Int_isCloseTo(Token thisToken, ...) {
    va_list argp;
    Token otherToken;
    Token tolerance;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);
    tolerance = va_arg(argp, Token);

    va_end(argp);
    return Boolean_new(fabs(thisToken.payload.Int - otherToken.payload.Int) < tolerance.payload.Double);
}
/**/

/***deleteBlock***/
/* Instead of Int_delete(), we call scalarDelete(). */
/**/

/***printBlock***/
Token Int_print(Token thisToken, ...) {
    printf("%d", thisToken.payload.Int);
}
/**/

/***toStringBlock***/
Token Int_toString(Token thisToken, ...) {
    return String_new(InttoString(thisToken.payload.Int));
}
/**/

/***addBlock***/
Token Int_add(Token thisToken, ...) {
    va_list argp; 
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return Int_new(thisToken.payload.Int + otherToken.payload.Int);
}
/**/

/***subtractBlock***/
Token Int_subtract(Token thisToken, ...) {
    va_list argp; 
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);	

    va_end(argp);
    return Int_new(thisToken.payload.Int - otherToken.payload.Int);
}
/**/

/***multiplyBlock***/
Token Int_multiply(Token thisToken, ...) {
    va_list argp; 
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);	

    switch (otherToken.type) {
    case TYPE_Int:
        result = Int_new(thisToken.payload.Int * otherToken.payload.Int);
        break;
    		
#ifdef TYPE_Double
    case TYPE_Double:
        result = Double_new(thisToken.payload.Int * otherToken.payload.Double);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Int_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/***divideBlock***/
Token Int_divide(Token thisToken, ...) {
    va_list argp; 
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);	

    va_end(argp);
    return Int_new(thisToken.payload.Int / otherToken.payload.Int);
}
/**/

/***negateBlock***/
Token Int_negate(Token thisToken, ...) {
    thisToken.payload.Int = -thisToken.payload.Int;
    return thisToken;
}
/**/

/***zeroBlock***/
Token Int_zero(Token token, ...) {
    return Int_new(0);
}
/**/

/***oneBlock***/
Token Int_one(Token token, ...) {
    return Int_new(1);
}
/**/

/***cloneBlock***/
Token Int_clone(Token thisToken, ...) {
    return thisToken;
}
/**/

---------------- static functions -----------------------

/***convertBlock***/
Token Int_convert(Token token, ...) {
    switch (token.type) {

#ifdef TYPE_Double
    case TYPE_Double:
        token.payload.Int = DoubletoInt(token.payload.Double);
        break;
#endif
	
        // FIXME: not finished
    default: 
        fprintf(stderr, "Int_convert(): Conversion from an unsupported type. (%d)\n", token.type);
        break;
    }    
    token.type = TYPE_Int;
    return token;
}    
/**/

