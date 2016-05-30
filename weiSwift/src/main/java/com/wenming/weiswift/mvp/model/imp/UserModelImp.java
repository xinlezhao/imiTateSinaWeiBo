package com.wenming.weiswift.mvp.model.imp;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.wenming.weiswift.api.FriendshipsAPI;
import com.wenming.weiswift.api.StatusesAPI;
import com.wenming.weiswift.api.UsersAPI;
import com.wenming.weiswift.entity.Status;
import com.wenming.weiswift.entity.Token;
import com.wenming.weiswift.entity.User;
import com.wenming.weiswift.entity.list.StatusList;
import com.wenming.weiswift.entity.list.TokenList;
import com.wenming.weiswift.entity.list.UserList;
import com.wenming.weiswift.mvp.model.UserModel;
import com.wenming.weiswift.ui.common.NewFeature;
import com.wenming.weiswift.ui.common.login.AccessTokenKeeper;
import com.wenming.weiswift.ui.common.login.Constants;
import com.wenming.weiswift.utils.SDCardUtil;
import com.wenming.weiswift.utils.ToastUtil;

import java.util.ArrayList;

/**
 * Created by wenmingvs on 16/5/14.
 */
public class UserModelImp implements UserModel {
    private ArrayList<Status> mStatusList = new ArrayList<>();
    private ArrayList<User> mFollowersList = new ArrayList<>();
    private ArrayList<User> mFriendsList = new ArrayList<>();
    private int mFollowersCursor;
    private int mFriendsCursor;
    private ArrayList<User> mUserArrayList;

    @Override
    public void showUserDetail(long uid, final Context context, final OnUserDetailRequestFinish onUserRequestFinish) {
        UsersAPI mUsersAPI = new UsersAPI(context, Constants.APP_KEY, AccessTokenKeeper.readAccessToken(context));
        mUsersAPI.show(uid, new RequestListener() {
            @Override
            public void onComplete(String response) {
                SDCardUtil.put(context, SDCardUtil.getSDCardPath() + "/weiSwift/", "username_" + AccessTokenKeeper.readAccessToken(context).getUid(), response);
                User user = User.parse(response);
                onUserRequestFinish.onComplete(user);
            }

            @Override
            public void onWeiboException(WeiboException e) {
                ToastUtil.showShort(context, e.getMessage());
                onUserRequestFinish.onError(e.getMessage());
            }
        });
    }

    @Override
    public User showUserDetailSync(long uid, final Context context) {
        UsersAPI mUsersAPI = new UsersAPI(context, Constants.APP_KEY, AccessTokenKeeper.readAccessToken(context));
        return User.parse(mUsersAPI.showSync(uid));
    }

    @Override
    public void userTimeline(final long uid, final Context context, final OnStatusListFinishedListener onStatusFinishedListener) {
        StatusesAPI mStatusesAPI = new StatusesAPI(context, Constants.APP_KEY, AccessTokenKeeper.readAccessToken(context));
        mStatusesAPI.userTimeline(uid, 0, 0, NewFeature.GET_WEIBO_NUMS, 1, false, 0, false, new RequestListener() {
            @Override
            public void onComplete(String response) {
                ArrayList<Status> temp = StatusList.parse(response).statusList;
                if (temp != null && temp.size() > 0) {
                    if (mStatusList != null) {
                        mStatusList.clear();
                    }
                    mStatusList = temp;
                    onStatusFinishedListener.onDataFinish(mStatusList);
                } else {
                    ToastUtil.showShort(context, "没有更新的内容了");
                    onStatusFinishedListener.noMoreDate();
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {
                ToastUtil.showShort(context, e.getMessage());
                onStatusFinishedListener.onError(e.getMessage());
            }
        });
    }

    @Override
    public void userTimelineNextPage(final long uid, final Context context, final OnStatusListFinishedListener onStatusFinishedListener) {
        StatusesAPI mStatusesAPI = new StatusesAPI(context, Constants.APP_KEY, AccessTokenKeeper.readAccessToken(context));
        mStatusesAPI.userTimeline(uid, 0, Long.valueOf(mStatusList.get(mStatusList.size() - 1).id), NewFeature.LOADMORE_WEIBO_ITEM, 1, false, 0, false, new RequestListener() {
            @Override
            public void onComplete(String response) {
                if (!TextUtils.isEmpty(response)) {
                    ArrayList<Status> temp = StatusList.parse(response).statusList;
                    if (temp.size() == 0 || (temp != null && temp.size() == 1 && temp.get(0).id.equals(mStatusList.get(mStatusList.size() - 1).id))) {
                        onStatusFinishedListener.noMoreDate();
                    } else if (temp.size() > 1) {
                        temp.remove(0);
                        mStatusList.addAll(temp);
                        onStatusFinishedListener.onDataFinish(mStatusList);
                    }
                } else {
                    onStatusFinishedListener.noMoreDate();
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {
                ToastUtil.showShort(context, e.getMessage());
                onStatusFinishedListener.onError(e.getMessage());
            }
        });
    }

    @Override
    public void followers(final long uid, final Context context, final OnUserListRequestFinish onUserListRequestFinish) {

        FriendshipsAPI mFriendshipsAPI = new FriendshipsAPI(context, Constants.APP_KEY, AccessTokenKeeper.readAccessToken(context));

        mFriendshipsAPI.followers(uid, 30, 0, false, new RequestListener() {
            @Override
            public void onComplete(String response) {
                ArrayList<User> temp = UserList.parse(response).usersList;
                if (temp != null && temp.size() > 0) {
                    if (mFollowersList != null) {
                        mFollowersList.clear();
                    }
                    mFollowersList = temp;
                    mFollowersCursor = Integer.valueOf(StatusList.parse(response).next_cursor);
                    onUserListRequestFinish.onDataFinish(mFollowersList);
                } else {
                    ToastUtil.showShort(context, "没有更新的内容了");
                    onUserListRequestFinish.noMoreDate();
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {
                ToastUtil.showShort(context, e.getMessage());
                onUserListRequestFinish.onError(e.getMessage());
            }
        });
    }

    @Override
    public void followersNextPage(final long uid, final Context context, final OnUserListRequestFinish onUserListRequestFinish) {
        FriendshipsAPI mFriendshipsAPI = new FriendshipsAPI(context, Constants.APP_KEY, AccessTokenKeeper.readAccessToken(context));
        mFriendshipsAPI.followers(uid, 20, mFollowersCursor, false, new RequestListener() {
            @Override
            public void onComplete(String response) {
                if (!TextUtils.isEmpty(response)) {
                    ArrayList<User> temp = UserList.parse(response).usersList;
                    if (temp.size() == 0 || (temp != null && temp.size() == 1 && temp.get(0).id.equals(mFollowersList.get(mFollowersList.size() - 1).id))) {
                        onUserListRequestFinish.noMoreDate();
                    } else if (temp.size() > 1) {
                        temp.remove(0);
                        mFollowersList.addAll(temp);
                        mFollowersCursor = Integer.valueOf(UserList.parse(response).next_cursor);
                        onUserListRequestFinish.onDataFinish(mFollowersList);
                    }
                } else {
                    ToastUtil.showShort(context, "内容已经加载完了");
                    onUserListRequestFinish.noMoreDate();
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {
                ToastUtil.showShort(context, e.getMessage());
                onUserListRequestFinish.onError(e.getMessage());
            }
        });
    }

    @Override
    public void friends(final long uid, final Context context, final OnUserListRequestFinish onUserListRequestFinish) {
        FriendshipsAPI mFriendshipsAPI = new FriendshipsAPI(context, Constants.APP_KEY, AccessTokenKeeper.readAccessToken(context));

        mFriendshipsAPI.friends(uid, 30, 0, false, new RequestListener() {
            @Override
            public void onComplete(String response) {
                ArrayList<User> temp = UserList.parse(response).usersList;

                if (temp != null && temp.size() > 0) {
                    if (mFriendsList != null) {
                        mFriendsList.clear();
                    }
                    mFriendsList = temp;
                    mFriendsCursor = Integer.valueOf(StatusList.parse(response).next_cursor);
                    onUserListRequestFinish.onDataFinish(mFriendsList);
                } else {
                    ToastUtil.showShort(context, "没有更新的内容了");
                    onUserListRequestFinish.noMoreDate();
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {
                ToastUtil.showShort(context, e.getMessage());
                onUserListRequestFinish.onError(e.getMessage());
            }
        });
    }

    /**
     * 获取指定用户的粉丝列表
     *
     * @param uid
     * @param context
     * @param onUserListRequestFinish
     */
    @Override
    public void friendsNextPage(final long uid, final Context context, final OnUserListRequestFinish onUserListRequestFinish) {
        FriendshipsAPI mFriendshipsAPI = new FriendshipsAPI(context, Constants.APP_KEY, AccessTokenKeeper.readAccessToken(context));
        mFriendshipsAPI.friends(uid, 20, mFriendsCursor, false, new RequestListener() {
            @Override
            public void onComplete(String response) {
                if (!TextUtils.isEmpty(response)) {
                    ArrayList<User> temp = UserList.parse(response).usersList;
                    if (temp.size() == 0 || (temp != null && temp.size() == 1 && temp.get(0).id.equals(mFriendsList.get(mFriendsList.size() - 1).id))) {
                        onUserListRequestFinish.noMoreDate();
                    } else if (temp.size() > 1) {
                        temp.remove(0);
                        mFriendsList.addAll(temp);
                        mFriendsCursor = Integer.valueOf(UserList.parse(response).next_cursor);
                        onUserListRequestFinish.onDataFinish(mFriendsList);
                    }
                } else {
                    ToastUtil.showShort(context, "内容已经加载完了");
                    onUserListRequestFinish.noMoreDate();
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {
                ToastUtil.showShort(context, e.getMessage());
                onUserListRequestFinish.onError(e.getMessage());
            }
        });
    }

    @Override
    public void getUserDetailList(final Context context, final OnUserListRequestFinish onUserListRequestFinish) {
        String jsonstring = SDCardUtil.get(context, SDCardUtil.getSDCardPath() + "/weiSwift/", "登录列表缓存.txt");
        if (jsonstring == null && AccessTokenKeeper.readAccessToken(context).isSessionValid()) {
            cacheCurrentOuthToken(context);
        }
        final ArrayList<Token> tokenList = TokenList.parse(SDCardUtil.get(context, SDCardUtil.getSDCardPath() + "/weiSwift/", "登录列表缓存.txt")).tokenList;
        if (tokenList == null || tokenList.size() == 0) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                mUserArrayList = new ArrayList<User>();
                for (Token token : tokenList) {
                    mUserArrayList.add(showUserDetailSync(Long.valueOf(token.getUid()), context));
                }
                onUserListRequestFinish.onDataFinish(mUserArrayList);
            }
        }).start();
    }

    @Override
    public void deleteUserByUid(long uid, Context context, OnUserDeleteListener onUserDeleteListener) {
        int i = 0;
        for (i = 0; i < mUserArrayList.size(); i++) {
            if (mUserArrayList.get(i).id.equals(String.valueOf(uid))) {
                mUserArrayList.remove(i);
                i--;
                break;
            }
        }

        if (mUserArrayList.size() == 0) {
            onUserDeleteListener.onEmpty();
            return;
        }


        if (i >= mUserArrayList.size()) {
            onUserDeleteListener.onError("没有找到对应的账户");
        } else {
            onUserDeleteListener.onSuccess(mUserArrayList);
        }
    }

    public void cacheCurrentOuthToken(Context context) {
        String tokenString = AccessTokenKeeper.readAccessToken(context).getToken();
        String expiresIn = String.valueOf(AccessTokenKeeper.readAccessToken(context).getExpiresTime());
        String refresh_token = AccessTokenKeeper.readAccessToken(context).getRefreshToken();
        String uid = AccessTokenKeeper.readAccessToken(context).getUid();
        Token token = new Token(tokenString, expiresIn, refresh_token, uid);
        TokenList tokenList = new TokenList();
        tokenList.tokenList.add(token);
        tokenList.current_uid = uid;
        tokenList.total_number = tokenList.tokenList.size();
        Gson gson = new Gson();
        SDCardUtil.put(context, SDCardUtil.getSDCardPath() + "/weiSwift/", "登录列表缓存.txt", gson.toJson(tokenList));
    }


}
