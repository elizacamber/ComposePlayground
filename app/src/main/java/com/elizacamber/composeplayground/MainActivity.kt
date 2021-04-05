package com.elizacamber.composeplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elizacamber.composeplayground.ui.theme.ComposePlaygroundTheme
import com.google.accompanist.coil.CoilImage
import kotlinx.coroutines.launch


const val switchToDummy = true

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                if (switchToDummy) MyScreenContent() else PhotographersScreen()
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
//</editor-fold>

//<editor-fold desc="Photographer's card">
@Composable
fun PhotographersScreen() {
    PhotographerCard()
}

@Composable
fun PhotographerCard() {
    Row {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f) // placeholder while image is loading
        ) {
            // Image
        }
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text("Alfred Sisley", fontWeight = FontWeight.Bold)
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text("3 minutes ago", style = MaterialTheme.typography.body2)
            }
        }
    }
}
//</editor-fold>

@Preview(showBackground = true, name = "MyScreen")
@Composable
fun DefaultPreview() {
    MyApp {
        if (switchToDummy) MyScreenContent() else PhotographersScreen()
    }
}