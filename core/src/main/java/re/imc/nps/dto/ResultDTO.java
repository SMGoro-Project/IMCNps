package re.imc.nps.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultDTO<T> implements Serializable {

    private int code = 0;
    private String msg = "成功";
    private String errDesc;
    private T data;
    private boolean success;

    public ResultDTO() {

    }

    public ResultDTO(T data) {
        setData(data);
    }

    public ResultDTO(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultDTO(int code, String msg, String errDesc) {
        this.code = code;
        this.msg = msg;
        this.errDesc = errDesc;
    }

    public ResultDTO(int code, String msg, T data) {
        setCode(code);
        setMsg(msg);
        setData(data);
    }


    public boolean isSuccess() {
        return this.code == 0;
    }
}
