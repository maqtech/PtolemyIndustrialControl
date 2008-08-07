/*

 Copyright (c) 2007-2008 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

package ptolemy.apps.naomi;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ptolemy.actor.Manager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.UserActorLibrary;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.VergilErrorHandler;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Connector extends MoMLApplication {

    public Connector(String basePath, String[] args) throws Exception {
        super(basePath, args);
    }

    public Connector(String[] args) throws Exception {
        super(args);
    }

    public synchronized void executionError(Manager manager, Throwable throwable) {
        super.executionError(manager, throwable);

        System.out.println("Execution error: " + throwable.getMessage());
    }

    public synchronized void executionFinished(Manager manager) {
        super.executionFinished(manager);

        if (_command == Command.EXECUTE) {
            try {
                _saveAttributes(_inModel, _attributesPath, true);
                _outputModel(_inModel);
                _outputInterface();
                StringUtilities.exit(0);
            } catch (IllegalActionException e) {
                throw new InternalErrorException(null, e,
                        "Unable to save attributes.");
            }
        }
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Connector(args).processCommand();
                } catch (Throwable t) {
                    MessageHandler.error("Command failed", t);
                    System.err.print(KernelException.stackTraceToString(t));
                    System.exit(1);
                }
            }
        });
    }

    public void processCommand() throws IllegalActionException {
        if (_root == null) {
            throw new IllegalActionException("NAOMI root directory is not "
                    + "specified with -root argument.");
        } else if (!_pathExists(_root)) {
            throw new IllegalActionException("NAOMI root directory \""
                    + _root + "\" does not exist.");
        }

        if (_command == null) {
            return;
        }

        if (_inModel == null || models().isEmpty()) {
            throw new IllegalActionException("No input model is specified.");
        }

        // We now decide to load the interface information from the model, and
        // generate the interface files in the SVN for use in the NAOMI client.
        /*File interfaceFile = _getInterfaceFile();
        _loadInterfaceFromSVN(interfaceFile);*/

        _loadInterfaceFromModel(_inModel);

        if (_command != Command.LIST) {
            if (!_pathExists(_attributesPath)) {
                throw new IllegalActionException("Attributes directory \""
                        + _attributesPath + "\" does not exist.");
            }
        }

        switch (_command) {
        case EXECUTE:
            _loadAttributes(_inModel, _attributesPath, true);
            try {
                _openModel(_inModel);
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(null, e,
                        "Unable to open model.");
            }
            runModels();
            System.out.println("Model execution started.");
            break;

        case LIST:
            HashMap<Attribute, String> sync = new HashMap<Attribute, String>();
            HashMap<Attribute, String> load = new HashMap<Attribute, String>();
            HashMap<Attribute, String> save = new HashMap<Attribute, String>();
            _list(_inModel, sync, load, save);
            for (String attr : sync.values()) {
                System.out.println("Sync: " + attr);
            }
            for (String attr : load.values()) {
                System.out.println("Load: " + attr);
            }
            for (String attr : save.values()) {
                System.out.println("Save: " + attr);
            }
            _outputInterface();
            break;

        case LOAD:
            _loadAttributes(_inModel, _attributesPath, true);
            _outputModel(_inModel);
            _outputInterface();
            break;

        case SAVE:
            _saveAttributes(_inModel, _attributesPath, true);
            _outputModel(_inModel);
            _outputInterface();
            break;

        case SYNC:
            _loadAttributes(_inModel, _attributesPath, false);
            _saveAttributes(_inModel, _attributesPath, false);
            _outputModel(_inModel);
            _outputInterface();
            break;
        }
    }

    public static final String[][] COMMAND_OPTIONS = new String[][] {
        {"-cmd", "<command>"},
        {"    ", "execute (load NAOMI attributes, execute the model, and " +
                 "save NAOMI attributes"},
        {"    ", "list (list NAOMI attributes)"},
        {"    ", "load (load NAOMI attributes (irregard of modification time)"},
        {"    ", "save (save NAOMI attributes (irregard of modification time)"},
        {"    ", "sync (synchronize NAOMI attributes)"},
        {"-in", "<input model>"},
        {"-out", "<output model>"},
        {"-owner", "<owner>"},
        {"-root", "<NAOMI root directory>"}
    };

    public static final String[][] NAMESPACES = new String[][] {
        {"att", "http://www.atl.lmco.com/naomi/attributes"},
        {"inf", "http://www.atl.lmco.com/naomi/interfaces"},
        {"xsi", "http://www.w3.org/2001/XMLSchema-instance"}
    };

    public class LinkNaomiAttributeAction extends AbstractAction {

        public LinkNaomiAttributeAction(PtolemyFrame frame) {
            super("Link NAOMI Attribute");
            putValue("tooltip", "Link a NAOMI attribute to a parameter");

            _frame = frame;
        }

        public void actionPerformed(ActionEvent e) {
            NamedObj model = _frame.getModel();

            List<Attribute> attributes = _linkableAttributes(model);
            Iterator<Attribute> iterator = attributes.iterator();
            while (iterator.hasNext()) {
                Attribute attribute = iterator.next();
                if (!attribute.attributeList(NaomiParameter.class).isEmpty()
                        || attribute.getName().startsWith("_")) {
                    iterator.remove();
                }
            }

            if (attributes.isEmpty()) {
                MessageHandler.message("No more parameter can be linked to a "
                        + "NAOMI attribute.");
                return;
            }

            Query query = new Query();

            String[] choices = new String[attributes.size()];
            int i = 0;
            for (Attribute attribute : attributes) {
                choices[i++] = attribute.getName(model);
            }
            query.addChoice("parameter", "Parameter", choices, choices[0]);

            NaomiParameter.Method[] methods = NaomiParameter.Method.values();
            choices = new String[methods.length];
            for (i = 0; i < methods.length; i++) {
                choices[i] = methods[i].toString();
            }
            query.addChoice("method", "Method", choices, choices[0]);
            query.addLine("naomiName", "NAOMI attribute", "");
            query.addLine("unit", "Unit", "");
            query.addLine("documentation", "Documentation", "");
            ComponentDialog dialog = new ComponentDialog(_frame,
                    (String) getValue("tooltip"), query);
            if (dialog.buttonPressed().equals("OK")) {
                String paramName = query.getStringValue("parameter");
                String methodName = query.getStringValue("method");
                NaomiParameter.Method method = null;
                for (NaomiParameter.Method m : NaomiParameter.Method.values()) {
                    if (m.toString().equals(methodName)) {
                        method = m;
                        break;
                    }
                }
                String naomiName = query.getStringValue("naomiName");
                String unit = query.getStringValue("unit");
                String documentation = query.getStringValue("documentation");

                if (naomiName.equals("")) {
                    MessageHandler.error("The \"NAOMI attribute\" must not be "
                            + "empty, and must contain a valid name of NAOMI "
                            + "attribute.");
                    return;
                }

                if (method == NaomiParameter.Method.GET) {
                    File attributesPath = new File(_root, "attributes");
                    try {
                        _loadAttribute(attributesPath, naomiName);
                    } catch (IllegalActionException e1) {
                        MessageHandler.error("Unable to locate NAOMI attribute "
                                + "with name \"" + naomiName + "\".");
                        return;
                    }
                }

                Attribute attribute = model.getAttribute(paramName);
                String expression = NaomiParameter.getExpression(method,
                        naomiName, new Date(), unit, documentation);
                String moml = "<property name=\"" +
                        attribute.uniqueName("naomi") + "\" class=\"" +
                        NaomiParameter.class.getName() + "\" value=\"" +
                        StringUtilities.unescapeForXML(expression) + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        attribute, moml);
                request.setUndoable(true);
                attribute.requestChange(request);
            }
        }

        private PtolemyFrame _frame;
    }

    public class ListAction extends AbstractAction {

        public ListAction(PtolemyFrame frame) {
            super("List");
            putValue("tooltip", "List NAOMI attributes");

            _frame = frame;
        }

        public void actionPerformed(ActionEvent e) {
            NamedObj model = _frame.getModel();

            _loadInterfaceFromModel(model);

            HashMap<Attribute, String> sync = new HashMap<Attribute, String>();
            HashMap<Attribute, String> load = new HashMap<Attribute, String>();
            HashMap<Attribute, String> save = new HashMap<Attribute, String>();
            _list(model, sync, load, save);
            try {
                _outputInterface();
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(ex);
            }

            Query query = new Query();
            for (Map.Entry<Attribute, String> entry : sync.entrySet()) {
                String paramName = entry.getKey().getName();
                query.addDisplay(paramName, paramName, entry.getValue());
            }
            for (Map.Entry<Attribute, String> entry : load.entrySet()) {
                String paramName = entry.getKey().getName();
                query.addDisplay(paramName, paramName, entry.getValue());
            }
            for (Map.Entry<Attribute, String> entry : save.entrySet()) {
                String paramName = entry.getKey().getName();
                query.addDisplay(paramName, paramName, entry.getValue());
            }
            new ComponentDialog(_frame, (String) getValue("tooltip"), query);
        }

        private PtolemyFrame _frame;
    }

    public class LoadAction extends AbstractAction {

        public LoadAction(PtolemyFrame frame) {
            super("Load");
            putValue("tooltip", "Load NAOMI attributes");

            _frame = frame;
        }

        public void actionPerformed(ActionEvent e) {
            final NamedObj model = _frame.getModel();
            model.requestChange(new ChangeRequest(this,
                    "Load NAOMI attributes") {
                protected void _execute() throws Exception {
                    try {
                        synchronized (model) {
                            _loadInterfaceFromModel(model);
                            _undoable = true;
                            _mergeWithPrevious = false;
                            _loadAttributes(model, _getAttributesPath(), true);
                            _outputInterface();
                        }
                    } catch (IllegalActionException e) {
                        throw new InternalErrorException(e);
                    }
                }
            });
        }

        private PtolemyFrame _frame;
    }

    public static class MappedNamespaceContext implements NamespaceContext {

        public MappedNamespaceContext(Map<String, String> map) {
            _map = map;
        }

        public MappedNamespaceContext(String[][] map) {
            _map = new HashMap<String, String>();
            for (String[] entry : map) {
                _map.put(entry[0], entry[1]);
            }
        }

        public String getNamespaceURI(String prefix) {
            return _map.get(prefix);
        }

        public String getPrefix(String namespaceURI) {
            for (Map.Entry<String, String> entry : _map.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        public Iterator<String> getPrefixes(String namespaceURI) {
            List<String> keys = new LinkedList<String>();
            for (Map.Entry<String, String> entry : _map.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    keys.add(entry.getKey());
                }
            }
            return keys.iterator();
        }

        private Map<String, String> _map;
    }

    public class SaveAction extends AbstractAction {

        public SaveAction(PtolemyFrame frame) {
            super("Save");
            putValue("tooltip", "Save NAOMI attributes");

            _frame = frame;
        }

        public void actionPerformed(ActionEvent e) {
            final NamedObj model = _frame.getModel();
            model.requestChange(new ChangeRequest(this,
                    "Save NAOMI attributes") {
                protected void _execute() throws Exception {
                    try {
                        synchronized (model) {
                            _loadInterfaceFromModel(model);
                            _undoable = true;
                            _mergeWithPrevious = false;
                            _saveAttributes(model, _getAttributesPath(), true);
                            _outputInterface();
                        }
                    } catch (IllegalActionException e) {
                        throw new InternalErrorException(e);
                    }
                }
            });
        }

        private PtolemyFrame _frame;
    }

    public class SyncAction extends AbstractAction {

        public SyncAction(PtolemyFrame frame) {
            super("Synchronize (Experimental)");
            putValue("tooltip", "Synchronize (load or save automatically) "
                    + "NAOMI attributes");

            _frame = frame;
        }

        public void actionPerformed(ActionEvent e) {
            final NamedObj model = _frame.getModel();
            model.requestChange(new ChangeRequest(this,
                    "Synchronize NAOMI attributes") {
                protected void _execute() throws Exception {
                    try {
                        synchronized (model) {
                            _loadInterfaceFromModel(model);
                            _undoable = true;
                            _mergeWithPrevious = false;
                            _loadAttributes(model, _getAttributesPath(), false);
                            _saveAttributes(model, _getAttributesPath(), false);
                            _outputInterface();
                        }
                    } catch (IllegalActionException e) {
                        throw new InternalErrorException(e);
                    }
                }
            });
        }

        private PtolemyFrame _frame;
    }

    public static class Tuple<T1, T2, T3, T4> {

        public Tuple(T1 v1, T2 v2, T3 v3, T4 v4) {
            _v1 = v1;
            _v2 = v2;
            _v3 = v3;
            _v4 = v4;
        }

        public T1 getV1() {
            return _v1;
        }

        public T2 getV2() {
            return _v2;
        }

        public T3 getV3() {
            return _v3;
        }

        public T4 getV4() {
            return _v4;
        }

        private T1 _v1;

        private T2 _v2;

        private T3 _v3;

        private T4 _v4;
    }

    public class UnlinkNaomiAttributeAction extends AbstractAction {
        public UnlinkNaomiAttributeAction(PtolemyFrame frame) {
            super("Unlink NAOMI Attribute");
            putValue("tooltip", "Remove a link between a NAOMI attribute and a "
                    + "parameter");

            _frame = frame;
        }

        public void actionPerformed(ActionEvent e) {
            NamedObj model = _frame.getModel();

            List<Attribute> attributes = _linkableAttributes(model);
            Iterator<Attribute> iterator = attributes.iterator();
            while (iterator.hasNext()) {
                Attribute attribute = iterator.next();
                if (attribute.attributeList(NaomiParameter.class).isEmpty()) {
                    iterator.remove();
                }
            }
            if (attributes.isEmpty()) {
                MessageHandler.message("No more parameter can be unlinked.");
                return;
            }

            Query query = new Query();
            String[] choices = new String[attributes.size()];
            int i = 0;
            for (Attribute attribute : attributes) {
                choices[i++] = attribute.getName(model);
            }
            query.addChoice("parameter", "Parameter", choices, choices[0]);
            ComponentDialog dialog = new ComponentDialog(_frame,
                    (String) getValue("tooltip"), query);
            boolean mergeWithPrevious = false;
            if (dialog.buttonPressed().equals("OK")) {
                String paramName = query.getStringValue("parameter");
                Attribute attribute = model.getAttribute(paramName);
                for (Object paramObject
                        : attribute.attributeList(NaomiParameter.class)) {
                    NaomiParameter param = (NaomiParameter) paramObject;
                    String moml = "<deleteProperty name=\"" + param.getName()
                            + "\"/>";
                    MoMLChangeRequest request = new MoMLChangeRequest(this,
                            attribute, moml);
                    request.setUndoable(true);
                    request.setMergeWithPreviousUndo(mergeWithPrevious);
                    mergeWithPrevious = true;
                    attribute.requestChange(request);
                }
            }
        }

        private PtolemyFrame _frame;
    }

    public enum Command {
        EXECUTE("execute"), LIST("list"), LOAD("load"), SAVE("save"),
        SYNC("sync");

        public String toString() {
            return _name;
        }

        Command(String name) {
            _name = name;
        }

        private String _name;
    }

    protected void _addMenus(PtolemyFrame frame) {
        JMenuBar menuBar = frame.getJMenuBar();
        JMenu naomiMenu = new JMenu("NAOMI");
        diva.gui.GUIUtilities.addMenuItem(naomiMenu, new ListAction(frame));
        diva.gui.GUIUtilities.addMenuItem(naomiMenu, new LoadAction(frame));
        diva.gui.GUIUtilities.addMenuItem(naomiMenu, new SaveAction(frame));
        diva.gui.GUIUtilities.addMenuItem(naomiMenu, new SyncAction(frame));
        naomiMenu.addSeparator();
        diva.gui.GUIUtilities.addMenuItem(naomiMenu,
                new LinkNaomiAttributeAction(frame));
        diva.gui.GUIUtilities.addMenuItem(naomiMenu,
                new UnlinkNaomiAttributeAction(frame));
        menuBar.add(naomiMenu);
    }

    protected Configuration _createDefaultConfiguration() throws Exception {
        if (_configurationURL == null) {
            _configurationURL = specToURL(_basePath
                    + "/full/configuration.xml");
        }

        // This has the side effects of merging properties from ptII.properties
        Configuration configuration = super._createDefaultConfiguration();

        try {
            configuration = readConfiguration(_configurationURL);
        } catch (Exception ex) {
            throw new Exception("Failed to read configuration '"
                    + _configurationURL + "'", ex);
        }

        // Read the user preferences, if any.
        PtolemyPreferences.setDefaultPreferences(configuration);

        // If _hideUserLibraryAttribute is not present, or is false,
        // call openUserLibrary().  openUserLibrary() will open either the
        // user library or the library named by the _alternateLibraryBuilder.
        Parameter hideUserLibraryAttribute = (Parameter) configuration
                .getAttribute("_hideUserLibrary", Parameter.class);

        if ((hideUserLibraryAttribute == null)
                || hideUserLibraryAttribute.getExpression().equals("false")) {

            // Load the user library.
            try {
                MoMLParser.setErrorHandler(new VergilErrorHandler());
                UserActorLibrary.openUserLibrary(configuration);
            } catch (Exception ex) {
                MessageHandler.error("Failed to display user library.", ex);
            }
        }

        return configuration;
    }

    protected File _getAttributesPath() {
        return new File(_root, "attributes");
    }

    protected void _list(NamedObj model, Map<Attribute, String> sync,
            Map<Attribute, String> load, Map<Attribute, String> save) {
        for (Object attrObject : model.attributeList(Variable.class)) {
            Attribute attr = (Attribute) attrObject;
            for (Object paramObject
                    : attr.attributeList(NaomiParameter.class)) {
                NaomiParameter naomiParam = (NaomiParameter) paramObject;
                String attributeName = naomiParam.getAttributeName();
                boolean needLoad = _inputAttributes.contains(attributeName);
                boolean needSave = _outputAttributes.contains(attributeName);
                String expression = naomiParam.getExpression();
                if (needLoad && needSave) {
                    sync.put(attr, expression);
                } else if (needLoad) {
                    load.put(attr, expression);
                } else if (needSave) {
                    save.put(attr, expression);
                }
            }
        }
    }

    protected Tuple<String, Date, String, String> _loadAttribute(
            File attributesPath, String attributeName)
            throws IllegalActionException {
        File attributeFile = new File(attributesPath, attributeName);
        Date fileDate = new Date(attributeFile.lastModified());
        try {
            Document document = _parseXML(attributeFile);

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(new MappedNamespaceContext(
                    NAMESPACES));

            XPathExpression expr = xpath.compile(
                    "/att:attribute/att:value");
            String value = expr.evaluate(document);
            value = StringUtilities.unescapeForXML(value);

            String unit;
            try {
                expr = xpath.compile("/att:attribute/att:units");
                unit = expr.evaluate(document);
                unit = StringUtilities.unescapeForXML(unit);
            } catch (Exception e) {
                unit = "";
            }

            String doc;
            try {
                doc = expr.evaluate(document);
                expr = xpath.compile("/att:attribute/att:documentation");
                doc = StringUtilities.unescapeForXML(doc);
            } catch (Exception e) {
                doc = "";
            }

            return new Tuple<String, Date, String, String>(value, fileDate,
                    unit, doc);
        } catch (XPathExpressionException e) {
            throw new KernelRuntimeException(e, "Unexpected error.");
        }
    }

    protected void _loadAttributes(NamedObj model, File attributesPath,
            boolean force)
    throws IllegalActionException {
        for (Object attrObject : model.attributeList(Variable.class)) {
            Attribute attr = (Attribute) attrObject;
            for (Object paramObject
                    : attr.attributeList(NaomiParameter.class)) {
                NaomiParameter naomiParam = (NaomiParameter) paramObject;
                String attributeName = naomiParam.getAttributeName();
                if (!_inputAttributes.contains(attributeName)) {
                    continue;
                }

                Tuple<String, Date, String, String> tuple = _loadAttribute(
                        attributesPath, attributeName);
                String value = tuple.getV1();
                Date date = tuple.getV2();
                String unit = tuple.getV3();
                String doc = tuple.getV4();

                if (!force) {
                    Date attributeDate = naomiParam.getModifiedDate();
                    if (!attributeDate.before(date)) {
                        continue;
                    }
                }

                System.out.println("Load: " + attributeName + " = " + value);

                String moml = "<property name=\"" + attr.getName() + "\" " +
                        "value=\"" + value + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        attr.getContainer(), moml);
                if (_undoable) {
                    request.setUndoable(true);
                    request.setMergeWithPreviousUndo(_mergeWithPrevious);
                    _mergeWithPrevious = true;
                }
                request.execute();

                moml = "<property name=\"" + naomiParam.getName() + "\" " +
                        "value=\"" + NaomiParameter.getExpression(
                                naomiParam.getMethod(),
                                naomiParam.getAttributeName(), date, unit,
                                doc) +
                        "\"/>";
                request = new MoMLChangeRequest(this, attr, moml);
                if (_undoable) {
                    request.setUndoable(true);
                    request.setMergeWithPreviousUndo(_mergeWithPrevious);
                    _mergeWithPrevious = true;
                }
                request.execute();

                break;
            }
        }
    }

    protected void _loadInterfaceFromModel(NamedObj model) {
        for (Object attrObject : model.attributeList(Variable.class)) {
            Attribute attr = (Attribute) attrObject;
            for (Object paramObject
                    : attr.attributeList(NaomiParameter.class)) {
                NaomiParameter naomiParam = (NaomiParameter) paramObject;
                String attributeName = naomiParam.getAttributeName();
                NaomiParameter.Method method = naomiParam.getMethod();
                switch (method) {
                case GET:
                    _inputAttributes.add(attributeName);
                    break;
                case PUT:
                    _outputAttributes.add(attributeName);
                    break;
                case SYNC:
                    _inputAttributes.add(attributeName);
                    _outputAttributes.add(attributeName);
                    break;
                }
            }
        }
    }

    protected void _loadInterfaceFromSVN(File file)
    throws IllegalActionException {
        Document document = _parseXML(file);

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(new MappedNamespaceContext(NAMESPACES));
        XPathExpression expr;

        String[] nodeTypes = new String[] {"input", "output"};
        List<Set<String>> sets = new LinkedList<Set<String>>();
        sets.add(_inputAttributes);
        sets.add(_outputAttributes);
        for (int i = 0; i < nodeTypes.length; i++) {
            try {
                expr = xpath.compile("/inf:interface/inf:" + nodeTypes[i]);
                NodeList nodes = (NodeList) expr.evaluate(document,
                        XPathConstants.NODESET);
                for (int j = 0; j < nodes.getLength(); j++) {
                    Node node = nodes.item(j);
                    String text = node.getTextContent();
                    sets.get(i).add(text);
                }
            } catch (XPathExpressionException e) {
                throw new KernelRuntimeException(e, "Unexpected error.");
            }
        }
    }

    protected Tableau _openModel(NamedObj entity) throws IllegalActionException,
    NameDuplicationException {
        Tableau tableau = super._openModel(entity);
        JFrame frame = tableau.getFrame();
        if (frame instanceof PtolemyFrame) {
            _addMenus((PtolemyFrame) frame);
        }
        return tableau;
    }

    protected Tableau _openModel(URL base, URL in, String identifier)
    throws Exception {
        Tableau tableau = super._openModel(base, in, identifier);
        JFrame frame = tableau.getFrame();
        if (frame instanceof PtolemyFrame) {
            _addMenus((PtolemyFrame) frame);
        }
        return tableau;
    }

    protected void _outputInterface() throws IllegalActionException {
        File interfaceFile = _getInterfaceFile();
        System.out.println("Output interface: " + interfaceFile.getPath());
        Document interfaceDocument = _generateInterface();
        try {
            _serializeXML(interfaceDocument,
                    new FileOutputStream(interfaceFile));
        } catch (FileNotFoundException e) {
            throw new IllegalActionException(null, e,
                    "Cannot output interface.");
        }
    }

    protected void _outputModel(NamedObj model) throws IllegalActionException {
        try {
            PrintStream stream;
            if (_outModelName == null) {
                stream = System.out;
            } else {
                stream = new PrintStream(_outModelName);
            }
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            model.exportMoML(writer);
            writer.flush();
        } catch (Exception e) {
            throw new IllegalActionException(null, e,
                    "Cannot output result model.");
        }
    }

    protected boolean _parseArg(String arg)
    throws Exception {
        if (arg.startsWith("-")) {
            arg = arg.toLowerCase();
            if (_isExpectingValue()) {
                throw new IllegalActionException("Expecting value for argument "
                        + "\"" + arg + "\"");
            } else if (arg.equals("-cmd") && _command == null) {
                _expectingCommand = true;
                return true;
            } else if (arg.equals("-in") && _inModel == null) {
                _expectingInModelName = true;
                return true;
            } else if (arg.equals("-out") && _outModelName == null) {
                _expectingOutModelName = true;
                return true;
            } else if (arg.equals("-owner") && _owner == null) {
                _expectingOwner = true;
                return true;
            } else if (arg.equals("-root") && _root == null) {
                _expectingRoot = true;
                return true;
            }
        } else if (_expectingCommand) {
            for (Command cmd : Command.values()) {
                if (arg.equals(cmd.toString())) {
                    _command = cmd;
                    _expectingCommand = false;
                    return true;
                }
            }
            throw new IllegalActionException("Unknown command: " + arg);
        } else if (_expectingInModelName) {
            _parseInModel(arg);
            _expectingInModelName = false;
            return true;
        } else if (_expectingOutModelName) {
            _outModelName = arg;
            _expectingOutModelName = false;
            return true;
        } else if (_expectingOwner) {
            _owner = arg;
            _expectingOwner = false;
            return true;
        } else if (_expectingRoot) {
            _root = arg;
            _attributesPath = new File(arg, "attributes");
            _expectingRoot = false;
            return true;
        }
        return super._parseArg(arg);
    }

    protected void _parseArgs(final String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println(_usage());
            System.exit(0);
        }

        super._parseArgs(args);

        if (_owner == null) {
            _owner = "ptII";
        }
    }

    protected void _saveAttributes(NamedObj model, File attributesPath,
            boolean force)
    throws IllegalActionException {

        for (Object attrObject : model.attributeList(Variable.class)) {
            Variable attr = (Variable) attrObject;
            for (Object paramObject
                    : attr.attributeList(NaomiParameter.class)) {
                NaomiParameter naomiParam = (NaomiParameter) paramObject;
                String attributeName = naomiParam.getAttributeName();
                if (!_outputAttributes.contains(attributeName)) {
                    continue;
                }

                File attributeFile = new File(attributesPath, attributeName);

                Date attributeDate = naomiParam.getModifiedDate();
                if (!force) {
                    Date fileDate = new Date(attributeFile.lastModified());
                    if (attributeFile.exists() && attributeFile.isFile()
                            && !fileDate.before(attributeDate)) {
                        continue;
                    }
                }

                String newValue = attr.getToken().toString();
                System.out.println("Save: " + attributeName + " = " + newValue);

                String unit = naomiParam.getUnit();
                String doc = naomiParam.getDocumentation();

                try {
                    DocumentBuilderFactory docFactory =
                        DocumentBuilderFactory.newInstance();
                    docFactory.setNamespaceAware(true);
                    DocumentBuilder builder;
                    builder = docFactory.newDocumentBuilder();
                    DOMImplementation impl = builder.getDOMImplementation();
                    Document document = impl.createDocument(NAMESPACES[0][1],
                            "attribute", null);
                    Element root = document.getDocumentElement();
                    Attr attribute = document.createAttributeNS(
                            "http://www.w3.org/2001/XMLSchema-instance",
                            "schemaLocation");
                    attribute.setPrefix("xsi");
                    attribute.setValue("http://www.atl.lmco.com/naomi attribute"
                            + "_simple.xsd");
                    root.setAttributeNodeNS(attribute);
                    Element owner = document.createElementNS(NAMESPACES[0][1],
                            "owner");
                    owner.setTextContent(_owner);
                    root.appendChild(owner);
                    Element value = document.createElementNS(NAMESPACES[0][1],
                            "value");
                    value.setTextContent(StringUtilities.escapeForXML(
                            newValue));
                    root.appendChild(value);
                    Element units = document.createElementNS(NAMESPACES[0][1],
                            "units");
                    if (!unit.equals("")) {
                        units.setTextContent(StringUtilities.escapeForXML(
                                unit));
                    }
                    root.appendChild(units);
                    Element documentation = document.createElementNS(
                            NAMESPACES[0][1], "documentation");
                    if (!doc.equals("")) {
                        documentation.setTextContent(StringUtilities
                                .escapeForXML(doc));
                    }
                    root.appendChild(documentation);

                    FileOutputStream stream =
                        new FileOutputStream(attributeFile);
                    _serializeXML(document, stream);

                    if (!attributeFile.setLastModified(attributeDate
                            .getTime())) {
                        throw new IllegalActionException("Failed to set last "
                                + "modified time of \""
                                + attributeFile.getName() + "\"");
                    }
                } catch (ParserConfigurationException e) {
                    throw new IllegalActionException(null, e,
                            "Cannot create DocumentBuilder.");
                } catch (FileNotFoundException e) {
                    throw new IllegalActionException(null, e,
                            "Cannot create attribute file: "
                            + attributeFile.getPath());
                }
            }
        }
    }

    protected String _usage() {
        return _configurationUsage(
                "java " + getClass().getName() + " [ options ]",
                COMMAND_OPTIONS, new String[] {});
    }

    private Document _generateInterface() throws IllegalActionException {
        try {
            DocumentBuilderFactory docFactory =
                DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder builder = docFactory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element interfaceNode = document.createElementNS(NAMESPACES[1][1],
                    "interface");
            document.appendChild(interfaceNode);

            Element nameNode = document.createElement("name");
            nameNode.setTextContent(_owner);
            interfaceNode.appendChild(nameNode);

            Element typeNode = document.createElement("type");
            typeNode.setTextContent(_owner);
            interfaceNode.appendChild(typeNode);

            for (String inputAttribute : _inputAttributes) {
                Element inputNode = document.createElement("input");
                inputNode.setTextContent(inputAttribute);
                interfaceNode.appendChild(inputNode);
            }

            for (String outputAttribute : _outputAttributes) {
                Element outputNode = document.createElement("output");
                outputNode.setTextContent(outputAttribute);
                interfaceNode.appendChild(outputNode);
            }

            return document;
        } catch (ParserConfigurationException e) {
            throw new IllegalActionException(null, e,
                    "Cannot generate model interface.");
        }
    }

    private File _getInterfaceFile() throws IllegalActionException {
        File interfacePath = new File(new File(_root, "interfaces"), _owner);
        if (!interfacePath.exists() || !interfacePath.isDirectory()) {
            if (!interfacePath.mkdirs()) {
                throw new IllegalActionException("Unable to create path to " +
                        "interface file " + interfacePath.getPath() + ".");
            }
        }
        File interfaceFile = new File(interfacePath, _owner + "_interface.xml");
        if (interfaceFile.exists() && !interfaceFile.isFile()) {
            throw new IllegalActionException("Interface file " +
                    interfaceFile.getPath() + " is not a file.");
        }
        return interfaceFile;
    }

    private boolean _isExpectingValue() {
        return _expectingInModelName || _expectingOutModelName
                || _expectingCommand || _expectingRoot || _expectingOwner;
    }

    private List<Attribute> _linkableAttributes(NamedObj model) {
        List<Attribute> attributes = new LinkedList<Attribute>();
        for (Object attrObject : model.attributeList(Variable.class)) {
            Attribute attr = (Attribute) attrObject;
            attributes.add(attr);
        }
        return attributes;
    }

    private void _parseInModel(String fileName) throws Exception {
        URL inURL = specToURL(fileName);
        URL base = inURL;
        String identifier = inURL.toExternalForm();
        ModelDirectory directory = _configuration.getDirectory();
        EffigyFactory factory = (EffigyFactory) _configuration.getEntity(
                "effigyFactory");
        PtolemyEffigy effigy = (PtolemyEffigy) factory.createEffigy(directory,
                base, inURL);
        effigy.identifier.setExpression(identifier);
        _inModel = effigy.getModel();
    }

    private Document _parseXML(File file) throws IllegalActionException {
        try {
            DocumentBuilderFactory docFactory =
                DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder builder = docFactory.newDocumentBuilder();
            return builder.parse(file);
        } catch (ParserConfigurationException e) {
            throw new IllegalActionException(null, e,
                    "Cannot create DocumentBuilder.");
        } catch (SAXException e) {
            throw new IllegalActionException(null, e,
                    "Fail to parse attribute file: " + file);
        } catch (IOException e) {
            throw new IllegalActionException(null, e,
                    "Cannot read from attribute file: " + file);
        }
    }

    private boolean _pathExists(File path) {
        return path.exists() && path.isDirectory();
    }

    private boolean _pathExists(String path) {
        return _pathExists(new File(path));
    }

    private void _serializeXML(Document document, OutputStream stream)
    throws IllegalActionException {
        try {
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(stream);
            TransformerFactory transformerFactory =
                TransformerFactory.newInstance();
            Transformer serializer = transformerFactory.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(domSource, streamResult);
            stream.flush();
            stream.close();
        } catch (TransformerException e) {
            throw new IllegalActionException(null, e,
                    "Unable to serialize XML stream.");
        } catch (IOException e) {
            throw new IllegalActionException(null, e,
                    "Unable to serialize XML stream.");
        }
    }

    private File _attributesPath;

    private Command _command;

    private URL _configurationURL;

    private boolean _expectingCommand;

    private boolean _expectingInModelName;

    private boolean _expectingOutModelName;

    private boolean _expectingOwner;

    private boolean _expectingRoot;

    private NamedObj _inModel;

    private Set<String> _inputAttributes = new HashSet<String>();

    private boolean _mergeWithPrevious = false;

    private String _outModelName;

    private Set<String> _outputAttributes = new HashSet<String>();;

    private String _owner;

    private String _root;

    private boolean _undoable = false;
}
