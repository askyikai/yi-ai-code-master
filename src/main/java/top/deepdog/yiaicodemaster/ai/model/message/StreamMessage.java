package top.deepdog.yiaicodemaster.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流消息响应基类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamMessage {
    private String type;
}
