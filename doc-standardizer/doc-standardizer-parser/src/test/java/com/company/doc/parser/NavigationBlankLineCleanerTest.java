package com.company.doc.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NavigationBlankLineCleanerTest {
    @Test
    void removesOnlyBlankHeadingParagraphsFromBody() throws Exception {
        File source = File.createTempFile("navigation-blank-source-", ".docx");
        File output = File.createTempFile("navigation-blank-output-", ".docx");
        try {
            try (XWPFDocument document = new XWPFDocument()) {
                CTStyle heading = CTStyle.Factory.newInstance();
                heading.setStyleId("headingOne");
                heading.addNewPPr().addNewOutlineLvl().setVal(java.math.BigInteger.ZERO);
                document.createStyles().addStyle(new XWPFStyle(heading));
                document.createParagraph().setStyle("headingOne");
                document.createParagraph().createRun().setText(" ");
                document.createParagraph().createRun().setText("ordinary body");
                try (FileOutputStream stream = new FileOutputStream(source)) { document.write(stream); }
            }
            assertEquals(1, new NavigationBlankLineCleaner().cleanToCopy(source, output));
            try (FileInputStream input = new FileInputStream(output); XWPFDocument result = new XWPFDocument(input)) {
                assertEquals(2, result.getParagraphs().size());
                assertEquals("ordinary body", result.getParagraphArray(1).getText());
            }
        } finally {
            source.delete(); output.delete();
        }
    }
}
