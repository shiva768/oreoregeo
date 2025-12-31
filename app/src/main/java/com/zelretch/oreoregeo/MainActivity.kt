package com.zelretch.oreoregeo

import android.Manifest
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import com.zelretch.oreoregeo.domain.Place
import com.zelretch.oreoregeo.domain.SearchResult
import com.zelretch.oreoregeo.ui.BackupState
import com.zelretch.oreoregeo.ui.CheckinViewModel
import com.zelretch.oreoregeo.ui.HistoryViewModel
import com.zelretch.oreoregeo.ui.NodeLoadState
import com.zelretch.oreoregeo.ui.OsmEditState
import com.zelretch.oreoregeo.ui.OsmEditViewModel
import com.zelretch.oreoregeo.ui.SearchState
import com.zelretch.oreoregeo.ui.SearchViewModel
import com.zelretch.oreoregeo.ui.SettingsViewModel
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as OreoregeoApplication).repository
        val checkinViewModel by viewModels<CheckinViewModel> {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return CheckinViewModel(repository, applicationContext) as T
                }
            }
        }
        val historyViewModel by viewModels<HistoryViewModel> {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return HistoryViewModel(repository) as T
                }
            }
        }
        val searchViewModel by viewModels<SearchViewModel> {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return SearchViewModel(repository, applicationContext) as T
                }
            }
        }
        val settingsViewModel by viewModels<SettingsViewModel> {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return SettingsViewModel(applicationContext, repository) as T
                }
            }
        }

        val osmEditViewModel by viewModels<OsmEditViewModel> {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return OsmEditViewModel(repository, applicationContext) as T
                }
            }
        }

        val locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
                if (granted.values.all { it }) {
                    fetchAndSearch(searchViewModel)
                }
            }

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                Scaffold { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = "search",
                        modifier = Modifier.padding(padding)
                    ) {
                        composable("search") {
                            SearchScreen(
                                state = searchViewModel.state,
                                onSearch = { fetchAndSearch(searchViewModel) },
                                onCheckIn = { place ->
                                    val now = System.currentTimeMillis()
                                    val utcTime = now + TimeZone.getDefault().rawOffset * -1
                                    checkinViewModel.checkIn(place, null, utcTime)
                                },
                                onHistory = { navController.navigate("history") },
                                onSettings = { navController.navigate("settings") },
                                onEditTags = { place -> navController.navigate("edit/${'$'}{place.placeKey}") }
                            )
                        }
                        composable("history") {
                            HistoryScreen(historyViewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onAddPlace = { navController.navigate("add") }
                            )
                        }
                        composable("add") {
                            AddPlaceScreen(osmEditViewModel) { navController.popBackStack() }
                        }
                        composable("edit/{placeKey}") { entry ->
                            val key = entry.arguments?.getString("placeKey") ?: ""
                            EditTagsScreen(key, osmEditViewModel) { navController.popBackStack() }
                        }
                    }
                }
            }
        }

        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun fetchAndSearch(searchViewModel: SearchViewModel) {
        val fused = LocationServices.getFusedLocationProviderClient(this)
        fused.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                searchViewModel.search(location.latitude, location.longitude)
            }
        }
    }
}

@Composable
fun SearchScreen(
    state: androidx.compose.runtime.State<SearchState>,
    onSearch: () -> Unit,
    onCheckIn: (Place) -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onEditTags: (Place) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSearch) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("周辺検索")
            }
            Button(onClick = onHistory) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("履歴")
            }
            Button(onClick = onSettings) {
                Icon(Icons.Default.Backup, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("設定")
            }
        }
        when (val s = state.value) {
            SearchState.Idle -> Text("周辺の店舗を検索できます")
            SearchState.Loading -> Text("検索中…")
            is SearchState.Error -> Text("エラー: ${'$'}{s.message}")
            is SearchState.Loaded -> SearchResultList(s.results, onCheckIn, onEditTags)
        }
    }
}

@Composable
fun SearchResultList(
    results: List<SearchResult>,
    onCheckIn: (Place) -> Unit,
    onEditTags: (Place) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(results) { item ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(item.place.name, style = MaterialTheme.typography.titleMedium)
                    Text("${'$'}{item.distanceMeters.toInt()} m")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onCheckIn(item.place) }) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("チェックイン")
                        }
                        val isNode = item.place.placeKey.startsWith("osm:node:")
                        TextButton(
                            enabled = isNode,
                            onClick = { onEditTags(item.place) }
                        ) {
                            Icon(Icons.Default.AddLocation, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(if (isNode) "タグ編集" else "nodeのみ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onClose: () -> Unit) {
    val history by viewModel.history.collectAsState(initial = emptyList())
    Column(Modifier.padding(16.dp)) {
        Text("チェックイン履歴", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(history) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.placeKey)
                        Text(item.visitedAt.toString())
                        item.note?.let { Text(it) }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onClose) { Text("戻る") }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onAddPlace: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val token by viewModel.osmToken.collectAsState()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
            .result
        if (account != null) {
            viewModel.backup(account)
        }
    }
    LaunchedEffect(Unit) { viewModel.refreshToken() }
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                launcher.launch(viewModel.signInClient().signInIntent)
            } else {
                viewModel.backup(account)
            }
        }) {
            Icon(Icons.Default.Backup, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Google Drive にバックアップ")
        }
        when (state) {
            BackupState.Idle -> Text("手動バックアップを実行します")
            BackupState.Loading -> Text("バックアップ中…")
            BackupState.Success -> Text("完了しました")
            is BackupState.Error -> Text("エラー: ${(state as BackupState.Error).message}")
        }

        Spacer(Modifier.height(12.dp))
        Text("OSM OAuth", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.startOsmAuthorizationUrl()))
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("OSM にログイン")
            }
            TextButton(onClick = { viewModel.logoutOsm() }) { Text("ログアウト") }
        }
        Text(if (token != null) "トークン取得済み" else "未ログイン")

        Spacer(Modifier.height(12.dp))
        Button(onClick = onAddPlace) {
            Icon(Icons.Default.AddLocation, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("OSM ノードを追加")
        }
    }
}

@Composable
fun AddPlaceScreen(viewModel: OsmEditViewModel, onClose: () -> Unit) {
    var lat by remember { androidx.compose.runtime.mutableStateOf("") }
    var lon by remember { androidx.compose.runtime.mutableStateOf("") }
    var name by remember { androidx.compose.runtime.mutableStateOf("") }
    var category by remember { androidx.compose.runtime.mutableStateOf("") }
    val editState by viewModel.editState.collectAsState()
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("OSM ノード追加", style = MaterialTheme.typography.titleLarge)
        TextField(value = lat, onValueChange = { lat = it }, label = { Text("緯度") })
        TextField(value = lon, onValueChange = { lon = it }, label = { Text("経度") })
        TextField(value = name, onValueChange = { name = it }, label = { Text("name タグ") })
        TextField(value = category, onValueChange = { category = it }, label = { Text("amenity/shop/tourism") })
        Button(onClick = {
            val latVal = lat.toDoubleOrNull()
            val lonVal = lon.toDoubleOrNull()
            if (latVal != null && lonVal != null) {
                val tags = buildMap {
                    if (name.isNotBlank()) put("name", name)
                    if (category.isNotBlank()) put("amenity", category)
                }
                viewModel.createPlace(latVal, lonVal, tags)
            }
        }) {
            Text("追加する")
        }
        when (val s = editState) {
            OsmEditState.Idle -> Text("必要なタグを入力してください")
            OsmEditState.Loading -> Text("送信中…")
            is OsmEditState.Error -> Text("エラー: ${'$'}{s.message}")
            is OsmEditState.Success -> Text("追加要求を送信しました")
        }
        Button(onClick = onClose) { Text("閉じる") }
    }
}

@Composable
fun EditTagsScreen(placeKey: String, viewModel: OsmEditViewModel, onClose: () -> Unit) {
    val nodeState by viewModel.nodeState.collectAsState()
    val editState by viewModel.editState.collectAsState()
    var name by remember(nodeState) {
        val loaded = (nodeState as? NodeLoadState.Loaded)?.detail
        mutableStateOf(loaded?.tags?.get("name") ?: "")
    }
    var category by remember(nodeState) {
        val loaded = (nodeState as? NodeLoadState.Loaded)?.detail
        mutableStateOf(loaded?.tags?.get("amenity") ?: loaded?.tags?.get("shop") ?: loaded?.tags?.get("tourism") ?: "")
    }
    LaunchedEffect(placeKey) {
        viewModel.loadNode(placeKey)
    }
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("OSM タグ編集", style = MaterialTheme.typography.titleLarge)
        when (val state = nodeState) {
            NodeLoadState.Idle -> Text("読み込み待ち")
            NodeLoadState.Loading -> Text("取得中…")
            is NodeLoadState.Error -> Text("エラー: ${'$'}{state.message}")
            is NodeLoadState.Loaded -> {
                TextField(value = name, onValueChange = { name = it }, label = { Text("name") })
                TextField(value = category, onValueChange = { category = it }, label = { Text("amenity/shop/tourism") })
                Button(onClick = {
                    val tags = buildMap {
                        if (name.isNotBlank()) put("name", name)
                        if (category.isNotBlank()) put("amenity", category)
                    }
                    viewModel.updateNodeTags(placeKey, tags)
                }) {
                    Text("タグを更新")
                }
            }
        }
        when (val s = editState) {
            OsmEditState.Idle -> {}
            OsmEditState.Loading -> Text("更新中…")
            is OsmEditState.Error -> Text("エラー: ${'$'}{s.message}")
            is OsmEditState.Success -> Text("更新しました")
        }
        Button(onClick = onClose) { Text("閉じる") }
    }
}
