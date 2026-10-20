package eu.hxreborn.tfs.ui.component

import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

private val StackedGap = 8.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AdaptiveButtonRow(
    count: Int,
    modifier: Modifier = Modifier,
    button: @Composable (index: Int, stacked: Boolean) -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val rowGap = ButtonGroupDefaults.ConnectedSpaceBetween.roundToPx()
        val slot = (constraints.maxWidth - rowGap * (count - 1)) / count
        val fits =
            subcompose("probe") { repeat(count) { button(it, false) } }
                .all { it.maxIntrinsicWidth(Constraints.Infinity) <= slot }
        val stacked = !fits
        val width = if (stacked) constraints.maxWidth else slot
        val placeables =
            subcompose(stacked) { repeat(count) { button(it, stacked) } }
                .map { it.measure(Constraints.fixedWidth(width)) }
        val gap = if (stacked) StackedGap.roundToPx() else rowGap
        val height =
            if (stacked) {
                placeables.sumOf { it.height } + gap * (count - 1)
            } else {
                placeables.maxOf { it.height }
            }
        layout(constraints.maxWidth, height) {
            var offset = 0
            placeables.forEach {
                if (stacked) {
                    it.placeRelative(0, offset)
                    offset += it.height + gap
                } else {
                    it.placeRelative(offset, (height - it.height) / 2)
                    offset += it.width + gap
                }
            }
        }
    }
}
