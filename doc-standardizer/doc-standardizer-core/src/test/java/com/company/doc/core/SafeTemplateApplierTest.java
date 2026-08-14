package com.company.doc.core;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumPr;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import com.company.doc.common.model.ParagraphNode;
import com.company.doc.common.model.DocumentModel;
import com.company.doc.common.enums.DocumentLevel;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeTemplateApplierTest {
    @Test
    void writesASeparateStandardizedCopyOfConfiguredDocuments() throws Exception {
        File template = new File("D:\\template.docx");
        File input = new File("D:\\test.docx");
        if (!template.isFile() || !input.isFile()) return;
        File output = new File("target", "test-template-assets-imported.docx");
        Files.deleteIfExists(output.toPath());
        new SafeTemplateApplier().replaceStyleLibrary(template, input, output);
        assertTrue(output.isFile() && output.length() > 0);
        try (FileInputStream stream = new FileInputStream(output); XWPFDocument reopened = new XWPFDocument(stream)) {
            assertTrue(reopened.getParagraphs().size() > 0);
            assertTrue(reopened.getNumbering() != null && reopened.getNumbering().getNums().size() > 0);
            try (FileInputStream templateStream = new FileInputStream(template); XWPFDocument templateDoc = new XWPFDocument(templateStream)) {
                assertTrue(reopened.getStyle().xmlText().equals(templateDoc.getStyle().xmlText()));
            }
        }
    }

    @Test
    void appliesTheTemplateHeadingStyleAfterReplacingLibrary() throws Exception {
        File template = new File("D:\\template.docx");
        File input = new File("D:\\test.docx");
        if (!template.isFile() || !input.isFile()) return;
        File output = new File("target", "test-template-headings-applied.docx");
        DocumentProcessor processor = new DocumentProcessor(template);
        DocumentModel model = processor.process(input);
        new SafeTemplateApplier().replaceLibraryAndApplyRecognizedBodyStyles(template, input, output, model);
        try (FileInputStream stream = new FileInputStream(output); XWPFDocument reopened = new XWPFDocument(stream)) {
            assertTrue(reopened.getParagraphs().stream().anyMatch(paragraph -> "10".equals(paragraph.getStyle())));
            return;
        }
    }

    @Test
    void appliesTemplateBodyTextToRecognizedNormalParagraphs() throws Exception {
        File template = new File("D:\\template.docx");
        File input = new File("D:\\test.docx");
        if (!template.isFile() || !input.isFile()) return;
        File output = new File("target", "test-heading-and-body-applied.docx");
        DocumentProcessor processor = new DocumentProcessor(template);
        processor.standardizeHeadingAndBodyTo(input, output);
        try (FileInputStream stream = new FileInputStream(output); XWPFDocument reopened = new XWPFDocument(stream)) {
            assertTrue(reopened.getParagraphs().stream().anyMatch(p -> "aa".equals(p.getStyle())));
            assertTrue(reopened.getParagraphs().stream().anyMatch(p -> "10".equals(p.getStyle())));
            assertTrue(reopened.getParagraphs().stream().anyMatch(p -> "20".equals(p.getStyle())));
            assertTrue(reopened.getParagraphs().stream().anyMatch(p -> "3".equals(p.getStyle())));
            assertHeadingNumbering(reopened, "10", 0);
            assertHeadingNumbering(reopened, "20", 1);
            assertHeadingNumbering(reopened, "3", 2);
        }
    }

    @Test
    void doesNotTurnOrdinaryListsIntoNumberedHeadings() throws Exception {
        File template = new File("D:\\template.docx");
        File input = new File("D:\\test.docx");
        if (!template.isFile() || !input.isFile()) return;
        File output = new File("target", "test-standardized-headings-and-body.docx");
        new DocumentProcessor(template).standardizeHeadingAndBodyTo(input, output);
        try (FileInputStream stream = new FileInputStream(output); XWPFDocument reopened = new XWPFDocument(stream)) {
            for (XWPFParagraph paragraph : reopened.getParagraphs()) {
                if (paragraph.getText().contains("FH =")) {
                    assertTrue(!"10".equals(paragraph.getStyle()));
                }
            }
        }
    }

    @Test
    void respectsNumberingLevelsWhenOutlineLevelsAreIncorrect() throws Exception {
        File template = new File("D:\\template.docx");
        File input = new File("D:\\test.docx");
        if (!template.isFile() || !input.isFile()) return;
        DocumentModel model = new DocumentProcessor(template).process(input);
        boolean secondLevel = false;
        boolean thirdLevel = false;
        for (com.company.doc.common.model.ParagraphNode node : model.getParagraphs()) {
            if (node.getText() == null) continue;
            if (node.getText().contains("标准操作规程确认SOP Verification")) secondLevel = node.getLevel() == DocumentLevel.TITLE_TWO;
            if (node.getText().equals("目的Purpose") && node.getNumberingLevel() != null && node.getNumberingLevel() == 2) thirdLevel = node.getLevel() == DocumentLevel.TITLE_THREE;
        }
        assertTrue(secondLevel, "standard operating procedure heading must be Heading 2");
        assertTrue(thirdLevel, "Purpose must be Heading 3 when ilvl=2");
    }

    @Test
    void exportsHeadingTreeBeforeAnyDocumentWriteBack() throws Exception {
        File template = new File("D:\\template.docx");
        File input = new File("D:\\test.docx");
        if (!template.isFile() || !input.isFile()) return;
        File report = new File("target", "test-heading-tree.txt");
        new DocumentProcessor(template).exportHeadingTree(input, report);
        assertTrue(report.isFile() && report.length() > 0);
        String text = Files.readString(report.toPath(), StandardCharsets.UTF_8);
        assertTrue(text.contains("Corrected heading tree"));
    }

    @Test
    void replacesLegacyOrderedListStyleWithTemplateListStyle() throws Exception {
        File template = new File("D:\\template.docx");
        if (!template.isFile()) return;
        File input = File.createTempFile("legacy-list", ".docx");
        File output = File.createTempFile("template-list", ".docx");
        try {
            try (XWPFDocument document = new XWPFDocument(); FileOutputStream stream = new FileOutputStream(input)) {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.createRun().setText("List item");
                paragraph.setStyle("104");
                document.write(stream);
            }
            DocumentModel model = new DocumentModel();
            ParagraphNode node = new ParagraphNode();
            node.setLocation("BODY");
            node.setRole("NORMAL");
            node.setNumberingLevel(0);
            model.getParagraphs().add(node);
            new SafeTemplateApplier().replaceLibraryAndApplyRecognizedBodyStyles(template, input, output, model);
            try (FileInputStream stream = new FileInputStream(output); XWPFDocument document = new XWPFDocument(stream)) {
                assertTrue("11".equals(document.getParagraphArray(0).getStyle()));
            }
        } finally {
            Files.deleteIfExists(input.toPath());
            Files.deleteIfExists(output.toPath());
        }
    }

    private void assertHeadingNumbering(XWPFDocument document, String style, int expectedLevel) {
        for (org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph : document.getParagraphs()) {
            if (!style.equals(paragraph.getStyle()) || paragraph.getCTP().getPPr() == null) continue;
            CTNumPr numbering = paragraph.getCTP().getPPr().getNumPr();
            if (numbering == null) continue;
            int level = numbering.getIlvl() == null ? 0 : numbering.getIlvl().getVal().intValue();
            if (numbering.getNumId() != null && numbering.getNumId().getVal().intValue() == 28 && level == expectedLevel) return;
        }
        throw new AssertionError("missing template numbering for style " + style);
    }

}
