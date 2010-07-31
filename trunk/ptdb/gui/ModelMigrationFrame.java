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
/*
 * 
 */
package ptdb.gui;

import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ptdb.kernel.bl.migration.MigrateModelsManager;


///////////////////////////////////////////////////////////////
//// ModelMigrationFrame

/**
 * 
 * Model migration frame that is responsible to provide a GUI for the users to
 * specify a path for a directory that contains the list of models that they
 * wish to migrate to the database.
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 * 
 */

public class ModelMigrationFrame extends JFrame {

    /** Creates new form ModelMigrationFrame. */
    public ModelMigrationFrame() {
        this.setTitle("Migrate Models");
        initComponents();
    }

    

    //////////////////////////////////////////////////////////////////////
    ////                public methods                                ////
    
    /**
     * Entry point for running the migration frame from the command line.
     * 
     * @param args the command line arguments.
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ModelMigrationFrame().setVisible(true);
            }
        });
    }
    
    

    //////////////////////////////////////////////////////////////////////
    ////                private methods                                ////
    
    
    /**
     * Handles the browse button action.
     * <p>
     * Displays a file chooser with the option of selecting directories only.
     * </p>
     * @param evt The action event performed on the browse button.
     */
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {
        
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (fileChooser.showOpenDialog(ModelMigrationFrame.this) == JFileChooser.APPROVE_OPTION) {
            
            String directoryPath = fileChooser.getSelectedFile().getAbsolutePath();
            
            _directoryPathTextField.setText(directoryPath);
            
            // Make sure the text field shows the beginning of the path.
            _directoryPathTextField.getCaret().setDot(0);
        }
    }
    
    /**
     * Handles the close button action.
     * <p>
     * This action will close the window.
     * </p>
     * @param evt The action event performed on the done button.
     */
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.setVisible(false);
        dispose();
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    private void initComponents() {
        
        _jLabel1 = new javax.swing.JLabel();
        _jLabel2 = new javax.swing.JLabel();
        _directoryPathTextField = new javax.swing.JTextField();
        _browseButton = new javax.swing.JButton();
        _migrateButton = new javax.swing.JButton();
        _closeButton = new javax.swing.JButton();
        _resultsTextField = new javax.swing.JTextField();
        _jLabel3 = new javax.swing.JLabel();
        _jLabel4 = new javax.swing.JLabel();
        _jLabel5 = new javax.swing.JLabel();
        _allSubDirectoriesCheckBox = new javax.swing.JCheckBox();
        _checkFileContentCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        setResizable(false);

        _jLabel1.setText("Please select the directory where the Ptolemy models exist.");

        _jLabel2.setText(" Directory Path: ");

        _browseButton.setText("Browse...");
        _browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        _migrateButton.setText("Migrate");
        _migrateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                migrateButtonActionPerformed(evt);
            }
        });

        _closeButton.setText("Close");
        _closeButton.setVisible(true);
        _closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        

        _resultsTextField.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        _resultsTextField.setEditable(false);

        _jLabel3.setText("NOTE:");

        _jLabel4.setText("- Only files with .xml extension will be migrated.");

        _jLabel5.setText("- The model name in the Database will be the name of the file without the extension.");
        
        
        _allSubDirectoriesCheckBox.setSelected(true);
        _allSubDirectoriesCheckBox.setText("Migrate all .xml files under the given folder path and all sub-directories under it.");
        
        _checkFileContentCheckBox.setSelected(true);
        _checkFileContentCheckBox.setText("Validate the file content before migrating the model. Only migrate proper Ptolemy models.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(_resultsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(156, 156, 156)
                .addComponent(_migrateButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_allSubDirectoriesCheckBox)
                    .addComponent(_checkFileContentCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_directoryPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(_browseButton))
                    .addComponent(_jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_jLabel3)
                .addGap(4, 4, 4)
                .addComponent(_jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_jLabel5)
                .addGap(18, 18, 18)
                .addComponent(_jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_jLabel2)
                    .addComponent(_directoryPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_browseButton))
                .addGap(18, 18, 18)
                .addComponent(_allSubDirectoriesCheckBox)
                .addComponent(_checkFileContentCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_migrateButton)
                    .addComponent(_closeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_resultsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        
        pack();
    }

    

    /**
     * Handles the migrate button action by calling _migrateModels() method.
     * @param evt The action event performed on the migrate button.
     */
    private void migrateButtonActionPerformed(java.awt.event.ActionEvent evt) {
        
        String directoryPath = _directoryPathTextField.getText();
        
        if (directoryPath != null && directoryPath.length() > 0) {
            
            _migrateModels(directoryPath);
            
        } else {
            JOptionPane.showMessageDialog(ModelMigrationFrame.this, 
                    "Please specify a path where the models to be migrated exist.", 
                    "Empty Path Error",
                    JOptionPane.INFORMATION_MESSAGE, null);
        }
        


    }
    
    /**
     * Migrate the models from the path given to the database by calling the business layer.
     * @param directoryPath The path where the models to be migrated exist.
     */
    private void _migrateModels(String directoryPath) {
        
        // Clear the results text field.
        _resultsTextField.setText("");
        
        MigrateModelsManager migrateModelsManager = new MigrateModelsManager();
        
        try {
            
            boolean migrateFilesInSubDirectories = _allSubDirectoriesCheckBox.isSelected();
            boolean checkFileContent = _checkFileContentCheckBox.isSelected();
            
            String csvFilePath = migrateModelsManager.migrateModels(
                    directoryPath, migrateFilesInSubDirectories, 
                    checkFileContent);
            
            _resultsTextField.setText("The migration results can be found in: " 
                    + csvFilePath);
            
            // Make sure the text field shows the beginning of the result message.
            _resultsTextField.getCaret().setDot(0);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(ModelMigrationFrame.this, e
                    .getMessage(), "Read/Write Error",
                    JOptionPane.INFORMATION_MESSAGE, null);
        }
        
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////


    private javax.swing.JButton _browseButton;
    private javax.swing.JTextField _directoryPathTextField;
    private javax.swing.JButton _closeButton;
    private javax.swing.JLabel _jLabel1;
    private javax.swing.JLabel _jLabel2;
    private javax.swing.JLabel _jLabel3;
    private javax.swing.JLabel _jLabel4;
    private javax.swing.JLabel _jLabel5;
    private javax.swing.JButton _migrateButton;
    private javax.swing.JTextField _resultsTextField;
    private javax.swing.JCheckBox _allSubDirectoriesCheckBox;
    private javax.swing.JCheckBox _checkFileContentCheckBox;

}
