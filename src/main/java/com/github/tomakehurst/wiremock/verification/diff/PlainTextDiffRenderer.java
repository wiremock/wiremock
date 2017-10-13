package com.github.tomakehurst.wiremock.verification.diff;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.rightPad;

public class PlainTextDiffRenderer {

    private static final String HEADER =
        "\n" +
        "                                               Request was not matched\n" +
        "                                               =======================\n" +
        "\n" +
        "-----------------------------------------------------------------------------------------------------------------------\n" +
        "| Closest stub                                             | Request                                                  |\n" +
        "-----------------------------------------------------------------------------------------------------------------------\n" +
        "                                                           |\n";

    private static final String FOOTER =
        "-----------------------------------------------------------------------------------------------------------------------";

    public String render(Diff diff) {
        StringBuilder sb = new StringBuilder(HEADER);

        if (diff.getStubMappingName() != null) {
            int nameLength = diff.getStubMappingName().length();
            writeSingleLine(sb, diff.getStubMappingName());
            writeSingleLine(sb, rightPad("", nameLength, "-"));
            writeBlankLine(sb);
        }

        for (DiffSection<?> section: diff.getSections()) {
            writeLine(sb, section.getExpected().toString(), section.getActual().toString(), section.getMessage());
        }

        writeBlankLine(sb);
        sb.append(FOOTER).append("\n");

        return sb.toString();
    }

    private static void writeLine(StringBuilder sb, String left, String right, String message) {
        String[] leftLines = left.split("\n");
        String[] rightLines = right.split("\n");

        int maxLines = Math.max(leftLines.length, rightLines.length);

        writeSingleLine(sb, leftLines[0], rightLines[0], message);

        if (maxLines > 1) {
            for (int i = 1; i < maxLines; i++) {
                String leftPart = leftLines.length > i ? leftLines[i] : "";
                String rightPart = rightLines.length > i ? rightLines[i] : "";
                writeSingleLine(sb, leftPart, rightPart, null);
            }
        }
    }

    private static void writeBlankLine(StringBuilder sb) {
        writeSingleLine(sb, "", null, null);
    }

    private static void writeSingleLine(StringBuilder sb, String left, String right) {
        writeSingleLine(sb, left, right, null);
    }

    private static void writeSingleLine(StringBuilder sb, String left) {
        writeSingleLine(sb, left, null);
    }

    private static void writeSingleLine(StringBuilder sb, String left, String right, String message) {
        sb
            .append("")
            .append(rightPad(left, 59, " "))
            .append("|");

        if (isNotEmpty(right)) {
            sb.append(" ");

            if (isNotEmpty(message)) {
                sb
                    .append(rightPad(right, 53, " "))
                    .append("<<<<< ")
                    .append(message);
            } else {
                sb.append(right);
            }

        }

        sb.append("\n");
    }
}
