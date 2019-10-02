package com.thaidt.demologinmvvm.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.thaidt.demologinmvvm.model.User;
import com.thaidt.demologinmvvm.model.UserRepository;
import com.thaidt.demologinmvvm.utils.ValidateData;

public class LoginViewModel extends AndroidViewModel {
    public MutableLiveData<String> usernameInput = new MutableLiveData<>();
    public MutableLiveData<String> passwordInput = new MutableLiveData<>();

    private MutableLiveData<DataWrapper<User>> loginLiveData;

    private UserRepository userRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<DataWrapper<User>> getUserLiveData() {
        if (loginLiveData == null) {
            loginLiveData = new MutableLiveData<>();
        }
        return loginLiveData;
    }

    public void login() {
        loginLiveData.setValue(new DataWrapper<User>(DataWrapper.State.LOADING));
        new Thread(new Runnable() {
            @Override
            public void run() {
                String email = usernameInput.getValue();
                String password = passwordInput.getValue();

                if(email == null || email.isEmpty()){
                    loginLiveData.postValue(new DataWrapper<User>(DataWrapper.State.ERROR, "email is empty"));
                    return;
                }

                if(!ValidateData.validateEmail(email)){
                    loginLiveData.postValue(new DataWrapper<User>(DataWrapper.State.ERROR, "email is invalid"));
                    return;
                }

                if(password == null || password.isEmpty()){
                    loginLiveData.postValue(new DataWrapper<User>(DataWrapper.State.ERROR, "password is empty"));
                    return;
                }

                if(!ValidateData.validatePassword(password)){
                    loginLiveData.postValue(new DataWrapper<User>(DataWrapper.State.ERROR, "password is invalid"));
                    return;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    loginLiveData.postValue(new DataWrapper<User>(DataWrapper.State.ERROR, ""));
                    Log.e("Error", e.getMessage(), e);
                }

                User result = userRepository.login(email, password);
                if (result != null) {
                    loginLiveData.postValue(new DataWrapper<User>(DataWrapper.State.SUCCESS, result));
                } else {
                    loginLiveData.postValue(new DataWrapper<User>(DataWrapper.State.ERROR, "wrong username or password"));
                }
            }
        }).start();
    }

}
