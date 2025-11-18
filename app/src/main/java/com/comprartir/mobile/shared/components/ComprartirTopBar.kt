package com.comprartir.mobile.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.theme.ColorTokens
import com.comprartir.mobile.core.ui.LocalAppBarTitle
import com.comprartir.mobile.core.navigation.AppDestination

@Composable
fun ComprartirTopBar(
    destinationRoute: String?,
    showBack: Boolean,
    onBack: () -> Unit,
    onProfileClick: () -> Unit = {},
) {
    val appBarTitleState = LocalAppBarTitle.current
    val customTitle = appBarTitleState.value?.takeIf { it.isNotBlank() }
    val title = remember(destinationRoute) {
        when {
            destinationRoute == AppDestination.Dashboard.route -> R.string.title_dashboard
            // Lists index: "lists/manage" or "lists/manage?..." -> "Shopping lists"
            destinationRoute?.startsWith("lists/manage") == true -> R.string.title_lists
            // List details: "lists/123" where 123 is the listId -> "List Details"
            destinationRoute?.matches(Regex("lists/\\d+")) == true -> R.string.title_list_details
            destinationRoute == AppDestination.Products.route -> R.string.title_products
            destinationRoute == AppDestination.Categorize.route -> R.string.title_categorize_products
            destinationRoute == AppDestination.Categories.route -> R.string.title_categories
            destinationRoute == AppDestination.Profile.route -> R.string.title_profile
            destinationRoute == AppDestination.Settings.route -> R.string.title_settings
            destinationRoute == AppDestination.Pantry.route -> R.string.title_pantry
            destinationRoute == AppDestination.SignIn.route -> R.string.title_sign_in
            destinationRoute == AppDestination.Register.route -> R.string.title_register
            destinationRoute == AppDestination.Verify.route -> R.string.title_verify
            destinationRoute == AppDestination.UpdatePassword.route -> R.string.title_update_password
            destinationRoute == AppDestination.ShareList.route -> R.string.title_share_list
            destinationRoute == AppDestination.AcquireProduct.route -> R.string.title_acquire_products
            else -> R.string.app_name
        }
    }

    val spacing = LocalSpacing.current
    val topBarShape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ColorTokens.NavSurface,
        shadowElevation = 12.dp,
        shape = topBarShape,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = spacing.large, end = 24.dp, top = spacing.medium, bottom = spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                if (showBack) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = stringResource(id = R.string.cd_back))
                    }
                }
                val fallbackTitle = stringResource(id = title)
                Text(
                    text = customTitle ?: fallbackTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 28.sp,
                    ),
                    modifier = Modifier.weight(1f),
                )
                // Profile button - only show if not already on profile screen
                if (destinationRoute != AppDestination.Profile.route) {
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = stringResource(R.string.title_profile),
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            Divider(
                thickness = 1.dp,
                color = ColorTokens.NavDivider,
            )
        }
    }
}
