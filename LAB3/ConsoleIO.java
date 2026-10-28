package LAB3;

import exception.InvalidAmountException;
import exception.ValidationException;
import LAB1.Money;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ConsoleIO {


    public static class EndOfInputException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final BufferedReader in;
    private final PrintStream out;
    private final Console systemConsole;

    public ConsoleIO(BufferedReader in, PrintStream out, Console systemConsole) {
        this.in = in;
        this.out = out;
        this.systemConsole = systemConsole;
    }

    public String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        try {
            String line = in.readLine();
            if (line == null) {
                throw new EndOfInputException();
            }
            return line.trim();
        } catch (IOException e) {
            throw new EndOfInputException();
        }
    }

    public String readRequired(String prompt) {
        String value = readLine(prompt);
        if (value.isEmpty()) {
            throw new ValidationException("This value is required.");
        }
        return value;
    }

    public String readSecret(String prompt) {
        if (systemConsole != null) {
            char[] secret = systemConsole.readPassword(prompt);
            if (secret == null) {
                throw new EndOfInputException();
            }
            return new String(secret).trim();
        }
        return readLine(prompt);
    }

    public Long readId(String prompt) {
        String text = readRequired(prompt);
        try {
            long value = Long.parseLong(text);
            if (value <= 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException("'" + text + "' is not a valid ID. Please enter a positive number.");
        }
    }

    public BigDecimal readAmount(String prompt) {
        String text = readLine(prompt);
        if (text.isEmpty()) {
            throw new InvalidAmountException("Amount is required.");
        }
        return Money.parse(text);
    }

    /** Empty input returns the default value. Format: yyyy-MM-dd. */
    public LocalDate readDate(String prompt, LocalDate defaultValue) {
        String text = readLine(prompt + " [" + (defaultValue == null ? "none" : defaultValue) + "]: ");
        if (text.isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException("'" + text + "' is not a valid date. Use yyyy-MM-dd, e.g. 2026-10-25.");
        }
    }

    public boolean confirm(String question) {
        String answer = readLine(question + " (y/n): ");
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    public void println(String text) {
        out.println(text);
    }

    public void println() {
        out.println();
    }

    public void success(String text) {
        out.println("[OK] " + text);
    }

    public void error(String text) {
        out.println("[ERROR] " + text);
    }

    public void header(String title) {
        out.println();
        out.println("==== " + title + " " + "=".repeat(Math.max(3, 60 - title.length())));
    }

    /** Prints rows as an aligned text table. */
    public void table(List<String> headers, List<List<String>> rows) {
        if (rows.isEmpty()) {
            out.println("(no records)");
            return;
        }
        int[] widths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            widths[i] = headers.get(i).length();
        }
        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                widths[i] = Math.min(45, Math.max(widths[i], row.get(i) == null ? 0 : row.get(i).length()));
            }
        }
        printRow(headers, widths);
        StringBuilder line = new StringBuilder();
        for (int width : widths) {
            line.append("-".repeat(width)).append("-+-");
        }
        out.println(line.substring(0, line.length() - 2));
        rows.forEach(row -> printRow(row, widths));
    }

    private void printRow(List<String> cells, int[] widths) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            String cell = cells.get(i) == null ? "" : cells.get(i);
            if (cell.length() > widths[i]) {
                cell = cell.substring(0, widths[i] - 3) + "...";
            }
            row.append(String.format("%-" + widths[i] + "s", cell)).append(" | ");
        }
        out.println(row.substring(0, row.length() - 3));
    }
}
