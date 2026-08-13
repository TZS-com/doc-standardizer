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
        if (isBlank(paragraph.getText())) return DocumentLevel.NORMAL;
        if (isTocStyle(paragraph.getStyleId(), paragraph.getStyleName())) return DocumentLevel.NORMAL;
        TemplateStyle templateStyle = paragraph.getStyleId() == null ? null : template.findStyle(paragraph.getStyleId());
        if (templateStyle != null && templateStyle.getLevel() != null) {
            return convertLevel(templateStyle.getLevel());
        }
        DocumentLevel styleLevel = levelFromWordStyle(paragraph.getStyleId(), paragraph.getStyleName());
        if (styleLevel != DocumentLevel.NORMAL) return styleLevel;
        if (paragraph.getNumberingLevel() != null) {
            return paragraph.isBulletNumbering() && paragraph.getNumberingLevel() == 0
                    ? DocumentLevel.NONE_TITLE_ONE
                    : convertLevel(paragraph.getNumberingLevel() + 1);
        }
        // Some Word files write outlineLvl=0 on every numbered heading. When
        // numbering exists, ilvl is the reliable hierarchy (11.1 is level 2).
        if (paragraph.getOutlineLevel() != null && isOutlineHeadingCandidate(paragraph.getText())) {
            return convertLevel(paragraph.getOutlineLevel() + 1);
        }
        return convertLevel(numberRule.matchLevel(paragraph.getText()));
    }

    /** Prevent full English body sentences carrying a corrupt outlineLvl=0 from becoming headings. */
    private boolean isOutlineHeadingCandidate(String text) {
        if (isBlank(text)) return false;
        String value = text.trim();
        // A corrupt outlineLvl is common in legacy documents. Chinese body
        // prose must not become a heading merely because that property exists.
        if (containsChinese(value)) {
            return value.length() <= 80 && !value.matches(".*[。；！？].*");
        }
        if (value.length() > 120) return false;
        // English-only outline candidates are allowed only for short labels,
        // never prose, equations or field output.
        return value.matches("^[A-Za-z][A-Za-z /&()\\-]{1,79}$");
    }

    private boolean containsChinese(String text) {
        for (int index = 0; index < text.length(); index++) {
            if (Character.UnicodeScript.of(text.charAt(index)) == Character.UnicodeScript.HAN) return true;
        }
        return false;
    }

    private boolean isBlank(String text) {
        return text == null || text.replace('\u00A0', ' ').trim().isEmpty();
    }

    private boolean isTocStyle(String styleId, String styleName) {
        String value = ((styleId == null ? "" : styleId) + " " + (styleName == null ? "" : styleName)).toLowerCase();
        return value.contains("toc") || value.contains("目录");
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
