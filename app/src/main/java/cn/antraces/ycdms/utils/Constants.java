package cn.antraces.ycdms.utils;

import cn.antraces.ycdms.MyApplication;
import cn.antraces.ycdms.R;

public interface Constants {
    /**
     * 更新地址
     */
    public static final String UpdateHostName = "https://f.hloli.cn";

    /**
     * 服务器地址
     */
    public static final String HostName = "http://127.0.0.1";

    /**
     * 新生注册地址
     */
    public static final String RegUrl = HostName + "/reg";

    /**
     * 登陆接口
     */
    public static final String LoginUrl = HostName + "/api/Worker/login";

    /**
     * 绑定(注册)接口
     */
    public static final String BoundUrl = HostName + "/api/Worker/bound";

    /**
     * 验证绑定接口
     */
    public static final String IsBoundUrl = HostName + "/api/Worker/isBound";

    /**
     * 验证权限接口
     */
    public static final String CheckPermissionUrl = HostName + "/api/Worker/checkPermission";

    /**
     * 获取会员信息接口
     */
    public static final String GetMembersUrl = HostName + "/api/Member/queryAll";

    /**
     * 删除会员接口
     */
    public static final String DelMemberUrl = HostName + "/api/Member/deleteById";

    /**
     * 修改会员照片状态接口
     */
    public static final String ChangePhotoUrl = HostName + "/api/Member/changePhoto";

    /**
     * 修改会员QQ接口
     */
    public static final String ChangeQQUrl = HostName + "/api/Member/changeQQ";

    /**
     * 获取会员信息表格接口
     */
    public static final String DownloadWorkerInfo = HostName + "/api/Member/exportExcel";

    /**
     * 获取注册信息接口
     */
    public static final String GetQrInfoUrl = HostName + "/api/Qrcode/getQrInfo";

    /**
     * 获取注册码接口
     */
    public static final String GetRegTokenUrl = HostName + "/api/Qrcode/getRegToken";

    /**
     * 获取注册码使用状态接口
     */
    public static final String IsRegTokenUsedUrl = HostName + "/api/Qrcode/isUsed";

    /**
     * 获取管理员信息接口
     */
    public static final String GetWorkerInfo = HostName + "/api/Worker/getWorkerInfo";

    /**
     * 未知错误Json字符串
     */
    public static final String UnknownErrorJsonString = "{'code':0,'msg':'" + MyApplication.getContext().getString(R.string.unknown_error) + "'}";

    /**
     * 弹窗 多个按钮
     */
    public static final int PopPageSelect = 1;

    /**
     * 弹窗 编辑框
     */
    public static final int PopPageEdit = 2;

    /**
     * 弹窗 显示详情
     */
    public static final int PopPageInfo = 3;

    /**
     * 弹窗 删除会员
     */
    public static final int PopPageDel = 4;

    /**
     * 弹窗 提示信息
     */
    public static final int PopPageTips = 5;

    /**
     * 弹窗 点击侧滑头部
     */
    public static final int PopPageNavHeader = 6;
}
