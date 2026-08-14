package com.company.doc.core;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.math.BigInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.company.doc.common.model.DocumentModel;
import com.company.doc.common.model.ParagraphNode;
import com.company.doc.template.TemplateDefinition;
import com.company.doc.template.TemplateStyle;
import com.company.doc.template.WordTemplateParser;

/** Replaces a copy's complete Word style and numbering library with the template's library. */
public class SafeTemplateApplier {
    private static final String STYLES = "word/styles.xml";
    private static final String NUMBERING = "word/numbering.xml";
    private static final String THEME = "word/theme/theme1.xml";

    public void replaceStyleLibrary(File templateFile, File testFile, File outputFile) throws Exception {
        if (testFile.getCanonicalFile().equals(outputFile.getCanonicalFile())) {
            throw new IllegalArgumentException("The output must be a separate copy.");
        }
        Map<String, byte[]> replacements = new HashMap<>();
        try (ZipFile template = new ZipFile(templateFile)) {
            copyPart(template, STYLES, replacements, true);
            copyPart(template, NUMBERING, replacements, false);
            copyPart(template, THEME, replacements, false);
        }
        try (ZipFile source = new ZipFile(testFile); ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(outputFile.toPath()))) {
            Enumeration<? extends ZipEntry> entries = source.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                ZipEntry copied = new ZipEntry(entry.getName());
                output.putNextEntry(copied);
                byte[] replacement = replacements.remove(entry.getName());
                if (replacement != null) output.write(replacement);
                else source.getInputStream(entry).transferTo(output);
                output.closeEntry();
            }
            for (Map.Entry<String, byte[]> missing : replacements.entrySet()) {
                output.putNextEntry(new ZipEntry(missing.getKey()));
                output.write(missing.getValue());
                output.closeEntry();
            }
        }
        try (FileInputStream input = new FileInputStream(outputFile); XWPFDocument ignored = new XWPFDocument(input)) {
            // Opening the generated copy is the structural validity gate.
        }
    }

    /**
     * Applies template styles only after the full library replacement.  For a
     * matched heading, direct paragraph/run formatting is removed so template
     * defaults (for example Songti) are not overridden by legacy formatting.
     */
    public void replaceLibraryAndApplyRecognizedBodyStyles(File templateFile, File testFile, File outputFile, DocumentModel model) throws Exception {
        replaceStyleLibrary(templateFile, testFile, outputFile);
        TemplateDefinition template = new WordTemplateParser().parse(templateFile);
        try (FileInputStream input = new FileInputStream(outputFile); XWPFDocument document = new XWPFDocument(input)) {
            int limit = Math.min(model.getParagraphs().size(), document.getParagraphs().size());
            ListContext lists = new ListContext();
            for (int index = 0; index < limit; index++) {
                ParagraphNode node = model.getParagraphs().get(index);
                if (!"BODY".equals(node.getLocation()) || node.getRole() == null) continue;
                XWPFParagraph paragraph = document.getParagraphArray(index);
                if (isTableOfContents(paragraph) || isTocStyle(node)) continue;
                if (isOrdinaryList(node)) {
                    int depth = lists.depthFor(node);
                    TemplateStyle listStyle = templateListStyle(template, node.isBulletNumbering(), depth);
                    if (listStyle == null) continue;
                    clearDirectFormatting(paragraph);
                    paragraph.setStyle(listStyle.getStyleId());
                    if (!node.isBulletNumbering() && depth == 0 && lists.needsOrderedRestart()) {
                        restartOrderedList(paragraph, document, listStyle);
                    }
                    lists.accept(node, depth, node.isBulletNumbering());
                    continue;
                }
                TemplateStyle style = templateStyleForRole(template, node.getRole());
                if (style == null) continue;
                clearDirectFormatting(paragraph);
                paragraph.setStyle(style.getStyleId());
                applyTemplateHeadingNumbering(paragraph, node.getRole());
                if (headingNumberingLevel(node.getRole()) != null) lists.resetAtHeading();
            }
            try (FileOutputStream output = new FileOutputStream(outputFile)) { document.write(output); }
        }
        try (FileInputStream input = new FileInputStream(outputFile); XWPFDocument ignored = new XWPFDocument(input)) { }
    }


    private TemplateStyle templateStyleForRole(TemplateDefinition template, String role) {
        // In this company template, aa / “Body Text” is the displayed 正文
        // style. Do not use the later “正文首行缩进” sample as its replacement.
        if ("NORMAL".equals(role)) return template.findStyle("aa");
        return template.findStyleByRole(role);
    }

    private boolean isTableOfContents(XWPFParagraph paragraph) {
        for (XWPFRun run : paragraph.getRuns()) {
            if (run.getCTR().getInstrTextList().stream().anyMatch(field -> field.getStringValue().toUpperCase().contains("TOC"))) return true;
        }
        return false;
    }

    private boolean isTocStyle(ParagraphNode node) {
        String value = ((node.getStyleId() == null ? "" : node.getStyleId()) + " "
                + (node.getStyleName() == null ? "" : node.getStyleName())).toLowerCase();
        return value.contains("toc") || value.contains("目录");
    }

    private boolean isOrdinaryList(ParagraphNode node) {
        return node.getNumberingLevel() != null && node.getOutlineLevel() == null;
    }

    /** Maps the source list depth to the reviewed list styles in the company template. */
    private TemplateStyle templateListStyle(TemplateDefinition template, boolean bullet, int depth) {
        String[] styleIds;
        if (bullet) {
            styleIds = new String[] {"1", "21", "30"};
        } else {
            styleIds = new String[] {"11", "23", "31"};
        }
        return template.findStyle(styleIds[Math.min(depth, styleIds.length - 1)]);
    }

    /** Creates a fresh instance of the template list's own numbering scheme. */
    private void restartOrderedList(XWPFParagraph paragraph, XWPFDocument document, TemplateStyle listStyle) {
        if (document.getNumbering() == null || document.getStyles() == null) return;
        org.apache.poi.xwpf.usermodel.XWPFStyle style = document.getStyles().getStyle(listStyle.getStyleId());
        if (style == null || style.getCTStyle().getPPr() == null || style.getCTStyle().getPPr().getNumPr() == null
                || style.getCTStyle().getPPr().getNumPr().getNumId() == null) return;
        BigInteger templateNumId = style.getCTStyle().getPPr().getNumPr().getNumId().getVal();
        org.apache.poi.xwpf.usermodel.XWPFNum templateNum = document.getNumbering().getNum(templateNumId);
        if (templateNum == null || templateNum.getCTNum().getAbstractNumId() == null) return;
        BigInteger freshNumId = document.getNumbering().addNum(templateNum.getCTNum().getAbstractNumId().getVal());
        if (freshNumId == null) return;
        paragraph.setNumID(freshNumId);
        paragraph.setNumILvl(BigInteger.ZERO);
    }

    /** Keeps nesting local to an adjacent list sequence, never to heading depth. */
    private static final class ListContext {
        private Integer previousSourceLevel;
        private int previousDepth;
        private boolean previousWasList;
        private boolean restartOrdered = true;

        int depthFor(ParagraphNode node) {
            int sourceLevel = node.getNumberingLevel() == null ? 0 : node.getNumberingLevel();
            if (!previousWasList || previousSourceLevel == null) return 0;
            if (sourceLevel > previousSourceLevel) return Math.min(previousDepth + 1, 2);
            if (sourceLevel < previousSourceLevel) return Math.max(0, previousDepth - (previousSourceLevel - sourceLevel));
            return previousDepth;
        }

        boolean needsOrderedRestart() { return restartOrdered; }

        void accept(ParagraphNode node, int depth, boolean bullet) {
            previousSourceLevel = node.getNumberingLevel() == null ? 0 : node.getNumberingLevel();
            previousDepth = depth;
            previousWasList = true;
            if (!bullet) restartOrdered = false;
        }

        void resetAtHeading() {
            previousSourceLevel = null;
            previousDepth = 0;
            previousWasList = false;
            restartOrdered = true;
        }
    }

    /**
     * The template heading styles all use numbering instance 28.  Write the
     * level explicitly because direct paragraph formatting in source files can
     * otherwise make Word treat a Heading 2 as another Heading 1 (11.1 -> 12).
     */
    private void applyTemplateHeadingNumbering(XWPFParagraph paragraph, String role) {
        Integer level = headingNumberingLevel(role);
        if (level == null) return;
        CTPPr properties = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();
        if (properties.isSetNumPr()) properties.unsetNumPr();
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumPr numbering = properties.addNewNumPr();
        numbering.addNewIlvl().setVal(java.math.BigInteger.valueOf(level));
        numbering.addNewNumId().setVal(java.math.BigInteger.valueOf(28));
    }

    private Integer headingNumberingLevel(String role) {
        if ("TITLE_ONE".equals(role)) return 0;
        if ("TITLE_TWO".equals(role)) return 1;
        if ("TITLE_THREE".equals(role)) return 2;
        return null;
    }

    private void clearDirectFormatting(XWPFParagraph paragraph) {
        CTPPr properties = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : null;
        if (properties != null) {
            // setStyle below writes a fresh pStyle. Removing the old pPr also
            // removes legacy indentation, spacing and direct paragraph format.
            paragraph.getCTP().unsetPPr();
        }
        for (XWPFRun run : paragraph.getRuns()) {
            if (run.getCTR().isSetRPr()) run.getCTR().unsetRPr();
        }
    }

    private void copyPart(ZipFile source, String partName, Map<String, byte[]> replacements, boolean required) throws Exception {
        ZipEntry entry = source.getEntry(partName);
        if (entry == null) {
            if (required) throw new IllegalArgumentException("Template has no required part: " + partName);
            return;
        }
        replacements.put(partName, source.getInputStream(entry).readAllBytes());
    }
}
