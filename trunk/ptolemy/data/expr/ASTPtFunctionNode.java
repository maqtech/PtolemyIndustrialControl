/* Generated By:JJTree: Do not edit this line. ASTPtFunctionNode.java */

package pt.data.parser;
import java.lang.reflect.*;

public class ASTPtFunctionNode extends ASTPtSimpleNode {
    String funcName;

    protected pt.data.Token _resolveType() throws Exception {
        // assuming only double types as function arguments
        for (int i = 0; i< jjtGetNumChildren(); i++){
            if (!( childTokens[i] instanceof pt.data.DoubleToken)) {
                throw new Exception();
            }
        }
        return new pt.data.DoubleToken(0);
    }

    protected void _resolveValue() throws Exception {
        int args = jjtGetNumChildren();
        Class[] argTypes = new Class[args];
        Double[] argValues = new Double[args];
        try {
            for (int i = 0; i<args; i++) {
                argValues[i] = new Double(((pt.data.DoubleToken)childTokens[i]).doubleValue()); 
                argTypes[i] = argValues[i].TYPE;
            }
            // Note: Java makes a dintinction between the class objects
            // for double & Double...
            Class tmp = Class.forName("java.lang.Math");
            Method m = tmp.getMethod(funcName, argTypes);
            Double result = (Double)m.invoke(tmp, argValues);
            _ptToken = new pt.data.DoubleToken(result.doubleValue());
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();
            for (int i=0; i<args; i++) {
                if (i==0) {
            sb.append(argValues[i].doubleValue());
                } else {
                    sb.append(", " + argValues[i].doubleValue());
                }
            }  
            System.out.print("Function "+funcName+"(");
            System.out.println(sb + ") cannot be executed with given arguments");
            throw new Exception();
        } 
    }


  public ASTPtFunctionNode(int id) {
    super(id);
  }

  public ASTPtFunctionNode(PtParser p, int id) {
    super(p, id);
  }

  public static Node jjtCreate(int id) {
      return new ASTPtFunctionNode(id);
  }

  public static Node jjtCreate(PtParser p, int id) {
      return new ASTPtFunctionNode(p, id);
  }
}
