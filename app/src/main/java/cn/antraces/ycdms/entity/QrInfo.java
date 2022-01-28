package cn.antraces.ycdms.entity;

public class QrInfo {
    private int id;
    private String token;
    private long usetime;
    private String name;
    private String job;

    @Override
    public String toString() {
        return "QrInfo{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", usetime=" + usetime +
                ", name='" + name + '\'' +
                ", job='" + job + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getUsetime() {
        return usetime;
    }

    public void setUsetime(long usetime) {
        this.usetime = usetime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }
}
