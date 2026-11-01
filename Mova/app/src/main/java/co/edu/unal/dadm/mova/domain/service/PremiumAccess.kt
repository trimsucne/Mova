package co.edu.unal.dadm.mova.domain.service

import co.edu.unal.dadm.mova.domain.model.PremiumFeature
import co.edu.unal.dadm.mova.domain.model.PremiumTier
import co.edu.unal.dadm.mova.domain.model.allows

class PremiumAccess {
    fun canUse(tier: PremiumTier, feature: PremiumFeature): Boolean =
        tier.allows(feature)
}
