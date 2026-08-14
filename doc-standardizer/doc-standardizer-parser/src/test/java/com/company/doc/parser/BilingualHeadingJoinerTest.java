package com.company.doc.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BilingualHeadingJoinerTest {
    @Test
    void joinsOnlyAdjacentChineseAndEnglishHeadings() throws Exception {
        File source = File.createTempFile("bilingual-heading-source-", ".docx");
        File output = File.createTempFile("bilingual-heading-output-", ".docx");
        try {
            try (XWPFDocument document = new XWPFDocument()) {
                document.createParagraph().getCTP().addNewPPr().addNewOutlineLvl().setVal(java.math.BigInteger.ONE);
                document.getParagraphArray(0).createRun().setText("标准操作规程确认");
                document.createParagraph().getCTP().addNewPPr().addNewOutlineLvl().setVal(java.math.BigInteger.ONE);
                document.getParagraphArray(1).createRun().setText("SOP Verification");
                document.createParagraph().createRun().setText("This is normal English body text and must remain separate.");
                try (FileOutputStream stream = new FileOutputStream(source)) { document.write(stream); }
            }
            assertEquals(1, new BilingualHeadingJoiner().joinToCopy(source, output));
            try (FileInputStream stream = new FileInputStream(output); XWPFDocument result = new XWPFDocument(stream)) {
                assertEquals(2, result.getParagraphs().size());
                assertTrue(result.getParagraphArray(0).getText().contains("标准操作规程确认"));
                assertTrue(result.getParagraphArray(0).getText().contains("SOP Verification"));
                assertTrue(result.getParagraphArray(0).getCTP().xmlText().contains("<w:br"));
                assertTrue(result.getParagraphArray(1).getText().startsWith("This is normal"));
            }
        } finally {
            source.delete(); output.delete();
        }
    }
}
