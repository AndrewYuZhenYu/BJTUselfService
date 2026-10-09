package team.bjtuss.bjtuselfservice.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import team.bjtuss.bjtuselfservice.entity.CourseEntity
import team.bjtuss.bjtuselfservice.repository.DataStoreRepository
import team.bjtuss.bjtuselfservice.utils.Utils
import team.bjtuss.bjtuselfservice.viewmodel.DataChange
import team.bjtuss.bjtuselfservice.viewmodel.MainViewModel

private val CourseTimeColumnWidth = 48.dp
private val CourseRowMinHeight = 92.dp
private val CourseCellPadding = 1.dp

private data class CoursePlaceDisplay(
    val building: String,
    val room: String?
)

private data class CourseCardColors(
    val background: Color,
    val stripe: Color,
    val content: Color
)

private val LightCourseColors = listOf(
    CourseCardColors(Color(0xFFF2ECFF), Color(0xFFC9B1FF), Color(0xFF6443B4)),
    CourseCardColors(Color(0xFFE7F4FF), Color(0xFF9ECEFF), Color(0xFF1967AD)),
    CourseCardColors(Color(0xFFFFECEF), Color(0xFFFFB1C2), Color(0xFFB73769)),
    CourseCardColors(Color(0xFFE5F8F1), Color(0xFF90DFC5), Color(0xFF197E68)),
    CourseCardColors(Color(0xFFFFF0E2), Color(0xFFFFC794), Color(0xFFA75320)),
    CourseCardColors(Color(0xFFFFF8D9), Color(0xFFFFE17A), Color(0xFF85600F)),
    CourseCardColors(Color(0xFFFCEBFA), Color(0xFFF2ABE6), Color(0xFFA53D91)),
    CourseCardColors(Color(0xFFECF8E8), Color(0xFFA9E69F), Color(0xFF347B32))
)

private val DarkCourseColors = listOf(
    CourseCardColors(Color(0xFF352948), Color(0xFF9B78DD), Color(0xFFDCC4FF)),
    CourseCardColors(Color(0xFF23384B), Color(0xFF66B6F3), Color(0xFFB7DDFE)),
    CourseCardColors(Color(0xFF482B39), Color(0xFFE78BAA), Color(0xFFFFC5DA)),
    CourseCardColors(Color(0xFF223D37), Color(0xFF6ACFAE), Color(0xFFBAF1DE)),
    CourseCardColors(Color(0xFF463425), Color(0xFFEBB075), Color(0xFFFAD3A8)),
    CourseCardColors(Color(0xFF413A24), Color(0xFFE5CA68), Color(0xFFF9E9A6)),
    CourseCardColors(Color(0xFF462B43), Color(0xFFDB8BD4), Color(0xFFF6C7F0)),
    CourseCardColors(Color(0xFF2D3E2B), Color(0xFF92D784), Color(0xFFCBF0BE))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScheduleScreen(
    mainViewModel: MainViewModel,
) {
    val courseScheduleViewModel = mainViewModel.courseScheduleViewModel
    val classroomViewModel = mainViewModel.classroomViewModel
    val storedCurrentWeek by DataStoreRepository.getCurrentWeek().collectAsState(0)
    LaunchedEffect(Unit) {
        courseScheduleViewModel.syncDataAndClearChange()
    }
    LaunchedEffect(storedCurrentWeek) {
        if (storedCurrentWeek <= 0) {
            classroomViewModel.refreshClassroomMap()
        }
    }
    val courseChangeList: List<DataChange<CourseEntity>> by courseScheduleViewModel.changeList.collectAsState()

    LaunchedEffect(courseChangeList) {
        courseScheduleViewModel.syncDataAndClearChange()
    }

    val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
    val courseTimes = listOf(
        "第一节\n08:00\n09:50",
        "第二节\n10:10\n12:00",
        "第三节\n12:10\n14:00",
        "第四节\n14:10\n16:00",
        "第五节\n16:20\n18:10",
        "第六节\n19:00\n20:50",
        "第七节\n21:00\n21:50"
    )

    var currentTerm by rememberSaveable { mutableStateOf(false) }
    val allWeeks = listOf("全部") + (1..26).map { "第${it}周" }
    var showWeekSelector by rememberSaveable { mutableStateOf(false) }
    val classroomMap by classroomViewModel.classroomMap.collectAsState()
    val currentWeek = storedCurrentWeek.takeIf { it > 0 }
        ?: classroomMap["nowWeek"]?.firstOrNull()
        ?: 0
    var selectedWeek by rememberSaveable { mutableIntStateOf(0) }
    var shouldFollowCurrentWeek by rememberSaveable { mutableStateOf(true) }
    val courseList by
        if (currentTerm) {
            courseScheduleViewModel.currentTermCourseList.collectAsState()
        } else {
            courseScheduleViewModel.nextTermCourseList.collectAsState()
        }

    LaunchedEffect(currentTerm, currentWeek, shouldFollowCurrentWeek) {
        if (!currentTerm && shouldFollowCurrentWeek && currentWeek != 0) {
            selectedWeek = currentWeek
            shouldFollowCurrentWeek = false
        }
    }

    fun selectSchedule(showCourseSelectionSchedule: Boolean) {
        currentTerm = showCourseSelectionSchedule
        selectedWeek = if (!showCourseSelectionSchedule) {
            currentWeek
        } else {
            0
        }
        shouldFollowCurrentWeek = !showCourseSelectionSchedule && currentWeek == 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "课程表")
                        Text(
                            text = "${if (!currentTerm) "本周" else "总览"} · ${selectedWeekLabel(selectedWeek)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CourseScheduleControls(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                showCourseSelectionSchedule = currentTerm,
                selectedWeek = selectedWeek,
                currentWeek = currentWeek,
                onScheduleChange = { selectSchedule(it) },
                onWeekClick = { showWeekSelector = true }
            )

            CourseScheduleContent(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                courseList = courseList,
                weekDays = weekDays,
                courseTimes = courseTimes,
                selectedWeek = selectedWeek
            )
        }
    }

    if (showWeekSelector) {
        AlertDialog(
            onDismissRequest = { showWeekSelector = false },
            title = { Text("选择周数") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    allWeeks.forEachIndexed { index, week ->
                        item {
                            val selected = index == selectedWeek
                            Text(
                                text = week,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        selectedWeek = index
                                        shouldFollowCurrentWeek = false
                                        showWeekSelector = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                fontSize = 16.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWeekSelector = false }) {
                    Text("关闭")
                }
            }
        )
    }
}

@Composable
private fun CourseScheduleControls(
    modifier: Modifier = Modifier,
    showCourseSelectionSchedule: Boolean,
    selectedWeek: Int,
    currentWeek: Int,
    onScheduleChange: (Boolean) -> Unit,
    onWeekClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ) {
            Row(modifier = Modifier.padding(3.dp)) {
                ScheduleSegmentButton(
                    label = "本周",
                    selected = !showCourseSelectionSchedule,
                    onClick = { onScheduleChange(false) },
                    modifier = Modifier.weight(1f)
                )
                ScheduleSegmentButton(
                    label = "总览",
                    selected = showCourseSelectionSchedule,
                    onClick = { onScheduleChange(true) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Surface(
            modifier = Modifier
                .width(118.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onWeekClick),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedWeekLabel(selectedWeek),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (currentWeek > 0) {
                    Text(
                        text = "当前第${currentWeek}周",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleSegmentButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.surface
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun selectedWeekLabel(selectedWeek: Int): String {
    return if (selectedWeek == 0) "全部周" else "第${selectedWeek}周"
}

@Composable
fun CourseScheduleContent(
    modifier: Modifier = Modifier,
    courseList: List<List<CourseEntity>>,
    weekDays: List<String>,
    courseTimes: List<String>,
    selectedWeek: Int
) {
    val scheduleShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .padding(start = 4.dp, end = 4.dp, bottom = 8.dp)
            .clip(scheduleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = scheduleShape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(CourseTimeColumnWidth)
                    .fillMaxHeight()
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedWeekLabel(selectedWeek),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            weekDays.take(7).forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        )
                        .padding(vertical = 11.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val rowHeight = maxOf(CourseRowMinHeight, maxHeight / 6.5f)

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .width(CourseTimeColumnWidth)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f))
                ) {
                    courseTimes.forEach { time ->
                        TimeSlotCell(
                            time = time,
                            modifier = Modifier.height(rowHeight)
                        )
                    }
                }

                CourseScheduleGrid(
                    courseList = courseList,
                    nowWeek = selectedWeek,
                    rowHeight = rowHeight
                )
            }
        }
    }
}

@Composable
private fun TimeSlotCell(
    time: String,
    modifier: Modifier = Modifier
) {
    val parts = time.split("\n")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            )
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = parts.getOrElse(0) { "" },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = parts.getOrElse(1) { "" },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = parts.getOrElse(2) { "" },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CourseScheduleGrid(
    courseList: List<List<CourseEntity>>,
    nowWeek: Int,
    rowHeight: Dp = CourseRowMinHeight
) {
    val weekDays: Int = 7
    val courseTimes: Int = 7
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        repeat(courseTimes) { timeSlot ->
            Row(
                modifier = Modifier
                    .height(rowHeight)
                    .fillMaxWidth()
            ) {
                repeat(weekDays) { day ->
                    val index = timeSlot * (weekDays + 1) + (day + 1)
                    val courses = courseList.getOrNull(index)

                    CourseCell(
                        courses = courses,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        nowWeek = nowWeek
                    )
                }
            }
        }
    }
}

fun parseCourseTime(courseTime: String): List<Int> {
    try {
        val courseTime2 = courseTime.replace("第", "").replace("周", "")
        val courseTimeList = mutableListOf<Int>()
        val timeList = courseTime2.split(",")
        for (time in timeList) {
            if (time.contains("-")) {
                val timeRange = time.split("-")
                for (i in timeRange[0].toInt()..timeRange[1].toInt()) {
                    courseTimeList.add(i)
                }
            } else {
                courseTimeList.add(time.toInt())
            }
        }
        return courseTimeList
    } catch (e: Exception) {
        return emptyList()
    }
}

@Composable
fun CourseCell(
    courses: List<CourseEntity>?,
    modifier: Modifier = Modifier,
    nowWeek: Int,
) {
    var showDetailedCourseInformationDialog by remember { mutableStateOf(false) }
    val coursesFiltered = remember(courses, nowWeek) {
        if (nowWeek != 0) {
            courses.orEmpty().filter { course ->
                val courseTimeList = parseCourseTime(course.courseTime)
                courseTimeList.contains(nowWeek)
            }
        } else {
            courses.orEmpty()
        }
    }
    val hasCourses = coursesFiltered.isNotEmpty()
    val clickableModifier = if (hasCourses) {
        Modifier.clickable { showDetailedCourseInformationDialog = true }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )
            .background(
                if (hasCourses) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                } else {
                    Color.Transparent
                }
            )
            .then(clickableModifier)
            .padding(CourseCellPadding),
        contentAlignment = Alignment.Center
    ) {
        when (coursesFiltered.size) {
            0 -> Unit
            1 -> CourseMiniCard(
                course = coursesFiltered.first(),
                modifier = Modifier.fillMaxSize(),
                showWeek = nowWeek == 0
            )
            else -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                coursesFiltered.take(2).forEach { course ->
                    CourseNameOnlyCard(
                        course = course,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
                if (coursesFiltered.size > 2) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        shape = RoundedCornerShape(5.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "+${coursesFiltered.size - 2}门",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
    if (showDetailedCourseInformationDialog) {
        DetailedCourseInformationDialog(
            courses = coursesFiltered,
            onDismissRequest = {
                showDetailedCourseInformationDialog = false
            }
        )
    }
}

@Composable
private fun CourseMiniCard(
    course: CourseEntity,
    modifier: Modifier = Modifier,
    showWeek: Boolean
) {
    val colors = courseCardColors(course)
    val place = remember(course.coursePlace) { compactCoursePlace(course.coursePlace) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(7.dp),
        color = colors.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val textMeasurer = rememberTextMeasurer()
            val titleWidthPx = with(density) { (maxWidth - 4.dp).coerceAtLeast(1.dp).roundToPx() }
            val placeStyle = TextStyle(
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            )
            val placeConstraints = Constraints(maxWidth = titleWidthPx)
            val buildingLines = textMeasurer.measure(
                text = AnnotatedString(place.building),
                style = placeStyle,
                maxLines = 2,
                constraints = placeConstraints
            ).lineCount
            val roomLines = place.room?.let { room ->
                textMeasurer.measure(
                    text = AnnotatedString(room),
                    style = placeStyle,
                    maxLines = 2,
                    constraints = placeConstraints
                ).lineCount
            } ?: 0
            val footerSpace = ((buildingLines + roomLines + if (showWeek) 1 else 0) * 12 + 12).dp
            val titleHeight = (maxHeight - footerSpace).coerceAtLeast(1.dp)
            val titleHeightPx = with(density) { titleHeight.roundToPx() }
            val nameSize = remember(course.courseName, titleWidthPx, titleHeightPx, density.fontScale) {
                listOf(12.sp, 11.sp, 10.sp, 9.sp).firstOrNull { size ->
                    textMeasurer.measure(
                        text = AnnotatedString(course.courseName),
                        style = TextStyle(
                            fontSize = size,
                            lineHeight = size * 1.18f,
                            fontWeight = FontWeight.SemiBold
                        ),
                        constraints = Constraints(maxWidth = titleWidthPx)
                    ).size.height <= titleHeightPx
                } ?: 9.sp
            }
            val titleMaxLines = (titleHeightPx / with(density) {
                (nameSize * 1.18f).toPx()
            }).toInt().coerceAtLeast(1)

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(colors.stripe)
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 2.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = course.courseName,
                        modifier = Modifier.heightIn(max = titleHeight),
                        style = TextStyle(
                            fontSize = nameSize,
                            lineHeight = nameSize * 1.18f,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.content,
                        maxLines = titleMaxLines,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = place.building,
                        style = placeStyle,
                        color = colors.content.copy(alpha = 0.88f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    place.room?.let { room ->
                        Text(
                            text = room,
                            style = placeStyle,
                            color = colors.content.copy(alpha = 0.88f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (showWeek) {
                        Text(
                            text = course.courseTime,
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.content,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseNameOnlyCard(course: CourseEntity, modifier: Modifier = Modifier) {
    val colors = courseCardColors(course)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(7.dp),
        color = colors.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(colors.stripe)
            )
            Text(
                text = course.courseName,
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp),
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.content,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun compactCoursePlace(place: String): CoursePlaceDisplay {
    val fields = place.split(',', '，', limit = 3)
    val hasCampus = fields.firstOrNull()?.trim() in listOf("海淀西校区", "海淀东校区")
    return when {
        hasCampus && fields.size >= 2 -> CoursePlaceDisplay(
            building = shortBuildingName(fields[1]),
            room = fields.getOrNull(2)
        )
        fields.size == 3 -> CoursePlaceDisplay(
            building = "${fields[0]},${shortBuildingName(fields[1])}",
            room = fields[2]
        )
        fields.size == 2 -> CoursePlaceDisplay(fields[0], fields[1])
        else -> CoursePlaceDisplay(place, null)
    }
}

private fun shortBuildingName(building: String): String {
    return when (building.trim()) {
        "学生活动服务中心" -> "学活"
        "第九教学楼" -> "九教"
        "第七教学楼" -> "七教"
        "第五教学楼" -> "五教"
        "第十七号教学楼" -> "建艺"
        "逸夫教学楼" -> "逸夫楼"
        else -> building
    }
}

@Composable
private fun courseCardColors(course: CourseEntity): CourseCardColors {
    val palette = if (isSystemInDarkTheme()) DarkCourseColors else LightCourseColors
    val seedColor = android.graphics.Color.parseColor(
        Utils.generateRandomColor(course.courseId, false)
    )
    return palette[(seedColor and 0x00FFFFFF) % palette.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedCourseInformationDialog(
    courses: List<CourseEntity>,
    onDismissRequest: () -> Unit = {}
) {

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        content = {
            Surface(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "课程信息",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (courses.size > 1) "本时段共 ${courses.size} 门课程" else "本时段课程详情",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(top = 14.dp, bottom = 14.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    )

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 460.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(courses.size) { index ->
                            CourseDetailCard(courses[index])
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismissRequest) {
                            Text("关闭")
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun CourseDetailCard(course: CourseEntity) {
    val colors = courseCardColors(course)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(colors.stripe)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "课程名称",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                CourseDetailRow("课程编号", course.courseId, Icons.Default.Code)
                CourseDetailRow("教师", course.courseTeacher, Icons.Default.Person)
                CourseDetailRow(
                    "上课时间",
                    course.courseTime,
                    Icons.Default.Schedule
                )
                CourseDetailRow("上课地点", course.coursePlace, Icons.Default.Place)
            }
        }
    }
}

@Composable
fun CourseDetailRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
