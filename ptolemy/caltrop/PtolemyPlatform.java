
package ptolemy.caltrop;

import caltrop.interpreter.Context;
import caltrop.interpreter.Function;
import caltrop.interpreter.InterpreterException;
import caltrop.interpreter.Procedure;
import caltrop.interpreter.environment.Environment;
import caltrop.interpreter.environment.HashEnvironment;
import caltrop.interpreter.java.ClassObject;
import ptolemy.caltrop.util.IntegerList;
import ptolemy.caltrop.util.PtArrayList;
import ptolemy.caltrop.util.PtCalFunction;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The PtolemyPlatform class contains code that configures the CAL interpreter infrastructure for use
 * inside the Ptolemy II software. In particular, it contains a context and a method that creates the
 * global environment to be used with Ptolemy.
 *
 * @author J�rn W. Janneck <janneck@eecs.berkeley.edu>, Christopher Chang <cbc@eecs.berkeley.edu>
 */

public class PtolemyPlatform {
    /**
     * This Context represents the Ptolemy II system of data objects in a way that can be used by the
     * {@link ptolemy.caltrop.ddi.util.DataflowActorInterpreter DataflowActorInterpreter}.
     * The interpreter infrastructure relies on a client-provided context for any manipulation of data values.
     *
     * @see ptolemy.caltrop.ddi.util.DataflowActorInterpreter
     * @see caltrop.interpreter.ExprEvaluator
     * @see caltrop.interpreter.StmtEvaluator
     */
    public static final Context _theContext = new Context() {
        public Object applyFunction(Object function, Object[] args) { // TODO: perhaps need to optimize array creation
            try {
                if (function instanceof ObjectToken) {
                    return ((Function) ((ObjectToken) function).getValue()).apply(args);
                } else {
                    return ((FunctionToken) function).apply(Arrays.asList(args));
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new InterpreterException("Cannot apply function.", e);
            }
        }

        public boolean isFunction(Object a) {
            return true;
        }

        public void callProcedure(Object procedure, Object[] args) // TODO: perhaps need to optimize array creation
        {
            try {
                ObjectToken pToken = (ObjectToken) procedure;
                Procedure p = (Procedure) pToken.getValue();
                p.call(args);
            } catch (Exception e) {
                e.printStackTrace();
                throw new InterpreterException("Error in procedure call.", e);
            }
        }

        public boolean booleanValue(Object b) {
            try {
                return ((BooleanToken) b).booleanValue();
            } catch (Exception e) {
                throw new InterpreterException("Cannot test token, expected boolean.", e);
            }
        }

        public Object createClass(Class c) {
            try {
                return new ObjectToken(new ClassObject(c, this));
            } catch (IllegalActionException e) {
                throw new InterpreterException("Cannot create class token.");
            }
        }

        public Object getLocation(Object structure, Object[] location) { // FIXME
            return null;
        }

        public void setLocation(Object structure, Object[] location, Object value) { // FIXME
        }

        public Object createBoolean(boolean b) {
            return b ? BooleanToken.TRUE : BooleanToken.FALSE;
        }

        public Object createCharacter(char c) {
            return new StringToken(Character.toString(c));
        }

        public Object createFunction(Function f) {

            Type[] argTypes = new Type[f.arity()];
            for (int i = 0; i < argTypes.length; i++)
                argTypes[i] = BaseType.UNKNOWN;
            return new FunctionToken(new PtCalFunction(f), new FunctionType(argTypes, BaseType.UNKNOWN));
        }

        public Object createProcedure(Procedure p) {
            try {
                return new ObjectToken(p);
            } catch (IllegalActionException e) {
                throw new InterpreterException("Could not create procedure token.", e);
            }
        }

        public Object createInteger(String s) {
            try {
                return new IntToken(s);
            } catch (IllegalActionException e) {
                throw new InterpreterException("Cannot create integer token from string: '" + s + "'.");
            }

        }

        public Object createInteger(int n) {
            return new IntToken(n);
        }

        public int intValue(Object o) {
            try {
                return ((IntToken) o).intValue();
            } catch (Exception e) {
                throw new InterpreterException("Cannot cast token, expected int.", e);
            }
        }

        // FIXME very preliminary. what about FunctionToken? also, how will reflection work on methods that need bytes, etc.
        public Class toJavaClass(Object o) {
            if (o == null) {
                return null;
            } else if (o instanceof BooleanToken) {
                return boolean.class;
            } else if (o instanceof DoubleToken) {
                return double.class;
            } else if (o instanceof IntToken) {
                return int.class;
            } else if (o instanceof StringToken) {
                return String.class;
            } else if (o instanceof ObjectToken) {
                return ((ObjectToken) o).getValue().getClass();
            } else if (o instanceof Token) {
                return o.getClass();
            } else throw new InterpreterException("Unrecognized Token type in toClass:" + o.getClass().toString());
        }

        public Object toJavaObject(Object o) {
            if (o instanceof BooleanToken) {
                return new Boolean(booleanValue(o));
            } else if (o instanceof DoubleToken) {
                return new Double(realValue(o));
            } else if (o instanceof IntToken) {
                return new Integer(intValue(o));
            } else if (o instanceof StringToken) {
                return stringValue(o);
            } else if (o instanceof ObjectToken) {
                return ((ObjectToken) o).getValue();
            } else if (o instanceof Token) {
                return o;
            } else throw new InterpreterException("Unrecognized Token type in toClass:" + o.getClass().toString());
        }

        public Object fromJavaObject(Object o) {
            try {
                if (o instanceof Token) {
                    return o;
                } else if (o instanceof Boolean) {
                    return new BooleanToken(((Boolean) o).booleanValue());
                } else if (o instanceof Double) {
                    return new DoubleToken(((Double) o).doubleValue());
                } else if (o instanceof Integer) {
                    return new IntToken(((Integer) o).intValue());
                } else if (o instanceof String) {
                    return new StringToken((String) o);
                } else {
                    return new ObjectToken(o);
                }
            } catch (IllegalActionException e) {
                throw new InterpreterException("Couldn't create ObjectToken from Java Object " + o.toString());
            }
        }

        public Object createNull() {
            try {
                return new ObjectToken(null);
            } catch (IllegalActionException e) {
                throw new InterpreterException("Cannot create null token.");
            }
        }

        public Object createReal(String s) {
            try {
                return new DoubleToken(s);
            } catch (IllegalActionException e) {
                throw new InterpreterException("Cannot create double token from string: '" + s + "'.");
            }
        }

        public double realValue(Object o) {
            try {
                return ((DoubleToken) o).doubleValue();
            } catch (Exception e) {
                throw new InterpreterException("Cannot cast token, expected double.", e);
            }
        }


        public Object createString(String s) {
            return new StringToken(s);
        }

        public String stringValue(Object o) {
            try {
                return ((StringToken) o).stringValue();
            } catch (Exception e) {
                throw new InterpreterException("Cannot cast token, expected string.", e);
            }
        }

        // FIXMELATER: implement collection classes
        public Object createList(List a) {
            try {
                return new ObjectToken(a);
            } catch (IllegalActionException e) {
                throw new InterpreterException("Cannot create list token.");
            }
        }

        public List listValue(Object o) {
            if (o instanceof ArrayToken) {
                return new PtArrayList((ArrayToken) o);
            } else {
                try {
                    return (List) ((ObjectToken) o).getValue();
                } catch (Exception e) {
                    throw new InterpreterException("Cannot cast token, expected a List.");
                }
            }
        }

        public Object createMap(Map m) {
            try {
                return new ObjectToken(m);
            } catch (IllegalActionException e) {
                throw new InterpreterException("Cannot create map token.");
            }
        }

        public Collection getCollection(Object a) {
            try {
                return (Collection)((ObjectToken)a).getValue();
            }
            catch (Exception exc) {
                throw new InterpreterException("Could not extract collection from token: " + a.toString(), exc);
            }
        }

        public Map getMap(Object a) {
            try {
                return (Map)((ObjectToken)a).getValue();
            }
            catch (Exception exc) {
                throw new InterpreterException("Could not extract map from token: " + a.toString(), exc);
            }
        }

        public Object applyMap(Object map, Object arg) {
            Map m = getMap(map);
            return m.get(arg);
        }

        public Object createSet(Set s) {
            try {
                return new ObjectToken(s);
            } catch (IllegalActionException e) {
                throw new InterpreterException("Cannot create set token.");
            }
        }
    };

    public static Environment _createGlobalEnvironment() {
        Environment env = new HashEnvironment(_theContext);

        env.bind("println", _theContext.createProcedure(new Procedure() {
            public void call(Object[] args) {
                System.out.println(args[0]);
            }

            public int arity() {
                return 1;
            }
        }));

        env.bind("SOP", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    System.out.println(args[0]);
                    return args[0];
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$not': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 1;
            }
        }));

        env.bind("Integers", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    int a = _theContext.intValue(args[0]);
                    int b = _theContext.intValue(args[1]);
                    List res = (b < a) ? Collections.EMPTY_LIST : new IntegerList(_theContext, a, b);
                    return _theContext.createList(res);
                } catch (Exception exc) {
                    throw new InterpreterException("Function 'Integers': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$not", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    BooleanToken b = (BooleanToken) args[0];
                    ;
                    return b.not();
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$not': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 1;
            }
        }));

        env.bind("$and", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    BooleanToken a = (BooleanToken) args[0];
                    BooleanToken b = (BooleanToken) args[1];
                    return a.and(b);
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$and': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$or", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    BooleanToken a = (BooleanToken) args[0];
                    BooleanToken b = (BooleanToken) args[1];
                    return a.or(b);
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$or': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$eq", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];
                    return a.isEqualTo(b);
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$add': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$lt", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    ScalarToken a = (ScalarToken) args[0];
                    ScalarToken b = (ScalarToken) args[1];
                    return a.isLessThan(b);
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$lt': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$le", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    ScalarToken a = (ScalarToken) args[0];
                    ScalarToken b = (ScalarToken) args[1];
                    if (a.isLessThan(b).booleanValue() || a.isEqualTo(b).booleanValue())
                        return BooleanToken.TRUE;
                    else
                        return BooleanToken.FALSE;
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$lt': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$gt", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    ScalarToken a = (ScalarToken) args[0];
                    ScalarToken b = (ScalarToken) args[1];
                    return a.isGreaterThan(b);
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$lt': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$ge", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    ScalarToken a = (ScalarToken) args[0];
                    ScalarToken b = (ScalarToken) args[1];
                    if (a.isGreaterThan(b).booleanValue() || a.isEqualTo(b).booleanValue())
                        return BooleanToken.TRUE;
                    else
                        return BooleanToken.FALSE;
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$lt': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$negate", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    ScalarToken a = (ScalarToken) args[0];

                    return a.zero().subtract(a);
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$negate': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$add", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];

                    if (a instanceof ObjectToken && b instanceof ObjectToken) {
                        Object oa = ((ObjectToken) a).getValue();
                        Object ob = ((ObjectToken) b).getValue();
                        if (oa instanceof Collection && ob instanceof Collection) {
                            Collection result;
                            if (oa instanceof Set)
                                result = new HashSet((Set) oa);
                            else if (oa instanceof List)
                                result = new ArrayList((List) oa);
                            else
                                throw new RuntimeException("WTF");
                            result.addAll((Collection) ob);
                            return new ObjectToken(result);
                        } else {
                            throw new InterpreterException("Unknown object types.");
                        }
                    } else {
                        return a.add(b);
                    }
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$add': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$mul", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];
                    if (a instanceof ObjectToken && b instanceof ObjectToken) {
                        Object oa = ((ObjectToken) a).getValue();
                        Object ob = ((ObjectToken) b).getValue();
                        if (oa instanceof Set && ob instanceof Collection) {
                            Set result = new HashSet((Set) oa);
                            result.retainAll((Collection) ob);
                            return new ObjectToken(result);
                        } else {
                            throw new InterpreterException("Cannot perform set intersection: bad object types.");
                        }
                    } else {
                        return a.multiply(b);
                    }
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$mul': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$sub", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];

                    if (a instanceof ObjectToken && b instanceof ObjectToken) {
                        Object oa = ((ObjectToken) a).getValue();
                        Object ob = ((ObjectToken) b).getValue();
                        if (oa instanceof Collection && ob instanceof Collection) {
                            Collection result;
                            if (oa instanceof Set)
                                result = new HashSet((Set) oa);
                            else if (oa instanceof List)
                                result = new ArrayList((List) oa);
                            else
                                throw new RuntimeException("WTF");
                            result.removeAll((Collection) ob);
                            return new ObjectToken(result);
                        } else {
                            throw new InterpreterException("Unknown object types.");
                        }
                    } else {
                        return a.subtract(b);
                    }
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$sub': Cannot apply.", exc);
                }

            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$div", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];
                    return a.divide(b);
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$div': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$mod", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];
                    return a.modulo(b);
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$mod': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$size", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    if (a instanceof ObjectToken) {
                        Object oa = ((ObjectToken) a).getValue();
                        if (oa instanceof Collection) {
                            return new IntToken(((Collection) oa).size());
                        } else {
                            throw new InterpreterException("Can only take $size of set, list, or map.");
                        }
                    } else if (a instanceof ArrayToken) {
                        return _theContext.createInteger(((ArrayToken) a).length());
                    } else {
                        throw new InterpreterException("$size needs an ObjectToken or ArrayToken.");
                    }
                } catch (Exception exc) {
                    throw new InterpreterException("Function '$size': Cannot apply.", exc);
                }
            }

            public int arity() {
                return 1;
            }
        }));

        env.bind("$createList", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    Collection c = _theContext.getCollection(args[0]);
                    FunctionToken f = (FunctionToken) args[1];
                    Object [] argument = new Object [1];
                    List res = new ArrayList();
                    for (Iterator i = c.iterator(); i.hasNext(); ) {
                        argument[0] = i.next();
                        Object listFragment = _theContext.applyFunction(f, argument);
                        res.addAll(_theContext.getCollection(listFragment));
                    }
                    return _theContext.createList(res);
                }
                catch (Exception exc) {
                    throw new InterpreterException("Cannot create list.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$createSet", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    Collection c = _theContext.getCollection(args[0]);
                    FunctionToken f = (FunctionToken) args[1];
                    Object [] argument = new Object [1];
                    Set res = new HashSet();
                    for (Iterator i = c.iterator(); i.hasNext(); ) {
                        argument[0] = i.next();
                        Object setFragment = _theContext.applyFunction(f, argument);
                        res.addAll(_theContext.getCollection(setFragment));
                    }
                    return _theContext.createSet(res);
                }
                catch (Exception exc) {
                    throw new InterpreterException("Cannot create set.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$createMap", _theContext.createFunction(new Function() {
            public Object apply(Object[] args) {
                try {
                    Collection c = _theContext.getCollection(args[0]);
                    FunctionToken f = (FunctionToken) args[1];
                    Object [] argument = new Object [1];
                    Map res = new HashMap();
                    for (Iterator i = c.iterator(); i.hasNext(); ) {
                        argument[0] = i.next();
                        Object mapFragment = _theContext.applyFunction(f, argument);
                        res.putAll(_theContext.getMap(mapFragment));
                    }
                    return _theContext.createMap(res);
                }
                catch (Exception exc) {
                    throw new InterpreterException("Cannot create map.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        env.bind("$iterate", _theContext.createProcedure(new Procedure() {
            public void call(Object[] args) {
                try {
                    Collection c = _theContext.getCollection(args[0]);
                    Object proc = args[1];
                    Object [] argument = new Object [1];
                    for (Iterator i = c.iterator(); i.hasNext(); ) {
                        argument[0] = i.next();
                        _theContext.callProcedure(proc, argument);
                    }
                }
                catch (Exception exc) {
                    throw new InterpreterException("Cannot iterate.", exc);
                }
            }

            public int arity() {
                return 2;
            }
        }));

        return env;
    }
}


