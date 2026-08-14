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

/** Applies the template's default header/footer and makes the TOC track Heading 1-3. */
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

    public void applyToCopy(File template, File source, File output) throws Exception {
        if (source.getCanonicalFile().equals(output.getCanonicalFile())) {
            throw new IllegalArgumentException("The output must be a separate copy.");
        }
        Map<String, byte[]> replacements = new HashMap<>();
        try (ZipFile templateZip = new ZipFile(template); ZipFile sourceZip = new ZipFile(source)) {
            replacements.put(HEADER, bytes(templateZip, HEADER));
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
        String xml = new String(source, StandardCharsets.UTF_8)
                .replace("TOC \\o \"1-2\"", "TOC \\o \"1-3\"");
        String references = "<w:headerReference w:type=\"default\" r:id=\"" + HEADER_RELATIONSHIP + "\"/>"
                + "<w:footerReference w:type=\"default\" r:id=\"" + FOOTER_RELATIONSHIP + "\"/>";
        xml = xml.replaceAll("(?s)(<w:sectPr(?: [^>]*)?>)(.*?)(</w:sectPr>)", "$1" + references + "$2$3");
        return xml.getBytes(StandardCharsets.UTF_8);
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
        if (!xml.contains("<w:updateFields")) {
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
