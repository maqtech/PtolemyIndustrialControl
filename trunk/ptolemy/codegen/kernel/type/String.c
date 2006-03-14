/***declareBlock***/
typedef char* StringToken;
/**/

/***funcDeclareBlock***/
Token String_convert(Token token);
Token String_print(Token thisToken);
/**/

/***newBlock***/
// make a new integer token from the given value.
Token String_new(char* s) {
    //Token *result = (Token *) malloc(sizeof(Token));
    Token result;
    result.type = TYPE_String;
    result.payload.String = s;
    return result;
}
/**/


/***deleteBlock***/
Token String_delete(Token token) {   
    free(token.payload.String);    
    free(&token);
}    
/**/

/***convertBlock***/
Token String_convert(Token token) {
    char* stringPointer;
    switch (token.type) {
        #ifdef TYPE_String
            case TYPE_Int:
                stringPointer = (char*) malloc(sizeof(char) * 12);
                sprintf(stringPointer, "%d", token.payload.Int);
                token.payload.String = stringPointer;
                break;
        #endif

        #ifdef TYPE_String
            case TYPE_Double:
                stringPointer = (char*) malloc(sizeof(char) * 12);
                sprintf(stringPointer, "%g", token.payload.Double);
                token.payload.String = stringPointer;
                break;
        #endif

        default:
            // FIXME: not finished
            fprintf(stderr, "Convertion from a not supported type.");
            break;
    }
    return token;
}    
/**/

/***printBlock***/
Token String_print(Token thisToken) {
    printf("\"%s\"", thisToken.payload.String);
}
/**/
