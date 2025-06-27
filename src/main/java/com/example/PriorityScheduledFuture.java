package com.example;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 🎀 優先度つきのScheduledFuture！超便利〜🎀
 * 
 * 普通のScheduledFutureに優先度をプラスしたの💖
 * これでタスクの優先度管理もバッチリ〜✨
 * 
 * @param <V> 結果の型
 */
public class PriorityScheduledFuture<V> implements ScheduledFuture<V> {
    
    private final ScheduledFuture<V> delegate;
    private final Priority priority;
    private final long submissionTime;
    
    /**
     * 優先度つきScheduledFutureのコンストラクタ〜💕
     * 
     * @param delegate 元のScheduledFuture
     * @param priority タスクの優先度
     */
    public PriorityScheduledFuture(ScheduledFuture<V> delegate, Priority priority) {
        this.delegate = delegate;
        this.priority = priority;
        this.submissionTime = System.nanoTime();
    }
    
    /**
     * 優先度を取得するよ〜✨
     * 
     * @return タスクの優先度
     */
    public Priority getPriority() {
        return priority;
    }
    
    /**
     * 提出時刻を取得〜⏰
     * 
     * @return 提出時刻（ナノ秒）
     */
    public long getSubmissionTime() {
        return submissionTime;
    }
    
    /**
     * 🌟 超重要！比較メソッド！🌟
     * 
     * 1. 優先度で比較〜
     * 2. 優先度が同じなら実行時刻で比較⏰
     * 3. 実行時刻も同じなら提出順で比較📝
     * 
     * @param other 比較対象
     * @return 比較結果
     */
    public int compareTo(PriorityScheduledFuture<V> other) {
        // まずは優先度で比較〜✨
        if (this.priority != other.priority) {
            int c = Integer.compare(this.priority.getValue(), other.priority.getValue());
            if (c != 0) {
                return c;
            }
        }

        // 実行時刻で比較〜⏰
        long thisDelay = this.getDelay(TimeUnit.NANOSECONDS);
        long otherDelay = other.getDelay(TimeUnit.NANOSECONDS);
        
        if (thisDelay != otherDelay) {
            return Long.compare(thisDelay, otherDelay);
        }
        
        // 実行時刻も同じなら提出順で比較〜📝
        return Long.compare(this.submissionTime, other.submissionTime);
    }
    
    /**
     * DelayedインターフェースのcompareTo実装〜✨
     * PriorityBlockingQueueで使われるよ〜💕
     * 
     * @param other 比較対象のDelayed
     * @return 比較結果
     */
    @Override
    public int compareTo(Delayed other) {
        if (other instanceof PriorityScheduledFuture) {
            return compareTo((PriorityScheduledFuture<V>) other);
        }
        return Long.compare(this.getDelay(TimeUnit.NANOSECONDS), 
                           other.getDelay(TimeUnit.NANOSECONDS));
    }
    
    // 以下、ScheduledFutureのメソッドを委譲〜✨
    
    @Override
    public long getDelay(TimeUnit unit) {
        return delegate.getDelay(unit);
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegate.cancel(mayInterruptIfRunning);
    }
    
    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }
    
    @Override
    public boolean isDone() {
        return delegate.isDone();
    }
    
    @Override
    public V get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }
    
    @Override
    public V get(long timeout, TimeUnit unit) 
            throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.get(timeout, unit);
    }
    
    /**
     * 文字列表現〜🎀
     * 
     * @return このオブジェクトの文字列表現
     */
    @Override
    public String toString() {
        return String.format("PriorityScheduledFuture{priority=%s, delay=%dms}", 
                           priority, getDelay(TimeUnit.MILLISECONDS));
    }
}
