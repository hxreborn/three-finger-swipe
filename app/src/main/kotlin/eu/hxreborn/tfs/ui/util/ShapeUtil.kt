package eu.hxreborn.tfs.ui.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val shapeOnly = RoundedCornerShape(24.dp)

private val shapeFirst =
    RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomEnd = 4.dp,
        bottomStart = 4.dp,
    )

private val shapeLast =
    RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 4.dp,
        bottomEnd = 24.dp,
        bottomStart = 24.dp,
    )

private val shapeMiddle = RoundedCornerShape(4.dp)

fun shapeForPosition(
    count: Int,
    index: Int,
): RoundedCornerShape =
    when {
        count == 1 -> shapeOnly
        index == 0 -> shapeFirst
        index == count - 1 -> shapeLast
        else -> shapeMiddle
    }
