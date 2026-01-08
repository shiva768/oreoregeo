package com.zelretch.oreoregeo

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.zelretch.oreoregeo.ui.AddPlaceScreen
import com.zelretch.oreoregeo.ui.CheckinDialog
import com.zelretch.oreoregeo.ui.CheckinViewModel
import com.zelretch.oreoregeo.ui.CheckinViewModelFactory
import com.zelretch.oreoregeo.ui.EditTagsScreen
import com.zelretch.oreoregeo.ui.HistoryScreen
import com.zelretch.oreoregeo.ui.HistoryViewModel
import com.zelretch.oreoregeo.ui.HistoryViewModelFactory
import com.zelretch.oreoregeo.ui.OsmEditState
import com.zelretch.oreoregeo.ui.OsmEditViewModel
import com.zelretch.oreoregeo.ui.OsmEditViewModelFactory
import com.zelretch.oreoregeo.ui.SearchScreen
import com.zelretch.oreoregeo.ui.SearchState
import com.zelretch.oreoregeo.ui.SearchViewModel
import com.zelretch.oreoregeo.ui.SearchViewModelFactory
import com.zelretch.oreoregeo.ui.SettingsScreen
import kotlinx.coroutines.launch

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
            var currentLocationPair by remember { mutableStateOf<Pair<Double, Double>?>(null) }

            OreoregeoTheme {
                MainScreen(
                    currentLocation = currentLocationPair,
                    onRequestLocation = { callback ->
                        requestLocationAndSearch { lat, lon ->
                            currentLocationPair = lat to lon
                            callback(lat, lon)
                        }
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
                // 位置情報が許可されている場合、現在地を取得
                getCurrentLocation { lat, lon ->
                    callback(lat, lon)
                }
            }
            else -> {
                // 権限を要求
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
    currentLocation: Pair<Double, Double>?,
    onRequestLocation: ((Double, Double) -> Unit) -> Unit
) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }

    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as OreoregeoApplication
    val repository = app.repository

    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(repository)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oreoregeo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (repository.isOsmAuthenticated()) {
                        IconButton(onClick = { navController.navigate("add_place") }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_place))
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
                    label = { Text(stringResource(R.string.search)) },
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                        navController.navigate("search") {
                            popUpTo("search") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = stringResource(R.string.checkin_history)) },
                    label = { Text(stringResource(R.string.checkin_history)) },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                        navController.navigate("history") {
                            popUpTo("search")
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title)) },
                    label = { Text(stringResource(R.string.settings_title)) },
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                        navController.navigate("settings") {
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

                val searchRadius by searchViewModel.searchRadius.collectAsState()
                val excludeUnnamed by searchViewModel.excludeUnnamed.collectAsState()
                
                SearchScreen(
                    searchState = searchState,
                    searchRadius = searchRadius,
                    onRadiusChange = { searchViewModel.setSearchRadius(it) },
                    excludeUnnamed = excludeUnnamed,
                    onExcludeUnnamedChange = { searchViewModel.setExcludeUnnamed(it) },
                    canEdit = repository.isOsmAuthenticated(),
                    currentLocation = currentLocation,
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
                    },
                    onCheckinClick = { placeKey ->
                        selectedPlaceKey = placeKey
                        selectedPlaceName = if (searchState is SearchState.Success) {
                            (searchState as SearchState.Success).places
                                .find { it.place.placeKey == placeKey }?.place?.name
                        } else null
                        showCheckinDialog = true
                        checkinViewModel.reset()
                    },
                    onEditPlace = { placeKey ->
                        navController.navigate("edit_tags/${placeKey.replace("/", "%2F")}")
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
                val checkins by historyViewModel.checkins.collectAsState()

                HistoryScreen(
                    checkins = checkins,
                    onDeleteClick = { historyViewModel.deleteCheckin(it) }
                )
            }
            
            composable("settings") {
                val scope = rememberCoroutineScope()
                val context = androidx.compose.ui.platform.LocalContext.current
                
                val signInLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        scope.launch {
                            val backupResult = repository.backupToGoogleDrive(account)
                            val messageId = if (backupResult.isSuccess) R.string.backup_success else R.string.backup_failed
                            android.widget.Toast.makeText(context, context.getString(messageId), android.widget.Toast.LENGTH_LONG).show()
                        }
                    } catch (e: ApiException) {
                        e.printStackTrace()
                        android.widget.Toast.makeText(context, context.getString(R.string.backup_failed), android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                SettingsScreen(
                    onBackupClick = {
                        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
                        if (lastAccount != null) {
                            scope.launch {
                                val backupResult = repository.backupToGoogleDrive(lastAccount)
                                val messageId = if (backupResult.isSuccess) R.string.backup_success else R.string.backup_failed
                                android.widget.Toast.makeText(context, context.getString(messageId), android.widget.Toast.LENGTH_LONG).show()
                            }
                        } else {
                            signInLauncher.launch(repository.getGoogleSignInIntent())
                        }
                    },
                    onOsmLoginClick = {
                        // TODO: OSM OAuth の実装
                    }
                )
            }

            composable("add_place") {
                val osmEditViewModel: OsmEditViewModel = viewModel(
                    factory = OsmEditViewModelFactory(repository)
                )
                val editState by osmEditViewModel.editState.collectAsState()
                
                var currentLat by remember { mutableStateOf<Double?>(null) }
                var currentLon by remember { mutableStateOf<Double?>(null) }

                LaunchedEffect(Unit) {
                    // Try to get current location for convenience
                    onRequestLocation { lat, lon ->
                        currentLat = lat
                        currentLon = lon
                    }
                }

                LaunchedEffect(editState) {
                    if (editState is OsmEditState.Success) {
                        navController.popBackStack()
                        osmEditViewModel.reset()
                    }
                }

                AddPlaceScreen(
                    currentLat = currentLat,
                    currentLon = currentLon,
                    onSave = { lat, lon, tags ->
                        osmEditViewModel.createPlace(lat, lon, tags)
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "edit_tags/{placeKey}",
                arguments = listOf(navArgument("placeKey") { type = NavType.StringType })
            ) { backStackEntry ->
                val placeKey = backStackEntry.arguments?.getString("placeKey") ?: ""
                val osmEditViewModel: OsmEditViewModel = viewModel(
                    factory = OsmEditViewModelFactory(repository)
                )
                val editState by osmEditViewModel.editState.collectAsState()

                LaunchedEffect(editState) {
                    if (editState is OsmEditState.Success) {
                        navController.popBackStack()
                        osmEditViewModel.reset()
                    }
                }

                // TODO: Load existing tags from place
                val existingTags = mapOf("name" to "Example")

                EditTagsScreen(
                    placeKey = placeKey,
                    existingTags = existingTags,
                    onSave = { nodeId, tags ->
                        osmEditViewModel.updateNodeTags(nodeId, tags)
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
