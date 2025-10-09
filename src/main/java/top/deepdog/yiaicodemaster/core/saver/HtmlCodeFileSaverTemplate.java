package top.deepdog.yiaicodemaster.core.saver;

import top.deepdog.yiaicodemaster.ai.model.HtmlCodeResult;
import top.deepdog.yiaicodemaster.model.enums.CodeGenTypeEnum;

public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult>
{
    @Override
    protected void saveFile(HtmlCodeResult result, String uniqueDir) {
        writeToFile(uniqueDir, "index.html", result.getHtmlCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }
}
