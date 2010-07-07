package ptdb.kernel.bl.load.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import ptdb.common.dto.RemoveModelsTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.load.LoadManager;
import ptdb.kernel.bl.save.SaveModelManager;
import ptdb.kernel.database.DBConnection;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.Entity;

public class TestImportByReferenceRequirementsIntegration {

    
    /**
     * Test importing a model by reference.  No DBReference tag is present.
     * @exception Exception
     */
    @Test
    public void testImportModelByRefNoTag() throws Exception {

        Entity container = new Entity("container");

        java.util.Date time = new java.util.Date();
        
        String modelName=String.valueOf(time.getTime()) + "model";

        Entity entity = null;
        
        XMLDBModel dbModel = new XMLDBModel(modelName);
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                        + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                        + "<entity name=\"" + modelName + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                        + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                        + "</property>"
                        + "<property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\" value=\"{bounds={232, 141, 815, 517}, maximized=false}\">"
                        + "</property>"
                        + "<property name=\"_vergilSize\" class=\"ptolemy.actor.gui.SizeAttribute\" value=\"[600, 400]\">"
                        + "</property>"
                        + "<property name=\"_vergilZoomFactor\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"1.0\">"
                        + "</property>"
                        + "<property name=\"_vergilCenter\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"{300.0, 200.0}\">"
                        + "</property>"
                        + "<entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">"
                        + "<doc>Create a constant sequence.</doc>"
                        + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                        + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                        + "</property>"
                        + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                        + "</property>"
                        + "</property>"
                        + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{150, 150}\">"
                        + "</property>" + "</entity>" + "</entity>");

            SaveModelManager saveManager = new SaveModelManager();
            String modelID = saveManager.save(dbModel);
            
            entity = LoadManager.importModel(modelName, true, container);
            assertEquals(entity.getName(), modelName);

            assertEquals(
                    ((StringParameter) entity.getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                        .getExpression(), 
                    "TRUE");

            removeModel(new XMLDBModel(dbModel.getModelName()));            
            

        }
    
    /**
     * Test importing a model by value.  No DBReference tag is present.
     * @exception Exception
     */
    @Test
    public void testImportModelByRefWithTag() throws Exception {

        Entity container = new Entity("container");

        java.util.Date time = new java.util.Date();
        
        String modelName=String.valueOf(time.getTime()) + "model";

        Entity entity = null;
        
        XMLDBModel dbModel = new XMLDBModel(modelName);
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                        + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                        + "<entity name=\"" + modelName + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                        + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                        + "</property>"
                        + "<property name=\"" + XMLDBModel.DB_REFERENCE_ATTR + "\" class=\"ptolemy.data.expr.StringParameter\" value=\"FALSE\"></property>"
                        + "<property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\" value=\"{bounds={232, 141, 815, 517}, maximized=false}\">"
                        + "</property>"
                        + "<property name=\"_vergilSize\" class=\"ptolemy.actor.gui.SizeAttribute\" value=\"[600, 400]\">"
                        + "</property>"
                        + "<property name=\"_vergilZoomFactor\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"1.0\">"
                        + "</property>"
                        + "<property name=\"_vergilCenter\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"{300.0, 200.0}\">"
                        + "</property>"
                        + "<entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">"
                        + "<doc>Create a constant sequence.</doc>"
                        + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                        + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                        + "</property>"
                        + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                        + "</property>"
                        + "</property>"
                        + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{150, 150}\">"
                        + "</property>" + "</entity>" + "</entity>");

            SaveModelManager saveManager = new SaveModelManager();
            String modelID = saveManager.save(dbModel);
            
            entity = LoadManager.importModel(modelName, true, container);
            assertEquals(entity.getName(), modelName);

            assertEquals(
                    ((StringParameter) entity.getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                        .getExpression(), 
                    "TRUE");
            

            removeModel(new XMLDBModel(dbModel.getModelName()));
            
            
            
        }
    
    /**
     * Test importing a model by reference.  No DBReference tag is present.
     * @exception Exception
     */
    @Test
    public void testImportModelByValueNoTag() throws Exception {


        Entity container = new Entity("container");

        java.util.Date time = new java.util.Date();
        
        String modelName=String.valueOf(time.getTime()) + "model";

        Entity entity = null;
        
        XMLDBModel dbModel = new XMLDBModel(modelName);
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\"" + modelName + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>"
                + "<property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\" value=\"{bounds={232, 141, 815, 517}, maximized=false}\">"
                + "</property>"
                + "<property name=\"_vergilSize\" class=\"ptolemy.actor.gui.SizeAttribute\" value=\"[600, 400]\">"
                + "</property>"
                + "<property name=\"_vergilZoomFactor\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"1.0\">"
                + "</property>"
                + "<property name=\"_vergilCenter\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"{300.0, 200.0}\">"
                + "</property>"
                + "<entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">"
                + "<doc>Create a constant sequence.</doc>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                + "</property>"
                + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                + "</property>"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{150, 150}\">"
                + "</property>" + "</entity>" + "</entity>");

            SaveModelManager saveManager = new SaveModelManager();
            String modelID = saveManager.save(dbModel);

            entity = LoadManager.importModel(modelName, false, container);
            assertEquals(entity.getName(), modelName);

            assertEquals(
                    ((StringParameter) entity.getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                        .getExpression(), 
                    "FALSE");
            

            removeModel(new XMLDBModel(dbModel.getModelName()));
            

        }
    
    /**
     * Test importing a model by reference.  No DBReference tag is present.
     * @exception Exception
     */
    @Test
    public void testImportModelByValueWithTag() throws Exception {

        Entity container = new Entity("container");

        java.util.Date time = new java.util.Date();
        
        String modelName=String.valueOf(time.getTime()) + "model";

        Entity entity = null;
        
        XMLDBModel dbModel = new XMLDBModel(modelName);
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                        + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                        + "<entity name=\"" + modelName + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                        + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                        + "</property>"
                        + "<property name=\"" + XMLDBModel.DB_REFERENCE_ATTR + "\" class=\"ptolemy.data.expr.StringParameter\" value=\"FALSE\"></property>"
                        + "<property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\" value=\"{bounds={232, 141, 815, 517}, maximized=false}\">"
                        + "</property>"
                        + "<property name=\"_vergilSize\" class=\"ptolemy.actor.gui.SizeAttribute\" value=\"[600, 400]\">"
                        + "</property>"
                        + "<property name=\"_vergilZoomFactor\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"1.0\">"
                        + "</property>"
                        + "<property name=\"_vergilCenter\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"{300.0, 200.0}\">"
                        + "</property>"
                        + "<entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">"
                        + "<doc>Create a constant sequence.</doc>"
                        + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                        + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                        + "</property>"
                        + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                        + "</property>"
                        + "</property>"
                        + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{150, 150}\">"
                        + "</property>" + "</entity>" + "</entity>");

            SaveModelManager saveManager = new SaveModelManager();
            String modelID = saveManager.save(dbModel);
        
            entity = LoadManager.importModel(modelName, false, container);
            assertEquals(entity.getName(), modelName);

            assertEquals(
                    ((StringParameter) entity.getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                        .getExpression(), 
                    "FALSE");
          

            removeModel(new XMLDBModel(dbModel.getModelName()));
            
            
        }
    
    /**
     * Test attempting to import a model with a null model name.
     * @exception Exception
     */
    @Test
    public void testNull() throws Exception {

        Entity container = new Entity("container");
        
        String inputString=null;

        Entity entity = null;

        boolean isSuccess = false;
        
        try{
        
            entity = LoadManager.importModel(inputString, false, container);
            
        } catch(Exception e){
            
            isSuccess = true;
            
        }
        
        assertTrue(isSuccess);

            
        }
    
    private void removeModel(XMLDBModel dbModel) throws Exception{
        
        DBConnection dbConnection = null;
        
        try {

            ArrayList<XMLDBModel> modelList = new ArrayList();
            modelList.add(dbModel);
            dbConnection = DBConnectorFactory.getSyncConnection(true);
            RemoveModelsTask removeModelsTask = new RemoveModelsTask(modelList);
            dbConnection.executeRemoveModelsTask(removeModelsTask);
            dbConnection.commitConnection();
            
        } catch (DBExecutionException e) {

            if (dbConnection != null) {

                dbConnection.abortConnection();
            }

            throw e;
            
        } finally {

            if (dbConnection != null) {

                dbConnection.closeConnection();

            }
        }
        
    }
}
