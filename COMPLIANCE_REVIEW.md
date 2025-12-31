# 要件準拠レビュー

このリポジトリの最新コミットが、提示された「Codex 実装指示書」の必須要件をどこまで満たしているかを確認した結果をまとめます。

## 判定サマリ

- **未充足**: OSM OAuth 認証が UI から到達不能で、node 作成・タグ更新が動作しない。
- **未充足**: Google Drive への DB バックアップを呼び出す UI が存在せず、自動／手動どちらも実行されない。
- **未充足**: 既存 node のタグ編集がハードコードのダミーデータのままで、実データ取得・送信が行われない。
- **未充足**: OSM 更新時の version mismatch (HTTP 409) に対する再取得・再試行処理が実装されていない。
- **未充足**: オフライン時の操作禁止（検索・チェックイン・OSM 書き込み）が UI/ロジックで強制されていない。

上記により、仕様で求められる主要機能は未達です。

## 詳細根拠

- `SettingsScreen` では OSM ログインおよび Drive バックアップのボタンがすべて `TODO` コメントのみで実装されておらず、`OsmApiClient` が必要とするトークンをセットする導線がありません。そのため認証必須の OSM API は失敗します。【F:app/src/main/java/com/zelretch/oreoregeo/MainActivity.kt†L189-L211】【F:app/src/main/java/com/zelretch/oreoregeo/data/remote/OsmApiClient.kt†L23-L34】
- Drive バックアップは `DriveBackupManager` が定義されていますが、UI から呼ばれず実行経路がありません。【F:app/src/main/java/com/zelretch/oreoregeo/data/remote/DriveBackupManager.kt†L1-L90】
- タグ編集画面は既存タグを `"Example"` で固定しており、OSM からの取得や送信が行われません。【F:app/src/main/java/com/zelretch/oreoregeo/MainActivity.kt†L257-L269】
- `OsmApiClient.updateNode` は HTTP 409 をそのまま `Result.failure` で返すだけで、再取得して version を更新する再試行処理がありません。【F:app/src/main/java/com/zelretch/oreoregeo/data/remote/OsmApiClient.kt†L63-L81】
- ネットワーク接続の有無を確認せずに Overpass 検索・チェックイン・OSM 書き込みを試みるため、オフライン時に操作不可とする要件を満たしていません。`NetworkUtil` などのチェックは用意されていません。【F:app/src/main/java/com/zelretch/oreoregeo/ui/NetworkUtil.kt†L1-L29】【F:app/src/main/java/com/zelretch/oreoregeo/ui/SearchViewModel.kt†L26-L54】

## 結論

現状のコードは主要な必須要件を複数満たしておらず、全体として「要件未達」です。上記の未充足点を実装し、OSM 認証～編集、Drive バックアップ、オフライン制御を含む動作確認を行う必要があります。
