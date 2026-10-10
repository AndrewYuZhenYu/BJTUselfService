package team.bjtuss.bjtuselfservice.screen

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.TextUtils
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import team.bjtuss.bjtuselfservice.repository.SchoolCalendarDay
import team.bjtuss.bjtuselfservice.repository.SchoolCalendarRepository
import team.bjtuss.bjtuselfservice.repository.SchoolCalendarSemester
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

private val weekdayNames = listOf("一", "二", "三", "四", "五", "六", "日")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolCalendarScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var reloadKey by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf<Result<List<SchoolCalendarSemester>>?>(null) }
    var selectedSemesterId by rememberSaveable { mutableIntStateOf(-1) }
    var semesterMenuExpanded by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf<SchoolCalendarDay?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(reloadKey) {
        result = null
        result = SchoolCalendarRepository.load()
    }

    val semesters = result?.getOrNull().orEmpty()
    val semester = semesters.firstOrNull { it.id == selectedSemesterId } ?: semesters.firstOrNull()
    val months = remember(semester) { semester?.calendarMonths().orEmpty() }
    LaunchedEffect(semester?.id) {
        if (semester != null) {
            val todayIndex = months.indexOf(YearMonth.from(LocalDate.now()))
            listState.scrollToItem(if (todayIndex >= 0) todayIndex + 1 else 0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("校历查看") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { reloadKey++ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新校历")
                    }
                    IconButton(
                        enabled = semester != null,
                        onClick = { semester?.let { SchoolCalendarPrinter.print(context, it) } }
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "打印或保存 PDF")
                    }
                }
            )
        }
    ) { insets ->
        when {
            result == null -> Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            result?.isFailure == true -> Column(
                modifier = Modifier.fillMaxSize().padding(insets).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(result?.exceptionOrNull()?.message ?: "校历加载失败", textAlign = TextAlign.Center)
                Button(onClick = { reloadKey++ }, modifier = Modifier.padding(top = 16.dp)) { Text("重试") }
            }
            semester != null -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(insets),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                item {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Box {
                            OutlinedButton(onClick = { semesterMenuExpanded = true }) {
                                Text(semester.title)
                                Icon(Icons.Default.ExpandMore, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = semesterMenuExpanded,
                                onDismissRequest = { semesterMenuExpanded = false }
                            ) {
                                semesters.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.title) },
                                        onClick = {
                                            selectedSemesterId = option.id
                                            selectedDay = null
                                            semesterMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Text(
                            "日期与教学周以学校校历为准",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                itemsIndexed(months, key = { _, month -> month.toString() }) { _, month ->
                    SchoolCalendarMonth(month, semester) { selectedDay = it }
                }
                if (semester.remarks.isNotEmpty()) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("校方说明", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            semester.remarks.forEach { remark ->
                                Text(remark, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    selectedDay?.let { day ->
        AlertDialog(
            onDismissRequest = { selectedDay = null },
            title = { Text("${day.date.year}年${day.date.monthValue}月${day.date.dayOfMonth}日") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("星期${weekdayNames[day.date.dayOfWeek.value - 1]}")
                    if (day.weekLabel.isNotBlank()) Text(day.weekLabel, fontWeight = FontWeight.SemiBold)
                    if (day.plan.isNotBlank()) Text(day.plan)
                }
            },
            confirmButton = { TextButton(onClick = { selectedDay = null }) { Text("关闭") } }
        )
    }
}

private fun SchoolCalendarSemester.calendarMonths(): List<YearMonth> {
    val visible = days.values.filter { it.inSemester }.ifEmpty { days.values.toList() }
    val first = YearMonth.from(visible.minOf { it.date })
    val last = YearMonth.from(visible.maxOf { it.date })
    return generateSequence(first) { it.plusMonths(1) }.takeWhile { !it.isAfter(last) }.toList()
}

private fun calendarRows(month: YearMonth): List<List<LocalDate>> {
    val start = month.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val end = month.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    return generateSequence(start) { it.plusDays(1) }
        .takeWhile { !it.isAfter(end) }.toList().chunked(7)
}

@Composable
private fun SchoolCalendarMonth(
    month: YearMonth,
    semester: SchoolCalendarSemester,
    onDateClick: (SchoolCalendarDay) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text("${month.year}年 ${month.monthValue}月", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayNames.forEach { name ->
                Text("周$name", modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(8.dp))
        calendarRows(month).forEach { week ->
            val weekLabel = semester.days[week.first()]?.weekLabel.orEmpty()
            if (weekLabel.isNotBlank()) {
                Text(weekLabel, style = MaterialTheme.typography.labelMedium,
                    color = if (weekLabel == "休") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 4.dp))
            } else Spacer(Modifier.height(18.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val day = semester.days[date]
                    val inMonth = YearMonth.from(date) == month
                    val today = date == LocalDate.now() && inMonth
                    val shape = RoundedCornerShape(6.dp)
                    Box(
                        modifier = Modifier.weight(1f).padding(2.dp).height(44.dp)
                            .clip(shape)
                            .background(if (today) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .then(if (today) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, shape) else Modifier)
                            .clickable(enabled = day != null && inMonth) { day?.let(onDateClick) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            val color = when {
                                !inMonth || !day.inSemester -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                                !day.regularDay -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                            Text(date.dayOfMonth.toString(), color = color,
                                fontWeight = if (today || day.emphasized) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

private object SchoolCalendarPrinter {
    private var pendingWebView: WebView? = null

    fun print(context: Context, semester: SchoolCalendarSemester) {
        val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "${semester.title}校历"
        val webView = WebView(context)
        pendingWebView = webView
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                manager.print(jobName, view.createPrintDocumentAdapter(jobName), PrintAttributes.Builder().build())
                pendingWebView = null
            }
        }
        webView.loadDataWithBaseURL(null, printHtml(semester), "text/html", "UTF-8", null)
    }

    private fun printHtml(semester: SchoolCalendarSemester): String = buildString {
        append("<html><head><meta charset='utf-8'><style>")
        append("body{font-family:sans-serif;color:#20232a;margin:24px}h1{font-size:22px}")
        append("h2{font-size:18px;margin:25px 0 8px}table{width:100%;border-collapse:collapse;table-layout:fixed}")
        append("th,td{text-align:center;padding:7px;border:1px solid #ddd}th{background:#f0f2f6}")
        append(".week td{text-align:left;background:#f6f7fa;font-size:12px;font-weight:bold}")
        append(".off{color:#b03939}.outside{color:#a0a0a0}.notes{margin-top:24px}")
        append("</style></head><body><h1>")
        append(TextUtils.htmlEncode(semester.title)).append(" 校历</h1>")
        semester.calendarMonths().forEach { month ->
            append("<h2>${month.year}年 ${month.monthValue}月</h2><table><tr>")
            weekdayNames.forEach { append("<th>周$it</th>") }
            append("</tr>")
            calendarRows(month).forEach { week ->
                val label = semester.days[week.first()]?.weekLabel.orEmpty()
                if (label.isNotBlank()) append("<tr class='week'><td colspan='7'>${TextUtils.htmlEncode(label)}</td></tr>")
                append("<tr>")
                week.forEach { date ->
                    val day = semester.days[date]
                    val css = when {
                        YearMonth.from(date) != month || day?.inSemester == false -> "outside"
                        day?.regularDay == false -> "off"
                        else -> ""
                    }
                    append("<td class='$css'>${if (day == null) "" else date.dayOfMonth}</td>")
                }
                append("</tr>")
            }
            append("</table>")
        }
        if (semester.remarks.isNotEmpty()) {
            append("<div class='notes'><h2>校方说明</h2>")
            semester.remarks.forEach { append("<p>${TextUtils.htmlEncode(it)}</p>") }
            append("</div>")
        }
        append("</body></html>")
    }
}
