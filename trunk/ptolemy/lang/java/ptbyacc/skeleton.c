#include <stdio.h>

#include <string.h>

#include "defs.h"



/*  The banner used here should be replaced with an #ident directive	*/

/*  if the target C compiler supports #ident directives.		*/

/*									*/

/*  If the skeleton is changed, the banner should be changed so that	*/

/*  the altered version can easily be distinguished from the original.	*/



char *banner[] =

{

    "#ifndef lint",

    "static char yysccsid[] = \"@(#)yaccpar	1.8 (Berkeley) 01/20/90\";",

    "#endif",

    "#define YYBYACC 1",

    0

};



char *jbanner[] =

{

    "//### This file created by BYACC 1.8(/Java extension  0.92)",

    "//### Java capabilities added 7 Jan 97, Bob Jamison",

    "//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten",

    "//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor",

    "//###           01 Jun 99  -- Bob Jamison -- added Runnable support",

    "//### Please send bug reports to rjamison@lincom-asg.com",

    "//### static char yysccsid[] = \"@(#)yaccpar	1.8 (Berkeley) 01/20/90\";",

    "//### Based on skeleton.c: $Id$",

    "//###",

    "//### This version of BYACC/Java has been modified by Jeff Tsay and ",

    "//### Christopher Hylands with the following changes:",

    "//### 1) Parser values need to be cloned. Added a clone() method to the ",

    "//###    parser value class, and added a call the clone() method in the",

    "//###    parser class.",

    "//### 2) Parser tables are written to ASCII .tbl files instead of inlined ",

    "//###    in the parser class code. The reason for this is the 64KB limit",

    "//###    on the size of .class files in Java. If .tbl files exist, the ",

    "//###    generated parser reads them and writes platform-independent ",

    "//###    binary .bin files, if the previous .bin files are older than",

    "//###    the .tbl files or no .bin files are found.",

    "//### 3) Added the -p option to set the package of the parser and parser",

    "//###    value classes. Example: ",

    "//### ",

    "//###    ./ptbyacc -j -p ptolemy.lang.java -f JavaParser jparser.y",

    "//###  ",

    "//###    where jparser.y is the definition file.",

    "//### ",

    "//### 4) If yydebug is set and yyrule.tbl is not present then",

    "//###    you will get a NullPointerException because a debug() call",

    "//###    dereferences yyrule[], which is null. (cxh)",

    "//### 5) New instances of parserval are created for the return value of",

    "//###    each rule, so that assignments to the fields of parserval do",

    "//###    not corrupt previous data.",

    "//### 6) To find the table files, we use the getSystemResource()",

    "//###    facility to look in ptolemy/lang/java",

    "//### Bob Jamison's version 0.93 fixes nil definitions, but the source",

    "//### code was unavailable.",

    "\n\n",

    0

};





char *tables[] =

{

    "extern short yylhs[];",

    "extern short yylen[];",

    "extern short yydefred[];",

    "extern short yydgoto[];",

    "extern short yysindex[];",

    "extern short yyrindex[];",

    "extern short yygindex[];",

    "extern short yytable[];",

    "extern short yycheck[];",

    "#if YYDEBUG",

    "extern char *yyname[];",

    "extern char *yyrule[];",

    "#endif",

    0

};



char *jtables[] =

{

    "extern short yylhs[];",

    0

};





char *header[] =

{

    "#define yyclearin (yychar=(-1))",

    "#define yyerrok (yyerrflag=0)",

    "#ifdef YYSTACKSIZE",

    "#ifndef YYMAXDEPTH",

    "#define YYMAXDEPTH YYSTACKSIZE",

    "#endif",

    "#else",

    "#ifdef YYMAXDEPTH",

    "#define YYSTACKSIZE YYMAXDEPTH",

    "#else",

    "#define YYSTACKSIZE 500",

    "#define YYMAXDEPTH 500",

    "#endif",

    "#endif",

    "int yydebug;",

    "int yynerrs;",

    "int yyerrflag;",

    "int yychar;",

    "short *yyssp;",

    "YYSTYPE *yyvsp;",

    "YYSTYPE yyval;",

    "YYSTYPE yylval;",

    "short yyss[YYSTACKSIZE];",

    "YYSTYPE yyvs[YYSTACKSIZE];",

    "#define yystacksize YYSTACKSIZE",

    0

};



char *jheader[] =

{

  "import java.io.BufferedReader;",

  "import java.io.DataInputStream;",

  "import java.io.InputStreamReader;",

  "import java.io.InputStream;",

  "import java.io.File;",

  "import java.io.FileReader;",

  "import java.io.LineNumberReader;",

  "import java.io.RandomAccessFile;",

  "import java.io.Reader;",

  "import java.io.StreamTokenizer;",

  "\n\n\n",

  "//#####################################################################",

  "@JAVA@// class: %s\n",

  "// does : encapsulates yacc() parser functionality in a Java",

  "//        class for quick code development",

  "//#####################################################################",

  "@JAVAX@public class %s",

  "{\n",

  "boolean yydebug;        //do I want debug output?",

  "int yynerrs;            //number of errors so far",

  "int yyerrflag;          //was there an error?",

  "int yychar;             //the current working character",

  "\n//########## MESSAGES ##########",

  "//###############################################################",

  "// method: debug",

  "//###############################################################",

  "void debug(String msg)",

  "{",

  "  if (yydebug)",

  "    System.out.println(msg);",

  "}",

  "\n//########## STATE STACK ##########",

  "final static int YYSTACKSIZE = 500;  //maximum stack size",

  "int statestk[],stateptr;             //state stack",

  "//###############################################################",

  "// methods: state stack push,pop,drop,peek",

  "//###############################################################",

  "void state_push(int state)",

  "{",

  "  if (stateptr>=YYSTACKSIZE)         //overflowed?",

  "    return;",

  "  statestk[++stateptr]=state;",

  "}",

  "int state_pop()",

  "{",

  "  if (stateptr<0)                    //underflowed?",

  "    return -1;",

  "  return statestk[stateptr--];",

  "}",

  "void state_drop(int cnt)",

  "{",

  "int ptr;",

  "  ptr=stateptr-cnt;",

  "  if (ptr<0)",

  "    return;",

  "  stateptr = ptr;",

  "}",

  "int state_peek(int relative)",

  "{",

  "int ptr;",

  "  ptr=stateptr-relative;",

  "  if (ptr<0)",

  "    return -1;",

  "  return statestk[ptr];",

  "}",

  "//###############################################################",

  "// method: init_stacks : allocate and prepare stacks",

  "//###############################################################",

  "boolean init_stacks()",

  "{",

  "  statestk = new int[YYSTACKSIZE];",

  "  stateptr = -1;",

  "  val_init();",

  "  return true;",

  "}",

  "//###############################################################",

  "// method: dump_stacks : show n levels of the stacks",

  "//###############################################################",

  "void dump_stacks(int count)",

  "{",

  "int i;",

  "  System.out.println(\"=index==state====value=     s:\"+stateptr+\"  v:\"+valptr);",

  "  for (i=0;i<count;i++)",

  "    System.out.println(\" \"+i+\"    \"+statestk[i]+\"      \"+valstk[i]);",

  "  System.out.println(\"======================\");",

  "}",

  "// Read in a table of shorts.",

  "// The tables are in two formats, a binary format and a text format.",

  "// To find the data, we use resources to look in the current",

  "// directory first for a .bin file, then for a .tbl file.",

  "// If the .bin file is not present, but the .tbl file is present,",

  "// then we write out the .bin file for use next time.",

  "static short[] read_short_table(String filename, int size)",

  "{",

  "  short[] retval = new short[size];",

  "",

  "  // Try reading the file in binary format.",

  "  // We have to use ClassLoader here because this method is static.",

  "  InputStream binaryInputStream =",

  "      ClassLoader.getSystemResourceAsStream(\"ptolemy/lang/java/\" + ",

  "              filename + \".bin\");",

  "  if (binaryInputStream != null) {",

  "      try {",

  "          // We found the .bin file, now go get the data.",

  "          DataInputStream binaryDataInput =",

  "              new DataInputStream(binaryInputStream);",

  "          for (int i = 0; i < size; i++) {",

  "              retval[i] = binaryDataInput.readShort();",

  "          }",

  "          binaryDataInput.close();",

  "          return retval;",

  "      } catch (IOException e) {",

  "          // Just read the text table instead if an I/O error occurs.",

  "      }",

  "  }",

  "",

  "  // Try reading the file in text format.",

  "  // For Ptolemy II, the tables should be in $PTII/ptolemy/lang/java",

  "  InputStream tableInputStream =",

  "      ClassLoader.getSystemResourceAsStream(",

  "              \"ptolemy/lang/java/\" + filename + \".tbl\");",

  "  if (tableInputStream == null) {",

  "      throw new RuntimeException(\"No tables for \" + filename +",

  "              \" could be found\");",

  "  }",

  "",

  "  // \"As of JDK version 1.1, the preferred way to tokenize an input stream",

  "  // is to convert it into a character stream\"",

  "",

  "  Reader reader = new BufferedReader(new InputStreamReader(tableInputStream));",

  "  StreamTokenizer tokenizer = new StreamTokenizer(reader);",

  "  for (int i = 0; i < size; i++) {",

  "      try {",

  "        tokenizer.nextToken();",

  "      } catch (IOException e) {",

  "        throw new RuntimeException(filename + ",

  "                \" does not contain enough entries\");",

  "      }",

  "      // This shouldn't happen if we didn't call",

  "      //  parseNumbers() - BUG in JDK 1.2.1",

  "      retval[i] = (short) tokenizer.nval;",

  "  }",

  "  try {",

  "    reader.close();",

  "  } catch (IOException e) {",

  "    throw new RuntimeException(filename + \" could not be closed\");",

  "  }",

  "",

  "",

  "  // Write out the table in binary format for next time.",

  "  // In the perfect world, we would like to write to a URL,",

  "  // but instead we just write to the current directory.",

  "  // Note that this will cause problems if we are running this",

  "  // as an applet.  I tried writing to a URL as per",

  "  // http://www.javasoft.com/docs/books/tutorial/networking/urls/readingWriting.html",

  "  // but got",

  "  // \"java.net.UnknownServiceException: protocol doesn't support output\"",

  "",

  "  File binFile = new File(filename + \".bin\");",

  "  try {",

  "    RandomAccessFile rafOut = new RandomAccessFile(binFile, \"rw\");",

  "    for (int i = 0; i < size; i++) {",

  "        rafOut.writeShort(retval[i]);",

  "    }",

  "    rafOut.close();",

  "  } catch (IOException e) {",

  "    System.err.println(\"Warning: could not write binary table to \" +",

  "                               filename + \".bin : \" + e);",

  "  }",

  "",

  "  return retval;",

  "}",

  "",

  "// read_string_table is only used if we run with yydebug != 0",

  "static String[] read_string_table(String filename, int size)",

  "{",

  "  FileReader fileReader = null;",

  "  try {",

  "    fileReader = new FileReader(filename);",

  "  } catch (IOException e) {",

  "    return null; // hide error if we delete this non-critical table",

  "  }",

  "  LineNumberReader lineReader = new LineNumberReader(fileReader);",

  "  String[] retval = new String[size];",

  "  for (int i = 0; i < size; i++) {",

  "      try {",

  "        retval[i] = lineReader.readLine();",

  "      } catch (IOException e) {",

  "        throw new RuntimeException(filename + \"does not contain enough entries\");",

  "      }",

  "  }",

  "  try {",

  "    fileReader.close();",

  "  } catch (IOException e) {",

  "    throw new RuntimeException(filename + \" could not be closed\");",

  "  }",

  "  return retval;",

  "}",

  0

};





char *body[] =

{

    "#define YYABORT goto yyabort",

    "#define YYACCEPT goto yyaccept",

    "#define YYERROR goto yyerrlab",

    "int",

    "yyparse()",

    "{",

    "    register int yym, yyn, yystate;",

    "#if YYDEBUG",

    "    register char *yys;",

    "    extern char *getenv();",

    "",

    "    if (yys = getenv(\"YYDEBUG\"))",

    "    {",

    "        yyn = *yys;",

    "        if (yyn >= '0' && yyn <= '9')",

    "            yydebug = yyn - '0';",

    "    }",

    "#endif",

    "",

    "    yynerrs = 0;",

    "    yyerrflag = 0;",

    "    yychar = (-1);",

    "",

    "    yyssp = yyss;",

    "    yyvsp = yyvs;",

    "    *yyssp = yystate = 0;",

    "",

    "yyloop:",

    "    if (yyn = yydefred[yystate]) goto yyreduce;",

    "    if (yychar < 0)",

    "    {",

    "        if ((yychar = yylex()) < 0) yychar = 0;",

    "#if YYDEBUG",

    "        if (yydebug)",

    "        {",

    "            yys = 0;",

    "            if (yychar <= YYMAXTOKEN) yys = yyname[yychar];",

    "            if (!yys) yys = \"illegal-symbol\";",

    "            printf(\"yydebug: state %d, reading %d (%s)\\n\", yystate,",

    "                    yychar, yys);",

    "        }",

    "#endif",

    "    }",

    "    if ((yyn = yysindex[yystate]) && (yyn += yychar) >= 0 &&",

    "            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)",

    "    {",

    "#if YYDEBUG",

    "        if (yydebug)",

    "            printf(\"yydebug: state %d, shifting to state %d\\n\",",

    "                    yystate, yytable[yyn]);",

    "#endif",

    "        if (yyssp >= yyss + yystacksize - 1)",

    "        {",

    "            goto yyoverflow;",

    "        }",

    "        *++yyssp = yystate = yytable[yyn];",

    "        *++yyvsp = yylval;",

    "        yychar = (-1);",

    "        if (yyerrflag > 0)  --yyerrflag;",

    "        goto yyloop;",

    "    }",

    "    if ((yyn = yyrindex[yystate]) && (yyn += yychar) >= 0 &&",

    "            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)",

    "    {",

    "        yyn = yytable[yyn];",

    "        goto yyreduce;",

    "    }",

    "    if (yyerrflag) goto yyinrecovery;",

    "#ifdef lint",

    "    goto yynewerror;",

    "#endif",

    "yynewerror:",

    "    yyerror(\"syntax error\");",

    "#ifdef lint",

    "    goto yyerrlab;",

    "#endif",

    "yyerrlab:",

    "    ++yynerrs;",

    "yyinrecovery:",

    "    if (yyerrflag < 3)",

    "    {",

    "        yyerrflag = 3;",

    "        for (;;)",

    "        {",

    "            if ((yyn = yysindex[*yyssp]) && (yyn += YYERRCODE) >= 0 &&",

    "                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)",

    "            {",

    "#if YYDEBUG",

    "                if (yydebug)",

    "                    printf(\"yydebug: state %d, error recovery shifting\\",

    " to state %d\\n\", *yyssp, yytable[yyn]);",

    "#endif",

    "                if (yyssp >= yyss + yystacksize - 1)",

    "                {",

    "                    goto yyoverflow;",

    "                }",

    "                *++yyssp = yystate = yytable[yyn];",

    "                *++yyvsp = yylval;",

    "                goto yyloop;",

    "            }",

    "            else",

    "            {",

    "#if YYDEBUG",

    "                if (yydebug)",

    "                    printf(\"yydebug: error recovery discarding state %d\

\\n\",",

    "                            *yyssp);",

    "#endif",

    "                if (yyssp <= yyss) goto yyabort;",

    "                --yyssp;",

    "                --yyvsp;",

    "            }",

    "        }",

    "    }",

    "    else",

    "    {",

    "        if (yychar == 0) goto yyabort;",

    "#if YYDEBUG",

    "        if (yydebug)",

    "        {",

    "            yys = 0;",

    "            if (yychar <= YYMAXTOKEN) yys = yyname[yychar];",

    "            if (!yys) yys = \"illegal-symbol\";",

    "            printf(\"yydebug: state %d, error recovery discards token %d\

 (%s)\\n\",",

    "                    yystate, yychar, yys);",

    "        }",

    "#endif",

    "        yychar = (-1);",

    "        goto yyloop;",

    "    }",

    "yyreduce:",

    "#if YYDEBUG",

    "    if (yydebug)",

    "        printf(\"yydebug: state %d, reducing by rule %d (%s)\\n\",",

    "                yystate, yyn, yyrule[yyn]);",

    "#endif",

    "    yym = yylen[yyn];",

    "    yyval = yyvsp[1-yym];",

    "    switch (yyn)",

    "    {",

    0

};



char *jbody[] =

{

    "//###############################################################",

    "// method: yylexdebug : check lexer state",

    "//###############################################################",

    "void yylexdebug(int state,int ch)",

    "{",

    "String s=null;",

    "  if (ch < 0) ch=0;",

    "  if (ch <= YYMAXTOKEN) //check index bounds",

    "     s = yyname[ch];    //now get it",

    "  if (s==null)",

    "    s = \"illegal-symbol\";",

    "  debug(\"state \"+state+\", reading \"+ch+\" (\"+s+\")\");",

    "}\n\n\n",

    "//###############################################################",

    "// method: yyparse : parse input and execute indicated items",

    "//###############################################################",

    "int yyparse()",

    "{",

    "int yyn;       //next next thing to do",

    "int yym;       //",

    "int yystate;   //current parsing state from state table",

    "String yys;    //current token string",

    "boolean doaction;",

    "  init_stacks();",

    "  yynerrs = 0;",

    "  yyerrflag = 0;",

    "  yychar = -1;          //impossible char forces a read",

    "  yystate=0;            //initial state",

    "  state_push(yystate);  //save it",

    "  String yyrule[] = null;",

    "  if (yydebug)",

    "     yyrule = read_string_table(\"yyrule.tbl\", NRULES - 2);",

    "  while (true) //until parsing is done, either correctly, or w/error",

    "    {",

    "    doaction=true;",

    "    if (yydebug) debug(\"loop\"); ",

    "    //#### NEXT ACTION (from reduction table)",

    "    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])",

    "      {",

    "      if (yydebug) debug(\"yyn:\"+yyn+\"  state:\"+yystate+\"  char:\"+yychar);",

    "      if (yychar < 0)      //we want a char?",

    "        {",

    "        yychar = yylex();  //get next token",

    "        //#### ERROR CHECK ####",

    "        if (yychar < 0)    //it it didn't work/error",

    "          {",

    "          yychar = 0;      //change it to default string (no -1!)",

    "          if (yydebug)",

    "            yylexdebug(yystate,yychar);",

    "          }",

    "        }//yychar<0",

    "      yyn = yysindex[yystate];  //get amount to shift by (shift index)",

    "      if ((yyn != 0) && (yyn += yychar) >= 0 &&",

    "          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)",

    "        {",

    "        if (yydebug)",

    "          debug(\"state \"+yystate+\", shifting to state \"+yytable[yyn]+\"\");",

    "        //#### NEXT STATE ####",

    "        yystate = yytable[yyn];//we are in a new state",

    "        state_push(yystate);   //save it",

    "        val_push(yylval);      //push our lval as the input for next rule",

    "        yychar = -1;           //since we have 'eaten' a token, say we need another",

    "        if (yyerrflag > 0)     //have we recovered an error?",

    "           --yyerrflag;        //give ourselves credit",

    "        doaction=false;        //but don't process yet",

    "        break;   //quit the yyn=0 loop",

    "        }",

    "",

    "    yyn = yyrindex[yystate];  //reduce",

    "    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&",

    "            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)",

    "      {   //we reduced!",

    "      if (yydebug) debug(\"reduce\");",

    "      yyn = yytable[yyn];",

    "      doaction=true; //get ready to execute",

    "      break;         //drop down to actions",

    "      }",

    "    else //ERROR RECOVERY",

    "      {",

    "      if (yyerrflag==0)",

    "        {",

    "        yyerror(\"syntax error\");",

    "        yynerrs++;",

    "        }",

    "      if (yyerrflag < 3) //low error count?",

    "        {",

    "        yyerrflag = 3;",

    "        while (true)   //do until break",

    "          {",

    "          if (stateptr<0)   //check for under & overflow here",

    "            {",

    "            yyerror(\"stack underflow. aborting...\");  //note lower case 's'",

    "            return 1;",

    "            }",

    "          yyn = yysindex[state_peek(0)];",

    "          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&",

    "                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)",

    "            {",

    "            if (yydebug)",

    "              debug(\"state \"+state_peek(0)+\", error recovery shifting to state \"+yytable[yyn]+\" \");",

    "            yystate = yytable[yyn];",

    "            state_push(yystate);",

    "            val_push(yylval);",

    "            doaction=false;",

    "            break;",

    "            }",

    "          else",

    "            {",

    "            if (yydebug)",

    "              debug(\"error recovery discarding state \"+state_peek(0)+\" \");",

    "            if (stateptr<0)   //check for under & overflow here",

    "              {",

    "              yyerror(\"Stack underflow. aborting...\");  //capital 'S'",

    "              return 1;",

    "              }",

    "            state_pop();",

    "            val_pop();",

    "            }",

    "          }",

    "        }",

    "      else            //discard this token",

    "        {",

    "        if (yychar == 0)",

    "          return 1; //yyabort",

    "        if (yydebug)",

    "          {",

    "          yys = null;",

    "          if (yychar <= YYMAXTOKEN) yys = yyname[yychar];",

    "          if (yys == null) yys = \"illegal-symbol\";",

    "          debug(\"state \"+yystate+\", error recovery discards token \"+yychar+\" (\"+yys+\")\");",

    "          }",

    "        yychar = -1;  //read another",

    "        }",

    "      }//end error recovery",

    "    }//yyn=0 loop",

    "    if (!doaction)   //any reason not to proceed?",

    "      continue;      //skip action",

    "    yym = yylen[yyn];          //get count of terminals on rhs",

    "    if (yydebug)",

    "      if (yyrule == null) {",

    "        debug(\"state \"+yystate+\", reducing \"+yym+\" by rule \"+yyn+\" yyrule is null, perhaps yyrule.tbl was not read in?\");",

    "      } else {",     

    "        debug(\"state \"+yystate+\", reducing \"+yym+\" by rule \"+yyn+\" (\"+yyrule[yyn]+\")\");",

    "      }",     

    "    if (yym>0) { //if count of rhs not 'nil'",

    0

};



char *jbody_object_semantic[] =

{

    "       try {",

    "@JAVAS@         yyval = (%s%s) val_peek(yym-1).clone(); //get current semantic value",

    "       } catch (CloneNotSupportedException e) {",

    "         yyerror(\"Clone not supported\");",

    "       }",

    "    } else {",

    "@JAVAS@      yyval = new %s%s();",

    "    }",

    "    switch(yyn)",

    "      {",

    "//########## USER-SUPPLIED ACTIONS ##########",

    0

};



char *jbody_prim_semantic[] =

{

    "    yyval = val_peek(yym-1); //get current semantic value",

    "    switch(yyn)",

    "      {",

    "//########## USER-SUPPLIED ACTIONS ##########",

    0

};







char *trailer[] =

{

    "    }",

    "    yyssp -= yym;",

    "    yystate = *yyssp;",

    "    yyvsp -= yym;",

    "    yym = yylhs[yyn];",

    "    if (yystate == 0 && yym == 0)",

    "    {",

    "#if YYDEBUG",

    "        if (yydebug)",

    "            printf(\"yydebug: after reduction, shifting from state 0 to\\",

    " state %d\\n\", YYFINAL);",

    "#endif",

    "        yystate = YYFINAL;",

    "        *++yyssp = YYFINAL;",

    "        *++yyvsp = yyval;",

    "        if (yychar < 0)",

    "        {",

    "            if ((yychar = yylex()) < 0) yychar = 0;",

    "#if YYDEBUG",

    "            if (yydebug)",

    "            {",

    "                yys = 0;",

    "                if (yychar <= YYMAXTOKEN) yys = yyname[yychar];",

    "                if (!yys) yys = \"illegal-symbol\";",

    "                printf(\"yydebug: state %d, reading %d (%s)\\n\",",

    "                        YYFINAL, yychar, yys);",

    "            }",

    "#endif",

    "        }",

    "        if (yychar == 0) goto yyaccept;",

    "        goto yyloop;",

    "    }",

    "    if ((yyn = yygindex[yym]) && (yyn += yystate) >= 0 &&",

    "            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)",

    "        yystate = yytable[yyn];",

    "    else",

    "        yystate = yydgoto[yym];",

    "#if YYDEBUG",

    "    if (yydebug)",

    "        printf(\"yydebug: after reduction, shifting from state %d \\",

    "to state %d\\n\", *yyssp, yystate);",

    "#endif",

    "    if (yyssp >= yyss + yystacksize - 1)",

    "    {",

    "        goto yyoverflow;",

    "    }",

    "    *++yyssp = yystate;",

    "    *++yyvsp = yyval;",

    "    goto yyloop;",

    "yyoverflow:",

    "    yyerror(\"yacc stack overflow\");",

    "yyabort:",

    "    return (1);",

    "yyaccept:",

    "    return (0);",

    "}",

    0

};



char *jtrailer[] =

{

    "//########## END OF USER-SUPPLIED ACTIONS ##########",

    "    }//switch",

    "    //#### Now let's reduce... ####",

    "    if (yydebug) debug(\"reduce\");",

    "    state_drop(yym);             //we just reduced yylen states",

    "    yystate = state_peek(0);     //get new state",

    "    val_drop(yym);               //corresponding value drop",

    "    yym = yylhs[yyn];            //select next TERMINAL(on lhs)",

    "    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL",

    "      {",

    "      debug(\"After reduction, shifting from state 0 to state \"+YYFINAL+\"\");",

    "      yystate = YYFINAL;         //explicitly say we're done",

    "      state_push(YYFINAL);       //and save it",

    "      val_push(yyval);           //also save the semantic value of parsing",

    "      if (yychar < 0)            //we want another character?",

    "        {",

    "        yychar = yylex();        //get next character",

    "        if (yychar<0) yychar=0;  //clean, if necessary",

    "        if (yydebug)",

    "          yylexdebug(yystate,yychar);",

    "        }",

    "      if (yychar == 0)          //Good exit (if lex returns 0 ;-)",

    "         break;                 //quit the loop--all DONE",

    "      }//if yystate",

    "    else                        //else not done yet",

    "      {                         //get next state and push, for next yydefred[]",

    "      yyn = yygindex[yym];      //find out where to go",

    "      if ((yyn != 0) && (yyn += yystate) >= 0 &&",

    "            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)",

    "        yystate = yytable[yyn]; //get new state",

    "      else",

    "        yystate = yydgoto[yym]; //else go to new defred",

    "      debug(\"after reduction, shifting from state \"+state_peek(0)+\" to state \"+yystate+\"\");",

    "      state_push(yystate);     //going again, so push state & val...",

    "      val_push(yyval);         //for next action",

    "      }",

    "    }//main loop",

    "  return 0;//yyaccept!!",

    "}",

    "//## end of method parse() ######################################",

    "\n\n",

    "//## run() --- for Thread #######################################",

    "public void run()",

    "{",

    "   yyparse();",

    "}",

    "//## end of method run() ########################################",

    "\n\n",

    "//## Constructor ################################################",

    "@JAVA@public %s()\n",

    "{",

    "}\n",

    "@JAVA@public %s(boolean debug_me)\n",

    "{",

    "  yydebug=debug_me;",

    "}",

    "//###############################################################",

    "\n\n",

    "}",

    "//################### END OF CLASS yaccpar ######################",

    0

};



char *jtrailer_nothread[] =

{

    "//########## END OF USER-SUPPLIED ACTIONS ##########",

    "    }//switch",

    "    //#### Now let's reduce... ####",

    "    if (yydebug) debug(\"reduce\");",

    "    state_drop(yym);             //we just reduced yylen states",

    "    yystate = state_peek(0);     //get new state",

    "    val_drop(yym);               //corresponding value drop",

    "    yym = yylhs[yyn];            //select next TERMINAL(on lhs)",

    "    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL",

    "      {",

    "      debug(\"After reduction, shifting from state 0 to state \"+YYFINAL+\"\");",

    "      yystate = YYFINAL;         //explicitly say we're done",

    "      state_push(YYFINAL);       //and save it",

    "      val_push(yyval);           //also save the semantic value of parsing",

    "      if (yychar < 0)            //we want another character?",

    "        {",

    "        yychar = yylex();        //get next character",

    "        if (yychar<0) yychar=0;  //clean, if necessary",

    "        if (yydebug)",

    "          yylexdebug(yystate,yychar);",

    "        }",

    "      if (yychar == 0)          //Good exit (if lex returns 0 ;-)",

    "         break;                 //quit the loop--all DONE",

    "      }//if yystate",

    "    else                        //else not done yet",

    "      {                         //get next state and push, for next yydefred[]",

    "      yyn = yygindex[yym];      //find out where to go",

    "      if ((yyn != 0) && (yyn += yystate) >= 0 &&",

    "            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)",

    "        yystate = yytable[yyn]; //get new state",

    "      else",

    "        yystate = yydgoto[yym]; //else go to new defred",

    "      debug(\"after reduction, shifting from state \"+state_peek(0)+\" to state \"+yystate+\"\");",

    "      state_push(yystate);     //going again, so push state & val...",

    "      val_push(yyval);         //for next action",

    "      }",

    "    }//main loop",

    "  return 0;//yyaccept!!",

    "}",

    "//## end of method parse() ######################################",

    "\n\n",

    "}",

    "//################### END OF CLASS yaccpar ######################",

    0

};





void write_section(char **section)

{

int i;

FILE *fp;

  fp = code_file;

  if (section==jtrailer)

    {

    if (strcmp(java_extend_name,"Thread")!=0)

      section=jtrailer_nothread;

    }

  for (i = 0; section[i]; ++i)

    {

	 ++outline;

    if (strncmp(section[i],"@JAVA@",6)==0)

	   fprintf(fp,&(section[i][6]),java_class_name);

    else if (strncmp(section[i],"@JAVAX@",7)==0)

	   {

      fprintf(fp,&(section[i][7]),java_class_name);

      if (java_extend_name[0]!='\0')

        fprintf(fp," extends %s",java_extend_name);

      if (java_implement_name[0]!='\0')

        fprintf(fp," implements %s",java_implement_name);

      fprintf(fp,"\n");

      }

    else if (strncmp(section[i],"@JAVAS@",7)==0)

      {

      if (java_semantic_type) {

         fprintf(fp,&(section[i][7]),java_semantic_type,"");

      } else {

         fprintf(fp,&(section[i][7]),java_class_name,"val");

      }

      fprintf(fp,"\n");      

      }

    else

	   fprintf(fp, "%s\n", section[i]);

    }

}



