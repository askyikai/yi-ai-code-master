package top.deepdog.yiaicodemaster.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.deepdog.yiaicodemaster.common.BaseResponse;
import top.deepdog.yiaicodemaster.common.ResultUtils;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/check")
    public BaseResponse<String> check() {
        return ResultUtils.success("ok");
    }
}
