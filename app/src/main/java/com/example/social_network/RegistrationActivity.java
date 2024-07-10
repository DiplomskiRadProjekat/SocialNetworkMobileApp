package com.example.social_network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.social_network.dtos.NewUserDTO;
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.services.IAuthService;
import com.example.social_network.services.ServiceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegistrationActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextUsername, editTextPassword;
    private Button buttonRegister;
    private TextView textViewFirstName, textViewLastName, textViewEmail, textViewUsername, textViewPassword, textViewLogin;

    private ImageView togglePasswordVisibility;

    private boolean isPasswordVisible = false;

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_registration);

        editTextFirstName = findViewById(R.id.firstName);
        editTextLastName = findViewById(R.id.lastName);
        editTextEmail = findViewById(R.id.email);
        editTextUsername = findViewById(R.id.username);
        editTextPassword = findViewById(R.id.password);

        buttonRegister = findViewById(R.id.register_button);

        textViewFirstName = findViewById(R.id.firstNameLabel);
        textViewLastName = findViewById(R.id.lastNameLabel);
        textViewEmail = findViewById(R.id.emailLabel);
        textViewUsername = findViewById(R.id.usernameLabel);
        textViewPassword = findViewById(R.id.passwordLabel);
        textViewLogin = findViewById(R.id.login);

        togglePasswordVisibility = findViewById(R.id.toggle_password_visibility);

        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
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

        buttonRegister.setOnClickListener(view -> {
            String firstName = editTextFirstName.getText().toString();
            String lastName = editTextLastName.getText().toString();
            String email = editTextEmail.getText().toString();
            String username = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            boolean hasError = false;

            if (firstName.isEmpty()) {
                Log.e("Error", "Empty first name");
                setError(editTextFirstName, textViewFirstName);
                hasError = true;
            } else {
                resetError(editTextFirstName, textViewFirstName);
            }

            if (lastName.isEmpty()) {
                Log.e("Error", "Empty last name");
                setError(editTextLastName, textViewLastName);
                hasError = true;
            } else {
                resetError(editTextLastName, textViewLastName);
            }

            if (email.isEmpty()) {
                Log.e("Error", "Empty email");
                setError(editTextEmail, textViewEmail);
                hasError = true;
            } else {
                resetError(editTextEmail, textViewEmail);
            }

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
                NewUserDTO newUserDTO = new NewUserDTO(email, firstName, lastName, username, password, null);
                IAuthService authService = ServiceUtils.authService(token);
                Call<UserDTO> call = authService.register(newUserDTO);
                call.enqueue(new Callback<UserDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<UserDTO> call, @NonNull Response<UserDTO> response) {
                        if (response.isSuccessful()) {
                            UserDTO userDTO = response.body();
                            if (userDTO != null) {
                                Log.i("Success", "New user: " + userDTO.getUsername());
                                Toast.makeText(RegistrationActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                            } else {
                                Log.e("Error", "Registration failed.");
                                Toast.makeText(RegistrationActivity.this, "Registration failed. Invalid server response.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (response.code() == 400) {
                                try {
                                    JSONObject errorBody = new JSONObject(response.errorBody().string());
                                    Map<String, String> errors = new HashMap<>();

                                    if (errorBody.has("message")) {
                                        String message = errorBody.getString("message");
                                        String[] errorMessages = message.split("; ");

                                        for (String errorMessage : errorMessages) {
                                            String[] fieldError = errorMessage.split(": ");
                                            if (fieldError.length == 2) {
                                                String fieldName = fieldError[0];
                                                String error = fieldError[1];
                                                errors.put(fieldName, error);
                                            }
                                        }
                                    }

                                    if (errors.containsKey("email")) {
                                        setError(editTextEmail, textViewEmail);
                                        String emailError = errors.get("email");
                                        editTextEmail.setError(emailError);
                                    } else {
                                        resetError(editTextEmail, textViewEmail);
                                    }

                                    if (errors.containsKey("username")) {
                                        setError(editTextUsername, textViewUsername);
                                        String usernameError = errors.get("username");
                                        editTextUsername.setError(usernameError);
                                    } else {
                                        resetError(editTextUsername, textViewUsername);
                                    }

                                    if (errors.containsKey("password")) {
                                        setError(editTextPassword, textViewPassword);
                                        String passwordError = errors.get("password");
                                        togglePasswordVisibility.setVisibility(View.GONE);
                                        editTextPassword.setError(passwordError);
                                    } else {
                                        togglePasswordVisibility.setVisibility(View.VISIBLE);
                                        resetError(editTextPassword, textViewPassword);
                                    }

                                } catch (JSONException | IOException e) {
                                    Toast.makeText(RegistrationActivity.this, "Error parsing error response", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("Error", "Unexpected status code: " + response.code());
                                Toast.makeText(RegistrationActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }


                    @Override
                    public void onFailure(@NonNull Call<UserDTO> call, @NonNull Throwable t) {
                        Log.e("Error", "Registration failed.");
                        Toast.makeText(RegistrationActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        textViewLogin.setOnClickListener(view -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void setError(EditText editText, TextView textView) {
        editText.setBackgroundResource(R.drawable.edit_text_error);
        textView.setTextColor(ContextCompat.getColor(this, R.color.error));
    }

    private void resetError(EditText editText, TextView textView) {
        editText.setBackgroundResource(R.drawable.edit_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.primaryDarkColor));
    }
    
}