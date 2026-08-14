package com.company.doc.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Joins a Chinese heading and its immediately following English translation
 * into one heading with a soft line break. This prevents Word from treating
 * the translation as a second numbered heading.
 */
public class BilingualHeadingJoiner {
    public int joinToCopy(File source, File output) throws Exception {
        if (source.getCanonicalFile().equals(output.getCanonicalFile())) {
            throw new IllegalArgumentException("Join output must be a separate copy.");
        }
        Files.copy(source.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
        int joined = 0;
        try (FileInputStream input = new FileInputStream(output); XWPFDocument document = new XWPFDocument(input)) {
            for (int index = document.getParagraphs().size() - 1; index > 0; index--) {
                XWPFParagraph english = document.getParagraphArray(index);
                XWPFParagraph chinese = document.getParagraphArray(index - 1);
                if (!isBilingualHeadingPair(chinese, english, document)) continue;
                appendWithSoftBreak(chinese, english);
                document.removeBodyElement(document.getPosOfParagraph(english));
                joined++;
            }
            try (FileOutputStream stream = new FileOutputStream(output)) { document.write(stream); }
        }
        return joined;
    }

    private boolean isBilingualHeadingPair(XWPFParagraph chinese, XWPFParagraph english, XWPFDocument document) {
        String chineseText = chinese.getText() == null ? "" : chinese.getText().trim();
        String englishText = english.getText() == null ? "" : english.getText().trim();
        if (chineseText.isEmpty() || englishText.isEmpty() || englishText.length() > 180) return false;
        if (!containsChinese(chineseText) || !isPredominantlyEnglish(englishText)) return false;
        // Only combine text that Word itself is currently treating as a heading.
        return hasHeadingStructure(chinese, document) && hasHeadingStructure(english, document);
    }

    private boolean hasHeadingStructure(XWPFParagraph paragraph, XWPFDocument document) {
        if (paragraph.getCTP().getPPr() != null
                && (paragraph.getCTP().getPPr().getOutlineLvl() != null || paragraph.getCTP().getPPr().getNumPr() != null)) return true;
        XWPFStyle style = document.getStyles() == null ? null : document.getStyles().getStyle(paragraph.getStyle());
        int safetyLimit = 32;
        while (style != null && safetyLimit-- > 0) {
            CTStyle value = style.getCTStyle();
            if (value != null && value.getPPr() != null
                    && (value.getPPr().getOutlineLvl() != null || value.getPPr().getNumPr() != null)) return true;
            String base = style.getBasisStyleID();
            style = base == null || document.getStyles() == null ? null : document.getStyles().getStyle(base);
        }
        return false;
    }

    private boolean containsChinese(String text) {
        for (int i = 0; i < text.length(); i++) if (Character.UnicodeScript.of(text.charAt(i)) == Character.UnicodeScript.HAN) return true;
        return false;
    }

    private boolean isPredominantlyEnglish(String text) {
        int latin = 0, chinese = 0;
        for (int i = 0; i < text.length(); i++) {
            char value = text.charAt(i);
            if (Character.UnicodeScript.of(value) == Character.UnicodeScript.HAN) chinese++;
            else if (Character.isLetter(value) && value <= 127) latin++;
        }
        return latin >= 3 && latin > chinese * 3;
    }

    private void appendWithSoftBreak(XWPFParagraph destination, XWPFParagraph source) {
        // <w:br/> is the Word Shift+Enter soft line break. Do not use a new
        // paragraph or a textWrapping/page break variant here.
        destination.createRun().addBreak();
        for (CTR run : source.getCTP().getRList()) {
            destination.getCTP().addNewR().set(run);
        }
    }
}
