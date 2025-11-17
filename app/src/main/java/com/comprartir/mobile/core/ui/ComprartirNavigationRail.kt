package com.comprartir.mobile.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
    modifier: Modifier = Modifier.width(92.dp),
) {
    val spacing = LocalSpacing.current
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        color = ColorTokens.NavSurface,
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    top = spacing.large,
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
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val background = if (selected) ColorTokens.NavActiveBackground else ColorTokens.NavInactiveBackground
    val contentColor = if (selected) ColorTokens.NavActiveContent else ColorTokens.NavInactiveContent
    val border = if (selected) null else BorderStroke(1.dp, ColorTokens.NavInactiveBorder)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .semantics { role = Role.Button }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(24.dp),
        color = background,
        contentColor = contentColor,
        border = border,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = spacing.small,
                    vertical = spacing.medium,
                ),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
            )
            Text(
                text = label,
                color = contentColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}
