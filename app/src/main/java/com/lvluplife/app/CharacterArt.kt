package com.lvluplife.app

import androidx.annotation.DrawableRes

object CharacterArt {
    @DrawableRes fun forMainScreen(name: String): Int? = when (name) {
        "Novato" -> R.drawable.novato_personaje
        "Guerrero" -> R.drawable.guerrero_personaje
        "Explorador" -> R.drawable.explorador_personaje
        "Guardián" -> R.drawable.guardian_personaje
        "Sabio" -> R.drawable.sabio_personaje
        "Monje" -> R.drawable.monje_personaje
        "Místico" -> R.drawable.mistico_personaje
        "Maestro de Armas" -> R.drawable.maestro_de_armas_personaje
        "Paladín" -> R.drawable.paladin_personaje
        "Guerrero Arcano" -> R.drawable.guerrero_arcano_personaje
        "Luchador Monje" -> R.drawable.luchador_monje_personaje
        "Guerrero Místico" -> R.drawable.guerrero_mistico_personaje
        "Ágil Guardián" -> R.drawable.agil_guardian_personaje
        "Artífice" -> R.drawable.artifice_personaje
        "Ninja" -> R.drawable.ninja_personaje
        "Explorador de Sueños" -> R.drawable.explorador_de_suenos_personaje
        "Guardián de la Sabiduría" -> R.drawable.guardian_de_la_sabiduria_personaje
        "Monje Guardián" -> R.drawable.monje_guardian_personaje
        "Druida" -> R.drawable.druida_personaje
        "Ilusionista" -> R.drawable.ilusionista_personaje
        "Oráculo" -> R.drawable.oraculo_personaje
        "Monje Espiritual" -> R.drawable.monje_espiritual_personaje
        "Aventurero Versátil" -> R.drawable.aventurero_versatil_personaje
        "Maestro del Equilibrio" -> R.drawable.maestro_del_equilibrio_personaje
        "Héroe Ascendente" -> R.drawable.heroe_ascendente_personaje
        "Héroe Integral" -> R.drawable.heroe_integral_personaje
        else -> null
    }

    @DrawableRes fun forClass(name: String): Int? = when (name) {
        "Novato" -> R.drawable.novato
        "Guerrero" -> R.drawable.guerrero
        "Explorador" -> R.drawable.explorador
        "Guardián" -> R.drawable.guardian
        "Sabio" -> R.drawable.sabio
        "Monje" -> R.drawable.monje
        "Místico" -> R.drawable.mistico
        "Maestro de Armas" -> R.drawable.maestro_de_armas
        "Paladín" -> R.drawable.paladin
        "Guerrero Arcano" -> R.drawable.guerrero_arcano
        "Luchador Monje" -> R.drawable.luchador_monje
        "Guerrero Místico" -> R.drawable.guerrero_mistico
        "Ágil Guardián" -> R.drawable.agil_guardian
        "Artífice" -> R.drawable.artifice
        "Ninja" -> R.drawable.ninja
        "Explorador de Sueños" -> R.drawable.explorador_de_suenos
        "Guardián de la Sabiduría" -> R.drawable.guardian_de_la_sabiduria
        "Monje Guardián" -> R.drawable.monje_guardian
        "Druida" -> R.drawable.druida
        "Ilusionista" -> R.drawable.ilusionista
        "Oráculo" -> R.drawable.oraculo
        "Monje Espiritual" -> R.drawable.monje_espiritual
        "Aventurero Versátil" -> R.drawable.aventurero_versatil
        "Maestro del Equilibrio" -> R.drawable.maestro_del_equilibrio
        "Héroe Ascendente" -> R.drawable.heroe_ascendente
        "Héroe Integral" -> R.drawable.heroe_integral
        else -> null
    }
}
