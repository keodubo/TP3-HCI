package com.comprartir.mobile.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.theme.ColorTokens
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.isDestinationSelected

@Composable
fun ComprartirNavigationRail(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier.width(112.dp),
    isLandscapePhone: Boolean = false,
) {
    val spacing = LocalSpacing.current
    val scrollState = rememberScrollState()
    // En landscape (tanto phone como tablet), no mostrar texto
    val showLabels = false
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        color = ColorTokens.NavSurface,
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(
                    top = spacing.xxl,
                    bottom = spacing.large,
                    start = spacing.small,
                    end = spacing.small,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.medium, Alignment.Top),
        ) {
            items.forEach { item ->
                val label = stringResource(id = item.labelRes)
                val selected = isDestinationSelected(currentRoute, item.destination)
                NavigationRailItem(
                    label = label,
                    icon = item.icon,
                    selected = selected,
                    showLabel = showLabels,
                    onClick = { onNavigate(item.destination) },
                )
            }
        }
    }
}

@Composable
private fun NavigationRailItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    showLabel: Boolean = true,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val background = if (selected) ColorTokens.NavActiveBackground else ColorTokens.NavInactiveBackground
    val contentColor = if (selected) ColorTokens.NavActiveContent else ColorTokens.NavInactiveContent
    val border = if (selected) null else BorderStroke(1.dp, ColorTokens.NavInactiveBorder)
    Surface(
        modifier = Modifier
            .then(
                if (showLabel) {
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                } else {
                    Modifier.size(48.dp)
                }
            )
            .semantics { role = Role.Button }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = if (showLabel) RoundedCornerShape(24.dp) else CircleShape,
        color = background,
        contentColor = contentColor,
        border = border,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (showLabel) {
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = spacing.small,
                                vertical = spacing.medium,
                            )
                    } else {
                        Modifier.padding(0.dp)
                    }
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
            )
            if (showLabel) {
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = label,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
