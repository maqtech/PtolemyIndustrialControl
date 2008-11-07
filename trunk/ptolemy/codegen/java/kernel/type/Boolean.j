/***declareBlock***/
/**/

/***funcDeclareBlock***/
/**/

/***Boolean_new***/
// make a new integer token from the given value.
Token Boolean_new(boolean b) {
    Token result = new Token();
    result.type = TYPE_Boolean;
    result.payload = Boolean.valueOf(b);
    return result;
}
/**/

/***Boolean_delete***/
/* Instead of Boolean_delete(), we call scalarDelete(). */
/**/

/***Boolean_equals***/
Token Boolean_equals(Token thisToken, Token... tokens) {
    Token otherToken;
    otherToken = tokens[0];

    return Boolean_new(
            ( (Boolean)thisToken.payload && (Boolean)otherToken.payload ) ||
            ( !(Boolean)thisToken.payload && !(Boolean)otherToken.payload ));
}
/**/


/***Boolean_isCloseTo***/
// No need to use Boolean_isCloseTo(), we use Boolean_equals() instead.
/**/

/***Boolean_print***/
Token Boolean_print(Token thisToken, ...) {
    printf((thisToken.payload.Boolean) ? "true" : "false");
}
/**/

/***Boolean_toString***/
Token Boolean_toString(Token thisToken, Token... ignored) {
    return String_new(BooleantoString(thisToken.payload.Boolean));
}
/**/

/***Boolean_add***/
Token Boolean_add(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Boolean_new(thisToken.payload.Boolean || otherToken.payload.Boolean);
}
/**/

/***Boolean_subtract***/
/** Boolean_subtract is not supported. */
/**/

/***Boolean_multiply***/
/** Boolean_multiply is not supported. */
/**/

/***Boolean_divide***/
/** Boolean_divide is not supported. */
/**/

/***Boolean_negate***/
Token Boolean_negate(Token thisToken, ...) {
    thisToken.payload.Boolean = !thisToken.payload.Boolean;
    return thisToken;
}
/**/

/***Boolean_zero***/
Token Boolean_zero(Token token, ...) {
    return Boolean_new(false);
}
/**/

/***Boolean_one***/
Token Boolean_one(Token token, ...) {
    return Boolean_new(true);
}
/**/


/***Boolean_clone***/
Token Boolean_clone(Token thisToken, ...) {
    return thisToken;
}
/**/


--------------------- static functions ------------------------------
/***Boolean_convert***/
Token Boolean_convert(Token token, Token... tokens) {
    switch (token.type) {
        // FIXME: not finished
    default:
        System.err.printf( "Boolean_convert(): Conversion from an unsupported type. (%d)", token.type);
        break;
    }
    token.type = TYPE_Boolean;
    return token;
}
/**/

