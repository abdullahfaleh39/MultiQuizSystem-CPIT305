package util;

import model.QuizResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ReportWriter {

    public static synchronized String exportResults(List<QuizResult> results) throws IOException {
        File folder = new File("reports");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, "results_report.txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
            writer.println("======================================");
            writer.println("        Multi Quiz System Report       ");
            writer.println("======================================");
            writer.println();

            if (results == null || results.isEmpty()) {
                writer.println("No results found.");
            } else {
                for (QuizResult result : results) {
                    writer.println(result);
                }
            }
        }

        return file.getAbsolutePath();
    }
}