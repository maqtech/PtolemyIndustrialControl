/* Generated By:JJTree: Do not edit this line. ASTPtProductNode.java */

package pt.data.parser;

import collections.LinkedList;

public class ASTPtProductNode extends ASTPtSimpleNode {
    protected LinkedList _tokenList = new LinkedList();

    protected void _resolveValue() throws Exception {
        int num =  jjtGetNumChildren();
        if (num ==1) {
            String str = childTokens[0].toString();
            _ptToken.fromString(String.valueOf(str));
            return;
        }
        if (num != ( _tokenList.size() +1) ) {
            throw new ParseException();
        }
        _ptToken = _ptToken.add(_ptToken, childTokens[0]);
        int size = _tokenList.size();
        for (int i=0; i<size; i++) {
            Token x = (Token)_tokenList.take();
            if (x.image.compareTo("*") == 0) {
                _ptToken = _ptToken.multiply(_ptToken, childTokens[i+1]);
            } else if (x.image.compareTo("/") == 0) {
                _ptToken = _ptToken.divide(_ptToken, childTokens[i+1]);
            } else {
                throw new Exception();
            }
        }
    }

  public ASTPtProductNode(int id) {
    super(id);
  }

  public ASTPtProductNode(PtParser p, int id) {
    super(p, id);
  }

  public static Node jjtCreate(int id) {
      return new ASTPtProductNode(id);
  }

  public static Node jjtCreate(PtParser p, int id) {
      return new ASTPtProductNode(p, id);
  }
}
