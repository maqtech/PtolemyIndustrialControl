package ptolemy.lang;

import java.io.*;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class GenerateVisitor {

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
       System.out.println("Usage : GenerateVisitor TypeNameListFile [VisitorClassName] [BaseNodeName]");
       return;
    }

    GenerateVisitor genVisitor = new GenerateVisitor(args);

    genVisitor.generate();
  }

  public GenerateVisitor(String[] args) throws IOException {

    File fsrc  = new File(args[0]);

    String visitorOutFile;
    if (args.length < 2) {
       _visitorClassName = "Visitor";
       visitorOutFile = "Visitor.java";
    } else {
       _visitorClassName = args[1];
       visitorOutFile = _visitorClassName + ".java";
    }

    if (args.length < 3) {
       _baseNodeName = "TreeNode";
    } else {
       _baseNodeName = args[2];
    }

    File fdest = new File(visitorOutFile);

    if (!fdest.createNewFile()) {
       fdest.delete();
       fdest = new File(visitorOutFile);
       fdest.createNewFile();
    }

    try {
      _ofs = new FileWriter(fdest);
    } catch (FileNotFoundException e) {
      System.out.println("Couldn't open destination file.");
      return;
    }

    try {
      _ifs = new LineNumberReader(new FileReader(fsrc));
    } catch (FileNotFoundException e) {
      System.out.println("Couldn't open input file.");
      return;
    }

    // initialize _lastLine
    _lastLine = _ifs.readLine();

    // get common header for nodes and visitor
    String commonHeader = _readBlock("cheader");

    // get header for visitor
    _visitorHeader = commonHeader + _readBlock("vheader");

    // get header for nodes
    _nodeHeader = commonHeader + _readBlock("nheader");
  }

  /** Generate the visitor class and the node class files */
  public void generate() throws IOException {

    _ofs.write(_visitorHeader);
    _ofs.write("public class "+ _visitorClassName + " implements IVisitor {\n" +
               "    public " + _visitorClassName + "() {\n" +
               "        this(TM_CHILDREN_FIRST);\n" +
               "    }\n\n" +
               "    public " + _visitorClassName + "(int traversalMethod) {\n" +
               "        if (traversalMethod > TM_CUSTOM) {\n" +
               "           throw new RuntimeException(\"Illegal traversal method\");\n" +
               "        }\n" +
               "        _traversalMethod = traversalMethod;\n" +
               "    }\n\n");

    _readPlacement();

    _readClassInfo();

    _ifs.close();

    Iterator itr = _typeList.listIterator();
    Iterator parItr = _parentTypeList.listIterator();
    Iterator concreteItr = _isConcreteList.listIterator();
    Iterator singletonItr = _isSingletonList.listIterator();
    Iterator inTreeItr = _isInTreeList.listIterator();
    Iterator interfaceItr = _isInterfaceList.listIterator();
    Iterator methodListItr = _methodListList.listIterator();
    Iterator implListItr = _implListList.listIterator();

    while (itr.hasNext()) {
      String typeName = (String) itr.next();
      String parentTypeName = (String) parItr.next();
      boolean isConcrete = ((Boolean) concreteItr.next()).booleanValue();
      boolean isSingleton = ((Boolean) singletonItr.next()).booleanValue();
      boolean isInterface = ((Boolean) interfaceItr.next()).booleanValue();
      boolean isInTree = ((Boolean) inTreeItr.next()).booleanValue();
      
      LinkedList methodList = (LinkedList) methodListItr.next();
      LinkedList implList = (LinkedList) implListItr.next();

      if (isConcrete && isInTree) {
         _ofs.write(
          "\n" +
          "    public Object visit" + typeName + "(" + typeName +
          " node, LinkedList args) {\n" +
          "        return _defaultVisit(node, args);\n" +
          "    }\n");
      }

      _generateNodeFile(typeName, parentTypeName, isConcrete, isSingleton,
       isInterface, methodList, implList);
    }

    _ofs.write(
     "\n" +
     "    /** Specify the order in visiting the nodes. */\n" +
     "    public final int traversalMethod() { return _traversalMethod; }\n" +
     "\n" +
     "    /** The default visit method. */\n" +
     "    protected Object _defaultVisit(" + _baseNodeName + " node, LinkedList args) {\n" +
     "        return null;\n" +
     "    }\n" +
     "\n" +
     "    protected final int _traversalMethod;\n" +
     "}\n");

    _ofs.close();
  }

  protected String _readBlock(String marker) throws IOException {
    while ((_lastLine != null) && _lastLine.equals("")) {
      _lastLine = _ifs.readLine();
    }

    String beginTag = "<" + marker + ">";
    String endTag   = "</" + marker + ">";

    if ((_lastLine == null) || !_lastLine.equals(beginTag)) {
       return "";
    }

    StringBuffer sb = new StringBuffer();

    boolean endHeader = false;
    do {
       _lastLine = _ifs.readLine();

       endHeader = ((_lastLine == null) || _lastLine.equals(endTag));

       if (!endHeader) {
          sb.append(_lastLine);
          sb.append('\n');
       }
    } while (!endHeader);

    _lastLine = _ifs.readLine();

    sb.append('\n');

    return sb.toString();
  }


  protected void _generateNodeFile(String typeName, String parentTypeName,
   boolean isConcrete, boolean isSingleton, boolean isInterface,
   LinkedList methodList, LinkedList implList) throws IOException {
    File fdest = new File(typeName + ".java");

    if (!fdest.createNewFile()) {
       fdest.delete();
       fdest = new File(typeName + ".java");
       fdest.createNewFile();
    }

    FileWriter fw = new FileWriter(fdest);

    StringBuffer sb = new StringBuffer();

    sb.append(_nodeHeader);

    sb.append("public ");

    if (!isConcrete && !isInterface) {
       sb.append("abstract ");
    } else if (isSingleton) {
       sb.append("final ");
    }

    if (isInterface) {
       sb.append("interface ");
    } else {
       sb.append("class ");
    }

    sb.append(typeName);

    if (!parentTypeName.equals("<none>")) {
       sb.append(" extends ");
       sb.append(parentTypeName);
    }

    Iterator implItr = implList.listIterator();

    if (implItr.hasNext()) {
       sb.append(" implements ");

       do {
         String interfaceName = (String) implItr.next();

         sb.append(interfaceName);

         if (implItr.hasNext()) {
            sb.append(", ");
         }
       } while (implItr.hasNext());
    }

    sb.append(" {\n");

    // do methods first
    Iterator methodItr = methodList.listIterator();

    while (methodItr.hasNext()) {
      Object o = methodItr.next();

      if (o instanceof MethodSignature) {
         sb.append(o.toString());
         sb.append('\n');
      }
    }

    // now do fields
    methodItr = methodList.listIterator();

    while (methodItr.hasNext()) {
      Object o = methodItr.next();

      if (o instanceof ClassField) {
         sb.append(o.toString());
         sb.append('\n');
      }
    }

    sb.append("}\n");

    fw.write(sb.toString());
    fw.close();
  }

  protected void _readPlacement() throws IOException {
    _lastLine = _ifs.readLine();

    if (_lastLine.startsWith("mplace")) {
       _defaultPlacement = _lastLine.charAt(7);
       _lastLine = _ifs.readLine();
    }
  }

  protected void _readClassInfo() throws IOException {
    StringTokenizer strTokenizer;
    String className;
    String marker;

    do {
      if ((_lastLine != null) && (_lastLine.length() > 4) &&
          !(_lastLine.startsWith("//"))) {

         strTokenizer = new StringTokenizer(_lastLine);

         className = strTokenizer.nextToken();

         System.out.println("Reading class info for : " + className);

         try {
            _typeList.addLast(className);
         } catch (NullPointerException e) {
            System.err.println("Not enough parameters in line : " + _lastLine);
            return;
          }

         String nextToken = strTokenizer.nextToken();

         boolean isSingleton;
         boolean isInterface;
         try {
            isSingleton = nextToken.startsWith("S");
            _isSingletonList.addLast(new Boolean(isSingleton));

            _isConcreteList.addLast(new Boolean(
             isSingleton || nextToken.startsWith("C")));

            isInterface = nextToken.startsWith("I");
            _isInterfaceList.addLast(new Boolean(isInterface));
            
            _isInTreeList.addLast(new Boolean(!nextToken.endsWith("N")));

         } catch (NullPointerException e) {
            System.err.println("Not enough parameters in line : " + _lastLine);
            return;
         }

         try {
            nextToken = strTokenizer.nextToken();
            _parentTypeList.addLast(nextToken);
         } catch (NullPointerException e) {
            System.err.println("Not enough parameters in line : " + _lastLine);
           return;
         }

         LinkedList methodList = new LinkedList();
         LinkedList implList   = new LinkedList();

         if (isSingleton) {
            ClassField cf  = new ClassField(className, "instance",
             "public static final", "new " + className + "()");

            methodList.addLast(cf);

            MethodSignature ms = new MethodSignature(className);

            methodList.addLast(ms);
         }

         while (strTokenizer.hasMoreTokens()) {
           marker = strTokenizer.nextToken();
           char markChar = marker.charAt(0);
           switch (markChar) {

           case 'c':
           case 'm':
           {
             MethodSignature ms =
               new MethodSignature(markChar, strTokenizer, className,
                _defaultPlacement, isInterface);
             methodList.addLast(ms);
           }
           break;

           case 'k':
           {
             MethodSignature ms =
               new MethodSignature(markChar, strTokenizer, className,
                _defaultPlacement, isInterface);

             methodList.addLast(ms);

             LinkedList accessorMethodList = ms.accessors();

             methodList.addAll(accessorMethodList);
           }
           break;

           case 'i':
           {
             boolean isName;
             do {
                nextToken = strTokenizer.nextToken();

                isName = ((nextToken != null) && !nextToken.equals("i"));
                if (isName) {
                  implList.addLast(nextToken);
                }
             } while (isName);
           }
           break;

           default:
           throw new RuntimeException("Unrecognized marker : " + marker);
           }
         }

         _methodListList.addLast(methodList);
         _implListList.addLast(implList);
      }
      _lastLine = _ifs.readLine();
    } while (_lastLine != null);
  }

  private static final String ident = "    ";

  public static class MethodSignature {
    public MethodSignature() {}

    /** a singleton constructor */
    public MethodSignature(String className) {
      _defConstruct = false;
      _construct = true;
      _isSingleton = true;
      _isInterface = false;
      _name = className;
      _returnType = "";
    }

    /** a constructor or method */
    public MethodSignature(char sigType, StringTokenizer strToken,
     String className, char defaultPlacement, boolean isInterface)
     throws IOException {

      _isInterface = isInterface;

      System.out.println("method sig constructor begin");

      _defConstruct = (sigType == 'k');
      _construct = (sigType == 'c') || _defConstruct;

      if (_construct) { // constructor
         _name = className;
         _returnType = "";

         _superParams = Integer.parseInt(strToken.nextToken());

      } else if (sigType == 'm') { // method
         _superParams = 0;

         _returnType = strToken.nextToken();

         _name = strToken.nextToken();
      } else {
         throw new RuntimeException("Invalid token for MethodSignature : " +
          sigType);
      }

      String s = strToken.nextToken();

      System.out.println("first s : " + s);

      while (!(s.equals("c") || s.equals("m") || s.equals("k"))) {
         if (s.charAt(0) == '{') {
           // explicit placement, don't check
           _varPlacements.addLast(new Character(s.charAt(1)));
            s = s.substring(3);
         }  else {

            if ((defaultPlacement == 'l') && isJavaType(s)) {
               // make it a member if it's a Java type and we default to put it in a list
               _varPlacements.addLast(new Character('m'));
            } else {
               _varPlacements.addLast(new Character(defaultPlacement));
            }
         }

         if (s.equals("[")) { // super constructor argument
            StringBuffer sb = new StringBuffer();
            boolean isInit;

            s = strToken.nextToken();

            sb.append(s);

            do {
               s = strToken.nextToken();

               isInit = !s.equals("]");

               if (isInit) {
                  sb.append(' ');
                  sb.append(s);
               }
            } while (isInit);

            _superArgs.addLast(sb.toString());

            _paramTypes.addLast("omitted");
            _paramNames.addLast("omitted");

         } else {

            _paramTypes.addLast(s);

            String paramName = strToken.nextToken();

            _paramNames.addLast(paramName);
            _superArgs.addLast(paramName);
         }

         s = strToken.nextToken();

         System.out.println("next s : " + s);
      }

      System.out.println("method sig constructor end");
    }

    public String methodBody() {

      if (_construct) {

        StringBuffer sb = new StringBuffer();

        if (_superParams > 0) {

           Iterator argsItr = _superArgs.listIterator();

           sb.append(ident);
           sb.append(ident);
           sb.append("super(");

           for (int i = 0; i < _superParams; i++) {
               sb.append((String) argsItr.next());

               if (i < (_superParams - 1)) {
                  sb.append(", ");
               }
           }

           sb.append(");\n");
        }

        if (_defConstruct) {

           Iterator typeItr = _paramTypes.listIterator();
           Iterator nameItr = _paramNames.listIterator();
           Iterator varPlaceItr = _varPlacements.listIterator();

           int varCount = 0;

           do {
              String typeStr = (String) typeItr.next();
              String nameStr = (String) nameItr.next();
              char placement = ((Character) varPlaceItr.next()).charValue();

              if (varCount >= _superParams) {

                 sb.append(ident);
                 sb.append(ident);

                 switch (placement) {

                 case 'l':
                 sb.append("_childList.addLast(");
                 sb.append(nameStr);
                 sb.append(");");
                 break;

                 case 'm':
                 case 'h':
                 sb.append('_');
                 sb.append(nameStr);
                 sb.append(" = ");
                 sb.append(nameStr);
                 sb.append(';');
                 break;

                 case 'p':
                 sb.append("setProperty(\"");
                 sb.append(nameStr);
                 sb.append("\", ");
                 sb.append(_wrapPrimitive(typeStr, nameStr));
                 sb.append(");");
                 break;

                 case 'n':
                 // do nothing
                 break;

                 default:
                 throw new RuntimeException("unknown variable placment");
                 }

                 if (typeItr.hasNext()) {
                    sb.append('\n');
                 }
              }

              varCount++;
           } while (typeItr.hasNext());
        }
        return sb.toString();
      } // if _construct

      if (_methodBody != null) {
         return ident + ident + _methodBody;
      }

      if (_returnType.equals("void") || _returnType.equals("")) {
         return "";
      }

      if (_returnType.equals("long")) {
         return ident + ident + "return 0L;";
      }

      if (_returnType.equals("int") || _returnType.equals("short") ||
          _returnType.equals("byte")) {
         return ident + ident + "return 0;";
      }

      if (_returnType.equals("char")) {
         return ident + ident + "return '\\0';";
      }

      if (_returnType.equals("float")) {
         return ident + ident + "return 0.0f;";
      }

      if (_returnType.equals("double")) {
         return ident + ident + "return 0.0d";
      }

      if (_returnType.equals("boolean")) {
         return ident + ident + "return false";
      }

      return ident + ident + "return null;";
    }

    public String toString() {
      StringBuffer sb = new StringBuffer(ident);

      sb.append(_isSingleton ? "private " : "public ");

      if (!_construct) { // if !constructor

         if (!_isInterface) {
            sb.append("final ");
         }
         sb.append(_returnType);
         sb.append(" ");
      }

      sb.append(_name);
      sb.append('(');

      Iterator typeItr = _paramTypes.listIterator();
      Iterator nameItr = _paramNames.listIterator();

      int paramCount = 0;
      while (typeItr.hasNext()) {

        String typeName = (String) typeItr.next();
        String paramName = (String) nameItr.next();

        if (!typeName.equals("omitted")) {
           if (paramCount > 0) {
              sb.append(", ");
           }
           paramCount++;

           sb.append(typeName);
           sb.append(' ');
           sb.append(paramName);
        }
      }

      sb.append(')');

      if (_isInterface) {
         sb.append(";");
      } else {
         sb.append(" {\n");
         sb.append(methodBody());
         sb.append('\n');
         sb.append(ident);
         sb.append("}\n");
      }

      return sb.toString();
    }

    /** a getter or a setter method for a child in the list */
    public MethodSignature(String returnType, String name, int childIndex, boolean setter) {

      Character firstLetter = new Character(Character.toUpperCase(name.charAt(0)));

      String partName = firstLetter.toString() + name.substring(1);

      if (setter) {
        _returnType = "void";
        _name = "set" + partName;

        _paramTypes.addLast(returnType);
        _paramNames.addLast(name);

        _methodBody = "_childList.set(" + childIndex + ", " + name + ");";

      } else {
        _returnType = returnType;
        _name = "get" + partName;


        _methodBody = "return (" + _returnType + ") _childList.get(" +
                      childIndex + ");";
      }
    }

    // a getter or a setter method for data not in the list
    public MethodSignature(String returnType, String name, boolean setter) {
      Character firstLetter = new Character(Character.toUpperCase(name.charAt(0)));

      String partName = firstLetter.toString() + name.substring(1);

      if (setter) {
         _returnType = "void";
         _name = "set" + partName;

        _paramTypes.addLast(returnType);
        _paramNames.addLast(name);

        _methodBody = "_" + name + " = " + name + ";";
      } else {
        _returnType = returnType;
        _name = "get" + partName;

        _methodBody = "return _" + name + ";";
      }
    }

    // a hasX() method that returns true
    public MethodSignature(String name, int dummy) {
      Character firstLetter = new Character(Character.toUpperCase(name.charAt(0)));

      String partName = firstLetter.toString() + name.substring(1);

      _name = "has" + partName;
      _returnType = "boolean";
      _methodBody = "return true;";
    }

    public LinkedList accessors() {
      LinkedList retval = new LinkedList();

      int varCount = 0;
      int childIndex = 0;

      Iterator typeItr = _paramTypes.listIterator();
      Iterator nameItr = _paramNames.listIterator();
      Iterator varPlaceItr = _varPlacements.listIterator();

      while (typeItr.hasNext()) {

        String typeStr = (String) typeItr.next();
        String nameStr = (String) nameItr.next();
        char placement = ((Character) varPlaceItr.next()).charValue();

        if (varCount >= _superParams) {

           switch (placement) {

           case 'l':
           // getter
           retval.addLast(new MethodSignature(typeStr, nameStr, childIndex, false));

           //setter
           retval.addLast(new MethodSignature(typeStr, nameStr, childIndex, true));

           // getter index
           retval.addLast(new ClassField("int",
            "CHILD_INDEX_" + nameStr.toUpperCase(),
            "public static final", Integer.toString(childIndex)));
           break;

           case 'h': // member with hasX()
           retval.addLast(new MethodSignature(nameStr, -1));
           // no break;

           case 'm': // member
           retval.addLast(new ClassField(typeStr, nameStr));

           // getter
           retval.addLast(new MethodSignature(typeStr, nameStr, false));

           // setter
           retval.addLast(new MethodSignature(typeStr, nameStr, true));
           break;

           case 'n':
           // do nothing
           break;

           case 'p': // property
           // do nothing : data is accessed through PropertyMap methods
           break;

           default:
           throw new RuntimeException("unknown variable placement");

           }
        }

        if (placement == 'l') {
           childIndex++;
        }

        varCount++;
      }

      return retval;
    }

    protected String _wrapPrimitive(String typeStr, String nameStr) {
      String wrapper = null;

      if (typeStr.startsWith("int")) {
         wrapper = "Integer";
      } else if (typeStr.startsWith("char")) {
         wrapper = "Character";
      } else if (typeStr.startsWith("long")) {
         wrapper = "Long";
      } else if (typeStr.startsWith("byte")) {
         wrapper = "Byte";
      } else if (typeStr.startsWith("float")) {
         wrapper = "Float";
      } else if (typeStr.startsWith("double")) {
         wrapper = "Double";
      } else if (typeStr.startsWith("boolean")) {
         wrapper = "Boolean";
      }

      if (wrapper != null) {
         return "new " + wrapper + "(" + nameStr + ")";
      } else {
         return nameStr;
      }
    }

    protected String _returnType;
    protected String _name;

    protected LinkedList _paramTypes = new LinkedList();
    protected LinkedList _paramNames = new LinkedList();
    protected LinkedList _varPlacements = new LinkedList();
    protected LinkedList _superArgs = new LinkedList();

    protected int _superParams = 0;

    protected String _methodBody = null;

    protected boolean _isSingleton = false;
    protected boolean _construct = false;
    protected boolean _isInterface = false;
    protected boolean _defConstruct = false;
  }

  public static class ClassField {
    public ClassField(String type, String name, String modifiers, String init) {
      _type = type;
      _name = name;
      _modifiers = modifiers;
      _init = init;
    }

    public ClassField(String type, String name) {
      this(type, name, "protected", null);
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(ident);
      sb.append(_modifiers);
      sb.append(' ');
      sb.append(_type);
      sb.append(' ');

      if (!_modifiers.startsWith("public")) {
         sb.append(" _");
      }

      sb.append(_name);

      if (_init != null) {
         sb.append(" = ");
         sb.append(_init);
      }

      sb.append(';');
      return sb.toString();
    }

    String _type;
    String _name;
    String _modifiers;
    String _init;
  }

  public static final boolean isPrimitiveType(String s) {
    return (s.startsWith("int") || s.startsWith("char") ||
            s.startsWith("long") || s.startsWith("byte") ||
            s.startsWith("float") || s.startsWith("double") ||
            s.startsWith("boolean"));
  }

  public static final boolean isJavaType(String s) {
    return (isPrimitiveType(s) || s.startsWith("String"));
  }

  protected char _defaultPlacement = 'l';

  protected FileWriter _ofs;
  protected LineNumberReader _ifs;

  protected LinkedList _typeList = new LinkedList();
  protected LinkedList _parentTypeList = new LinkedList();
  protected LinkedList _isConcreteList = new LinkedList();
  protected LinkedList _isSingletonList = new LinkedList();
  protected LinkedList _isInterfaceList = new LinkedList();
  protected LinkedList _isInTreeList = new LinkedList();
  protected LinkedList _methodListList = new LinkedList();
  protected LinkedList _implListList = new LinkedList();

  protected String _visitorClassName;
  protected String _baseNodeName;
  protected String _nodeHeader;
  protected String _visitorHeader;

  protected String _lastLine;
}
