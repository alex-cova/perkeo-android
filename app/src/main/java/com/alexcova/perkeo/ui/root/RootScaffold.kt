package com.alexcova.perkeo.ui.root

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alexcova.perkeo.PerkeoApp
import com.alexcova.perkeo.navigation.AppDestination
import com.alexcova.perkeo.navigation.AppNavHost
import com.alexcova.perkeo.ui.theme.PerkeoAccentRed
import com.alexcova.perkeo.ui.theme.PerkeoBackgroundDark

@Composable
fun RootScaffold(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination
    val context = LocalContext.current
    val appGraph = remember(context) {
        (context.applicationContext as PerkeoApp).appGraph
    }

    // Observe saved seeds count for badge
    val savedSeeds by appGraph.seedRepository.observeSavedSeeds().collectAsState(initial = emptyList())
    val savedCount = savedSeeds.size

    val currentDestination = AppDestination.entries.firstOrNull { dest ->
        current?.hierarchy?.any { it.route == dest.route } == true
    } ?: AppDestination.Analyzer

    Scaffold(
        modifier = modifier,
        containerColor = PerkeoBackgroundDark,
        bottomBar = {
            PerkeoTabBar(
                currentDestination = currentDestination,
                savedCount = savedCount,
                onTabSelected = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            appGraph = appGraph,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun PerkeoTabBar(
    currentDestination: AppDestination,
    savedCount: Int,
    onTabSelected: (AppDestination) -> Unit,
) {
    val isOnAnalyzer = currentDestination == AppDestination.Analyzer
    val bgAlpha = if (isOnAnalyzer) 0.4f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PerkeoBackgroundDark.copy(alpha = bgAlpha))
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = Color.Black.copy(alpha = 0.4f), thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 15.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            AppDestination.entries.forEach { destination ->
                val isActive = destination == currentDestination
                val badge = if (destination == AppDestination.Saved && savedCount > 0) savedCount else null

                TabBarButton(
                    destination = destination,
                    isActive = isActive,
                    badge = badge,
                    onClick = { onTabSelected(destination) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TabBarButton(
    destination: AppDestination,
    isActive: Boolean,
    badge: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconSize by animateDpAsState(
        targetValue = if (isActive) 26.dp else 20.dp,
        animationSpec = spring(),
        label = "iconSize",
    )
    val iconColor by animateColorAsState(
        targetValue = if (isActive) Color.White else Color.Gray,
        animationSpec = spring(),
        label = "iconColor",
    )
    val topPadding by animateDpAsState(
        targetValue = if (isActive) 4.dp else 16.dp,
        animationSpec = spring(),
        label = "topPadding",
    )
    val labelColor by animateColorAsState(
        targetValue = if (isActive) PerkeoAccentRed else Color.White,
        animationSpec = spring(),
        label = "labelColor",
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(top = topPadding, bottom = 8.dp),
        ) {
            // Icon with red circle background when active
            Box(contentAlignment = Alignment.Center) {
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PerkeoAccentRed.copy(alpha = 0.4f)),
                    )
                }
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.label,
                    tint = iconColor,
                    modifier = Modifier.size(iconSize),
                )
                // Badge
                if (badge != null && badge > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(PerkeoAccentRed),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (badge > 9) "9+" else badge.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Text(
                text = destination.label,
                color = labelColor,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
            )
        }
    }
}

private val AppDestination.icon: ImageVector
    get() = when (this) {
        AppDestination.Analyzer -> Icons.AutoMirrored.Filled.ManageSearch
        AppDestination.Saved -> Icons.Filled.Bookmarks
        AppDestination.Finder -> Icons.Filled.TravelExplore
        AppDestination.Community -> Icons.Filled.Groups
    }
