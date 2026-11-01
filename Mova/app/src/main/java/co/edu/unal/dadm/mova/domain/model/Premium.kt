package co.edu.unal.dadm.mova.domain.model

enum class PremiumTier {
    BASIC,
    PREMIUM
}

enum class PremiumFeature {
    ADAPTIVE_RECOMMENDATIONS,
    DETAILED_STATISTICS,
    AI_ASSISTANT,
    AD_FREE
}

fun PremiumTier.allows(feature: PremiumFeature): Boolean =
    this == PremiumTier.PREMIUM
