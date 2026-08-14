package com.company.doc.core;

import com.company.doc.template.TemplateDefinition;
import com.company.doc.template.TemplateStyle;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ArticleTableStyleApplierTest {
    @Test
    void stylesArticleCaptionsAndTableRowsWithoutChangingCoverTables() throws Exception {
        File input = Files.createTempFile("article-table-input", ".docx").toFile();
        File output = Files.createTempFile("article-table-output", ".docx").toFile();
        try {
            try (XWPFDocument document = new XWPFDocument()) {
                XWPFTable coverTable = document.createTable(2, 1);
                coverTable.getRow(0).getCell(0).setText("cover header");
                coverTable.getRow(1).getCell(0).setText("cover body");
                XWPFParagraph heading = document.createParagraph();
                heading.createRun().setText("1 Scope");
                heading.setStyle("heading-1");
                XWPFParagraph caption = document.createParagraph();
                caption.createRun().setText("Table 1 - Article data");
                XWPFTable articleTable = document.createTable(2, 1);
                articleTable.getRow(0).getCell(0).setText("header");
                articleTable.getRow(1).getCell(0).setText("body");
                try (FileOutputStream stream = new FileOutputStream(input)) {
                    document.write(stream);
                }
            }

            new ArticleTableStyleApplier().applyToCopy(template(), input, output);

            try (FileInputStream stream = new FileInputStream(output); XWPFDocument document = new XWPFDocument(stream)) {
                assertNull(document.getTables().get(0).getRow(0).getCell(0).getParagraphArray(0).getStyle());
                assertEquals("caption", document.getParagraphs().stream()
                        .filter(paragraph -> paragraph.getText().startsWith("Table 1"))
                        .findFirst().orElseThrow().getStyle());
                assertEquals("table-header", document.getTables().get(1).getRow(0).getCell(0).getParagraphArray(0).getStyle());
                assertEquals("table-body", document.getTables().get(1).getRow(1).getCell(0).getParagraphArray(0).getStyle());
            }
        } finally {
            Files.deleteIfExists(input.toPath());
            Files.deleteIfExists(output.toPath());
        }
    }

    private TemplateDefinition template() {
        TemplateDefinition definition = new TemplateDefinition();
        definition.getStyles().add(style("heading-1", "TITLE_ONE"));
        definition.getStyles().add(style("caption", "TABLE_CAPTION"));
        definition.getStyles().add(style("table-header", "TABLE_HEADER"));
        definition.getStyles().add(style("table-body", "TABLE_BODY"));
        return definition;
    }

    private TemplateStyle style(String id, String role) {
        TemplateStyle style = new TemplateStyle();
        style.setStyleId(id);
        style.setRole(role);
        return style;
    }
}
