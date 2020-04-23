/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRFunctionWrapper;

/**
 * Wrapper of Java class fields.
 *
 * @since 1.2.0
 */
class JFieldFunctionWrapper extends BIRFunctionWrapper implements ExternalFunctionWrapper {

    JavaField jField;

    JFieldFunctionWrapper(BIRFunctionWrapper functionWrapper, JavaField jField) {

        super(functionWrapper.orgName, functionWrapper.moduleName, functionWrapper.version, functionWrapper.func,
                functionWrapper.fullQualifiedClassName, functionWrapper.jvmMethodDescription,
                functionWrapper.jvmMethodDescriptionBString);
        this.jField = jField;
    }
}
