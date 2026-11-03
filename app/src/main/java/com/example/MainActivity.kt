package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.BeatMakerScreen
import com.example.ui.screens.CollabScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.SunoAiScreen
import com.example.ui.screens.VocalBoothScreen
import com.example.ui.theme.CLRMusicTheme
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDarkBackground
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.viewmodel.StudioUiState
import com.example.ui.viewmodel.StudioViewModel

enum class NavigationScreen(val label: String, val icon: ImageVector) {
    STUDIO("Studio", Icons.Default.GraphicEq),
    SUNO_AI("Suno AI", Icons.Default.AutoAwesome),
    BEAT_MAKER("Beats", Icons.Default.GridOn),
    VOCAL_BOOTH("Vocal", Icons.Default.Mic),
    PLAYER("Player", Icons.Default.PlayCircle),
    COLLAB("Collab", Icons.Default.Group)
}

class MainActivity : ComponentActivity() {

    private val viewModel: StudioViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CLRMusicTheme {
                var currentScreen by remember { mutableStateOf(NavigationScreen.STUDIO) }
                val snackbarHostState = remember { SnackbarHostState() }
                val uiState by viewModel.uiState.collectAsState()
                val isPlaying by viewModel.audioEngine.isPlaying.collectAsState()

                // BackHandler
                if (currentScreen != NavigationScreen.STUDIO) {
                    BackHandler {
                        currentScreen = NavigationScreen.STUDIO
                    }
                }

                // Show Snackbar on State changes
                LaunchedEffect(uiState) {
                    when (val state = uiState) {
                        is StudioUiState.Success -> {
                            snackbarHostState.showSnackbar(state.message)
                            viewModel.dismissUiState()
                        }
                        is StudioUiState.Error -> {
                            snackbarHostState.showSnackbar("Error: ${state.message}")
                            viewModel.dismissUiState()
                        }
                        else -> {}
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // App logo badge
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NeonPink),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "CLR",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Column(modifier = Modifier.padding(start = 10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "CLR MUSIC",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White,
                                                letterSpacing = 1.5.sp
                                            )
                                            if (isPlaying) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(start = 6.dp)
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(NeonLime)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "AI DAW & Vocal Workstation",
                                            fontSize = 10.sp,
                                            color = NeonCyan
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = StudioDarkSurface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = StudioDarkSurface,
                            modifier = Modifier
                                .height(68.dp)
                                .testTag("main_navigation_bar")
                        ) {
                            NavigationScreen.entries.forEach { screen ->
                                val isSelected = currentScreen == screen
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.label,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NeonCyan,
                                        selectedTextColor = NeonCyan,
                                        unselectedIconColor = Color(0xFF7E8CA3),
                                        unselectedTextColor = Color(0xFF7E8CA3),
                                        indicatorColor = NeonCyan.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
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
                            NavigationScreen.STUDIO -> {
                                StudioScreen(
                                    viewModel = viewModel,
                                    onNavigateToVocalBooth = { currentScreen = NavigationScreen.VOCAL_BOOTH }
                                )
                            }
                            NavigationScreen.SUNO_AI -> {
                                SunoAiScreen(
                                    viewModel = viewModel,
                                    onOpenInStudio = { currentScreen = NavigationScreen.STUDIO },
                                    onNavigateToPlayer = { currentScreen = NavigationScreen.PLAYER }
                                )
                            }
                            NavigationScreen.BEAT_MAKER -> {
                                BeatMakerScreen(viewModel = viewModel)
                            }
                            NavigationScreen.VOCAL_BOOTH -> {
                                VocalBoothScreen(
                                    viewModel = viewModel,
                                    onNavigateToStudio = { currentScreen = NavigationScreen.STUDIO }
                                )
                            }
                            NavigationScreen.PLAYER -> {
                                PlayerScreen(
                                    viewModel = viewModel,
                                    onEditLyrics = { currentScreen = NavigationScreen.SUNO_AI }
                                )
                            }
                            NavigationScreen.COLLAB -> {
                                CollabScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
