package com.company.doc.template;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyle;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Compiles the labelled examples in the company template into stable semantic
 * roles.  A label ending in “样式” applies to the next non-empty example
 * paragraph; a sample paragraph (for example “一级标题…”) uses its own style.
 */
public class WordTemplateParser implements TemplateParser {
    @Override
    public TemplateDefinition parse(File file) throws Exception {
        TemplateDefinition definition = new TemplateDefinition();
        definition.setName(file.getName());
        Map<String, TemplateStyle> styles = new LinkedHashMap<>();
        try (FileInputStream input = new FileInputStream(file); XWPFDocument document = new XWPFDocument(input)) {
            List<XWPFParagraph> paragraphs = new ArrayList<>(document.getParagraphs());
            for (XWPFParagraph paragraph : paragraphs) addStyle(styles, document, paragraph);
            for (int index = 0; index < paragraphs.size(); index++) {
                XWPFParagraph paragraph = paragraphs.get(index);
                if (!isStyleLabel(paragraph.getText()) && !isHeadingOrListSample(paragraph.getText())) continue;
                String role = roleOfLabel(paragraph.getText());
                if (role == null) continue;
                XWPFParagraph example = isStyleLabel(paragraph.getText()) ? nextNonEmpty(paragraphs, index + 1) : paragraph;
                if (example == null || example.getStyle() == null) continue;
                TemplateStyle style = styles.get(example.getStyle());
                if (style == null) continue;
                assignRole(style, role);
            }
        }
        definition.getStyles().addAll(styles.values());
        return definition;
    }

    private void addStyle(Map<String, TemplateStyle> styles, XWPFDocument document, XWPFParagraph paragraph) {
        String styleId = paragraph.getStyle();
        if (styleId == null || styleId.trim().isEmpty()) return;
        TemplateStyle style = styles.get(styleId);
        if (style == null) {
            style = new TemplateStyle();
            style.setStyleId(styleId);
            XWPFStyle wordStyle = document.getStyles() == null ? null : document.getStyles().getStyle(styleId);
            style.setStyleName(wordStyle == null ? null : wordStyle.getName());
            styles.put(styleId, style);
        }
        style.setUseCount(style.getUseCount() + 1);
    }

    private XWPFParagraph nextNonEmpty(List<XWPFParagraph> paragraphs, int start) {
        for (int index = start; index < paragraphs.size(); index++) {
            if (paragraphs.get(index).getText() != null && !paragraphs.get(index).getText().trim().isEmpty()) return paragraphs.get(index);
        }
        return null;
    }

    private boolean isStyleLabel(String text) {
        return text != null && text.contains("样式");
    }

    private boolean isHeadingOrListSample(String text) {
        if (text == null) return false;
        return text.contains("一级标题") || text.contains("二级标题") || text.contains("三级标题")
                || text.contains("四级标题") || text.contains("有序列表") || text.contains("无序列表");
    }

    private void assignRole(TemplateStyle style, String role) {
        if (style.getRole() != null && !"NORMAL".equals(style.getRole())) return;
        style.setRole(role);
        if (role.startsWith("TITLE_")) {
            style.setHeading(true);
            style.setLevel(Integer.valueOf(role.substring("TITLE_".length()).replace("ONE", "1").replace("TWO", "2").replace("THREE", "3").replace("FOUR", "4")));
        }
    }

    private String roleOfLabel(String text) {
        if (text == null) return null;
        String value = text.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        if (value.contains("无序列表1")) return "NONE_TITLE_ONE";
        if (value.contains("无序列表2")) return "NONE_TITLE_TWO";
        if (value.contains("无序列表3")) return "NONE_TITLE_THREE";
        if (value.contains("有序列表")) return "TITLE_ONE";
        if (value.contains("一级标题")) return "TITLE_ONE";
        if (value.contains("二级标题")) return "TITLE_TWO";
        if (value.contains("三级标题")) return "TITLE_THREE";
        if (value.contains("四级标题")) return "TITLE_FOUR";
        if (value.contains("表头")) return "TABLE_HEADER";
        if (value.contains("表格标题")) return "TABLE_CAPTION";
        if (value.contains("表格文本")) return "TABLE_BODY";
        if (value.contains("页眉")) return "HEADER";
        if (value.contains("页脚")) return "FOOTER";
        if (value.contains("正文文本")) return "NORMAL";
        return null;
    }
}
