package com.github.epoth.webcomponents.generator;

import com.google.common.annotations.GwtIncompatible;


/**
 * Copyright 2022 Eric Ponthiaux -/- ponthiaux.eric@gmail.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@GwtIncompatible
public class ClassNameUtils {

    public static String lowerCaseClassName(String className) {

        return className.substring(className.lastIndexOf('.') + 1).toLowerCase();

    }


    public static String simpleClassName(String className) {

        return className.substring(className.lastIndexOf('.') + 1);

    }


    public static String packagePath(String className) {

        return className.substring(0, className.lastIndexOf('.')).replaceAll("\\.", "/");

    }


}



