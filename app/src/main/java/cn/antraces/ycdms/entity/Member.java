package cn.antraces.ycdms.entity;

import com.alibaba.fastjson.annotation.JSONType;

import java.io.Serializable;

/**
 * (Member)实体类
 *
 * @author silver
 * @since 2021-11-16 11:39:56
 */
@JSONType(orders = {"id", "name", "cn", "series", "major", "phone", "qq", "tung", "room", "photo", "qrid", "department", "grade", "sex", "nation", "politics", "classe"})
public class Member implements Serializable {
    /**
     * 编号
     */
    private int id;
    /**
     * 姓名
     */
    private String name;

    private String cn;
    /**
     * 系别
     */
    private String series;
    /**
     * 专业
     */
    private String major;
    /**
     * 手机号
     */
    private long phone;
    /**
     * QQ号
     */
    private long qq;
    /**
     * 寝室栋数
     */
    private int tung;
    /**
     * 寝室房间号
     */
    private int room;
    /**
     * 是否已交照片
     */
    private int photo;
    /**
     * 二维码ID
     */
    private int qrid;
    /**
     * 1活动 2技术 3秘书 4宣传 5财务
     */
    private int department;
    /**
     * 年级
     */
    private int grade;
    /**
     * 性别
     */
    private int sex;
    /**
     * 民族
     */
    private String nation;
    /**
     * 政治面貌
     */
    private int politics;
    /**
     * 班级
     */
    private int classe;

    private int position;

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cn='" + cn + '\'' +
                ", series='" + series + '\'' +
                ", major='" + major + '\'' +
                ", phone=" + phone +
                ", qq=" + qq +
                ", tung=" + tung +
                ", room=" + room +
                ", photo=" + photo +
                ", qrid=" + qrid +
                ", department=" + department +
                ", grade=" + grade +
                ", sex=" + sex +
                ", nation='" + nation + '\'' +
                ", politics=" + politics +
                ", classe=" + classe +
                '}';
    }

    public String formatString(String from, String token, String time) {
        return "编号: " + id + "\n" +
                "圈名: " + cn + "\n" +
                "姓名: " + name + "\n" +
                "性别: " + (sex == 1 ? "男" : sex == 2 ? "女" : "未知") + "\n" +
                "民族: " + nation + "\n" +
                "年级: " + grade + "\n" +
                "院系: " + series + "\n" +
                "班级: " + major + classe + "班\n" +
                "手机: " + phone + "\n" +
                "QQ号: " + qq + "\n" +
                "部门: " + (department == 1 ? "活动部" : department == 2 ? "技术部" : department == 3 ? "秘书部" : department == 4 ? "宣传部" : department == 5 ? "财务部" : "未知") + "\n" +
                "面貌: " + (politics == 1 ? "群众" : politics == 2 ? "团员" : politics == 3 ? "党员" : "其他") + "\n" +
                "寝室: " + tung + "栋" + room + "\n" +
                "来源: " + from + "\n" +
                "时间: " + time + "\n" +
                "凭证: " + token;
    }

    public Member() {
        id = 0;
        name = "";
        cn = "";
        series = "";
        major = "";
        phone = 0;
        qq = 0;
        tung = 0;
        room = 0;
        photo = 0;
        qrid = 0;
        department = 0;
        grade = 0;
        sex = 0;
        nation = "";
        politics = 0;
        classe = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public long getQq() {
        return qq;
    }

    public void setQq(long qq) {
        this.qq = qq;
    }

    public int getTung() {
        return tung;
    }

    public void setTung(int tung) {
        this.tung = tung;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }

    public int getQrid() {
        return qrid;
    }

    public void setQrid(int qrid) {
        this.qrid = qrid;
    }

    public int getDepartment() {
        return department;
    }

    public void setDepartment(int department) {
        this.department = department;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public int getPolitics() {
        return politics;
    }

    public void setPolitics(int politics) {
        this.politics = politics;
    }

    public int getClasse() {
        return classe;
    }

    public void setClasse(int classe) {
        this.classe = classe;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}

