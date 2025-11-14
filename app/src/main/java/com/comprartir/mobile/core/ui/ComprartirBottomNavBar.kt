package com.comprartir.mobile.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.List
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.ComprartirPillShape
import com.comprartir.mobile.core.designsystem.ComprartirTheme
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.theme.ColorTokens
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.isDestinationSelected

data class BottomNavItem(
    val destination: AppDestination,
    val labelRes: Int,
    val icon: ImageVector,
)

@Composable
fun ComprartirBottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (AppDestination) -> Unit,
) {
    val spacing = LocalSpacing.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = ColorTokens.NavSurface,
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Divider(
                thickness = 1.dp,
                color = ColorTokens.NavDivider,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = spacing.large,
                        end = spacing.large,
                        top = spacing.medium,
                        bottom = spacing.medium + spacing.xs,
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    val label = stringResource(id = item.labelRes)
                    val selected = isDestinationSelected(currentRoute, item.destination)
                    BottomNavPill(
                        label = label,
                        icon = item.icon,
                        selected = selected,
                        onClick = { onNavigate(item.destination) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) ColorTokens.NavActiveBackground else ColorTokens.NavInactiveBackground
    val contentColor = if (selected) ColorTokens.NavActiveContent else ColorTokens.NavInactiveContent
    val border = if (selected) null else BorderStroke(1.dp, ColorTokens.NavInactiveBorder)
    val semanticsDescription = if (selected) {
        stringResource(id = R.string.nav_item_selected, label)
    } else {
        stringResource(id = R.string.nav_item_not_selected, label)
    }
    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .size(48.dp)
            .semantics {
                role = Role.Button
                contentDescription = semanticsDescription
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(999.dp),
        color = background,
        contentColor = contentColor,
        border = border,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ComprartirBottomNavBarPreview() {
    ComprartirTheme {
        ComprartirBottomNavBar(
            items = listOf(
                BottomNavItem(AppDestination.Dashboard, R.string.title_dashboard, Icons.Rounded.Home),
                BottomNavItem(AppDestination.Lists, R.string.title_lists, Icons.Rounded.List),
                BottomNavItem(AppDestination.Pantry, R.string.title_pantry, Icons.Rounded.Inventory2),
                BottomNavItem(AppDestination.OptionalHistory, R.string.title_history, Icons.Rounded.History),
            ),
            currentRoute = AppDestination.Lists.route,
            onNavigate = {},
        )
    }
}

@Preview
@Composable
private fun BottomNavPillSelectedPreview() {
    ComprartirTheme {
        BottomNavPill(
            label = "Inicio",
            icon = Icons.Rounded.Home,
            selected = true,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun BottomNavPillUnselectedPreview() {
    ComprartirTheme {
        BottomNavPill(
            label = "Listas",
            icon = Icons.Rounded.List,
            selected = false,
            onClick = {},
        )
    }
}
