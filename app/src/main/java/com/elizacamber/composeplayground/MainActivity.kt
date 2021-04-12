package com.elizacamber.composeplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elizacamber.composeplayground.ui.theme.ComposePlaygroundTheme
import com.google.accompanist.coil.CoilImage
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


const val switchToDummy = false

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                if (switchToDummy) MyScreenContent() else StaggeredGridScreen()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    ComposePlaygroundTheme {
        Surface(color = Color.White) {
            content()
        }
    }
}

//<editor-fold desc="Dummy screen">
@Composable
fun MyScreenContent(names: List<String> = List(1000) { "Android #$it" }) {
    val counterState = remember { mutableStateOf(0) }

    // We save the scrolling position of the list with this state that can also
    // be used to programmatically scroll the list
    val scrollState = rememberLazyListState()
    // We save the coroutine scope where our animated scroll will be executed
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxHeight()) {
        Row {
            Button(onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollToItem(0)
                }
            }) {
                Text(text = "Scroll on top")
            }
            Button(onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollToItem(names.size - 1)
                }
            }) {
                Text(text = "Scroll to bottom")
            }
        }
        NameList(
            names = names, modifier = Modifier
                .weight(1f), state = scrollState
        )
        Counter(counterState.value, updateCount = { newCount ->
            counterState.value = newCount
        })
    }
}

@Composable
fun Greeting(name: String) {
    val isSelected = remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(targetValue = if (isSelected.value) Color.Red else Color.Transparent)
    Row(verticalAlignment = Alignment.CenterVertically) {
        CoilImage(
            data = "https://developer.android.com/images/brand/Android_Robot.png",
            contentDescription = "Android Logo",
            modifier = Modifier.size(50.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Hello $name!",
            modifier = Modifier
                .padding(24.dp)
                .background(bgColor)
                .clickable { isSelected.value = !isSelected.value },
            style = MaterialTheme.typography.subtitle1.copy(color = Color.Blue)
        )
    }
}

@Composable
fun Counter(count: Int, updateCount: (Int) -> Unit) {
    Button(
        onClick = { updateCount(count + 1) },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (count > 5) Color.Green else Color.White
        ),
    ) {
        Text(text = "I've been hit $count times")
    }
}

@Composable
fun NameList(names: List<String>, modifier: Modifier, state: LazyListState) {
    // LazyColumn is the equivalent of the RecyclerView
    LazyColumn(modifier = modifier, state = state) {
        items(items = names) { name ->
            Greeting(name = name)
            Divider(color = Color.Black)
        }
    }
}

fun Modifier.firstBaselineToTop(
    firstBaselineToTop: Dp
) = Modifier.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)

    // Check the composable has a first baseline
    check(placeable[FirstBaseline] != AlignmentLine.Unspecified)
    val firstBaseline = placeable[FirstBaseline]

    // Height of the composable with padding - first baseline
    val placeableY = firstBaselineToTop.roundToPx() - firstBaseline
    val height = placeable.height + placeableY
    layout(placeable.width, height) {
        // Where the composable gets placed
        placeable.placeRelative(0, placeableY)
    }
}
//</editor-fold>

//<editor-fold desc="Staggered Grid">
@Composable
fun StaggeredGridScreen() {
    val topics = listOf(
        "Arts & Crafts", "Beauty", "Books", "Business", "Comics", "Culinary",
        "Design", "Fashion", "Film", "History", "Maths", "Music", "People", "Philosophy",
        "Religion", "Social sciences", "Technology", "TV", "Writing"
    )
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        StaggeredGrid(modifier = Modifier.padding(8.dp)) {
            for (topic in topics) {
                Chip(modifier = Modifier.padding(8.dp), text = topic)
            }
        }
    }
}

@Composable
fun Chip(modifier: Modifier = Modifier, text: String) {
    Card(
        modifier = modifier,
        border = BorderStroke(color = Color.Black, width = Dp.Hairline),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp, 16.dp)
                    .background(color = MaterialTheme.colors.secondary)
            )
            Spacer(Modifier.width(4.dp))
            Text(text = text)
        }
    }
}

@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    rows: Int = 3,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // Keep track of the width of each row
        val rowWidths = IntArray(rows) { 0 }

        // Keep track of the max height of each row
        val rowMaxHeights = IntArray(rows) { 0 }

        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables = measurables.mapIndexed { index, measurable ->

            // Measure each child
            val placeable = measurable.measure(constraints)

            // Track the width and max height of each row
            val row = index % rows
            rowWidths[row] = rowWidths[row] + placeable.width.absoluteValue
            rowMaxHeights[row] = kotlin.math.max(rowMaxHeights[row], placeable.height.absoluteValue)

            placeable
        }

        // Grid's width is the widest row
        val width = rowWidths.maxOrNull()
            ?.coerceIn(constraints.minWidth.rangeTo(constraints.maxWidth)) ?: constraints.minWidth

        // Grid's height is the sum of the tallest element of each row
        // coerced to the height constraints
        val height = rowMaxHeights.sumBy { it }
            .coerceIn(constraints.minHeight.rangeTo(constraints.maxHeight))

        // Y of each row, based on the height accumulation of previous rows
        val rowY = IntArray(rows) { 0 }
        for (i in 1 until rows) {
            rowY[i] = rowY[i - 1] + rowMaxHeights[i - 1]
        }

        // Set the size of the parent layout
        layout(width, height) {
            // x cord we have placed up to, per row
            val rowX = IntArray(rows) { 0 }

            placeables.forEachIndexed { index, placeable ->
                val row = index % rows
                placeable.placeRelative(
                    x = rowX[row],
                    y = rowY[row]
                )
                rowX[row] += placeable.width
            }
        }
    }
}


//</editor-fold>

@Preview(showBackground = true, name = "MyScreen")
@Composable
fun DefaultPreview() {
    MyApp {
        if (switchToDummy) MyScreenContent() else StaggeredGridScreen()
    }
}