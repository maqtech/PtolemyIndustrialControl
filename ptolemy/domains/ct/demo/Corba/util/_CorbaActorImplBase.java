/*
 * File: ../../../../../PTOLEMY/DOMAINS/CT/DEMO/CORBA/UTIL/_CORBAACTORIMPLBASE.JAVA
 * From: CORBAACTOR.IDL
 * Date: Thu Jul 29 14:22:20 1999
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package ptolemy.domains.ct.demo.Corba.util;
public abstract class _CorbaActorImplBase extends org.omg.CORBA.DynamicImplementation implements ptolemy.domains.ct.demo.Corba.util.CorbaActor {
    // Constructor
    public _CorbaActorImplBase() {
        super();
    }
    // Type strings for this class and its superclases
    private static final String _type_ids[] = {
        "IDL:util/CorbaActor:1.0"
    };

    public String[] _ids() { return (String[]) _type_ids.clone(); }

    private static java.util.Dictionary _methods = new java.util.Hashtable();
    static {
        _methods.put("fire", new java.lang.Integer(0));
        _methods.put("getParameter", new java.lang.Integer(1));
        _methods.put("initialize", new java.lang.Integer(2));
        _methods.put("hasData", new java.lang.Integer(3));
        _methods.put("hasParameter", new java.lang.Integer(4));
        _methods.put("hasPort", new java.lang.Integer(5));
        _methods.put("setPortWidth", new java.lang.Integer(6));
        _methods.put("postfire", new java.lang.Integer(7));
        _methods.put("prefire", new java.lang.Integer(8));
        _methods.put("setParameter", new java.lang.Integer(9));
        _methods.put("stopFire", new java.lang.Integer(10));
        _methods.put("terminate", new java.lang.Integer(11));
        _methods.put("transferInput", new java.lang.Integer(12));
        _methods.put("transferOutput", new java.lang.Integer(13));
        _methods.put("wrapup", new java.lang.Integer(14));
    }
    // DSI Dispatch call
    public void invoke(org.omg.CORBA.ServerRequest r) {
        switch (((java.lang.Integer) _methods.get(r.operation())).intValue()) {
        case 0: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.fire
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                r.arguments(_list);
                try {
                    this.fire();
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __return = _orb().create_any();
                __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
                r.set_result(__return);
            }
            break;
        case 1: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.getParameter
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                org.omg.CORBA.Any _paramName = _orb().create_any();
                _paramName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
                _list.add_value("paramName", _paramName, org.omg.CORBA.ARG_IN.value);
                r.arguments(_list);
                String paramName;
                paramName = _paramName.extract_string();
                String ___result;
                try {
                    ___result = this.getParameter(paramName);
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaUnknownParamException e1) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaUnknownParamExceptionHelper.insert(_except, e1);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __result = _orb().create_any();
                __result.insert_string(___result);
                r.set_result(__result);
            }
            break;
        case 2: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.initialize
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                r.arguments(_list);
                try {
                    this.initialize();
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __return = _orb().create_any();
                __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
                r.set_result(__return);
            }
            break;
        case 3: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.hasData
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                org.omg.CORBA.Any _portName = _orb().create_any();
                _portName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
                _list.add_value("portName", _portName, org.omg.CORBA.ARG_IN.value);
                org.omg.CORBA.Any _portIndex = _orb().create_any();
                _portIndex.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short));
                _list.add_value("portIndex", _portIndex, org.omg.CORBA.ARG_IN.value);
                r.arguments(_list);
                String portName;
                portName = _portName.extract_string();
                short portIndex;
                portIndex = _portIndex.extract_short();
                boolean ___result;
                try {
                    ___result = this.hasData(portName, portIndex);
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIndexOutofBoundException e1) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIndexOutofBoundExceptionHelper.insert(_except, e1);
                    r.set_exception(_except);
                    return;
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException e2) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortExceptionHelper.insert(_except, e2);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __result = _orb().create_any();
                __result.insert_boolean(___result);
                r.set_result(__result);
            }
            break;
        case 4: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.hasParameter
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                org.omg.CORBA.Any _paramName = _orb().create_any();
                _paramName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
                _list.add_value("paramName", _paramName, org.omg.CORBA.ARG_IN.value);
                r.arguments(_list);
                String paramName;
                paramName = _paramName.extract_string();
                boolean ___result;
                ___result = this.hasParameter(paramName);
                org.omg.CORBA.Any __result = _orb().create_any();
                __result.insert_boolean(___result);
                r.set_result(__result);
            }
            break;
        case 5: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.hasPort
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                org.omg.CORBA.Any _portName = _orb().create_any();
                _portName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
                _list.add_value("portName", _portName, org.omg.CORBA.ARG_IN.value);
                org.omg.CORBA.Any _isInput = _orb().create_any();
                _isInput.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
                _list.add_value("isInput", _isInput, org.omg.CORBA.ARG_IN.value);
                org.omg.CORBA.Any _isOutput = _orb().create_any();
                _isOutput.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
                _list.add_value("isOutput", _isOutput, org.omg.CORBA.ARG_IN.value);
                org.omg.CORBA.Any _isMultiport = _orb().create_any();
                _isMultiport.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
                _list.add_value("isMultiport", _isMultiport, org.omg.CORBA.ARG_IN.value);
                r.arguments(_list);
                String portName;
                portName = _portName.extract_string();
                boolean isInput;
                isInput = _isInput.extract_boolean();
                boolean isOutput;
                isOutput = _isOutput.extract_boolean();
                boolean isMultiport;
                isMultiport = _isMultiport.extract_boolean();
                boolean ___result;
                ___result = this.hasPort(portName, isInput, isOutput, isMultiport);
                org.omg.CORBA.Any __result = _orb().create_any();
                __result.insert_boolean(___result);
                r.set_result(__result);
            }
            break;
        case 6: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.setPortWidth
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                org.omg.CORBA.Any _portName = _orb().create_any();
                _portName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
                _list.add_value("portName", _portName, org.omg.CORBA.ARG_IN.value);
                org.omg.CORBA.Any _width = _orb().create_any();
                _width.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short));
                _list.add_value("width", _width, org.omg.CORBA.ARG_IN.value);
                r.arguments(_list);
                String portName;
                portName = _portName.extract_string();
                short width;
                width = _width.extract_short();
                try {
                    this.setPortWidth(portName, width);
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException e1) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortExceptionHelper.insert(_except, e1);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __return = _orb().create_any();
                __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
                r.set_result(__return);
            }
            break;
        case 7: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.postfire
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                r.arguments(_list);
                boolean ___result;
                try {
                    ___result = this.postfire();
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __result = _orb().create_any();
                __result.insert_boolean(___result);
                r.set_result(__result);
            }
            break;
        case 8: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.prefire
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                r.arguments(_list);
                boolean ___result;
                try {
                    ___result = this.prefire();
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __result = _orb().create_any();
                __result.insert_boolean(___result);
                r.set_result(__result);
            }
            break;
        case 9: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.setParameter
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                org.omg.CORBA.Any _paramName = _orb().create_any();
                _paramName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
                _list.add_value("paramName", _paramName, org.omg.CORBA.ARG_IN.value);
                org.omg.CORBA.Any _paramValue = _orb().create_any();
                _paramValue.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
                _list.add_value("paramValue", _paramValue, org.omg.CORBA.ARG_IN.value);
                r.arguments(_list);
                String paramName;
                paramName = _paramName.extract_string();
                String paramValue;
                paramValue = _paramValue.extract_string();
                try {
                    this.setParameter(paramName, paramValue);
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaUnknownParamException e1) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaUnknownParamExceptionHelper.insert(_except, e1);
                    r.set_exception(_except);
                    return;
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueException e2) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueExceptionHelper.insert(_except, e2);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __return = _orb().create_any();
                __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
                r.set_result(__return);
            }
            break;
        case 10: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.stopFire
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                r.arguments(_list);
                try {
                    this.stopFire();
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __return = _orb().create_any();
                __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
                r.set_result(__return);
            }
            break;
        case 11: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.terminate
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                r.arguments(_list);
                try {
                    this.terminate();
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __return = _orb().create_any();
                __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
                r.set_result(__return);
            }
            break;
        case 12: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.transferInput
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                org.omg.CORBA.Any _portName = _orb().create_any();
                _portName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
                _list.add_value("portName", _portName, org.omg.CORBA.ARG_IN.value);
                org.omg.CORBA.Any _portIndex = _orb().create_any();
                _portIndex.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short));
                _list.add_value("portIndex", _portIndex, org.omg.CORBA.ARG_IN.value);
                org.omg.CORBA.Any _tokenValue = _orb().create_any();
                _tokenValue.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
                _list.add_value("tokenValue", _tokenValue, org.omg.CORBA.ARG_IN.value);
                r.arguments(_list);
                String portName;
                portName = _portName.extract_string();
                short portIndex;
                portIndex = _portIndex.extract_short();
                String tokenValue;
                tokenValue = _tokenValue.extract_string();
                try {
                    this.transferInput(portName, portIndex, tokenValue);
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException e1) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortExceptionHelper.insert(_except, e1);
                    r.set_exception(_except);
                    return;
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIndexOutofBoundException e2) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIndexOutofBoundExceptionHelper.insert(_except, e2);
                    r.set_exception(_except);
                    return;
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueException e3) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueExceptionHelper.insert(_except, e3);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __return = _orb().create_any();
                __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
                r.set_result(__return);
            }
            break;
        case 13: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.transferOutput
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                org.omg.CORBA.Any _portName = _orb().create_any();
                _portName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
                _list.add_value("portName", _portName, org.omg.CORBA.ARG_IN.value);
                org.omg.CORBA.Any _portIndex = _orb().create_any();
                _portIndex.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short));
                _list.add_value("portIndex", _portIndex, org.omg.CORBA.ARG_IN.value);
                r.arguments(_list);
                String portName;
                portName = _portName.extract_string();
                short portIndex;
                portIndex = _portIndex.extract_short();
                String ___result;
                try {
                    ___result = this.transferOutput(portName, portIndex);
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException e1) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortExceptionHelper.insert(_except, e1);
                    r.set_exception(_except);
                    return;
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIndexOutofBoundException e2) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIndexOutofBoundExceptionHelper.insert(_except, e2);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __result = _orb().create_any();
                __result.insert_string(___result);
                r.set_result(__result);
            }
            break;
        case 14: // ptolemy.domains.ct.demo.Corba.util.CorbaActor.wrapup
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                r.arguments(_list);
                try {
                    this.wrapup();
                }
                catch (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException e0) {
                    org.omg.CORBA.Any _except = _orb().create_any();
                    ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                    r.set_exception(_except);
                    return;
                }
                org.omg.CORBA.Any __return = _orb().create_any();
                __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
                r.set_result(__return);
            }
            break;
        default:
            throw new org.omg.CORBA.BAD_OPERATION(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        }
    }
}
