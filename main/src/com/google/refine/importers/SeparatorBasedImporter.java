/*

Copyright 2010, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package com.google.refine.importers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.json.JSONObject;

import au.com.bytecode.opencsv.CSVParser;

import com.google.refine.importing.ImportingJob;
import com.google.refine.importing.ImportingUtilities;
import com.google.refine.model.Project;
import com.google.refine.model.metadata.ProjectMetadata;
import com.google.refine.util.JSONUtilities;

public class SeparatorBasedImporter extends TabularImportingParserBase {

    public SeparatorBasedImporter() {
        super(false);
    }

    public static final boolean IGNORE_QUOTES = false;

    @Override
    public JSONObject createParserUIInitializationData(ImportingJob job, List<JSONObject> fileRecords, String format) {
        JSONObject options = super.createParserUIInitializationData(job, fileRecords, format);

        boolean quotes = guessQuotes(job, fileRecords);
        String separator = guessSeparator(job, fileRecords, quotes);

        JSONUtilities.safePut(options, "separator", separator != null ? separator : "\\t");
        JSONUtilities.safePut(options, "guessCellValueTypes", false);
        JSONUtilities.safePut(options, "processQuotes", quotes);
        JSONUtilities.safePut(options, "quoteCharacter", String.valueOf(CSVParser.DEFAULT_QUOTE_CHARACTER));

        return options;
    }

    @Override
    public void parseOneFile(Project project, ProjectMetadata metadata, ImportingJob job, String fileSource,
            Reader reader, int limit, JSONObject options, List<Exception> exceptions) {
        String sep = JSONUtilities.getString(options, "separator", "\\t");
        if (sep == null || "".equals(sep)) {
            sep = "\\t";
        }
        sep = StringEscapeUtils.unescapeJava(sep);
        boolean processQuotes = JSONUtilities.getBoolean(options, "processQuotes", true);
        boolean strictQuotes = JSONUtilities.getBoolean(options, "strictQuotes", false);

        Character quote = CSVParser.DEFAULT_QUOTE_CHARACTER;
        String quoteCharacter = JSONUtilities.getString(options, "quoteCharacter", null);
        if (quoteCharacter != null && quoteCharacter.trim().length() == 1) {
            quote = quoteCharacter.trim().charAt(0);
        }

        final CSVParser parser = new CSVParser(sep, quote, (char) 0, // we don't want escape processing
                strictQuotes, CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE, !processQuotes);

        final LineNumberReader lnReader = new LineNumberReader(reader);

        TableDataReader dataReader = new TableDataReader() {

            @Override
            public List<Object> getNextRowOfCells()
                    throws IOException {
                String line = lnReader.readLine();
                if (line == null) {
                    return null;
                } else {
                    return getCells(line, parser, lnReader);
                }
            }
        };

        TabularImportingParserBase.readTable(project, metadata, job, dataReader, fileSource, limit, options,
                exceptions);
        super.parseOneFile(project, metadata, job, fileSource, lnReader, limit, options, exceptions);
    }

    static protected ArrayList<Object> getCells(String line, CSVParser parser, LineNumberReader lnReader)
            throws IOException {

        ArrayList<Object> cells = new ArrayList<Object>();
        String[] tokens = parser.parseLineMulti(line);
        cells.addAll(Arrays.asList(tokens));
        while (parser.isPending()) {
            tokens = parser.parseLineMulti(lnReader.readLine());
            cells.addAll(Arrays.asList(tokens));
        }
        return cells;
    }

    static public boolean guessQuotes(ImportingJob job, List<JSONObject> fileRecords) {
        for (int i = 0; i < 5 && i < fileRecords.size(); i++) {
            JSONObject fileRecord = fileRecords.get(i);
            String encoding = ImportingUtilities.getEncoding(fileRecord);
            String location = JSONUtilities.getString(fileRecord, "location", null);

            if (location != null) {
                File file = new File(job.getRawDataDir(), location);
                // Quotes are turned on by default, so use that for guessing
                return guessQuotes(file, encoding);
            }
        }
        return true;
    }

    static public boolean guessQuotes(File file, String encoding) {
        boolean quotes = !IGNORE_QUOTES;
        try (InputStream is = new FileInputStream(file);
                Reader reader = encoding != null ? new InputStreamReader(is, encoding) : new InputStreamReader(is);
                LineNumberReader lineNumberReader = new LineNumberReader(reader);) {

            int totalChars = 0;
            int lineCount = 0;
            int totalQuotes = 0;
            boolean inEscape = false;

            String s;
            while (totalChars < 64 * 1024 && lineCount < 100 && (s = lineNumberReader.readLine()) != null) {

                totalChars += s.length() + 1; // count the new line character
                if (s.length() == 0) {
                    continue;
                }
                if (!inEscape) {
                    lineCount++;
                }

                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (c == '"' && !inEscape) {
                        totalQuotes++;
                    }
                    if ('\\' == c) {
                        inEscape = !inEscape;
                    } else {
                        inEscape = false;
                    }

                }
                if (totalQuotes % 2 == 0) {
                    totalQuotes = 0;
                } else {
                    quotes = IGNORE_QUOTES;
                    break;
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return quotes;
    }

    static public String guessSeparator(ImportingJob job, List<JSONObject> fileRecords, boolean quotes) {
        for (int i = 0; i < 5 && i < fileRecords.size(); i++) {
            JSONObject fileRecord = fileRecords.get(i);
            String encoding = ImportingUtilities.getEncoding(fileRecord);
            String location = JSONUtilities.getString(fileRecord, "location", null);

            if (location != null) {
                File file = new File(job.getRawDataDir(), location);
                // Quotes are turned on by default, so use that for guessing
                Separator separator = guessSeparator(file, encoding, quotes);
                if (separator != null) {
                    return StringEscapeUtils.escapeJava(Character.toString(separator.separator));
                }
            }
        }
        return null;
    }

    static public class Separator {

        public char separator;
        public int totalCount = 0;
        public int totalOfSquaredCount = 0;
        public int currentLineCount = 0;

        public double averagePerLine;
        public double stddev;
    }

    static public Separator guessSeparator(File file, String encoding) {
        return guessSeparator(file, encoding, false); // quotes off for backward compatibility
    }

    // TODO: Move this to the CSV project?
    static public Separator guessSeparator(File file, String encoding, boolean handleQuotes) {
        try (InputStream is = new FileInputStream(file);
                Reader reader = encoding != null ? new InputStreamReader(is, encoding) : new InputStreamReader(is);
                LineNumberReader lineNumberReader = new LineNumberReader(reader);) {

            List<Separator> separators = new ArrayList<SeparatorBasedImporter.Separator>();
            Map<Character, Separator> separatorMap = new HashMap<Character, SeparatorBasedImporter.Separator>();

            int totalChars = 0;
            int lineCount = 0;
            boolean inQuote = false;
            String line;
            while (totalChars < 64 * 1024 && lineCount < 100 && (line = lineNumberReader.readLine()) != null) {

                totalChars += line.length() + 1; // count the new line character
                if (line.length() == 0) {
                    continue;
                }
                if (!inQuote) {
                    lineCount++;
                }

                inQuote = getSeperatorsOnLine(handleQuotes, separators, separatorMap, inQuote, line);

                if (!inQuote) {
                    incrementSeperators(separators);
                }
            }

            if (separators.size() > 0) {
                calcStandardDeviationForSeperators(separators, lineCount);

                separators.sort((a, b) -> Double.compare(a.stddev / a.averagePerLine, b.stddev / b.averagePerLine));

                Separator mostLikelySeparator = separators.get(0);
                if (mostLikelySeparator.stddev / mostLikelySeparator.averagePerLine < 0.1) {
                    return mostLikelySeparator;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean getSeperatorsOnLine(boolean handleQuotes, List<Separator> separators,
            Map<Character, Separator> separatorMap, boolean inQuote, String string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if ('"' == c && handleQuotes) {
                inQuote = !inQuote;
            }
            if (isPotentialSeparator(handleQuotes, inQuote, string, i, c)) {
                Separator separator = separatorMap.get(c);
                if (separator == null) {
                    separator = new Separator();
                    separator.separator = c;

                    separatorMap.put(c, separator);
                    separators.add(separator);
                }
                separator.currentLineCount++;
            }
        }
        return inQuote;
    }

    private static boolean isPotentialSeparator(boolean handleQuotes, boolean inQuote, String string, int i, char c) {
        return !Character.isLetterOrDigit(c) && !"\"' .-".contains(string.subSequence(i, i + 1))
                && (!handleQuotes || !inQuote);
    }

    private static void calcStandardDeviationForSeperators(List<Separator> separators, int lineCount) {
        for (Separator separator : separators) {
            separator.averagePerLine = separator.totalCount / (double) lineCount;
            separator.stddev = Math.sqrt((((double) lineCount * separator.totalOfSquaredCount)
                    - (separator.totalCount * separator.totalCount)) / ((double) lineCount * (lineCount - 1)));
        }
    }

    public static void incrementSeperators(List<Separator> separators) {
        for (Separator separator : separators) {
            separator.totalCount += separator.currentLineCount;
            separator.totalOfSquaredCount += separator.currentLineCount * separator.currentLineCount;
            separator.currentLineCount = 0;
        }
    }
}
