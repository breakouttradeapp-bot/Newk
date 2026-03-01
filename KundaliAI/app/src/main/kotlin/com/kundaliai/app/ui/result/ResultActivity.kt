package com.kundaliai.app.ui.result

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.kundaliai.app.R
import com.kundaliai.app.ads.AdManager
import com.kundaliai.app.billing.BillingManager
import com.kundaliai.app.data.models.BillingState
import com.kundaliai.app.data.models.KundaliResult
import com.kundaliai.app.databinding.ActivityResultBinding
import com.kundaliai.app.pdf.PdfGenerator
import com.kundaliai.app.ui.viewmodel.KundaliViewModel
import com.kundaliai.app.utils.Constants
import com.kundaliai.app.utils.gone
import com.kundaliai.app.utils.showToast
import com.kundaliai.app.utils.visible
import kotlinx.coroutines.launch

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private val viewModel: KundaliViewModel by viewModels()
    private lateinit var billingManager: BillingManager
    private lateinit var pdfGenerator: PdfGenerator

    private var kundaliResult: KundaliResult? = null
    private var isPdfUnlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        kundaliResult = intent.getParcelableExtra(Constants.KEY_KUNDALI_RESULT)
        
        pdfGenerator = PdfGenerator(this)

        setupUI()
        setupBilling()
        displayResult()
        loadBannerAd()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.your_kundali)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnDownloadPdf.setOnClickListener {
            if (isPdfUnlocked) {
                generateAndSharePdf()
            } else {
                billingManager.launchPurchaseFlow(this)
            }
        }
    }

    private fun setupBilling() {
        billingManager = BillingManager(this) { state ->
            runOnUiThread { handleBillingState(state) }
        }
        billingManager.initialize()

        // Check if already purchased
        lifecycleScope.launch {
            isPdfUnlocked = billingManager.checkIfPurchased()
            updateDownloadButton()
        }
    }

    private fun handleBillingState(state: BillingState) {
        when (state) {
            is BillingState.Loading -> {
                binding.progressPdf.visible()
            }
            is BillingState.PurchaseSuccess, is BillingState.AlreadyPurchased -> {
                binding.progressPdf.gone()
                isPdfUnlocked = true
                updateDownloadButton()
                showToast(getString(R.string.purchase_success))
                // Auto-generate PDF
                generateAndSharePdf()
            }
            is BillingState.PurchaseCancelled -> {
                binding.progressPdf.gone()
                showToast(getString(R.string.purchase_cancelled))
            }
            is BillingState.Error -> {
                binding.progressPdf.gone()
                showToast(state.message)
            }
            is BillingState.Idle -> {
                binding.progressPdf.gone()
            }
        }
    }

    private fun updateDownloadButton() {
        if (isPdfUnlocked) {
            binding.btnDownloadPdf.text = getString(R.string.download_pdf_free)
            binding.btnDownloadPdf.setIconResource(R.drawable.ic_download)
        } else {
            binding.btnDownloadPdf.text = getString(R.string.download_pdf_paid)
            binding.btnDownloadPdf.setIconResource(R.drawable.ic_lock)
        }
    }

    private fun displayResult() {
        val result = kundaliResult ?: return

        binding.tvUserName.text = result.userName
        binding.tvDob.text = "${result.dateOfBirth}, ${result.timeOfBirth}"
        binding.tvPob.text = result.placeOfBirth

        // Kundali basics
        binding.tvLagna.text = result.lagna
        binding.tvRashi.text = result.rashi
        binding.tvNakshatra.text = result.nakshatra
        binding.tvLagnaLord.text = result.lagnaLord
        binding.tvRashiLord.text = result.rashiLord

        // Planet positions
        val planetBuilder = StringBuilder()
        result.planetPositions.forEach { planet ->
            planetBuilder.append("${planet.planet}: ${planet.sign} (House ${planet.house})\n")
        }
        binding.tvPlanetPositions.text = planetBuilder.toString().trim()

        // AI Preview
        binding.tvAiPreview.text = result.aiPreview

        // Lucky details
        binding.tvLuckyColor.text = result.luckyColor
        binding.tvLuckyNumber.text = result.luckyNumber.toString()
        binding.tvGemstone.text = result.gemstone

        // Disclaimer
        binding.tvDisclaimer.text = result.disclaimer
    }

    private fun generateAndSharePdf() {
        val result = kundaliResult ?: return

        lifecycleScope.launch {
            binding.progressPdf.visible()
            binding.btnDownloadPdf.isEnabled = false

            val file = pdfGenerator.generatePdf(result)

            binding.progressPdf.gone()
            binding.btnDownloadPdf.isEnabled = true

            if (file != null && file.exists()) {
                val uri = FileProvider.getUriForFile(
                    this@ResultActivity,
                    "${packageName}.provider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                startActivity(Intent.createChooser(shareIntent, getString(R.string.open_pdf)))
            } else {
                showToast(getString(R.string.pdf_error))
            }
        }
    }

    private fun loadBannerAd() {
        AdManager.getInstance().loadBannerAd(this, binding.adContainer)
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.destroy()
    }
}
