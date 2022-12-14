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
public class TemplateBinding {

    public static final int FIELD = 0;
    public static final int FUNCTION = 1;

    private String id;
    private String field;
    private String function;

    private String event;
    private int type;

    public TemplateBinding(int type) {

        this.type = type;

    }

    public int getType() {

        return type;
    }

    public void setType(int type) {

        this.type = type;

    }

    public String getId() {

        return id;

    }

    public void setId(String id) {

        this.id = id;

    }

    public String getFunction() {

        return function;

    }

    public void setFunction(String function) {

        this.function = function;

    }

    public String getField() {

        return field;

    }

    public void setField(String field) {

        this.field = field;

    }

    public String getEvent() {

        return event;

    }

    public void setEvent(String event) {

        this.event = event;

    }
}
