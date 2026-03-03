package com.paulosd.neblina.audio

import com.paulosd.neblina.R
import com.paulosd.neblina.model.Sound
import com.paulosd.neblina.model.SoundCategory

object SoundRepository {

    val sounds = listOf(
        Sound(
            id = 1,
            name = "Chuva Leve",
            category = SoundCategory.CHUVA,
            rawResId = R.raw.chuva_leve_1
        ),
        Sound(
            id = 2,
            name = "Ruído Branco Subaquático",
            category = SoundCategory.RUIDO_BRANCO,
            rawResId = R.raw.underwater_white_noise
        )
    )

    fun categories(): List<SoundCategory> =
        sounds.map { it.category }.distinct()

    fun soundsByCategory(category: SoundCategory): List<Sound> =
        sounds.filter { it.category == category }
}