package com.comprartir.mobile.core.model

import androidx.annotation.StringRes
import com.comprartir.mobile.R

enum class Unit(val value: String, @StringRes val labelRes: Int) {
    KG("kg", R.string.unit_kg),
    G("g", R.string.unit_g),
    L("L", R.string.unit_l),
    ML("mL", R.string.unit_ml),
    UNITS("un", R.string.unit_units),
    PACKAGE("paq", R.string.unit_package),
    DOZEN("doc", R.string.unit_dozen),
    ;

    companion object {
        fun fromValue(value: String?): Unit? = entries.find { it.value.equals(value, ignoreCase = true) }
        fun getDefault(): Unit = UNITS
    }
}
