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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.math.BigInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern WORD_STYLE = Pattern.compile(
            "(?s)<w:style\\b(?=[^>]*\\bw:styleId=\"([^\"]+)\")[^>]*>.*?</w:style>");

    public void replaceStyleLibrary(File templateFile, File testFile, File outputFile) throws Exception {
        if (testFile.getCanonicalFile().equals(outputFile.getCanonicalFile())) {
            throw new IllegalArgumentException("The output must be a separate copy.");
        }
        Map<String, byte[]> replacements = new HashMap<>();
        try (ZipFile template = new ZipFile(templateFile); ZipFile source = new ZipFile(testFile)) {
            replacements.put(STYLES, mergeTemplateStylesWithSourceTocStyles(template, source));
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
                if (!"BODY".equals(node.getLocation())) continue;
                XWPFParagraph paragraph = document.getParagraphArray(index);
                if (isTableOfContents(paragraph) || isTocStyle(node)) continue;
                if (isOrdinaryList(node)) {
                    int depth = lists.depthFor(node);
                    TemplateStyle listStyle = templateListStyle(template, node.isBulletNumbering(), depth);
                    if (listStyle == null) continue;
                    clearDirectFormatting(paragraph);
                    paragraph.setStyle(listStyle.getStyleId());
                    if (!node.isBulletNumbering() && lists.needsOrderedRestart(node)) {
                        restartOrderedList(paragraph, document, listStyle);
                    }
                    lists.accept(node, depth, node.isBulletNumbering());
                    continue;
                }
                // A non-list body paragraph ends the current list sequence.
                // The next ordered list must therefore receive a fresh numbering
                // instance and restart from 1 instead of continuing this list.
                lists.resetAtNonList();
                if (node.getRole() == null) continue;
                TemplateStyle style = templateStyleForRole(template, node.getRole());
                if (style == null) continue;
                clearDirectFormatting(paragraph);
                paragraph.setStyle(style.getStyleId());
                applyTemplateHeadingNumbering(paragraph, node.getRole());
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
        // Numbering alone is not enough to identify a list: numbered Heading 1
        // and Heading 2 nodes belong to the heading tree and must terminate an
        // adjacent ordered-list sequence (for example, 4.2 质量控制部).
        return node.getNumberingLevel() != null
                && node.getOutlineLevel() == null
                && !hasHeadingStyle(node)
                && (!isTitleTreeNode(node) || hasListStyle(node));
    }

    private boolean isTitleTreeNode(ParagraphNode node) {
        return "TITLE_ONE".equals(node.getRole()) || "TITLE_TWO".equals(node.getRole());
    }

    private boolean hasHeadingStyle(ParagraphNode node) {
        String value = ((node.getStyleId() == null ? "" : node.getStyleId()) + " "
                + (node.getStyleName() == null ? "" : node.getStyleName())).toLowerCase();
        return value.matches(".*(?:heading|标题|標題)\\s*[1１2２3３].*");
    }

    private boolean hasListStyle(ParagraphNode node) {
        String value = ((node.getStyleId() == null ? "" : node.getStyleId()) + " "
                + (node.getStyleName() == null ? "" : node.getStyleName())).toLowerCase();
        return value.contains("list") || value.contains("列表");
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
        // A new numId is normally sufficient, but Word-compatible editors can
        // still continue a visually adjacent list unless the instance carries
        // an explicit start override.  Always make the restart unambiguous.
        org.apache.poi.xwpf.usermodel.XWPFNum freshNum = document.getNumbering().getNum(freshNumId);
        if (freshNum != null) {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumLvl override = freshNum.getCTNum().addNewLvlOverride();
            override.setIlvl(BigInteger.ZERO);
            override.addNewStartOverride().setVal(BigInteger.ONE);
        }
        paragraph.setNumID(freshNumId);
        paragraph.setNumILvl(BigInteger.ZERO);
    }

    /** Keeps nesting local to an adjacent list sequence, never to heading depth. */
    private static final class ListContext {
        private Integer previousSourceLevel;
        private int previousDepth;
        private boolean previousWasList;
        private boolean restartOrdered = true;
        private boolean restartWhenEnteringChildList;

        int depthFor(ParagraphNode node) {
            int sourceLevel = node.getNumberingLevel() == null ? 0 : node.getNumberingLevel();
            if (!previousWasList || previousSourceLevel == null) return 0;
            if (sourceLevel > previousSourceLevel) return Math.min(previousDepth + 1, 2);
            if (sourceLevel < previousSourceLevel) return Math.max(0, previousDepth - (previousSourceLevel - sourceLevel));
            return previousDepth;
        }

        /**
         * A return from an item level to its parent (for example, 4.1 → item
         * 5 → 4.2) starts a new child-list sequence.  Wait until the following
         * child item to restart, so the parent heading itself keeps its outline
         * number and the first child of 4.2 becomes 1.
         */
        boolean needsOrderedRestart(ParagraphNode node) {
            int sourceLevel = node.getNumberingLevel() == null ? 0 : node.getNumberingLevel();
            if (restartOrdered && (!previousWasList || previousSourceLevel == null)) return true;
            return restartWhenEnteringChildList
                    && previousSourceLevel != null
                    && sourceLevel > previousSourceLevel;
        }

        void accept(ParagraphNode node, int depth, boolean bullet) {
            int sourceLevel = node.getNumberingLevel() == null ? 0 : node.getNumberingLevel();
            if (!bullet && previousWasList && previousSourceLevel != null) {
                if (sourceLevel < previousSourceLevel) restartWhenEnteringChildList = true;
                else if (sourceLevel > previousSourceLevel) restartWhenEnteringChildList = false;
            }
            previousSourceLevel = sourceLevel;
            previousDepth = depth;
            previousWasList = true;
            if (!bullet) restartOrdered = false;
        }

        void resetAtNonList() {
            previousSourceLevel = null;
            previousDepth = 0;
            previousWasList = false;
            restartOrdered = true;
            restartWhenEnteringChildList = false;
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

    /** Keeps the source document's TOC style definitions untouched. */
    private byte[] mergeTemplateStylesWithSourceTocStyles(ZipFile template, ZipFile source) throws Exception {
        String templateStyles = new String(bytes(template, STYLES, true), java.nio.charset.StandardCharsets.UTF_8);
        String sourceStyles = new String(bytes(source, STYLES, true), java.nio.charset.StandardCharsets.UTF_8);
        List<String> tocStyles = new ArrayList<>();
        List<String> tocStyleIds = new ArrayList<>();
        Matcher sourceStyle = WORD_STYLE.matcher(sourceStyles);
        while (sourceStyle.find()) {
            String styleId = sourceStyle.group(1);
            if (!styleId.toLowerCase().startsWith("toc")) continue;
            tocStyles.add(sourceStyle.group());
            tocStyleIds.add(styleId);
        }
        for (String styleId : tocStyleIds) {
            Pattern templateStyle = Pattern.compile(
                    "(?s)<w:style\\b(?=[^>]*\\bw:styleId=\"" + Pattern.quote(styleId) + "\")[^>]*>.*?</w:style>\\s*");
            templateStyles = templateStyle.matcher(templateStyles).replaceAll("");
        }
        if (tocStyles.isEmpty()) return templateStyles.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String sourceTocStyles = String.join("\n", tocStyles) + "\n";
        if (!templateStyles.contains("</w:styles>")) throw new IllegalArgumentException("Template styles.xml has no closing w:styles element");
        return templateStyles.replace("</w:styles>", sourceTocStyles + "</w:styles>")
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] bytes(ZipFile source, String partName, boolean required) throws Exception {
        ZipEntry entry = source.getEntry(partName);
        if (entry == null) {
            if (required) throw new IllegalArgumentException("Missing required Word part: " + partName);
            return null;
        }
        return source.getInputStream(entry).readAllBytes();
    }
}
