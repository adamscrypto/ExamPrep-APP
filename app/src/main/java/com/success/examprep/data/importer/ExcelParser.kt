package com.success.examprep.data.importer

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.xml.parsers.SAXParserFactory

/**
 * Lightweight streaming parser for OpenXML Excel spreadsheets (.xlsx).
 * Uses SAX parsing and ZipInputStream for high speed, low memory overhead,
 * and zero external library dependencies.
 */
object ExcelParser {

    fun parse(inputStream: InputStream): RawSpreadsheetData {
        var sharedStringsXmlBytes: ByteArray? = null
        var worksheetXmlBytes: ByteArray? = null

        val zip = ZipInputStream(inputStream)
        var entry = zip.nextEntry
        while (entry != null) {
            val name = entry.name.lowercase()
            if (name == "xl/sharedstrings.xml") {
                sharedStringsXmlBytes = zip.readBytes()
            } else if (name == "xl/worksheets/sheet1.xml" || (worksheetXmlBytes == null && name.startsWith("xl/worksheets/sheet") && name.endsWith(".xml"))) {
                worksheetXmlBytes = zip.readBytes()
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }

        if (worksheetXmlBytes == null) {
            return RawSpreadsheetData(headers = emptyList(), rows = emptyList())
        }

        // 1. Parse shared strings if present
        val sharedStrings = if (sharedStringsXmlBytes != null) {
            parseSharedStrings(ByteArrayInputStream(sharedStringsXmlBytes))
        } else {
            emptyList()
        }

        // 2. Parse worksheet rows
        val allRows = parseWorksheet(ByteArrayInputStream(worksheetXmlBytes), sharedStrings)
        if (allRows.isEmpty()) {
            return RawSpreadsheetData(headers = emptyList(), rows = emptyList())
        }

        val headers = allRows.first().map { it.trim() }
        val dataRows = allRows.drop(1).filter { row -> row.any { it.isNotBlank() } }
        return RawSpreadsheetData(headers = headers, rows = dataRows)
    }

    private fun parseSharedStrings(stream: InputStream): List<String> {
        val strings = ArrayList<String>()
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()

        val handler = object : DefaultHandler() {
            private val currentText = StringBuilder()
            private var insideT = false

            override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
                val tag = (qName ?: localName ?: "").lowercase()
                if (tag == "t") {
                    insideT = true
                } else if (tag == "si") {
                    currentText.clear()
                }
            }

            override fun characters(ch: CharArray?, start: Int, length: Int) {
                if (insideT && ch != null) {
                    currentText.append(ch, start, length)
                }
            }

            override fun endElement(uri: String?, localName: String?, qName: String?) {
                val tag = (qName ?: localName ?: "").lowercase()
                if (tag == "t") {
                    insideT = false
                } else if (tag == "si") {
                    strings.add(currentText.toString())
                }
            }
        }

        parser.parse(stream, handler)
        return strings
    }

    private fun parseWorksheet(stream: InputStream, sharedStrings: List<String>): List<List<String>> {
        val rows = ArrayList<List<String>>()
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()

        val handler = object : DefaultHandler() {
            private val currentRowCells = HashMap<Int, String>()
            private var currentCellIndex = 0
            private var currentCellType: String? = null
            private val currentCellValue = StringBuilder()
            private var insideV = false
            private var insideInlineT = false

            override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
                val tag = (qName ?: localName ?: "").lowercase()
                when (tag) {
                    "row" -> {
                        currentRowCells.clear()
                        currentCellIndex = 0
                    }
                    "c" -> {
                        val ref = attributes?.getValue("r")
                        currentCellIndex = if (!ref.isNullOrBlank()) {
                            columnNameToIndex(ref.takeWhile { it.isLetter() })
                        } else {
                            currentCellIndex
                        }
                        currentCellType = attributes?.getValue("t")
                        currentCellValue.clear()
                    }
                    "v" -> {
                        insideV = true
                    }
                    "t" -> {
                        insideInlineT = true
                    }
                }
            }

            override fun characters(ch: CharArray?, start: Int, length: Int) {
                if ((insideV || insideInlineT) && ch != null) {
                    currentCellValue.append(ch, start, length)
                }
            }

            override fun endElement(uri: String?, localName: String?, qName: String?) {
                val tag = (qName ?: localName ?: "").lowercase()
                when (tag) {
                    "v" -> {
                        insideV = false
                    }
                    "t" -> {
                        insideInlineT = false
                    }
                    "c" -> {
                        val rawText = currentCellValue.toString().trim()
                        val resolvedValue = when (currentCellType) {
                            "s" -> {
                                val idx = rawText.toIntOrNull()
                                if (idx != null && idx in sharedStrings.indices) {
                                    sharedStrings[idx]
                                } else {
                                    rawText
                                }
                            }
                            "b" -> {
                                if (rawText == "1") "TRUE" else "FALSE"
                            }
                            else -> {
                                // Numeric or plain string
                                if (rawText.endsWith(".0")) {
                                    rawText.removeSuffix(".0")
                                } else {
                                    rawText
                                }
                            }
                        }
                        currentRowCells[currentCellIndex] = resolvedValue
                        currentCellIndex++
                    }
                    "row" -> {
                        if (currentRowCells.isNotEmpty()) {
                            val maxIndex = currentRowCells.keys.maxOrNull() ?: -1
                            val rowValues = ArrayList<String>(maxIndex + 1)
                            for (i in 0..maxIndex) {
                                rowValues.add(currentRowCells[i] ?: "")
                            }
                            if (rowValues.any { it.isNotBlank() }) {
                                rows.add(rowValues)
                            }
                        }
                    }
                }
            }
        }

        parser.parse(stream, handler)
        return rows
    }

    /**
     * Converts spreadsheet column letters (e.g., "A", "B", "AA") to a 0-based column index.
     */
    fun columnNameToIndex(colName: String): Int {
        var index = 0
        for (char in colName.uppercase()) {
            if (char in 'A'..'Z') {
                index = index * 26 + (char - 'A' + 1)
            }
        }
        return (index - 1).coerceAtLeast(0)
    }
}
