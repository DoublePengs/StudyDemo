package com.example.studydemo;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/7/11
 */

public class CMDMsgManager {

    private static CMDMsgManager instance = new CMDMsgManager();

    private OnRequestListener onEnterRoomListener;

    private CMDMsgManager() {
    }

    public static CMDMsgManager getInstance() {
        return instance;
    }

    public void receiveOnEnterRoom(int interviewId, String userName) {
        if (onEnterRoomListener != null) {
            onEnterRoomListener.onEnterRoom(interviewId, userName);
        }
    }

    public void setOnEnterRoomListener(OnRequestListener listener) {
        onEnterRoomListener = listener;
    }

    public interface OnRequestListener {
        /**
         * 全局进入房间的 CMD 接收处理回调
         *
         * @param interviewId 面试ID
         * @param userName    用户姓名
         */
        void onEnterRoom(int interviewId, String userName);
    }

}
