package com.kundaliai.app.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.kundaliai.app.data.models.KundaliResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator(private val context: Context) {

    // Page dimensions (A4 at 72dpi)
    private val PAGE_WIDTH = 595
    private val PAGE_HEIGHT = 842
    private val MARGIN = 40f

    // Colors
    private val colorDeepBlue = Color.parseColor("#0D1B2A")
    private val colorGold = Color.parseColor("#FFD700")
    private val colorPurple = Color.parseColor("#3A0CA3")
    private val colorWhite = Color.WHITE
    private val colorLightGrey = Color.parseColor("#F0F0F0")
    private val colorDarkText = Color.parseColor("#1E1E32")
    private val colorMutedText = Color.parseColor("#546E7A")

    suspend fun generatePdf(result: KundaliResult): File? {
        return withContext(Dispatchers.IO) {
            try {
                val pdfDocument = PdfDocument()
                var pageNumber = 1
                var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                var page = pdfDocument.startPage(pageInfo)
                var canvas = page.canvas
                var y = MARGIN

                // ── Page 1 ──────────────────────────────────────────────────

                // Header background
                val headerPaint = Paint().apply { color = colorDeepBlue }
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 120f, headerPaint)

                // App title
                drawText(canvas, "✨ KUNDALI AI ✨", PAGE_WIDTH / 2f, 45f,
                    28f, colorGold, Paint.Align.CENTER, bold = true)
                drawText(canvas, "AI-Powered Vedic Horoscope Report", PAGE_WIDTH / 2f, 70f,
                    13f, colorWhite, Paint.Align.CENTER)
                drawText(canvas, "Ancient Wisdom. Powered by AI.", PAGE_WIDTH / 2f, 92f,
                    11f, colorGold, Paint.Align.CENTER, italic = true)

                y = 140f

                // User Details section
                y = drawSectionTitle(canvas, "USER DETAILS", y)
                y = drawTableRow(canvas, "Name", result.userName, y, alternate = false)
                y = drawTableRow(canvas, "Date of Birth", result.dateOfBirth, y, alternate = true)
                y = drawTableRow(canvas, "Time of Birth", result.timeOfBirth, y, alternate = false)
                y = drawTableRow(canvas, "Place of Birth", result.placeOfBirth, y, alternate = true)
                y = drawTableRow(canvas, "Report Date",
                    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()),
                    y, alternate = false)
                y += 16f

                // Kundali Details section
                y = drawSectionTitle(canvas, "KUNDALI DETAILS", y)
                y = drawTableRow(canvas, "Lagna (Ascendant)", result.lagna, y, alternate = false)
                y = drawTableRow(canvas, "Rashi (Moon Sign)", result.rashi, y, alternate = true)
                y = drawTableRow(canvas, "Nakshatra (Birth Star)", result.nakshatra, y, alternate = false)
                y = drawTableRow(canvas, "Lagna Lord", result.lagnaLord, y, alternate = true)
                y = drawTableRow(canvas, "Rashi Lord", result.rashiLord, y, alternate = false)
                y += 16f

                // Planet Positions
                y = drawSectionTitle(canvas, "PLANET POSITIONS", y)
                y = drawPlanetHeader(canvas, y)
                result.planetPositions.forEachIndexed { i, planet ->
                    if (y > PAGE_HEIGHT - 60f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = MARGIN
                    }
                    y = drawPlanetRow(canvas, planet.planet, planet.sign,
                        "House ${planet.house}", "%.1f°".format(planet.degree), y, i % 2 == 0)
                }
                y += 16f

                // Lucky Details
                if (y > PAGE_HEIGHT - 120f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = MARGIN
                }
                y = drawSectionTitle(canvas, "LUCKY GUIDANCE", y)
                y = drawTableRow(canvas, "Lucky Color", result.luckyColor, y, alternate = false)
                y = drawTableRow(canvas, "Lucky Number", result.luckyNumber.toString(), y, alternate = true)
                y = drawTableRow(canvas, "Gemstone", result.gemstone, y, alternate = false)
                y += 16f

                // AI Preview
                y = drawSectionTitle(canvas, "AI PREDICTION PREVIEW", y)
                y = drawMultilineText(canvas, result.aiPreview, y)
                y += 16f

                // Predictions (if available)
                if (result.careerPrediction.isNotBlank()) {
                    if (y > PAGE_HEIGHT - 100f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = MARGIN
                    }
                    y = drawSectionTitle(canvas, "CAREER", y)
                    y = drawMultilineText(canvas, result.careerPrediction, y)
                    y += 8f

                    y = drawSectionTitle(canvas, "MARRIAGE & RELATIONSHIPS", y)
                    y = drawMultilineText(canvas, result.marriagePrediction, y)
                    y += 8f

                    if (y > PAGE_HEIGHT - 120f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = MARGIN
                    }
                    y = drawSectionTitle(canvas, "HEALTH", y)
                    y = drawMultilineText(canvas, result.healthPrediction, y)
                    y += 8f

                    y = drawSectionTitle(canvas, "FINANCE", y)
                    y = drawMultilineText(canvas, result.financePrediction, y)
                    y += 8f

                    y = drawSectionTitle(canvas, "DASHA PERIOD", y)
                    y = drawMultilineText(canvas, result.dashaInfo, y)
                    y += 8f

                    y = drawSectionTitle(canvas, "VEDIC REMEDIES", y)
                    y = drawMultilineText(canvas, result.remedies, y)
                    y += 16f
                }

                // Disclaimer box
                if (y > PAGE_HEIGHT - 100f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = MARGIN
                }
                val disclaimerBg = Paint().apply { color = colorLightGrey }
                canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 70f, disclaimerBg)
                drawText(canvas, "⚠ Disclaimer", MARGIN + 8f, y + 16f, 10f, colorDarkText, bold = true)
                drawTextWrapped(canvas, result.disclaimer, MARGIN + 8f, y + 30f,
                    PAGE_WIDTH - MARGIN * 2 - 16f, 9f, colorMutedText)

                y += 80f

                // Footer
                drawText(canvas, "Generated by Kundali AI  •  For guidance purposes only",
                    PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, 8f, colorMutedText, Paint.Align.CENTER)

                pdfDocument.finishPage(page)

                // Save to file
                val fileName = "Kundali_${result.userName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
                val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    ?: context.filesDir
                val file = File(outputDir, fileName)
                FileOutputStream(file).use { pdfDocument.writeTo(it) }
                pdfDocument.close()
                file

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // ── Drawing Helpers ──────────────────────────────────────────────────────

    private fun drawText(
        canvas: Canvas, text: String, x: Float, y: Float,
        size: Float, color: Int,
        align: Paint.Align = Paint.Align.LEFT,
        bold: Boolean = false, italic: Boolean = false
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            textAlign = align
            typeface = when {
                bold && italic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                bold -> Typeface.DEFAULT_BOLD
                italic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                else -> Typeface.DEFAULT
            }
        }
        canvas.drawText(text, x, y, paint)
    }

    private fun drawSectionTitle(canvas: Canvas, title: String, y: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorDeepBlue
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }
        drawText(canvas, title, MARGIN, y + 13f, 11f, colorPurple, bold = true)
        canvas.drawLine(MARGIN, y + 18f, PAGE_WIDTH - MARGIN, y + 18f, paint)
        return y + 26f
    }

    private fun drawTableRow(canvas: Canvas, label: String, value: String, y: Float, alternate: Boolean): Float {
        val bgColor = if (alternate) colorLightGrey else colorWhite
        val bg = Paint().apply { color = bgColor }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 22f, bg)
        drawText(canvas, label, MARGIN + 8f, y + 15f, 9f, colorMutedText)
        drawText(canvas, value, PAGE_WIDTH / 2f, y + 15f, 9f, colorDarkText, bold = true)
        return y + 22f
    }

    private fun drawPlanetHeader(canvas: Canvas, y: Float): Float {
        val bg = Paint().apply { color = colorPurple }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 22f, bg)
        val colW = (PAGE_WIDTH - MARGIN * 2) / 4f
        listOf("Planet", "Sign", "House", "Degree").forEachIndexed { i, h ->
            drawText(canvas, h, MARGIN + colW * i + 4f, y + 15f, 9f, colorWhite, bold = true)
        }
        return y + 22f
    }

    private fun drawPlanetRow(canvas: Canvas, planet: String, sign: String,
                               house: String, degree: String, y: Float, alternate: Boolean): Float {
        val bg = Paint().apply { color = if (alternate) colorLightGrey else colorWhite }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f, bg)
        val colW = (PAGE_WIDTH - MARGIN * 2) / 4f
        drawText(canvas, planet, MARGIN + 4f, y + 14f, 9f, colorDarkText, bold = true)
        drawText(canvas, sign, MARGIN + colW + 4f, y + 14f, 9f, colorDarkText)
        drawText(canvas, house, MARGIN + colW * 2 + 4f, y + 14f, 9f, colorDarkText)
        drawText(canvas, degree, MARGIN + colW * 3 + 4f, y + 14f, 9f, colorDarkText)
        return y + 20f
    }

    private fun drawMultilineText(canvas: Canvas, text: String, startY: Float): Float {
        var y = startY
        val maxWidth = PAGE_WIDTH - MARGIN * 2
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f
            color = colorDarkText
        }
        val words = text.split(" ")
        var line = ""
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth) {
                canvas.drawText(line, MARGIN, y + 12f, paint)
                y += 15f
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, MARGIN, y + 12f, paint)
            y += 15f
        }
        return y
    }

    private fun drawTextWrapped(canvas: Canvas, text: String, x: Float, startY: Float,
                                 maxWidth: Float, textSize: Float, color: Int): Float {
        var y = startY
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            this.color = color
        }
        val words = text.split(" ")
        var line = ""
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth) {
                canvas.drawText(line, x, y, paint)
                y += textSize + 3f
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, x, y, paint)
        }
        return y + textSize
    }
}
