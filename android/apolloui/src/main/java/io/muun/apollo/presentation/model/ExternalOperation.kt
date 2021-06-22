package io.muun.apollo.presentation.model

import android.content.Context
import io.muun.apollo.R
import io.muun.apollo.domain.model.CurrencyDisplayMode
import io.muun.apollo.domain.model.Operation
import io.muun.apollo.domain.utils.isEmpty
import io.muun.apollo.presentation.ui.utils.LinkBuilder
import io.muun.apollo.presentation.ui.utils.Uri
import org.bitcoinj.core.NetworkParameters

class ExternalOperation(
    operation: Operation,
    linkBuilder: LinkBuilder,
    currencyDisplayMode: CurrencyDisplayMode
) : UiOperation(operation, linkBuilder, currencyDisplayMode) {

    override fun getFormattedTitle(context: Context, shortName: Boolean): CharSequence =
        if (isCyclical) {
            context.getString(R.string.operation_sent_to_yourself)
        } else if (isIncoming) {
            if (isIncomingSwap && !lnUrlSender.isEmpty()) {
                context.getString(R.string.lnurl_withdraw_op_detail_title, lnUrlSender)
            } else {
                context.getString(R.string.external_incoming_operation)
            }
        } else {
            context.getString(R.string.external_outgoing_operation)
        }

    override fun getPictureUri(context: Context): String {
        val resId: Int = if (isSwap || isIncomingSwap) {
            R.drawable.lightning
        } else {
            R.drawable.btc
        }

        return Uri.getResourceUri(context, resId).toString()
    }
}