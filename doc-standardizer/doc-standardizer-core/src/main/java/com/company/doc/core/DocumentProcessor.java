package com.company.doc.core;


import com.company.doc.common.model.DocumentModel;
import com.company.doc.template.TemplateDefinition;
import com.company.doc.template.TemplateLoader;
import com.company.doc.parser.NavigationBlankLineCleaner;
import com.company.doc.parser.BilingualHeadingJoiner;


import java.io.File;



public class DocumentProcessor {


    private final StandardizePipeline pipeline;

    private final File templateFile;



    public DocumentProcessor(
            File templateFile
    )
            throws Exception {

        this.templateFile = templateFile;


        TemplateLoader loader =
                new TemplateLoader();



        TemplateDefinition template =
                loader.load(
                        templateFile
                );



        this.pipeline =
                new StandardizePipeline(
                        template
                );

    }



    public DocumentModel process(
            File file
    )
            throws Exception {


        return pipeline.execute(
                file
        );

    }

    /** Read-only analysis gate: inspect this report before any style write-back. */
    public void exportHeadingTree(File input, File reportFile) throws Exception {
        File joinedInput = new File(reportFile.getAbsolutePath() + ".bilingual-joined.docx");
        File cleanedInput = new File(reportFile.getAbsolutePath() + ".navigation-clean.docx");
        try {
            // The report must represent the same corrected source that later
            // write-back would use; never report the uncorrected raw document.
            new BilingualHeadingJoiner().joinToCopy(input, joinedInput);
            new NavigationBlankLineCleaner().cleanToCopy(joinedInput, cleanedInput);
            new HeadingTreeReportWriter().write(process(cleanedInput), reportFile);
        } finally {
            java.nio.file.Files.deleteIfExists(joinedInput.toPath());
            java.nio.file.Files.deleteIfExists(cleanedInput.toPath());
        }
    }

    /** Imports all template assets into a new copy; input remains unchanged. */
    public void importTemplateAssetsTo(File input, File outputFile) throws Exception {
        new SafeTemplateApplier().replaceStyleLibrary(templateFile, input, outputFile);
    }

    public void standardizeRecognizedHeadingsTo(File input, File outputFile) throws Exception {
        DocumentModel model = process(input);
        new SafeTemplateApplier().replaceLibraryAndApplyRecognizedBodyStyles(templateFile, input, outputFile, model);
    }

    /** Applies Heading 1–3 and Body Text to recognized body paragraphs only. */
    public void standardizeHeadingAndBodyTo(File input, File outputFile) throws Exception {
        File joinedInput = new File(outputFile.getAbsolutePath() + ".bilingual-joined.docx");
        File cleanedInput = new File(outputFile.getAbsolutePath() + ".navigation-clean.docx");
        try {
            // Preprocessing phase 1: merge bilingual headings before any
            // navigation cleanup, hierarchy detection or style replacement.
            new BilingualHeadingJoiner().joinToCopy(input, joinedInput);
            // Preprocessing phase 2: remove only blank navigation headings.
            new NavigationBlankLineCleaner().cleanToCopy(joinedInput, cleanedInput);
            // Only now build the hierarchy and apply template styles.
            standardizeRecognizedHeadingsTo(cleanedInput, outputFile);
        } finally {
            java.nio.file.Files.deleteIfExists(joinedInput.toPath());
            java.nio.file.Files.deleteIfExists(cleanedInput.toPath());
        }
    }

    /** Final document phase: use the template's placeholders in header/footer and include Heading 3 in the TOC. */
    public void applyTemplateHeaderFooterAndTocTo(File input, File outputFile) throws Exception {
        new TemplateHeaderFooterAndTocApplier().applyToCopy(templateFile, input, outputFile);
    }

    /** Applies the template's cover typography and its first two cover-table style patterns. */
    public void applyCoverStylesTo(File input, File outputFile) throws Exception {
        new CoverStyleApplier().applyToCopy(templateFile, input, outputFile);
    }

    /** Applies the template's caption, header and body styles to article tables. */
    public void applyArticleTableStylesTo(File input, File outputFile) throws Exception {
        new ArticleTableStyleApplier().applyToCopy(templateFile, input, outputFile);
    }


}
