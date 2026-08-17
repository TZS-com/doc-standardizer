package com.company.doc.core;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Applies the template's default header/footer and asks Word to refresh the existing TOC fields. */
public class TemplateHeaderFooterAndTocApplier {
    private static final String DOCUMENT = "word/document.xml";
    private static final String SETTINGS = "word/settings.xml";
    private static final String HEADER = "word/header1.xml";
    private static final String FOOTER = "word/footer1.xml";
    private static final String HEADER_RELATIONSHIPS = "word/_rels/header1.xml.rels";
    private static final String LEFT_IMAGE = "word/media/company-header-left.png";
    private static final String RIGHT_IMAGE = "word/media/company-header-right.png";
    private static final String RELATIONSHIPS = "word/_rels/document.xml.rels";
    private static final String HEADER_RELATIONSHIP = "rId5";
    private static final String FOOTER_RELATIONSHIP = "rId8";
    private static final Pattern SECTION_PROPERTIES = Pattern.compile("(?s)(<w:sectPr(?: [^>]*)?>)(.*?)(</w:sectPr>)");
    private static final Pattern HEADER_OR_FOOTER_REFERENCE = Pattern.compile("<w:(?:header|footer)Reference\\b[^>]*/>");
    private static final Pattern UPDATE_FIELDS = Pattern.compile("<w:updateFields\\b[^>]*/>");
    private static final Pattern FIRST_CATALOGUE_PARAGRAPH = Pattern.compile(
            "(?s)<w:p(?: [^>]*)?>(?:(?!</w:p>).)*?<w:t(?: [^>]*)?>\\s*目录\\s*</w:t>(?:(?!</w:p>).)*?</w:p>");
    private static final Pattern HEADER_CHINESE_TITLE_PLACEHOLDER = Pattern.compile(
            "(?s)<w:r(?: [^>]*)?>(?:<w:rPr>.*?</w:rPr>)?<w:t>\\{VE\\.</w:t></w:r>"
                    + "<w:r(?: [^>]*)?>(?:<w:rPr>.*?</w:rPr>)?<w:t>方案名称</w:t></w:r>"
                    + "<w:r(?: [^>]*)?>(?:<w:rPr>.*?</w:rPr>)?<w:t>\\}</w:t></w:r>");
    private static final String PAGE_BREAK = "<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>";

    public void applyToCopy(File template, File source, File output) throws Exception {
        applyToCopy(template, source, output, source.getName());
    }

    /** Uses the original source filename for the Chinese title in the template header. */
    public void applyToCopy(File template, File source, File output, String sourceFileName) throws Exception {
        if (source.getCanonicalFile().equals(output.getCanonicalFile())) {
            throw new IllegalArgumentException("The output must be a separate copy.");
        }
        Map<String, byte[]> replacements = new HashMap<>();
        try (ZipFile templateZip = new ZipFile(template); ZipFile sourceZip = new ZipFile(source)) {
            replacements.put(HEADER, updatedHeader(bytes(templateZip, HEADER), sourceFileName));
            replacements.put(FOOTER, bytes(templateZip, FOOTER));
            replacements.put(HEADER_RELATIONSHIPS, headerImageRelationships());
            replacements.put(LEFT_IMAGE, Files.readAllBytes(findProjectImage("图片1.png").toPath()));
            replacements.put(RIGHT_IMAGE, Files.readAllBytes(findProjectImage("图片2.png").toPath()));
            replacements.put(DOCUMENT, updatedDocument(bytes(sourceZip, DOCUMENT)));
            replacements.put(SETTINGS, updatedSettings(bytes(sourceZip, SETTINGS)));
            replacements.put(RELATIONSHIPS, updatedRelationships(bytes(sourceZip, RELATIONSHIPS)));

            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(output.toPath()))) {
                Enumeration<? extends ZipEntry> entries = sourceZip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    zip.putNextEntry(new ZipEntry(entry.getName()));
                    byte[] replacement = replacements.remove(entry.getName());
                    if (replacement == null) sourceZip.getInputStream(entry).transferTo(zip);
                    else zip.write(replacement);
                    zip.closeEntry();
                }
                for (Map.Entry<String, byte[]> entry : replacements.entrySet()) {
                    zip.putNextEntry(new ZipEntry(entry.getKey()));
                    zip.write(entry.getValue());
                    zip.closeEntry();
                }
            }
        }
    }

    private byte[] updatedDocument(byte[] source) {
        String xml = new String(source, StandardCharsets.UTF_8);
        String references = "<w:headerReference w:type=\"default\" r:id=\"" + HEADER_RELATIONSHIP + "\"/>"
                + "<w:footerReference w:type=\"default\" r:id=\"" + FOOTER_RELATIONSHIP + "\"/>";
        Matcher sections = SECTION_PROPERTIES.matcher(xml);
        StringBuffer rewritten = new StringBuffer();
        while (sections.find()) {
            // Replace, rather than append to, the references. Otherwise a
            // source section can retain its legacy header/footer alongside the
            // template's header/footer and Word renders both.
            String properties = HEADER_OR_FOOTER_REFERENCE.matcher(sections.group(2)).replaceAll("");
            sections.appendReplacement(rewritten, Matcher.quoteReplacement(
                    sections.group(1) + references + properties + sections.group(3)));
        }
        sections.appendTail(rewritten);
        return insertPageBreakBeforeFirstCatalogue(rewritten.toString()).getBytes(StandardCharsets.UTF_8);
    }

    /** Starts the table of contents on a new page without affecting header/footer text. */
    private String insertPageBreakBeforeFirstCatalogue(String xml) {
        Matcher catalogue = FIRST_CATALOGUE_PARAGRAPH.matcher(xml);
        if (!catalogue.find()) return xml;
        return xml.substring(0, catalogue.start()) + PAGE_BREAK + xml.substring(catalogue.start());
    }

    private byte[] updatedHeader(byte[] templateHeader, String sourceFileName) {
        String title = sourceFileName == null ? "" : new File(sourceFileName).getName()
                .replaceFirst("(?i)\\.docx$", "")
                .replaceFirst("(?i)_standardized$", "");
        String replacement = "<w:r><w:t xml:space=\"preserve\">" + escapeXml(title) + "</w:t></w:r>";
        String xml = HEADER_CHINESE_TITLE_PLACEHOLDER.matcher(new String(templateHeader, StandardCharsets.UTF_8))
                .replaceFirst(Matcher.quoteReplacement(replacement));
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /** rId1/rId2 are the existing left/right drawing placeholders in header1.xml. */
    private byte[] headerImageRelationships() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"media/company-header-left.png\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"media/company-header-right.png\"/>"
                + "</Relationships>";
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] updatedSettings(byte[] source) {
        String xml = new String(source, StandardCharsets.UTF_8);
        Matcher updateFields = UPDATE_FIELDS.matcher(xml);
        if (updateFields.find()) {
            // A source document can explicitly disable field updates.  Replace
            // that setting so Word refreshes the unchanged TOC field on open.
            xml = updateFields.replaceFirst("<w:updateFields w:val=\"true\"/>");
        } else {
            xml = xml.replace("</w:settings>", "<w:updateFields w:val=\"true\"/></w:settings>");
        }
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] updatedRelationships(byte[] source) {
        String xml = new String(source, StandardCharsets.UTF_8);
        xml = xml.replaceAll("<Relationship Id=\"" + HEADER_RELATIONSHIP + "\"[^>]*/>",
                "<Relationship Id=\"" + HEADER_RELATIONSHIP + "\" Target=\"header1.xml\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/header\"/>");
        xml = xml.replaceAll("<Relationship Id=\"" + FOOTER_RELATIONSHIP + "\"[^>]*/>",
                "<Relationship Id=\"" + FOOTER_RELATIONSHIP + "\" Target=\"footer1.xml\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer\"/>");
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] bytes(ZipFile zip, String name) throws Exception {
        ZipEntry entry = zip.getEntry(name);
        if (entry == null) throw new IllegalArgumentException("Missing required Word part: " + name);
        return zip.getInputStream(entry).readAllBytes();
    }

    private File findProjectImage(String name) {
        File current = new File(name);
        if (current.isFile()) return current;
        File parent = new File("..", name);
        if (parent.isFile()) return parent;
        throw new IllegalArgumentException("Missing header image in the project folder: " + name);
    }
}
