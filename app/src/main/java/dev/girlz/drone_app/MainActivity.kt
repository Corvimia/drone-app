package dev.girlz.drone_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import dev.girlz.drone_app.ui.noise.NoisePresetViewModel
import dev.girlz.drone_app.ui.noise.NoisePresetViewModelFactory

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
                AppDestinations.HOME -> HomeScreen(modifier = modifier)
                AppDestinations.FAVORITES -> Greeting(name = "favorites", modifier = modifier)
                AppDestinations.PROFILE -> ProfileScreen(modifier = modifier)
                AppDestinations.NOISE -> NoiseScreen(modifier = modifier)
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
    NOISE("Noise", Icons.Default.GraphicEq),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "hello $name",
        modifier = modifier
    )
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: NoisePresetViewModel = viewModel(
        factory = NoisePresetViewModelFactory(context.applicationContext)
    )
    val presets by viewModel.presets.collectAsState()
    val playingPresetId by viewModel.playingPresetId.collectAsState()
    val scrollState = rememberScrollState()

    Column(modifier = modifier.verticalScroll(scrollState)) {
        Text(text = "Home")
        Spacer(modifier = Modifier.height(12.dp))
        if (presets.isEmpty()) {
            Text(text = "No saved noise presets yet.")
        } else {
            Text(text = "Saved noise presets")
            Spacer(modifier = Modifier.height(8.dp))
            presets.forEach { preset ->
                val isPlaying = preset.id == playingPresetId
                Button(onClick = { viewModel.togglePreset(preset) }) {
                    Row {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = preset.name)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context.applicationContext)
    )
    val dummyItems by viewModel.dummyItems.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }
    var value by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier) {
        Text(text = "Profile")
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DroneappTheme {
        Greeting("mia")
    }
}
