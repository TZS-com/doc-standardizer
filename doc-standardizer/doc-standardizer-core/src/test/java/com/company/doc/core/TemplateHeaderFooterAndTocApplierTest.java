package com.company.doc.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateHeaderFooterAndTocApplierTest {
    @Test
    void replacesDefaultHeaderFooterAndKeepsExistingTocField() throws Exception {
        File template = new File("D:\\template.docx");
        File input = new File("target", "test-standardized-headings-and-body.docx");
        if (!template.isFile() || !input.isFile()) return;
        File output = new File("target", "test-standardized-final.docx");
        String originalDocument;
        try (ZipFile source = new ZipFile(input)) {
            originalDocument = new String(source.getInputStream(source.getEntry("word/document.xml")).readAllBytes(), StandardCharsets.UTF_8);
        }
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
            assertEquals(tocInstruction(originalDocument), tocInstruction(document));
            assertTrue(document.contains("headerReference"));
            assertTrue(settings.contains("<w:updateFields w:val=\"true\"/>"));
        }
    }

    private String tocInstruction(String documentXml) {
        return documentXml.replaceAll("(?s).*?(<w:instrText[^>]*>[^<]*TOC[^<]*</w:instrText>).*", "$1");
    }
}
