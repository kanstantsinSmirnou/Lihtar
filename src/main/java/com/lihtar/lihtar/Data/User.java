package com.lihtar.lihtar.Data;

import java.util.ArrayList;

public class User {

    public static User MainUser = null;
    private String nickname;
    public static Integer UserID;
    private User(String Nickname, Integer id) {
        this.nickname = Nickname;
        this.UserID = id;
    }
    public String getNickname(){
        return this.nickname;
    }
    public static User makeUserFromBase(String nickname, Integer id) {
        return new User(nickname, id);
    }


}
