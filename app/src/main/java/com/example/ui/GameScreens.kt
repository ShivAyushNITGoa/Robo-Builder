package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import com.example.engine.three_d.Point3D
import com.example.engine.three_d.VectorMath
import com.example.engine.three_d.CuboidRenderer.draw3DCuboid
import com.example.ui.components.TelemetryDashboard
import com.example.ui.components.CockpitControls
import com.example.ui.components.ThreeRobotViewer
import com.example.ui.components.ThreeGameplayViewer
import com.example.ui.components.SpaceStarfieldBackground
import com.example.ui.components.ParticleSystem.drawSimParticles
import com.example.ui.components.PartSpecifications.getPartSpecDescription
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.SpaceshipTasksScreen
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameAppContent(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val levelProgress by viewModel.levelProgressList.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val achievements by viewModel.achievementsList.collectAsState()

    val currentScreen = viewModel.currentScreen
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberObsidian,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Robo Icon",
                            tint = CyberBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "ROBO BUILDER",
                            color = CyberWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                actions = {
                    // Coins Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberSteel)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Coins",
                            tint = CyberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${userProfile?.coins ?: 0}",
                            color = CyberGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    if (currentScreen != Screen.Menu) {
                        IconButton(
                            onClick = { viewModel.onScreenChanged(Screen.Menu) },
                            modifier = Modifier.testTag("menu_nav_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home Menu",
                                tint = CyberWhite
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSteel,
                    titleContentColor = CyberWhite
                ),
                windowInsets = WindowInsets.safeDrawing
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Among Us inspired scrolling space starfield
            SpaceStarfieldBackground()

            when (currentScreen) {
                Screen.Menu -> MenuScreen(viewModel, userProfile)
                Screen.LevelSelect -> LevelSelectScreen(viewModel, levelProgress)
                Screen.Game -> GamePlayScreen(viewModel, userProfile)
                Screen.Customization -> CustomizationScreen(viewModel, userProfile)
                Screen.Achievements -> AchievementsScreen(viewModel, achievements)
                Screen.DailyChallenge -> DailyChallengeScreen(viewModel, userProfile)
                Screen.CustomLevelSelect -> CustomLevelSelectScreen(viewModel)
                Screen.CustomLevelPlay -> CustomLevelPlayScreen(viewModel, userProfile)
                Screen.CustomLevelEditor -> CustomLevelEditorScreen(viewModel)
                Screen.SpaceshipTasks -> SpaceshipTasksScreen(viewModel)
            }
        }
    }
}

@Composable
fun MenuScreen(
    viewModel: GameViewModel,
    profile: UserProfile?
) {
    val coroutineScope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Splash Graphic
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, CyberBlue, RoundedCornerShape(16.dp))
                    .background(CyberSteel)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.robo_builder_banner)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Robo Cover Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Overlay text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            text = "BUILD. WATCH. SOLVE.",
                            color = CyberLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Assemble modular robot technologies to execute extreme puzzles automatically.",
                            color = CyberWhite,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Daily Harvest Alert
        item {
            Card(
                onClick = { viewModel.claimDailyBonus() },
                colors = CardDefaults.cardColors(containerColor = CyberSteel),
                border = BorderStroke(1.dp, CyberLime.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(CyberLime.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Daily",
                            tint = CyberLime
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Oil Harvest",
                            color = CyberWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Claim daily credit of +75 gold coins!",
                            color = CyberGray,
                            fontSize = 13.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.claimDailyBonus() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberLime),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("CLAIM", color = CyberObsidian, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Robot customizer preview card
                Card(
                    onClick = { viewModel.onScreenChanged(Screen.Customization) },
                    colors = CardDefaults.cardColors(containerColor = CyberSteel),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Garage",
                            tint = CyberOrange,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text("Robot Garage", color = CyberWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Paints, Hats, Eyes", color = CyberGray, fontSize = 11.sp)
                        }
                    }
                }

                // Achievements Card
                Card(
                    onClick = { viewModel.onScreenChanged(Screen.Achievements) },
                    colors = CardDefaults.cardColors(containerColor = CyberSteel),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Accolades",
                            tint = CyberGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text("Accolades", color = CyberWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("6 Badges Unlocked", color = CyberGray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Action Buttons for Play
        item {
            Button(
                onClick = { viewModel.onScreenChanged(Screen.LevelSelect) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("play_campaign_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Campaign Play",
                        tint = CyberObsidian,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "LAUNCH CAMPAIGN",
                        color = CyberObsidian,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.onScreenChanged(Screen.CustomLevelSelect) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberLime),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("play_custom_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = "Custom Level Maker",
                        tint = CyberObsidian,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "CUSTOM PUZZLES",
                        color = CyberObsidian,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.onScreenChanged(Screen.SpaceshipTasks) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)), // Among Us Red!
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("play_spaceship_tasks_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Report,
                        contentDescription = "Spaceship Tasks",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "SPACESHIP TASKS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFEA00)) // Glowing yellow badge
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NEW",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { viewModel.onScreenChanged(Screen.DailyChallenge) },
                border = BorderStroke(1.5.dp, CyberBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Daily Challenge",
                        tint = CyberBlue
                    )
                    Text(
                        text = "DAILY SANDBOX CODE",
                        color = CyberBlue,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // RESET PROGRESS
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "RESET PROGRESS DATABASE",
                color = CyberRed.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .clickable { showResetDialog = true }
                    .padding(8.dp)
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Purge Database?", color = CyberWhite) },
            text = { Text("This will delete all completed level records, stars, custom items, and coins. Action is irreversible.", color = CyberGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllProgress()
                        showResetDialog = false
                    }
                ) {
                    Text("PURGE", color = CyberRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL", color = CyberWhite)
                }
            },
            containerColor = CyberSteel
        )
    }
}

@Composable
fun LevelSelectScreen(
    viewModel: GameViewModel,
    progressList: List<LevelProgress>
) {
    var selectedWorldId by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Worlds selector row
        Text(
            text = "CHOOSE SECTOR",
            color = CyberGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LevelDefinitions.worlds.forEachIndexed { idx, (worldName, colorHex) ->
                val worldId = idx + 1
                val isSelected = selectedWorldId == worldId
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyberBlue else CyberSteel)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else CyberIron,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedWorldId = worldId }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "W$worldId: $worldName",
                        color = if (isSelected) CyberObsidian else CyberWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Divider(color = CyberIron)

        // Levels inside world
        Text(
            text = "SECTOR PUZZLES",
            color = CyberGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )

        val worldLevels = LevelDefinitions.levels.filter { it.worldId == selectedWorldId }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(worldLevels) { level ->
                val progress = progressList.find { it.levelId == level.id }
                val completed = progress?.completed ?: false
                val stars = progress?.stars ?: 0

                Card(
                    onClick = { viewModel.selectLevel(level.id) },
                    colors = CardDefaults.cardColors(containerColor = CyberSteel),
                    border = BorderStroke(1.dp, if (completed) CyberLime.copy(alpha = 0.5f) else CyberIron),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("level_card_${level.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Level number bubble
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (completed) CyberLime.copy(alpha = 0.15f) else CyberIron),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${level.levelNumber}",
                                color = if (completed) CyberLime else CyberWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = level.title,
                                    color = CyberWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (completed) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Cleared",
                                        tint = CyberLime,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = level.description,
                                color = CyberGray,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Stars tally
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            for (s in 1..3) {
                                Icon(
                                    imageVector = if (s <= stars) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star",
                                    tint = if (s <= stars) CyberGold else CyberIron,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    profile: UserProfile?
) {
    val level = viewModel.selectedLevel ?: return
    val progressList by viewModel.levelProgressList.collectAsState()
    val levelProgress = progressList.find { it.levelId == level.id }

    // Active Slot highlighters
    var activeSlot by remember { mutableStateOf<SlotType?>(null) }
    var playIn3D by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header info card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSteel),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WORLD ${level.worldId} • LEVEL ${level.levelNumber}",
                        color = CyberBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberIron)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = level.missionType.uppercase(),
                            color = CyberWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = level.title,
                    color = CyberWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = level.description,
                    color = CyberGray,
                    fontSize = 13.sp
                )
            }
        }

        // Simulator Mode Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SIMULATOR STREAM",
                color = CyberBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberSteel)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(false to "2D Hologram", true to "Immersive 3D").forEach { (is3D, label) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (playIn3D == is3D) CyberBlue else Color.Transparent)
                            .clickable { playIn3D = is3D }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (playIn3D == is3D) CyberWhite else CyberGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live Simulation Card / WebGL Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, if (playIn3D) CyberBlue else CyberIron, RoundedCornerShape(12.dp))
                .background(Color(0xFF070A13))
        ) {
            if (playIn3D) {
                ThreeGameplayViewer(
                    robotX = 0f,
                    robotY = if (viewModel.selectedLegs == "Hover Engine" || viewModel.selectedLegs == "Jetpack") 10f else 0f,
                    robotZ = (1f - viewModel.simulationProgress) * 400f - 200f,
                    speed = if (viewModel.simulationState == SimulationState.Running) 1.5f else 0f,
                    paintColor = profile?.selectedPaint ?: "#3D5AFE",
                    eyesType = profile?.selectedEyes ?: "digital",
                    hatType = profile?.selectedHat ?: "none",
                    legs = viewModel.selectedLegs,
                    leftArm = viewModel.selectedLeftArm,
                    rightArm = viewModel.selectedRightArm,
                    utility = viewModel.selectedUtility,
                    hazardType = level.hazardType,
                    progress = viewModel.simulationProgress,
                    simulationState = viewModel.simulationState,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Background Canvas drawing
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                viewModel.modelRotationAngleY += dragAmount.x * 0.015f
                                viewModel.modelRotationAngleX -= dragAmount.y * 0.015f
                            }
                        }
                ) {
                    drawSimulationWorldBackground(level.worldName)
                    
                    // Draw Level Obstacles
                    drawWorldObstacles(level.hazardType, viewModel.simulationProgress, viewModel.simulationState)
                    
                    // Draw custom robot configuration
                    drawRobotChassis(
                        paintColor = profile?.selectedPaint ?: "#3D5AFE",
                        eyesType = profile?.selectedEyes ?: "digital",
                        hatType = profile?.selectedHat ?: "none",
                        legs = viewModel.selectedLegs,
                        leftArm = viewModel.selectedLeftArm,
                        rightArm = viewModel.selectedRightArm,
                        utility = viewModel.selectedUtility,
                        progress = viewModel.simulationProgress,
                        state = viewModel.simulationState,
                        rotX = viewModel.modelRotationAngleX,
                        rotY = viewModel.modelRotationAngleY,
                        cameraShake = viewModel.cameraShakeAmount,
                        headStyle = viewModel.headStyle,
                        torsoStyle = viewModel.torsoStyle
                    )
                    
                    // Draw custom spark/exhaust particles
                    drawSimParticles(viewModel.particles)
                }
            }

            // Top Status Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.simulationLog,
                    color = when (viewModel.simulationState) {
                        is SimulationState.Success -> CyberLime
                        is SimulationState.Failure -> CyberRed
                        is SimulationState.Running -> CyberBlue
                        else -> CyberWhite
                    },
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (viewModel.simulationState == SimulationState.Running) {
                    Text(
                        text = "${(viewModel.simulationProgress * 100).toInt()}%",
                        color = CyberBlue,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        var diagnosticsExpanded by remember { mutableStateOf(false) }
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1424)),
            border = BorderStroke(1.dp, CyberBlue.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { diagnosticsExpanded = !diagnosticsExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeveloperBoard,
                            contentDescription = "Diag",
                            tint = CyberBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE HARDWARE CORE & COCKPIT",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = if (diagnosticsExpanded) "[ CLOSE ]" else "[ OPEN TELEMETRY & COCKPIT ]",
                        color = CyberBlue,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (diagnosticsExpanded) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TelemetryDashboard(
                        motorRpm = viewModel.liveMotorRpm,
                        coreHeat = viewModel.liveCoreHeat,
                        batteryCharge = viewModel.liveBatteryCharge,
                        manualOverride = viewModel.manualOverrideMode
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    var thrusterPower by remember { mutableStateOf(50f) }
                    var shieldFreq by remember { mutableStateOf(4.5f) }
                    var laserInt by remember { mutableStateOf(100f) }
                    CockpitControls(
                        thrusterPower = thrusterPower,
                        onThrusterChange = { 
                            thrusterPower = it
                            viewModel.reactorPowerLevel = it / 100f
                            viewModel.liveMotorRpm = viewModel.reactorPowerLevel * 8000f
                        },
                        shieldFrequency = shieldFreq,
                        onShieldChange = { shieldFreq = it },
                        laserIntensity = laserInt,
                        onLaserChange = { laserInt = it },
                        safetyBreaker = viewModel.manualOverrideMode,
                        onBreakerToggle = { 
                            viewModel.toggleManualOverride()
                        }
                    )
                }
            }
        }

        // Configuration deck header
        Text(
            text = "MODULE SOCKET ASSEMBLY",
            color = CyberGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )

        // Chassis Attachment Slots Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SlotCard(
                name = "LEGS",
                selectedPart = viewModel.selectedLegs,
                icon = Icons.Default.DirectionsWalk,
                color = CyberBlue,
                isSelected = activeSlot == SlotType.Leg,
                modifier = Modifier.weight(1f).testTag("legs_slot")
            ) { activeSlot = SlotType.Leg }

            SlotCard(
                name = "L. ARM",
                selectedPart = viewModel.selectedLeftArm,
                icon = Icons.Default.Hardware,
                color = CyberOrange,
                isSelected = activeSlot == SlotType.LeftArm,
                modifier = Modifier.weight(1f).testTag("left_arm_slot")
            ) { activeSlot = SlotType.LeftArm }

            SlotCard(
                name = "R. ARM",
                selectedPart = viewModel.selectedRightArm,
                icon = Icons.Default.Hardware,
                color = CyberOrange,
                isSelected = activeSlot == SlotType.RightArm,
                modifier = Modifier.weight(1f).testTag("right_arm_slot")
            ) { activeSlot = SlotType.RightArm }

            SlotCard(
                name = "UTILITY",
                selectedPart = viewModel.selectedUtility,
                icon = Icons.Default.BatteryChargingFull,
                color = CyberLime,
                isSelected = activeSlot == SlotType.Utility,
                modifier = Modifier.weight(1f).testTag("utility_slot")
            ) { activeSlot = SlotType.Utility }
        }

        // Active Part Selector Panel Drawer (if a slot is selected)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (activeSlot != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSteel),
                    border = BorderStroke(1.5.dp, CyberBlue),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "INSTALL ${activeSlot?.name ?: ""} CHIP",
                                color = CyberBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(onClick = { activeSlot = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close Selector", tint = CyberGray)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        val partOptions = when (activeSlot) {
                            SlotType.Leg -> listOf("Wheels", "Tank Tracks", "Spider Legs", "Hover Engine", "Jump Springs", "Jetpack")
                            SlotType.LeftArm, SlotType.RightArm -> listOf("Empty", "Grabber", "Magnet", "Hammer", "Drill", "Welding Torch", "Shield Arm")
                            SlotType.Utility -> listOf("Empty", "Battery Pack", "Turbo Battery", "Cooling System", "Object Detector", "Heat Sensor")
                            else -> emptyList()
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(partOptions) { part ->
                                val isCurrentlyEquipped = when (activeSlot) {
                                    SlotType.Leg -> viewModel.selectedLegs == part
                                    SlotType.LeftArm -> viewModel.selectedLeftArm == part
                                    SlotType.RightArm -> viewModel.selectedRightArm == part
                                    SlotType.Utility -> viewModel.selectedUtility == part
                                    else -> false
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCurrentlyEquipped) CyberIron else CyberObsidian)
                                        .clickable {
                                            viewModel.selectPart(activeSlot!!, part)
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = part,
                                            color = if (isCurrentlyEquipped) CyberBlue else CyberWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = getPartSpecDescription(part),
                                            color = CyberGray,
                                            fontSize = 12.sp
                                        )
                                    }
                                    if (isCurrentlyEquipped) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Equipped",
                                            tint = CyberBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Idle Help Banner
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSteel.copy(alpha = 0.5f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help",
                            tint = CyberGray,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = level.promptText,
                            color = CyberWhite,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap any module slot above to connect compatible components and tools.",
                            color = CyberGray,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Interactive Simulation Control Button Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (viewModel.simulationState == SimulationState.Running) {
                Button(
                    onClick = { viewModel.resetSimulation() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = CyberWhite)
                        Text("EMERGENCY ABORT", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.startSimulation() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberLime),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("launch_simulation_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = CyberObsidian)
                        Text("PRESS TO LAUNCH", color = CyberObsidian, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
                
                OutlinedButton(
                    onClick = { viewModel.resetSimulation() },
                    border = BorderStroke(1.dp, CyberWhite),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("CLEAR PARTS", color = CyberWhite)
                }
            }
        }
    }

    // Success dialog overlays
    when (val state = viewModel.simulationState) {
        is SimulationState.Success -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetSimulation() },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Cup", tint = CyberGold)
                        Text("MISSION ACCOMPLISHED!", color = CyberWhite, fontWeight = FontWeight.Black)
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Spring Star count
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (s in 1..3) {
                                val starTint = if (s <= state.stars) CyberGold else CyberIron
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star",
                                    tint = starTint,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Text(
                            text = state.message,
                            color = CyberWhite,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp
                        )

                        Text(
                            text = "Reward Earned: +${state.coinsEarned} Gold Credits 🪙",
                            color = CyberGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetSimulation()
                            val nextId = level.id + 1
                            val nextDef = LevelDefinitions.levels.find { it.id == nextId }
                            if (nextDef != null) {
                                viewModel.selectLevel(nextId)
                            } else {
                                viewModel.onScreenChanged(Screen.LevelSelect)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlue)
                    ) {
                        Text("NEXT MISSION", color = CyberObsidian, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.resetSimulation() }) {
                        Text("REBUILD CHASSIS", color = CyberWhite)
                    }
                },
                containerColor = CyberSteel
            )
        }
        is SimulationState.Failure -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetSimulation() },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = "Alert", tint = CyberRed)
                        Text("ROBOT SENSOR FAILURE!", color = CyberWhite, fontWeight = FontWeight.Black)
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Diagnostic image
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(CyberRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Crash",
                                tint = CyberRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "Error Code: ${state.failType.uppercase()}",
                            color = CyberRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = state.message,
                            color = CyberWhite,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resetSimulation() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberLime)
                    ) {
                        Text("REPAIR & ASSEMBLE", color = CyberObsidian, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onScreenChanged(Screen.LevelSelect) }) {
                        Text("SECTOR SELECT", color = CyberWhite)
                    }
                },
                containerColor = CyberSteel
            )
        }
        else -> {}
    }
}

@Composable
fun SlotCard(
    name: String,
    selectedPart: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else CyberSteel)
            .border(
                width = 1.5.dp,
                color = if (isSelected) color else CyberIron,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (selectedPart == "Empty") CyberGray else color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = name,
                color = CyberGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = selectedPart,
                color = if (selectedPart == "Empty") CyberGray else CyberWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CustomizationScreen(
    viewModel: GameViewModel,
    profile: UserProfile?
) {
    var activeTab by remember { mutableStateOf("paint") }
    var useThreeDView by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    val paints = listOf(
        "Standard Blue" to "#3D5AFE" to 0,
        "Lime Neon" to "#00E676" to 100,
        "Magma Orange" to "#FF9100" to 150,
        "Hot Cyber Pink" to "#FF1744" to 120,
        "Imperial Gold" to "#FFD600" to 250
    )

    val eyes = listOf(
        "Standard Digital" to "digital" to 0,
        "Camera Lenses" to "retro" to 80,
        "Cyber Lenses" to "laser" to 120,
        "Aviator Spectacles" to "glass" to 200
    )

    val hats = listOf(
        "Slick Bald" to "none" to 0,
        "Constructor Hat" to "builder" to 50,
        "Dapper Top Hat" to "top_hat" to 100,
        "Imperial Crown" to "builder_crown" to 250,
        "Green Sprout" to "sprout" to 30,
        "Toilet Paper" to "toilet_paper" to 40,
        "Fried Egg" to "egg" to 60,
        "Sticky Note" to "sticky_note" to 25,
        "Red Cherry" to "cherry" to 45,
        "Toilet Plunger" to "plunger" to 75
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ROBOT GARAGE",
                color = CyberWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            // Dynamic 3D/2D Visualizer Mode Selector
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF151B33))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(false to "2D DIAGRAM", true to "3D LAB").forEach { (is3D, label) ->
                    val isSelected = useThreeDView == is3D
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) CyberBlue else Color.Transparent)
                            .clickable { useThreeDView = is3D }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) CyberWhite else CyberGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live Customization Preview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, CyberIron, RoundedCornerShape(12.dp))
                .background(Color(0xFF0C101F)),
            contentAlignment = Alignment.Center
        ) {
            if (useThreeDView) {
                ThreeRobotViewer(
                    paintColor = profile?.selectedPaint ?: "#3D5AFE",
                    eyesType = profile?.selectedEyes ?: "digital",
                    hatType = profile?.selectedHat ?: "none",
                    legs = viewModel.selectedLegs,
                    leftArm = viewModel.selectedLeftArm,
                    rightArm = viewModel.selectedRightArm,
                    utility = viewModel.selectedUtility,
                    headStyle = viewModel.headStyle,
                    headMaterial = viewModel.headMaterial,
                    torsoStyle = viewModel.torsoStyle,
                    torsoMaterial = viewModel.torsoMaterial,
                    armsStyle = viewModel.armsStyle,
                    armsMaterial = viewModel.armsMaterial,
                    legsStyle = viewModel.legsStyle,
                    legsMaterial = viewModel.legsMaterial,
                    activeAnimation = viewModel.activeAnimation,
                    isAnimating = viewModel.isAnimating,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                viewModel.modelRotationAngleY += dragAmount.x * 0.015f
                                viewModel.modelRotationAngleX -= dragAmount.y * 0.015f
                            }
                        }
                ) {
                    drawRobotChassis(
                        paintColor = profile?.selectedPaint ?: "#3D5AFE",
                        eyesType = profile?.selectedEyes ?: "digital",
                        hatType = profile?.selectedHat ?: "none",
                        legs = viewModel.selectedLegs,
                        leftArm = viewModel.selectedLeftArm,
                        rightArm = viewModel.selectedRightArm,
                        utility = viewModel.selectedUtility,
                        progress = 0.5f,
                        state = SimulationState.Idle,
                        rotX = viewModel.modelRotationAngleX,
                        rotY = viewModel.modelRotationAngleY,
                        headStyle = viewModel.headStyle,
                        torsoStyle = viewModel.torsoStyle
                    )
                }
            }
        }

        // Real-time Performance Statistics Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1324)),
            border = BorderStroke(1.dp, CyberIron),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "REAL-TIME PERFORMANCE TELEMETRY",
                    color = CyberBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val customWeight = viewModel.getCustomWeight()
                    val customPower = viewModel.getCustomPower()
                    val customEfficiency = viewModel.getCustomEfficiency()

                    // 1. Weight Stat
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF161C33))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Weight",
                            tint = CyberLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "WEIGHT",
                            color = CyberGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${customWeight} kg",
                            color = CyberWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // 2. Energy Consumption Stat
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF161C33))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Energy Consumption",
                            tint = CyberOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ENERGY COST",
                            color = CyberGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${customPower} W",
                            color = CyberWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // 3. Efficiency Stat
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF161C33))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Efficiency",
                            tint = CyberBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "EFFICIENCY",
                            color = CyberGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${customEfficiency}%",
                            color = if (customEfficiency > 75) CyberLime else if (customEfficiency > 45) CyberBlue else CyberRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Shop tab switchers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "paint" to "PAINT",
                "eyes" to "EYES",
                "hat" to "HATS",
                "parts" to "PARTS",
                "assembly" to "ASSEMBLY",
                "anim" to "ANIMATE"
            ).forEach { (tabId, tabLabel) ->
                val isSelected = activeTab == tabId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyberBlue else CyberSteel)
                        .clickable { activeTab = tabId }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabLabel,
                        color = if (isSelected) CyberObsidian else CyberWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Divider(color = CyberIron)

        // Options Scroller List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            when (activeTab) {
                "paint" -> {
                    items(paints) { item ->
                        val label = item.first.first
                        val hex = item.first.second
                        val price = item.second
                        val isUnlocked = profile?.unlockedCosmetics?.contains("paint_$hex") == true || price == 0
                        val isEquipped = profile?.selectedPaint == hex

                        CosmeticItemRow(
                            label = label,
                            price = price,
                            isUnlocked = isUnlocked,
                            isEquipped = isEquipped,
                            colorCircle = Color(android.graphics.Color.parseColor(hex))
                        ) {
                            coroutineScope.launch {
                                if (isUnlocked) {
                                    viewModel.selectCosmetic("paint", hex)
                                } else {
                                    viewModel.purchaseSkin("paint_$hex", price)
                                }
                            }
                        }
                    }
                }
                "eyes" -> {
                    items(eyes) { item ->
                        val label = item.first.first
                        val id = item.first.second
                        val price = item.second
                        val isUnlocked = profile?.unlockedCosmetics?.contains("eyes_$id") == true || price == 0
                        val isEquipped = profile?.selectedEyes == id

                        CosmeticItemRow(
                            label = label,
                            price = price,
                            isUnlocked = isUnlocked,
                            isEquipped = isEquipped,
                            icon = Icons.Default.Visibility
                        ) {
                            coroutineScope.launch {
                                if (isUnlocked) {
                                    viewModel.selectCosmetic("eyes", id)
                                } else {
                                    viewModel.purchaseSkin("eyes_$id", price)
                                }
                            }
                        }
                    }
                }
                "hat" -> {
                    items(hats) { item ->
                        val label = item.first.first
                        val id = item.first.second
                        val price = item.second
                        val isUnlocked = profile?.unlockedCosmetics?.contains("hat_$id") == true || price == 0
                        val isEquipped = profile?.selectedHat == id

                        CosmeticItemRow(
                            label = label,
                            price = price,
                            isUnlocked = isUnlocked,
                            isEquipped = isEquipped,
                            icon = Icons.Default.SmartToy
                        ) {
                            coroutineScope.launch {
                                if (isUnlocked) {
                                    viewModel.selectCosmetic("hat", id)
                                } else {
                                    viewModel.purchaseSkin("hat_$id", price)
                                }
                            }
                        }
                    }
                }
                "parts" -> {
                    val headStyleOptions = listOf("Standard Dome", "Stealth Visor", "Retro Lens", "Monocular Laser", "Crewmate Visor")
                    val torsoStyleOptions = listOf("Fusion Frame", "Shielded Carapace", "Minimal Chassis", "Crewmate Suit")
                    val materialOptions = listOf("Chrome Metal", "Golden Cyber", "Carbon Fiber", "Rusty Scrapyard", "Neon Grid", "Crewmate Cel")

                    item {
                        CycleSettingRow(
                            label = "HEAD STYLE",
                            currentValue = viewModel.headStyle,
                            options = headStyleOptions,
                            icon = Icons.Default.Category
                        ) { viewModel.headStyle = it }
                    }
                    item {
                        CycleSettingRow(
                            label = "HEAD MATERIAL",
                            currentValue = viewModel.headMaterial,
                            options = materialOptions,
                            icon = Icons.Default.Palette
                        ) { viewModel.headMaterial = it }
                    }
                    item {
                        CycleSettingRow(
                            label = "TORSO STYLE",
                            currentValue = viewModel.torsoStyle,
                            options = torsoStyleOptions,
                            icon = Icons.Default.Category
                        ) { viewModel.torsoStyle = it }
                    }
                    item {
                        CycleSettingRow(
                            label = "TORSO MATERIAL",
                            currentValue = viewModel.torsoMaterial,
                            options = materialOptions,
                            icon = Icons.Default.Palette
                        ) { viewModel.torsoMaterial = it }
                    }
                    item {
                        CycleSettingRow(
                            label = "ARMS MATERIAL",
                            currentValue = viewModel.armsMaterial,
                            options = materialOptions,
                            icon = Icons.Default.Palette
                        ) { viewModel.armsMaterial = it }
                    }
                    item {
                        CycleSettingRow(
                            label = "LEGS MATERIAL",
                            currentValue = viewModel.legsMaterial,
                            options = materialOptions,
                            icon = Icons.Default.Palette
                        ) { viewModel.legsMaterial = it }
                    }
                }
                "assembly" -> {
                    val legsOptions = listOf("Wheels", "Tank Tracks", "Spider Legs", "Hover Engine", "Jump Springs", "Jetpack")
                    val armOptions = listOf("Empty", "Grabber", "Magnet", "Hammer", "Drill", "Welding Torch", "Shield Arm")
                    val utilityOptions = listOf("Empty", "Battery Pack", "Turbo Battery", "Cooling System", "Object Detector", "Heat Sensor")

                    item {
                        CycleSettingRow(
                            label = "LEGS MODULE",
                            currentValue = viewModel.selectedLegs,
                            options = legsOptions,
                            icon = Icons.Default.DirectionsRun
                        ) { viewModel.selectPart(SlotType.Leg, it) }
                    }
                    item {
                        CycleSettingRow(
                            label = "LEFT ARM MODULE",
                            currentValue = viewModel.selectedLeftArm,
                            options = armOptions,
                            icon = Icons.Default.BackHand
                        ) { viewModel.selectPart(SlotType.LeftArm, it) }
                    }
                    item {
                        CycleSettingRow(
                            label = "RIGHT ARM MODULE",
                            currentValue = viewModel.selectedRightArm,
                            options = armOptions,
                            icon = Icons.Default.FrontHand
                        ) { viewModel.selectPart(SlotType.RightArm, it) }
                    }
                    item {
                        CycleSettingRow(
                            label = "UTILITY MODULE",
                            currentValue = viewModel.selectedUtility,
                            options = utilityOptions,
                            icon = Icons.Default.Settings
                        ) { viewModel.selectPart(SlotType.Utility, it) }
                    }
                }
                "anim" -> {
                    val animationOptions = listOf("Idle Float", "Weapon Test", "Walk Cycle", "Radar Scan", "Disco Party")

                    item {
                        CycleSettingRow(
                            label = "ACTIVE ANIMATION",
                            currentValue = viewModel.activeAnimation,
                            options = animationOptions,
                            icon = Icons.Default.AutoAwesome
                        ) { viewModel.activeAnimation = it }
                    }
                    item {
                        Card(
                            onClick = { viewModel.isAnimating = !viewModel.isAnimating },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF13182C)),
                            border = BorderStroke(1.dp, CyberIron),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1D2440)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (viewModel.isAnimating) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = CyberBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "ANIMATION STATE",
                                        color = CyberGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (viewModel.isAnimating) "PLAYING" else "PAUSED",
                                        color = CyberWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1D2440))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (viewModel.isAnimating) "PAUSE" else "PLAY",
                                        color = CyberBlue,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CycleSettingRow(
    label: String,
    currentValue: String,
    options: List<String>,
    icon: ImageVector,
    onValueChange: (String) -> Unit
) {
    Card(
        onClick = {
            val currentIndex = options.indexOf(currentValue)
            val nextIndex = if (currentIndex < 0) 0 else (currentIndex + 1) % options.size
            onValueChange(options[nextIndex])
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13182C)),
        border = BorderStroke(1.dp, CyberIron),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1D2440)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = CyberBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    color = CyberGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = currentValue.uppercase(),
                    color = CyberWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1D2440))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "CYCLE",
                    color = CyberBlue,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun CosmeticItemRow(
    label: String,
    price: Int,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    colorCircle: Color? = null,
    icon: ImageVector? = null,
    onAction: () -> Unit
) {
    Card(
        onClick = onAction,
        colors = CardDefaults.cardColors(containerColor = CyberSteel),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circle Paint preview or Icon
            if (colorCircle != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colorCircle)
                        .border(1.5.dp, CyberWhite, CircleShape)
                )
            } else if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberIron),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = "Cosmetic", tint = CyberBlue, modifier = Modifier.size(18.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = CyberWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (isEquipped) {
                    Text("EQUIPPED", color = CyberBlue, fontSize = 11.sp, fontWeight = FontWeight.Black)
                } else if (isUnlocked) {
                    Text("UNLOCKED", color = CyberLime, fontSize = 11.sp)
                } else {
                    Text("LOCKED", color = CyberGray, fontSize = 11.sp)
                }
            }

            if (!isUnlocked) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGold),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Lock", tint = CyberObsidian, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$price 🪙", color = CyberObsidian, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else if (!isEquipped) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberIron),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("EQUIP", color = CyberWhite, fontSize = 12.sp)
                }
            }
        }
    }
}



// ------------------------------------------------------------------------
// PHYSICS & GRAPHICS RENDER FUNCTIONS FOR THE CUSTOM CANVAS
// ------------------------------------------------------------------------

fun DrawScope.drawSimulationWorldBackground(worldName: String) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    val bgGradient = when (worldName) {
        "Factory" -> Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
        "Forest" -> Brush.verticalGradient(listOf(Color(0xFF065F46), Color(0xFF022C22)))
        "Desert" -> Brush.verticalGradient(listOf(Color(0xFF78350F), Color(0xFF451A03)))
        "Snow" -> Brush.verticalGradient(listOf(Color(0xFF155E75), Color(0xFF083344)))
        "Volcano" -> Brush.verticalGradient(listOf(Color(0xFF7F1D1D), Color(0xFF450A0A)))
        "Deep Space" -> Brush.verticalGradient(listOf(Color(0xFF1A102F), Color(0xFF0B0418)))
        "Cyber City" -> Brush.verticalGradient(listOf(Color(0xFF23073E), Color(0xFF080211)))
        "Ocean Depths" -> Brush.verticalGradient(listOf(Color(0xFF0B2545), Color(0xFF010811)))
        else -> Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617)))
    }

    drawRect(brush = bgGradient, size = size)

    // Draw secondary decor details (e.g., grids, cogs, stars)
    when (worldName) {
        "Factory" -> {
            // Draw grid and metal plates
            for (x in 0..canvasWidth.toInt() step 60) {
                drawLine(Color(0xFF334155).copy(alpha = 0.3f), Offset(x.toFloat(), 0f), Offset(x.toFloat(), canvasHeight))
            }
            // Background cogs
            drawCircle(Color(0xFF334155).copy(alpha = 0.15f), 40f, Offset(canvasWidth - 100f, 60f))
            drawCircle(Color(0xFF334155).copy(alpha = 0.15f), 25f, Offset(canvasWidth - 135f, 75f))
        }
        "Forest" -> {
            // Draw tall grass blades in background
            for (x in 20..canvasWidth.toInt() step 50) {
                drawLine(Color(0xFF059669).copy(alpha = 0.2f), Offset(x.toFloat(), canvasHeight - 50f), Offset(x.toFloat() + 10f, canvasHeight - 110f), strokeWidth = 3f)
            }
        }
        "Desert" -> {
            // Draw soft dune curvatures
            val dunePath = Path().apply {
                moveTo(0f, canvasHeight - 40f)
                quadraticTo(canvasWidth * 0.4f, canvasHeight - 120f, canvasWidth * 0.7f, canvasHeight - 50f)
                quadraticTo(canvasWidth * 0.85f, canvasHeight - 30f, canvasWidth, canvasHeight - 80f)
                lineTo(canvasWidth, canvasHeight)
                lineTo(0f, canvasHeight)
                close()
            }
            drawPath(dunePath, Color(0xFFB45309).copy(alpha = 0.25f))
        }
        "Snow" -> {
            // Draw tiny falling snow star points
            drawCircle(Color.White.copy(alpha = 0.5f), 2f, Offset(canvasWidth * 0.2f, 30f))
            drawCircle(Color.White.copy(alpha = 0.5f), 3f, Offset(canvasWidth * 0.45f, 60f))
            drawCircle(Color.White.copy(alpha = 0.5f), 2f, Offset(canvasWidth * 0.75f, 25f))
            drawCircle(Color.White.copy(alpha = 0.3f), 4f, Offset(canvasWidth * 0.9f, 80f))
        }
        "Volcano" -> {
            // Heat haze squiggles
            for (i in 0..3) {
                val waveX = canvasWidth * 0.2f * i + 100f
                drawLine(Color(0xFFEF4444).copy(alpha = 0.15f), Offset(waveX, canvasHeight - 60f), Offset(waveX + 10f, canvasHeight - 140f), strokeWidth = 2f)
            }
        }
        "Deep Space" -> {
            // Nebula glow
            drawCircle(Color(0xFF8E24AA).copy(alpha = 0.12f), 120f, Offset(canvasWidth * 0.3f, canvasHeight * 0.4f))
            drawCircle(Color(0xFF00E5FF).copy(alpha = 0.08f), 80f, Offset(canvasWidth * 0.7f, canvasHeight * 0.3f))
            
            // Crescent moon/planet
            drawCircle(Color(0xFFEDE7F6), 35f, Offset(canvasWidth - 90f, 65f))
            drawCircle(Color(0xFF1A102F), 32f, Offset(canvasWidth - 105f, 55f)) // shadow overlap

            // Twinkling stars
            val starPositions = listOf(
                Offset(canvasWidth * 0.1f, 35f),
                Offset(canvasWidth * 0.25f, 85f),
                Offset(canvasWidth * 0.5f, 40f),
                Offset(canvasWidth * 0.75f, 90f),
                Offset(canvasWidth * 0.9f, 30f)
            )
            starPositions.forEach { pos ->
                drawCircle(Color.White, 2f, pos)
                // Draw a small 4-point lens flare
                drawLine(Color.White.copy(alpha = 0.6f), Offset(pos.x - 5f, pos.y), Offset(pos.x + 5f, pos.y), strokeWidth = 1f)
                drawLine(Color.White.copy(alpha = 0.6f), Offset(pos.x, pos.y - 5f), Offset(pos.x, pos.y + 5f), strokeWidth = 1f)
            }
        }
        "Cyber City" -> {
            // Glowing neon wireframe skyscrapers
            val buildingColor = Color(0xFFE91E63).copy(alpha = 0.15f)
            val windowColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
            
            // Building 1
            drawRect(buildingColor, topLeft = Offset(40f, canvasHeight - 160f), size = Size(65f, 120f))
            // Windows
            for (r in 0..3) {
                for (c in 0..1) {
                    drawRect(windowColor, topLeft = Offset(52f + c * 20f, canvasHeight - 145f + r * 25f), size = Size(8f, 12f))
                }
            }

            // Building 2
            drawRect(buildingColor, topLeft = Offset(canvasWidth - 120f, canvasHeight - 190f), size = Size(75f, 150f))
            for (r in 0..4) {
                for (c in 0..2) {
                    drawRect(windowColor, topLeft = Offset(canvasWidth - 110f + c * 18f, canvasHeight - 175f + r * 28f), size = Size(6f, 10f))
                }
            }

            // Tech grids in sky
            for (y in 20..80 step 20) {
                drawLine(Color(0xFF00E5FF).copy(alpha = 0.1f), Offset(0f, y.toFloat()), Offset(canvasWidth, y.toFloat()))
            }
        }
        "Ocean Depths" -> {
            // Floating bubbles
            val bubbleColor = Color(0xFF81D4FA).copy(alpha = 0.25f)
            val bubbleOffsets = listOf(
                Offset(canvasWidth * 0.15f, canvasHeight - 70f),
                Offset(canvasWidth * 0.35f, canvasHeight - 120f),
                Offset(canvasWidth * 0.55f, canvasHeight - 60f),
                Offset(canvasWidth * 0.8f, canvasHeight - 100f),
                Offset(canvasWidth * 0.9f, canvasHeight - 50f)
            )
            bubbleOffsets.forEachIndexed { index, offset ->
                val r = 5f + (index % 3) * 3f
                drawCircle(bubbleColor, r, offset, style = Stroke(width = 1.5f))
                drawCircle(Color.White.copy(alpha = 0.2f), r * 0.3f, Offset(offset.x - r * 0.3f, offset.y - r * 0.3f))
            }

            // Wavy sea kelp paths
            for (i in 0..2) {
                val kelpX = canvasWidth * 0.25f * i + 80f
                val kelpPath = Path().apply {
                    moveTo(kelpX, canvasHeight - 40f)
                    quadraticTo(kelpX - 15f, canvasHeight - 90f, kelpX + 5f, canvasHeight - 140f)
                    quadraticTo(kelpX + 20f, canvasHeight - 180f, kelpX, canvasHeight - 210f)
                }
                drawPath(
                    path = kelpPath,
                    color = Color(0xFF00B0FF).copy(alpha = 0.15f),
                    style = Stroke(width = 4f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
        }
    }

    // Ground platform line
    val groundY = canvasHeight - 40f
    drawLine(
        color = Color(0xFF64748B),
        start = Offset(0f, groundY),
        end = Offset(canvasWidth, groundY),
        strokeWidth = 4f
    )
}

fun DrawScope.drawWorldObstacles(
    hazardType: String,
    progress: Float,
    state: SimulationState
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val groundY = canvasHeight - 40f

    val midX = canvasWidth * 0.5f

    when (hazardType) {
        "lava", "lava_repair" -> {
            // Magma pit in the middle
            val poolWidth = 140f
            val startX = midX - poolWidth / 2
            
            // Lava body
            drawRect(
                color = Color(0xFFFF3D00),
                topLeft = Offset(startX, groundY - 6f),
                size = Size(poolWidth, 24f)
            )
            // Yellow heat highlights
            drawRect(
                color = Color(0xFFFFC400),
                topLeft = Offset(startX + 10f, groundY - 4f),
                size = Size(poolWidth - 20f, 6f)
            )
        }
        "river" -> {
            // River pit
            val poolWidth = 130f
            val startX = midX - poolWidth / 2
            drawRect(
                color = Color(0xFF2979FF),
                topLeft = Offset(startX, groundY - 6f),
                size = Size(poolWidth, 24f)
            )
            // Waves
            drawLine(Color.White.copy(alpha = 0.6f), Offset(startX + 10f, groundY + 4f), Offset(startX + 40f, groundY + 4f), strokeWidth = 2f)
            drawLine(Color.White.copy(alpha = 0.6f), Offset(startX + 70f, groundY + 12f), Offset(startX + 110f, groundY + 12f), strokeWidth = 2f)
        }
        "quicksand" -> {
            // Shifting desert mud
            val poolWidth = 120f
            val startX = midX - poolWidth / 2
            drawRect(
                color = Color(0xFF8D6E63),
                topLeft = Offset(startX, groundY - 6f),
                size = Size(poolWidth, 22f)
            )
        }
        "laser" -> {
            // Draw a high tech vertical laser beam in the middle
            val isBlocked = progress >= 0.45f && state is SimulationState.Running && !state.javaClass.name.contains("Failure")
            
            if (isBlocked) {
                // Deflected short laser
                drawLine(
                    color = Color(0xFFFF1744),
                    start = Offset(midX, 0f),
                    end = Offset(midX, groundY - 120f),
                    strokeWidth = 6f
                )
                // Scatter sparks at contact point
                drawCircle(Color.White, 8f, Offset(midX, groundY - 120f))
            } else {
                // Complete cutting beam
                drawLine(
                    color = Color(0xFFFF1744),
                    start = Offset(midX, 0f),
                    end = Offset(midX, groundY),
                    strokeWidth = 6f
                )
            }
        }
        "wall" -> {
            // Draw a big concrete barrier at progress = 0.5
            val wallDestroyed = progress >= 0.5f && state is SimulationState.Success
            if (!wallDestroyed) {
                drawRoundRect(
                    color = Color(0xFF64748B),
                    topLeft = Offset(midX - 15f, groundY - 80f),
                    size = Size(30f, 80f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // Cracks/stripes
                drawLine(Color.Black, Offset(midX - 15f, groundY - 40f), Offset(midX + 15f, groundY - 30f), strokeWidth = 2f)
                drawLine(Color.Black, Offset(midX, groundY - 70f), Offset(midX - 10f, groundY - 50f), strokeWidth = 2f)
            } else {
                // Draw shattered rocks falling
                drawCircle(Color(0xFF64748B), 6f, Offset(midX - 25f, groundY - 15f))
                drawCircle(Color(0xFF64748B), 8f, Offset(midX + 20f, groundY - 10f))
                drawCircle(Color(0xFF64748B), 5f, Offset(midX + 5f, groundY - 5f))
            }
        }
        "glacier" -> {
            // Shiny glacier blocks
            val wallDestroyed = progress >= 0.5f && state is SimulationState.Success
            if (!wallDestroyed) {
                drawRoundRect(
                    color = Color(0xFFE0F7FA),
                    topLeft = Offset(midX - 20f, groundY - 90f),
                    size = Size(40f, 90f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Ice highlights
                drawLine(Color.White, Offset(midX - 10f, groundY - 80f), Offset(midX - 10f, groundY - 10f), strokeWidth = 2f)
            } else {
                // Melting water puddle
                drawRoundRect(
                    color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                    topLeft = Offset(midX - 30f, groundY - 4f),
                    size = Size(60f, 6f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }
        "box" -> {
            // Crate waiting at mid
            val pickedUp = progress >= 0.45f && (state is SimulationState.Success || (state is SimulationState.Running && progress >= 0.45f))
            if (!pickedUp) {
                drawRoundRect(
                    color = Color(0xFFD84315),
                    topLeft = Offset(midX - 15f, groundY - 30f),
                    size = Size(30f, 30f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                // Diagonal cross on crate
                drawLine(Color.Black, Offset(midX - 15f, groundY - 30f), Offset(midX + 15f, groundY), strokeWidth = 2f)
                drawLine(Color.Black, Offset(midX + 15f, groundY - 30f), Offset(midX - 15f, groundY), strokeWidth = 2f)
            }
        }
        "magnetic_coin" -> {
            // Golden circular coin in thorn pit
            val retrieved = progress >= 0.45f && state is SimulationState.Success
            if (!retrieved) {
                // Thorns
                drawLine(Color(0xFF4E342E), Offset(midX - 25f, groundY), Offset(midX, groundY - 15f), strokeWidth = 3f)
                drawLine(Color(0xFF4E342E), Offset(midX + 25f, groundY), Offset(midX, groundY - 15f), strokeWidth = 3f)
                
                // Coin floating
                drawCircle(
                    color = CyberGold,
                    radius = 8f,
                    center = Offset(midX, groundY - 25f)
                )
            }
        }
        "asteroid" -> {
            val destroyed = progress >= 0.5f && state is SimulationState.Success
            if (!destroyed) {
                // Main meteor body
                drawCircle(Color(0xFF475569), 32f, Offset(midX, groundY - 40f))
                drawCircle(Color(0xFF334155), 24f, Offset(midX - 10f, groundY - 45f))
                // Crater highlights
                drawCircle(Color(0xFF1E293B).copy(alpha = 0.6f), 6f, Offset(midX + 10f, groundY - 35f))
                drawCircle(Color(0xFF1E293B).copy(alpha = 0.6f), 4f, Offset(midX - 12f, groundY - 25f))
                drawCircle(Color(0xFF1E293B).copy(alpha = 0.6f), 5f, Offset(midX, groundY - 50f))
            } else {
                // Pulverized space rubble drifting away
                drawCircle(Color(0xFF475569), 6f, Offset(midX - 25f, groundY - 60f))
                drawCircle(Color(0xFF334155), 4f, Offset(midX + 30f, groundY - 20f))
                drawCircle(Color(0xFF64748B), 5f, Offset(midX + 10f, groundY - 75f))
                drawCircle(Color(0xFF475569), 3f, Offset(midX - 15f, groundY - 15f))
            }
        }
        "cosmic_radiation" -> {
            val isDeflected = progress >= 0.45f && state is SimulationState.Running && !state.javaClass.name.contains("Failure")
            
            // Radiating wave fields in the center
            for (offsetY in 10..70 step 20) {
                val waveShift = kotlin.math.sin(progress * 20f + offsetY) * 12f
                drawLine(
                    color = Color(0xFFD500F9).copy(alpha = 0.4f),
                    start = Offset(midX - 30f + waveShift, groundY - 80f + offsetY),
                    end = Offset(midX + 30f + waveShift, groundY - 80f + offsetY),
                    strokeWidth = 3f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
            if (isDeflected) {
                // Draw a stunning spherical blue protective forcefield bubble around the robot's advance location
                val robotX = 60f + progress * (size.width - 140f)
                val robotY = groundY - 60f
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                    radius = 45f,
                    center = Offset(robotX, robotY),
                    style = Stroke(width = 3f)
                )
            }
        }
        "zero_g" -> {
            // Draw a spinning violet anomaly vortex
            val rotationAngle = progress * 360f
            drawCircle(
                color = Color(0xFF7C4DFF).copy(alpha = 0.3f),
                radius = 35f,
                center = Offset(midX, groundY - 45f)
            )
            drawCircle(
                color = Color(0xFFE040FB).copy(alpha = 0.2f),
                radius = 20f,
                center = Offset(midX, groundY - 45f)
            )
            // Inner core
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = Offset(midX, groundY - 45f)
            )
        }
        "cyber_grid" -> {
            val isBypassed = progress >= 0.45f && state is SimulationState.Running && !state.javaClass.name.contains("Failure")
            if (!isBypassed) {
                // Laser security grid wall
                for (y in 0..120 step 20) {
                    drawLine(
                        color = Color(0xFFFF007F),
                        start = Offset(midX - 25f, groundY - y.toFloat()),
                        end = Offset(midX + 25f, groundY - y.toFloat()),
                        strokeWidth = 2f
                    )
                }
                drawLine(Color(0xFFFF007F), Offset(midX - 25f, groundY), Offset(midX - 25f, groundY - 120f), strokeWidth = 3f)
                drawLine(Color(0xFFFF007F), Offset(midX + 25f, groundY), Offset(midX + 25f, groundY - 120f), strokeWidth = 3f)
            } else {
                // Flickering offline grid
                drawLine(Color(0xFFFF007F).copy(alpha = 0.2f), Offset(midX - 25f, groundY), Offset(midX - 25f, groundY - 120f), strokeWidth = 1f)
                drawLine(Color(0xFFFF007F).copy(alpha = 0.2f), Offset(midX + 25f, groundY), Offset(midX + 25f, groundY - 120f), strokeWidth = 1f)
            }
        }
        "neon_emp" -> {
            // Pulsing mechanical EMP spire
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(midX - 12f, groundY - 80f),
                size = Size(24f, 80f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Glowing neon cap
            val pulseAlpha = 0.4f + kotlin.math.sin(progress * 25f) * 0.3f
            drawCircle(
                color = Color(0xFFFF007F),
                radius = 12f,
                center = Offset(midX, groundY - 85f)
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = pulseAlpha),
                radius = 25f,
                center = Offset(midX, groundY - 85f),
                style = Stroke(width = 2f)
            )
            
            // Random electrical lightning discharge arcs if active
            if (state is SimulationState.Running) {
                val arcOffset = kotlin.math.sin(progress * 100f) * 15f
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(midX, groundY - 85f),
                    end = Offset(midX + arcOffset, groundY - 30f),
                    strokeWidth = 2f
                )
            }
        }
        "nano_swarm" -> {
            val cleared = progress >= 0.5f && state is SimulationState.Success
            if (!cleared) {
                // Draw floating green diamond dust nanites
                val naniteColor = Color(0xFF00E676)
                val particles = listOf(
                    Offset(midX - 20f, groundY - 45f),
                    Offset(midX - 5f, groundY - 60f),
                    Offset(midX + 15f, groundY - 35f),
                    Offset(midX - 10f, groundY - 20f),
                    Offset(midX + 8f, groundY - 50f),
                    Offset(midX + 22f, groundY - 65f)
                )
                particles.forEach { offset ->
                    val jitterX = kotlin.math.sin(progress * 40f + offset.y) * 4f
                    drawCircle(naniteColor, 3f, Offset(offset.x + jitterX, offset.y))
                }
            }
        }
        "abyssal_pressure" -> {
            // Heavy vertical water pressure wave
            val pressureColor = Color(0xFF0D47A1).copy(alpha = 0.5f)
            drawRect(
                color = pressureColor,
                topLeft = Offset(midX - 35f, groundY - 120f),
                size = Size(70f, 120f)
            )
            // Vertical water streams
            for (x in -25..25 step 12) {
                drawLine(
                    color = Color(0xFFE0F7FA).copy(alpha = 0.3f),
                    start = Offset(midX + x.toFloat(), groundY - 120f),
                    end = Offset(midX + x.toFloat(), groundY),
                    strokeWidth = 2f
                )
            }
        }
        "water_current" -> {
            // Horizontal water torrent arrows
            val flowColor = Color(0xFF29B6F6).copy(alpha = 0.6f)
            for (y in 20..90 step 25) {
                val flowShift = (progress * 120f) % 60f
                drawLine(
                    color = flowColor,
                    start = Offset(midX - 40f + flowShift, groundY - y.toFloat()),
                    end = Offset(midX + 20f + flowShift, groundY - y.toFloat()),
                    strokeWidth = 3f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                // Arrow tip
                drawLine(
                    color = flowColor,
                    start = Offset(midX + 20f + flowShift, groundY - y.toFloat()),
                    end = Offset(midX + 12f + flowShift, groundY - y.toFloat() - 5f),
                    strokeWidth = 3f
                )
            }
        }
        "electric_eel" -> {
            val isDeflected = progress >= 0.45f && state is SimulationState.Running && !state.javaClass.name.contains("Failure")
            
            // Draw a yellow bioluminescent slithering eel
            val eelPath = Path().apply {
                val startX = midX - 35f
                moveTo(startX, groundY - 35f)
                for (xOffset in 0..70 step 10) {
                    val angleOffset = progress * 30f + xOffset.toFloat()
                    val dynamicY = groundY - 35f + kotlin.math.sin(angleOffset * 0.1f) * 15f
                    lineTo(startX + xOffset.toFloat(), dynamicY)
                }
            }
            drawPath(
                path = eelPath,
                color = Color(0xFFFFD600),
                style = Stroke(width = 5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            
            // Electric shocks
            if (!isDeflected) {
                val electricX = midX + kotlin.math.sin(progress * 150f) * 20f
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(electricX, groundY - 50f),
                    end = Offset(electricX + 10f, groundY - 10f),
                    strokeWidth = 2f
                )
            }
        }
        "deep_sea_trench" -> {
            // Draw a massive drop-off on the ocean floor
            drawRect(
                color = Color(0xFF010811),
                topLeft = Offset(midX - 45f, groundY - 4f),
                size = Size(90f, 44f)
            )
            // Left reef wall
            drawLine(Color(0xFF0B2545), Offset(midX - 45f, groundY), Offset(midX - 45f, groundY + 40f), strokeWidth = 4f)
            // Right reef wall
            drawLine(Color(0xFF0B2545), Offset(midX + 45f, groundY), Offset(midX + 45f, groundY + 40f), strokeWidth = 4f)
        }
        else -> {}
    }
}


fun DrawScope.drawRobotChassis(
    paintColor: String,
    eyesType: String,
    hatType: String,
    legs: String,
    leftArm: String,
    rightArm: String,
    utility: String,
    progress: Float,
    state: SimulationState,
    rotX: Float = -0.15f,
    rotY: Float = 0.4f,
    rotZ: Float = 0f,
    cameraShake: Float = 0f,
    headStyle: String = "Standard Dome",
    torsoStyle: String = "Fusion Frame"
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val groundY = canvasHeight - 40f

    val startX = 60f
    val endX = canvasWidth - 80f
    
    // Position tracking
    var rx = startX + progress * (endX - startX)
    var ry = groundY - 60f

    val isFailed = state is SimulationState.Failure

    // Vertical jump/drift animations
    if (state is SimulationState.Running || state is SimulationState.Success) {
        when (legs) {
            "Jump Springs" -> {
                val bounce = abs(sin(progress * 15f)) * 42f
                ry -= bounce
            }
            "Jetpack", "Hover Engine" -> {
                val floatDrift = sin(progress * 25f) * 10f
                ry -= (floatDrift + 18f)
            }
        }
    }

    // Sink on drown failure
    if (isFailed) {
        val failState = state as SimulationState.Failure
        if (failState.failType == "wrong_legs" || failState.failType == "no_legs") {
            ry += 20f
        }
    }

    // Apply Camera Screen Shake
    if (cameraShake > 0f) {
        val shakeX = (Math.random() * cameraShake * 16f - cameraShake * 8f).toFloat()
        val shakeY = (Math.random() * cameraShake * 16f - cameraShake * 8f).toFloat()
        rx += shakeX
        ry += shakeY
    }

    val botBodyColor = try {
        Color(android.graphics.Color.parseColor(paintColor))
    } catch (e: Exception) {
        CyberBlue
    }

    val finalBodyColor = if (isFailed) CyberGray else botBodyColor

    // ACTIVE DEPLOYED FORCE SHIELD (3D Glowing Shell)
    if ((leftArm == "Shield Arm" || rightArm == "Shield Arm") && progress >= 0.42f && progress <= 0.68f) {
        drawCircle(
            color = CyberBlue.copy(alpha = 0.15f),
            radius = 48f,
            center = Offset(rx, ry)
        )
        drawCircle(
            color = CyberBlue,
            radius = 48f,
            center = Offset(rx, ry),
            style = Stroke(width = 2.5f)
        )
        // Shield impact ribs
        for (a in 0..3) {
            drawArc(
                color = CyberBlue.copy(alpha = 0.6f),
                startAngle = a * 90f + (progress * 400f) % 360f,
                sweepAngle = 35f,
                useCenter = false,
                topLeft = Offset(rx - 48f, ry - 48f),
                size = Size(96f, 96f),
                style = Stroke(width = 2f)
            )
        }
    }

    // --- PAINTER'S ALGORITHM ORDERED 3D COMPONENT LIST ---

    // 1. BACKPACK/UTILITY MODULE (Highly detailed sub-components)
    if (utility != "Empty") {
        val backpackColor = when (utility) {
            "Cooling System" -> CyberBlue
            "Turbo Battery" -> CyberLime
            "Object Detector", "Heat Sensor" -> CyberOrange
            else -> Color.Gray
        }
        
        // Base main backpack bracket
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 0f, cy = -5f, cz = -19f,
            sizeX = 14f, sizeY = 28f, sizeZ = 14f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = backpackColor
        )

        // Sub-component visual details inside the utility modules
        when (utility) {
            "Cooling System" -> {
                // Circular cooling radiator fans (rotating)
                val fanAngle = progress * 15f
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 0f, cy = -12f, cz = -26.1f,
                    sizeX = 8f, sizeY = 8f, sizeZ = 1f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ + fanAngle,
                    baseColor = Color.DarkGray,
                    outlineColor = CyberBlue
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 0f, cy = 2f, cz = -26.1f,
                    sizeX = 8f, sizeY = 8f, sizeZ = 1f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ - fanAngle * 1.3f,
                    baseColor = Color.DarkGray,
                    outlineColor = CyberBlue
                )
                // Dual coolant conduits running from back to chest side
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -10f, cy = -5f, cz = -14f,
                    sizeX = 4f, sizeY = 16f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ,
                    baseColor = CyberBlue.copy(alpha = 0.8f),
                    outlineColor = Color.White
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 10f, cy = -5f, cz = -14f,
                    sizeX = 4f, sizeY = 16f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ,
                    baseColor = CyberBlue.copy(alpha = 0.8f),
                    outlineColor = Color.White
                )
            }
            "Turbo Battery" -> {
                // 3 visible high-density cells side-by-side inside translucent casing
                for (cellX in listOf(-4f, 0f, 4f)) {
                    draw3DCuboid(
                        rx = rx, ry = ry,
                        cx = cellX, cy = -5f, cz = -26.1f,
                        sizeX = 3f, sizeY = 20f, sizeZ = 3f,
                        rotX = rotX, rotY = rotY, rotZ = rotZ,
                        baseColor = CyberLime,
                        outlineColor = Color.White
                    )
                }
            }
            "Battery Pack" -> {
                // Dual copper electrodes and red power indicator cap
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -4f, cy = -16f, cz = -22f,
                    sizeX = 3f, sizeY = 4f, sizeZ = 3f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ,
                    baseColor = CyberGold,
                    outlineColor = Color.White
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 4f, cy = -16f, cz = -22f,
                    sizeX = 3f, sizeY = 4f, sizeZ = 3f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ,
                    baseColor = CyberGold,
                    outlineColor = Color.White
                )
                // Pulsing indicator light
                val batteryIndicatorColor = if (progress % 0.4f > 0.2f) CyberOrange else CyberRed
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 0f, cy = 6f, cz = -26.1f,
                    sizeX = 4f, sizeY = 4f, sizeZ = 1f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ,
                    baseColor = batteryIndicatorColor
                )
            }
            "Object Detector", "Heat Sensor" -> {
                // Rotating sonar scanner dish/radar assembly on top of the module
                val radarSpin = progress * 8f
                // Pivot bar
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 0f, cy = -22f, cz = -19f,
                    sizeX = 4f, sizeY = 6f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ,
                    baseColor = CyberSteel
                )
                // Scanning panel dish
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 0f, cy = -26f, cz = -19f,
                    sizeX = 16f, sizeY = 2f, sizeZ = 10f,
                    rotX = rotX + 0.2f, rotY = rotY + radarSpin, rotZ = rotZ,
                    baseColor = CyberOrange,
                    outlineColor = Color.White
                )
            }
        }
    }

    // 2. BACK CANISTERS (JETPACK ONLY) (Enhanced visuals)
    if (legs == "Jetpack") {
        val canisterColor = if (isFailed) CyberGray else CyberSteel
        // Left canister
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = -12f, cy = -4f, cz = -16f,
            sizeX = 8f, sizeY = 24f, sizeZ = 8f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = canisterColor
        )
        // Left chrome strap
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = -12f, cy = -4f, cz = -11.9f,
            sizeX = 8.2f, sizeY = 3f, sizeZ = 1f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = Color.White,
            outlineColor = Color.Black
        )
        // Right canister
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 12f, cy = -4f, cz = -16f,
            sizeX = 8f, sizeY = 24f, sizeZ = 8f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = canisterColor
        )
        // Right chrome strap
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 12f, cy = -4f, cz = -11.9f,
            sizeX = 8.2f, sizeY = 3f, sizeZ = 1f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = Color.White,
            outlineColor = Color.Black
        )
        // Fire thruster cones
        if (!isFailed && progress > 0.05f) {
            val fl = 12f + abs(sin(progress * 35f)) * 14f
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = -12f, cy = 12f, cz = -16f,
                sizeX = 6f, sizeY = fl, sizeZ = 6f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = CyberOrange,
                outlineColor = CyberRed
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 12f, cy = 12f, cz = -16f,
                sizeX = 6f, sizeY = fl, sizeZ = 6f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = CyberOrange,
                outlineColor = CyberRed
            )
        }
    }

    // 3. SHOULDER PIVOT PLATES (Heavy Joint Integration)
    val armColor = if (isFailed) CyberGray else CyberIron
    draw3DCuboid(
        rx = rx, ry = ry,
        cx = -18f, cy = -5f, cz = 0f,
        sizeX = 4f, sizeY = 10f, sizeZ = 10f,
        rotX = rotX, rotY = rotY, rotZ = rotZ,
        baseColor = CyberSteel,
        outlineColor = Color.Black
    )
    draw3DCuboid(
        rx = rx, ry = ry,
        cx = 18f, cy = -5f, cz = 0f,
        sizeX = 4f, sizeY = 10f, sizeZ = 10f,
        rotX = rotX, rotY = rotY, rotZ = rotZ,
        baseColor = CyberSteel,
        outlineColor = Color.Black
    )

    // 4. LEFT TOOL ARM (Articulated forearm + customized components)
    if (leftArm != "Empty") {
        val swingFactor = if (progress >= 0.42f) sin(progress * 38f) * 0.4f else 0f
        
        // Upper arm segment
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = -21f, cy = -5f, cz = 0f,
            sizeX = 6f, sizeY = 8f, sizeZ = 8f,
            rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
            baseColor = armColor
        )
        // Silver forearm cylinder (Hydraulic feel)
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = -28f, cy = 0f, cz = 4f,
            sizeX = 14f, sizeY = 6f, sizeZ = 6f,
            rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
            baseColor = CyberSteel,
            outlineColor = Color.White
        )
        // Active component tooling visualisations
        when (leftArm) {
            "Hammer" -> {
                // Mallet hammer block with Caution design
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -38f, cy = 0f, cz = 6f,
                    sizeX = 12f, sizeY = 22f, sizeZ = 12f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = Color(0xFF4B5563),
                    outlineColor = CyberGold
                )
                // Piston rod
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -32f, cy = 0f, cz = 6f,
                    sizeX = 4f, sizeY = 14f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = Color.White
                )
                // Yellow safety guard
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -38f, cy = 0f, cz = 12.2f,
                    sizeX = 8f, sizeY = 16f, sizeZ = 1f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberGold,
                    outlineColor = Color.Black
                )
            }
            "Drill" -> {
                val drillAngle = if (!isFailed) progress * 140f else 0f
                // Three-tier rotating drilling bit cone
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -34f, cy = 0f, cz = 6f,
                    sizeX = 14f, sizeY = 14f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ + drillAngle,
                    baseColor = CyberSteel,
                    outlineColor = Color.White
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -40f, cy = 0f, cz = 6f,
                    sizeX = 10f, sizeY = 10f, sizeZ = 8f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ - drillAngle * 1.5f,
                    baseColor = Color.Gray,
                    outlineColor = CyberBlue
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -45f, cy = 0f, cz = 6f,
                    sizeX = 6f, sizeY = 6f, sizeZ = 6f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ + drillAngle * 2f,
                    baseColor = CyberWhite,
                    outlineColor = CyberBlue
                )
            }
            "Shield Arm" -> {
                // Hexagonal protective crystal emitter
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -32f, cy = 0f, cz = 6f,
                    sizeX = 8f, sizeY = 12f, sizeZ = 12f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberIron,
                    outlineColor = CyberBlue
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -38f, cy = 0f, cz = 6f,
                    sizeX = 3f, sizeY = 32f, sizeZ = 32f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberBlue.copy(alpha = 0.6f),
                    outlineColor = Color.White
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -38.2f, cy = 0f, cz = 6f,
                    sizeX = 1f, sizeY = 16f, sizeZ = 16f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = Color.White.copy(alpha = 0.8f)
                )
            }
            "Welding Torch" -> {
                // Detailed high-temp brass gas nozzle
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -34f, cy = 0f, cz = 6f,
                    sizeX = 12f, sizeY = 6f, sizeZ = 6f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = Color.DarkGray,
                    outlineColor = CyberOrange
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -42f, cy = 0f, cz = 6f,
                    sizeX = 8f, sizeY = 4f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberGold,
                    outlineColor = Color.White
                )
                // Jet spark flame and glowing white plasma core
                if (!isFailed && progress >= 0.42f) {
                    val pulseFlame = 6f + abs(sin(progress * 45f)) * 8f
                    draw3DCuboid(
                        rx = rx, ry = ry,
                        cx = -48f, cy = 0f, cz = 6f,
                        sizeX = pulseFlame, sizeY = 4f, sizeZ = 4f,
                        rotX = rotX, rotY = rotY, rotZ = rotZ,
                        baseColor = CyberBlue,
                        outlineColor = Color.White
                    )
                    // Golden sparks emitting from welding tip
                    val torchTipPt = VectorMath.project(VectorMath.rotateY(VectorMath.rotateX(Point3D(-48f, 0f, 6f), rotX), rotY), 350f, rx, ry)
                    drawCircle(Color.White, 3f, torchTipPt)
                    for (s in 1..4) {
                        val sx = torchTipPt.x - (s * 4f + abs(sin(progress * 90f * s)) * 12f)
                        val sy = torchTipPt.y + (sin(progress * 40f * s) * 10f)
                        drawCircle(CyberGold, 1.8f, Offset(sx, sy))
                    }
                }
            }
            "Grabber" -> {
                // Jointed base block
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -32f, cy = 0f, cz = 6f,
                    sizeX = 6f, sizeY = 10f, sizeZ = 10f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberIron
                )
                // Double articulated claws that open/close dynamically
                val pinch = if (!isFailed) abs(sin(progress * 15f)) * 8f else 4f
                // Upper finger
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -38f, cy = -6f + pinch, cz = 6f,
                    sizeX = 10f, sizeY = 3f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ + 0.2f,
                    baseColor = armColor,
                    outlineColor = Color.White
                )
                // Lower finger
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -38f, cy = 6f - pinch, cz = 6f,
                    sizeX = 10f, sizeY = 3f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ - 0.2f,
                    baseColor = armColor,
                    outlineColor = Color.White
                )
                // Central laser rangefinder lens
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -35f, cy = 0f, cz = 6f,
                    sizeX = 2f, sizeY = 2f, sizeZ = 2f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberRed
                )
            }
            "Magnet" -> {
                // Copper coil wraps + horseshoe poles
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -32f, cy = 0f, cz = 6f,
                    sizeX = 8f, sizeY = 12f, sizeZ = 12f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberSteel,
                    outlineColor = Color.Black
                )
                // Horseshoe red north pole
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -38f, cy = -5f, cz = 6f,
                    sizeX = 10f, sizeY = 5f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberRed,
                    outlineColor = Color.White
                )
                // Horseshoe silver south pole
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -38f, cy = 5f, cz = 6f,
                    sizeX = 10f, sizeY = 5f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberSteel,
                    outlineColor = Color.White
                )
                // Copper coil winding detailing
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -32f, cy = 0f, cz = 6f,
                    sizeX = 4f, sizeY = 10f, sizeZ = 10f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberOrange
                )
            }
        }
    }

    // 5. RIGHT TOOL ARM (Articulated forearm + customized components)
    if (rightArm != "Empty") {
        val swingFactor = if (progress >= 0.42f) -sin(progress * 38f) * 0.4f else 0f

        // Upper arm segment
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 21f, cy = -5f, cz = 0f,
            sizeX = 6f, sizeY = 8f, sizeZ = 8f,
            rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
            baseColor = armColor
        )
        // Silver forearm cylinder (Hydraulic feel)
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 28f, cy = 0f, cz = 4f,
            sizeX = 14f, sizeY = 6f, sizeZ = 6f,
            rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
            baseColor = CyberSteel,
            outlineColor = Color.White
        )
        // Active component tooling visualisations
        when (rightArm) {
            "Hammer" -> {
                // Mallet hammer block with Caution design
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 38f, cy = 0f, cz = 6f,
                    sizeX = 12f, sizeY = 22f, sizeZ = 12f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = Color(0xFF4B5563),
                    outlineColor = CyberGold
                )
                // Piston rod
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 32f, cy = 0f, cz = 6f,
                    sizeX = 4f, sizeY = 14f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = Color.White
                )
                // Yellow safety guard
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 38f, cy = 0f, cz = 12.2f,
                    sizeX = 8f, sizeY = 16f, sizeZ = 1f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberGold,
                    outlineColor = Color.Black
                )
            }
            "Drill" -> {
                val drillAngle = if (!isFailed) progress * 140f else 0f
                // Three-tier rotating drilling bit cone
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 34f, cy = 0f, cz = 6f,
                    sizeX = 14f, sizeY = 14f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ + drillAngle,
                    baseColor = CyberSteel,
                    outlineColor = Color.White
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 40f, cy = 0f, cz = 6f,
                    sizeX = 10f, sizeY = 10f, sizeZ = 8f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ - drillAngle * 1.5f,
                    baseColor = Color.Gray,
                    outlineColor = CyberBlue
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 45f, cy = 0f, cz = 6f,
                    sizeX = 6f, sizeY = 6f, sizeZ = 6f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ + drillAngle * 2f,
                    baseColor = CyberWhite,
                    outlineColor = CyberBlue
                )
            }
            "Shield Arm" -> {
                // Hexagonal protective crystal emitter
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 32f, cy = 0f, cz = 6f,
                    sizeX = 8f, sizeY = 12f, sizeZ = 12f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberIron,
                    outlineColor = CyberBlue
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 38f, cy = 0f, cz = 6f,
                    sizeX = 3f, sizeY = 32f, sizeZ = 32f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberBlue.copy(alpha = 0.6f),
                    outlineColor = Color.White
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 38.2f, cy = 0f, cz = 6f,
                    sizeX = 1f, sizeY = 16f, sizeZ = 16f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = Color.White.copy(alpha = 0.8f)
                )
            }
            "Welding Torch" -> {
                // Detailed high-temp brass gas nozzle
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 34f, cy = 0f, cz = 6f,
                    sizeX = 12f, sizeY = 6f, sizeZ = 6f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = Color.DarkGray,
                    outlineColor = CyberOrange
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 42f, cy = 0f, cz = 6f,
                    sizeX = 8f, sizeY = 4f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberGold,
                    outlineColor = Color.White
                )
                // Jet spark flame and glowing white plasma core
                if (!isFailed && progress >= 0.42f) {
                    val pulseFlame = 6f + abs(sin(progress * 45f)) * 8f
                    draw3DCuboid(
                        rx = rx, ry = ry,
                        cx = 48f, cy = 0f, cz = 6f,
                        sizeX = pulseFlame, sizeY = 4f, sizeZ = 4f,
                        rotX = rotX, rotY = rotY, rotZ = rotZ,
                        baseColor = CyberBlue,
                        outlineColor = Color.White
                    )
                    // Golden sparks emitting from welding tip
                    val torchTipPt = VectorMath.project(VectorMath.rotateY(VectorMath.rotateX(Point3D(48f, 0f, 6f), rotX), rotY), 350f, rx, ry)
                    drawCircle(Color.White, 3f, torchTipPt)
                    for (s in 1..4) {
                        val sx = torchTipPt.x + (s * 4f + abs(sin(progress * 90f * s)) * 12f)
                        val sy = torchTipPt.y + (sin(progress * 40f * s) * 10f)
                        drawCircle(CyberGold, 1.8f, Offset(sx, sy))
                    }
                }
            }
            "Grabber" -> {
                // Jointed base block
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 32f, cy = 0f, cz = 6f,
                    sizeX = 6f, sizeY = 10f, sizeZ = 10f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberIron
                )
                // Double articulated claws that open/close dynamically
                val pinch = if (!isFailed) abs(sin(progress * 15f)) * 8f else 4f
                // Upper finger
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 38f, cy = -6f + pinch, cz = 6f,
                    sizeX = 10f, sizeY = 3f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ + 0.2f,
                    baseColor = armColor,
                    outlineColor = Color.White
                )
                // Lower finger
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 38f, cy = 6f - pinch, cz = 6f,
                    sizeX = 10f, sizeY = 3f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ - 0.2f,
                    baseColor = armColor,
                    outlineColor = Color.White
                )
                // Central laser rangefinder lens
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 35f, cy = 0f, cz = 6f,
                    sizeX = 2f, sizeY = 2f, sizeZ = 2f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberRed
                )
            }
            "Magnet" -> {
                // Copper coil wraps + horseshoe poles
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 32f, cy = 0f, cz = 6f,
                    sizeX = 8f, sizeY = 12f, sizeZ = 12f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberSteel,
                    outlineColor = Color.Black
                )
                // Horseshoe red north pole
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 38f, cy = -5f, cz = 6f,
                    sizeX = 10f, sizeY = 5f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberRed,
                    outlineColor = Color.White
                )
                // Horseshoe silver south pole
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 38f, cy = 5f, cz = 6f,
                    sizeX = 10f, sizeY = 5f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberSteel,
                    outlineColor = Color.White
                )
                // Copper coil winding detailing
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 32f, cy = 0f, cz = 6f,
                    sizeX = 4f, sizeY = 10f, sizeZ = 10f,
                    rotX = rotX, rotY = rotY + swingFactor, rotZ = rotZ,
                    baseColor = CyberOrange
                )
            }
        }
    }

    // 6. ADVANCED MOBILITY LEGS SYSTEM
    val mobilityColor = if (isFailed) CyberGray else CyberIron
    val wheelSpin = if (!isFailed) progress * 15f else 0f

    when (legs) {
        "Wheels" -> {
            // Detailed wheel rubber tread outer block
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = -18f, cy = 20f, cz = 0f,
                sizeX = 8f, sizeY = 22f, sizeZ = 22f,
                rotX = rotX + wheelSpin, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFF1E293B),
                outlineColor = Color.Black
            )
            // Left alloy wheel hub insert with spokes
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = -19f, cy = 20f, cz = 0f,
                sizeX = 4f, sizeY = 12f, sizeZ = 12f,
                rotX = rotX + wheelSpin * 2.5f, rotY = rotY, rotZ = rotZ,
                baseColor = CyberSteel,
                outlineColor = Color.White
            )
            // Right detailed wheel block
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 18f, cy = 20f, cz = 0f,
                sizeX = 8f, sizeY = 22f, sizeZ = 22f,
                rotX = rotX + wheelSpin, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFF1E293B),
                outlineColor = Color.Black
            )
            // Right alloy wheel hub insert with spokes
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 19f, cy = 20f, cz = 0f,
                sizeX = 4f, sizeY = 12f, sizeZ = 12f,
                rotX = rotX + wheelSpin * 2.5f, rotY = rotY, rotZ = rotZ,
                baseColor = CyberSteel,
                outlineColor = Color.White
            )
            // Horizontal axle rod
            drawLine(mobilityColor, Offset(rx - 14f, ry + 20f), Offset(rx + 14f, ry + 20f), strokeWidth = 3f)
            
            // Suspension spring coils (red visual accent)
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = -14f, cy = 11f, cz = 0f,
                sizeX = 4f, sizeY = 12f, sizeZ = 4f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = CyberRed,
                outlineColor = Color.White
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 14f, cy = 11f, cz = 0f,
                sizeX = 4f, sizeY = 12f, sizeZ = 4f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = CyberRed,
                outlineColor = Color.White
            )
        }
        "Tank Tracks" -> {
            // Main caterpillar outer track block
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = -18f, cy = 18f, cz = 0f,
                sizeX = 10f, sizeY = 16f, sizeZ = 34f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFF1F2937),
                outlineColor = Color.Black
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 18f, cy = 18f, cz = 0f,
                sizeX = 10f, sizeY = 16f, sizeZ = 34f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFF1F2937),
                outlineColor = Color.Black
            )
            // Three rotating inner road wheel sprocket rollers
            for (j in listOf(-12f, 0f, 12f)) {
                val spinAngle = if (!isFailed) wheelSpin * 1.5f else 0f
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -18.5f, cy = 20f, cz = j,
                    sizeX = 4f, sizeY = 8f, sizeZ = 8f,
                    rotX = rotX + spinAngle, rotY = rotY, rotZ = rotZ,
                    baseColor = CyberSteel,
                    outlineColor = Color.Black
                )
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 18.5f, cy = 20f, cz = j,
                    sizeX = 4f, sizeY = 8f, sizeZ = 8f,
                    rotX = rotX + spinAngle, rotY = rotY, rotZ = rotZ,
                    baseColor = CyberSteel,
                    outlineColor = Color.Black
                )
            }
        }
        "Spider Legs" -> {
            // Multi-segment spider crawling limbs with pivot joint bolts
            for (step in listOf(-1, 0, 1)) {
                val stepOffset = step * 12f
                val activeLobe = if (!isFailed) sin(progress * 25f + step * 3.5f) * 8f else 0f
                
                // --- LEFT SIDE Crawlers ---
                // Thigh joint segment
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -18f, cy = 8f + activeLobe * 0.3f, cz = stepOffset,
                    sizeX = 8f, sizeY = 4f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ - 0.4f,
                    baseColor = CyberIron,
                    outlineColor = Color.Black
                )
                // Knee joint cap (gold)
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -24f, cy = 11f + activeLobe * 0.6f, cz = stepOffset,
                    sizeX = 5f, sizeY = 5f, sizeZ = 5f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ,
                    baseColor = CyberGold,
                    outlineColor = Color.White
                )
                // Shin segment
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = -26f, cy = 18f + activeLobe, cz = stepOffset,
                    sizeX = 4f, sizeY = 12f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ + 0.3f,
                    baseColor = mobilityColor,
                    outlineColor = Color.White
                )
                
                // --- RIGHT SIDE Crawlers ---
                // Thigh joint segment
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 18f, cy = 8f - activeLobe * 0.3f, cz = stepOffset,
                    sizeX = 8f, sizeY = 4f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ + 0.4f,
                    baseColor = CyberIron,
                    outlineColor = Color.Black
                )
                // Knee joint cap
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 24f, cy = 11f - activeLobe * 0.6f, cz = stepOffset,
                    sizeX = 5f, sizeY = 5f, sizeZ = 5f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ,
                    baseColor = CyberGold,
                    outlineColor = Color.White
                )
                // Shin segment
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 26f, cy = 18f - activeLobe, cz = stepOffset,
                    sizeX = 4f, sizeY = 12f, sizeZ = 4f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ - 0.3f,
                    baseColor = mobilityColor,
                    outlineColor = Color.White
                )
            }
        }
        "Hover Engine" -> {
            // Curved thrust nozzle bracket
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = 14f, cz = 0f,
                sizeX = 20f, sizeY = 10f, sizeZ = 20f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = mobilityColor,
                outlineColor = CyberBlue
            )
            // Metallic exhaust nozzle
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = 20f, cz = 0f,
                sizeX = 14f, sizeY = 4f, sizeZ = 14f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color.DarkGray,
                outlineColor = Color.White
            )
            // Floating plasma jet flame & concentric vector ripples
            if (!isFailed) {
                val plasmaSize = 10f + abs(sin(progress * 50f)) * 8f
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 0f, cy = 26f, cz = 0f,
                    sizeX = plasmaSize, sizeY = plasmaSize, sizeZ = plasmaSize,
                    rotX = rotX, rotY = rotY, rotZ = rotZ,
                    baseColor = CyberBlue.copy(alpha = 0.7f),
                    outlineColor = Color.White
                )
                
                // Hover anti-grav floor ripples
                val nozzleCenterPt = VectorMath.project(VectorMath.rotateY(VectorMath.rotateX(Point3D(0f, 24f, 0f), rotX), rotY), 350f, rx, ry)
                for (r in 1..2) {
                    val activeRadius = 14f * r + (progress * 60f) % 24f
                    drawCircle(
                        color = CyberBlue.copy(alpha = (1f - (activeRadius / 48f)).coerceIn(0f, 1f)),
                        radius = activeRadius,
                        center = nozzleCenterPt,
                        style = Stroke(width = 1.5f)
                    )
                }
            }
        }
        "Jump Springs" -> {
            // Helical steel spring layout with dampener plates
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = 11f, cz = 0f,
                sizeX = 16f, sizeY = 4f, sizeZ = 16f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = CyberSteel,
                outlineColor = Color.Black
            )
            val compress = if (!isFailed) 14f + sin(progress * 15f) * 6f else 18f
            val coilSegmentsCount = 4
            for (c in 0 until coilSegmentsCount) {
                val segmentHeight = compress / coilSegmentsCount
                val segmentY = 13f + c * segmentHeight
                val sizeOffset = if (c % 2 == 0) 14f else 11f
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = 0f, cy = segmentY, cz = 0f,
                    sizeX = sizeOffset, sizeY = 3f, sizeZ = sizeOffset,
                    rotX = rotX, rotY = rotY, rotZ = rotZ,
                    baseColor = CyberOrange,
                    outlineColor = Color.White
                )
            }
            // Spring damper heavy ground pad
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = 13f + compress, cz = 0f,
                sizeX = 18f, sizeY = 4f, sizeZ = 18f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color.DarkGray,
                outlineColor = Color.White
            )
        }
        else -> {
            // Standard static column placeholder
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = 18f, cz = 0f,
                sizeX = 6f, sizeY = 14f, sizeZ = 6f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = mobilityColor
            )
        }
    }

    // 7. ROBOT BODY CORE (Solid 3D Cuboid Hull)
    if (torsoStyle == "Crewmate Suit") {
        // Main rounded Crewmate body capsule
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 0f, cy = -5f, cz = 0f,
            sizeX = 28f, sizeY = 40f, sizeZ = 28f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = finalBodyColor,
            outlineColor = Color.Black
        )
        // Oxygen backpack canister
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 0f, cy = -5f, cz = -15f,
            sizeX = 12f, sizeY = 28f, sizeZ = 22f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = finalBodyColor,
            outlineColor = Color.Black
        )
    } else {
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 0f, cy = -5f, cz = 0f,
            sizeX = 34f, sizeY = 36f, sizeZ = 34f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = finalBodyColor
        )

        // 8. COGNITIVE ARC ENERGY CORE REACTOR (Glow center on chest)
        val corePt = VectorMath.rotateY(VectorMath.rotateX(Point3D(0f, -4f, 18.2f), rotX), rotY)
        val coreScreen = VectorMath.project(corePt, 350f, rx, ry)
        val corePulse = 7f + abs(sin(progress * 40f)) * 4f
        drawCircle(
            color = if (isFailed) Color.DarkGray else CyberBlue,
            radius = corePulse,
            center = coreScreen
        )
        drawCircle(
            color = if (isFailed) Color.Gray else Color.White,
            radius = corePulse / 2f,
            center = coreScreen
        )

        // 9. CYBER CIRCUITS PCB INTEGRATION PATHS (Fine copper nodes on chest)
        if (!isFailed) {
            val nodeL1 = VectorMath.project(VectorMath.rotateY(VectorMath.rotateX(Point3D(-12f, -18f, 17.5f), rotX), rotY), 350f, rx, ry)
            val nodeL2 = VectorMath.project(VectorMath.rotateY(VectorMath.rotateX(Point3D(-6f, -12f, 17.5f), rotX), rotY), 350f, rx, ry)
            val nodeR1 = VectorMath.project(VectorMath.rotateY(VectorMath.rotateX(Point3D(12f, -18f, 17.5f), rotX), rotY), 350f, rx, ry)
            val nodeR2 = VectorMath.project(VectorMath.rotateY(VectorMath.rotateX(Point3D(6f, -12f, 17.5f), rotX), rotY), 350f, rx, ry)
            
            drawLine(CyberLime.copy(alpha = 0.5f), nodeL1, nodeL2, strokeWidth = 1.5f)
            drawLine(CyberLime.copy(alpha = 0.5f), nodeR1, nodeR2, strokeWidth = 1.5f)
            drawCircle(CyberLime, 2.5f, nodeL2)
            drawCircle(CyberLime, 2.5f, nodeR2)
        }

        // 10. CHEST LED PANEL TELEMETRY / BATTERY READOUT
        val ledColor = if (isFailed) Color.DarkGray else CyberBlue.copy(alpha = 0.9f)
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 0f, cy = -4f, cz = 17f,
            sizeX = 22f, sizeY = 20f, sizeZ = 3f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = ledColor,
            outlineColor = if (isFailed) Color.Black else Color.White
        )
        
        // Live battery discharge animation bar on the chest screen
        if (!isFailed) {
            val batStartPt = VectorMath.project(VectorMath.rotateY(VectorMath.rotateX(Point3D(-8f, 2f, 18.2f), rotX), rotY), 350f, rx, ry)
            val batEndPt = VectorMath.project(VectorMath.rotateY(VectorMath.rotateX(Point3D(8f, 2f, 18.2f), rotX), rotY), 350f, rx, ry)
            drawLine(CyberLime.copy(alpha = 0.3f), batStartPt, batEndPt, strokeWidth = 4f)
            val activeFraction = if (state is SimulationState.Running) (1f - progress).coerceIn(0f, 1f) else 0.8f
            val activeEnd = Offset(
                batStartPt.x + (batEndPt.x - batStartPt.x) * activeFraction,
                batStartPt.y + (batEndPt.y - batStartPt.y) * activeFraction
            )
            drawLine(CyberLime, batStartPt, activeEnd, strokeWidth = 4f)
        }
    }

    // 11. ROBOT HEAD
    if (headStyle == "Crewmate Visor") {
        // Main rounded helmet head
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 0f, cy = -32f, cz = 0f,
            sizeX = 22f, sizeY = 22f, sizeZ = 22f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = finalBodyColor,
            outlineColor = Color.Black
        )
        // Big glass cyan visor
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 0f, cy = -32f, cz = 11f,
            sizeX = 16f, sizeY = 10f, sizeZ = 4f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = Color(0xFF80DEEA), // cyan visor color
            outlineColor = Color.Black
        )
        // Visor glass reflection highlight
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = -4f, cy = -34f, cz = 13.1f,
            sizeX = 4f, sizeY = 3f, sizeZ = 1f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = Color.White
        )
    } else {
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 0f, cy = -32f, cz = 0f,
            sizeX = 24f, sizeY = 18f, sizeZ = 24f,
            rotX = rotX, rotY = rotY, rotZ = rotZ,
            baseColor = finalBodyColor
        )

        // 12. CYBERNETIC EYES
        if (!isFailed) {
            val eyeColor = when (eyesType) {
                "laser" -> CyberRed
                "retro" -> CyberOrange
                "glass" -> CyberWhite
                else -> CyberBlue
            }
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = -6f, cy = -32f, cz = 12f,
                sizeX = 4f, sizeY = 4f, sizeZ = 2f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = eyeColor,
                outlineColor = Color.White
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 6f, cy = -32f, cz = 12f,
                sizeX = 4f, sizeY = 4f, sizeZ = 2f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = eyeColor,
                outlineColor = Color.White
            )

            if (eyesType == "laser" && state is SimulationState.Running) {
                val laserStartPt = VectorMath.rotateY(VectorMath.rotateX(Point3D(0f, -32f, 13f), rotX), rotY)
                val laserEndPt = VectorMath.rotateY(VectorMath.rotateX(Point3D(0f, -32f, 180f), rotX), rotY)
                
                val pStart = VectorMath.project(laserStartPt, 350f, rx, ry)
                val pEnd = VectorMath.project(laserEndPt, 350f, rx, ry)
                
                drawLine(
                    color = CyberRed,
                    start = pStart,
                    end = pEnd,
                    strokeWidth = 3f
                )
                drawCircle(Color.White, 4f, pStart)
            }
        } else {
            val leftEyeC = VectorMath.project(VectorMath.rotateY(VectorMath.rotateX(Point3D(-6f, -32f, 12f), rotX), rotY), 350f, rx, ry)
            val rightEyeC = VectorMath.project(VectorMath.rotateY(VectorMath.rotateX(Point3D(6f, -32f, 12f), rotX), rotY), 350f, rx, ry)
            
            drawLine(CyberRed, Offset(leftEyeC.x - 4f, leftEyeC.y - 4f), Offset(leftEyeC.x + 4f, leftEyeC.y + 4f), strokeWidth = 2.2f)
            drawLine(CyberRed, Offset(leftEyeC.x + 4f, leftEyeC.y - 4f), Offset(leftEyeC.x - 4f, leftEyeC.y + 4f), strokeWidth = 2.2f)

            drawLine(CyberRed, Offset(rightEyeC.x - 4f, rightEyeC.y - 4f), Offset(rightEyeC.x + 4f, rightEyeC.y + 4f), strokeWidth = 2.2f)
            drawLine(CyberRed, Offset(rightEyeC.x + 4f, rightEyeC.y - 4f), Offset(rightEyeC.x - 4f, rightEyeC.y + 4f), strokeWidth = 2.2f)
        }
    }

    // 13. HATS ON HEAD (3D voxel style)
    when (hatType) {
        "builder" -> {
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -41f, cz = 0f,
                sizeX = 28f, sizeY = 2f, sizeZ = 28f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = CyberGold
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -45f, cz = 0f,
                sizeX = 18f, sizeY = 7f, sizeZ = 18f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = CyberGold,
                outlineColor = Color.White
            )
        }
        "top_hat" -> {
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -41f, cz = 0f,
                sizeX = 28f, sizeY = 2f, sizeZ = 28f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFF1F2937)
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -51f, cz = 0f,
                sizeX = 18f, sizeY = 18f, sizeZ = 18f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color.Black,
                outlineColor = Color.DarkGray
            )
        }
        "builder_crown" -> {
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -42f, cz = 0f,
                sizeX = 24f, sizeY = 3f, sizeZ = 24f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = CyberGold
            )
            for (step in listOf(-10f, 0f, 10f)) {
                draw3DCuboid(
                    rx = rx, ry = ry,
                    cx = step, cy = -46f, cz = 10f,
                    sizeX = 4f, sizeY = 6f, sizeZ = 2f,
                    rotX = rotX, rotY = rotY, rotZ = rotZ,
                    baseColor = CyberGold,
                    outlineColor = Color.White
                )
            }
        }
        "sprout" -> {
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -44f, cz = 0f,
                sizeX = 2f, sizeY = 8f, sizeZ = 2f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFF5D4037)
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = -6f, cy = -47f, cz = 0f,
                sizeX = 10f, sizeY = 3f, sizeZ = 6f,
                rotX = rotX, rotY = rotY, rotZ = rotZ + 0.5f,
                baseColor = Color(0xFF4CAF50),
                outlineColor = Color.Black
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 6f, cy = -47f, cz = 0f,
                sizeX = 10f, sizeY = 3f, sizeZ = 6f,
                rotX = rotX, rotY = rotY, rotZ = rotZ - 0.5f,
                baseColor = Color(0xFF4CAF50),
                outlineColor = Color.Black
            )
        }
        "toilet_paper" -> {
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -46f, cz = 0f,
                sizeX = 14f, sizeY = 12f, sizeZ = 14f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFFEEEEEE),
                outlineColor = Color.Black
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -46f, cz = 0f,
                sizeX = 4f, sizeY = 12.2f, sizeZ = 4f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFF8D6E63)
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -38f, cz = -8f,
                sizeX = 12f, sizeY = 8f, sizeZ = 1f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFFEEEEEE),
                outlineColor = Color.Black
            )
        }
        "egg" -> {
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -42f, cz = 0f,
                sizeX = 26f, sizeY = 2f, sizeZ = 24f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFFF5F5F5),
                outlineColor = Color.Black
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = -2f, cy = -44f, cz = 2f,
                sizeX = 10f, sizeY = 4f, sizeZ = 10f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFFFFC107),
                outlineColor = Color.Black
            )
        }
        "sticky_note" -> {
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 2f, cy = -32f, cz = 12.5f,
                sizeX = 16f, sizeY = 16f, sizeZ = 1f,
                rotX = rotX, rotY = rotY, rotZ = rotZ - 0.15f,
                baseColor = Color(0xFFFFEB3B),
                outlineColor = Color.Black
            )
        }
        "cherry" -> {
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -46f, cz = 0f,
                sizeX = 12f, sizeY = 12f, sizeZ = 12f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFFD32F2F),
                outlineColor = Color.Black
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 4f, cy = -54f, cz = 2f,
                sizeX = 2f, sizeY = 10f, sizeZ = 2f,
                rotX = rotX, rotY = rotY, rotZ = rotZ + 0.3f,
                baseColor = Color(0xFF388E3C)
            )
        }
        "plunger" -> {
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -44f, cz = 0f,
                sizeX = 16f, sizeY = 6f, sizeZ = 16f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFFC62828),
                outlineColor = Color.Black
            )
            draw3DCuboid(
                rx = rx, ry = ry,
                cx = 0f, cy = -53f, cz = 0f,
                sizeX = 2f, sizeY = 14f, sizeZ = 2f,
                rotX = rotX, rotY = rotY, rotZ = rotZ,
                baseColor = Color(0xFFD7CCC8),
                outlineColor = Color.Black
            )
        }
    }

    // 14. CARRIED CRATE PUZZLE PAYLOAD
    val carriesCrate = progress >= 0.45f && (state is SimulationState.Success || (state is SimulationState.Running && progress >= 0.45f))
    if (carriesCrate && (leftArm == "Grabber" || rightArm == "Grabber" || leftArm == "Magnet" || rightArm == "Magnet")) {
        draw3DCuboid(
            rx = rx, ry = ry,
            cx = 24f, cy = 4f, cz = 12f,
            sizeX = 18f, sizeY = 18f, sizeZ = 18f,
            rotX = rotX + 0.1f, rotY = rotY, rotZ = rotZ,
            baseColor = Color(0xFFC2410C),
            outlineColor = Color.Yellow
        )
    }
}

fun DrawScope.drawLegsModule(legs: String, progress: Float, isFailed: Boolean) {
    val legColor = if (isFailed) CyberGray else CyberIron
    val animatedAngle = progress * 720f

    when (legs) {
        "Wheels" -> {
            // Draw two rotating wheels
            rotate(degrees = animatedAngle, pivot = Offset(-10f, 20f)) {
                drawCircle(Color.Black, 11f, Offset(-10f, 20f))
                drawCircle(CyberWhite, 3f, Offset(-10f, 20f))
                drawLine(CyberWhite, Offset(-10f, 9f), Offset(-10f, 31f), strokeWidth = 1.5f)
            }
            rotate(degrees = animatedAngle, pivot = Offset(10f, 20f)) {
                drawCircle(Color.Black, 11f, Offset(10f, 20f))
                drawCircle(CyberWhite, 3f, Offset(10f, 20f))
                drawLine(CyberWhite, Offset(10f, 9f), Offset(10f, 31f), strokeWidth = 1.5f)
            }
        }
        "Tank Tracks" -> {
            // Continuous tread outline
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(-22f, 10f),
                size = Size(44f, 14f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Treads gears inside
            drawCircle(legColor, 4f, Offset(-14f, 17f))
            drawCircle(legColor, 4f, Offset(0f, 17f))
            drawCircle(legColor, 4f, Offset(14f, 17f))
        }
        "Spider Legs" -> {
            // Multi jointed walk lines alternating
            val phase = sin(progress * 40f) * 8f
            // Front leg
            drawLine(legColor, Offset(-10f, 10f), Offset(-18f - phase, 16f), strokeWidth = 3f)
            drawLine(legColor, Offset(-18f - phase, 16f), Offset(-24f - phase, 24f), strokeWidth = 3f)
            // Back leg
            drawLine(legColor, Offset(10f, 10f), Offset(18f + phase, 16f), strokeWidth = 3f)
            drawLine(legColor, Offset(18f + phase, 16f), Offset(24f + phase, 24f), strokeWidth = 3f)
        }
        "Jump Springs" -> {
            // Spring coils expanding/compressing
            val springCompress = 5f + abs(sin(progress * 15f)) * 8f
            drawLine(legColor, Offset(-8f, 10f), Offset(8f, 10f), strokeWidth = 3f)
            // Coil zigzags
            drawLine(legColor, Offset(0f, 10f), Offset(-6f, 10f + springCompress / 3), strokeWidth = 2.5f)
            drawLine(legColor, Offset(-6f, 10f + springCompress / 3), Offset(6f, 10f + 2 * springCompress / 3), strokeWidth = 2.5f)
            drawLine(legColor, Offset(6f, 10f + 2 * springCompress / 3), Offset(-4f, 10f + springCompress), strokeWidth = 2.5f)
            // Base plate
            drawRoundRect(Color.Black, Offset(-12f, 10f + springCompress), Size(24f, 4f), CornerRadius(1f, 1f))
        }
        "Hover Engine" -> {
            // Floating thruster nozzle emitting blue plasma circles
            drawRoundRect(legColor, Offset(-12f, 10f), Size(24f, 8f), CornerRadius(2f, 2f))
            val pulse = abs(sin(progress * 30f)) * 4f
            drawCircle(
                color = CyberBlue.copy(alpha = 0.5f),
                radius = 6f + pulse,
                center = Offset(0f, 20f)
            )
        }
        "Jetpack" -> {
            // Twin canisters on back emitting active flame exhaust
            drawRoundRect(legColor, Offset(-22f, -10f), Size(10f, 24f), CornerRadius(2f, 2f))
            if (!isFailed) {
                // Rocket exhaust cones
                val fl = 10f + abs(sin(progress * 35f)) * 10f
                val flamePath = Path().apply {
                    moveTo(-21f, 14f)
                    lineTo(-17f, 14f + fl)
                    lineTo(-13f, 14f)
                    close()
                }
                drawPath(flamePath, CyberOrange)
            }
        }
        else -> {
            // Simple peg stand
            drawLine(legColor, Offset(0f, 10f), Offset(0f, 22f), strokeWidth = 4f)
            drawCircle(legColor, 3f, Offset(0f, 22f))
        }
    }
}

fun DrawScope.drawLeftToolArm(arm: String, progress: Float, isFailed: Boolean) {
    val armColor = if (isFailed) CyberGray else CyberIron
    val activeAngle = if (progress >= 0.45f) sin(progress * 40f) * 30f else 0f

    // Draw left shoulder to arm connector (facing back/leftward)
    rotate(degrees = activeAngle, pivot = Offset(-18f, 0f)) {
        drawLine(armColor, Offset(-18f, 0f), Offset(-32f, -5f), strokeWidth = 4f)

        // Draw tool end heads
        when (arm) {
            "Grabber" -> {
                // Double claw
                drawLine(armColor, Offset(-32f, -5f), Offset(-38f, -12f), strokeWidth = 2.5f)
                drawLine(armColor, Offset(-32f, -5f), Offset(-38f, 2f), strokeWidth = 2.5f)
            }
            "Magnet" -> {
                // Horseshoe red/silver magnet
                drawCircle(CyberRed, 6f, Offset(-36f, -5f), style = Stroke(width = 3f))
            }
            "Hammer" -> {
                // Heavy grey hammer head block
                drawRoundRect(Color.Gray, Offset(-42f, -14f), Size(12f, 18f), CornerRadius(2f, 2f))
            }
            "Drill" -> {
                // Spiral striped cone
                val drillPath = Path().apply {
                    moveTo(-32f, -5f)
                    lineTo(-44f, -11f)
                    lineTo(-44f, 1f)
                    close()
                }
                drawPath(drillPath, Color.Gray)
            }
            "Welding Torch" -> {
                // Tiny nozzle with flickering blue flame
                drawLine(Color.Gray, Offset(-32f, -5f), Offset(-38f, -7f), strokeWidth = 2f)
                if (progress >= 0.45f) {
                    drawCircle(CyberBlue, 4f, Offset(-42f, -8f))
                }
            }
            "Shield Arm" -> {
                // Round composite energy shield disk
                drawCircle(CyberBlue, 12f, Offset(-32f, -5f), style = Stroke(width = 2f))
            }
            else -> {}
        }
    }
}

fun DrawScope.drawRightToolArm(arm: String, progress: Float, isFailed: Boolean) {
    val armColor = if (isFailed) CyberGray else CyberIron
    val activeAngle = if (progress >= 0.45f) -sin(progress * 40f) * 30f else 0f

    // Draw right shoulder to arm connector (facing frontward/rightward)
    rotate(degrees = activeAngle, pivot = Offset(18f, 0f)) {
        drawLine(armColor, Offset(18f, 0f), Offset(32f, -5f), strokeWidth = 4f)

        when (arm) {
            "Grabber" -> {
                drawLine(armColor, Offset(32f, -5f), Offset(38f, -12f), strokeWidth = 2.5f)
                drawLine(armColor, Offset(32f, -5f), Offset(38f, 2f), strokeWidth = 2.5f)
            }
            "Magnet" -> {
                drawCircle(CyberRed, 6f, Offset(36f, -5f), style = Stroke(width = 3f))
            }
            "Hammer" -> {
                drawRoundRect(Color.Gray, Offset(30f, -14f), Size(12f, 18f), CornerRadius(2f, 2f))
            }
            "Drill" -> {
                val drillPath = Path().apply {
                    moveTo(32f, -5f)
                    lineTo(44f, -11f)
                    lineTo(44f, 1f)
                    close()
                }
                drawPath(drillPath, Color.Gray)
            }
            "Welding Torch" -> {
                drawLine(Color.Gray, Offset(32f, -5f), Offset(38f, -7f), strokeWidth = 2f)
                if (progress >= 0.45f) {
                    drawCircle(CyberBlue, 4f, Offset(42f, -8f))
                }
            }
            "Shield Arm" -> {
                drawCircle(CyberBlue, 12f, Offset(32f, -5f), style = Stroke(width = 2f))
            }
            else -> {}
        }
    }
}


fun DrawScope.drawCustomWorldObstacles(obstaclesLayout: String, progress: Float, state: SimulationState) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val groundY = canvasHeight - 40f
    
    val obstacles = obstaclesLayout.split(",").filter { it.contains(":") }
    obstacles.forEach { obs ->
        val split = obs.split(":")
        val type = split[0]
        val percent = split[1].toFloatOrNull() ?: 0.5f
        val startX = canvasWidth * percent
        
        when (type) {
            "lava" -> {
                val poolWidth = 80f
                val drawX = startX - poolWidth / 2f
                drawRect(
                    color = Color(0xFFFF3D00),
                    topLeft = Offset(drawX, groundY - 6f),
                    size = Size(poolWidth, 24f)
                )
                drawRect(
                    color = Color(0xFFFFC400),
                    topLeft = Offset(drawX + 8f, groundY - 4f),
                    size = Size(poolWidth - 16f, 6f)
                )
            }
            "river" -> {
                val poolWidth = 80f
                val drawX = startX - poolWidth / 2f
                drawRect(
                    color = Color(0xFF2979FF),
                    topLeft = Offset(drawX, groundY - 6f),
                    size = Size(poolWidth, 24f)
                )
            }
            "quicksand" -> {
                val poolWidth = 70f
                val drawX = startX - poolWidth / 2f
                drawRect(
                    color = Color(0xFF8D6E63),
                    topLeft = Offset(drawX, groundY - 6f),
                    size = Size(poolWidth, 22f)
                )
            }
            "laser" -> {
                val isBlocked = progress >= percent && state is SimulationState.Running && !state.javaClass.name.contains("Failure")
                if (isBlocked) {
                    drawLine(Color(0xFFFF1744), Offset(startX, 0f), Offset(startX, groundY - 120f), strokeWidth = 6f)
                    drawCircle(Color.White, 8f, Offset(startX, groundY - 120f))
                } else {
                    drawLine(Color(0xFFFF1744), Offset(startX, 0f), Offset(startX, groundY), strokeWidth = 6f)
                }
            }
            "wall" -> {
                val wallWidth = 30f
                val wallHeight = 100f
                val drawX = startX - wallWidth / 2f
                val isDestroyed = progress > percent
                if (!isDestroyed) {
                    drawRoundRect(
                        color = Color(0xFF757575),
                        topLeft = Offset(drawX, groundY - wallHeight),
                        size = Size(wallWidth, wallHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawLine(Color.Black, Offset(drawX, groundY - 50f), Offset(drawX + wallWidth, groundY - 50f), strokeWidth = 1.5f)
                    drawLine(Color.Black, Offset(drawX + 15f, groundY - wallHeight), Offset(drawX + 15f, groundY - 50f), strokeWidth = 1.5f)
                    drawLine(Color.Black, Offset(drawX + 10f, groundY - 50f), Offset(drawX + 10f, groundY), strokeWidth = 1.5f)
                } else {
                    drawCircle(Color(0xFF757575), 10f, Offset(startX - 15f, groundY - 10f))
                    drawCircle(Color(0xFF757575), 8f, Offset(startX + 15f, groundY - 8f))
                }
            }
        }
    }
}

@Composable
fun CustomLevelSelectScreen(viewModel: GameViewModel) {
    val customLevels by viewModel.customLevelsList.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SANDBOX WORKSHOP",
                    color = CyberLime,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Build and play custom mechanical puzzles.",
                    color = CyberGray,
                    fontSize = 12.sp
                )
            }
            
            Button(
                onClick = { viewModel.onScreenChanged(Screen.CustomLevelEditor) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberLime),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create", tint = CyberObsidian)
                Spacer(modifier = Modifier.width(4.dp))
                Text("NEW PUZZLE", color = CyberObsidian, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (customLevels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, CyberGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .background(CyberSteel)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Architecture, contentDescription = "No Levels", tint = CyberGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("NO CUSTOM PUZZLES DETECTED", color = CyberWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Click 'NEW PUZZLE' above to invent your first challenge!", color = CyberGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(customLevels) { level ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSteel),
                        border = BorderStroke(1.dp, if (level.completed) CyberLime.copy(alpha = 0.4f) else CyberBlue.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = level.title.uppercase(),
                                        color = CyberWhite,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "By ${level.authorName} • Difficulty: ${level.difficulty}",
                                        color = when (level.difficulty) {
                                            "Easy" -> CyberLime
                                            "Medium" -> CyberBlue
                                            "Hard" -> CyberOrange
                                            else -> CyberRed
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    for (i in 1..3) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Star",
                                            tint = if (i <= level.starsEarned) CyberGold else CyberGray.copy(alpha = 0.3f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = level.description,
                                color = CyberGray,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Weight limit: ${level.maxWeight}kg • Power budget: ${level.maxEnergyCost}W",
                                    color = CyberBlue.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { viewModel.deleteCustomLevel(level.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberRed.copy(alpha = 0.7f))
                                    }
                                    
                                    Button(
                                        onClick = { viewModel.selectCustomLevel(level.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (level.completed) CyberLime else CyberBlue),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (level.completed) "REPLAY" else "PLAY TEST",
                                            color = CyberObsidian,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomLevelEditorScreen(viewModel: GameViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("Designer Bot") }
    var difficulty by remember { mutableStateOf("Medium") }
    
    var maxWeight by remember { mutableStateOf(60f) }
    var maxEnergyCost by remember { mutableStateOf(100f) }
    var batteryCapacity by remember { mutableStateOf(1200f) }
    
    var includeLava by remember { mutableStateOf(false) }
    var includeRiver by remember { mutableStateOf(false) }
    var includeQuicksand by remember { mutableStateOf(false) }
    var includeLaser by remember { mutableStateOf(false) }
    var includeWall by remember { mutableStateOf(true) }
    
    var objAviation by remember { mutableStateOf(true) }
    var objDemolition by remember { mutableStateOf(false) }
    var objShielding by remember { mutableStateOf(false) }
    var objUltralight by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.onScreenChanged(Screen.CustomLevelSelect) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CyberBlue)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PUZZLE LAB COMPOSER",
                color = CyberBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSteel),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("1. BASIC METADATA", color = CyberLime, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Level Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CyberWhite,
                        unfocusedTextColor = CyberWhite,
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = CyberGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mission Description & Objective Hint") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CyberWhite,
                        unfocusedTextColor = CyberWhite,
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = CyberGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CyberWhite,
                        unfocusedTextColor = CyberWhite,
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = CyberGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Sector Hazard Rating:", color = CyberWhite, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val levels = listOf("Easy", "Medium", "Hard", "Expert")
                    levels.forEach { level ->
                        val isSelected = difficulty == level
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, if (isSelected) CyberLime else CyberGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .background(if (isSelected) CyberLime.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { difficulty = level },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level.uppercase(),
                                color = if (isSelected) CyberLime else CyberGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSteel),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("2. PHYSICAL DIAGNOSTICS & LIMITS", color = CyberLime, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Maximum Chassis Weight Allowed:", color = CyberWhite, fontSize = 13.sp)
                        Text("${maxWeight.toInt()} kg", color = CyberLime, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = maxWeight,
                        onValueChange = { maxWeight = it },
                        valueRange = 30f..100f,
                        colors = SliderDefaults.colors(thumbColor = CyberLime, activeTrackColor = CyberLime, inactiveTrackColor = CyberGray)
                    )
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Maximum Energy Draw Tolerated:", color = CyberWhite, fontSize = 13.sp)
                        Text("${maxEnergyCost.toInt()} Units", color = CyberBlue, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = maxEnergyCost,
                        onValueChange = { maxEnergyCost = it },
                        valueRange = 50f..150f,
                        colors = SliderDefaults.colors(thumbColor = CyberBlue, activeTrackColor = CyberBlue, inactiveTrackColor = CyberGray)
                    )
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Available Battery Capacity Budget:", color = CyberWhite, fontSize = 13.sp)
                        Text("${batteryCapacity.toInt()} mAh", color = CyberOrange, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = batteryCapacity,
                        onValueChange = { batteryCapacity = it },
                        valueRange = 600f..2000f,
                        colors = SliderDefaults.colors(thumbColor = CyberOrange, activeTrackColor = CyberOrange, inactiveTrackColor = CyberGray)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSteel),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("3. SECTOR OBSTACLE PLACEMENT", color = CyberLime, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Text("Select hazards to position along the automatic simulation route:", color = CyberGray, fontSize = 12.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { includeRiver = !includeRiver }) {
                    Checkbox(checked = includeRiver, onCheckedChange = { includeRiver = it }, colors = CheckboxDefaults.colors(checkedColor = CyberBlue))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Liquid Water Trench (Requires Hover Engine / Jetpack / Springs)", color = CyberWhite, fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { includeLava = !includeLava }) {
                    Checkbox(checked = includeLava, onCheckedChange = { includeLava = it }, colors = CheckboxDefaults.colors(checkedColor = CyberRed))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Boiling Magma Pit (Requires Jetpack or Cooling System)", color = CyberWhite, fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { includeQuicksand = !includeQuicksand }) {
                    Checkbox(checked = includeQuicksand, onCheckedChange = { includeQuicksand = it }, colors = CheckboxDefaults.colors(checkedColor = CyberOrange))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Swallowing Quicksand (Requires Spider Legs or Hover/Jetpack)", color = CyberWhite, fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { includeLaser = !includeLaser }) {
                    Checkbox(checked = includeLaser, onCheckedChange = { includeLaser = it }, colors = CheckboxDefaults.colors(checkedColor = CyberRed))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thermal Laser Barrier (Requires Shield Arm or Cooling System)", color = CyberWhite, fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { includeWall = !includeWall }) {
                    Checkbox(checked = includeWall, onCheckedChange = { includeWall = it }, colors = CheckboxDefaults.colors(checkedColor = CyberWhite))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Heavy Concrete Firewall (Requires Hammer, Drill, or Torch)", color = CyberWhite, fontSize = 13.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSteel),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("4. COMPLEX VERIFICATION GOALS", color = CyberLime, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Text("Define goals players can achieve in multiple ways to solve the puzzle:", color = CyberGray, fontSize = 12.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { objAviation = !objAviation }) {
                    Checkbox(checked = objAviation, onCheckedChange = { objAviation = it }, colors = CheckboxDefaults.colors(checkedColor = CyberLime))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aerial Flight Goal (Requires Jetpack or Hover Engine)", color = CyberWhite, fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { objDemolition = !objDemolition }) {
                    Checkbox(checked = objDemolition, onCheckedChange = { objDemolition = it }, colors = CheckboxDefaults.colors(checkedColor = CyberLime))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Demolition Goal (Requires Hammer, Drill, or Torch)", color = CyberWhite, fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { objShielding = !objShielding }) {
                    Checkbox(checked = objShielding, onCheckedChange = { objShielding = it }, colors = CheckboxDefaults.colors(checkedColor = CyberLime))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Energy Deflector Goal (Requires Shield Arm or Cooling)", color = CyberWhite, fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { objUltralight = !objUltralight }) {
                    Checkbox(checked = objUltralight, onCheckedChange = { objUltralight = it }, colors = CheckboxDefaults.colors(checkedColor = CyberLime))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lightweight Integrity Goal (Keep total weight under 40kg)", color = CyberWhite, fontSize = 13.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (title.isBlank()) return@Button
                
                val obstaclesList = mutableListOf<String>()
                if (includeRiver) obstaclesList.add("river:0.25")
                if (includeLava) obstaclesList.add("lava:0.4")
                if (includeQuicksand) obstaclesList.add("quicksand:0.55")
                if (includeLaser) obstaclesList.add("laser:0.7")
                if (includeWall) obstaclesList.add("wall:0.85")
                val obstaclesString = if (obstaclesList.isEmpty()) "" else obstaclesList.joinToString(",")
                
                val goalsList = mutableListOf<String>()
                if (objAviation) {
                    goalsList.add("Airflow Lift|Hover or Jetpack high above the course|LEG:Jetpack,Hover Engine")
                }
                if (objDemolition) {
                    goalsList.add("Rupture Structure|Use dense Hammer, core Drill, or Welding Torch|ARM:Hammer,Drill,Welding Torch")
                }
                if (objShielding) {
                    goalsList.add("Absorb Shockwave|Equip a physical Shield Arm or a Cooling System|ARM:Shield Arm,UTIL:Cooling System")
                }
                if (objUltralight) {
                    goalsList.add("Featherweight Build|Maintain structural weight under 40kg to save energy|WEIGHT:40")
                }
                
                if (goalsList.isEmpty()) {
                    goalsList.add("Mission Traverse|Complete full traversal under physics boundaries|WEIGHT:100")
                }
                val goalsString = goalsList.joinToString(";")
                
                val newLevel = CustomLevel(
                    title = title,
                    description = description.ifBlank { "Traverse custom sector with physics constraints and objective goals." },
                    authorName = author.ifBlank { "Anon Bot" },
                    difficulty = difficulty,
                    maxWeight = maxWeight.toInt(),
                    maxEnergyCost = maxEnergyCost.toInt(),
                    batteryCapacity = batteryCapacity.toInt(),
                    obstaclesLayout = obstaclesString,
                    objectiveGoals = goalsString
                )
                
                viewModel.saveCustomLevel(newLevel)
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberLime),
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "COMPILE & TRANSMIT TO SECTOR LOBBY",
                color = CyberObsidian,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun CustomLevelPlayScreen(
    viewModel: GameViewModel,
    profile: UserProfile?
) {
    val level = viewModel.selectedCustomLevel ?: return
    var activeSlot by remember { mutableStateOf<SlotType?>(null) }
    var playIn3D by remember { mutableStateOf(true) }
    
    val totalWeight = viewModel.getPartWeight(viewModel.selectedLegs) + 
            viewModel.getPartWeight(viewModel.selectedLeftArm) + 
            viewModel.getPartWeight(viewModel.selectedRightArm) + 
            viewModel.getPartWeight(viewModel.selectedUtility)
            
    val totalPower = viewModel.getPartPower(viewModel.selectedLegs) + 
            viewModel.getPartPower(viewModel.selectedLeftArm) + 
            viewModel.getPartPower(viewModel.selectedRightArm) + 
            viewModel.getPartPower(viewModel.selectedUtility)

    val isWeightOver = totalWeight > level.maxWeight
    val isPowerOver = totalPower > level.maxEnergyCost

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberObsidian)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.onScreenChanged(Screen.CustomLevelSelect) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CyberBlue)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.title.uppercase(),
                    color = CyberWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Custom Puzzle Sector • Designed by ${level.authorName}",
                    color = CyberGray,
                    fontSize = 12.sp
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSteel),
            border = BorderStroke(1.dp, CyberBlue.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = level.description,
                color = CyberWhite,
                fontSize = 13.sp,
                modifier = Modifier.padding(12.dp)
            )
        }

        // Custom Simulator Mode Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PUZZLE SIMULATOR",
                color = CyberBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberSteel)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(false to "2D Hologram", true to "Immersive 3D").forEach { (is3D, label) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (playIn3D == is3D) CyberBlue else Color.Transparent)
                            .clickable { playIn3D = is3D }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (playIn3D == is3D) CyberWhite else CyberGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live Custom Simulation / WebGL Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, if (playIn3D) CyberBlue else CyberIron, RoundedCornerShape(12.dp))
                .background(Color(0xFF070A13))
        ) {
            if (playIn3D) {
                val parsedHazard = level.obstaclesLayout.split(",").firstOrNull()?.split(":")?.firstOrNull() ?: "none"
                ThreeGameplayViewer(
                    robotX = 0f,
                    robotY = if (viewModel.selectedLegs == "Hover Engine" || viewModel.selectedLegs == "Jetpack") 10f else 0f,
                    robotZ = (1f - viewModel.simulationProgress) * 400f - 200f,
                    speed = if (viewModel.simulationState == SimulationState.Running) 1.5f else 0f,
                    paintColor = profile?.selectedPaint ?: "#3D5AFE",
                    eyesType = profile?.selectedEyes ?: "digital",
                    hatType = profile?.selectedHat ?: "none",
                    legs = viewModel.selectedLegs,
                    leftArm = viewModel.selectedLeftArm,
                    rightArm = viewModel.selectedRightArm,
                    utility = viewModel.selectedUtility,
                    hazardType = parsedHazard,
                    progress = viewModel.simulationProgress,
                    simulationState = viewModel.simulationState,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                viewModel.modelRotationAngleY += dragAmount.x * 0.015f
                                viewModel.modelRotationAngleX -= dragAmount.y * 0.015f
                            }
                        }
                ) {
                    val cols = 15
                    val rows = 10
                    for (c in 0..cols) {
                        val x = c * (size.width / cols)
                        drawLine(CyberSteel.copy(alpha = 0.15f), Offset(x, 0f), Offset(x, size.height))
                    }
                    for (r in 0..rows) {
                        val y = r * (size.height / rows)
                        drawLine(CyberSteel.copy(alpha = 0.15f), Offset(0f, y), Offset(size.width, y))
                    }

                    val groundY = size.height - 40f
                    drawLine(CyberSteel, Offset(0f, groundY), Offset(size.width, groundY), strokeWidth = 4f)

                    drawCustomWorldObstacles(level.obstaclesLayout, viewModel.simulationProgress, viewModel.simulationState)

                    drawRobotChassis(
                        paintColor = profile?.selectedPaint ?: "#3D5AFE",
                        eyesType = profile?.selectedEyes ?: "digital",
                        hatType = profile?.selectedHat ?: "none",
                        legs = viewModel.selectedLegs,
                        leftArm = viewModel.selectedLeftArm,
                        rightArm = viewModel.selectedRightArm,
                        utility = viewModel.selectedUtility,
                        progress = viewModel.simulationProgress,
                        state = viewModel.simulationState,
                        rotX = viewModel.modelRotationAngleX,
                        rotY = viewModel.modelRotationAngleY,
                        cameraShake = viewModel.cameraShakeAmount,
                        headStyle = viewModel.headStyle,
                        torsoStyle = viewModel.torsoStyle
                    )

                    drawSimParticles(viewModel.particles)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSteel),
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("WEIGHT", color = CyberGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("$totalWeight/${level.maxWeight} kg", color = if (isWeightOver) CyberRed else CyberLime, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                    LinearProgressIndicator(
                        progress = { (totalWeight / level.maxWeight.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (isWeightOver) CyberRed else CyberLime,
                        trackColor = CyberObsidian
                    )
                    Text(
                        text = if (isWeightOver) "WEIGHT EXCEEDED" else "WEIGHT IS OK",
                        color = if (isWeightOver) CyberRed else CyberLime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSteel),
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("POWER DRAW", color = CyberGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("$totalPower/${level.maxEnergyCost} W", color = if (isPowerOver) CyberRed else CyberBlue, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                    LinearProgressIndicator(
                        progress = { (totalPower / level.maxEnergyCost.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (isPowerOver) CyberRed else CyberBlue,
                        trackColor = CyberObsidian
                    )
                    Text(
                        text = if (isPowerOver) "POWER DRAIN OVERLIMIT" else "POWER BALANCE OK",
                        color = if (isPowerOver) CyberRed else CyberBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SlotCard(
                name = "LEGS",
                selectedPart = viewModel.selectedLegs,
                icon = Icons.Default.DirectionsRun,
                color = CyberLime,
                isSelected = activeSlot == SlotType.Leg,
                modifier = Modifier.weight(1f)
            ) { activeSlot = SlotType.Leg }

            SlotCard(
                name = "L-ARM",
                selectedPart = viewModel.selectedLeftArm,
                icon = Icons.Default.BackHand,
                color = CyberBlue,
                isSelected = activeSlot == SlotType.LeftArm,
                modifier = Modifier.weight(1f)
            ) { activeSlot = SlotType.LeftArm }

            SlotCard(
                name = "R-ARM",
                selectedPart = viewModel.selectedRightArm,
                icon = Icons.Default.FrontHand,
                color = CyberBlue,
                isSelected = activeSlot == SlotType.RightArm,
                modifier = Modifier.weight(1f)
            ) { activeSlot = SlotType.RightArm }

            SlotCard(
                name = "UTILITY",
                selectedPart = viewModel.selectedUtility,
                icon = Icons.Default.Settings,
                color = CyberOrange,
                isSelected = activeSlot == SlotType.Utility,
                modifier = Modifier.weight(1f)
            ) { activeSlot = SlotType.Utility }
        }

        val activePart = when (activeSlot) {
            SlotType.Leg -> viewModel.selectedLegs
            SlotType.LeftArm -> viewModel.selectedLeftArm
            SlotType.RightArm -> viewModel.selectedRightArm
            SlotType.Utility -> viewModel.selectedUtility
            else -> null
        }
        if (activePart != null) {
            Text(
                text = "${activePart.uppercase()}: ${getPartSpecDescription(activePart)}",
                color = CyberBlue,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = BorderStroke(1.dp, CyberSteel),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "AUTOMATED_LOGGER.BAT",
                    color = CyberLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = viewModel.simulationLog,
                    color = CyberWhite,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        }

        val isSimActive = viewModel.simulationState is SimulationState.Running
        Button(
            onClick = { viewModel.startCustomSimulation() },
            colors = ButtonDefaults.buttonColors(containerColor = CyberLime),
            enabled = !isSimActive && !isWeightOver && !isPowerOver,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Terminal, contentDescription = "Run", tint = CyberObsidian)
                Text(
                    text = if (isSimActive) "SIMULATING TRAJECTORY..." else "EXECUTE AUTOMATED CODE",
                    color = CyberObsidian,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }
        }

        if (activeSlot != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSteel),
                border = BorderStroke(1.5.dp, CyberBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CHIP SELECTION: ${activeSlot?.name?.uppercase()}",
                            color = CyberBlue,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(onClick = { activeSlot = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = CyberGray)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    val allowedPartsList = when (activeSlot) {
                        SlotType.Leg -> level.allowedLegs.split(",")
                        SlotType.LeftArm, SlotType.RightArm -> level.allowedArms.split(",")
                        SlotType.Utility -> level.allowedUtilities.split(",")
                        else -> emptyList()
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allowedPartsList) { part ->
                            val isCurrentlyEquipped = when (activeSlot) {
                                SlotType.Leg -> viewModel.selectedLegs == part
                                SlotType.LeftArm -> viewModel.selectedLeftArm == part
                                SlotType.RightArm -> viewModel.selectedRightArm == part
                                SlotType.Utility -> viewModel.selectedUtility == part
                                else -> false
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isCurrentlyEquipped) CyberLime else CyberGray.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .background(if (isCurrentlyEquipped) CyberLime.copy(alpha = 0.15f) else CyberObsidian)
                                    .clickable {
                                        viewModel.selectPart(activeSlot!!, part)
                                        activeSlot = null
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = part,
                                        color = if (isCurrentlyEquipped) CyberLime else CyberWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = getPartSpecDescription(part),
                                        color = CyberGray,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Weight: ${viewModel.getPartWeight(part)}kg  Power: ${viewModel.getPartPower(part)}W",
                                        color = CyberBlue.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                if (isCurrentlyEquipped) {
                                    Icon(Icons.Default.Check, contentDescription = "Equipped", tint = CyberLime)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val state = viewModel.simulationState
    if (state is SimulationState.Success && viewModel.simulationProgress == 1f) {
        AlertDialog(
            onDismissRequest = { viewModel.resetSimulation() },
            containerColor = CyberSteel,
            title = {
                Text(
                    text = "🏆 SECTOR SECURED!",
                    color = CyberLime,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star",
                                tint = if (i <= state.stars) CyberGold else CyberGray.copy(alpha = 0.3f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Text(
                        text = state.message,
                        color = CyberWhite,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSimulation()
                        viewModel.onScreenChanged(Screen.CustomLevelSelect)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberLime)
                ) {
                    Text("OK", color = CyberObsidian, fontWeight = FontWeight.Bold)
                }
            }
        )
    } else if (state is SimulationState.Failure) {
        AlertDialog(
            onDismissRequest = { viewModel.resetSimulation() },
            containerColor = CyberSteel,
            title = {
                Text(
                    text = "💥 DIAGNOSTICS FAILURE",
                    color = CyberRed,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = state.message,
                    color = CyberWhite,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetSimulation() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed)
                ) {
                    Text("RE-ENGINEER CHASSIS", color = CyberWhite, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
