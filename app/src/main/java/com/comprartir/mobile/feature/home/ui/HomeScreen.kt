package com.comprartir.mobile.feature.home.ui

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.ComprartirPillShape
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.brand
import com.comprartir.mobile.core.designsystem.brandTint
import com.comprartir.mobile.core.designsystem.darkNavy
import com.comprartir.mobile.core.designsystem.surfaceCard
import com.comprartir.mobile.core.designsystem.textMuted
import com.comprartir.mobile.core.designsystem.theme.LocalColorTokens
import com.comprartir.mobile.feature.home.model.ActivityUi
import com.comprartir.mobile.feature.home.model.HomeUiState
import com.comprartir.mobile.feature.home.model.ListStatusType
import com.comprartir.mobile.feature.home.model.RecentListUi
import com.comprartir.mobile.feature.home.model.SharedListUi
import com.comprartir.mobile.core.ui.rememberIsLandscape
import com.comprartir.mobile.core.ui.rememberIsTablet
import java.time.Instant

@Composable
fun HomeScreen(
    state: HomeUiState,
    onCreateList: () -> Unit,
    onViewAllLists: () -> Unit,
    onRecentListClick: (String) -> Unit,
    onSharedListClick: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass? = null,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val spacing = LocalSpacing.current
    val isLandscape = rememberIsLandscape()
    val isTablet = windowSizeClass?.let { rememberIsTablet(it) } ?: false
    val columns = when {
        isTablet -> 4
        isLandscape -> 3
        else -> 2
    }
    val layoutDirection = LocalLayoutDirection.current
    val horizontalPadding = when {
        isTablet -> spacing.xl
        isLandscape -> spacing.large
        else -> spacing.medium
    }
    val verticalPadding = if (isTablet) spacing.large else spacing.medium
    val horizontalSpacing = if (isTablet) spacing.large else spacing.medium
    val verticalSpacing = if (isTablet) spacing.large else spacing.medium

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.TopCenter,
    ) {
        val combinedPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection) + horizontalPadding,
            end = contentPadding.calculateEndPadding(layoutDirection) + horizontalPadding,
            top = contentPadding.calculateTopPadding() + verticalPadding,
            bottom = contentPadding.calculateBottomPadding() + spacing.xxl,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = combinedPadding,
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeHeroCard(
                    userName = state.userName,
                    onCreateList = onCreateList,
                    onViewAllLists = onViewAllLists,
                )
            }
            state.error?.let { message ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    HomeErrorBanner(
                        message = message,
                        onRetry = onRefresh,
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                RecentListsSection(
                    lists = state.recentLists,
                    onRecentListClick = onRecentListClick,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SharedListsSection(
                    lists = state.sharedLists,
                    onSharedListClick = onSharedListClick,
                )
            }
        }

        if (state.isLoading) {
            val loadingDescription = stringResource(id = R.string.cd_loading_home)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.semantics {
                        contentDescription = loadingDescription
                    },
                    color = MaterialTheme.colorScheme.brand,
                )
            }
        }
    }
}

@Composable
private fun HomeHeroCard(
    userName: String,
    onCreateList: () -> Unit,
    onViewAllLists: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp),
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.brand.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.large, vertical = spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                val logoRes = if (LocalColorTokens.current.isDark) {
                    R.drawable.logo_comprartir_nobg
                } else {
                    R.drawable.logo_comprartir
                }
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier.size(80.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val subtitle = if (userName.isBlank()) {
                        stringResource(id = R.string.home_welcome_subtitle)
                    } else {
                        stringResource(id = R.string.home_welcome_subtitle_with_name, userName)
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.textMuted,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                Button(
                    onClick = onCreateList,
                    shape = ComprartirPillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.brand),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(id = R.string.home_new_list),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.home_new_list))
                }
                FilledTonalButton(
                    onClick = onViewAllLists,
                    shape = ComprartirPillShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(id = R.string.home_view_all))
                }
            }
        }
    }
}

@Composable
private fun RecentListsSection(
    lists: List<RecentListUi>,
    onRecentListClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(text = stringResource(id = R.string.home_recent_lists))
        if (lists.isEmpty()) {
            HomePlaceholder(
                icon = Icons.Outlined.ContentPaste,
                message = stringResource(id = R.string.home_empty_lists),
            )
        } else {
            val fadeColor = MaterialTheme.colorScheme.background
            val fadeWidth = 48.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    items(lists, key = { it.id }) { item ->
                        RecentListCard(
                            item = item,
                            onClick = { onRecentListClick(item.id) },
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(fadeWidth)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(fadeColor, Color.Transparent),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(fadeWidth)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, fadeColor),
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
private fun SharedListsSection(
    lists: List<SharedListUi>,
    onSharedListClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionTitle(text = stringResource(id = R.string.home_shared_with_me))
        if (lists.isEmpty()) {
            HomePlaceholder(
                icon = Icons.Outlined.Share,
                message = stringResource(id = R.string.home_empty_shared),
            )
        } else {
            val rowHeight = 84.dp
            val spacing = 12.dp
            val rows = lists.size
            val totalHeight = (rowHeight * rows.toFloat()) +
                (spacing * (rows - 1).coerceAtLeast(0).toFloat())
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = totalHeight.coerceAtMost(600.dp)),
                verticalArrangement = Arrangement.spacedBy(spacing),
                userScrollEnabled = false,
            ) {
                items(lists, key = { it.id }) { item ->
                    SharedListRow(item = item, onClick = { onSharedListClick(item.id) })
                }
            }
        }
    }
}

@Composable
private fun ActivitySection(
    items: List<ActivityUi>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionTitle(text = stringResource(id = R.string.home_recent_activity))
        if (items.isEmpty()) {
            HomePlaceholder(
                icon = Icons.Outlined.History,
                message = stringResource(id = R.string.home_empty_activity),
            )
        } else {
            val rowHeight = 72.dp
            val spacing = 10.dp
            val rows = items.size
            val totalHeight = (rowHeight * rows.toFloat()) +
                (spacing * (rows - 1).coerceAtLeast(0).toFloat())
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = totalHeight.coerceAtMost(600.dp)),
                verticalArrangement = Arrangement.spacedBy(spacing),
                userScrollEnabled = false,
            ) {
                items(items, key = { it.id }) { item ->
                    ActivityRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun HomeDualColumnSection(
    sharedLists: List<SharedListUi>,
    activityItems: List<ActivityUi>,
    onSharedListClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 1200.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        userScrollEnabled = false,
    ) {
        item {
            SharedListsSection(
                lists = sharedLists,
                onSharedListClick = onSharedListClick,
            )
        }
        item {
            ActivitySection(items = activityItems)
        }
    }
}

@Composable
private fun RecentListCard(
    item: RecentListUi,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(160.dp),
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.brand.copy(alpha = 0.2f)),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.TopStart),
                )
                ItemCountPill(
                    completedCount = item.completedItemCount,
                    totalCount = item.itemCount,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomStart,
            ) {
                StatusChip(
                    statusType = item.statusType,
                )
            }
        }
    }
}

@Composable
private fun SharedListRow(
    item: SharedListUi,
    onClick: () -> Unit,
) {
    val ownerLabel = if (item.ownerId.isNotBlank()) {
        stringResource(id = R.string.home_shared_owner_placeholder, item.ownerId.takeLast(6))
    } else {
        stringResource(id = R.string.home_shared_owner_unknown)
    }
    val lastUpdatedText = stringResource(
        id = R.string.common_last_updated,
        DateUtils.getRelativeTimeSpanString(
            item.updatedAt.toEpochMilli(),
            Instant.now().toEpochMilli(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE,
        ).toString(),
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.brand.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.brandTint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = stringResource(id = R.string.home_shared_avatar_cd, ownerLabel),
                    tint = MaterialTheme.colorScheme.brand,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = lastUpdatedText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.textMuted,
                )
            }
            TextButton(onClick = onClick, shape = ComprartirPillShape) {
                Text(text = stringResource(id = R.string.home_open_shared))
            }
        }
    }
}

@Composable
private fun ActivityRow(item: ActivityUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.brand.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.brandTint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = activityIconFor(item.iconType),
                    contentDescription = stringResource(id = R.string.home_activity_icon_cd, item.description),
                    tint = MaterialTheme.colorScheme.brand,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.textMuted,
                )
            }
        }
    }
}

@Composable
private fun HomePlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
) {
    val cardShape = RoundedCornerShape(16.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, cardShape),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.brand.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.textMuted,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.textMuted,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun ItemCountPill(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = MaterialTheme.colorScheme.darkNavy,
        contentColor = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = "$completedCount/$totalCount",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = Color.White,
        )
    }
}

@Composable
private fun StatusChip(
    statusType: ListStatusType,
) {
    val tokens = LocalColorTokens.current
    val isDark = tokens.isDark
    val text = when (statusType) {
        ListStatusType.COMPLETE -> stringResource(id = R.string.list_status_complete)
        ListStatusType.IN_PROGRESS -> stringResource(id = R.string.list_status_in_progress)
        ListStatusType.PENDING -> stringResource(id = R.string.list_status_pending)
        ListStatusType.EMPTY -> stringResource(id = R.string.list_status_empty)
    }
    val (background, contentColor) = when (statusType) {
        ListStatusType.COMPLETE -> {
            val bg = if (isDark) {
                MaterialTheme.colorScheme.brand.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.brandTint
            }
            bg to MaterialTheme.colorScheme.brand
        }

        ListStatusType.IN_PROGRESS -> {
            val bg = if (isDark) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
            bg to MaterialTheme.colorScheme.onSecondaryContainer
        }

        ListStatusType.PENDING -> {
            val bg = if (isDark) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            bg to MaterialTheme.colorScheme.onSurface
        }

        ListStatusType.EMPTY -> {
            val bg = if (isDark) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            bg to MaterialTheme.colorScheme.textMuted
        }
    }

    Box(
        modifier = Modifier
            .background(
                color = background,
                shape = ComprartirPillShape,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}

@Composable
private fun HomeErrorBanner(
    message: String,
    onRetry: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(16.dp))
            TextButton(onClick = onRetry) {
                Text(text = stringResource(id = R.string.common_retry))
            }
        }
    }
}

@Composable
private fun activityIconFor(type: String) = when (type) {
    "check" -> Icons.Outlined.CheckCircle
    "share" -> Icons.Outlined.Share
    else -> Icons.Outlined.Assignment
}
