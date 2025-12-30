# Oreoregeo

A Swarm-compatible manual check-in Android app for OpenStreetMap places.

## Overview

Oreoregeo is an Android application that allows users to:
- Search for nearby places using OpenStreetMap and Overpass API
- Manually check-in to places
- View check-in history
- Add new places to OpenStreetMap
- Edit tags on existing OSM nodes
- Backup check-in data to Google Drive

## Technical Stack

- **Platform**: Android (minSdk 26, targetSdk 34)
- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Database**: Room (SQLite with WAL mode enabled)
- **Async**: Kotlin Coroutines
- **HTTP**: OkHttp
- **APIs**: 
  - Overpass API for place search
  - OSM API v0.6 for editing
  - Google Drive API for backups

## Features

### Implemented

1. **Nearby Search**
   - Uses Overpass API with 80m radius
   - Searches for amenity, shop, and tourism tags
   - Calculates distance from current location
   - Sorts results by distance

2. **Manual Check-in**
   - Complete manual process (no auto check-in)
   - 30-minute duplicate prevention via database constraint
   - UTC timestamp storage
   - Optional notes

3. **Check-in History**
   - Display all check-ins with place information
   - Sorted by date (newest first)

4. **Database**
   - `places` table with place_key (osm:type:id format)
   - `checkins` table with unique constraint on place+30min bucket
   - WAL mode enabled for better concurrency

5. **OSM Integration**
   - Add new nodes with tags
   - Update existing node tags
   - Changeset management
   - Version conflict handling

6. **Google Drive Backup**
   - Backup database and WAL files
   - Single generation backup (replaces previous)

### Data Schema

#### places table
```sql
CREATE TABLE places (
  place_key TEXT PRIMARY KEY,  -- Format: osm:{type}:{id}
  name TEXT,
  category TEXT,
  lat REAL,
  lon REAL,
  updated_at INTEGER  -- epoch milliseconds
);
```

#### checkins table
```sql
CREATE TABLE checkins (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  place_key TEXT,
  visited_at INTEGER,  -- epoch milliseconds, UTC
  note TEXT,
  visited_at_bucket INTEGER  -- visited_at / 1800000 (30min)
);

CREATE UNIQUE INDEX ux_checkins_place_bucket_30m 
  ON checkins(place_key, visited_at_bucket);
```

## Architecture

```
app/
├── data/
│   ├── local/          # Room entities and DAOs
│   ├── remote/         # API clients (Overpass, OSM)
│   └── DriveBackupManager.kt
├── domain/             # Business logic and repository
│   ├── Models.kt
│   └── Repository.kt
└── ui/                 # Compose UI and ViewModels
    ├── SearchScreen.kt
    ├── CheckinDialog.kt
    ├── HistoryScreen.kt
    ├── AddPlaceScreen.kt
    ├── EditTagsScreen.kt
    ├── SettingsScreen.kt
    └── *ViewModel.kt files
```

## Requirements

- Android 8.0 (API 26) or higher
- Location permissions for nearby search
- Internet access for API calls
- Google account for Drive backups
- OSM account for editing features

## Building

```bash
./gradlew assembleDebug
```

## Configuration

### OSM OAuth
To enable OSM editing features, you need to configure OAuth credentials:
1. Register an application at https://www.openstreetmap.org/oauth2/applications
2. Request `write_api` scope only
3. Implement OAuth flow in the app

### Google Drive API
To enable backup features:
1. Enable Drive API in Google Cloud Console
2. Add OAuth 2.0 credentials
3. Add google-services.json to app directory

## Constraints

- **Manual check-in only** - No automatic check-in features
- **Node editing only** - Cannot create/edit ways or relations
- **No social features** - No friends, sharing, or feed
- **No location tracking** - Location is only accessed on demand
- **Local-first** - All data stored locally, cloud sync only for backups

## Database Backup

- Backs up to Google Drive
- Includes both .db and .db-wal files
- Keeps only the latest version
- Manual trigger from settings

## API Usage

### Overpass Query Example
```
[out:json];
(
  node["amenity"](around:80,{lat},{lon});
  way["amenity"](around:80,{lat},{lon});
  relation["amenity"](around:80,{lat},{lon});
  node["shop"](around:80,{lat},{lon});
  way["shop"](around:80,{lat},{lon});
  relation["shop"](around:80,{lat},{lon});
  node["tourism"](around:80,{lat},{lon});
  way["tourism"](around:80,{lat},{lon});
  relation["tourism"](around:80,{lat},{lon});
);
out center tags;
```

### OSM Changeset Workflow
1. Create changeset with comment
2. Create/update node
3. Close changeset
4. Handle version conflicts by refetching

## Error Handling

- **Offline**: History viewing available, search/check-in disabled
- **Overpass failure**: Error message displayed, retry option
- **OSM delay**: User notified that changes may take time to reflect
- **Version conflict**: Automatic refetch and retry

## License

See LICENSE file for details.

## Contributing

This is an initial implementation. Contributions welcome for:
- OAuth implementation
- Drive API integration
- UI improvements
- Bug fixes
- Documentation