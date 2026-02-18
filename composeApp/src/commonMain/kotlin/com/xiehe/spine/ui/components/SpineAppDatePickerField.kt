package com.xiehe.spine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun SpineDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showing by remember { mutableStateOf(false) }
    val parsed = parseDate(value)
    var year by remember(value) { mutableIntStateOf(parsed.first) }
    var month by remember(value) { mutableIntStateOf(parsed.second) }
    var day by remember(value) { mutableIntStateOf(parsed.third) }

    val maxDay = daysInMonth(year, month)
    if (day > maxDay) {
        day = maxDay
    }

    SpineTextField(
        value = formatDate(year, month, day),
        onValueChange = {},
        placeholder = "请选择出生日期",
        modifier = modifier
            .clickable { showing = true },
        readOnly = true,
        trailingGlyph = SpineGlyph.CALENDAR,
        onTrailingClick = { showing = true },
    )

    if (showing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { showing = false },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(312.dp)
                    .clip(RoundedCornerShape(SpineTheme.radius.lg))
                    .background(SpineTheme.colors.surface)
                    .padding(16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {},
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SpineText(text = "选择出生日期", style = SpineTheme.typography.title)
                DateStepper(
                    label = "年",
                    value = year,
                    min = 1940,
                    max = 2100,
                    onValueChange = { year = it },
                )
                DateStepper(
                    label = "月",
                    value = month,
                    min = 1,
                    max = 12,
                    onValueChange = { month = it },
                )
                DateStepper(
                    label = "日",
                    value = day,
                    min = 1,
                    max = daysInMonth(year, month),
                    onValueChange = { day = it },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SpineButton(
                        text = "取消",
                        onClick = { showing = false },
                        modifier = Modifier.weight(1f).height(44.dp),
                    )
                    SpineButton(
                        text = "确定",
                        onClick = {
                            onValueChange(formatDate(year, month, day))
                            showing = false
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DateStepper(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpineText(text = label, style = SpineTheme.typography.subhead)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            MiniStepButton(text = "-") { onValueChange((value - 1).coerceAtLeast(min)) }
            SpineText(text = value.toString(), style = SpineTheme.typography.title)
            MiniStepButton(text = "+") { onValueChange((value + 1).coerceAtMost(max)) }
        }
    }
}

@Composable
private fun MiniStepButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(30.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(SpineTheme.radius.full))
            .background(SpineTheme.colors.primaryMuted)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        SpineText(text = text, style = SpineTheme.typography.title.copy(color = SpineTheme.colors.primary))
    }
}

private fun parseDate(value: String): Triple<Int, Int, Int> {
    val parts = value.split('-')
    val y = parts.getOrNull(0)?.toIntOrNull() ?: 1990
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 1
    val d = parts.getOrNull(2)?.toIntOrNull() ?: 1
    return Triple(y, m.coerceIn(1, 12), d.coerceIn(1, 31))
}

private fun formatDate(year: Int, month: Int, day: Int): String {
    return "%04d-%02d-%02d".format(year, month, day)
}

private fun daysInMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}
