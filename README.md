# Oreoregeo

OpenStreetMap のスポットに手動でチェックインできる、Swarm 互換の Android アプリです。

## 概要

Oreoregeo は、以下の機能を持つ Android アプリケーションです：
- OpenStreetMap と Overpass API を使用した周辺スポットの検索
- スポットへの手動チェックイン
- チェックイン履歴の閲覧
- OpenStreetMap への新しいスポットの追加
- 既存の OSM ノードのタグ編集
- Google ドライブへのチェックインデータのバックアップ

## 技術スタック

- **プラットフォーム**: Android (minSdk 26, targetSdk 34)
- **言語**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **データベース**: Room (SQLite, WAL モード有効)
- **非同期処理**: Kotlin Coroutines
- **HTTP 通信**: OkHttp
- **使用 API**: 
  - スポット検索用 Overpass API
  - 編集用 OSM API v0.6
  - バックアップ用 Google Drive API

## 機能

### 実装済み

1. **周辺検索**
   - 半径 80m 以内を Overpass API で検索
   - amenity, shop, tourism タグを対象に検索
   - 現在地からの距離を計算
   - 距離順でソート

2. **手動チェックイン**
   - 完全に手動でのプロセス（自動チェックインなし）
   - データベース制約による 30 分以内の重複チェックイン防止
   - UTC タイムスタンプでの保存
   - 任意のメモ入力

3. **チェックイン履歴**
   - スポット情報を含む全チェックインの表示
   - 日付順（新しい順）でのソート

4. **データベース**
   - `places` テーブル：place_key（osm:type:id 形式）を使用
   - `checkins` テーブル：場所と 30 分単位のバケットによるユニーク制約
   - 並行性向上のための WAL モード有効化

5. **OSM 連携**
   - タグを指定して新しいノードを追加
   - 既存ノードのタグを更新
   - チェンジセット（Changeset）管理
   - バージョン競合のハンドリング

6. **Google ドライブ バックアップ**
   - データベースファイル（.db）および WAL ファイルのバックアップ
   - 単一世代のバックアップ（前回分を上書き）

### データスキーマ

#### places テーブル
```sql
CREATE TABLE places (
  place_key TEXT PRIMARY KEY,  -- 形式: osm:{type}:{id}
  name TEXT,
  category TEXT,
  lat REAL,
  lon REAL,
  updated_at INTEGER  -- エポックミリ秒
);
```

#### checkins テーブル
```sql
CREATE TABLE checkins (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  place_key TEXT,
  visited_at INTEGER,  -- エポックミリ秒 (UTC)
  note TEXT,
  visited_at_bucket INTEGER  -- visited_at / 1800000 (30分単位)
);

CREATE UNIQUE INDEX ux_checkins_place_bucket_30m 
  ON checkins(place_key, visited_at_bucket);
```

## アーキテクチャ

```
app/
├── data/
│   ├── local/          # Room エンティティと DAO
│   ├── remote/         # API クライアント (Overpass, OSM)
│   └── DriveBackupManager.kt
├── domain/             # ビジネスロジックとリポジトリ
│   ├── Models.kt
│   └── Repository.kt
└── ui/                 # Compose UI と ViewModel
    ├── SearchScreen.kt
    ├── CheckinDialog.kt
    ├── HistoryScreen.kt
    ├── AddPlaceScreen.kt
    ├── EditTagsScreen.kt
    ├── SettingsScreen.kt
    └── *ViewModel.kt ファイル
```

## 必要要件

- Android 8.0 (API 26) 以上
- 周辺検索用の位置情報権限
- API コール用のインターネットアクセス
- バックアップ用の Google アカウント
- 編集機能用の OSM アカウント

## ビルド方法

```bash
./gradlew assembleDebug
```

## 設定

### OSM OAuth
OSM の編集機能を有効にするには、OAuth クレデンシャルの設定が必要です：
1. https://www.openstreetmap.org/oauth2/applications でアプリケーションを登録
2. `write_api` スコープのみをリクエスト
3. アプリに OAuth フローを実装

### Google Drive API
バックアップ機能を有効にするには：
1. Google Cloud Console で Drive API を有効にする
2. OAuth 2.0 クレデンシャルを追加
3. `google-services.json` を Firebase コンソールから取得し、`app/` ディレクトリに配置する

#### google-services.json の管理について
このリポジトリには、ビルドを通すための**ダミー**の `google-services.json` が含まれています。本物のファイルは `.gitignore` により Git 管理から除外されています。

**CI (GitHub Actions) での利用:**
1. 本物の `google-services.json` を Base64 エンコードします：
   `base64 -i app/google-services.json`
2. GitHub リポジトリの Secrets に `GOOGLE_SERVICES_JSON` という名前で値を保存します。

**ローカル開発:**
Firebase コンソールからダウンロードしたファイルを `app/google-services.json` に上書きしてください。

## 制約事項

- **手動チェックインのみ** - 自動チェックイン機能はありません
- **ノード（Node）編集のみ** - ウェイ（Way）やリレーション（Relation）の作成・編集はできません
- **ソーシャル機能なし** - フレンド、共有、フィード機能はありません
- **位置追跡なし** - 位置情報はオンデマンドでのみアクセスされます
- **ローカルファースト** - すべてのデータはローカルに保存され、クラウド同期はバックアップのみです

## データベースのバックアップ

- Google ドライブにバックアップ
- .db と .db-wal ファイルの両方を含みます
- 最新バージョンのみを保持
- 設定画面から手動で実行

## API の使用

### Overpass クエリ例
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

### OSM チェンジセットのワークフロー
1. コメント付きでチェンジセットを作成
2. ノードを作成または更新
3. チェンジセットを閉じる
4. 再取得によるバージョン競合の処理

## エラーハンドリング

- **オフライン**: 履歴の閲覧は可能、検索とチェックインは無効
- **Overpass 失敗**: エラーメッセージを表示し、再試行オプションを提供
- **OSM 反映遅延**: 変更が反映されるまで時間がかかる場合があることをユーザーに通知
- **バージョン競合**: 自動的に再取得して再試行

## ライセンス

詳細は LICENSE ファイルを参照してください。

## 貢献について

これは初期の実装です。以下の貢献を歓迎します：
- OAuth の実装
- Drive API の統合
- UI の改善
- バグ修正
- ドキュメントの整備