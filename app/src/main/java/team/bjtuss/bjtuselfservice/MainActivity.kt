package team.bjtuss.bjtuselfservice

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderAll
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import team.bjtuss.bjtuselfservice.MainApplication.Companion.appContext
import team.bjtuss.bjtuselfservice.component.elasticOverscroll
import team.bjtuss.bjtuselfservice.component.AppDestination
import team.bjtuss.bjtuselfservice.component.LocalAppTransition
import team.bjtuss.bjtuselfservice.component.LocalAppVisibility
import team.bjtuss.bjtuselfservice.notification.CourseReminderScheduler
import team.bjtuss.bjtuselfservice.RouteManager.ClassroomDetection
import team.bjtuss.bjtuselfservice.repository.fetchLatestRelease
import team.bjtuss.bjtuselfservice.screen.BuildingScreen
import team.bjtuss.bjtuselfservice.screen.ClassroomScreen
import team.bjtuss.bjtuselfservice.screen.CourseScheduleScreen
import team.bjtuss.bjtuselfservice.screen.CoursewareScreen
import team.bjtuss.bjtuselfservice.screen.EmailScreen
import team.bjtuss.bjtuselfservice.screen.ExamScheduleScreen
import team.bjtuss.bjtuselfservice.screen.GradeScreen
import team.bjtuss.bjtuselfservice.screen.HomeScreen
import team.bjtuss.bjtuselfservice.screen.HomeworkScreen
import team.bjtuss.bjtuselfservice.screen.OtherFunctionScreen
import team.bjtuss.bjtuselfservice.screen.RoomOccupancyScreen
import team.bjtuss.bjtuselfservice.screen.SchoolCalendarScreen
import team.bjtuss.bjtuselfservice.screen.SettingScreen
import team.bjtuss.bjtuselfservice.screen.SpaceScreen
import team.bjtuss.bjtuselfservice.statemanager.AppState
import team.bjtuss.bjtuselfservice.statemanager.AppStateManager
import team.bjtuss.bjtuselfservice.statemanager.AuthenticatorManager
import team.bjtuss.bjtuselfservice.statemanager.LoginDialog
import team.bjtuss.bjtuselfservice.ui.theme.AppTheme
import team.bjtuss.bjtuselfservice.viewmodel.ClassroomViewModel
import team.bjtuss.bjtuselfservice.viewmodel.CourseScheduleViewModel
import team.bjtuss.bjtuselfservice.viewmodel.CoursewareViewModel
import team.bjtuss.bjtuselfservice.viewmodel.ExamScheduleViewModel
import team.bjtuss.bjtuselfservice.viewmodel.GradeViewModel
import team.bjtuss.bjtuselfservice.viewmodel.HomeworkViewModel
import team.bjtuss.bjtuselfservice.viewmodel.MainViewModel
import team.bjtuss.bjtuselfservice.viewmodel.MainViewModelFactory
import team.bjtuss.bjtuselfservice.viewmodel.SettingViewModel
import team.bjtuss.bjtuselfservice.viewmodel.StatusViewModel
import team.bjtuss.bjtuselfservice.web.ClassroomCapacityService
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs


import androidx.compose.material3.ExperimentalMaterial3Api
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.border

val LocalHasBackground = androidx.compose.runtime.compositionLocalOf { false }

class MainActivity : ComponentActivity() {
    private var reminderNavigationRequest by mutableIntStateOf(0)

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra(CourseReminderScheduler.OPEN_SCHEDULE_EXTRA, false)) {
            reminderNavigationRequest++
        }
        enableEdgeToEdge()
        CaptchaModel.init(this)


        setContent {

            val classroomViewModel: ClassroomViewModel = viewModel()
            val gradeViewModel: GradeViewModel = viewModel()
            val courseScheduleViewModel: CourseScheduleViewModel = viewModel()
            val examScheduleViewModel: ExamScheduleViewModel = viewModel()
            val homeworkViewModel: HomeworkViewModel = viewModel()
            val statusViewModel: StatusViewModel = viewModel()
            val settingViewModel: SettingViewModel = viewModel()
            val coursewareViewModel: CoursewareViewModel = viewModel()
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(
                    gradeViewModel,
                    courseScheduleViewModel,
                    examScheduleViewModel,
                    classroomViewModel,
                    homeworkViewModel,
                    statusViewModel,
                    settingViewModel,
                    coursewareViewModel,
                )
            )
            val dynamicColorEnable by settingViewModel.dynamicColorEnable.collectAsState()
            val appState by AppStateManager.appState.collectAsState()
            val credentials by AuthenticatorManager.credentials.collectAsState()
            val currentTheme by mainViewModel.settingViewModel.currentTheme.collectAsState()
            val backgroundImageUri by settingViewModel.backgroundImageUri.collectAsState()

            AppTheme(currentTheme = currentTheme, dynamicColor = dynamicColorEnable) {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalHasBackground provides backgroundImageUri.isNotEmpty()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                    val bitmap = remember(backgroundImageUri) {
                        if (backgroundImageUri.isNotEmpty() && java.io.File(backgroundImageUri).exists()) {
                            android.graphics.BitmapFactory.decodeFile(backgroundImageUri)?.asImageBitmap()
                        } else null
                    }
                    if (bitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = bitmap,
                            contentDescription = "Background",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    Surface(
                        color = if (bitmap != null) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (appState == AppState.Logout || appState == AppState.Error) {

                            LoginDialog(credentials)
                        }

                        App(
                            mainViewModel,
                            hasBackground = bitmap != null,
                            reminderNavigationRequest = reminderNavigationRequest,
                        )
                        FloatingLoggingIndicator(appState)

                    }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(CourseReminderScheduler.OPEN_SCHEDULE_EXTRA, false)) {
            reminderNavigationRequest++
        }
    }

    override fun onResume() {
        super.onResume()
        (application as MainApplication).refreshCourseReminders()
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun CheckUpdate() {
    val versionName =
        appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
    var versionLatest by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf("") }
    var downloadUrl by remember { mutableStateOf<String?>(null) }
    var updateMarkdown by remember { mutableStateOf("") }
    var hasChecked by remember { mutableStateOf(false) }

    scope.launch {
        if (hasChecked) return@launch
        hasChecked = true
        val release = fetchLatestRelease()
        updateMessage = release?.let {
            val instant = Instant.parse(it.publishedAt)
            val localDateTime = instant.atZone(ZoneId.systemDefault())
            "发布时间: ${
                localDateTime.format(
                    DateTimeFormatter.ofPattern(
                        "yyyy年M月d日 HH:mm", Locale.getDefault()
                    )
                )
            }"
        } ?: "检查失败，请稍后再试"
        updateMarkdown = release?.body ?: ""
        versionLatest = release?.tagName ?: ""
        downloadUrl = release?.htmlUrl
        if (versionLatest.isNotEmpty() && (versionName < versionLatest)) showDialog = true
    }

    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog = false }, confirmButton = {
            downloadUrl?.let { url ->
                Button(onClick = {
                    showDialog = false
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    appContext.startActivity(intent)
                }) {
                    Text("前往下载")
                }
            }
        }, dismissButton = {
            TextButton(onClick = { showDialog = false }) {
                Text("人习于枸且非一日")
            }
        }, text = {
            Column {
                Text(
                    "发现新版本${versionLatest}！",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    updateMessage, style = MaterialTheme.typography.bodyLarge
                )
                LazyColumn {
                    item {
                        MarkdownText(
                            markdown = updateMarkdown
                        )
                    }
                }
            }
        }, title = { Text("检查更新") })
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun App(
    mainViewModel: MainViewModel,
    hasBackground: Boolean = false,
    reminderNavigationRequest: Int = 0,
) {

    val navController = rememberNavController()
    LaunchedEffect(reminderNavigationRequest) {
        if (reminderNavigationRequest > 0) {
            navController.navigate(RouteManager.CourseSchedule) {
                popUpTo(RouteManager.Navigation)
                launchSingleTop = true
            }
        }
    }
    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalAppTransition provides this) {
            NavHost(navController = navController,
                startDestination = RouteManager.Navigation,
                enterTransition = { activityEnterTransition() },
                exitTransition = { activityExitTransition() },
                popEnterTransition = { activityPopEnterTransition() },
                popExitTransition = { activityPopExitTransition() }) {
                composable(RouteManager.Navigation) {
                    CompositionLocalProvider(LocalAppVisibility provides this) {
                        AppNavigation(navController, mainViewModel, hasBackground)
                    }
                }
                composable(RouteManager.CourseSchedule) {
                    AppDestination(RouteManager.CourseSchedule, this) { CourseScheduleScreen(mainViewModel) }
                }
                composable(RouteManager.RoomOccupancy) {
                    AppDestination(RouteManager.RoomOccupancy, this) {
                        RoomOccupancyScreen(mainViewModel, onBack = { navController.popBackStack() })
                    }
                }
                composable(RouteManager.ExamSchedule) {
                    AppDestination(RouteManager.ExamSchedule, this) { ExamScheduleScreen(mainViewModel) }
                }
                composable(RouteManager.Building) {
                    AppDestination(RouteManager.Building, this) { BuildingScreen(navController) }
                }
                composable(
                    route = ClassroomDetection,
                    arguments = listOf(navArgument("buildingName") { type = NavType.StringType })
                ) {
                    val buildingName = it.arguments?.getString("buildingName") ?: ""
                    ClassroomScreen(buildingName = buildingName, mainViewModel)
                }
                composable(RouteManager.Grade) {
                    AppDestination(RouteManager.Grade, this) { GradeScreen(mainViewModel) }
                }
                composable(RouteManager.Email) {
                    AppDestination(RouteManager.Email, this) { EmailScreen() }
                }
                composable(RouteManager.HomeWork) {
                    AppDestination(RouteManager.HomeWork, this) { HomeworkScreen(mainViewModel) }
                }
                composable(RouteManager.OtherFunction) {
                    AppDestination(RouteManager.OtherFunction, this) {
                        OtherFunctionScreen(onCalendarClick = { navController.navigate(RouteManager.SchoolCalendar) })
                    }
                }
                composable(RouteManager.SchoolCalendar) {
                    AppDestination(RouteManager.SchoolCalendar, this) {
                        SchoolCalendarScreen(onBack = { navController.popBackStack() })
                    }
                }
                composable(RouteManager.Courseware) {
                    AppDestination(RouteManager.Courseware, this) { CoursewareScreen(mainViewModel) }
                }
            }
        }
    }
}

@Composable
fun FloatingLoggingIndicator(appState: AppState) {
    AnimatedVisibility(
        visible = appState == AppState.Logging,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it / 2 }
                ),
        exit = fadeOut(animationSpec = tween(300)) +
                slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { -it / 2 }
                )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 加载指示器背景卡片
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp) // 距离顶部的距离
                    .wrapContentSize()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 旋转动画的圆形进度指示器
                    val infiniteTransition = rememberInfiniteTransition(label = "loading_indicator")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )

                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Loading",
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { rotationZ = rotation },
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        "正在登录...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

data class PageItem(
    val route: String, val title: String, val icon: ImageVector
)

object RouteManager {
    const val Home: String = "Home"
    const val Space: String = "Space"
    const val Setting: String = "Setting"
    const val Navigation: String = "Navigation"
    const val ExamSchedule: String = "Exam"
    const val Building: String = "Building"
    const val ClassroomDetection: String = "ClassroomDetection/{buildingName}"
    const val Grade: String = "Grade"
    const val CourseSchedule: String = "CourseSchedule"
    const val RoomOccupancy: String = "RoomOccupancy"
    const val Email: String = "Email"
    const val HomeWork: String = "HomeWork"
    const val OtherFunction: String = "OtherFunction"
    const val SchoolCalendar: String = "SchoolCalendar"

    //    const val CoursePlatform: String = "CoursePlatform"
    const val Courseware: String = "Courseware"
}

@Composable
fun AppNavigation(
    navController: NavController, mainViewModel: MainViewModel, hasBackground: Boolean = false
) {
    val pages = remember {
        listOf(
            PageItem(RouteManager.Home, "首页", Icons.Default.Home),
            PageItem(RouteManager.Space, "应用", Icons.Default.BorderAll),
            PageItem(RouteManager.Setting, "设置", Icons.Default.Settings)
        )
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val clickSequence = remember { mutableStateListOf<Int>() }
    val answer = remember { listOf(1, 2, 2, 0, 1, 1) }
    val coroutineScope = rememberCoroutineScope()
    var pageAnimation by remember { mutableStateOf<Job?>(null) }
    val navigationHazeState = remember { dev.chrisbanes.haze.HazeState() }

    Scaffold(
        containerColor = if (hasBackground) Color.Transparent else MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
                    .haze(navigationHazeState)
                    .elasticOverscroll(orientation = Orientation.Horizontal),
                // Keep the three small root screens composed, including their scroll positions.
                beyondViewportPageCount = pages.lastIndex,
                key = { pages[it].route },
                userScrollEnabled = true,
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    snapAnimationSpec = spring(dampingRatio = 0.95f, stiffness = 280f)
                )
            ) { page ->
                Box(Modifier.fillMaxSize().graphicsLayer {
                    val distance = abs(pagerState.currentPage + pagerState.currentPageOffsetFraction - page)
                        .coerceIn(0f, 1f)
                    scaleX = 1f - distance * 0.018f
                    scaleY = scaleX
                }) {
                    when (page) {
                        0 -> HomeScreen(navController = navController, mainViewModel = mainViewModel)
                        1 -> SpaceScreen(
                            navController = navController,
                            enableLaunchMotion = pagerState.settledPage == 1
                        )
                        2 -> SettingScreen(mainViewModel = mainViewModel)
                    }
                }
            }

            Box(Modifier.align(Alignment.BottomCenter)) {
                ContinuousNavigationBar(pages, pagerState, navigationHazeState) { index ->
                    clickSequence.add(index)
                    if (clickSequence.size > answer.size) clickSequence.removeAt(0)
                    if (clickSequence.toList() == answer) ClassroomCapacityService.ok = true

                    if (pagerState.targetPage != index || !pagerState.isScrollInProgress) {
                        pageAnimation?.cancel()
                        pageAnimation = coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                index,
                                animationSpec = tween(
                                    durationMillis = PAGE_TRANSITION_DURATION,
                                    easing = PageTransitionEasing
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinuousNavigationBar(
    pages: List<PageItem>,
    pagerState: PagerState,
    hazeState: dev.chrisbanes.haze.HazeState,
    onSelect: (Int) -> Unit,
) {
    val selectedPage by remember(pagerState) { derivedStateOf { pagerState.targetPage } }
    val shape = CircleShape
    val darkGlass = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val glassTint = MaterialTheme.colorScheme.surface.copy(alpha = if (darkGlass) 0.23f else 0.18f)
    val glassEdge = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (darkGlass) 0.42f else 0.88f),
            Color.White.copy(alpha = if (darkGlass) 0.10f else 0.30f),
            MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
        )
    )
    val selectionGlass = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (darkGlass) 0.20f else 0.56f),
            Color.White.copy(alpha = if (darkGlass) 0.10f else 0.32f)
        )
    )
    val direction = if (androidx.compose.ui.platform.LocalLayoutDirection.current ==
        androidx.compose.ui.unit.LayoutDirection.Rtl) -1f else 1f

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp, start = 20.dp, end = 20.dp)
            .height(68.dp)
            .shadow(10.dp, shape)
            .clip(shape)
            .hazeChild(
                hazeState,
                shape = shape,
                style = HazeStyle(tint = glassTint, blurRadius = 16.dp)
            )
            .border(1.dp, glassEdge, shape)
            .selectableGroup()
    ) {
        val cellWidth = maxWidth / pages.size
        val selectionWidth = cellWidth - 8.dp

        Box(
            Modifier.align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .width(selectionWidth)
                .height(60.dp)
                .graphicsLayer {
                    val position = (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                        .coerceIn(0f, pages.lastIndex.toFloat())
                    val stretch = (abs(pagerState.currentPageOffsetFraction) * 2f)
                        .coerceIn(0f, 1f)
                    translationX = direction * cellWidth.toPx() * position
                    scaleX = 1f + 0.06f * stretch
                    scaleY = 1f - 0.025f * stretch
                }
                .shadow(4.dp, shape)
                .clip(shape)
                .background(selectionGlass, shape)
                .border(1.dp, glassEdge, shape)
        )

        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            pages.forEachIndexed { index, page ->
                val interactionSource = remember { MutableInteractionSource() }
                val selectionFraction = {
                    (1f - abs(pagerState.currentPage + pagerState.currentPageOffsetFraction - index))
                        .coerceIn(0f, 1f)
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.fillMaxWidth().height(68.dp)
                            .clip(shape)
                            .selectable(
                                selected = selectedPage == index,
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                role = Role.Tab,
                                onClick = { onSelect(index) }
                            )
                            .semantics { contentDescription = page.title },
                        contentAlignment = Alignment.Center
                    ) {
                        NavigationItemContent(
                            page,
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            Modifier.graphicsLayer { alpha = 1f - selectionFraction() }
                        )
                        NavigationItemContent(
                            page,
                            MaterialTheme.colorScheme.onSurface,
                            Modifier.graphicsLayer { alpha = selectionFraction() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationItemContent(page: PageItem, tint: Color, modifier: Modifier) {
    Column(
        modifier = modifier.clearAndSetSemantics { },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(page.icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(2.dp))
        Text(page.title, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = tint)
    }
}

private const val PAGE_TRANSITION_DURATION = 420
private val PageTransitionEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
private const val APP_TRANSITION_DURATION = 300

private fun AnimatedContentTransitionScope<NavBackStackEntry>.activityEnterTransition(): EnterTransition {
    if (initialState.destination.route == RouteManager.Navigation) {
        return fadeIn(tween(APP_TRANSITION_DURATION - 60, delayMillis = 60, easing = FastOutSlowInEasing))
    }
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(PAGE_TRANSITION_DURATION, easing = PageTransitionEasing),
        initialOffset = { it }
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.activityExitTransition(): ExitTransition {
    if (initialState.destination.route == RouteManager.Navigation) {
        return fadeOut(tween(APP_TRANSITION_DURATION - 60, easing = FastOutSlowInEasing))
    }
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(PAGE_TRANSITION_DURATION, easing = PageTransitionEasing),
        targetOffset = { it / 4 }
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.activityPopEnterTransition(): EnterTransition {
    if (targetState.destination.route == RouteManager.Navigation) {
        return fadeIn(tween(APP_TRANSITION_DURATION - 50, delayMillis = 50, easing = FastOutSlowInEasing))
    }
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(PAGE_TRANSITION_DURATION, easing = PageTransitionEasing),
        initialOffset = { it / 4 }
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.activityPopExitTransition(): ExitTransition {
    if (targetState.destination.route == RouteManager.Navigation) {
        return fadeOut(tween(APP_TRANSITION_DURATION - 80, easing = FastOutSlowInEasing))
    }
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(PAGE_TRANSITION_DURATION, easing = PageTransitionEasing),
        targetOffset = { it }
    )
}
