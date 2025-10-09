package top.deepdog.yiaicodemaster.core.parser;

/**
 * 代码解析器策略接口
 *
 * @param <T> 解析结果类型
 */
public interface CodeParser<T> {

    /**
     * 解析代码
     *
     * @param codeContent 代码内容
     * @return 解析结果
     */
    T parseCode(String codeContent);
}
