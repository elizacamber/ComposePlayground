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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elizacamber.composeplayground.ui.theme.ComposePlaygroundTheme
import com.google.accompanist.coil.CoilImage
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


const val DUMMY = 0
const val STAGGERED = 1
const val MORE_LESS_TEXT = 2

const val selectedLayout = 2

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                when (selectedLayout) {
                    0 -> MyScreenContent()
                    1 -> StaggeredGridScreen()
                    2 -> MoreLessTextScreen()
                }
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

@Composable
fun MoreLessTextScreen() {
    val texts = listOf(
        "a dummy text that should show full in two lines",
        "a dummy text that should not be too long to show entirely in juuust two lines",
        "a dummy text that should be too long to show entirely in two lines and should show the expand buttons",
        "a dummy text that should be too long to show entirely in two lines and should show the expand buttons",
        "a dummy text that should be too long to show entirely in two lines and should show the expand buttons",
        "a dummy text that should be too long to show entirely in two lines and should show the expand buttons"
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        items(items = texts) { text ->
            MoreLessText(
                text = text,
                collapsedTag = "show more",
                expandedTag = "show less",
                maxLines = 2
            )
            Divider(color = Color.Black)
        }
    }
}

/**
 * A text that will collapse if its length is extended further than a specified amount
 * of lines. It will then ellipsize the text and show a string tag, indicating to the user
 * that there's more text to be shown once tapped. When it's fully expanded a new tag will
 * be added indicating that the text can be collapsed again.
 * If the length of the size fits the number of maxLines, no tags are added.
 *
 * @param collapsedTag the tag showing when the text is collapsed. Default is "show more"
 * @param expandedTag the tag showing when the text is already expanded. Default is "show less"
 * @param maxLines the number of lines the text should fill. Default is 2
 * @param collapsedTagSpace the space and/or characters between the text and the 'collapsedTag'. Default is ellipsis followed by 5 space characters
 * @param expandedTagSpace the space and/or characters between the text and the 'expandedTag'. Default is 5 space characters
 * @param spanStyle the style the 'collapsedTag' and the 'expandedTag' are using. Default is underline and bold.
 */
@Composable
fun MoreLessText(
    text: String,
    collapsedTag: String = "show more",
    expandedTag: String = "show less",
    maxLines: Int = 2,
    collapsedTagSpace: String = "...     ",
    expandedTagSpace: String = "     ",
    spanStyle: SpanStyle = SpanStyle(
        textDecoration = TextDecoration.Underline,
        fontWeight = FontWeight.Bold
    )
) {
    var collapsedText = buildAnnotatedString { append(text) }
    val expandedText = buildAnnotatedString {
        append(text)
        append(expandedTagSpace)
        pushStyle(spanStyle)
        append(expandedTag)
        pop()
    }

    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(buildAnnotatedString { append(text) }) }
    var allowToggle by remember { mutableStateOf(false) }
    Column(
        Modifier
            .toggleable(value = isExpanded, onValueChange = {
                if (allowToggle) {
                    isExpanded = it
                    selectedText = if (isExpanded) expandedText else collapsedText
                }
            })
    ) {
        Text(
            text = selectedText,
            onTextLayout = {
                if (it.lineCount > maxLines) {
                    allowToggle = true
                    val lineEndIndex = it.getLineEnd(maxLines - 1)
                    collapsedText =
                        buildAnnotatedString {
                            append(
                                text.substring(
                                    0,
                                    lineEndIndex - collapsedTag.length - collapsedTagSpace.length
                                )
                            )
                            append(collapsedTagSpace)
                            pushStyle(spanStyle)
                            append(collapsedTag)
                            pop()
                        }
                    selectedText = if (isExpanded) expandedText else collapsedText
                }
            }
        )
    }
}

@Composable
fun AnnotatedClickableText() {
    Text(buildAnnotatedString {
        append("Click ")

        // We attach this *URL* annotation to the following content
        // until `pop()` is called
        pushStringAnnotation(
            tag = "URL",
            annotation = "https://developer.android.com"
        )
        withStyle(
            style = SpanStyle(
                color = Color.Blue,
                fontWeight = FontWeight.Bold
            )
        ) {
            append("here")
        }
        pop()
    })
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
        when (selectedLayout) {
            0 -> MyScreenContent()
            1 -> StaggeredGridScreen()
            2 -> MoreLessTextScreen()
        }
    }
}