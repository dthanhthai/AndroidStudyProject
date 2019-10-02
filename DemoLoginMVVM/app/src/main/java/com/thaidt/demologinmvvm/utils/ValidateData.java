package com.thaidt.demologinmvvm.utils;

public class ValidateData {

    private ValidateData(){}

    public static boolean validatePassword(String input){
        return input != null && input.trim().length() >= 6;
    }

    public static boolean validateEmail(String input){
        String pattern = "^([a-zA-Z0-9])(?!(\\.|\\_|\\-))(?!.*?(\\.|\\_|\\-)(\\.|\\_|\\-))([a-zA-Z0-9._-]+[a-zA-Z0-9])?@[a-z0-9]{1,}(\\.[a-z0-9]{1,4}){1,2}$";
        return input != null && input.trim().matches(pattern);
    }
}
