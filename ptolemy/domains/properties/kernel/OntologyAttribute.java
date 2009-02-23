package ptolemy.domains.properties.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.domains.properties.kernel.ModelAttribute;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.toolbox.TextEditorTableauFactory;

public class OntologyAttribute extends ModelAttribute {    
    
    public OntologyAttribute(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    public void parseSpecificationRules() {
        
    }
    
    public Object executeRules() {
        return null;
    }
       
    protected String _getContainedModelClassName() {
        return getClass().getName() + "$OntologyComposite";
    }    
    
    public static class OntologyComposite extends CompositeActor {

        public static final String RULES = "_rules";

        public OntologyComposite(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        public OntologyComposite(Workspace workspace) {
            super(workspace);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        protected void _addEntity(ComponentEntity entity)
        throws IllegalActionException, NameDuplicationException {

            if (entity.getAttribute(RULES) == null) {
                StringAttribute userRules = new StringAttribute(entity, RULES);
                userRules.setVisibility(Settable.EXPERT);
            }
            
            if (entity.getAttribute("_tableauFactory") == null) {
                TextEditorTableauFactory factory = 
                    new TextEditorTableauFactory(entity, "_tableauFactory");
                factory.attributeName.setExpression(RULES);
                
            }
            super._addEntity(entity);
        }    

        
    }
}
