package com.alexcova.perkeo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alexcova.perkeo.data.AppGraph
import com.alexcova.perkeo.features.analyzer.AnalyzerScreen
import com.alexcova.perkeo.features.analyzer.AnalyzerViewModel
import com.alexcova.perkeo.features.community.CommunityScreen
import com.alexcova.perkeo.features.finder.FinderScreen
import com.alexcova.perkeo.features.finder.FinderViewModel
import com.alexcova.perkeo.features.saved.SavedSeedsScreen
import com.alexcova.perkeo.features.saved.SavedSeedsViewModel
import androidx.compose.runtime.remember

@Composable
fun AppNavHost(
    navController: NavHostController,
    appGraph: AppGraph,
    modifier: Modifier = Modifier,
) {
    val analyzerViewModel = remember { AnalyzerViewModel(appGraph.seedRepository) }
    val savedSeedsViewModel = remember { SavedSeedsViewModel(appGraph.seedRepository) }
    val finderViewModel = remember { FinderViewModel(appGraph.finderCacheRepository) }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Analyzer.route,
        modifier = modifier,
    ) {
        composable(AppDestination.Analyzer.route) {
            AnalyzerScreen(viewModel = analyzerViewModel)
        }
        composable(AppDestination.Saved.route) {
            SavedSeedsScreen(
                viewModel = savedSeedsViewModel,
                analyzerViewModel = analyzerViewModel,
                onNavigateToSeed = { seed ->
                    analyzerViewModel.changeSeed(seed)
                    navController.navigate(AppDestination.Analyzer.route) {
                        launchSingleTop = true
                        restoreState = false
                    }
                },
            )
        }
        composable(AppDestination.Finder.route) {
            FinderScreen(
                viewModel = finderViewModel,
                analyzerViewModel = analyzerViewModel,
                onNavigateToSeed = { seed ->
                    analyzerViewModel.changeSeed(seed)
                    navController.navigate(AppDestination.Analyzer.route) {
                        launchSingleTop = true
                        restoreState = false
                    }
                },
            )
        }
        composable(AppDestination.Community.route) {
            CommunityScreen(
                analyzerViewModel = analyzerViewModel,
                onNavigateToSeed = { seed ->
                    analyzerViewModel.changeSeed(seed)
                    navController.navigate(AppDestination.Analyzer.route) {
                        launchSingleTop = true
                        restoreState = false
                    }
                },
            )
        }
    }
}
