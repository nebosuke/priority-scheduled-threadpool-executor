# 🌟 Priority Scheduled Thread Pool Executor 🌟

超絶アゲアゲな優先度つきScheduledThreadPoolExecutor！💖

## 📝 概要

普通のScheduledThreadPoolExecutorじゃ物足りないあなたに〜✨  
優先度に基づいてタスクの実行順序を制御できる、まじ便利なExecutorだよ〜🎯

### 🎀 特徴

- **🔥 優先度制御**: URGENT、HIGH、NORMAL、LOWの4段階で優先度を設定可能！
- **⏰ スケジュール時刻優先**: 基本はスケジュール時刻順、同じ時刻なら優先度順で実行！
- **💕 優しい設計**: 実行中のタスクは割り込まない安全設計！
- **✨ 完全互換**: 標準のScheduledExecutorServiceと完全互換！
- **🧪 テスト完備**: JUnit5でしっかりテスト済み！

## 🚀 使い方

### 基本的な使い方

```java
// Executorを作成〜💖
PriorityScheduledThreadPoolExecutor executor = 
    new PriorityScheduledThreadPoolExecutor(4);

// 優先度つきでタスクをスケジュール〜✨
executor.scheduleWithPriority(() -> {
    System.out.println("超緊急タスク実行中〜🔥");
}, 1, TimeUnit.SECONDS, Priority.URGENT);

executor.scheduleWithPriority(() -> {
    System.out.println("普通のタスク実行中〜📝");
}, 1, TimeUnit.SECONDS, Priority.NORMAL);

// 同じ実行時刻でも優先度順に実行されるよ〜💕
```

### Callableも使えるよ〜

```java
// 結果を返すタスクも優先度つきで〜✨
ScheduledFuture<String> future = executor.scheduleWithPriority(() -> {
    return "アゲアゲ結果〜💖";
}, 500, TimeUnit.MILLISECONDS, Priority.HIGH);

String result = future.get();
System.out.println(result); // "アゲアゲ結果〜💖"
```

### 定期実行も優先度つき〜

```java
// 固定レートで実行〜⏰
executor.scheduleAtFixedRateWithPriority(() -> {
    System.out.println("定期タスク実行中〜🔄");
}, 0, 1, TimeUnit.SECONDS, Priority.HIGH);

// 固定遅延で実行〜⏳
executor.scheduleWithFixedDelayWithPriority(() -> {
    System.out.println("遅延タスク実行中〜😴");
}, 0, 2, TimeUnit.SECONDS, Priority.LOW);
```

## 🎯 優先度について

### Priority enum

```java
public enum Priority {
    URGENT(1),  // 🔥 超緊急！今すぐやって！
    HIGH(2),    // ⚡ 高優先度！急いで〜
    NORMAL(3),  // 📝 普通の優先度だよ〜
    LOW(4)      // 😴 低優先度〜時間あるときでいいよ〜
}
```

### 実行順序のルール

1. **⏰ スケジュール時刻が最優先**: 実行予定時刻が早いものから順番に
2. **🎯 とにかく優先度順**: URGENT → HIGH → NORMAL → LOW の順、実行時刻以降のタスクは優先度が高い子から実行するよ
3. **📝 同じ優先度なら提出順**: 先に提出されたタスクから実行

## 🛠️ ビルド方法

```bash
# コンパイル〜💻
mvn compile

# テスト実行〜🧪
mvn test

# パッケージ作成〜📦
mvn package
```

## 🧪 テスト

JUnit5でしっかりテストしてるよ〜✨

```bash
mvn test
```

### テスト内容

- ✅ 基本的なスケジュール実行
- ✅ 優先度順実行の確認
- ✅ スケジュール時刻優先の確認
- ✅ Callableのスケジュール
- ✅ 固定レート・固定遅延スケジュール
- ✅ エラーハンドリング
- ✅ シャットダウン処理

## 📋 要件

- Java 11以上
- Maven 3.6以上

## 🎨 アーキテクチャ

### クラス構成

- **PriorityScheduledThreadPoolExecutor**: メインのExecutorクラス
- **Priority**: 優先度を表すenum
- **PriorityScheduledFuture**: 優先度つきのScheduledFuture
- **PriorityTask**: 内部で使用する優先度つきタスク

### 動作原理

1. **スケジューリング**: 内部のScheduledThreadPoolExecutorでタイミング管理
2. **優先度制御**: PriorityBlockingQueueで優先度順にタスクを管理
3. **実行**: 優先度順にタスクを取り出して実行

## 💖 まとめ

優先度つきのScheduledThreadPoolExecutorで、あなたのアプリもアゲアゲに〜✨  
重要なタスクを優先的に実行して、パフォーマンスもバッチリ〜💕

何か質問があったら気軽に聞いてね〜🎀
