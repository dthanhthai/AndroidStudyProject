package com.thaidt.demologinmvvm.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.thaidt.demologinmvvm.model.User;
import com.thaidt.demologinmvvm.model.UserRepository;

public class LoginViewModel extends AndroidViewModel {
    public MutableLiveData<String> usernameInput = new MutableLiveData<>();
    public MutableLiveData<String> passwordInput = new MutableLiveData<>();

    private MutableLiveData<DataWrapper<User>> loginLiveData;

    private MutableLiveData<Boolean> isShowLoading;

    private UserRepository userRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    private MutableLiveData<Boolean> getShowLoading() {
        if (isShowLoading == null) {
            isShowLoading = new MutableLiveData<>();
            isShowLoading.setValue(false);
        }
        return isShowLoading;
    }

    public LiveData<DataWrapper<User>> getUserLiveData() {
        if (loginLiveData == null) {
            loginLiveData = new MutableLiveData<>();
        }
        return loginLiveData;
    }

    @SuppressLint("StaticFieldLeak")
    public void login() {
        loginLiveData.setValue(new DataWrapper<User>(DataWrapper.State.LOADING));
//        new LoginUserAsyncTask(userRepository).execute(usernameInput.getValue(), passwordInput.getValue());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    loginLiveData.postValue(new DataWrapper<User>(DataWrapper.State.ERROR, ""));
                    Log.e("Error", e.getMessage(), e);
                }

                User result = userRepository.login(usernameInput.getValue(), passwordInput.getValue());

                if (result != null) {
                    loginLiveData.postValue(new DataWrapper<User>(DataWrapper.State.SUCCESS, result));
                } else {
                    loginLiveData.postValue(new DataWrapper<User>(DataWrapper.State.ERROR, "wrong username or password"));
                }
            }
        }).start();
    }

//    private class LoginUserAsyncTask extends AsyncTask<String, Void, User> {
//
//        private UserRepository userRepo;
//
//        private LoginUserAsyncTask(UserRepository userRepo) {
//            this.userRepo = userRepo;
//        }
//
//        @Override
//        protected User doInBackground(String... params) {
//            try {
//                Thread.sleep(1500);
//            } catch (InterruptedException e) {
//                loginLiveData.postValue(new DataWrapper<User>(DataWrapper.State.ERROR, ""));
//                Log.e("Error", e.getMessage(), e);
//            }
//            return userRepo.login(params[0], params[1]);
//        }
//
//        @Override
//        protected void onPostExecute(User user) {
//            if (user != null) {
//                loginLiveData.setValue(new DataWrapper<User>(DataWrapper.State.SUCCESS, user));
//            } else {
//                loginLiveData.setValue(new DataWrapper<User>(DataWrapper.State.ERROR, "Wrong email or password"));
//            }
//        }
//    }

}
