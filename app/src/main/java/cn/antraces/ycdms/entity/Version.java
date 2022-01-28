package cn.antraces.ycdms.entity;

import java.io.Serializable;

public class Version implements Serializable {
    private int vn;
    private String ver;
    private String content;

    @Override
    public String toString() {
        return "Version{" +
                "vn=" + vn +
                ", ver='" + ver + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    public int getVn() {
        return vn;
    }

    public void setVn(int vn) {
        this.vn = vn;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
