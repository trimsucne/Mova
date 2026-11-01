package co.edu.unal.dadm.mova.domain.model

data class UpliftScore(val value: Int) {
    init {
        require(value in 0..100)
    }
}
