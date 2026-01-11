package dev.girlz.drone_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.girlz.drone_app.ui.profile.ProfileViewModel
import dev.girlz.drone_app.ui.profile.ProfileViewModelFactory
import dev.girlz.drone_app.ui.theme.DroneappTheme
import dev.girlz.drone_app.ui.noise.NoiseScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroneappTheme {
                DroneappApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun DroneappApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            val modifier = Modifier.padding(innerPadding).padding(16.dp)
            when (currentDestination) {
                AppDestinations.HOME -> Greeting(name = "mia", modifier = modifier)
                AppDestinations.NOISE -> NoiseScreen(modifier = modifier)
                AppDestinations.SHOCK -> ShockScreen(modifier = modifier)
                AppDestinations.TESTING -> TestingScreen(modifier = modifier)
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    NOISE("Noise", Icons.Default.GraphicEq),
    SHOCK("Shock", Icons.Default.Warning),
    TESTING("Testing", Icons.Default.Science),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "hello $name",
        modifier = modifier
    )
}

@Composable
fun TestingScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context.applicationContext)
    )
    val dummyItems by viewModel.dummyItems.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }
    var value by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier) {
        Text(text = "Testing")
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("Value") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                val currentName = name.trim()
                val currentValue = value.trim()
                if (currentName.isNotBlank() && currentValue.isNotBlank()) {
                    viewModel.insert(currentName, currentValue)
                    name = ""
                    value = ""
                }
            }
        ) {
            Text(text = "Save dummy")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Saved entries")
        dummyItems.forEach { item ->
            Text(text = "${item.name}: ${item.value}")
        }
    }
}

@Composable
fun ShockScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Under construction",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DroneappTheme {
        Greeting("mia")
    }
}
