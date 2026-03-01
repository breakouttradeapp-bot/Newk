package com.kundaliai.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.kundaliai.app.data.models.BillingState
import com.kundaliai.app.utils.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BillingManager(
    private val context: Context,
    private val onBillingStateChanged: (BillingState) -> Unit
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState

    // ── Initialization ───────────────────────────────────────────────────────
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Connection established - handle pending purchases
                    scope.launch { handlePendingPurchases() }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection
                scope.launch {
                    delay(3000)
                    initialize()
                }
            }
        })
    }

    // ── Launch Purchase Flow ─────────────────────────────────────────────────
    fun launchPurchaseFlow(activity: Activity) {
        val client = billingClient
        if (client == null || !client.isReady) {
            onBillingStateChanged(BillingState.Error("Billing service not ready. Please try again."))
            initialize()
            return
        }

        _billingState.value = BillingState.Loading
        onBillingStateChanged(BillingState.Loading)

        scope.launch {
            // Query product details
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(Constants.PRODUCT_KUNDALI_PDF)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val (billingResult, productDetailsList) = client.queryProductDetails(params)

            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                val state = BillingState.Error("Failed to load product details: ${billingResult.debugMessage}")
                _billingState.value = state
                onBillingStateChanged(state)
                return@launch
            }

            val productDetails = productDetailsList?.firstOrNull()
            if (productDetails == null) {
                val state = BillingState.Error("Product not found. Please contact support.")
                _billingState.value = state
                onBillingStateChanged(state)
                return@launch
            }

            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            client.launchBillingFlow(activity, billingFlowParams)
        }
    }

    // ── Purchase Listener ────────────────────────────────────────────────────
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase -> scope.launch { handlePurchase(purchase) } }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                val state = BillingState.PurchaseCancelled
                _billingState.value = state
                onBillingStateChanged(state)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                val state = BillingState.AlreadyPurchased
                _billingState.value = state
                onBillingStateChanged(state)
                // Restore the purchase
                scope.launch { handlePendingPurchases() }
            }
            else -> {
                val state = BillingState.Error("Purchase failed: ${billingResult.debugMessage}")
                _billingState.value = state
                onBillingStateChanged(state)
            }
        }
    }

    // ── Handle Purchase (Acknowledge) ────────────────────────────────────────
    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                val ackResult = billingClient?.acknowledgePurchase(acknowledgePurchaseParams)

                if (ackResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                    val state = BillingState.PurchaseSuccess
                    _billingState.value = state
                    onBillingStateChanged(state)
                } else {
                    val state = BillingState.Error("Failed to acknowledge purchase. Please contact support.")
                    _billingState.value = state
                    onBillingStateChanged(state)
                }
            } else {
                // Already acknowledged
                val state = BillingState.PurchaseSuccess
                _billingState.value = state
                onBillingStateChanged(state)
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            // Inform user purchase is pending
            val state = BillingState.Error("Purchase is pending. It will be processed once payment is confirmed.")
            _billingState.value = state
            onBillingStateChanged(state)
        }
    }

    // ── Check Existing Purchases ─────────────────────────────────────────────
    suspend fun checkIfPurchased(): Boolean {
        val client = billingClient ?: return false
        if (!client.isReady) return false

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = client.queryPurchasesAsync(params)

        return result.purchasesList.any { purchase ->
            purchase.products.contains(Constants.PRODUCT_KUNDALI_PDF) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }
    }

    private suspend fun handlePendingPurchases() {
        val client = billingClient ?: return
        if (!client.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = client.queryPurchasesAsync(params)
        result.purchasesList.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                !purchase.isAcknowledged
            ) {
                handlePurchase(purchase)
            } else if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                val state = BillingState.AlreadyPurchased
                _billingState.value = state
                onBillingStateChanged(state)
            }
        }
    }

    // ── Cleanup ──────────────────────────────────────────────────────────────
    fun destroy() {
        scope.cancel()
        billingClient?.endConnection()
        billingClient = null
    }
}
