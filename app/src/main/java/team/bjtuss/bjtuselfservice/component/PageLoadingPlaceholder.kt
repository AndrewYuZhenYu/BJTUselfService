package team.bjtuss.bjtuselfservice.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun PageLoadingPlaceholder(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.surfaceVariant
    LazyColumn(
        modifier = modifier.fillMaxSize().semantics { contentDescription = "正在加载" },
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        userScrollEnabled = false
    ) {
        item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp)) }
        items(5) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(48.dp).background(color, RoundedCornerShape(8.dp)))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.fillMaxWidth(0.68f).height(18.dp).background(color, RoundedCornerShape(4.dp)))
                    Box(Modifier.fillMaxWidth().height(12.dp).background(color.copy(alpha = 0.55f), RoundedCornerShape(4.dp)))
                    Box(Modifier.fillMaxWidth(0.45f).height(12.dp).background(color.copy(alpha = 0.55f), RoundedCornerShape(4.dp)))
                }
            }
        }
    }
}
