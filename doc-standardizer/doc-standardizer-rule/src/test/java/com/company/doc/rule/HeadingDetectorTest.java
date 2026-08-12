package com.company.doc.rule;

import com.company.doc.common.enums.DocumentLevel;
import com.company.doc.common.model.DocumentModel;
import com.company.doc.common.model.ParagraphNode;
import com.company.doc.template.TemplateDefinition;
import com.company.doc.template.TemplateStyle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HeadingDetectorTest {
    @Test
    void recognizesMultipleHeadingSignals() {
        DocumentModel document = new DocumentModel();
        document.getParagraphs().add(paragraph("29", null, null, "项目概述"));
        document.getParagraphs().add(paragraph(null, "Heading 2", null, "范围"));
        document.getParagraphs().add(paragraph(null, null, 2, "自动编号标题"));
        document.getParagraphs().add(paragraph(null, null, null, "1.2.3 技术方案"));
        document.getParagraphs().add(paragraph(null, null, null, "这是普通正文，包含 1.2 但不是标题。"));
        TemplateStyle mapped = new TemplateStyle(); mapped.setStyleId("29"); mapped.setLevel(1);
        TemplateDefinition template = new TemplateDefinition(); template.getStyles().add(mapped);
        new HeadingDetector().detect(document, template);
        assertEquals(DocumentLevel.TITLE_ONE, document.getParagraphs().get(0).getLevel());
        assertEquals(DocumentLevel.TITLE_TWO, document.getParagraphs().get(1).getLevel());
        assertEquals(DocumentLevel.TITLE_THREE, document.getParagraphs().get(2).getLevel());
        assertEquals(DocumentLevel.TITLE_THREE, document.getParagraphs().get(3).getLevel());
        assertEquals(DocumentLevel.NORMAL, document.getParagraphs().get(4).getLevel());
    }
    private ParagraphNode paragraph(String id, String name, Integer outline, String text) {
        ParagraphNode node = new ParagraphNode(); node.setStyleId(id); node.setStyleName(name); node.setOutlineLevel(outline); node.setText(text); return node;
    }
}
