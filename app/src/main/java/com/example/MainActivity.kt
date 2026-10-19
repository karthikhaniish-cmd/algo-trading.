package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BacktestingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MarketDataScreen
import com.example.ui.screens.PaperTradingScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.RiskManagementScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StrategiesScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel
import kotlinx.coroutines.launch

enum class AppScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.ShowChart),
    MARKET_DATA("Market Data", Icons.Default.Storage),
    STRATEGIES("Strategies", Icons.Default.QueryStats),
    BACKTESTING("Backtesting", Icons.Default.PlayArrow),
    PAPER_TRADING("Paper Trading", Icons.Default.AccountBalanceWallet),
    RISK_MANAGEMENT("Risk Controls", Icons.Default.Security),
    REPORTS("History & Reports", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AlgoTradingApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlgoTradingApp(viewModel: TradingViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Intercept Back button if on sub-screen
    if (currentScreen != AppScreen.DASHBOARD) {
        BackHandler {
            currentScreen = AppScreen.DASHBOARD
        }
    }

    // Show user notifications via Snackbar
    LaunchedEffect(uiState.userMessage) {
        val msg = uiState.userMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = NavySurface,
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .border(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ALGO TRADING",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Quantitative Analytics & Simulation",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AppScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) CyanAccent else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyanAccent else TextPrimary
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            currentScreen = screen
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = NavySurfaceVariant,
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .testTag("drawer_item_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = NavyBackground,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentScreen.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (uiState.selectedDataset != null) {
                                Text(
                                    text = "${uiState.selectedDataset?.symbol} • ${uiState.strategyConfig.strategyType.displayName}",
                                    fontSize = 10.sp,
                                    color = CyanAccent
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_toggle_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = CyanAccent
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NavySurface,
                        titleContentColor = TextPrimary
                    )
                )
            },
            bottomBar = {
                // Primary 4 quick destinations in Bottom Navigation Bar
                val bottomScreens = listOf(
                    AppScreen.DASHBOARD,
                    AppScreen.MARKET_DATA,
                    AppScreen.BACKTESTING,
                    AppScreen.PAPER_TRADING
                )
                NavigationBar(
                    containerColor = NavySurface,
                    contentColor = CyanAccent,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    bottomScreens.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 10.sp) },
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanAccent,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = CyanAccent,
                                unselectedTextColor = TextMuted,
                                indicatorColor = NavySurfaceVariant
                            ),
                            modifier = Modifier.testTag("bottom_nav_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        state = uiState,
                        onSelectDataset = { viewModel.selectDataset(it) },
                        onNavigateToBacktest = { currentScreen = AppScreen.BACKTESTING },
                        onNavigateToPaperTrading = { currentScreen = AppScreen.PAPER_TRADING }
                    )
                    AppScreen.MARKET_DATA -> MarketDataScreen(
                        state = uiState,
                        onSelectDataset = { viewModel.selectDataset(it) },
                        onDeleteDataset = { viewModel.deleteDataset(it) },
                        onImportCsv = { sym, name, desc, csv -> viewModel.importCsv(sym, name, desc, csv) },
                        onCreateSynthetic = { sym, name, desc, price, vol, drift, bars ->
                            viewModel.createSynthetic(sym, name, desc, price, vol, drift, bars)
                        }
                    )
                    AppScreen.STRATEGIES -> StrategiesScreen(
                        state = uiState,
                        onUpdateStrategy = { viewModel.updateStrategyConfig(it) }
                    )
                    AppScreen.BACKTESTING -> BacktestingScreen(
                        state = uiState,
                        onRunBacktest = { viewModel.runBacktest() },
                        onUpdateConfig = { viewModel.updateBacktestConfig(it) }
                    )
                    AppScreen.PAPER_TRADING -> PaperTradingScreen(
                        state = uiState,
                        onExecuteOrder = { sym, side, qty, price ->
                            viewModel.executeManualPaperOrder(sym, side, qty, price)
                        },
                        onStepSimulation = { viewModel.stepSimulationPaperTrade() }
                    )
                    AppScreen.RISK_MANAGEMENT -> RiskManagementScreen(
                        state = uiState,
                        onUpdateRiskConfig = { viewModel.updateRiskConfig(it) }
                    )
                    AppScreen.REPORTS -> ReportsScreen(
                        state = uiState
                    )
                    AppScreen.SETTINGS -> SettingsScreen(
                        state = uiState,
                        onResetDemoData = { viewModel.resetDemoData() }
                    )
                }
            }
        }
    }
}
