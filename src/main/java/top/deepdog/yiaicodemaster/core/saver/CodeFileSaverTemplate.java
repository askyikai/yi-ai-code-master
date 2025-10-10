package top.deepdog.yiaicodemaster.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import top.deepdog.yiaicodemaster.constant.AppConstant;
import top.deepdog.yiaicodemaster.exception.ErrorCode;
import top.deepdog.yiaicodemaster.exception.ThrowUtils;
import top.deepdog.yiaicodemaster.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

public abstract class CodeFileSaverTemplate<T> {
    // File save root directory
    private static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 保存代码
     */
    public final File saveCode(T result, Long appId) {
        // 1.验证输入
        validateInput(result);
        // 2.构建目录
        String uniqueDir = buildUniqueDir(appId);
        // 3.保存文件（抽象方法）
        saveFile(result, uniqueDir);
        // 4.返回保存的目录文件对象
        return new File(uniqueDir);
    }

    protected abstract void saveFile(T result, String uniqueDir);

    protected void validateInput(T result) {
        ThrowUtils.throwIf(result == null, ErrorCode.PARAMS_ERROR, "输入参数为空");
    }

    protected final String buildUniqueDir(Long appId) {
        String codeType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", codeType, appId);
        String uniqueDirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(uniqueDirPath);
        return uniqueDirPath;
    }

    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 写入单个文件
     */
    protected final void writeToFile(String dirPath, String fileName, String content) {
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + fileName;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

}
