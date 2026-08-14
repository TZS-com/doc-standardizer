package com.company.doc.core;

import com.company.doc.template.TemplateDefinition;
import com.company.doc.template.TemplateStyle;
import com.company.doc.template.WordTemplateParser;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/** Applies only the template's cover-page typography to the first cover block. */
public class CoverStyleApplier {
    public void applyToCopy(File template, File source, File output) throws Exception {
        if (source.getCanonicalFile().equals(output.getCanonicalFile())) {
            throw new IllegalArgumentException("The output must be a separate copy.");
        }
        Files.copy(source.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
        TemplateDefinition styles = new WordTemplateParser().parse(template);
        try (FileInputStream input = new FileInputStream(output); XWPFDocument document = new XWPFDocument(input)) {
            applyCoverParagraphStyles(document.getParagraphs(), styles);
            applyCoverTableStyles(document.getTables(), styles);
            try (FileOutputStream stream = new FileOutputStream(output)) { document.write(stream); }
        }
    }

    private void applyCoverParagraphStyles(List<XWPFParagraph> paragraphs, TemplateDefinition styles) {
        boolean titleAssigned = false;
        for (XWPFParagraph paragraph : paragraphs) {
            String text = paragraph.getText() == null ? "" : paragraph.getText().trim();
            if (text.isEmpty()) continue;
            if (!titleAssigned) {
                setStyle(paragraph, styles.findStyle("af0"));
                titleAssigned = true;
            } else if (text.contains("在下表中签名")) {
                setStyle(paragraph, styles.findStyle("afb"));
            } else if (text.contains("批准") || text.contains("修订索引")) {
                setStyle(paragraph, styles.findStyle("af2"));
            }
            // The cover ends at the first structural heading. Later text is body content.
            if ("10".equals(paragraph.getStyle())) break;
        }
    }

    private void applyCoverTableStyles(List<XWPFTable> tables, TemplateDefinition styles) {
        if (tables.isEmpty()) return;
        applyTable(tables.get(0), styles.findStyle("1e"), styles.findStyle("2c"), styles.findStyle("1f0"));
        if (tables.size() > 1) applyTable(tables.get(1), styles.findStyle("2c"), styles.findStyle("2e"), styles.findStyle("2e"));
    }


    private void applyTable(XWPFTable table, TemplateStyle firstRow, TemplateStyle secondRow, TemplateStyle remainingRows) {
        List<XWPFTableRow> rows = table.getRows();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            TemplateStyle style = rowIndex == 0 ? firstRow : rowIndex == 1 ? secondRow : remainingRows;
            for (XWPFTableCell cell : rows.get(rowIndex).getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) setStyle(paragraph, style);
            }
        }
    }

    private void setStyle(XWPFParagraph paragraph, TemplateStyle style) {
        if (style != null) paragraph.setStyle(style.getStyleId());
    }
}
