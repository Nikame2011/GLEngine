package com.nikame.spacerunner1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikame.spacerunner1.ui.theme.GLCoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_SpaceRunner)
        super.onCreate(savedInstanceState)
        setContent {
            GLCoreTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android") { finish() }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, onExitClick: () -> Unit) {

    val context = LocalContext.current

    Box(
        modifier = with(Modifier) {
            fillMaxSize()
                .paint(
                    // Replace with your image id
                    painterResource(id = R.drawable.ic_launcher_background),
                    contentScale = ContentScale.FillHeight
                )

        }) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = "Space: Runner-1",
                fontSize = 30.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Surface(
                shape = MaterialTheme.shapes.medium, shadowElevation = 1.dp,
                modifier = Modifier.padding(bottom = 5.dp)
            ) {
                Row {
                    Text(
                        text = "Hello,",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(all = 4.dp)
                    )

                    Text(
                        text = name,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(all = 4.dp),
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1F))

            Text(
                text = "Hello bluad",
                modifier = Modifier.padding(bottom = 5.dp)
            )

            MenuButton(stringResource(R.string.new_game)) {
                context.startActivity(
                    Intent(
                        context,
                        GameActivity::class.java
                    )
                )
            }
            MenuButton("Hello bluad' opyat'") {}
            MenuButton("Hello bluad' trizdi") {}
            MenuButton(stringResource(R.string.exit), onExitClick)

            Spacer(modifier = Modifier.weight(1F))
        }
    }
}

@Composable
fun MenuButton(name: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(fraction = 0.9f).padding(bottom = 15.dp)
    ) {
        Text(
            text = name
        )
    }
}

@Preview(
    name = "Light Mode", showBackground = true

)
//@Preview(
//    uiMode = Configuration.UI_MODE_NIGHT_YES,
//    showBackground = true,
//    name = "Dark Mode"
//)
@Composable
fun GreetingPreview() {
    GLCoreTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Greeting("Android") {}
        }
    }
}