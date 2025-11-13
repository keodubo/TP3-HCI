package com.comprartir.mobile.feature.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowForward
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.ComprartirPillShape
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.brand
import com.comprartir.mobile.core.designsystem.brandTint
import com.comprartir.mobile.core.designsystem.surfaceCard
import com.comprartir.mobile.core.designsystem.textMuted
import com.comprartir.mobile.feature.home.model.ActivityUi
import com.comprartir.mobile.feature.home.model.HomeUiState
import com.comprartir.mobile.feature.home.model.RecentListUi
import com.comprartir.mobile.feature.home.model.SharedListUi

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
    val useTwoColumnLayout = windowSizeClass?.widthSizeClass?.let { it >= WindowWidthSizeClass.Medium } ?: false
    val containerMaxWidth = if (useTwoColumnLayout) 900.dp else Dp.Unspecified
    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                val combinedPadding = PaddingValues(
                    start = innerPadding.calculateStartPadding(layoutDirection) +
                        contentPadding.calculateStartPadding(layoutDirection) + 20.dp,
                    end = innerPadding.calculateEndPadding(layoutDirection) +
                        contentPadding.calculateEndPadding(layoutDirection) + 20.dp,
                    top = innerPadding.calculateTopPadding() +
                        contentPadding.calculateTopPadding() + 24.dp,
                    bottom = innerPadding.calculateBottomPadding() +
                        contentPadding.calculateBottomPadding() + 24.dp,
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = containerMaxWidth),
                    contentPadding = combinedPadding,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    item {
                        HomeHeroCard(
                            userName = state.userName,
                            onCreateList = onCreateList,
                            onViewAllLists = onViewAllLists,
                        )
                    }
                    state.error?.let { message ->
                        item {
                            HomeErrorBanner(
                                message = message,
                                onRetry = onRefresh,
                            )
                        }
                    }
                    item {
                        RecentListsSection(
                            lists = state.recentLists,
                            onRecentListClick = onRecentListClick,
                        )
                    }
                    if (useTwoColumnLayout) {
                        item {
                            HomeDualColumnSection(
                                sharedLists = state.sharedLists,
                                activityItems = state.recentActivity,
                                onSharedListClick = onSharedListClick,
                            )
                        }
                    } else {
                        item {
                            SharedListsSection(
                                lists = state.sharedLists,
                                onSharedListClick = onSharedListClick,
                            )
                        }
                        item {
                            ActivitySection(
                                items = state.recentActivity,
                            )
                        }
                    }
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
                Image(
                    painter = painterResource(id = R.drawable.logo_comprartir),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier.size(80.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.home_welcome_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    val subtitle = if (userName.isBlank()) {
                        stringResource(id = R.string.home_welcome_subtitle)
                    } else {
                        stringResource(id = R.string.home_welcome_subtitle_with_name, userName)
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(lists, key = { it.id }) { item ->
                    RecentListCard(
                        item = item,
                        onClick = { onRecentListClick(item.id) },
                    )
                }
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
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.textMuted,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.home_recent_list_items, item.itemCount),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    StatusChip(text = item.status)
                }
                Icon(
                    imageVector = Icons.Outlined.ArrowForward,
                    contentDescription = stringResource(id = R.string.home_recent_list_cd, item.name),
                    tint = MaterialTheme.colorScheme.brand,
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.brand.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                    contentDescription = stringResource(id = R.string.home_shared_avatar_cd, item.ownerName),
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
                    text = item.lastUpdated,
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
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.brand.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.brand.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
private fun StatusChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.brandTint,
                shape = ComprartirPillShape,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.brand,
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
