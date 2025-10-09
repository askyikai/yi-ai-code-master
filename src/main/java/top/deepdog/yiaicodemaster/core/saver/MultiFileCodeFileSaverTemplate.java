package top.deepdog.yiaicodemaster.core.saver;

import top.deepdog.yiaicodemaster.ai.model.MultiFileCodeResult;
import top.deepdog.yiaicodemaster.exception.ErrorCode;
import top.deepdog.yiaicodemaster.exception.ThrowUtils;
import top.deepdog.yiaicodemaster.model.enums.CodeGenTypeEnum;

public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult>{
    @Override
    protected void saveFile(MultiFileCodeResult result, String uniqueDir) {
        writeToFile(uniqueDir, "index.html", result.getHtmlCode());
        writeToFile(uniqueDir, "style.css", result.getCssCode());
        writeToFile(uniqueDir, "script.js", result.getJsCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        ThrowUtils.throwIf(result.getHtmlCode() == null || result.getHtmlCode().isEmpty(),
                ErrorCode.SYSTEM_ERROR, "HTML代码不能为空");
    }
}
