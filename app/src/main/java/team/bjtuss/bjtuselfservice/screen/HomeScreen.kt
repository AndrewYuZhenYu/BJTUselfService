package team.bjtuss.bjtuselfservice.screen

import team.bjtuss.bjtuselfservice.component.elasticOverscroll

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import team.bjtuss.bjtuselfservice.R
import team.bjtuss.bjtuselfservice.RouteManager
import team.bjtuss.bjtuselfservice.component.CalendarComponent
import team.bjtuss.bjtuselfservice.entity.CourseEntity
import team.bjtuss.bjtuselfservice.entity.ExamScheduleEntity
import team.bjtuss.bjtuselfservice.entity.GradeEntity
import team.bjtuss.bjtuselfservice.entity.HomeworkEntity
import team.bjtuss.bjtuselfservice.repository.DataStoreRepository
import team.bjtuss.bjtuselfservice.statemanager.AppEvent
import team.bjtuss.bjtuselfservice.statemanager.AppEventManager
import team.bjtuss.bjtuselfservice.statemanager.AppState
import team.bjtuss.bjtuselfservice.statemanager.AppStateManager
import team.bjtuss.bjtuselfservice.viewmodel.DataChange
import team.bjtuss.bjtuselfservice.viewmodel.MainViewModel
import team.bjtuss.bjtuselfservice.utils.parseCourseWeeks
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, mainViewModel: MainViewModel) {
    val appState by AppStateManager.appState.collectAsState()
    val refreshState =
        rememberSwipeRefreshState(isRefreshing = appState == AppState.NetworkProgress)


    val gradeChangeList: List<DataChange<GradeEntity>> by mainViewModel.gradeViewModel.changeList.collectAsState()
    val courseChangeList: List<DataChange<CourseEntity>> by mainViewModel.courseScheduleViewModel.changeList.collectAsState()
    val examChangeList: List<DataChange<ExamScheduleEntity>> by mainViewModel.examScheduleViewModel.changeList.collectAsState()
    val homeworkChangeList: List<DataChange<HomeworkEntity>> by mainViewModel.homeworkViewModel.changeList.collectAsState()
    val homeworkList: List<HomeworkEntity> by mainViewModel.homeworkViewModel.homeworkList.collectAsState()
    val status by mainViewModel.statusViewModel.status.collectAsState()

    var selectedGradeChange by remember { mutableStateOf<DataChange<GradeEntity>?>(null) }
    var selectedHomeworkChange by remember { mutableStateOf<DataChange<HomeworkEntity>?>(null) }
    var selectedExamChange by remember { mutableStateOf<DataChange<ExamScheduleEntity>?>(null) }
    var showGradeDialog by remember { mutableStateOf(false) }
    var showHomeworkDialog by remember { mutableStateOf(false) }
    var showExamDialog by remember { mutableStateOf(false) }
    var showGradeChanges by rememberSaveable { mutableStateOf(false) }
    var showHomeworkChanges by rememberSaveable { mutableStateOf(false) }
    var showOtherNotifications by rememberSaveable { mutableStateOf(false) }


    val autoSyncGradeEnable by mainViewModel.settingViewModel.autoSyncGradeEnable.collectAsState()
    val autoSyncHomeworkEnable by mainViewModel.settingViewModel.autoSyncHomeworkEnable.collectAsState()
    val autoSyncScheduleEnable by mainViewModel.settingViewModel.autoSyncScheduleEnable.collectAsState()
    val autoSyncExamEnable by mainViewModel.settingViewModel.autoSyncExamEnable.collectAsState()
    val storedCurrentWeek by DataStoreRepository.getCurrentWeek().collectAsState(0)
    val classroomMap by mainViewModel.classroomViewModel.classroomMap.collectAsState()
    val currentWeek = storedCurrentWeek.takeIf { it > 0 }
        ?: classroomMap["nowWeek"]?.firstOrNull()
        ?: 0
    val courseList by mainViewModel.courseScheduleViewModel.nextTermCourseList.collectAsState()
    val today = LocalDate.now()
    val weekday = today.dayOfWeek.value
    val todayCourses = remember(courseList, currentWeek, weekday) {
        if (currentWeek <= 0) emptyList() else (0..6).flatMap { period ->
            courseList.getOrNull(period * 8 + weekday).orEmpty()
                .filter { currentWeek in parseCourseWeeks(it.courseTime) }
                .map { period to it }
        }
    }

    LaunchedEffect(gradeChangeList, autoSyncGradeEnable) {
        if (autoSyncGradeEnable && gradeChangeList.isNotEmpty()) mainViewModel.gradeViewModel.syncDataAndClearChange()
    }
    LaunchedEffect(homeworkChangeList, autoSyncHomeworkEnable) {
        if (autoSyncHomeworkEnable && homeworkChangeList.isNotEmpty()) mainViewModel.homeworkViewModel.syncDataAndClearChange()
    }
    LaunchedEffect(courseChangeList, autoSyncScheduleEnable) {
        if (autoSyncScheduleEnable && courseChangeList.isNotEmpty()) mainViewModel.courseScheduleViewModel.syncDataAndClearChange()
    }
    LaunchedEffect(examChangeList, autoSyncExamEnable) {
        if (autoSyncExamEnable && examChangeList.isNotEmpty()) mainViewModel.examScheduleViewModel.syncDataAndClearChange()
    }

    val handleRefresh = {
        if (appState != AppState.NetworkProgress) {
            AppEventManager.sendEvent(AppEvent.DataSyncRequest)
        }
    }
    
    val backgroundImageUri by mainViewModel.settingViewModel.backgroundImageUri.collectAsState()
    val hasBackground = backgroundImageUri.isNotEmpty()

    SwipeRefresh(state = refreshState, onRefresh = handleRefresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().elasticOverscroll(allowStart = false),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HomeNotificationShortcuts(
                    mailCount = status?.NewMailCount?.trim()?.toIntOrNull() ?: 0,
                    gradeCount = if (autoSyncGradeEnable) 0 else gradeChangeList.sumOf { it.itemCount() },
                    homeworkCount = if (autoSyncHomeworkEnable) 0 else homeworkChangeList.sumOf { it.itemCount() },
                    onMailClick = { navController.navigate(RouteManager.Email) },
                    onGradeClick = {
                        if (!autoSyncGradeEnable && gradeChangeList.isNotEmpty()) showGradeChanges = true
                        else navController.navigate(RouteManager.Grade)
                    },
                    onHomeworkClick = {
                        if (!autoSyncHomeworkEnable && homeworkChangeList.isNotEmpty()) showHomeworkChanges = true
                        else navController.navigate(RouteManager.HomeWork)
                    }
                )
            }
            item { CalendarComponent(mainViewModel) }
            item {
                DailyCourseSection(
                    courses = todayCourses,
                    currentWeek = currentWeek,
                    onCourseClick = { navController.navigate(RouteManager.CourseSchedule) }
                )
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showOtherNotifications = !showOtherNotifications }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("其他通知", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Icon(
                            imageVector = if (showOtherNotifications) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showOtherNotifications) "收起其他通知" else "展开其他通知"
                        )
                    }
                    if (showOtherNotifications) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            EcardButton({ Text("校园卡余额：${status?.EcardBalance ?: "未知"}") }, hasBackground)
                            NetButton({ Text("校园网余额：${status?.NetBalance ?: "未知"}") }, hasBackground)
                            HomeworkNoticeCard(homeworkList, navController, hasBackground)
                            if (!autoSyncScheduleEnable && courseChangeList.isNotEmpty()) {
                                ChangeSection("课程表变动", courseChangeList, { navController.navigate(RouteManager.CourseSchedule) }, hasBackground)
                            }
                            if (!autoSyncExamEnable && examChangeList.isNotEmpty()) {
                                ChangeSection("考试安排变动", examChangeList, { change ->
                                    selectedExamChange = change
                                    showExamDialog = true
                                }, hasBackground)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGradeChanges) {
        ChangeListDialog("成绩单变动", gradeChangeList, {
            showGradeChanges = false
        }, { change ->
            selectedGradeChange = change
            showGradeChanges = false
            showGradeDialog = true
        }, { navController.navigate(RouteManager.Grade) })
    }
    if (showHomeworkChanges) {
        ChangeListDialog("作业变动", homeworkChangeList, {
            showHomeworkChanges = false
        }, { change ->
            selectedHomeworkChange = change
            showHomeworkChanges = false
            showHomeworkDialog = true
        }, { navController.navigate(RouteManager.HomeWork) })
    }

    // Dialogs
    if (showGradeDialog && selectedGradeChange != null) {
        DetailedChangeDialog(
            change = selectedGradeChange!!,
            onDismiss = { showGradeDialog = false },
            cardItem = { GradeItemCard(it) },
            onClick = { navController.navigate(RouteManager.Grade) }
        )
    }

    if (showHomeworkDialog && selectedHomeworkChange != null) {
        DetailedChangeDialog(
            change = selectedHomeworkChange!!,
            onDismiss = { showHomeworkDialog = false },
            cardItem = { HomeworkItemCard(it) },
            onClick = { navController.navigate(RouteManager.HomeWork) }
        )
    }

    if (showExamDialog && selectedExamChange != null) {
        DetailedChangeDialog(
            change = selectedExamChange!!,
            onDismiss = { showExamDialog = false },
            cardItem = { ExamItemCard(it) },
            onClick = { navController.navigate(RouteManager.ExamSchedule) }
        )
    }
}

@Composable
private fun <T> ChangeSection(
    title: String,
    changes: List<DataChange<T>>,
    onItemClick: (DataChange<T>) -> Unit,
    hasBackground: Boolean = false
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp)
    )

    Column(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        changes.forEach { change ->
            ChangeCard(
                dataChange = change,
                onClick = { onItemClick(change) },
                hasBackground = hasBackground
            )
        }
    }
}

private fun <T> DataChange<T>.itemCount(): Int = when (this) {
    is DataChange.Added -> items.size
    is DataChange.Modified -> items.size
    is DataChange.Deleted -> items.size
}

@Composable
private fun HomeNotificationShortcuts(
    mailCount: Int,
    gradeCount: Int,
    homeworkCount: Int,
    onMailClick: () -> Unit,
    onGradeClick: () -> Unit,
    onHomeworkClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NotificationShortcut(
            label = "邮件", count = mailCount, icon = Icons.Default.Email,
            onClick = onMailClick,
            modifier = Modifier.weight(1f)
        )
        NotificationShortcut(
            label = "成绩", count = gradeCount,
            imageRes = R.drawable.grade_report_icon,
            onClick = onGradeClick,
            modifier = Modifier.weight(1f)
        )
        NotificationShortcut(
            label = "作业", count = homeworkCount, icon = Icons.Default.Edit,
            onClick = onHomeworkClick,
            modifier = Modifier.weight(1f)
        )
    }
}

// Clip only the paper silhouette in the supplied 1254px artwork, excluding its baked-in backdrop.
private val GradePaperShape = GenericShape { size, _ ->
    val x = size.width / 1254f
    val y = size.height / 1254f
    moveTo(433f * x, 340f * y)
    lineTo(738f * x, 340f * y)
    lineTo(870f * x, 474f * y)
    lineTo(870f * x, 873f * y)
    quadraticTo(870f * x, 914f * y, 829f * x, 914f * y)
    lineTo(434f * x, 914f * y)
    quadraticTo(394f * x, 914f * y, 394f * x, 873f * y)
    lineTo(394f * x, 381f * y)
    quadraticTo(394f * x, 340f * y, 433f * x, 340f * y)
    close()
}

@Composable
private fun NotificationShortcut(
    label: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    imageRes: Int? = null,
) {
    Column(modifier = modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(86.dp)) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.BottomCenter)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (imageRes != null) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(GradePaperShape),
                        contentScale = ContentScale.Fit
                    )
                } else if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Box(
                modifier = Modifier.size(28.dp).align(Alignment.TopEnd).background(MaterialTheme.colorScheme.error, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 99) "99+" else count.coerceAtLeast(0).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DailyCourseSection(
    courses: List<Pair<Int, CourseEntity>>,
    currentWeek: Int,
    onCourseClick: () -> Unit,
) {
    val times = listOf(
        "08:00" to "09:50", "10:10" to "12:00", "12:10" to "14:00",
        "14:10" to "16:00", "16:20" to "18:10", "19:00" to "20:50",
        "21:00" to "21:50"
    )
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("本日课程", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            if (currentWeek > 0) {
                Text("共 ${courses.size} 节", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (courses.isEmpty()) {
            Text(
                text = if (currentWeek > 0) "今天没有课程" else "教学周尚未同步",
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            courses.forEach { (period, course) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(
                        modifier = Modifier.width(64.dp).padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("第${period + 1}节", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(times[period].first, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(times[period].second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Card(
                        onClick = onCourseClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(course.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            if (course.coursePlace.isNotBlank()) {
                                Text("上课地点：${course.coursePlace}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (course.courseTeacher.isNotBlank()) {
                                Text("上课老师：${course.courseTeacher}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> ChangeListDialog(
    title: String,
    changes: List<DataChange<T>>,
    onDismiss: () -> Unit,
    onChangeClick: (DataChange<T>) -> Unit,
    onNavigate: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                changes.forEach { change ->
                    val label = when (change) {
                        is DataChange.Added -> "新增 ${change.items.size} 项"
                        is DataChange.Modified -> "变化 ${change.items.size} 项"
                        is DataChange.Deleted -> "删除 ${change.items.size} 项"
                    }
                    TextButton(onClick = { onChangeClick(change) }, modifier = Modifier.fillMaxWidth()) {
                        Text(label, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onNavigate()
            }) { Text("查看全部") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
fun EcardButton(content: @Composable () -> Unit, hasBackground: Boolean = false) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = if (hasBackground) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)) else null,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (hasBackground) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (hasBackground) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = "Ecard Balance",
                tint = if (hasBackground) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            content()
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint = if (hasBackground) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }

    // 显示对话框
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("校园卡充值") },
            text = {
                Text("请注意，接下来即将转跳完美校园app\n确保自己已安装哦☺️")
            },
            confirmButton = {
                Button(
                    onClick = {
                        launchWanMeiCampusApp(context)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("打开应用")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

fun shareToWeChat(context: Context) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "https://weixin.bjtu.edu.cn/pay/wap/network/recharge.html")
    }
    val chooser = Intent.createChooser(shareIntent, "请选择：“微信：发送给朋友”")

    try {
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "未找到“微信”app？？？？", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun NetButton(content: @Composable () -> Unit, hasBackground: Boolean = false) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = if (hasBackground) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)) else null,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (hasBackground) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (hasBackground) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Net Balance",
                tint = if (hasBackground) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            content()
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint = if (hasBackground) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }

    // 显示充值提醒对话框
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("校园网续费") },
            text = {
                Text("不好意思直接转跳微信成本还是太高，不过\n注意：以下操作需微信绑定学校企业号\n请分享至微信，后打开（莫吐槽🙏）哦")
            },
            confirmButton = {
                Button(
                    onClick = {
                        shareToWeChat(context)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("分享至微信")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}


// 尝试启动“完美校园”应用
fun launchWanMeiCampusApp(context: Context) {
    val intent = Intent().apply {
        component = ComponentName(
            "com.newcapec.mobile.ncp",
            "com.wanxiao.basebusiness.activity.SplashActivity"
        )
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "未找到“完美校园”app", Toast.LENGTH_LONG).show()
    }
}


@Composable
private fun HomeworkNoticeCard(
    homeworkList: List<HomeworkEntity>,
    navController: NavController,
    hasBackground: Boolean = false
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val now = LocalDateTime.now()
    var countForDeadline = 0
    val DDLList = mutableListOf<HomeworkEntity>()
    var showDetail by remember { mutableStateOf(false) }
    homeworkList.forEach {
        try {
            if (ChronoUnit.HOURS.between(
                    now,
                    LocalDateTime.parse(it.endTime, formatter)
                ) in 0..48
            ) {
                if (it.subStatus != "已提交") {
                    countForDeadline++
                    DDLList.add(it)
                }
            }
        } catch (_: Exception) {
        }
    }

    if (countForDeadline > 0) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = if (hasBackground) androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f)) else null,
            colors = CardDefaults.cardColors(containerColor = if (hasBackground) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = if (hasBackground) 0.dp else 2.dp),
            onClick = {
                showDetail = true
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (hasBackground) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.onErrorContainer)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "有${countForDeadline}项作业已经迫在眉睫！",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = if (hasBackground) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.errorContainer
                        )
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View Details",
                    tint = if (hasBackground) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                )
            }
        }

    }
    if (showDetail) {
        DetailedDialog(
            title = "作业截止提醒",
            items = DDLList,
            onDismiss = { showDetail = false },
            cardItem = { HomeworkItemCard(it) },
            onClick = { navController.navigate(RouteManager.HomeWork) }
        )
    }
}

@Composable
private fun <T> ChangeCard(
    dataChange: DataChange<T>,
    onClick: () -> Unit,
    hasBackground: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = if (hasBackground) androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f)) else null,
        colors = CardDefaults.cardColors(containerColor = if (hasBackground) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (hasBackground) 0.dp else 2.dp)
    ) {
        val (backgroundColor, textColor, icon) = when (dataChange) {
            is DataChange.Added -> Triple(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
                Icons.Default.Add
            )

            is DataChange.Modified -> Triple(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer,
                Icons.Default.Edit
            )

            is DataChange.Deleted -> Triple(
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
                Icons.Default.Delete
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (hasBackground) androidx.compose.ui.graphics.Color.Transparent else backgroundColor)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (hasBackground) androidx.compose.ui.graphics.Color.White else textColor
                )
                Text(
                    text = when (dataChange) {
                        is DataChange.Added -> "新增 ${dataChange.items.size}项"
                        is DataChange.Modified -> "变化 ${dataChange.items.size}项"
                        is DataChange.Deleted -> "删除 ${dataChange.items.size}项"
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (hasBackground) androidx.compose.ui.graphics.Color.White else textColor
                    )
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View Details",
                tint = if (hasBackground) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f) else textColor.copy(alpha = 0.7f)
            )
        }
    }
}


@Composable
fun <T> DetailedChangeDialog(
    change: DataChange<T>,
    onDismiss: () -> Unit,
    cardItem: @Composable (T) -> Unit,
    onClick: () -> Unit = {},
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Dialog Header with count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (change) {
                            is DataChange.Added -> "新增详情"
                            is DataChange.Modified -> "变动详情"
                            is DataChange.Deleted -> "删除详情"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (change) {
                            is DataChange.Added -> "${change.items.size}项"
                            is DataChange.Modified -> "${change.items.size}项"
                            is DataChange.Deleted -> "${change.items.size}项"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Dialog Content
                when (change) {
                    is DataChange.Added -> {
                        change.items.forEachIndexed { index, grade ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                            cardItem(grade)
                        }
                    }

                    is DataChange.Modified -> {
                        change.items.forEachIndexed { index, (newGrade, oldGrade) ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                            cardItem(newGrade)
                            cardItem(oldGrade)
                        }
                    }

                    is DataChange.Deleted -> {
                        change.items.forEachIndexed { index, grade ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                            cardItem(grade)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 16.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                    ) {
                        Text("撤了", color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = onClick,
                    ) {
                        Text("查看更多👀", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun <T> DetailedDialog(
    title: String,
    items: List<T>,
    onDismiss: () -> Unit,
    cardItem: @Composable (T) -> Unit,
    onClick: () -> Unit = {},
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Dialog Header with count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${items.size}项",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Dialog Content
                items.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                    cardItem(item)
                }


                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 16.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                    ) {
                        Text("撤了", color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = onClick,
                    ) {
                        Text("查看更多👀", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}


@Composable
private fun GradeChangeRow(
    fieldName: String,
    oldValue: String,
    newValue: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(
            text = fieldName,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = oldValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Changed to",
                modifier = Modifier.padding(horizontal = 8.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = newValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
