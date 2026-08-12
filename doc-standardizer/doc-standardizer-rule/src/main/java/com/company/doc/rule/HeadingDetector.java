package com.company.doc.rule;

import com.company.doc.common.enums.DocumentLevel;
import com.company.doc.common.model.DocumentModel;
import com.company.doc.common.model.ParagraphNode;
import com.company.doc.template.TemplateDefinition;
import com.company.doc.template.TemplateStyle;

/** Identifies document headings using template, Word structure and text evidence. */
public class HeadingDetector {
    private final NumberHeadingRule numberRule = new NumberHeadingRule();

    public void detect(DocumentModel model, TemplateDefinition template) {
        for (ParagraphNode paragraph : model.getParagraphs()) {
            DocumentLevel level = detectLevel(paragraph, template);
            paragraph.setLevel(level);
            paragraph.setRole(level == DocumentLevel.NORMAL ? "NORMAL" : level.name());
        }
    }

    private DocumentLevel detectLevel(ParagraphNode paragraph, TemplateDefinition template) {
        TemplateStyle templateStyle = paragraph.getStyleId() == null ? null : template.findStyle(paragraph.getStyleId());
        if (templateStyle != null && templateStyle.getLevel() != null) {
            return convertLevel(templateStyle.getLevel());
        }
        DocumentLevel styleLevel = levelFromWordStyle(paragraph.getStyleId(), paragraph.getStyleName());
        if (styleLevel != DocumentLevel.NORMAL) return styleLevel;
        if (paragraph.getOutlineLevel() != null) return convertLevel(paragraph.getOutlineLevel() + 1);
        return convertLevel(numberRule.matchLevel(paragraph.getText()));
    }

    private DocumentLevel levelFromWordStyle(String styleId, String styleName) {
        String value = ((styleId == null ? "" : styleId) + " " + (styleName == null ? "" : styleName)).toLowerCase();
        if (value.matches(".*(?:heading|标题|標題)\\s*[1１].*")) return DocumentLevel.TITLE_ONE;
        if (value.matches(".*(?:heading|标题|標題)\\s*[2２].*")) return DocumentLevel.TITLE_TWO;
        if (value.matches(".*(?:heading|标题|標題)\\s*[3３].*")) return DocumentLevel.TITLE_THREE;
        return DocumentLevel.NORMAL;
    }

    private DocumentLevel convertLevel(int level) {
        switch (level) {
            case 1: return DocumentLevel.TITLE_ONE;
            case 2: return DocumentLevel.TITLE_TWO;
            case 3: return DocumentLevel.TITLE_THREE;
            default: return DocumentLevel.NORMAL;
        }
    }
}
