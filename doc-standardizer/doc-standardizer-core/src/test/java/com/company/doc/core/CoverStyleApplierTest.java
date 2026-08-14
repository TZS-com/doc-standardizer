package com.company.doc.core;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoverStyleApplierTest {
    @Test
    void appliesTemplateTypographyToCoverOnly() throws Exception {
        File template = new File("D:\\template.docx");
        File input = new File("target", "test-standardized-final.docx");
        if (!template.isFile() || !input.isFile()) return;
        File output = new File("target", "test-standardized-cover-styled.docx");
        new DocumentProcessor(template).applyCoverStylesTo(input, output);
        try (FileInputStream stream = new FileInputStream(output); XWPFDocument document = new XWPFDocument(stream)) {
            assertEquals("af0", document.getParagraphArray(7).getStyle());
            assertEquals("af2", document.getParagraphArray(25).getStyle());
            assertEquals("afb", document.getParagraphArray(26).getStyle());
            assertEquals("1e", document.getTables().get(0).getRow(0).getCell(0).getParagraphArray(0).getStyle());
            assertEquals("2e", document.getTables().get(1).getRow(1).getCell(0).getParagraphArray(0).getStyle());
        }
    }
}
