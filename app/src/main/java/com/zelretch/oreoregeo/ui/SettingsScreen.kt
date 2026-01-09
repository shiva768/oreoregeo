package com.zelretch.oreoregeo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zelretch.oreoregeo.R

@Composable
fun SettingsScreen(onBackupClick: () -> Unit, onOsmLoginClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.osm_section_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.osm_section_desc),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onOsmLoginClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.connect_osm_account))
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.backup_restore_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.backup_drive_desc),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onBackupClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.backup_to_drive))
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.about_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.app_version_label, stringResource(R.string.app_name), "1.0"),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.app_desc),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
