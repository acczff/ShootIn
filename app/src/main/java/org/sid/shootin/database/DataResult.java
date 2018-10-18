package org.sid.shootin.database;

/**
 * 处理结果
 *
 * @param <T> 成功时携带数据
 */
public class DataResult<T> {
    public static final int
            RESULT_CODE_OK = 1,
            RESULT_CODE_ERR = 2,
            RESULT_CODE_NOTHING = 0;

    public int resultCode;
    public T data;
    public String message = "";

    public DataResult(int resultCode, T data, String message) {
        this.resultCode = resultCode;
        this.data = data;
        this.message = message;
    }

    public DataResult() {
        this(RESULT_CODE_NOTHING, null, null);
    }

    /**
     * 成功断言<br/>
     * 如果返回码为{@link #RESULT_CODE_OK}时返回携带的数据，否则抛出{@link ResultException}异常
     *
     * @return 成功数据为
     * @throws ResultException 失败异常
     */
    public T success() throws ResultException {
        if (this.resultCode == RESULT_CODE_OK)
            return this.data;
        else throw new ResultException(this.message);
    }

    /**
     * 错误断言<br/>
     * 如果返回码为{@link #RESULT_CODE_ERR}则抛出{@link ResultException}异常
     */
    public void errAssert() {
        if (this.resultCode == RESULT_CODE_ERR)
            throw new ResultException(this.message);
    }

    public static <E> DataResult<E> err() {
        return new DataResult<>(RESULT_CODE_ERR, null, null);
    }

    public static <D> DataResult<D> ok(D data) {
        DataResult<D> dataResult = new DataResult<>();
        dataResult.resultCode = RESULT_CODE_OK;
        dataResult.data = data;
        return dataResult;
    }

    public static class ResultException extends RuntimeException {
        public ResultException(String msg) {
            super(msg);
        }

    }
}
