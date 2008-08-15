package ptolemy.actor.gt.controller;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;

import ptolemy.actor.Manager;
import ptolemy.actor.gt.GTAttribute;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.erg.kernel.ERGModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.gt.TransformationAttributeController;
import ptolemy.vergil.gt.TransformationAttributeEditorFactory;
import ptolemy.vergil.gt.TransformationAttributeIcon;

public class TransformationAttribute extends GTAttribute
implements Configurable {

    public TransformationAttribute(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        condition = new Parameter(this, "condition");
        condition.setExpression("true");

        _configurer = new Configurer(workspace());
        _configurer.setName("Configurer");
        new DEDirector(_configurer, "_director");
        _configurer.setManager(new Manager(workspace(), "_manager"));
        _configurer.setConfiguredObject(this);

        String moml = "<entity name=\"ModelUpdater\" " +
                "class=\"ptolemy.actor.gt.controller.ModelUpdater\"/>";
        MoMLChangeRequest request = new MoMLChangeRequest(this, _configurer,
                moml);
        request.execute();
        _modelUpdater = (ERGModalModel) _configurer.getEntity("ModelUpdater");

        new TransformationAttributeIcon(this, "_icon");
        new TransformationAttributeController.Factory(this,
                "_controllerFactory");
        editorFactory = new TransformationAttributeEditorFactory(this,
                "editorFactory");
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TransformationAttribute newObject =
            (TransformationAttribute) super.clone(workspace);
        try {
            newObject._configurer = new Configurer(workspace);
            newObject._configurer.setName("Configurer");
            new DEDirector(newObject._configurer, "_director");
            newObject._configurer.setManager(new Manager(workspace,
                    "_manager"));
            newObject._configurer.setConfiguredObject(newObject);
            newObject._modelUpdater = (ERGModalModel) _modelUpdater.clone(
                    workspace);
            newObject._modelUpdater.setContainer(newObject._configurer);
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    public void configure(URL base, String source, String text)
            throws Exception {
        _configureSource = source;
        text = text.trim();
        if (!text.equals("")) {
            MoMLParser parser = new MoMLParser(workspace());
            _configurer.removeAllEntities();
            parser.setContext(_configurer);
            parser.parse(base, new StringReader(text));
            _modelUpdater = (ERGModalModel) _configurer.entityList().get(0);
            _clearURI(_modelUpdater);
        }
    }

    public String getConfigureSource() {
        return _configureSource;
    }

    public String getConfigureText() {
        return null;
    }

    public ERGModalModel getModelUpdater() {
        return _modelUpdater;
    }

    public Parameter condition;

    public TransformationAttributeEditorFactory editorFactory;

    protected void _exportMoMLContents(Writer output, int depth)
    throws IOException {
        super._exportMoMLContents(output, depth);

        String sourceSpec = "";

        if ((_configureSource != null) && !_configureSource.trim().equals("")) {
            sourceSpec = " source=\"" + _configureSource + "\"";
        }

        output.write(_getIndentPrefix(depth) + "<configure" + sourceSpec +
                ">\n");
        _modelUpdater.exportMoML(output, depth + 1);
        output.write("</configure>\n");
    }

    private static void _clearURI(NamedObj object)
    throws IllegalActionException, NameDuplicationException {
        URIAttribute attribute = (URIAttribute) object.getAttribute("_uri",
                URIAttribute.class);
        if (attribute != null) {
            attribute.setContainer(null);
        }
    }

    private String _configureSource;

    private Configurer _configurer;

    private ERGModalModel _modelUpdater;
}
