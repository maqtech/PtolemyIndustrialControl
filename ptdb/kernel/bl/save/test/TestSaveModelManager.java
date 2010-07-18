/*
@Copyright (c) 2010 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptdb.kernel.bl.save.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.dto.RemoveModelsTask;
import ptdb.common.dto.RenameModelTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.XMLDBModelParsingException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.common.util.Utilities;
import ptdb.kernel.bl.load.DBModelFetcher;
import ptdb.kernel.bl.save.SaveModelManager;
import ptdb.kernel.database.CacheManager;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// TestSaveModelManager

/**
 * JUnit test case for testing SaveModelManager class.
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (yalsaeed)
 * @Pt.AcceptedRating red (yalsaeed)
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { SaveModelManager.class, DBConnection.class,
        DBConnectorFactory.class, CreateModelTask.class, SaveModelTask.class,
        DBExecutionException.class, CacheManager.class, RenameModelTask.class })
@SuppressStaticInitializationFor("ptdb.common.util.DBConnectorFactory,ptdb.kernel.database.CacheManager")
public class TestSaveModelManager {

    ///////////////////////////////////////////////////////////////////
    ////                public methods                            ////

    /**
     * Test the SaveManager.save() method.
     * <p>
     * The condition for this test case:<br/>
     * 
     * - The model being saved is a new model and should be created in the
     * database.
     * </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testSave_CreateModel() throws Exception {

        SaveModelManager saveManager = new SaveModelManager();
        
        PowerMock.mockStatic(DBConnectorFactory.class);
        
        
        PowerMock.mockStatic(CacheManager.class);
        
        

        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);
        
        DBConnection dBCacheConnectionMock = PowerMock
                .createMock(DBConnection.class);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dBConnectionMock);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dBCacheConnectionMock);        


        
        XMLDBModel xmlDBModel = new XMLDBModel(Utilities.generateId("test"));

        xmlDBModel.setIsNew(true);
        
        xmlDBModel.setModel("<entity name=\""+ xmlDBModel.getModelName()+"\"></entity>");
        
        
        CreateModelTask createModelTaskMock = PowerMock
                .createMock(CreateModelTask.class);

        PowerMock.expectNew(CreateModelTask.class, xmlDBModel).andReturn(
                createModelTaskMock);
        
        EasyMock.expect(dBConnectionMock.executeCreateModelTask(createModelTaskMock)).andReturn(xmlDBModel.getModelName());
        
        FetchHierarchyTask fetchHierarchyTaskMock = PowerMock.createMock(FetchHierarchyTask.class);

        ArrayList<XMLDBModel> modelList = new ArrayList();
        modelList.add(xmlDBModel);
        
        fetchHierarchyTaskMock.setModelsList(modelList);
        
        EasyMock.expect(CacheManager.removeFromCache(modelList)).andReturn(true);
           
        PowerMock.expectNew(FetchHierarchyTask.class).andReturn(fetchHierarchyTaskMock);
        
        EasyMock.expect(dBCacheConnectionMock.executeFetchHierarchyTask(fetchHierarchyTaskMock)).andReturn(null);
  
        dBConnectionMock.commitConnection();

        dBConnectionMock.closeConnection();
        
        dBCacheConnectionMock.closeConnection();

        PowerMock.replayAll();

        boolean isSuccess = (saveManager.save(xmlDBModel) != null);

        assertTrue(isSuccess);

        PowerMock.verifyAll();

    }

    /**
     * Test the SaveManager.save() method.
     * <p>
     * The condition for this test case:
     * 
     * <br>- The model being saved is a new model and should be created in the
     * database. 
     * <br>- The executeCreateModelTask method throws exception.
     * </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testSave_CreateModelNotSuccessful() throws Exception {

        SaveModelManager saveManager = new SaveModelManager();

        PowerMock.mockStatic(DBConnectorFactory.class);

        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dBConnectionMock);

        XMLDBModel modelMock = PowerMock.createPartialMock(XMLDBModel.class, "getIsNew");

        EasyMock.expect(modelMock.getIsNew()).andReturn(true);

        CreateModelTask createModelTaskMock = PowerMock
                .createMock(CreateModelTask.class);

        PowerMock.expectNew(CreateModelTask.class, modelMock).andReturn(
                createModelTaskMock);

        //createModelTaskMock.setXMLDBModel(modelMock);

        dBConnectionMock.executeCreateModelTask(createModelTaskMock);

        PowerMock.expectLastCall().andAnswer(new IAnswer() {
            public Object answer() throws DBExecutionException {

                throw new DBExecutionException("Message");

            }
        });

        dBConnectionMock.closeConnection();

        dBConnectionMock.abortConnection();

        PowerMock.replayAll();

        boolean isSuccess = false;

        try {

            saveManager.save(modelMock);

        } catch (DBExecutionException e) {

            isSuccess = true;
        }

        assertTrue(isSuccess);

        PowerMock.verifyAll();

    }

    /**
     * Test the SaveManager.save() method.
     * <p>
     * The condition for this test case:
     * 
     * <br>- The model being saved is an existing model and should be updated in
     * the database.
     * </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testSave_SaveModel() throws Exception {

SaveModelManager saveManager = new SaveModelManager();
        
        PowerMock.mockStatic(DBConnectorFactory.class);
        
        
        PowerMock.mockStatic(CacheManager.class);
        
        

        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);
        
        DBConnection dBCacheConnectionMock = PowerMock
                .createMock(DBConnection.class);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dBConnectionMock);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dBCacheConnectionMock);        


        
        XMLDBModel xmlDBModel = new XMLDBModel(Utilities.generateId("test"));

        xmlDBModel.setIsNew(false);
        
        xmlDBModel.setModel("<entity name=\""+ xmlDBModel.getModelName()+"\"></entity>");
        
        
        SaveModelTask saveModelTaskMock = PowerMock
                .createMock(SaveModelTask.class);

        PowerMock.expectNew(SaveModelTask.class, xmlDBModel).andReturn(
                saveModelTaskMock);
        
        EasyMock.expect(dBConnectionMock.executeSaveModelTask(saveModelTaskMock)).andReturn(xmlDBModel.getModelName());
        
        FetchHierarchyTask fetchHierarchyTaskMock = PowerMock.createMock(FetchHierarchyTask.class);

        ArrayList<XMLDBModel> modelList = new ArrayList();
        modelList.add(xmlDBModel);
        
        fetchHierarchyTaskMock.setModelsList(modelList);
        
        EasyMock.expect(CacheManager.removeFromCache(modelList)).andReturn(true);
           
        PowerMock.expectNew(FetchHierarchyTask.class).andReturn(fetchHierarchyTaskMock);
        
        EasyMock.expect(dBCacheConnectionMock.executeFetchHierarchyTask(fetchHierarchyTaskMock)).andReturn(null);
          
        dBConnectionMock.commitConnection();

        dBConnectionMock.closeConnection();
        
        dBCacheConnectionMock.closeConnection();

        PowerMock.replayAll();

        boolean isSuccess = (saveManager.save(xmlDBModel) != null);

        assertTrue(isSuccess);

        PowerMock.verifyAll();

    }

    /**
     * Test the SaveManager.save() method. 
     * <p> 
     * The condition for this test case:
     * 
     * <br> - The model being saved is an existing model and should be
     * updated in the database. 
     * <br>- The executeSaveModelTask method throws
     * exception. 
     * </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testSave_SaveModelNotSuccessful() throws Exception {

        SaveModelManager saveManager = new SaveModelManager();

        PowerMock.mockStatic(DBConnectorFactory.class);

        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dBConnectionMock);

        XMLDBModel modelMock = PowerMock.createPartialMock(XMLDBModel.class, "getIsNew");

        EasyMock.expect(modelMock.getIsNew()).andReturn(false);

        SaveModelTask saveModelTaskMock = PowerMock
                .createMock(SaveModelTask.class);

        PowerMock.expectNew(SaveModelTask.class, modelMock).andReturn(
                saveModelTaskMock);

        //saveModelTaskMock.setXMLDBModel(modelMock);

        dBConnectionMock.executeSaveModelTask(saveModelTaskMock);

        PowerMock.expectLastCall().andAnswer(new IAnswer() {
            public Object answer() throws DBExecutionException {

                throw new DBExecutionException("Test Message");

            }
        });

        dBConnectionMock.closeConnection();

        dBConnectionMock.abortConnection();

        PowerMock.replayAll();

        boolean isSuccess = false;

        try {

            saveManager.save(modelMock);

        } catch (DBExecutionException e) {

            isSuccess = true;
        }

        assertTrue(isSuccess);

        PowerMock.verifyAll();

    }

    /**
     * Test the SaveManager.save() method with DBConnection being null.
     * 
     * <p>
     * The condition for this test case:
     * 
     * <br>- Fail to get a DBConnection from the DBConnectionFactory.
     * 
     * </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testSave_NullDBConn() throws Exception {

        SaveModelManager saveManager = new SaveModelManager();

        PowerMock.mockStatic(DBConnectorFactory.class);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                null);

        XMLDBModel modelMock = PowerMock.createPartialMock(XMLDBModel.class, "getIsNew");

        PowerMock.replayAll();

        boolean isSuccess = false;

        try {

            saveManager.save(modelMock);

        } catch (DBConnectionException e) {

            isSuccess = true;
        }

        assertTrue(isSuccess);

        PowerMock.verifyAll();

    }

    /**
     * Test the SaveManager.save() with null parameters passed to it.
     * 
     * <p>
     * The condition for this test case: 
     * <br>- The model passed as a parameter is null. The test result should 
     * throw an exception.
     * </p>
     * 
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testSave_NullModelParam() throws Exception {

        SaveModelManager saveManager = new SaveModelManager();

        PowerMock.replayAll();

        boolean isSuccess = false;

        try {

            saveManager.save(null);
        } catch (IllegalArgumentException e) {

            isSuccess = true;
        }

        assertTrue(isSuccess);

        PowerMock.verifyAll();

    }

    @Test
    public void testPopulateChildModelsList() throws DBExecutionException,
            DBConnectionException {
        // Two referenceModels
        XMLDBModel model = new XMLDBModel("Test1");
        String content = "<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\"  "
                + "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + " <entity name=\"Test1\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + " <property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + " </property>"
                + " <property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\" value=\"{bounds={271, 127, 823, 514}, maximized=false}\">"
                + " </property>"
                + " <property name=\"_vergilSize\" class=\"ptolemy.actor.gui.SizeAttribute\" value=\"[600, 400]\">"
                + " </property>"
                + " <property name=\"_vergilZoomFactor\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"1.0\">"
                + " </property>"
                + " <property name=\"_vergilCenter\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"{300.0, 200.0}\">"
                + " </property>"
                + " <entity name=\"TypeComp1\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + " <entity name=\"AddSubtract\" class=\"ptolemy.actor.lib.AddSubtract\">"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{240, 150}\">"
                + "</property>"
                + "</entity>"
                + "<property name=\"" + XMLDBModel.DB_MODEL_ID_ATTR + "\" value=\"ModelX\">"
                + "  </property>"
                + "  <property name=\"" + XMLDBModel.DB_REFERENCE_ATTR + "\" value=\"TRUE\">  </property>"
                + "  <property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{240, 150}\">"
                + " </property>"
                + "</entity>"
                + " <entity name=\"TypeComp2\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<entity name=\"AddSubtract\" class=\"ptolemy.actor.lib.AddSubtract\">"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{240, 150}\">"
                + "</property>"
                + "</entity>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{240, 150}\">"
                + " </property>"
                + "  <property name=\"" + XMLDBModel.DB_MODEL_ID_ATTR + "\" value=\"ModelX\">"
                + "  </property>"
                + "  <property name=\"" + XMLDBModel.DB_REFERENCE_ATTR + "\" value=\"TRUE\">  </property>"
                + "</entity></entity>";

        model.setModel(content);
        DBConnectorFactory.loadDBProperties();
        SaveModelManager manager = new SaveModelManager();
        try {
            model = manager.populateChildModelsList(model);
            assertTrue("Referenced models list is null.", model
                    .getReferencedChildren() != null);
            assertTrue("Incorrect number of reference models were returned.",
                    model.getReferencedChildren().size() == 2);
        } catch (XMLDBModelParsingException e) {
            fail("Failed with error - " + e.getMessage());
        }

        // No referenceModels.
        XMLDBModel adderModel = DBModelFetcher.load("Adder");

        try {
            adderModel = manager.populateChildModelsList(adderModel);

            assertTrue("Referenced models list is not empty.", adderModel
                    .getReferencedChildren() == null
                    || adderModel.getReferencedChildren().size() == 0);
        } catch (XMLDBModelParsingException e) {
            fail("Failed with error - " + e.getMessage());
        }

        // Reference model with extra attributes. 
        XMLDBModel oneAdderModel = DBModelFetcher.load("modelWithTwoAdders");

        try {
            oneAdderModel = manager.populateChildModelsList(oneAdderModel);

            assertTrue("Referenced models list is null.", oneAdderModel
                    .getReferencedChildren() != null);
            
            assertTrue("Incorrect number of reference models were returned.",
                    oneAdderModel.getReferencedChildren().size() == 2);
        } catch (XMLDBModelParsingException e) {
            fail("Failed with error - " + e.getMessage());
        }
        // Reference model with two referenced model.
        XMLDBModel twoAdderModel = DBModelFetcher.load("modelWithTwoAdders");
        try {
            twoAdderModel = manager.populateChildModelsList(twoAdderModel);

            assertTrue("Referenced models list is null.", twoAdderModel
                    .getReferencedChildren() != null);
            
            assertTrue("Incorrect number of reference models were returned.",
                    twoAdderModel.getReferencedChildren().size() == 2);
            
        } catch (XMLDBModelParsingException e) {
            fail("Failed with error - " + e.getMessage());
        }

        // Reference model with two referenced model. 
    }
    
    
    /**
     * Test the renaming of a model when the model and the name are correct.
     * @throws Exception
     */
    @Test
    public void testRenameModel() throws Exception {

        SaveModelManager saveManager = new SaveModelManager();

        PowerMock.mockStatic(DBConnectorFactory.class);


        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);

        
        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dBConnectionMock);        
        

        XMLDBModel modelMock = new XMLDBModel(Utilities.generateId("old"));
        
        modelMock.setModelId(modelMock.getModelName());
        
        String newName = "newModelName";
            
        RenameModelTask taskMock = new RenameModelTask(modelMock, newName);
        
        PowerMock.expectNew(RenameModelTask.class, modelMock, newName).andReturn(taskMock);

        dBConnectionMock.closeConnection();
        
        PowerMock.replayAll();

        try {

            saveManager.renameModel(modelMock, newName);

            assertTrue(true);
            
        } catch (DBConnectionException e) {

            fail("Exception was thrown");
        }
        
        PowerMock.verifyAll();

        
    }
    
    

    /**
     * Test the renaming of a model when the new name is empty.
     * @throws Exception
     */
    @Test
    public void testRenameModel_emptyNewName() throws Exception {

        SaveModelManager saveManager = new SaveModelManager();

        PowerMock.mockStatic(DBConnectorFactory.class);

        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);
        
        EasyMock.expect(modelMock.getModelId()).andReturn("modelId");
        
        String newName = "";
        
        PowerMock.replayAll();

        try {

            saveManager.renameModel(modelMock, newName);

            fail("Should throw IllegralArgumentException...");
            
        } catch (IllegalArgumentException e) {

            assertTrue(true);
        }
        
        PowerMock.verifyAll();
        
    }
    
    
    /**
     * Test the renaming of a model when the original model does not contain 
     * an Id or a name.
     * @throws Exception
     */
    @Test
    public void testRenameModel_NoIdOrNameInOriginal() throws Exception {

        SaveModelManager saveManager = new SaveModelManager();

        PowerMock.mockStatic(DBConnectorFactory.class);       

        XMLDBModel originalModel = new XMLDBModel("");
        
        originalModel.setModelId(null);        
        
        String newName = "modelName";
        
        PowerMock.replayAll();

        try {

            saveManager.renameModel(originalModel, newName);

            fail("Should throw IllegralArgumentException...");
            
        } catch (IllegalArgumentException e) {

            assertTrue(true);
        }
        
        PowerMock.verifyAll();
        
    }
    
    
    /**
     * Test the renaming of a model when the original model does not contain 
     * an Id but contains a name.
     * @throws Exception
     */
    @Test
    public void testRenameModel_NoIdInOriginal() throws Exception {

        SaveModelManager saveManager = new SaveModelManager();

        PowerMock.mockStatic(DBConnectorFactory.class);


        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);

        
        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dBConnectionMock);        
        
        XMLDBModel originalModel = new XMLDBModel("Test");
        
        originalModel.setModelId(null);        
        
        String newName = "modelName";
        
        dBConnectionMock.closeConnection();
        
        PowerMock.replayAll();

        try {

            saveManager.renameModel(originalModel, newName);

            assertTrue(true);
            
        } catch (IllegalArgumentException e) {

            fail("Should not throw an IllegalArgumentException...");
        }
        
        PowerMock.verifyAll();
        
    }
    
    
    /**
     * Test the renaming of a model when the original model contains 
     * an Id but does not contains a name.
     * @throws Exception
     */
    @Test
    public void testRenameModel_NoNameInOriginal() throws Exception {

        SaveModelManager saveManager = new SaveModelManager();

        PowerMock.mockStatic(DBConnectorFactory.class);


        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);

        
        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dBConnectionMock);        
        
        XMLDBModel originalModel = new XMLDBModel(null);
        
        originalModel.setModelId("testId");        
        
        String newName = "modelName";
        
        dBConnectionMock.closeConnection();
        
        PowerMock.replayAll();

        try {

            saveManager.renameModel(originalModel, newName);

            assertTrue(true);
            
        } catch (IllegalArgumentException e) {

            fail("Should not throw an IllegalArgumentException...");
        }
        
        PowerMock.verifyAll();
        
    }
    
}
