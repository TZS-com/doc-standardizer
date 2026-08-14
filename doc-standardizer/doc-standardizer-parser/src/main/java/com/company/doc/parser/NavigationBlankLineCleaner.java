package com.company.doc.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPrGeneral;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Deletes only blank body paragraphs that appear in Word navigation as headings. */
public class NavigationBlankLineCleaner {
    public int cleanToCopy(File source, File output) throws Exception {
        if (source.getCanonicalFile().equals(output.getCanonicalFile())) {
            throw new IllegalArgumentException("Cleanup output must be a separate copy.");
        }
        Files.copy(source.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
        int removed = 0;
        try (FileInputStream input = new FileInputStream(output); XWPFDocument document = new XWPFDocument(input)) {
            for (int index = document.getParagraphs().size() - 1; index >= 0; index--) {
                XWPFParagraph paragraph = document.getParagraphArray(index);
                if (!isBlank(paragraph) || !hasOutlineLevel(paragraph, document)) continue;
                document.removeBodyElement(document.getPosOfParagraph(paragraph));
                removed++;
            }
            try (FileOutputStream stream = new FileOutputStream(output)) { document.write(stream); }
        }
        return removed;
    }

    private boolean isBlank(XWPFParagraph paragraph) {
        String text = paragraph.getText();
        return text == null || text.replace('\u00A0', ' ').trim().isEmpty();
    }

    private boolean hasOutlineLevel(XWPFParagraph paragraph, XWPFDocument document) {
        if (outlineOf(paragraph.getCTP().getPPr()) != null) return true;
        XWPFStyle style = document.getStyles() == null ? null : document.getStyles().getStyle(paragraph.getStyle());
        int safetyLimit = 32;
        while (style != null && safetyLimit-- > 0) {
            CTStyle value = style.getCTStyle();
            if (value != null && outlineOf(value.getPPr()) != null) return true;
            String base = style.getBasisStyleID();
            style = base == null || document.getStyles() == null ? null : document.getStyles().getStyle(base);
        }
        return false;
    }

    private Integer outlineOf(CTPPr properties) {
        return properties != null && properties.getOutlineLvl() != null ? properties.getOutlineLvl().getVal().intValue() : null;
    }

    private Integer outlineOf(CTPPrGeneral properties) {
        return properties != null && properties.getOutlineLvl() != null ? properties.getOutlineLvl().getVal().intValue() : null;
    }
}
