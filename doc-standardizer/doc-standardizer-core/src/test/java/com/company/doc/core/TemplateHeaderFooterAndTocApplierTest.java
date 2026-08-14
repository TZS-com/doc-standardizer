package com.company.doc.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateHeaderFooterAndTocApplierTest {
    @Test
    void replacesDefaultHeaderFooterAndEnablesThreeLevelToc() throws Exception {
        File template = new File("D:\\template.docx");
        File input = new File("target", "test-standardized-headings-and-body.docx");
        if (!template.isFile() || !input.isFile()) return;
        File output = new File("target", "test-standardized-final.docx");
        new DocumentProcessor(template).applyTemplateHeaderFooterAndTocTo(input, output);
        try (ZipFile zip = new ZipFile(output)) {
            String header = new String(zip.getInputStream(zip.getEntry("word/header1.xml")).readAllBytes(), StandardCharsets.UTF_8);
            String footer = new String(zip.getInputStream(zip.getEntry("word/footer1.xml")).readAllBytes(), StandardCharsets.UTF_8);
            String headerRelationships = new String(zip.getInputStream(zip.getEntry("word/_rels/header1.xml.rels")).readAllBytes(), StandardCharsets.UTF_8);
            String document = new String(zip.getInputStream(zip.getEntry("word/document.xml")).readAllBytes(), StandardCharsets.UTF_8);
            String settings = new String(zip.getInputStream(zip.getEntry("word/settings.xml")).readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(header.contains("<w:hdr"));
            assertTrue(footer.contains("<w:ftr"));
            assertTrue(headerRelationships.contains("company-header-left.png"));
            assertTrue(headerRelationships.contains("company-header-right.png"));
            assertTrue(zip.getEntry("word/media/company-header-left.png").getSize() > 0);
            assertTrue(zip.getEntry("word/media/company-header-right.png").getSize() > 0);
            assertTrue(document.contains("TOC \\o \"1-3\""));
            assertTrue(document.contains("headerReference"));
            assertTrue(settings.contains("updateFields"));
        }
    }
}
