package com.company.doc.parser;


import com.company.doc.common.model.DocumentModel;
import com.company.doc.common.model.ParagraphNode;


import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFNum;
import org.apache.poi.xwpf.usermodel.XWPFAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPrGeneral;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLvl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat;


import java.io.File;
import java.io.FileInputStream;



public class DocxParser {


    public DocumentModel parse(File file)
            throws Exception {


        DocumentModel model =
                new DocumentModel();


        try(
                FileInputStream fis =
                        new FileInputStream(file);

                XWPFDocument document =
                        new XWPFDocument(fis)
        ){


            int index = 0;


            for(XWPFParagraph paragraph :
                    document.getParagraphs()){


                ParagraphNode node =
                        new ParagraphNode();



                node.setIndex(index++);



                node.setText(
                        paragraph.getText()
                );

                node.setStyleId(
                        paragraph.getStyle()
                );



                XWPFStyle style = document.getStyles() == null ? null : document.getStyles().getStyle(paragraph.getStyle());
                node.setStyleName(style == null ? null : style.getName());



                /*
                 * 获取Word大纲级别
                 */
                node.setOutlineLevel(resolveOutlineLevel(paragraph, document));



                /*
                 * 判断是否自动编号
                 */
                Integer numberingLevel = resolveNumberingLevel(paragraph, document);
                node.setNumbered(numberingLevel != null);
                node.setNumberingLevel(numberingLevel);
                node.setBulletNumbering(isBulletNumbering(paragraph, document, numberingLevel));



                node.setSource(
                        paragraph
                );



                model.getParagraphs()
                        .add(node);

            }

        }


        return model;

    }

    private Integer resolveOutlineLevel(XWPFParagraph paragraph, XWPFDocument document) {
        CTPPr properties = paragraph.getCTP().getPPr();
        Integer directLevel = outlineLevelOf(properties);
        if (directLevel != null) return directLevel;

        XWPFStyle style = document.getStyles() == null ? null : document.getStyles().getStyle(paragraph.getStyle());
        int safetyLimit = 32;
        while (style != null && safetyLimit-- > 0) {
            CTStyle ctStyle = style.getCTStyle();
            Integer styleLevel = ctStyle == null ? null : outlineLevelOf(ctStyle.getPPr());
            if (styleLevel != null) return styleLevel;
            String baseStyleId = style.getBasisStyleID();
            style = baseStyleId == null || document.getStyles() == null ? null : document.getStyles().getStyle(baseStyleId);
        }
        return null;
    }

    private Integer outlineLevelOf(CTPPr properties) {
        return properties != null && properties.getOutlineLvl() != null
                ? properties.getOutlineLvl().getVal().intValue()
                : null;
    }

    private Integer outlineLevelOf(CTPPrGeneral properties) {
        return properties != null && properties.getOutlineLvl() != null
                ? properties.getOutlineLvl().getVal().intValue()
                : null;
    }

    /** Reads numbering from the paragraph first, then from its based-on style chain. */
    private Integer resolveNumberingLevel(XWPFParagraph paragraph, XWPFDocument document) {
        Integer level = numberingLevelOf(paragraph.getCTP().getPPr());
        if (hasNumberId(paragraph.getCTP().getPPr())) return level == null ? 0 : level;

        XWPFStyle style = document.getStyles() == null ? null : document.getStyles().getStyle(paragraph.getStyle());
        int safetyLimit = 32;
        while (style != null && safetyLimit-- > 0) {
            CTStyle ctStyle = style.getCTStyle();
            CTPPrGeneral styleProperties = ctStyle == null ? null : ctStyle.getPPr();
            Integer styleLevel = numberingLevelOf(styleProperties);
            if (level == null && styleLevel != null) level = styleLevel;
            if (hasNumberId(styleProperties)) return level == null ? 0 : level;
            String baseStyleId = style.getBasisStyleID();
            style = baseStyleId == null || document.getStyles() == null ? null : document.getStyles().getStyle(baseStyleId);
        }
        return null;
    }

    private Integer numberingLevelOf(CTPPr properties) {
        return numberingLevelOf(properties == null ? null : properties.getNumPr());
    }

    private Integer numberingLevelOf(CTPPrGeneral properties) {
        return numberingLevelOf(properties == null ? null : properties.getNumPr());
    }

    private Integer numberingLevelOf(CTNumPr numbering) {
        if (numbering == null || numbering.getIlvl() == null) return null;
        return numbering.getIlvl().getVal().intValue();
    }

    private boolean hasNumberId(CTPPr properties) {
        return properties != null && properties.getNumPr() != null && properties.getNumPr().getNumId() != null;
    }

    private boolean hasNumberId(CTPPrGeneral properties) {
        return properties != null && properties.getNumPr() != null && properties.getNumPr().getNumId() != null;
    }

    private boolean isBulletNumbering(XWPFParagraph paragraph, XWPFDocument document, Integer numberingLevel) {
        if (numberingLevel == null || document.getNumbering() == null) return false;
        CTNumPr numberProperties = resolveNumberingProperties(paragraph, document);
        if (numberProperties == null || numberProperties.getNumId() == null) return false;
        XWPFNum number = document.getNumbering().getNum(numberProperties.getNumId().getVal());
        if (number == null || number.getCTNum().getAbstractNumId() == null) return false;
        XWPFAbstractNum abstractNumber = document.getNumbering().getAbstractNum(number.getCTNum().getAbstractNumId().getVal());
        if (abstractNumber == null) return false;
        CTLvl level = abstractNumber.getCTAbstractNum().getLvlArray(numberingLevel);
        return level != null && level.getNumFmt() != null && level.getNumFmt().getVal() == STNumberFormat.BULLET;
    }

    private CTNumPr resolveNumberingProperties(XWPFParagraph paragraph, XWPFDocument document) {
        CTPPr properties = paragraph.getCTP().getPPr();
        if (properties != null && properties.getNumPr() != null && properties.getNumPr().getNumId() != null) return properties.getNumPr();
        XWPFStyle style = document.getStyles() == null ? null : document.getStyles().getStyle(paragraph.getStyle());
        int safetyLimit = 32;
        while (style != null && safetyLimit-- > 0) {
            CTStyle ctStyle = style.getCTStyle();
            CTPPrGeneral styleProperties = ctStyle == null ? null : ctStyle.getPPr();
            if (styleProperties != null && styleProperties.getNumPr() != null && styleProperties.getNumPr().getNumId() != null) return styleProperties.getNumPr();
            String baseStyleId = style.getBasisStyleID();
            style = baseStyleId == null || document.getStyles() == null ? null : document.getStyles().getStyle(baseStyleId);
        }
        return null;
    }

}
