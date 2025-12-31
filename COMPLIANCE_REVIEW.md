# 要件準拠レビュー

最新コミットが「Codex 実装指示書」の必須要件をどこまで満たしているかを整理します。

## 判定サマリ
- **ほぼ充足**: OSM OAuth 導線、node 追加・タグ更新（409 時の再取得リトライ含む）、Overpass 周辺検索、30 分重複防止付きチェックイン保存、オフライン時の操作ブロック、Google Drive バックアップ起動など、要件で求められた主要機能は実装済みです。
- **前提設定あり**: 実機で OAuth/Drive バックアップを動かすには、`build.gradle.kts` の OSM クライアント ID/シークレットや Google のクレデンシャル設定を適切に投入する必要があります。

## 充足根拠
- 設定画面から OSM ログイン URL を開き、`oreoregeo://oauth/callback` で認可コードを受け取り `OsmOAuthManager` がアクセストークンを保存・リポジトリへ反映する導線を用意しています。【F:app/src/main/java/com/zelretch/oreoregeo/MainActivity.kt†L129-L170】【F:app/src/main/java/com/zelretch/oreoregeo/OAuthCallbackActivity.kt†L5-L20】【F:app/src/main/java/com/zelretch/oreoregeo/data/remote/OsmOAuthManager.kt†L13-L56】
- node 追加・タグ更新は changeset を開閉しつつ、作成時は OSM が返す node ID を取得して place_key を構成、更新時は version conflict を検知して再取得後にリトライします。【F:app/src/main/java/com/zelretch/oreoregeo/data/remote/OsmApiClient.kt†L9-L90】【F:app/src/main/java/com/zelretch/oreoregeo/domain/OreoregeoRepository.kt†L63-L93】【F:app/src/main/java/com/zelretch/oreoregeo/ui/OsmEditViewModel.kt†L34-L83】
- Overpass で amenity/shop/tourism の node/way/relation を半径 80m で検索し、Location.distanceBetween で距離を計算してソートしています。【F:app/src/main/java/com/zelretch/oreoregeo/data/remote/OverpassClient.kt†L12-L57】【F:app/src/main/java/com/zelretch/oreoregeo/domain/OreoregeoRepository.kt†L21-L54】
- チェックインは visited_at/1800000 に基づくユニークインデックスで 30 分以内の重複を禁止し、Room/Compose 経由で履歴表示できます。【F:app/src/main/java/com/zelretch/oreoregeo/data/local/Entities.kt†L9-L30】【F:app/src/main/java/com/zelretch/oreoregeo/domain/OreoregeoRepository.kt†L56-L81】【F:app/src/main/java/com/zelretch/oreoregeo/ui/MainActivity.kt†L200-L257】
- 検索・チェックイン・OSM 書き込み・node 取得は `NetworkUtil` でオンライン状態を確認し、オフライン時はエラーメッセージを表示して実行を抑止します。【F:app/src/main/java/com/zelretch/oreoregeo/ui/NetworkUtil.kt†L1-L13】【F:app/src/main/java/com/zelretch/oreoregeo/ui/SearchViewModel.kt†L17-L45】【F:app/src/main/java/com/zelretch/oreoregeo/ui/CheckinViewModel.kt†L17-L45】【F:app/src/main/java/com/zelretch/oreoregeo/ui/OsmEditViewModel.kt†L40-L80】
- Google Drive バックアップは `appDataFolder` へ DB/WAL を最新版一世代でアップロードする処理を用意し、設定画面からサインイン/バックアップを実行できます。【F:app/src/main/java/com/zelretch/oreoregeo/data/remote/DriveBackupManager.kt†L6-L50】【F:app/src/main/java/com/zelretch/oreoregeo/ui/SettingsViewModel.kt†L15-L53】【F:app/src/main/java/com/zelretch/oreoregeo/MainActivity.kt†L171-L199】

## 注意点
- `build.gradle.kts` の `OSM_CLIENT_ID`/`OSM_CLIENT_SECRET` はダミー値なので、OSM の OAuth アプリ登録情報で差し替えてください。【F:app/build.gradle.kts†L13-L40】
- Google Drive 連携には `google-services.json` の投入と SHA-1 証明書登録など環境側のセットアップが前提です。
