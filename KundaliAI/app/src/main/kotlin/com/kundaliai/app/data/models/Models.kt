package com.kundaliai.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class KundaliRequest(
    val name: String,
    val dateOfBirth: String,   // dd/MM/yyyy
    val timeOfBirth: String,   // HH:mm
    val placeOfBirth: String,
    val language: String       // en / hi / mr
)

@Parcelize
data class KundaliResult(
    val userName: String,
    val dateOfBirth: String,
    val timeOfBirth: String,
    val placeOfBirth: String,
    val language: String,

    // Jyotish basics
    val lagna: String,
    val rashi: String,
    val nakshatra: String,
    val rashiLord: String,
    val lagnaLord: String,

    // Planet positions
    val planetPositions: List<PlanetPosition>,

    // AI predictions (preview = short, full = detailed)
    val aiPreview: String,
    val careerPrediction: String = "",
    val marriagePrediction: String = "",
    val healthPrediction: String = "",
    val financePrediction: String = "",
    val dashaInfo: String = "",
    val luckyColor: String,
    val luckyNumber: Int,
    val gemstone: String,
    val remedies: String = "",
    val disclaimer: String
) : Parcelable

@Parcelize
data class PlanetPosition(
    val planet: String,
    val sign: String,
    val house: Int,
    val degree: Float
) : Parcelable

// Cerebras API models
data class CerebrasRequest(
    val model: String = "gpt-oss-120b",
    val messages: List<CerebrasMessage>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 2000,
    val top_p: Float = 1f
)

data class CerebrasMessage(
    val role: String,
    val content: String
)

data class CerebrasResponse(
    val id: String,
    val choices: List<CerebrasChoice>,
    val usage: CerebrasUsage?
)

data class CerebrasChoice(
    val message: CerebrasMessage,
    val finish_reason: String?
)

data class CerebrasUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

// API State sealed class
sealed class ApiState<out T> {
    object Loading : ApiState<Nothing>()
    data class Success<T>(val data: T) : ApiState<T>()
    data class Error(val message: String, val code: Int = -1) : ApiState<Nothing>()
}

// Billing state
sealed class BillingState {
    object Idle : BillingState()
    object Loading : BillingState()
    object PurchaseSuccess : BillingState()
    object AlreadyPurchased : BillingState()
    data class Error(val message: String) : BillingState()
    object PurchaseCancelled : BillingState()
}
