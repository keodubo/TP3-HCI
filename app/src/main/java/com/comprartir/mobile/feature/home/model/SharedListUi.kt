package com.comprartir.mobile.feature.home.model

import java.time.Instant

data class SharedListUi(
    val id: String,
    val name: String,
    val ownerId: String,
    val updatedAt: Instant,
    val avatarUrl: String?,
)
