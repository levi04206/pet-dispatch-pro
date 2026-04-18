package com.wenxu.common;

public final class ApiMessages {

    public static final String CODE_SENT = "验证码发送成功";
    public static final String CODE_INVALID = "验证码错误或已失效";

    public static final String UPLOAD_FILE_EMPTY = "上传文件不能为空";
    public static final String UPLOAD_FILENAME_EMPTY = "上传文件名不能为空";
    public static final String UPLOAD_FAILED = "文件上传失败";

    public static final String PET_ADD_SUCCESS = "添加宠物成功";
    public static final String PET_UPDATE_SUCCESS = "修改宠物成功";
    public static final String PET_UPDATE_NOT_FOUND_OR_FORBIDDEN = "宠物不存在或无权修改";
    public static final String PET_DELETE_SUCCESS = "删除成功";
    public static final String PET_NOT_FOUND_OR_FORBIDDEN = "宠物不存在或无权删除";

    public static final String SITTER_APPLY_DUPLICATE = "您已提交过申请或已经是宠托师，请勿重复提交";
    public static final String SITTER_APPLY_SUCCESS = "入驻申请已提交，请等待管理员审核";
    public static final String SITTER_NOT_FOUND = "当前用户还不是宠托师";
    public static final String SITTER_WORK_STATUS_SWITCH_FAILED = "状态切换失败，请确认已通过审核且状态值合法";
    public static final String SITTER_WORK_ACCEPTING = "已切换为接单中";
    public static final String SITTER_WORK_RESTING = "已切换为休息中";
    public static final String SITTER_AUDIT_STATUS_INVALID = "非法的审核状态码";
    public static final String SITTER_AUDIT_FAILED = "审核失败，找不到该申请记录";
    public static final String SITTER_AUDIT_APPROVED = "审批通过，该用户正式成为宠托师";
    public static final String SITTER_AUDIT_REJECTED = "已驳回该申请";

    public static final String ORDER_PAY_SUCCESS = "支付成功，订单已进入待接单状态";
    public static final String ORDER_PAY_FAILED = "订单不存在或状态异常，无法支付";
    public static final String ORDER_NOT_FOUND_OR_FORBIDDEN = "订单不存在或无权查看";
    public static final String ORDER_CANCEL_SUCCESS = "订单已取消";
    public static final String ORDER_CANCEL_FAILED = "取消失败，订单不存在、无权操作或当前状态不可取消";
    public static final String ORDER_EVALUATE_SUCCESS = "评价成功";
    public static final String ORDER_EVALUATE_FAILED = "评价失败，订单不存在、无权操作或当前状态不可评价";
    public static final String ORDER_GRAB_SUCCESS = "抢单成功";
    public static final String ORDER_GRAB_FAILED = "抢单失败，订单已被抢走或当前用户不具备接单资格";
    public static final String ORDER_REJECT_SUCCESS = "已拒绝该指定订单，系统已模拟取消订单、退款并通知用户重新选择";
    public static final String ORDER_REJECT_FAILED = "拒单失败，订单不存在、已被处理或不属于当前宠托师";
    public static final String ORDER_START_SUCCESS = "打卡成功，服务开始";
    public static final String ORDER_SERVICE_COMPLETE_SUCCESS = "服务已完成";
    public static final String ORDER_OPERATION_FAILED = "操作失败，请检查订单状态或归属权";

    private ApiMessages() {
    }
}
