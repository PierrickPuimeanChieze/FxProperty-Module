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
 * http://www.netbeans.org/cddl-gpl v2.html
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.puimean.fxpropertymodule;

/**
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public class AddFXPropertyConfig {
    // Default Add Property template path

    public static final String DEFAULT_TEMPLATE_PATH = "org.puimean.fxpropertymodule/AddFXProperty.freemarker"; // NOI18N
    private String TEMPLATE_PATH = DEFAULT_TEMPLATE_PATH; // NOI18N
    private String name;
    private String initializer;
    private String valueType;
    
    private String propertyType;
    private String propertyTypeImplementation;
    private String className;
    private boolean _static;
    private boolean generateGetter;
    private boolean generateSetter;
    private boolean generateJavadoc = true;


    public AddFXPropertyConfig(
            String name,
            String initializer,
            String valueType,
            String propertyType,
            String propertyTypeImplementation,
            String className,
            boolean _static,
            boolean generateGetter,
            boolean generateSetter,
            boolean generateJavadoc) {
        this.name = name;
        this.initializer = initializer;
        this.valueType = valueType;
        this.propertyType = propertyType;
        this.propertyTypeImplementation = propertyTypeImplementation;
        this.className = className;
        this.generateGetter = generateGetter;
        this.generateSetter = generateSetter;
        this._static = _static;
        this.generateJavadoc = generateJavadoc;

    }

    public String getTEMPLATE_PATH() {
        return TEMPLATE_PATH;
    }

    public void setTEMPLATE_PATH(String TEMPLATE_PATH) {
        this.TEMPLATE_PATH = TEMPLATE_PATH;
    }

    public boolean isStatic() {
        return _static;
    }

    public void setStatic(boolean _static) {
        this._static = _static;
    }

    public boolean getGenerateGetter() {
        return generateGetter;
    }

    public void setGenerateGetter(boolean generateGetter) {
        this.generateGetter = generateGetter;
    }

    public boolean getGenerateSetter() {
        return generateSetter;
    }

    public void setGenerateSetter(boolean generateSetter) {
        this.generateSetter = generateSetter;
    }

    public boolean isGenerateJavadoc() {
        return generateJavadoc;
    }

    public void setGenerateJavadoc(boolean generateJavadoc) {
        this.generateJavadoc = generateJavadoc;
    }

    public String getInitializer() {
        return initializer;
    }

    public void setInitializer(String initializer) {
        this.initializer = initializer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueType() {
        return valueType;
    }

    public void setType(String valueType) {
        this.valueType = valueType;
    }

    public String getClassName() {
        return className;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getPropertyTypeImplementation() {
        return propertyTypeImplementation;
    }

    public void setPropertyTypeImplementation(String propertyTypeImplementation) {
        this.propertyTypeImplementation = propertyTypeImplementation;
    }
    
    
}
