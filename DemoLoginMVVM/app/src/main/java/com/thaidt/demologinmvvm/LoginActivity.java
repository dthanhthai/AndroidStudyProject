package com.thaidt.demologinmvvm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.thaidt.demologinmvvm.databinding.ActivityLoginBinding;
import com.thaidt.demologinmvvm.model.User;

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

        Button button = findViewById(R.id.btn_login);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginViewModel.showLoading(true);
                loginViewModel.login();
            }
        });
    }

    public void observeData() {
        loginViewModel.getUserLiveData().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                loginViewModel.showLoading(false);
                if(user != null){
                    Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(LoginActivity.this, "Fail", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
