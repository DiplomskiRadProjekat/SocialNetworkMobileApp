package com.example.social_network;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.social_network.dtos.PasswordChangeDTO;
import com.example.social_network.dtos.UpdateUserDTO;
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.services.ServiceUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextUsername, editTextCurrentPassword, editTextNewPassword, editTextConfirmPassword;

    private Button buttonSaveChanges;

    private TextView textViewChangePassword, textViewNewPassword, textViewCurrentPassword, textViewConfirmPassword;

    private String token;

    private Long myId;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_edit_profile);

        editTextFirstName = findViewById(R.id.firstName);
        editTextLastName = findViewById(R.id.lastName);
        editTextEmail = findViewById(R.id.email);
        editTextUsername = findViewById(R.id.username);

        buttonSaveChanges = findViewById(R.id.saveChanges);

        textViewChangePassword = findViewById(R.id.changePassword);

        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("pref_token", "");
        myId = sharedPreferences.getLong("pref_id", 0);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
        setupBottomNavigationListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadUser(token);

        buttonSaveChanges.setOnClickListener(view -> {
            String firstName = editTextFirstName.getText().toString();
            String lastName = editTextLastName.getText().toString();
            String username = editTextUsername.getText().toString();
            String email = editTextEmail.getText().toString();

            boolean hasError = false;

            if (firstName.isEmpty()) {
                Log.e("Error", "Empty first name");
                setError(editTextUsername);
                hasError = true;
            } else {
                resetError(editTextUsername);
            }

            if (lastName.isEmpty()) {
                Log.e("Error", "Empty last name");
                setError(editTextLastName);
                hasError = true;
            } else {
                resetError(editTextLastName);
            }

            if (username.isEmpty()) {
                Log.e("Error", "Empty username");
                setError(editTextUsername);
                hasError = true;
            } else {
                resetError(editTextUsername);
            }

            if (email.isEmpty()) {
                Log.e("Error", "Empty email");
                setError(editTextEmail);
                hasError = true;
            } else {
                resetError(editTextEmail);
            }

            if (!hasError) {
                UpdateUserDTO updateUserDTO = new UpdateUserDTO(email, firstName, lastName, username, null);
                Call<UserDTO> call = ServiceUtils.userService(token).update(myId, updateUserDTO);

                call.enqueue(new Callback<UserDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<UserDTO> call, @NonNull Response<UserDTO> response) {
                        if (response.isSuccessful()) {
                            Log.i("Success", response.message());
                            UserDTO userDTO = response.body();
                            if (userDTO != null) {
                                loadUser(token);
                                Toast.makeText(EditProfileActivity.this, "Successfully updated profile!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (response.code() == 400) {
                                try {
                                    assert response.errorBody() != null;
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
                                        setError(editTextEmail);
                                        String emailError = errors.get("email");
                                        editTextEmail.setError(emailError);
                                    } else {
                                        resetError(editTextEmail);
                                    }

                                    if (errors.containsKey("username")) {
                                        setError(editTextUsername);
                                        String usernameError = errors.get("username");
                                        editTextUsername.setError(usernameError);
                                    } else {
                                        resetError(editTextUsername);
                                    }

                                } catch (JSONException | IOException e) {
                                    Toast.makeText(EditProfileActivity.this, "Error parsing error response", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("Error", "Unexpected status code: " + response.code());
                                Toast.makeText(EditProfileActivity.this, "Password change failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserDTO> call, @NonNull Throwable t) {
                        Log.d("Fail", Objects.requireNonNull(t.getMessage()));
                    }
                });
            }
        });

        textViewChangePassword.setOnClickListener(view -> showPasswordEditDialog());
    }

    private void showPasswordEditDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_edit_password);

        editTextCurrentPassword = dialog.findViewById(R.id.currentPassword);
        editTextNewPassword = dialog.findViewById(R.id.newPassword);
        editTextConfirmPassword = dialog.findViewById(R.id.confirmPassword);

        textViewCurrentPassword = dialog.findViewById(R.id.currentPasswordLabel);
        textViewNewPassword = dialog.findViewById(R.id.newPasswordLabel);
        textViewConfirmPassword = dialog.findViewById(R.id.confirmPasswordLabel);

        Button buttonCancel = dialog.findViewById(R.id.cancelButton);
        Button buttonSave = dialog.findViewById(R.id.saveButton);

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonSave.setOnClickListener(v -> {
            String currentPassword = editTextCurrentPassword.getText().toString();
            String newPassword = editTextNewPassword.getText().toString();
            String confirmPassword = editTextConfirmPassword.getText().toString();

            boolean hasError = false;

            if (currentPassword.isEmpty()) {
                Log.e("Error", "Empty current password");
                setError(editTextCurrentPassword, textViewCurrentPassword);
                hasError = true;
            } else {
                resetError(editTextCurrentPassword, textViewCurrentPassword);
            }

            if (newPassword.isEmpty()) {
                Log.e("Error", "Empty new password");
                setError(editTextNewPassword, textViewNewPassword);
                hasError = true;
            } else {
                resetError(editTextNewPassword, textViewNewPassword);
            }

            if (confirmPassword.isEmpty()) {
                Log.e("Error", "Empty confirm password");
                setError(editTextConfirmPassword, textViewConfirmPassword);
                hasError = true;
            } else {
                resetError(editTextConfirmPassword, textViewConfirmPassword);
            }

            if (!newPassword.isEmpty() && !confirmPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
                Log.e("Error", "Passwords don't match");
                setError(editTextNewPassword, textViewNewPassword);
                setError(editTextConfirmPassword, textViewConfirmPassword);
                editTextNewPassword.setError("Passwords don't match!");
                editTextConfirmPassword.setError("Passwords don't match!");
                hasError = true;
            } else if (!hasError) {
                resetError(editTextNewPassword, textViewNewPassword);
                resetError(editTextConfirmPassword, textViewConfirmPassword);
            }

            if (!hasError) {
                PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO(currentPassword, newPassword);
                Call<Void> call = ServiceUtils.userService(token).changePassword(myId, passwordChangeDTO);

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.i("Success", response.message());
                            dialog.dismiss();
                            Toast.makeText(EditProfileActivity.this, "Successfully changed password!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (response.code() == 400) {
                                try {
                                    assert response.errorBody() != null;
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

                                    if (errors.containsKey("currentPassword")) {
                                        setError(editTextCurrentPassword, textViewCurrentPassword);
                                        String currentPasswordError = errors.get("currentPassword");
                                        editTextCurrentPassword.setError(currentPasswordError);
                                    } else {
                                        resetError(editTextCurrentPassword, textViewCurrentPassword);
                                    }

                                    if (errors.containsKey("newPassword")) {
                                        setError(editTextNewPassword, textViewNewPassword);
                                        String newPasswordError = errors.get("newPassword");
                                        editTextNewPassword.setError(newPasswordError);
                                    } else {
                                        resetError(editTextNewPassword, textViewNewPassword);
                                    }

                                } catch (JSONException | IOException e) {
                                    Toast.makeText(EditProfileActivity.this, "Error parsing error response", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("Error", "Unexpected status code: " + response.code());
                                Toast.makeText(EditProfileActivity.this, "Password change failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.d("Fail", Objects.requireNonNull(t.getMessage()));
                    }
                });
            }

        });

        dialog.show();
    }

    private void loadUser(String jwtToken) {
        Call<UserDTO> call = ServiceUtils.userService(jwtToken).get(myId);

        call.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(@NonNull Call<UserDTO> call, @NonNull Response<UserDTO> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    UserDTO userDTO = response.body();
                    if (userDTO != null) {
                        editTextFirstName.setText(userDTO.getFirstName());
                        editTextLastName.setText(userDTO.getLastName());
                        editTextUsername.setText(userDTO.getUsername());
                        editTextEmail.setText(userDTO.getEmail());
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

    private void setupBottomNavigationListener() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_friend_requests) {
                Intent intent = new Intent(EditProfileActivity.this, FriendRequestsActivity.class);
                intent.putExtra("userId", myId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(EditProfileActivity.this, InboxActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                intent.putExtra("userId", myId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_settings) {
                showSettingsMenu();
                return true;
            }
            return false;
        });
    }

    private void setError(EditText editText, TextView textView) {
        editText.setBackgroundResource(R.drawable.edit_text_error);
        textView.setTextColor(ContextCompat.getColor(this, R.color.error));
    }

    private void setError(EditText editText) {
        editText.setBackgroundResource(R.drawable.edit_text_error);
    }

    private void resetError(EditText editText, TextView textView) {
        editText.setBackgroundResource(R.drawable.edit_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.primaryDarkColor));
    }

    private void resetError(EditText editText) {
        editText.setBackgroundResource(R.drawable.edit_text);
    }

    private void showSettingsMenu() {
        View view = findViewById(R.id.nav_settings);
        Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.getMenuInflater().inflate(R.menu.settings_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_profile) {
                return true;
            } else if (itemId == R.id.action_delete_profile) {
                showAreYouSureDialog();
                return true;
            } else if (itemId == R.id.action_logout) {
                deletePreferences();
                startActivity(new Intent(EditProfileActivity.this, LoginActivity.class));
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void showAreYouSureDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_are_you_sure);

        Button buttonYes = dialog.findViewById(R.id.yesButton);
        Button buttonNo = dialog.findViewById(R.id.noButton);

        buttonNo.setOnClickListener(v -> dialog.dismiss());

        buttonYes.setOnClickListener(v -> {
            deleteProfile();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteProfile() {
        Call<Void> call = ServiceUtils.userService(token).delete(myId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    deletePreferences();
                    startActivity(new Intent(EditProfileActivity.this, LoginActivity.class));
                    Toast.makeText(EditProfileActivity.this, "Successfully deleted profile!", Toast.LENGTH_SHORT).show();
                } else {
                    onFailure(call, new Throwable("API call failed with status code: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, Throwable t) {
                Log.d("Fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void deletePreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.clear().apply();
    }

}