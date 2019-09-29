package com.thaidt.demologinmvvm;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thaidt.demologinmvvm.model.User;
import com.thaidt.demologinmvvm.model.UserRepository;

public class LoginViewModel extends AndroidViewModel {
    public MutableLiveData<String> emailInput = new MutableLiveData<>();
    public MutableLiveData<String> passwordInput = new MutableLiveData<>();

    private MutableLiveData<User> loginLiveData;

    public MutableLiveData<Boolean> isShowLoading;

    private UserRepository userRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public MutableLiveData<Boolean> getShowLoading(){
        if (isShowLoading == null) {
            isShowLoading = new MutableLiveData<Boolean>();
            isShowLoading.setValue(false);
        }
        return isShowLoading;
    }

    public LiveData<User> getUserLiveData() {
        if (loginLiveData == null) {
            loginLiveData = new MutableLiveData<>();
        }
        return loginLiveData;
    }

    public void showLoading(Boolean isShow) {
        getShowLoading().setValue(isShow);
    }

    @SuppressLint("StaticFieldLeak")
    public void login() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new LoginUserAsyncTask(userRepository).execute(emailInput.getValue(), passwordInput.getValue());
            }
        }, 2000);

    }

    private class LoginUserAsyncTask extends AsyncTask<String, Void, User> {

        private UserRepository userRepo;

        private LoginUserAsyncTask(UserRepository userRepo) {
            this.userRepo = userRepo;
        }

        @Override
        protected User doInBackground(String... params) {
            loginLiveData.postValue(userRepo.login(params[0], params[1]));
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            loginLiveData.setValue(user);
        }
    }

//    @BindingAdapter("isShow")
//    public static void setVisibilityLoading(View view, Boolean isShow) {
//        if (isShow) {
//            view.setVisibility(View.VISIBLE);
//        } else {
//            view.setVisibility(View.GONE);
//        }
//    }
}
