package com.success.examprep.data.importer

import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets

/**
 * Robust RFC-4180 compliant CSV parser.
 * Handles embedded commas, quoted fields, escaped quotes (""), multiline quoted text,
 * UTF-8 BOM, and mathematical unicode characters.
 */
object CsvParser {

    fun parse(inputStream: InputStream): RawSpreadsheetData {
        val reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
        return parse(reader)
    }

    fun parse(reader: Reader): RawSpreadsheetData {
        val rows = parseCsvRows(reader)
        if (rows.isEmpty()) {
            return RawSpreadsheetData(headers = emptyList(), rows = emptyList())
        }
        val headers = rows.first().map { it.trim() }
        val dataRows = rows.drop(1).filter { row -> row.any { it.isNotBlank() } }
        return RawSpreadsheetData(headers = headers, rows = dataRows)
    }

    fun parseCsvRows(reader: Reader): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false

        var intChar = reader.read()
        // Skip UTF-8 BOM if present
        if (intChar == 0xFEFF) {
            intChar = reader.read()
        }

        while (intChar != -1) {
            val c = intChar.toChar()

            if (inQuotes) {
                if (c == '"') {
                    val nextInt = reader.read()
                    if (nextInt != -1 && nextInt.toChar() == '"') {
                        // Escaped double quote: "" -> "
                        currentField.append('"')
                    } else {
                        // Closing quote
                        inQuotes = false
                        intChar = nextInt
                        continue
                    }
                } else {
                    currentField.append(c)
                }
            } else {
                when (c) {
                    '"' -> {
                        inQuotes = true
                    }
                    ',' -> {
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                    }
                    '\r' -> {
                        val nextInt = reader.read()
                        if (nextInt != -1 && nextInt.toChar() == '\n') {
                            // CRLF
                        } else {
                            intChar = nextInt
                            currentRow.add(currentField.toString().trim())
                            currentField.clear()
                            if (currentRow.any { it.isNotBlank() }) {
                                rows.add(currentRow.toList())
                            }
                            currentRow.clear()
                            continue
                        }
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                        if (currentRow.any { it.isNotBlank() }) {
                            rows.add(currentRow.toList())
                        }
                        currentRow.clear()
                    }
                    '\n' -> {
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                        if (currentRow.any { it.isNotBlank() }) {
                            rows.add(currentRow.toList())
                        }
                        currentRow.clear()
                    }
                    else -> {
                        currentField.append(c)
                    }
                }
            }
            intChar = reader.read()
        }

        // Add trailing field and row
        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString().trim())
            if (currentRow.any { it.isNotBlank() }) {
                rows.add(currentRow.toList())
            }
        }

        return rows
    }
}
