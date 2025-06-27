package com.example;

public enum Priority {

    /**
     * 🔥 超緊急！今すぐやって！🔥
     * めっちゃ重要なタスクはこれ使って〜
     */
    URGENT(1),
    
    /**
     * ⚡ 高優先度！急いで〜⚡
     * 普通に重要なやつはこれかな〜
     */
    HIGH(2),
    
    /**
     * 📝 普通の優先度だよ〜📝
     * デフォルトはこれ！まあまあ大事〜
     */
    NORMAL(3),
    
    /**
     * 😴 低優先度〜時間あるときでいいよ〜😴
     * 急がないやつはこれで〜
     */
    LOW(4);
    
    private final int value;
    
    /**
     * 優先度の値を設定するコンストラクタ〜💖
     * 
     * @param value 優先度の数値（小さいほど優先度高い！）
     */
    Priority(int value) {
        this.value = value;
    }
    
    /**
     * 優先度の数値を取得するよ〜✨
     * 
     * @return 優先度の数値
     */
    public int getValue() {
        return value;
    }
    
    /**
     * 他の優先度と比較するメソッド〜💕
     * 
     * @param other 比較する優先度
     * @return この優先度の方が高い場合true
     */
    public boolean isHigherThan(Priority other) {
        return this.value < other.value;
    }
    
    /**
     * 優先度を文字列で表現〜🎀
     * 
     * @return 優先度の文字列表現
     */
    @Override
    public String toString() {
        return name() + "(" + value + ")";
    }
}
