package sg.gov.tech.molbagencyportalbackend.util

import com.google.common.collect.ImmutableMap
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class FeatureToggle(@Qualifier("featureToggleSettings") settings: Map<String, Boolean>) {
    private val toggles: ImmutableMap<String, Boolean> = ImmutableMap.copyOf(settings)

    companion object {
        const val REASSIGN_NOT_ENABLED = "Reassign feature is not enabled"
        const val RFA_NOT_ENABLED = "RFA feature is not enabled"
        const val WITHDRAWAL_NOT_ENABLED = "Withdrawal feature is not enabled"
    }

    private fun isFeatureEnabled(featureName: String) = toggles[featureName] ?: false

    fun isReassignEnabled() = isFeatureEnabled("reassign")
    fun isRFAEnabled() = isFeatureEnabled("rfa")
    fun isWithdrawalEnabled() = isFeatureEnabled("withdrawal")
}
