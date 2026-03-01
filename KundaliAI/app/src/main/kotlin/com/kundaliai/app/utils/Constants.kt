package com.kundaliai.app.utils

object Constants {
    // Cerebras AI
    const val CEREBRAS_BASE_URL = "https://api.cerebras.ai/v1/"
    const val CEREBRAS_MODEL = "gpt-oss-120b"

    // Billing
    const val PRODUCT_KUNDALI_PDF = "kundali_pdf_unlock"

    // Languages
    const val LANG_ENGLISH = "en"
    const val LANG_HINDI = "hi"
    const val LANG_MARATHI = "mr"

    // Shared Prefs
    const val PREFS_NAME = "kundali_prefs"
    const val PREF_LANGUAGE = "selected_language"
    const val PREF_PDF_UNLOCKED = "pdf_unlocked"

    // Links
    const val PRIVACY_POLICY_URL = "https://yourwebsite.com/privacy-policy"
    const val TERMS_URL = "https://yourwebsite.com/terms"
    const val CONTACT_EMAIL = "support@kundaliai.app"

    // Intent Keys
    const val KEY_KUNDALI_RESULT = "kundali_result"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_DOB = "user_dob"
    const val KEY_USER_TOB = "user_tob"
    const val KEY_USER_POB = "user_pob"
    const val KEY_LANGUAGE = "language"

    // Zodiac Signs
    val ZODIAC_SIGNS = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    val NAKSHATRAS = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
        "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
        "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
        "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishtha",
        "Shatabhisha", "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )

    val PLANETS = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")

    val LUCKY_COLORS = listOf("Red", "Blue", "Yellow", "Green", "White", "Orange", "Purple", "Pink", "Gold", "Silver")
    val GEMSTONES = listOf("Ruby", "Pearl", "Coral", "Emerald", "Yellow Sapphire", "Diamond", "Blue Sapphire", "Hessonite", "Cat's Eye")
}
