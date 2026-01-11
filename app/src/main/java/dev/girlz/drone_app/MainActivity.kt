package dev.girlz.drone_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.girlz.drone_app.data.local.LocalDatabase
import dev.girlz.drone_app.data.local.SeedData
import dev.girlz.drone_app.ui.noise.NoisePresetViewModel
import dev.girlz.drone_app.ui.noise.NoisePresetViewModelFactory
import dev.girlz.drone_app.ui.noise.NoiseScreen
import dev.girlz.drone_app.ui.command.CommandScreen
import dev.girlz.drone_app.ui.command.CommandViewModel
import dev.girlz.drone_app.ui.command.CommandViewModelFactory
import dev.girlz.drone_app.ui.theme.DroneappTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                AppDestinations.NOISE -> NoiseScreen(modifier = modifier)
                AppDestinations.COMMAND -> CommandScreen(modifier = modifier)
                AppDestinations.SHOCK -> ShockScreen(modifier = modifier)
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
    COMMAND("Command", Icons.Default.RecordVoiceOver),
    SHOCK("Shock", Icons.Default.Warning),
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
    val noiseViewModel: NoisePresetViewModel = viewModel(
        factory = NoisePresetViewModelFactory(context.applicationContext)
    )
    val commandViewModel: CommandViewModel = viewModel(
        factory = CommandViewModelFactory(context.applicationContext)
    )
    val presets by noiseViewModel.presets.collectAsState()
    val playingPresetId by noiseViewModel.playingPresetId.collectAsState()
    val commands by commandViewModel.commands.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.verticalScroll(scrollState)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Home")
            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val database = LocalDatabase.getInstance(context.applicationContext)
                        SeedData.seed(database)
                    }
                }
            ) {
                Text(text = "Seed")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (presets.isEmpty()) {
            Text(text = "No saved noise presets yet.")
        } else {
            Text(text = "Saved noise presets")
            Spacer(modifier = Modifier.height(8.dp))
            presets.forEach { preset ->
                val isPlaying = preset.id == playingPresetId
                Button(onClick = { noiseViewModel.togglePreset(preset) }) {
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
        Spacer(modifier = Modifier.height(16.dp))
        if (commands.isEmpty()) {
            Text(text = "No saved commands yet.")
        } else {
            Text(text = "Saved commands")
            Spacer(modifier = Modifier.height(8.dp))
            commands.forEach { command ->
                Button(onClick = { commandViewModel.playCommand(command) }) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = command.text)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
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
