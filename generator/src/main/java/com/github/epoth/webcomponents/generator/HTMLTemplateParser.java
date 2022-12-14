package com.github.epoth.webcomponents.generator;

import com.google.common.annotations.GwtIncompatible;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.processing.ProcessingEnvironment;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
public class HTMLTemplateParser {

    private static final String ID = "id";
    private static String[] eventsAttributes;
    private static String[] componentAttributes;

    static {

        eventsAttributes = new String[]{"@afterprint", "@beforeprint", "@beforeunload", "@error", "@hashchange", "@load", "@message", "@offline", "@online", "@pagehide", "@pageshow", "@popstate", "@resize", "@storage", "@unload", "@blur", "@change", "@contextmenu", "@focus", "@input", "@invalid", "@reset", "@search", "@select", "@submit", "@keydown", "@keypress", "@keyup", "@click", "@dblclick", "@mousedown", "@mousemove", "@mouseout", "@mouseover", "@mouseup", "@mousewheel", "@wheel", "@drag", "@dragend", "@dragenter", "@dragleave", "@dragover", "@dragstart", "@drop", "@scroll", "@copy", "@cut", "@paste", "@abort", "@canplay", "@canplaythrough", "@cuechange", "@durationchange", "@emptied", "@ended", "@error", "@loadeddata", "@loadedmetadata", "@loadstart", "@pause", "@play", "@playing", "@progress", "@ratechange", "@seeked", "@seeking", "@stalled", "@suspend", "@timeupdate", "@volumechange", "@waiting", "@toggle"};

        componentAttributes = new String[]{"@field"};

    }

    private int identifierCount = 0;

    public TemplateParserResult parse(

            ProcessingEnvironment processingEnvironment,

            String templateContents

    ) {

        TemplateParserResult result = new TemplateParserResult();

        Document document = Jsoup.parse(templateContents);

        for (String componentAttributeName : componentAttributes) {

            Elements elements = document.select("[" + componentAttributeName + "]");

            if (!elements.isEmpty()) {

                for (Element element : elements) {

                    TemplateBinding templateBinding = new TemplateBinding(TemplateBinding.FIELD);

                    /* */

                    String id = null;

                    if (element.attr(ID) != null && !element.attr(ID).trim().equals("")) {

                        id = element.attr(ID);

                    } else {

                        // If using HTML4 ids must start with a letter

                        id = Base64
                                .getEncoder()
                                .encodeToString(("" + (identifierCount++))
                                        .getBytes(StandardCharsets.UTF_8))
                                .replaceAll("=", "");

                        element.attr(ID, id);

                    }

                    /* */

                    templateBinding.setId(id);
                    templateBinding.setField(element.attr(componentAttributeName));

                    result.bindingList.add(templateBinding);

                    element.removeAttr(componentAttributeName);

                }

            }


        }

        for (String eventAttributeName : eventsAttributes) {

            Elements elements = document.select("[" + eventAttributeName + "]");

            if (!elements.isEmpty()) {

                for (Element element : elements) {

                    TemplateBinding templateBinding = new TemplateBinding(TemplateBinding.FUNCTION);

                    /* */

                    String id = null;

                    if (element.attr(ID) != null && !element.attr(ID).trim().equals("")) {

                        id = element.attr(ID);

                    } else {

                        // If using HTML4 ids must start with a letter

                        id = Base64
                                .getEncoder()
                                .encodeToString(("" + (identifierCount++))
                                        .getBytes(StandardCharsets.UTF_8))
                                .replaceAll("=", "");

                        element.attr(ID, id);

                    }

                    /* */

                    templateBinding.setId(id);
                    templateBinding.setEvent(eventAttributeName.substring(1));
                    templateBinding.setFunction(element.attr(eventAttributeName));

                    result.bindingList.add(templateBinding);

                    element.removeAttr(eventAttributeName);

                }

            }

        }

        result.parserOutput = document.head().html() + document.body().html();

        return result;

    }

    public static class TemplateParserResult {

        List<TemplateBinding> bindingList = new ArrayList<>();

        String parserOutput;

    }

}


