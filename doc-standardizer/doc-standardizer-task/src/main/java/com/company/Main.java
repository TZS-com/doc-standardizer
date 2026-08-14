package com.company;

import com.company.doc.core.DocumentProcessor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Windows batch entry point. The source documents are never modified in place. */
public class Main {
    private static final String STANDARDIZED_SUFFIX = "_standardized.docx";

    public static void main(String[] args) {
        try {
            Settings settings = Settings.parse(args);
            int failures = run(settings);
            System.exit(failures == 0 ? 0 : 1);
        } catch (IllegalArgumentException exception) {
            System.err.println("Configuration error: " + exception.getMessage());
            printUsage();
            System.exit(2);
        } catch (Exception exception) {
            System.err.println("Batch processing could not start: " + exception.getMessage());
            exception.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static int run(Settings settings) throws Exception {
        validate(settings);
        Files.createDirectories(settings.outputDirectory);
        List<Path> inputs;
        try (Stream<Path> files = Files.list(settings.inputDirectory)) {
            inputs = files.filter(Main::isProcessableDocument)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (inputs.isEmpty()) {
            System.out.println("No .docx files found in " + settings.inputDirectory);
            return 0;
        }

        DocumentProcessor processor = new DocumentProcessor(settings.template.toFile());
        int failures = 0;
        for (Path input : inputs) {
            Path output = settings.outputDirectory.resolve(outputFileName(input));
            try {
                processOne(processor, input, output);
                System.out.println("SUCCESS  " + input.getFileName() + " -> " + output.getFileName());
            } catch (Exception exception) {
                failures++;
                System.err.println("FAILED   " + input.getFileName() + ": " + exception.getMessage());
            }
        }
        System.out.printf("Finished: %d succeeded, %d failed.%n", inputs.size() - failures, failures);
        return failures;
    }

    private static void processOne(DocumentProcessor processor, Path input, Path output) throws Exception {
        Path workDirectory = Files.createTempDirectory("doc-standardizer-");
        try {
            Path headings = workDirectory.resolve("1-headings.docx");
            Path headerFooter = workDirectory.resolve("2-header-footer.docx");
            Path cover = workDirectory.resolve("3-cover.docx");
            processor.standardizeHeadingAndBodyTo(input.toFile(), headings.toFile());
            processor.applyTemplateHeaderFooterAndTocTo(headings.toFile(), headerFooter.toFile());
            processor.applyCoverStylesTo(headerFooter.toFile(), cover.toFile());
            processor.applyArticleTableStylesTo(cover.toFile(), output.toFile());
        } finally {
            deleteRecursively(workDirectory);
        }
    }

    private static void validate(Settings settings) {
        if (!Files.isRegularFile(settings.template)) {
            throw new IllegalArgumentException("Template does not exist: " + settings.template);
        }
        if (!Files.isDirectory(settings.inputDirectory)) {
            throw new IllegalArgumentException("Input directory does not exist: " + settings.inputDirectory);
        }
        if (settings.inputDirectory.equals(settings.outputDirectory)) {
            throw new IllegalArgumentException("Input and output directories must be different.");
        }
    }

    private static boolean isProcessableDocument(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return Files.isRegularFile(path) && name.endsWith(".docx") && !name.startsWith("~$");
    }

    private static String outputFileName(Path input) {
        String name = input.getFileName().toString();
        return name.substring(0, name.length() - ".docx".length()) + STANDARDIZED_SUFFIX;
    }

    private static void deleteRecursively(Path directory) throws Exception {
        if (!Files.exists(directory)) return;
        try (Stream<Path> files = Files.walk(directory)) {
            for (Path path : files.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static void printUsage() {
        System.err.println("Usage: doc-standardizer.bat [--template <file>] [--input-dir <folder>] [--output-dir <folder>]");
    }

    private static final class Settings {
        private final Path template;
        private final Path inputDirectory;
        private final Path outputDirectory;

        private Settings(Path template, Path inputDirectory, Path outputDirectory) {
            this.template = template;
            this.inputDirectory = inputDirectory;
            this.outputDirectory = outputDirectory;
        }

        private static Settings parse(String[] args) {
            Path applicationDirectory = new File(System.getProperty("user.dir")).toPath();
            Path template = applicationDirectory.resolve("template.docx");
            Path inputDirectory = applicationDirectory.resolve("input");
            Path outputDirectory = applicationDirectory.resolve("output");
            for (int index = 0; index < args.length; index += 2) {
                if (index + 1 >= args.length) throw new IllegalArgumentException("Missing value for " + args[index]);
                Path value = new File(args[index + 1]).toPath().toAbsolutePath().normalize();
                if ("--template".equals(args[index])) template = value;
                else if ("--input-dir".equals(args[index])) inputDirectory = value;
                else if ("--output-dir".equals(args[index])) outputDirectory = value;
                else throw new IllegalArgumentException("Unknown option: " + args[index]);
            }
            return new Settings(template.toAbsolutePath().normalize(), inputDirectory.toAbsolutePath().normalize(), outputDirectory.toAbsolutePath().normalize());
        }
    }
}
