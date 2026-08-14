package com.company.doc.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deliberately read-only test entry point for reviewing the corrected heading
 * hierarchy before running any document style replacement.
 */
class HeadingTreeReportTest {
    @Test
    void exportsCorrectedHeadingTreeOnly() throws Exception {
        File template = new File("D:\\template.docx");
        File input = new File("D:\\test.docx");
        if (!template.isFile() || !input.isFile()) return;

        File report = new File("target", "test-heading-tree.txt");
        new DocumentProcessor(template).exportHeadingTree(input, report);

        assertTrue(report.isFile() && report.length() > 0);
        String text = Files.readString(report.toPath(), StandardCharsets.UTF_8);
        assertTrue(text.contains("Corrected heading tree"));
        assertFalse(text.contains("| style=TOC"));
        assertFalse(text.contains("FH ="));
    }
}
