/***constantsBlock***/
#define MISSING 0
#define boolean unsigned char
#define NaN nanf(0)
#define false 0
#define true 1
/**/

/***funcHeaderBlock ($function)***/
Token $function (Token this, ...);
/**/

/***tokenDeclareBlock ($types)***/
struct token {         // Base type for tokens.
    char type;         // TYPE field has to be the first field.
    union typeMembers {
        // type member declarations [i.e. Type1Token Type1;]
        $types                 
    } payload;
};
/**/


/***convertPrimitivesBlock***/
#define StringtoInt atoi
#define StringtoDouble atof
#define StringtoLong atol
#define DoubletoInt floor
#define InttoDouble (double)

char* InttoString (int i) {
    char* string = (char*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%d", i);
    return string;       
}

char* LongtoString (long long l) {
    char* string = (char*) malloc(sizeof(char) * 22);
    sprintf(string, "%lld", l);
    return string;       
}

char* DoubletoString (double d) {
    int index;
    char* string = (char*) malloc(sizeof(char) * 20);
    sprintf(string, "%.14g", d);

	// Make sure that there is a decimal point.
    if (strrchr(string, '.') == NULL) {
        index = strlen(string);
        if (index == 20) {
            string = (char*) realloc(string, sizeof(char) * 22);
        }
        string[index] = '.';
        string[index + 1] = '0';
        string[index + 2] = '\0';
    }
    return string;       
}

char* BooleantoString (boolean b) {
#ifdef PT_NO_STRDUP
    return (b) ? strcpy(malloc(5),"true") : strcpy(malloc(6),"false");
#else
    return (b) ? strdup("true") : strdup("false");
#endif
}

/**/
