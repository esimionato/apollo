package io.muun.apollo.presentation.ui.show_qr.ln

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.core.widget.NestedScrollView
import butterknife.BindView
import butterknife.OnClick
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import icepick.State
import io.muun.apollo.R
import io.muun.apollo.domain.libwallet.DecodedInvoice
import io.muun.apollo.domain.model.BitcoinUnit
import io.muun.apollo.presentation.ui.InvoiceExpirationCountdownTimer
import io.muun.apollo.presentation.ui.new_operation.TitleAndDescriptionDrawer
import io.muun.apollo.presentation.ui.select_amount.SelectAmountActivity
import io.muun.apollo.presentation.ui.show_qr.QrFragment
import io.muun.apollo.presentation.ui.view.EditAmountItem
import io.muun.apollo.presentation.ui.view.ExpirationTimeItem
import io.muun.apollo.presentation.ui.view.HiddenSection
import io.muun.apollo.presentation.ui.view.LoadingView
import io.muun.apollo.presentation.ui.view.MuunButton
import javax.money.MonetaryAmount


class LnInvoiceQrFragment : QrFragment<LnInvoiceQrPresenter>(),
    LnInvoiceView,
    EditAmountItem.EditAmountHandler {

    companion object {
        private const val REQUEST_AMOUNT = 2
    }

    @BindView(R.id.scrollView)
    lateinit var scrollView: NestedScrollView

    @BindView(R.id.qr_overlay)
    lateinit var qrOverlay: View

    @BindView(R.id.invoice_settings)
    lateinit var hiddenSection: HiddenSection

    @BindView(R.id.invoice_settings_content)
    lateinit var invoiceSettingsContent: View

    @BindView(R.id.edit_amount_item)
    lateinit var editAmountItem: EditAmountItem

    @BindView(R.id.expiration_time_item)
    lateinit var expirationTimeItem: ExpirationTimeItem

    @BindView(R.id.invoice_loading)
    lateinit var loadingView: LoadingView

    @BindView(R.id.show_qr_copy)
    lateinit var copyButton: MuunButton

    @BindView(R.id.show_qr_share)
    lateinit var shareButton: MuunButton

    @BindView(R.id.invoice_expired_overlay)
    lateinit var invoiceExpiredOverlay: View

    // State:

    @State
    lateinit var mBitcoinUnit: BitcoinUnit

    private var countdownTimer: InvoiceExpirationCountdownTimer? = null

    override fun inject() {
        component.inject(this)
    }

    override fun getLayoutResource() =
        R.layout.fragment_show_qr_ln

    override fun initializeUi(view: View?) {
        super.initializeUi(view)
        editAmountItem.setEditAmountHandler(this)
    }

    override fun setShowingAdvancedSettings(showingAdvancedSettings: Boolean) {
        hiddenSection.setExpanded(showingAdvancedSettings)
        if (showingAdvancedSettings) {
            invoiceSettingsContent.visibility = View.VISIBLE
        }
    }

    override fun setBitcoinUnit(bitcoinUnit: BitcoinUnit) {
        this.mBitcoinUnit = bitcoinUnit
    }

    override fun setLoading(loading: Boolean) {
        loadingView.visibility = if (loading) View.VISIBLE else View.GONE
        qrContent.visibility = if (loading) View.INVISIBLE else View.VISIBLE // Keep view Bounds

        copyButton.isEnabled = !loading
        shareButton.isEnabled = !loading

        editAmountItem.setLoading(loading)
        expirationTimeItem.setLoading(loading)
    }

    override fun setInvoice(invoice: DecodedInvoice, amount: MonetaryAmount?) {

        // Enable extra QR compression mode. Uppercase bech32 strings are more efficiently encoded
        super.setQrContent(invoice.original, invoice.original.toUpperCase())

        // Detect if 1h left of expiration time, and show countdown
        stopTimer()
        countdownTimer = buildCountDownTimer(invoice.remainingMillis())
        countdownTimer!!.start()

        if (amount != null) {
            editAmountItem.setAmount(amount, mBitcoinUnit)

        } else {
            editAmountItem.resetAmount()
        }
    }

    override fun onStop() {
        super.onStop()
        stopTimer()
    }

    override fun showFullContent(invoice: String) {
        val dialog = TitleAndDescriptionDrawer()
        dialog.setTitle(R.string.your_ln_invoice)
        dialog.setDescription(invoice)
        showDrawerDialog(dialog)
    }

    override fun getErrorCorrection(): ErrorCorrectionLevel =
        ErrorCorrectionLevel.L  // Bech 32 already has its own error correction.

    @OnClick(R.id.create_other_invoice)
    fun onCreateInvoiceClick() {
        presenter.generateNewEmptyInvoice()
        resetViewState()
    }

    @OnClick(R.id.invoice_settings)
    fun onInvoiceSettingsClick() {
        presenter.toggleAdvancedSettings()
        hiddenSection.toggleSection()

        if (invoiceSettingsContent.visibility == View.VISIBLE) {
            invoiceSettingsContent.visibility = View.GONE

        } else {
            invoiceSettingsContent.visibility = View.VISIBLE

            scrollView.postDelayed({
                scrollView.fullScroll(View.FOCUS_DOWN)
            }, 100)
        }
    }

    override fun onEditAmount(amount: MonetaryAmount?) {
        requestDelegatedExternalResult(
            REQUEST_AMOUNT,
            SelectAmountActivity.getSelectInvoiceAmountIntent(requireContext(), amount)
        )
    }

    override fun onExternalResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onExternalResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_AMOUNT && resultCode == Activity.RESULT_OK) {
            val result = SelectAmountActivity.getResult(data!!)

            if (result != null && !result.isZero) {
                presenter.setAmount(result)

            } else {
                resetAmount()
            }
        }
    }

    override fun resetAmount() {
        presenter.setAmount(null)
    }

    private fun resetViewState() {
        invoiceExpiredOverlay.visibility = View.GONE
        qrOverlay.visibility = View.VISIBLE
        hiddenSection.visibility = View.VISIBLE
    }

    private fun stopTimer() {
        if (countdownTimer != null) {
            countdownTimer!!.cancel()
            countdownTimer = null
        }
    }

    private fun buildCountDownTimer(remainingMillis: Long): InvoiceExpirationCountdownTimer {

        return object : InvoiceExpirationCountdownTimer(context, remainingMillis) {
            override fun onTextUpdate(remainingSeconds: Long, text: CharSequence) {
                expirationTimeItem.setExpirationTime(text)
            }

            override fun onFinish() {
                invoiceExpiredOverlay.visibility = View.VISIBLE
                qrOverlay.visibility = View.GONE
                hiddenSection.visibility = View.GONE
                invoiceSettingsContent.visibility = View.GONE
            }
        }
    }

    fun refresh() {
        resetViewState()
        presenter.generateNewInvoice()
    }
}
