package com.company.doc.core;

import com.company.doc.common.enums.DocumentLevel;
import com.company.doc.common.model.DocumentModel;
import com.company.doc.common.model.ParagraphNode;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Writes a read-only, auditable view of the detected heading hierarchy. */
public class HeadingTreeReportWriter {
    public void write(DocumentModel document, File output) throws Exception {
        StringBuilder report = new StringBuilder();
        report.append("Corrected heading tree (no DOCX changes were made)\n");
        report.append("Each node: level | paragraph index | ilvl | outline | style | text\n");
        report.append("Automatic lists without an outline level are excluded from this tree.\n\n");
        int count = 0;
        for (ParagraphNode node : document.getParagraphs()) {
            if (!"BODY".equals(node.getLocation()) || !isStructuralHeading(node)) continue;
            int depth = node.getLevel().getLevel();
            for (int indent = 1; indent < depth; indent++) report.append("  ");
            report.append("- ").append(node.getLevel())
                    .append(" | p=").append(node.getIndex())
                    .append(" | ilvl=").append(value(node.getNumberingLevel()))
                    .append(" | outline=").append(value(node.getOutlineLevel()))
                    .append(" | style=").append(value(node.getStyleId()))
                    .append(" | ").append(clean(node.getText()))
                    .append('\n');
            count++;
        }
        report.append("\nTotal heading nodes: ").append(count).append('\n');
        Files.writeString(output.toPath(), report.toString(), StandardCharsets.UTF_8);
    }

    private boolean isStructuralHeading(ParagraphNode node) {
        if (node.getLevel() == null || node.getLevel() == DocumentLevel.NORMAL) return false;
        // In the test documents, actual automatic headings have both numPr and
        // outline information. Ordinary lists have numPr only, and must not
        // take part in the chapter-number hierarchy.
        return node.getNumberingLevel() == null || node.getOutlineLevel() != null;
    }

    private String value(Object value) {
        return value == null ? "-" : value.toString();
    }

    private String clean(String text) {
        return text == null ? "" : text.replace('\n', ' ').replace('\r', ' ').trim();
    }
}
