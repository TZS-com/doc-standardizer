package com.company.doc.parser;

import com.company.doc.common.model.DocumentModel;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumPr;

import java.io.File;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocxParserTest {
    @Test
    void readsOutlineLevelInheritedFromParagraphStyle() throws Exception {
        File file = File.createTempFile("outline-style-", ".docx");
        try {
            try (XWPFDocument document = new XWPFDocument()) {
                CTStyle style = CTStyle.Factory.newInstance();
                style.setStyleId("companyHeadingOne");
                style.addNewName().setVal("公司一级标题");
                style.addNewPPr().addNewOutlineLvl().setVal(java.math.BigInteger.ZERO);
                document.createStyles().addStyle(new XWPFStyle(style));
                document.createParagraph().setStyle("companyHeadingOne");
                document.getParagraphArray(0).createRun().setText("目的");
                try (FileOutputStream output = new FileOutputStream(file)) { document.write(output); }
            }
            DocumentModel result = new DocxParser().parse(file);
            assertEquals(0, result.getParagraphs().get(0).getOutlineLevel());
        } finally {
            file.delete();
        }
    }

    @Test
    void readsAutomaticNumberingLevelWhenRenderedNumberIsNotInParagraphText() throws Exception {
        File file = File.createTempFile("numbering-level-", ".docx");
        try {
            try (XWPFDocument document = new XWPFDocument()) {
                org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph = document.createParagraph();
                CTNumPr numbering = paragraph.getCTP().addNewPPr().addNewNumPr();
                numbering.addNewNumId().setVal(java.math.BigInteger.ONE);
                numbering.addNewIlvl().setVal(java.math.BigInteger.ONE);
                paragraph.createRun().setText("Automatic second-level heading");
                try (FileOutputStream output = new FileOutputStream(file)) { document.write(output); }
            }
            DocumentModel result = new DocxParser().parse(file);
            assertEquals(1, result.getParagraphs().get(0).getNumberingLevel());
            assertEquals(true, result.getParagraphs().get(0).isNumbered());
        } finally {
            file.delete();
        }
    }

    @Test
    void combinesChildNumberingLevelWithParentNumberingInstance() throws Exception {
        File file = File.createTempFile("inherited-numbering-level-", ".docx");
        try {
            try (XWPFDocument document = new XWPFDocument()) {
                CTStyle parent = CTStyle.Factory.newInstance();
                parent.setStyleId("parentNumbering");
                parent.addNewName().setVal("Parent numbering");
                parent.addNewPPr().addNewNumPr().addNewNumId().setVal(java.math.BigInteger.ONE);
                document.createStyles().addStyle(new XWPFStyle(parent));
                CTStyle child = CTStyle.Factory.newInstance();
                child.setStyleId("childNumbering");
                child.addNewName().setVal("Child numbering");
                child.addNewBasedOn().setVal("parentNumbering");
                child.addNewPPr().addNewNumPr().addNewIlvl().setVal(java.math.BigInteger.valueOf(2));
                document.getStyles().addStyle(new XWPFStyle(child));
                document.createParagraph().setStyle("childNumbering");
                document.getParagraphArray(0).createRun().setText("Inherited third-level heading");
                try (FileOutputStream output = new FileOutputStream(file)) { document.write(output); }
            }
            DocumentModel result = new DocxParser().parse(file);
            assertEquals(2, result.getParagraphs().get(0).getNumberingLevel());
        } finally {
            file.delete();
        }
    }
}
