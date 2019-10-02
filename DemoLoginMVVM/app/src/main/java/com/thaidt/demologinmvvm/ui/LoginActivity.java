package com.thaidt.demologinmvvm.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.thaidt.demologinmvvm.R;
import com.thaidt.demologinmvvm.databinding.ActivityLoginBinding;
import com.thaidt.demologinmvvm.model.User;
import com.thaidt.demologinmvvm.viewmodel.DataWrapper;
import com.thaidt.demologinmvvm.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        binding.setLifecycleOwner(this);

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        binding.setLoginVM(loginViewModel);
        observeData();

//        Button button = findViewById(R.id.btn_login);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                loginViewModel.login();
//            }
//        });
    }

    public void observeData() {
        loginViewModel.getUserLiveData().observe(this, new Observer<DataWrapper<User>>() {
            @Override
            public void onChanged(DataWrapper<User> data) {
                View loadingView = findViewById(R.id.view_loading);
                switch (data.getState()) {
                    case LOADING:
                        loadingView.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        loadingView.setVisibility(View.GONE);
                        goToMainActivity();
                        Toast.makeText(LoginActivity.this, "Welcome " + data.getData().getUsername(), Toast.LENGTH_LONG).show();
                        break;
                    case ERROR:
                        loadingView.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Error: " +data.getMessage(), Toast.LENGTH_LONG).show();
                        break;
                    default:
                        loadingView.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }

    private void goToMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
