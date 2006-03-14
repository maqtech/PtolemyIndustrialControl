/***declareBlock***/
typedef int IntToken;
/**/

/***funcDeclareBlock***/
Token Int_convert(Token token);
Token Int_print(Token thisToken);
/**/


/***newBlock***/
// make a new integer token from the given value.
Token Int_new(int i) {
    //Token *result = (Token *) malloc(sizeof(Token));
    Token result;
    result.type = TYPE_Int;
    result.payload.Int = i;
    return result;
}
/**/


/***deleteBlock***/
Token Int_delete(Token token) {   
    free(&token);
}    
/**/


/***convertBlock***/
Token Int_convert(Token token) {
    switch (token.type) {
        #ifdef TYPE_Double
            case TYPE_Double:
                token.payload.Int = floor(token.payload.Double);
                break;
        #endif
        
        // FIXME: not finished
        default:
            fprintf(stderr, "Convertion from a not supported type.");
            break;
    }    
    token.type = TYPE_Int;
    return token;
}    
/**/

/***printBlock***/
Token Int_print(Token thisToken) {
    printf("%d", thisToken.payload.Int);
}
/**/
