package com.kundaliai.app.data.repository

import com.kundaliai.app.BuildConfig
import com.kundaliai.app.data.api.CerebrasApiService
import com.kundaliai.app.data.models.*
import com.kundaliai.app.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

class KundaliRepository {

    private val apiService = CerebrasApiService.create()
    private val apiKey = BuildConfig.CEREBRAS_API_KEY

    suspend fun generateKundali(request: KundaliRequest): ApiState<KundaliResult> {
        return withContext(Dispatchers.IO) {
            try {
                // Step 1: Compute basic Jyotish data locally (deterministic seed from DOB/TOB)
                val jyotishData = computeJyotishData(request)

                // Step 2: Get AI preview from Cerebras
                val previewResult = fetchAIPreview(request, jyotishData)

                ApiState.Success(previewResult)
            } catch (e: Exception) {
                ApiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    suspend fun generateFullPrediction(result: KundaliResult): ApiState<KundaliResult> {
        return withContext(Dispatchers.IO) {
            try {
                val fullResult = fetchFullPredictions(result)
                ApiState.Success(fullResult)
            } catch (e: Exception) {
                ApiState.Error(e.message ?: "Failed to generate full prediction")
            }
        }
    }

    // ── Jyotish Calculations (Simplified deterministic) ─────────────────────
    private fun computeJyotishData(request: KundaliRequest): JyotishData {
        val seed = buildSeed(request)
        val rng = Random(seed)

        val lagna = Constants.ZODIAC_SIGNS[rng.nextInt(12)]
        val rashi = Constants.ZODIAC_SIGNS[rng.nextInt(12)]
        val nakshatra = Constants.NAKSHATRAS[rng.nextInt(27)]
        val rashiLord = getPlanetaryLord(rashi)
        val lagnaLord = getPlanetaryLord(lagna)

        val planets = Constants.PLANETS.map { planet ->
            PlanetPosition(
                planet = planet,
                sign = Constants.ZODIAC_SIGNS[rng.nextInt(12)],
                house = rng.nextInt(12) + 1,
                degree = rng.nextFloat() * 30f
            )
        }

        return JyotishData(lagna, rashi, nakshatra, rashiLord, lagnaLord, planets)
    }

    private fun buildSeed(request: KundaliRequest): Long {
        return (request.dateOfBirth + request.timeOfBirth + request.placeOfBirth)
            .hashCode().toLong()
    }

    private fun getPlanetaryLord(sign: String): String = when (sign) {
        "Aries" -> "Mars"
        "Taurus" -> "Venus"
        "Gemini" -> "Mercury"
        "Cancer" -> "Moon"
        "Leo" -> "Sun"
        "Virgo" -> "Mercury"
        "Libra" -> "Venus"
        "Scorpio" -> "Mars"
        "Sagittarius" -> "Jupiter"
        "Capricorn" -> "Saturn"
        "Aquarius" -> "Saturn"
        "Pisces" -> "Jupiter"
        else -> "Unknown"
    }

    // ── Cerebras AI Calls ────────────────────────────────────────────────────
    private suspend fun fetchAIPreview(request: KundaliRequest, jyotish: JyotishData): KundaliResult {
        val langInstruction = getLanguageInstruction(request.language)
        val prompt = """
            You are an expert Vedic astrologer. Generate a brief horoscope preview for:
            Name: ${request.name}
            Date of Birth: ${request.dateOfBirth}
            Time of Birth: ${request.timeOfBirth}
            Place of Birth: ${request.placeOfBirth}
            Lagna: ${jyotish.lagna}
            Rashi: ${jyotish.rashi}
            Nakshatra: ${jyotish.nakshatra}
            $langInstruction
            
            Provide a 3-4 sentence personalized horoscope preview in the specified language.
            Also suggest:
            Lucky Color: [one color]
            Lucky Number: [1-9]
            Gemstone: [one gemstone]
            
            Format response exactly as:
            PREVIEW: [Your preview text here]
            LUCKY_COLOR: [color]
            LUCKY_NUMBER: [number]
            GEMSTONE: [gemstone]
        """.trimIndent()

        val cerebrasMsgs = listOf(
            CerebrasMessage("system", "You are an expert Vedic astrologer who provides insightful, guidance-oriented horoscope readings. Always include a disclaimer that predictions are for guidance only."),
            CerebrasMessage("user", prompt)
        )

        val response = apiService.getChatCompletion(
            auth = "Bearer $apiKey",
            request = CerebrasRequest(messages = cerebrasMsgs, max_tokens = 500)
        )

        val content = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
        return parsePreviewResponse(request, jyotish, content)
    }

    private suspend fun fetchFullPredictions(existing: KundaliResult): KundaliResult {
        val langInstruction = getLanguageInstruction(existing.language)
        val prompt = """
            You are an expert Vedic astrologer. Generate detailed predictions for:
            Name: ${existing.userName}
            Date of Birth: ${existing.dateOfBirth}
            Lagna: ${existing.lagna}
            Rashi: ${existing.rashi}
            Nakshatra: ${existing.nakshatra}
            $langInstruction
            
            Provide detailed predictions in the specified language for each category.
            Format exactly as:
            CAREER: [2-3 sentences]
            MARRIAGE: [2-3 sentences]
            HEALTH: [2-3 sentences]
            FINANCE: [2-3 sentences]
            DASHA: [current dasha period and effects, 2-3 sentences]
            REMEDIES: [3-4 specific Vedic remedies]
        """.trimIndent()

        val cerebrasMsgs = listOf(
            CerebrasMessage("system", "You are an expert Vedic astrologer. Provide detailed, personalized, guidance-oriented predictions. Never make absolute claims or medical/financial guarantees."),
            CerebrasMessage("user", prompt)
        )

        val response = apiService.getChatCompletion(
            auth = "Bearer $apiKey",
            request = CerebrasRequest(messages = cerebrasMsgs, max_tokens = 1500)
        )

        val content = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
        return parseFullPredictionResponse(existing, content)
    }

    // ── Response Parsers ─────────────────────────────────────────────────────
    private fun parsePreviewResponse(
        request: KundaliRequest,
        jyotish: JyotishData,
        content: String
    ): KundaliResult {
        val preview = extractField(content, "PREVIEW") ?: "Your stars reveal a journey of growth and wisdom."
        val luckyColor = extractField(content, "LUCKY_COLOR") ?: Constants.LUCKY_COLORS.random()
        val luckyNumberStr = extractField(content, "LUCKY_NUMBER") ?: "7"
        val gemstone = extractField(content, "GEMSTONE") ?: Constants.GEMSTONES.first()
        val luckyNumber = luckyNumberStr.trim().filter { it.isDigit() }.toIntOrNull() ?: 7

        val disclaimer = getDisclaimer(request.language)

        return KundaliResult(
            userName = request.name,
            dateOfBirth = request.dateOfBirth,
            timeOfBirth = request.timeOfBirth,
            placeOfBirth = request.placeOfBirth,
            language = request.language,
            lagna = jyotish.lagna,
            rashi = jyotish.rashi,
            nakshatra = jyotish.nakshatra,
            rashiLord = jyotish.rashiLord,
            lagnaLord = jyotish.lagnaLord,
            planetPositions = jyotish.planets,
            aiPreview = preview,
            luckyColor = luckyColor,
            luckyNumber = luckyNumber,
            gemstone = gemstone,
            disclaimer = disclaimer
        )
    }

    private fun parseFullPredictionResponse(existing: KundaliResult, content: String): KundaliResult {
        return existing.copy(
            careerPrediction = extractField(content, "CAREER") ?: "",
            marriagePrediction = extractField(content, "MARRIAGE") ?: "",
            healthPrediction = extractField(content, "HEALTH") ?: "",
            financePrediction = extractField(content, "FINANCE") ?: "",
            dashaInfo = extractField(content, "DASHA") ?: "",
            remedies = extractField(content, "REMEDIES") ?: ""
        )
    }

    private fun extractField(content: String, key: String): String? {
        val lines = content.lines()
        for (line in lines) {
            if (line.startsWith("$key:")) {
                return line.substringAfter("$key:").trim()
            }
        }
        return null
    }

    private fun getLanguageInstruction(language: String): String = when (language) {
        Constants.LANG_HINDI -> "Language: Hindi (Respond in Hindi language using Devanagari script)"
        Constants.LANG_MARATHI -> "Language: Marathi (Respond in Marathi language using Devanagari script)"
        else -> "Language: English"
    }

    private fun getDisclaimer(language: String): String = when (language) {
        Constants.LANG_HINDI -> "ये भविष्यवाणियाँ AI-जनित ज्योतिष अंतर्दृष्टि हैं जो केवल मार्गदर्शन उद्देश्यों के लिए हैं। इन्हें निश्चित या गारंटीकृत परिणाम नहीं माना जाना चाहिए।"
        Constants.LANG_MARATHI -> "या भविष्यवाण्या AI-निर्मित ज्योतिष अंतर्दृष्टी आहेत जे केवळ मार्गदर्शनाच्या उद्देशाने आहेत. त्यांना निश्चित किंवा हमी दिलेले परिणाम मानू नये."
        else -> "These predictions are AI-generated astrology insights for guidance purposes only and should not be considered absolute or guaranteed outcomes. Consult a professional astrologer for important life decisions."
    }

    data class JyotishData(
        val lagna: String,
        val rashi: String,
        val nakshatra: String,
        val rashiLord: String,
        val lagnaLord: String,
        val planets: List<PlanetPosition>
    )
}
