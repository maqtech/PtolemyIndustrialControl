/***declareBlock***/
/**/

/***funcDeclareBlock***/
/**/

/***String_new***/
/* Make a new integer token from the given value. */
Token String_new(String s) {
    Token result = new Token();;
    result.type = TYPE_String;
    result.payload = new String(s);
    return result;
}
/**/

/***String_delete***/
Token String_delete(Token token, Token... ignored) {
    //free(token.payload.String);
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return emptyToken;
}
/**/

/***String_equals***/
Token String_equals(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Boolean_new(((String)(thisToken.payload)).equals((String)(otherToken.payload)));
}
/**/

/***String_isCloseTo***/
/* No need to use String_isCloseTo(), we use String_equals() instead. */
}
/**/

/***String_print***/
Token String_print(Token thisToken, Token... tokens) {
    System.out.println((String)(thisToken.payload));
    return emptyToken;
}
/**/

/***String_toString***/
Token String_toString(Token thisToken, Token... ignored) {
    return String_new((String)(thisToken.payload));
}
/**/

/***String_add***/
Token String_add(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];

    return thisToken + otherToken;
}
/**/

/***String_subtract***/
/** String_subtract is not supported. */
/**/

/***String_multiply***/
/** String_multiply is not supported. */
/**/

/***String_divide***/
/** String_divide is not supported. */
/**/

/***String_neg1ate***/
Token String_negate(Token thisToken, Token... tokens) {
    return emptyToken;
}
/**/

/***String_zero***/
Token String_zero(Token token, Token... tokens) {
    return String_new("");
}
/**/

/***String_one***/
/** String_one is not supported. */
/**/

/***String_clone***/
Token String_clone(Token thisToken, Token... tokens) {
    return String_new((String)(thisToken.payload));
}
/**/



------------------ static functions --------------------------------------

/***String_convert***/
Token String_convert(Token token, Token... ignored) {
    token.type = TYPE_String;
    switch (token.type) {
#ifdef PTCG_TYPE_Boolean
    case TYPE_Boolean:
        token.payload = BooleantoString((Boolean)(token.payload));
        return token;
#endif

#ifdef PTCG_TYPE_Integer
    case TYPE_Integer:
        token.payload = IntegertoString((Integer(token.payload));
        return token;
#endif

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        token.payload = DoubletoString((Double)(token.payload));
        return token;
#endif
    default:
        throw new RuntimeException("String_convert(): Conversion from an unsupported type: "
	 + token.type);
    }

}
/**/

