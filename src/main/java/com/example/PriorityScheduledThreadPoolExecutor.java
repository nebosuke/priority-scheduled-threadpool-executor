package com.example;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 優先度付きスケジューリングができる超便利なThreadPoolExecutor！✨
 * 普通のThreadPoolExecutorじゃ物足りない時に使うやつ〜💕
 * 
 * タスクに優先度を付けて、重要なやつから先に実行できちゃう！
 * べ、別にあんたのために作ったわけじゃないんだからね！😤
 * でも...まぁ、めっちゃ賢いでしょ？😊💕
 */
public class PriorityScheduledThreadPoolExecutor extends ThreadPoolExecutor implements ScheduledExecutorService {

    // デフォルトのキープアライブ時間〜 10ミリ秒で十分でしょ💅 
    private static final long DEFAULT_KEEP_ALIVE_TIME_MILLIS = 10L;

    // スケジューリング担当の子！この子がタイミング管理してくれるの〜⏰
    private final ScheduledThreadPoolExecutor scheduler;

    // タスクの順番を決めるシーケンス番号！同じ優先度の時はこれで順番決めるよ〜📝
    // ちゃんと順番守りなさいよね！💢
    private final AtomicLong sequencer = new AtomicLong(0);

    // 優先度付きタスクを溜めておく魔法のキュー✨ 優先度高い子から出てくるの〜
    private final PriorityBlockingQueue<PriorityTask> priorityQueue;

    /**
     * コンストラクタ〜！💖
     * コアプールサイズを指定して、優先度付きExecutorを作っちゃうよ〜
     * 
     * @param corePoolSize コアスレッド数だよ〜 この数だけスレッドが常駐するの💪
     */
    public PriorityScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize, Integer.MAX_VALUE, DEFAULT_KEEP_ALIVE_TIME_MILLIS, TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<>());
        
        // スケジューラーを1スレッドで初期化〜 タイミング管理はこの子にお任せ！
        this.scheduler = new ScheduledThreadPoolExecutor(1);
        // 優先度キューも準備完了〜✨
        this.priorityQueue = new PriorityBlockingQueue<>();
    }
    
    /**
     * 優先度付きでタスクをスケジュールしちゃう超便利メソッド！✨
     * 指定した遅延時間の後に、優先度に従ってタスクを実行するよ〜💪
     * 
     * @param command 実行したいタスクだよ〜 nullはダメよ😤
     * @param delay 遅延時間！この時間待ってから実行するの⏰
     * @param unit 時間の単位〜 秒とかミリ秒とか指定してね💅
     * @param priority タスクの優先度！HIGH、NORMAL、LOWから選んでね〜🎯
     * @return ScheduledFutureが返ってくるよ〜 キャンセルとかに使えるの✨
     * @throws NullPointerException 引数がnullの時は怒っちゃうからね😱
     */
    public ScheduledFuture<?> scheduleWithPriority(Runnable command, long delay, TimeUnit unit, Priority priority) {
        if (command == null || unit == null || priority == null) {
            throw new NullPointerException("引数がnullだよ〜😱");
        }
        
        // 優先度付きタスクを作成〜 シーケンス番号も付けておくよ💖
        final var task = new PriorityTask(command, priority, sequencer.getAndIncrement());
        
        // 指定時間後にキューに追加して処理開始〜
        ScheduledFuture<?> scheduledFuture = scheduler.schedule(() -> {
            priorityQueue.offer(task); // キューにポイッ！
            submit(this::processNextTask); // 処理開始〜
        }, delay, unit);
        
        return new PriorityScheduledFuture<>(scheduledFuture, priority);
    }
    
    /**
     * Callable版の優先度付きスケジューリング〜✨
     * 戻り値が欲しい時はこっちを使ってね〜
     * 
     * @param <V> 戻り値の型だよ〜
     * @param callable 実行したいCallable〜 戻り値があるやつね✨
     * @param delay 遅延時間だよ〜⏰
     * @param unit 時間の単位💅
     * @param priority 優先度〜🎯
     * @return 結果が取得できるScheduledFuture💖
     * @throws NullPointerException 引数がnullはダメよ〜😤
     */
    public <V> ScheduledFuture<V> scheduleWithPriority(Callable<V> callable, long delay, TimeUnit unit, Priority priority) {
        if (callable == null || unit == null || priority == null) {
            throw new NullPointerException("引数がnullだよ〜😱");
        }
        
        // CallableをFutureTaskでラップ〜
        FutureTask<V> futureTask = new FutureTask<>(callable);
        PriorityTask task = new PriorityTask(futureTask, priority, sequencer.getAndIncrement());
        
        // スケジューリング開始〜✨
        ScheduledFuture<?> scheduledFuture = scheduler.schedule(() -> {
            priorityQueue.offer(task);
            submit(this::processNextTask);
        }, delay, unit);
        
        return new PriorityScheduledFuture<>(new ScheduledFutureAdapter<>(scheduledFuture, futureTask), priority);
    }
    
    /**
     * 次のタスクを処理する内部メソッド〜💪
     * キューから優先度順にタスクを取り出して実行しちゃうの！
     * 
     * この子が優先度制御の心臓部分だよっ💖
     */
    private void processNextTask() {
        PriorityTask task;
        // キューが空になるまでタスクを処理し続けるよ〜
        while ((task = priorityQueue.poll()) != null) {
            this.execute(task);
        }
    }
    
    // ScheduledExecutorServiceのインターフェース実装〜 普通の優先度で実行するよ💅
    
    /**
     * 普通のscheduleメソッド〜 優先度はNORMALで実行するの💖
     */
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduleWithPriority(command, delay, unit, Priority.NORMAL);
    }
    
    /**
     * Callable版の普通のschedule〜 これも優先度NORMALよ✨
     */
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return scheduleWithPriority(callable, delay, unit, Priority.NORMAL);
    }
    
    /**
     * 固定レートでの実行〜 優先度はNORMALでやっちゃう💪
     */
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduleAtFixedRateWithPriority(command, initialDelay, period, unit, Priority.NORMAL);
    }
    
    /**
     * 固定遅延での実行〜 これも優先度NORMALね😊
     */
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduleWithFixedDelayWithPriority(command, initialDelay, delay, unit, Priority.NORMAL);
    }
    
    /**
     * 優先度付きの固定レート実行〜！💕
     * 指定した間隔で繰り返し実行するけど、優先度も指定できちゃう✨
     * 
     * @param command 繰り返し実行するタスク
     * @param initialDelay 最初の遅延時間
     * @param period 実行間隔〜
     * @param unit 時間の単位
     * @param priority タスクの優先度💖
     * @return ScheduledFuture
     * @throws NullPointerException 引数がnullはダメよ〜😱
     */
    public ScheduledFuture<?> scheduleAtFixedRateWithPriority(Runnable command, long initialDelay, long period, TimeUnit unit, Priority priority) {
        if (command == null || unit == null || priority == null) {
            throw new NullPointerException("引数がnullだよ〜😱");
        }
        
        // 固定レートでタスクを繰り返しキューに追加〜
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            PriorityTask task = new PriorityTask(command, priority, sequencer.getAndIncrement());
            priorityQueue.offer(task);
            processNextTask();
        }, initialDelay, period, unit);
        
        return new PriorityScheduledFuture<>(scheduledFuture, priority);
    }
    
    /**
     * 優先度付きの固定遅延実行〜！💖
     * 前回の実行完了から指定時間後に次を実行するやつ〜
     * 
     * @param command 繰り返し実行するタスク
     * @param initialDelay 最初の遅延時間
     * @param delay 実行間隔の遅延時間
     * @param unit 時間の単位
     * @param priority タスクの優先度✨
     * @return ScheduledFuture
     * @throws NullPointerException 引数がnullはダメよ〜😤
     */
    public ScheduledFuture<?> scheduleWithFixedDelayWithPriority(Runnable command, long initialDelay, long delay, TimeUnit unit, Priority priority) {
        if (command == null || unit == null || priority == null) {
            throw new NullPointerException("引数がnullだよ〜😱");
        }
        
        // 固定遅延でタスクを繰り返しキューに追加〜
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
            PriorityTask task = new PriorityTask(command, priority, sequencer.getAndIncrement());
            priorityQueue.offer(task);
            processNextTask();
        }, initialDelay, delay, unit);
        
        return new PriorityScheduledFuture<>(scheduledFuture, priority);
    }
    
    /**
     * 優雅にシャットダウン〜💅
     * 実行中のタスクは完了させてから終了するの
     */
    @Override
    public void shutdown() {
        scheduler.shutdown(); // スケジューラーも止める
        super.shutdown(); // 親クラスも止める
    }
    
    /**
     * 強制シャットダウン〜😤
     * 実行中のタスクも容赦なく止めちゃう！
     * 
     * @return 実行待ちだったタスクのリスト
     */
    @Override
    public java.util.List<Runnable> shutdownNow() {
        scheduler.shutdownNow(); // スケジューラーを強制停止
        return super.shutdownNow(); // 親クラスも強制停止
    }
    
    /**
     * 優先度付きタスクを表現する内部クラス〜💖
     * 普通のRunnableに優先度とシーケンス番号を付けた超便利な子！
     * 
     * Comparableを実装してるから、優先度順にソートできちゃうの✨
     */
    private static class PriorityTask implements Runnable, Comparable<PriorityTask> {
        private final Runnable task; // 実際に実行するタスク〜
        private final Priority priority; // 優先度だよ〜💅
        private final long sequenceNumber; // 同じ優先度の時の順番決め用📝
        
        /**
         * PriorityTaskのコンストラクタ〜！
         * タスクと優先度とシーケンス番号をセットするの💕
         */
        public PriorityTask(Runnable task, Priority priority, long sequenceNumber) {
            this.task = task;
            this.priority = priority;
            this.sequenceNumber = sequenceNumber;
        }
        
        /**
         * タスクを実行〜！💪
         * 中身のRunnableを呼び出すだけのシンプルな子
         */
        @Override
        public void run() {
            task.run();
        }
        
        /**
         * 他のPriorityTaskと比較する超重要メソッド！✨
         * まず優先度で比較して、同じなら登録順で比較するの〜
         * 
         * @param other 比較対象のPriorityTask
         * @return 比較結果（負の数、0、正の数）
         */
        @Override
        public int compareTo(PriorityTask other) {
            // まずは優先度で比較〜 数値が小さいほど優先度高いよ💖
            int priorityComparison = Integer.compare(this.priority.getValue(), other.priority.getValue());
            if (priorityComparison != 0) {
                return priorityComparison;
            }

            // 優先度が同じなら、シーケンス番号で比較（FIFO）
            return Long.compare(this.sequenceNumber, other.sequenceNumber);
        }
        
        /**
         * 優先度を取得〜💅
         */
        public Priority getPriority() {
            return priority;
        }
        
        /**
         * デバッグ用の文字列表現〜 優先度とシーケンス番号を表示するよ😊
         */
        @Override
        public String toString() {
            return String.format("PriorityTask{priority=%s, seq=%d}", priority, sequenceNumber);
        }
    }
    
    /**
     * ScheduledFutureとFutureTaskを組み合わせるアダプタークラス〜✨
     * Callable版のscheduleWithPriorityで使う超便利な子！
     * 
     * ScheduledFutureの機能とFutureTaskの結果取得機能を両方使えちゃうの💕
     */
    private static class ScheduledFutureAdapter<V> implements ScheduledFuture<V> {
        private final ScheduledFuture<?> scheduledFuture; // スケジューリング情報
        private final FutureTask<V> futureTask; // 実際の結果を持つ子
        
        /**
         * アダプターのコンストラクタ〜💖
         */
        public ScheduledFutureAdapter(ScheduledFuture<?> scheduledFuture, FutureTask<V> futureTask) {
            this.scheduledFuture = scheduledFuture;
            this.futureTask = futureTask;
        }
        
        /**
         * 遅延時間を取得〜⏰
         */
        @Override
        public long getDelay(TimeUnit unit) {
            return scheduledFuture.getDelay(unit);
        }
        
        /**
         * 他のDelayedオブジェクトと比較〜💅
         */
        @Override
        public int compareTo(Delayed o) {
            return scheduledFuture.compareTo(o);
        }
        
        /**
         * タスクをキャンセル〜😤
         * 両方ともキャンセルしないとダメなの
         */
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean cancelled = scheduledFuture.cancel(mayInterruptIfRunning);
            futureTask.cancel(mayInterruptIfRunning);
            return cancelled;
        }
        
        /**
         * キャンセルされてるかチェック〜
         * どちらかがキャンセルされてたらtrueよ💔
         */
        @Override
        public boolean isCancelled() {
            return scheduledFuture.isCancelled() || futureTask.isCancelled();
        }
        
        /**
         * 完了してるかチェック〜✨
         * 両方とも完了してたらtrueよ💖
         */
        @Override
        public boolean isDone() {
            return scheduledFuture.isDone() && futureTask.isDone();
        }
        
        /**
         * 結果を取得〜！💕
         * FutureTaskから結果をもらってくるの
         */
        @Override
        public V get() throws InterruptedException, ExecutionException {
            return futureTask.get();
        }
        
        /**
         * タイムアウト付きで結果を取得〜⏰
         */
        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return futureTask.get(timeout, unit);
        }
    }
}
