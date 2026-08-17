package com.company.doc.core;

import com.company.doc.template.TemplateDefinition;
import com.company.doc.template.TemplateStyle;
import com.company.doc.template.WordTemplateParser;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblLook;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Applies the template's caption, header and body styles to article tables. */
public class ArticleTableStyleApplier {
    private static final String COMPANY_STANDARD_TABLE_STYLE = "CompanyStandardTable";
    public void applyToCopy(File templateFile, File sourceFile, File outputFile) throws Exception {
        TemplateDefinition template = new WordTemplateParser().parse(templateFile);
        applyToCopy(template, sourceFile, outputFile);
    }

    void applyToCopy(TemplateDefinition template, File sourceFile, File outputFile) throws Exception {
        if (sourceFile.getCanonicalFile().equals(outputFile.getCanonicalFile())) {
            throw new IllegalArgumentException("The output must be a separate copy.");
        }
        Files.copy(sourceFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        try (FileInputStream input = new FileInputStream(outputFile);
             XWPFDocument document = new XWPFDocument(input)) {
            apply(document, template);
            try (FileOutputStream output = new FileOutputStream(outputFile)) {
                document.write(output);
            }
        }
    }

    void apply(XWPFDocument document, TemplateDefinition template) {
        TemplateStyle captionStyle = template.findStyleByRole("TABLE_CAPTION");
        TemplateStyle headerStyle = template.findStyleByRole("TABLE_HEADER");
        TemplateStyle bodyStyle = template.findStyleByRole("TABLE_BODY");
        boolean hasArticleHeading = hasArticleHeading(document, template);
        boolean inArticle = !hasArticleHeading;
        XWPFParagraph previousParagraph = null;

        for (IBodyElement element : document.getBodyElements()) {
            if (element.getElementType() == BodyElementType.PARAGRAPH) {
                XWPFParagraph paragraph = (XWPFParagraph) element;
                if (isArticleHeading(paragraph, template)) inArticle = true;
                previousParagraph = paragraph;
            } else if (element.getElementType() == BodyElementType.TABLE) {
                if (inArticle) {
                    if (captionStyle != null && isTableCaption(previousParagraph)) {
                        applyStyle(previousParagraph, captionStyle);
                    }
                    applyTable((XWPFTable) element, headerStyle, bodyStyle);
                }
                previousParagraph = null;
            }
        }
    }

    private boolean hasArticleHeading(XWPFDocument document, TemplateDefinition template) {
        for (IBodyElement element : document.getBodyElements()) {
            if (element.getElementType() == BodyElementType.PARAGRAPH
                    && isArticleHeading((XWPFParagraph) element, template)) return true;
        }
        return false;
    }

    private boolean isArticleHeading(XWPFParagraph paragraph, TemplateDefinition template) {
        TemplateStyle heading = template.findStyleByRole("TITLE_ONE");
        return heading != null && heading.getStyleId().equals(paragraph.getStyle());
    }

    private boolean isTableCaption(XWPFParagraph paragraph) {
        if (paragraph == null || paragraph.getText() == null) return false;
        String value = paragraph.getText().trim();
        return value.matches("(?i)^(?:\\u8868|table)\\s*\\d+.*")
                || value.matches("^\\u9644\\u8868\\s*\\d+.*");
    }

    private void applyTable(XWPFTable table, TemplateStyle headerStyle, TemplateStyle bodyStyle) {
        table.setStyleID(COMPANY_STANDARD_TABLE_STYLE);
        enableFirstRowFormatting(table);
        for (int rowIndex = 0; rowIndex < table.getRows().size(); rowIndex++) {
            TemplateStyle rowStyle = rowIndex == 0 ? headerStyle : bodyStyle;
            if (rowStyle == null) continue;
            XWPFTableRow row = table.getRow(rowIndex);
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) applyStyle(paragraph, rowStyle);
            }
        }
    }

    private void enableFirstRowFormatting(XWPFTable table) {
        CTTblPr properties = table.getCTTbl().getTblPr();
        CTTblLook look = properties.isSetTblLook() ? properties.getTblLook() : properties.addNewTblLook();
        look.setFirstRow(STOnOff1.ON);
        if (table.getNumberOfRows() == 0) return;
        XWPFTableRow headerRow = table.getRow(0);
        if (headerRow.getCtRow().getTrPr() == null) headerRow.getCtRow().addNewTrPr();
        if (headerRow.getCtRow().getTrPr().getTblHeaderList().isEmpty()) {
            headerRow.getCtRow().getTrPr().addNewTblHeader();
        }
    }

    private void applyStyle(XWPFParagraph paragraph, TemplateStyle style) {
        if (paragraph.getCTP().isSetPPr()) paragraph.getCTP().unsetPPr();
        for (XWPFRun run : paragraph.getRuns()) {
            if (run.getCTR().isSetRPr()) run.getCTR().unsetRPr();
        }
        paragraph.setStyle(style.getStyleId());
    }
}
