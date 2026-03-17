package top.emilejones.hhu.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "请求失败后返回的失败原因")
public class FailureVO {
    @Schema(description = "失败原因")
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
