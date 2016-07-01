package com.dym.film.manager.data;

import com.dym.film.application.UserInfo;
import com.dym.film.manager.NetworkManager;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/12/7
 */
public class MainSharedTicketDataManager extends BaseSharedTicketDataManager
{
    public final static MainSharedTicketDataManager mInstance = new MainSharedTicketDataManager();

    /**
     * 改变所有我的头像的url
     */
    public synchronized void refreshMyUserInfo()
    {
        for (NetworkManager.SharedTicketRespModel model: mListData) {
            if (model.writer != null &&
                    UserInfo.userID == model.writer.userID) {
                model.writer.avatar = UserInfo.avatar;
                model.writer.gender = UserInfo.gender;
                model.writer.name = UserInfo.name;
                model.writer.mobile = UserInfo.mobile;
            }
        }
    }
}
