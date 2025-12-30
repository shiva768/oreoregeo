package com.example.oreoregeo

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.oreoregeo.ui.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                getCurrentLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                getCurrentLocation()
            }
            else -> {
                // Permission denied
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            OreoregeoTheme {
                MainScreen(
                    onRequestLocation = { callback ->
                        requestLocationAndSearch(callback)
                    }
                )
            }
        }
    }

    private fun requestLocationAndSearch(callback: (Double, Double) -> Unit) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation { lat, lon ->
                    callback(lat, lon)
                }
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getCurrentLocation(callback: ((Double, Double) -> Unit)? = null) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLocation = it
                    callback?.invoke(it.latitude, it.longitude)
                }
            }
        }
    }
}

@Composable
fun OreoregeoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onRequestLocation: ((Double, Double) -> Unit) -> Unit
) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }

    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as OreoregeoApplication
    val repository = app.repository

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oreoregeo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") },
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                        navController.navigate("search") {
                            popUpTo("search") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                        navController.navigate("history") {
                            popUpTo("search")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "search",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("search") {
                val searchViewModel: SearchViewModel = viewModel(
                    factory = SearchViewModelFactory(repository)
                )
                val checkinViewModel: CheckinViewModel = viewModel(
                    factory = CheckinViewModelFactory(repository)
                )
                
                val searchState by searchViewModel.searchState.collectAsState()
                val checkinState by checkinViewModel.checkinState.collectAsState()
                
                var showCheckinDialog by remember { mutableStateOf(false) }
                var selectedPlaceKey by remember { mutableStateOf("") }
                var selectedPlaceName by remember { mutableStateOf<String?>(null) }

                SearchScreen(
                    searchState = searchState,
                    onSearchClick = {
                        onRequestLocation { lat, lon ->
                            searchViewModel.searchNearby(lat, lon)
                        }
                    },
                    onPlaceClick = { placeKey ->
                        selectedPlaceKey = placeKey
                        selectedPlaceName = if (searchState is SearchState.Success) {
                            (searchState as SearchState.Success).places
                                .find { it.place.placeKey == placeKey }?.place?.name
                        } else null
                        showCheckinDialog = true
                        checkinViewModel.reset()
                    }
                )

                if (showCheckinDialog) {
                    CheckinDialog(
                        placeKey = selectedPlaceKey,
                        placeName = selectedPlaceName,
                        checkinState = checkinState,
                        onCheckin = { note ->
                            checkinViewModel.performCheckin(selectedPlaceKey, note)
                        },
                        onDismiss = {
                            showCheckinDialog = false
                            checkinViewModel.reset()
                        }
                    )
                }
            }
            
            composable("history") {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModelFactory(repository)
                )
                val checkins by historyViewModel.checkins.collectAsState()

                HistoryScreen(checkins = checkins)
            }
        }
    }
}
