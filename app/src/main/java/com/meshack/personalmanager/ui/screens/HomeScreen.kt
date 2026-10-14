package com.meshack.personalmanager.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.meshack.personalmanager.R
import com.meshack.personalmanager.data.local.entities.AppLimit
import com.meshack.personalmanager.data.local.entities.RoutineBlock
import com.meshack.personalmanager.data.local.entities.Task
import com.meshack.personalmanager.data.repositories.GoalSummary
import com.meshack.personalmanager.ui.HomeViewModel
import com.meshack.personalmanager.ui.PomodoroViewModel
import com.meshack.personalmanager.ui.Screen
import com.meshack.personalmanager.ui.SettingsViewModel
import com.meshack.personalmanager.ui.components.formatClock
import com.meshack.personalmanager.ui.components.formatMinute
import com.meshack.personalmanager.ui.theme.GoalColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

private val GoalRed = Color(0xFFBA1A1A)
private val GoalOrange = Color(0xFFE8710A)
private val GoalGreen = Color(0xFF1E7E34)

private fun barColor(percent: Float): Color = when {
    percent >= 0.66f -> GoalGreen
    percent >= 0.33f -> GoalOrange
    else -> GoalRed
}

private fun formatHours(totalMinutes: Int): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

/** Display title: drops the legacy "Sample:" marker, identity now comes from the color dot. */
private fun displayTitle(raw: String): String =
    raw.removePrefix("Sample:").trimStart().ifBlank { raw }

@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    pomodoroViewModel: PomodoroViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val userName by homeViewModel.welcomeName.collectAsState()
    val congratsMessage by homeViewModel.congratsMessage.collectAsState()
    val summaries by homeViewModel.goalSummaries.collectAsState()
    val overview by homeViewModel.routineOverview.collectAsState()
    val limits by homeViewModel.blacklistedApps.collectAsState()
    val focusStats by homeViewModel.focusStats.collectAsState()
    val focusTarget by homeViewModel.focusTargetMinutes.collectAsState()
    val todayTasks by homeViewModel.todayTasks.collectAsState()
    val use24h by settingsViewModel.use24h.collectAsState()
    // Completed goals step aside so the next open goal slides into the
    // spotlight — every finished goal gets its own congrats moment.
    val activeGoal = summaries.firstOrNull { it.percent < 1f }
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            now = LocalDateTime.now()
        }
    }
    // Edge-triggered celebration: fires once per goal when it freshly crosses
    // to 100% — never on cold open, never on every recomposition. The pad
    // itself already stepped aside to the next open goal, so each finished
    // goal gets its own congrats moment. Auto-dismisses after 8s.
    var overlayGoal by remember { mutableStateOf<GoalSummary?>(null) }
    var celebratedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var firstEmission by remember { mutableStateOf(true) }
    val completedIds = summaries.filter { it.percent >= 1f }.map { it.goal.id }.toSet()
    LaunchedEffect(completedIds) {
        if (firstEmission) {
            celebratedIds = completedIds
            firstEmission = false
            return@LaunchedEffect
        }
        val fresh = (completedIds - celebratedIds).firstOrNull()
        if (fresh != null) {
            celebratedIds += fresh
            overlayGoal = summaries.firstOrNull { it.goal.id == fresh }
            delay(8000L)
            overlayGoal = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        if (userName.isBlank()) "Welcome" else "Welcome, $userName",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            today,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            formatClock(now, use24h),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        item {
            ActiveGoalPad(
                activeGoal = activeGoal,
                recent = summaries.take(3),
                todayTasks = todayTasks,
                onToggleTask = { homeViewModel.toggleTask(it) },
                onOpenGoals = { onNavigate(Screen.Goals) }
            )
        }
        item {
            RoutineOverviewCard(
                current = overview.current,
                next = overview.next,
                use24h = use24h,
                onOpenRoutine = { onNavigate(Screen.Routine) }
            )
        }
        item {
            BlockingOverviewCard(
                limits = limits.filter { it.blacklisted },
                onOpenBlocking = { onNavigate(Screen.AppLimits) }
            )
        }
        item {
            FocusOverviewCard(
                viewModel = pomodoroViewModel,
                todayMinutes = focusStats.todayMinutes,
                totalMinutes = focusStats.totalMinutes,
                targetMinutes = focusTarget
            )
        }
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = overlayGoal != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) +
                androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) +
                androidx.compose.animation.fadeOut()
        ) {
            overlayGoal?.let { celebrated ->
                CelebrationOverlay(
                    title = displayTitle(celebrated.goal.title),
                    message = congratsMessage,
                    onDismiss = { overlayGoal = null }
                )
            }
        }
    }
}

@Composable
private fun ActiveGoalPad(
    activeGoal: GoalSummary?,
    recent: List<GoalSummary>,
    todayTasks: List<Task>,
    onToggleTask: (Task) -> Unit,
    onOpenGoals: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onOpenGoals) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (activeGoal == null && recent.isEmpty()) {
                Text("Active Goal", style = MaterialTheme.typography.titleMedium)
                Text(
                    "No goals yet — your active goal and its progress will live here.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (activeGoal == null) {
                Text("Active Goal", style = MaterialTheme.typography.titleMedium)
                Text(
                    "All goals complete — every one earned its congrats.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoalGreen,
                    fontWeight = FontWeight.SemiBold
                )
                HorizontalDivider()
                PadBottomRow(
                    recent = recent,
                    todayTasks = todayTasks,
                    onToggleTask = onToggleTask
                )
            } else {
                GoalPadContent(
                    activeGoal = activeGoal,
                    recent = recent,
                    todayTasks = todayTasks,
                    onToggleTask = onToggleTask
                )
            }
        }
    }
}

@Composable
private fun GoalPadContent(
    activeGoal: GoalSummary,
    recent: List<GoalSummary>,
    todayTasks: List<Task>,
    onToggleTask: (Task) -> Unit
) {
    val percent = activeGoal.percent
    val animated by animateFloatAsState(targetValue = percent, label = "goalProgress")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.size(14.dp).clip(CircleShape)
                    .background(GoalColors.forGoal(activeGoal.goal.id))
            )
            Text(
                displayTitle(activeGoal.goal.title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${(percent * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = barColor(percent)
            )
        }
    }
    LinearProgressIndicator(
        progress = animated,
        modifier = Modifier.fillMaxWidth(),
        color = barColor(percent),
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
    if (percent >= 1f) {
        Text(
            "Complete",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = GoalGreen
        )
    }
    HorizontalDivider()
    PadBottomRow(recent = recent, todayTasks = todayTasks, onToggleTask = onToggleTask)
}

@Composable
private fun PadBottomRow(
    recent: List<GoalSummary>,
    todayTasks: List<Task>,
    onToggleTask: (Task) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Goals", style = MaterialTheme.typography.titleSmall)
            if (recent.isEmpty()) {
                Text("Nothing here yet.", style = MaterialTheme.typography.bodySmall)
            } else {
                recent.forEach { summary ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(8.dp).clip(CircleShape)
                                .background(GoalColors.forGoal(summary.goal.id))
                        )
                        Text(
                            displayTitle(summary.goal.title),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    LinearProgressIndicator(
                        progress = summary.percent,
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = barColor(summary.percent),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
        VerticalDivider()
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Tasks", style = MaterialTheme.typography.titleSmall)
            val undone = todayTasks.filter { !it.done }
            val upcoming = (listOfNotNull(undone.firstOrNull()) + undone.drop(1).take(1)).take(2)
            if (upcoming.isEmpty()) {
                Text("All tasks done — enjoy the win.", style = MaterialTheme.typography.bodySmall)
            } else {
                upcoming.forEachIndexed { index, task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { onToggleTask(task) }
                        )
                        Text(
                            displayTitle(task.title),
                            style = if (index == 0) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            else MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineOverviewCard(
    current: RoutineBlock?,
    next: RoutineBlock?,
    use24h: Boolean,
    onOpenRoutine: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpenRoutine,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Routine",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            if (current == null && next == null) {
                Text(
                    "No blocks scheduled — open Routine to plan your day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            } else {
                current?.let {
                    Text(
                        "Now: ${it.name} · ${formatMinute(it.startMinute, use24h)} – ${formatMinute(it.endMinute, use24h)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                next?.let {
                    Text(
                        "Next: ${it.name} · ${formatMinute(it.startMinute, use24h)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockingOverviewCard(
    limits: List<AppLimit>,
    onOpenBlocking: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpenBlocking,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Blocked apps", style = MaterialTheme.typography.titleMedium)
            }
            if (limits.isEmpty()) {
                Text("Nothing blocked — open Blocking to protect your focus.", style = MaterialTheme.typography.bodyMedium)
            } else {
                limits.take(4).forEach { limit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            limit.appName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${limit.dailyLimitMinutes}m/day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (limits.size > 4) {
                    Text("+${limits.size - 4} more", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun FocusOverviewCard(
    viewModel: PomodoroViewModel,
    todayMinutes: Int,
    totalMinutes: Int,
    targetMinutes: Int
) {
    val targetProgress = (todayMinutes.toFloat() / targetMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Focus Mode · ${viewModel.phase.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { viewModel.toggle() }) {
                    Text(if (viewModel.isRunning) "Pause" else "Start")
                }
                OutlinedButton(onClick = { viewModel.resetWorkPhase() }) {
                    Text("Reset")
                }
            }
            Text(
                "${viewModel.remainingSeconds / 60}m ${viewModel.remainingSeconds % 60}s left",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            LinearProgressIndicator(
                progress = targetProgress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                "Today ${formatHours(todayMinutes)} of ${formatHours(targetMinutes)} · ${formatHours(totalMinutes)} all time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun CelebrationOverlay(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = onDismiss
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (composition != null) {
                LottieAnimation(
                    composition = composition,
                    iterations = 1,
                    modifier = Modifier.size(72.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
