# コードスタイル / 静的解析「理想形」ガイド

このドキュメントは、リファクタリング時に参照するための「理想形（北極星）」を定義します。現状の設定を尊重しつつ、段階的に理想へ近づける運用を前提にしています。

- 対象: Kotlin（Android/Compose）
- ツール: ktlint（整形・フォーマット）, detekt（設計・可読性・複雑度）
- 設定ファイル: 
  - .editorconfig（ktlint ルールの多くをここで管理）
  - config/detekt/detekt.yml

---

## TL;DR（PR 時チェックリスト）
- [ ] フォーマットを適用: `./gradlew ktlintFormat`
- [ ] 解析が通る: `./gradlew ktlintCheck detekt`
- [ ] 本番コードでのワイルドカード import は使わない（テストは可）
- [ ] 長過ぎる関数/クラスは分割を検討（下記の目安を参照）
- [ ] マジックナンバーは定数化（例外のある箇所は下記参照）
- [ ] UI で複雑化しがちな処理は ViewModel/ドメイン側に寄せる

---

## 現状（2026-01 時点）

- ktlint（.editorconfig）
  - ktlint_code_style = android_studio
  - 本番コード: no-wildcard-imports 有効
  - テスト: `[*Test.kt]` セクションで no-wildcard-imports 無効化（Assert.* などを許容）
  - max-line-length ルールは無効化（エディタの表示目安 max_line_length=120）
  - Composable の命名に配慮: `ktlint_function_naming_ignore_when_annotated_with=Composable`
- detekt（config/detekt/detekt.yml）
  - buildUponDefaultConfig=true, parallel=true, autoCorrect=false
  - レポート: xml/html を常時、CI では sarif も出力
  - 複雑度系（主な抜粋）
    - CognitiveComplexMethod: active, threshold=25, `**/ui/**` と `**/MainActivity.kt` を除外
    - CyclomaticComplexMethod: active, threshold=20
    - LongMethod: active, threshold=60
    - LargeClass: active, threshold=400
  - MagicNumber: inactive（主に UI/テーマやテストに配慮）

---

## 理想形（最終的に目指す状態）

- 共通原則
  - フォーマットは ktlint に一本化（detekt-formatting は導入しない）
  - 設計・可読性・複雑度は detekt に委譲
  - 生成物やリソースは対象外（誤検知を減らす）

- ktlint（最終目標）
  - no-wildcard-imports: 本番=有効、テスト=無効（現状通り）
  - max-line-length: 本番=140 で有効（最終的には 120 を目指す）、テスト=任意
  - import の順序/整形は Android Studio デフォルトに準拠
  - ファイル末尾の改行、インデントは現状維持

- detekt（最終目標のしきい値の目安）
  - CognitiveComplexMethod: threshold=15（UI も対象にできるのが理想。段階的に UI の除外を縮小）
  - CyclomaticComplexMethod: threshold=15
  - LongMethod: threshold=50（UI では 80 を上限目安として分割を推奨）
  - LargeClass: threshold=300（最終的に 250 程度までの引き下げも検討）
  - MagicNumber: active。本番コードでは有効化し、次の除外を維持:
    - テスト, `**/*.kts`, `**/Theme.kt`（UI テーマ系）

- 除外ポリシー
  - 一時的な除外はできるだけファイル/シンボル単位に限定し、モジュール/ディレクトリ単位の広い除外は段階的に縮小する

---

## 段階的移行プラン

- Phase 0（現状）
  - ktlint: 本番 no-wildcard、max-line-length 無効
  - detekt: CognitiveComplexMethod=25（UI 除外）、LargeClass=400 ほか

- Phase 1（軽微な厳格化）
  - ktlint: 現状維持（導入済みの no-wildcard を定着）
  - detekt: CognitiveComplexMethod 25 → 20、LargeClass 400 → 350

- Phase 2（実害少なめの強化）
  - ktlint: max-line-length=140 を有効化（長行の分割 PR を並行実施）
  - detekt: CyclomaticComplexMethod 20 → 18、LongMethod 60 → 55

- Phase 3（理想形へ）
  - detekt: CognitiveComplexMethod 20 → 15、UI 除外の縮小（`**/ui/**` → 主要スクリーン単位、最終的に撤廃）
  - LongMethod 55 → 50、LargeClass 350 → 300
  - ktlint: 余力があれば max-line-length=120 へ引き下げ

各フェーズは既存違反の件数と開発負荷を見ながら進めます。必要に応じて detekt の baseline を導入し、違反返済を段階的に行います。

---

## 例外・裁量の指針

- Compose UI
  - UI は宣言的で行数が増えやすいが、複雑な分岐/ロジックは ViewModel/ドメインへ移す
  - 長大な Composable はセクションごとに分割し、プレビュー単位で検証できる粒度を推奨

- マジックナンバー
  - テーマ/レイアウトの寸法（dp/sp）などはデザインシステムの定数に寄せる
  - アルゴリズム/ビジネスロジックでのハードコード値は定数化し意味を付与

- テスト
  - 可読性優先で import や長行の許容範囲は広めに。失敗時のメッセージは読みやすさを重視

---

## 運用（CI/ローカル）

- 整形（自動修正）: `./gradlew ktlintFormat`
- 解析: `./gradlew ktlintCheck detekt`
- detekt レポート: `app/build/reports/detekt/` に xml/html、CI では sarif も出力

---

## 変更方法（ルール更新時）

- ktlint: .editorconfig を更新（PR では「現状」「変更理由」「影響範囲」を明記）
- detekt: config/detekt/detekt.yml を更新（しきい値と除外の根拠を PR に記載）
- 影響が大きい場合は baseline を用いて段階的に返済

---

## リファクタリング時の実践ガイド（Boy Scout ルール）

触った範囲だけでよいので、次のいずれかを 1 つ以上行うことを推奨します。

- 関数を 1 つ分割して 50 行未満に近づける
- 分岐の入れ子を 1 段階浅くする（早期 return / when の活用）
- マジックナンバーを 1 件定数化する
- UI の複雑な処理を ViewModel/ドメインへ 1 件抽出する
- 輸入（import）を明示化して差分安定性を上げる

---

このドキュメントは運用しながらアップデートしていきます。疑問や変更提案は PR か Issue でお願いします。
