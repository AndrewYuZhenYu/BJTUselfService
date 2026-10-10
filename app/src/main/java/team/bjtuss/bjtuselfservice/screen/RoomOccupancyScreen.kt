package team.bjtuss.bjtuselfservice.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import team.bjtuss.bjtuselfservice.repository.RoomOccupancy
import team.bjtuss.bjtuselfservice.repository.RoomOccupancyStatus
import team.bjtuss.bjtuselfservice.repository.RoomOccupancyWeek
import team.bjtuss.bjtuselfservice.repository.RoomOccupancyRepository
import team.bjtuss.bjtuselfservice.viewmodel.MainViewModel
import team.bjtuss.bjtuselfservice.viewmodel.RoomOccupancyState
import java.time.LocalDate
import java.time.LocalTime

private val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
private val periodTimes = listOf(
    "08:00–09:50", "10:10–12:00", "12:10–14:00", "14:10–16:00",
    "16:20–18:10", "19:00–20:50", "21:00–21:50",
)
private val datePattern = Regex("""\d{1,2}月\d{1,2}日""")
private val datePartsPattern = Regex("""(\d{1,2})月(\d{1,2})日""")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomOccupancyScreen(mainViewModel: MainViewModel, onBack: () -> Unit) {
    val viewModel = mainViewModel.classroomViewModel
    val state by viewModel.roomOccupancy.collectAsState()
    var selectedDay by rememberSaveable { mutableIntStateOf(LocalDate.now().dayOfWeek.value - 1) }
    var selectedPeriod by rememberSaveable { mutableIntStateOf(defaultPeriod()) }
    var requestedWeek by rememberSaveable { mutableStateOf<Int?>(null) }
    var onlyFree by rememberSaveable { mutableStateOf(true) }
    var search by rememberSaveable { mutableStateOf("") }
    var selectedBuilding by rememberSaveable { mutableStateOf(RoomOccupancyRepository.ALL_BUILDINGS) }
    var weekMenuExpanded by remember { mutableStateOf(false) }
    var dialogWeekMenuExpanded by remember { mutableStateOf(false) }
    var buildingMenuExpanded by remember { mutableStateOf(false) }
    var datePickerExpanded by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf<RoomOccupancy?>(null) }
    var lastWeek by remember { mutableStateOf<RoomOccupancyWeek?>(null) }
    val dayListState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    LaunchedEffect(Unit) {
        if (selectedBuilding.isBlank()) selectedBuilding = RoomOccupancyRepository.ALL_BUILDINGS
        viewModel.loadRoomOccupancy(requestedWeek, selectedBuilding)
    }
    val data = (state as? RoomOccupancyState.Ready)?.week
    LaunchedEffect(data?.week) {
        if (data != null) {
            lastWeek = data
            selectedDay = if (data.week == data.currentWeek) LocalDate.now().dayOfWeek.value - 1 else 0
        }
    }
    LaunchedEffect(data?.week, selectedDay) {
        if (data != null) dayListState.animateScrollToItem((selectedDay - 1).coerceAtLeast(0))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("教室占用") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadRoomOccupancy(requestedWeek, selectedBuilding, forceRefresh = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新教室占用")
                    }
                }
            )
        }
    ) { insets ->
        when (val currentState = state) {
            RoomOccupancyState.Idle, RoomOccupancyState.Loading -> {
                Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RoomOccupancyState.Failed -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(insets).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(currentState.message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Button(
                        onClick = { viewModel.loadRoomOccupancy(requestedWeek, selectedBuilding, forceRefresh = true) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) { Text("重试") }
                }
            }
            is RoomOccupancyState.Ready -> {
                val week = currentState.week
                val visibleRooms = remember(week.rooms, selectedDay, selectedPeriod, onlyFree, search) {
                    week.rooms.filter { room ->
                        (search.isBlank() || room.name.contains(search.trim(), ignoreCase = true)) &&
                            (!onlyFree || room.status(selectedDay, selectedPeriod) == RoomOccupancyStatus.FREE)
                    }.sortedBy { it.name }
                }

                Column(modifier = Modifier.fillMaxSize().padding(insets)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(onClick = { datePickerExpanded = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("日期选择")
                        }
                        Box {
                            OutlinedButton(onClick = { weekMenuExpanded = true }) {
                                Text("第${week.week}周")
                                Icon(Icons.Default.ExpandMore, contentDescription = null)
                            }
                            DropdownMenu(expanded = weekMenuExpanded, onDismissRequest = { weekMenuExpanded = false }) {
                                week.availableWeeks.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text("第${option}周" + if (option == week.currentWeek) " · 本周" else "") },
                                        onClick = {
                                            weekMenuExpanded = false
                                            if (option != week.week) {
                                                requestedWeek = option
                                                viewModel.loadRoomOccupancy(option, selectedBuilding)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        "正在查看 · ${weekDays[selectedDay]} ${datePattern.find(week.dayLabels.getOrNull(selectedDay).orEmpty())?.value ?: "日期待确认"}",
                        modifier = Modifier.padding(start = 18.dp, bottom = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        state = dayListState,
                        contentPadding = PaddingValues(start = 16.dp, end = screenWidth / 2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(7) { day ->
                            val date = datePattern.find(week.dayLabels.getOrNull(day).orEmpty())?.value
                            FilterChip(
                                selected = selectedDay == day,
                                onClick = { selectedDay = day },
                                label = { Text(if (date == null) weekDays[day] else "${weekDays[day]} $date") }
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(7) { period ->
                            FilterChip(
                                selected = selectedPeriod == period,
                                onClick = { selectedPeriod = period },
                                label = { Text("第${period + 1}节") }
                            )
                        }
                    }

                    Text(
                        periodTimes[selectedPeriod],
                        modifier = Modifier.padding(start = 18.dp, bottom = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            label = { Text("搜索教室") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Box(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                        OutlinedButton(onClick = { buildingMenuExpanded = true }) {
                            Text(
                                if (selectedBuilding == RoomOccupancyRepository.ALL_BUILDINGS) "全部楼宇"
                                else week.buildings.firstOrNull { it.value == selectedBuilding }?.name ?: "选择教学楼"
                            )
                            Icon(Icons.Default.ExpandMore, contentDescription = null)
                        }
                        DropdownMenu(expanded = buildingMenuExpanded, onDismissRequest = { buildingMenuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("全部楼宇") },
                                onClick = {
                                    selectedBuilding = RoomOccupancyRepository.ALL_BUILDINGS
                                    buildingMenuExpanded = false
                                    viewModel.loadRoomOccupancy(week.week, selectedBuilding)
                                }
                            )
                            week.buildings.forEach { building ->
                                DropdownMenuItem(
                                    text = { Text(building.name) },
                                    onClick = {
                                        selectedBuilding = building.value
                                        buildingMenuExpanded = false
                                        viewModel.loadRoomOccupancy(week.week, building.value)
                                    }
                                )
                            }
                        }
                    }

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        listOf("空闲", "全部").forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = onlyFree == (index == 0),
                                onClick = { onlyFree = index == 0 },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                            ) { Text(label) }
                        }
                    }

                    OccupancyLegend()

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${visibleRooms.size} 间教室", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text("第${selectedPeriod + 1}节", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
                    ) {
                        item { OccupancyHeader() }
                        if (visibleRooms.isEmpty()) {
                            item {
                                Text(
                                    if (selectedBuilding.isBlank()) "请选择教学楼查看教室"
                                    else if (onlyFree) "这一节没有符合条件的空闲教室" else "没有匹配的教室",
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(visibleRooms, key = { it.name }) { room ->
                                OccupancyRoomRow(room, selectedDay, selectedPeriod) { selectedRoom = room }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }

                selectedRoom?.let { room ->
                    RoomOccupancyDialog(room, week, selectedDay) { selectedRoom = null }
                }
            }
        }
    }

    if (datePickerExpanded) {
        (data ?: lastWeek)?.let { week ->
            val weekIndex = week.availableWeeks.indexOf(week.week)
            val loading = state is RoomOccupancyState.Loading
            AlertDialog(
                onDismissRequest = { datePickerExpanded = false },
                title = { Text("日期选择") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            IconButton(
                                enabled = !loading && weekIndex > 0,
                                onClick = {
                                    val option = week.availableWeeks[weekIndex - 1]
                                    requestedWeek = option
                                    viewModel.loadRoomOccupancy(option, selectedBuilding)
                                }
                            ) { Icon(Icons.Default.ChevronLeft, contentDescription = "上一教学周") }
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                TextButton(onClick = { dialogWeekMenuExpanded = true }, enabled = !loading) {
                                    Text("第${week.week}教学周")
                                    Icon(Icons.Default.ExpandMore, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = dialogWeekMenuExpanded,
                                    onDismissRequest = { dialogWeekMenuExpanded = false }
                                ) {
                                    week.availableWeeks.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text("第${option}周" + if (option == week.currentWeek) " · 本周" else "") },
                                            onClick = {
                                                dialogWeekMenuExpanded = false
                                                if (option != week.week) {
                                                    requestedWeek = option
                                                    viewModel.loadRoomOccupancy(option, selectedBuilding)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            IconButton(
                                enabled = !loading && weekIndex >= 0 && weekIndex < week.availableWeeks.lastIndex,
                                onClick = {
                                    val option = week.availableWeeks[weekIndex + 1]
                                    requestedWeek = option
                                    viewModel.loadRoomOccupancy(option, selectedBuilding)
                                }
                            ) { Icon(Icons.Default.ChevronRight, contentDescription = "下一教学周") }
                        }
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else if (state is RoomOccupancyState.Failed) {
                            Text((state as RoomOccupancyState.Failed).message,
                                color = MaterialTheme.colorScheme.error)
                        } else {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                weekDays.forEachIndexed { day, label ->
                                    val date = datePartsPattern.find(week.dayLabels.getOrNull(day).orEmpty())
                                    val selected = day == selectedDay && week.week == data?.week
                                    Column(
                                        modifier = Modifier.weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                            .clickable(enabled = date != null) {
                                                selectedDay = day
                                                datePickerExpanded = false
                                            }
                                            .padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                        Text(date?.groupValues?.get(1)?.let { "${it}月" } ?: "--",
                                            style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                        Text(date?.groupValues?.get(2) ?: "--",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { datePickerExpanded = false }) { Text("关闭") } }
            )
        }
    }
}

private fun defaultPeriod(): Int {
    val now = LocalTime.now()
    return periodTimes.indexOfFirst { now.isBefore(LocalTime.parse(it.substringAfter('–'))) }
        .takeIf { it >= 0 } ?: 6
}

@Composable
private fun OccupancyHeader() {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("教室", modifier = Modifier.width(86.dp), style = MaterialTheme.typography.labelMedium)
        repeat(7) { period ->
            Text("${period + 1}", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
        }
    }
    HorizontalDivider()
}

@Composable
private fun OccupancyRoomRow(room: RoomOccupancy, day: Int, selectedPeriod: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(86.dp)) {
            Text(room.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            room.capacity?.let {
                Text("${it}座", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        repeat(7) { period ->
            val status = room.status(day, period)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 1.dp)
                    .height(30.dp)
                    .background(occupancyColor(status), RoundedCornerShape(3.dp))
                    .border(
                        width = if (period == selectedPeriod) 2.dp else 1.dp,
                        color = if (period == selectedPeriod) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(3.dp)
                    )
                    .semantics { contentDescription = "第${period + 1}节${status.label}" }
            )
        }
    }
}

@Composable
private fun OccupancyLegend() {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoomOccupancyStatus.entries.forEach { status ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    Modifier.size(10.dp).background(occupancyColor(status), RoundedCornerShape(2.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                )
                Text(status.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun RoomOccupancyDialog(
    room: RoomOccupancy,
    week: RoomOccupancyWeek,
    day: Int,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(room.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("第${week.week}周 · ${week.dayLabels.getOrNull(day) ?: weekDays[day]}")
                repeat(7) { period ->
                    val status = room.status(day, period)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).background(occupancyColor(status), RoundedCornerShape(2.dp)))
                        Text("第${period + 1}节  ${periodTimes[period]}  ${status.label}", modifier = Modifier.padding(start = 10.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
private fun occupancyColor(status: RoomOccupancyStatus): Color = when (status) {
    RoomOccupancyStatus.FREE -> MaterialTheme.colorScheme.surface
    RoomOccupancyStatus.SCHEDULED -> Color(0xFFE46868)
    RoomOccupancyStatus.ADJUSTED -> Color(0xFF9E6868)
    RoomOccupancyStatus.EXAM -> Color(0xFF5671D5)
    RoomOccupancyStatus.LAB -> Color(0xFF77BF6D)
    RoomOccupancyStatus.OTHER -> Color(0xFFD8CC56)
    RoomOccupancyStatus.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
}
