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
            name = "Chuva Calma",
            category = SoundCategory.CHUVA,
            rawResId = R.raw.chuva_calma_loop
        ),
        Sound(
            id = 3,
            name = "Chuva Moderada",
            category = SoundCategory.CHUVA,
            rawResId = R.raw.chuva_media
        ),
        Sound(
            id = 4,
            name = "Chuva Gelada",
            category = SoundCategory.CHUVA,
            rawResId = R.raw.chuva_gelo
        ),
        Sound(
            id = 5,
            name = "Tempestade",
            category = SoundCategory.CHUVA,
            rawResId = R.raw.thunderstorm
        ),
        Sound(
            id = 6,
            name = "Acampamento Chuvoso",
            category = SoundCategory.CHUVA,
            rawResId = R.raw.acampamento_chuva
        ),
        Sound(
            id = 7,
            name = "Chuva com vento",
            category = SoundCategory.VENTO,
            rawResId = R.raw.tempestade_vento
        ),
        Sound(
            id = 8,
            name = "Vento Soprando",
            category = SoundCategory.VENTO,
            rawResId = R.raw.vento_soprando
        ),
        Sound(
            id = 9,
            name = "Deserto",
            category = SoundCategory.VENTO,
            rawResId = R.raw.deserto_vento
        ),
        Sound(
            id = 10,
            name = "Vento Nas Árvores",
            category = SoundCategory.VENTO,
            rawResId = R.raw.vento_arvores
        ),
        Sound(
            id = 11,
            name = "Ruído Branco",
            category = SoundCategory.RUIDO_BRANCO,
            rawResId = R.raw.ruido_branco
        ),
        Sound(
            id = 12,
            name = "Vento Baixo",
            category = SoundCategory.RUIDO_BRANCO,
            rawResId = R.raw.vento_baixo
        ),
        Sound(
            id = 13,
            name = "TV Estática",
            category = SoundCategory.RUIDO_BRANCO,
            rawResId = R.raw.tv_estatica
        ),
        Sound(
            id = 14,
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