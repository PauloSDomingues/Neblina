package com.paulosd.neblina.model

enum class SoundCategory(val displayName: String) {
    NATUREZA("Natureza"),
    CHUVA("Chuva"),
    VENTO("Sopro de Vento"),
    RUIDO_BRANCO("Ruído Branco")
}

data class Sound(
    val id: Int,
    val name: String,
    val category: SoundCategory,
    val rawResId: Int
)