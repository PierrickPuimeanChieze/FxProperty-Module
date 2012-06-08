/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.puimean.fxpropertymodule;

import java.awt.Rectangle;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javax.lang.model.element.TypeElement;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.tools.JavaCompiler;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.api.java.source.ui.TypeElementFinder.Customizer;


import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * A simple GUI for Add Property action.
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public class AddFXPropertyPanel extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(AddFXPropertyPanel.class.getName());
    private FileObject file;
    private String className;
    private List<String> existingFields;
    private String[] pcsName;
    private String[] vcsName;
    private JButton okButton;

    public AddFXPropertyPanel(FileObject file, String className, List<String> existingFields, String[] pcsName, String[] vcsName, JButton okButton) {

        this.file = file;
        this.className = className;
        this.existingFields = existingFields;
        this.pcsName = pcsName;
        this.vcsName = vcsName;
        this.okButton = okButton;
        initComponents();
        previewScrollPane.putClientProperty(
                "HighlightsLayerExcludes", // NOI18N
                "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" // NOI18N
                );

        DocumentListener documentListener = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                showPreview();
            }

            public void removeUpdate(DocumentEvent e) {
                showPreview();
            }

            public void changedUpdate(DocumentEvent e) {
                showPreview();
            }
        };
        nameTextField.getDocument().addDocumentListener(documentListener);
        initializerTextField.getDocument().addDocumentListener(documentListener);
        valueTypeTextField.getDocument().addDocumentListener(documentListener);
        propertyTypeTextField.getDocument().addDocumentListener(documentListener);
        propertyTypeImplementationTextField.getDocument().addDocumentListener(documentListener);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        previewEditorPane.setText("");

        showPreview();
    }

    private void showPreview() {

        final String previewTemplate = new AddFXPropertyGenerator().generate(getAddPropertyConfig());
        previewEditorPane.setText(previewTemplate);

        String error = resolveError();

        if (error != null) {
            errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/error-glyph.gif"))); // NOI18N
            errorLabel.setText(error);
        }

        okButton.setEnabled(error == null);

        String warning = resolveWarning();

        if (warning != null) {
            errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/warning.gif"))); // NOI18N
            errorLabel.setText(warning);
        }

        errorLabel.setVisible(error != null || warning != null);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                previewEditorPane.setCaretPosition(0);
                previewEditorPane.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
            }
        });
    }

    private String resolveError() {
        if (nameTextField.getText().length() == 0) {
            return NbBundle.getMessage(AddFXPropertyPanel.class, "ERR_FieldIsEmpty");
        }

        if (valueTypeTextField.getText().length() == 0) {
            return NbBundle.getMessage(AddFXPropertyPanel.class, "ERR_TypeIsEmpty");
        }


        if (existingFields.contains(nameTextField.getText())) {
            return NbBundle.getMessage(AddFXPropertyPanel.class, "ERR_FieldAlreadyExists", new Object[]{String.valueOf(nameTextField.getText())});
        }

        return null;
    }

    private String resolveWarning() {
        return null;
    }

    public AddFXPropertyConfig getAddPropertyConfig() {
        final String valueType = valueTypeTextField.getText().trim();

        final String name = nameTextField.getText().trim();
        final String initializer = initializerTextField.getText().trim();
        final String propertyType = propertyTypeTextField.getText().trim();
        final String propertyImplementationType = propertyTypeImplementationTextField.getText().trim();



        int i = staticCheckBox.isSelected() ? 1 : 0;
        AddFXPropertyConfig addPropertyConfig = new AddFXPropertyConfig(
                name, initializer, valueType, propertyType, propertyImplementationType, className, staticCheckBox.isSelected(), generateGetterCheckBox.isSelected(), jCheckBox1.isSelected(), generateJavadocCheckBox.isSelected());
        return addPropertyConfig;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        initializerTextField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        browseValueTypeButton = new javax.swing.JButton();
        staticCheckBox = new javax.swing.JCheckBox();
        generateJavadocCheckBox = new javax.swing.JCheckBox();
        previewLabel = new javax.swing.JLabel();
        previewScrollPane = new javax.swing.JScrollPane();
        previewEditorPane = new javax.swing.JEditorPane();
        errorLabel = new javax.swing.JLabel();
        generateGetterCheckBox = new javax.swing.JCheckBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        propertyTypeTextField = new javax.swing.JTextField();
        propertyTypeImplementationTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        valueTypeTextField = new javax.swing.JTextField();
        browsePropertyTypeButton = new javax.swing.JButton();
        browsePropertyImplementationType = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(800, 633));

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.nameLabel.text")); // NOI18N

        nameTextField.setText(org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.nameTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.typeLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseValueTypeButton, org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.browseValueTypeButton.text")); // NOI18N
        browseValueTypeButton.setToolTipText(org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.browseValueTypeButton.toolTipText")); // NOI18N
        browseValueTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseValueTypeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(staticCheckBox, org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.staticCheckBox.text")); // NOI18N
        staticCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                staticCheckBoxActionPerformed(evt);
            }
        });

        generateJavadocCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(generateJavadocCheckBox, org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.generateJavadocCheckBox.text")); // NOI18N
        generateJavadocCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateJavadocCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(previewLabel, org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.previewLabel.text")); // NOI18N

        previewEditorPane.setContentType(org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.previewEditorPane.contentType")); // NOI18N
        previewEditorPane.setEditable(false);
        previewEditorPane.setText(org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.previewEditorPane.text")); // NOI18N
        previewEditorPane.setToolTipText(org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.previewEditorPane.toolTipText")); // NOI18N
        previewScrollPane.setViewportView(previewEditorPane);

        errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/error-glyph.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getBundle(AddFXPropertyPanel.class).getString("AddFXPropertyPanel.errorLabel.text")); // NOI18N

        generateGetterCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(generateGetterCheckBox, org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.generateGetterCheckBox.text")); // NOI18N
        generateGetterCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.generateGetterCheckBox.toolTipText")); // NOI18N
        generateGetterCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                generateGetterCheckBoxItemStateChanged(evt);
            }
        });

        jCheckBox1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.generateSetterCheckBox.text")); // NOI18N
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.jLabel2.text")); // NOI18N

        valueTypeTextField.setText(org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.valueTypeTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browsePropertyTypeButton, org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.browsePropertyTypeButton.text")); // NOI18N
        browsePropertyTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browsePropertyTypeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(browsePropertyImplementationType, org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.browsePropertyImplementationType.text")); // NOI18N
        browsePropertyImplementationType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browsePropertyImplementationTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameLabel)
                            .addComponent(typeLabel)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(initializerTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(staticCheckBox)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(generateGetterCheckBox)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jCheckBox1))
                                    .addComponent(generateJavadocCheckBox))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(propertyTypeImplementationTextField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(propertyTypeTextField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(valueTypeTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(browseValueTypeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(browsePropertyTypeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(browsePropertyImplementationType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(previewLabel)
                        .addGap(100, 100, 100)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(errorLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(previewScrollPane))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {nameLabel, previewLabel, typeLabel});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(initializerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(browseValueTypeButton)
                    .addComponent(valueTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(propertyTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(propertyTypeImplementationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(browsePropertyImplementationType))
                        .addGap(18, 18, 18)
                        .addComponent(staticCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(generateGetterCheckBox)
                            .addComponent(jCheckBox1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generateJavadocCheckBox))
                    .addComponent(browsePropertyTypeButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(previewScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                        .addGap(12, 12, 12)
                        .addComponent(errorLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(previewLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddFXPropertyPanel.class, "AddFXPropertyPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void staticCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_staticCheckBoxActionPerformed
        showPreview();
    }//GEN-LAST:event_staticCheckBoxActionPerformed

    private void generateJavadocCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateJavadocCheckBoxActionPerformed
        showPreview();
    }//GEN-LAST:event_generateJavadocCheckBoxActionPerformed

    private void browseValueTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseValueTypeButtonActionPerformed
        ElementHandle<TypeElement> type = TypeElementFinder.find(ClasspathInfo.create(file), null);

        if (type != null) {
            String fqn = type.getQualifiedName().toString();
            valueTypeTextField.setText(fqn);
        }
    }//GEN-LAST:event_browseValueTypeButtonActionPerformed

    private void generateGetterCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_generateGetterCheckBoxItemStateChanged
        showPreview();
    }//GEN-LAST:event_generateGetterCheckBoxItemStateChanged

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
        showPreview();
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void browsePropertyTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browsePropertyTypeButtonActionPerformed
        final Customizer customizer = new Customizer() {

            @Override
            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {


                final ClassIndex index = classpathInfo.getClassIndex();
                final Set<ElementHandle<TypeElement>> declaredTypes = index.getDeclaredTypes(textForQuery, nameKind, searchScopes);
                final Set<ElementHandle<TypeElement>> returnedTypes = new HashSet<>();
                for (ElementHandle<TypeElement> elementHandle : declaredTypes) {
                    String var = elementHandle.getBinaryName();
                    try {
                        final ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);

                        Class testClass = classLoader.loadClass(var);
                        if (Property.class.isAssignableFrom(testClass)) {
                            returnedTypes.add(elementHandle);
                        }
                    } catch (ClassNotFoundException ex) {
                    }
                }
                return returnedTypes;
            }

            @Override
            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                final ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);

                Class testClass;
                try {
                    testClass = classLoader.loadClass(typeHandle.getBinaryName());
                } catch (ClassNotFoundException ex) {
                    return false;
                }
                return Property.class.isAssignableFrom(testClass);

            }
        };
        ElementHandle<TypeElement> type = TypeElementFinder.find(ClasspathInfo.create(file), customizer);

        if (type != null) {
            String fqn = type.getQualifiedName().toString();
            propertyTypeTextField.setText(fqn);
        }
    }//GEN-LAST:event_browsePropertyTypeButtonActionPerformed

    private void browsePropertyImplementationTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browsePropertyImplementationTypeActionPerformed
        final Customizer customizer = new Customizer() {

            @Override
            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {

                final ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
                final ClassIndex index = classpathInfo.getClassIndex();
                final Set<ElementHandle<TypeElement>> declaredTypes = index.getDeclaredTypes(textForQuery, nameKind, searchScopes);
                final Set<ElementHandle<TypeElement>> returnedTypes = new HashSet<>();
                for (ElementHandle<TypeElement> elementHandle : declaredTypes) {
                    String var = elementHandle.getBinaryName();
                    try {

                        Class testClass = classLoader.loadClass(var);
                        if (Property.class.isAssignableFrom(testClass)) {
                            returnedTypes.add(elementHandle);
                        }
                    } catch (ClassNotFoundException ex) {
                    }
                }
                return returnedTypes;
            }

            @Override
            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                System.out.println(typeHandle.getBinaryName());
                return true;

            }
        };
        ElementHandle<TypeElement> type = TypeElementFinder.find(ClasspathInfo.create(file), customizer);

        if (type != null) {
            String fqn = type.getQualifiedName().toString();
            propertyTypeImplementationTextField.setText(fqn);
        }
    }//GEN-LAST:event_browsePropertyImplementationTypeActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browsePropertyImplementationType;
    private javax.swing.JButton browsePropertyTypeButton;
    private javax.swing.JButton browseValueTypeButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JCheckBox generateGetterCheckBox;
    private javax.swing.JCheckBox generateJavadocCheckBox;
    private javax.swing.JTextField initializerTextField;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JEditorPane previewEditorPane;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JScrollPane previewScrollPane;
    private javax.swing.JTextField propertyTypeImplementationTextField;
    private javax.swing.JTextField propertyTypeTextField;
    private javax.swing.JCheckBox staticCheckBox;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JTextField valueTypeTextField;
    // End of variables declaration//GEN-END:variables
}
