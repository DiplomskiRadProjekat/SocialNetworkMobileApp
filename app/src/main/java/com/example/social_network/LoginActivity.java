package com.example.social_network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.auth0.android.jwt.JWT;
import com.example.social_network.dtos.AuthenticationRequestDTO;
import com.example.social_network.dtos.AuthenticationResponseDTO;
import com.example.social_network.dtos.Token;
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.services.IAuthService;
import com.example.social_network.services.ServiceUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewUsername, textViewPassword, textViewRegister;

    private ImageView togglePasswordVisibility;

    private boolean isPasswordVisible = false;

    private SharedPreferences sharedPreferences;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deleteTokenPreferences();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.username);
        editTextPassword = findViewById(R.id.password);

        buttonLogin = findViewById(R.id.login_button);

        textViewUsername = findViewById(R.id.usernameLabel);
        textViewPassword = findViewById(R.id.passwordLabel);
        textViewRegister = findViewById(R.id.register);

        togglePasswordVisibility = findViewById(R.id.toggle_password_visibility);

        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("pref_token", "");
    }

    @Override
    protected void onResume() {
        super.onResume();

        togglePasswordVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
            } else {
                editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility);
            }
            isPasswordVisible = !isPasswordVisible;

            editTextPassword.setSelection(editTextPassword.getText().length());
        });

        buttonLogin.setOnClickListener(view -> {
            String username = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            boolean hasError = false;

            if (username.isEmpty()) {
                Log.e("Error", "Empty username");
                setError(editTextUsername, textViewUsername);
                hasError = true;
            } else {
                resetError(editTextUsername, textViewUsername);
            }

            if (password.isEmpty()) {
                Log.e("Error", "Empty password");
                setError(editTextPassword, textViewPassword);
                hasError = true;
            } else {
                resetError(editTextPassword, textViewPassword);
            }

            if (!hasError) {
                AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO(username, password);
                IAuthService authService = ServiceUtils.authService(token);
                Call<AuthenticationResponseDTO> call = authService.login(authenticationRequestDTO);
                call.enqueue(new Callback<AuthenticationResponseDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthenticationResponseDTO> call, @NonNull Response<AuthenticationResponseDTO> response) {
                        if (response.isSuccessful()) {
                            AuthenticationResponseDTO tokenDTO = response.body();
                            if (tokenDTO != null) {
                                String token = tokenDTO.getToken();
                                Log.i("Success", "Token: " + token);
                                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                JWT jwt = new JWT(token);

                                Long id = jwt.getClaim("id").asLong();
                                String username = jwt.getClaim("sub").asString();

                                setToken(tokenDTO);
                                setPreferences(id, username, tokenDTO);
                                setTokenPreference(tokenDTO.getToken(), tokenDTO.getRefreshToken());

                                loadUser(id, password, tokenDTO.getToken());
                            } else {
                                Log.e("Error", "Login failed.");
                                Toast.makeText(LoginActivity.this, "Login failed. Invalid server response.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("Error", "Login failed.");
                            Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }


                    @Override
                    public void onFailure(@NonNull Call<AuthenticationResponseDTO> call, @NonNull Throwable t) {
                        Log.e("Error", "Login failed.");
                        Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        textViewRegister.setOnClickListener(view -> startActivity(new Intent(this, RegistrationActivity.class)));
    }

    private void setError(EditText editText, TextView textView) {
        editText.setBackgroundResource(R.drawable.edit_text_error);
        textView.setTextColor(ContextCompat.getColor(this, R.color.error));
    }

    private void resetError(EditText editText, TextView textView) {
        editText.setBackgroundResource(R.drawable.edit_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.primaryDarkColor));
    }

    private void setToken(AuthenticationResponseDTO authenticationResponseDTO) {
        Token tokenDTO = Token.getInstance();
        tokenDTO.setAccessToken(authenticationResponseDTO.getToken());
        tokenDTO.setRefreshToken(authenticationResponseDTO.getRefreshToken());
    }

    private void deleteTokenPreferences() {
        Token tokenDTO = Token.getInstance();
        tokenDTO.setAccessToken(null);
        tokenDTO.setRefreshToken(null);
        this.sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = this.sharedPreferences.edit();
        spEditor.clear().apply();
    }

    private void setTokenPreference(String token, String refreshToken) {
        this.sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = this.sharedPreferences.edit();
        spEditor.putString("pref_token", token);
        spEditor.putString("pref_refreshToken", refreshToken);
        spEditor.apply();
    }

    private void setSharedPreferences(Long id, String username){
        this.sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = this.sharedPreferences.edit();
        spEditor.putLong("pref_id", id);
        spEditor.putString("pref_sub", username);
        spEditor.apply();
    }

    private void setPreferences(Long id, String username, AuthenticationResponseDTO loginResponse){
        setSharedPreferences(id, username);
        setTokenPreference(loginResponse.getToken(), loginResponse.getRefreshToken());
    }

    private void loadUser(Long id, String password, String accessToken) {
        Call<UserDTO> call = ServiceUtils.userService(accessToken).get(id);
        call.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(@NonNull Call<UserDTO> call, @NonNull Response<UserDTO> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    UserDTO userDTO = response.body();
                    if (userDTO != null) {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(userDTO.getEmail(), password)
                                .addOnSuccessListener(authResult -> {
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    if (e instanceof FirebaseAuthInvalidUserException) {
                                        Toast.makeText(LoginActivity.this, "User doesn't exist!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    onFailure(call, new Throwable("API call failed with status code: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDTO> call, Throwable t) {
                Log.d("Fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

}