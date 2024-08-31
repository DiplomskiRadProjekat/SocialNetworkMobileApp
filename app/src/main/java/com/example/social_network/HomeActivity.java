package com.example.social_network;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.social_network.dtos.NewPostDTO;
import com.example.social_network.dtos.PostDTO;
import com.example.social_network.fragments.PostsFragment;
import com.example.social_network.fragments.SearchResultsFragment;
import com.example.social_network.services.ServiceUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 100;

    private EditText editTextPost, editTextSearch;

    private TextView textViewAddImage, textViewSelectedImage;

    private Button buttonCreatePost;

    private ImageButton buttonSearch, imageButtonClear;

    private ImageView imageViewProfilePicture;

    private String token;

    private Long myId;

    private BottomNavigationView bottomNavigationView;

    private final List<Uri> selectedImageUris = new ArrayList<>();

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        selectedImageUris.clear();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            selectedImageUris.add(imageUri);
                        }
                        updateSelectedImagesUI();
                    } else if (result.getData().getData() != null) {
                        selectedImageUris.clear();
                        selectedImageUris.add(result.getData().getData());
                        updateSelectedImagesUI();
                    }
                }
            }
    );

    private void updateSelectedImagesUI() {
        if (selectedImageUris.isEmpty()) {
            textViewSelectedImage.setVisibility(View.GONE);
            imageButtonClear.setVisibility(View.GONE);
            textViewAddImage.setVisibility(View.VISIBLE);
        } else {
            textViewAddImage.setVisibility(View.GONE);
            textViewSelectedImage.setVisibility(View.VISIBLE);
            textViewSelectedImage.setText(String.format("%s selected images", selectedImageUris.size()));
            imageButtonClear.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_home);

        editTextPost = findViewById(R.id.postDescription);
        editTextSearch = findViewById(R.id.search);
        textViewAddImage = findViewById(R.id.addImage);
        buttonCreatePost = findViewById(R.id.createPost);
        textViewSelectedImage = findViewById(R.id.selectedImage);
        buttonSearch = findViewById(R.id.searchButton);
        imageViewProfilePicture = findViewById(R.id.profile_image);
        imageButtonClear = findViewById(R.id.clear);

        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("pref_token", "");
        myId = sharedPreferences.getLong("pref_id", 0);

        if (savedInstanceState == null) {
            PostsFragment fragment = new PostsFragment(true, myId, myId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        setupBottomNavigationListener();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadProfilePicture(myId);

        textViewAddImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_READ_EXTERNAL_STORAGE);
            } else {
                pickImagesFromGallery();
            }
        });

        imageButtonClear.setOnClickListener(view -> {
            selectedImageUris.clear();
            textViewSelectedImage.setVisibility(View.GONE);
            imageButtonClear.setVisibility(View.GONE);
            textViewAddImage.setVisibility(View.VISIBLE);
        });

        buttonCreatePost.setOnClickListener(v -> createPost());

        buttonSearch.setOnClickListener(view -> {
            String search = editTextSearch.getText().toString();
            SearchResultsFragment fragment = new SearchResultsFragment(search);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.searchResultsContainer, fragment);
            transaction.commit();
        });

    }

    private void pickImagesFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickImageLauncher.launch(intent);
    }

    private void createPost() {
        String description = editTextPost.getText().toString();

        boolean hasError = false;

        if (description.isEmpty()) {
            Log.e("Error", "Empty username");
            setError(editTextPost);
            hasError = true;
        } else {
            resetError(editTextPost);
        }

        if (!hasError) {
            NewPostDTO newPost = new NewPostDTO(description);
            Call<PostDTO> call = ServiceUtils.userService(token).createPost(myId, newPost);
            call.enqueue(new Callback<PostDTO>() {
                @Override
                public void onResponse(@NonNull Call<PostDTO> call, @NonNull Response<PostDTO> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(HomeActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                        PostDTO createdPost = response.body();
                        if (createdPost != null) {
                            Long postId = createdPost.getId();
                            editTextPost.setText("");
                            textViewSelectedImage.setVisibility(View.GONE);
                            imageButtonClear.setVisibility(View.GONE);
                            textViewAddImage.setVisibility(View.VISIBLE);

                            if (!selectedImageUris.isEmpty()) {
                                uploadImagesToPost(postId, selectedImageUris);
                            }
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, "Failed to create post", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PostDTO> call, @NonNull Throwable t) {
                    Toast.makeText(HomeActivity.this, "Failed to create post: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadImagesToPost(Long postId, List<Uri> imageUris) {
        for (Uri uri : imageUris) {
            String filePath = getPathFromUri(uri);
            if (filePath == null) {
                Toast.makeText(this, "Upload failed for : " + uri.toString(), Toast.LENGTH_SHORT).show();
                continue;
            }

            File file = new File(filePath);
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileBody);

            Call<Void> call = ServiceUtils.postService(token).setImageToPost(postId, filePart);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.i("ImageUpload", "Image successfully added to post with id: " + postId);
                    } else {
                        Log.e("ImageUpload", "Image upload failed for post with id: " + postId);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Log.e("ImageUpload", "Image upload failed for post: " + t.getMessage());
                }
            });
        }
    }


    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImagesFromGallery();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupBottomNavigationListener() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_friend_requests) {
                Intent intent = new Intent(HomeActivity.this, FriendRequestsActivity.class);
                intent.putExtra("userId", myId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(HomeActivity.this, InboxActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
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

    private void showSettingsMenu() {
        View view = findViewById(R.id.nav_settings);
        Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.getMenuInflater().inflate(R.menu.settings_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_profile) {
                Intent intent = new Intent(HomeActivity.this, EditProfileActivity.class);
                intent.putExtra("userId", myId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_delete_profile) {
                showAreYouSureDialog();
                return true;
            } else if (itemId == R.id.action_logout) {
                deletePreferences();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    Toast.makeText(HomeActivity.this, "Successfully deleted profile!", Toast.LENGTH_SHORT).show();
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

    private void setError(EditText editText) {
        editText.setBackgroundResource(R.drawable.edit_text_error);
    }

    private void resetError(EditText editText) {
        editText.setBackgroundResource(R.drawable.edit_text);
    }

    private void loadProfilePicture(Long id) {
        Call<ResponseBody> call = ServiceUtils.userService(token).downloadProfilePicture(id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    assert response.body() != null;
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        ImageViewCompat.setImageTintList(imageViewProfilePicture, null);
                        imageViewProfilePicture.setImageBitmap(bitmap);
                    } else {
                        Log.e("LoadImage", "Failed to decode bitmap from stream");
                    }

                } else {
                    onFailure(call, new Throwable("API call failed with status code: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
                Log.d("Fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }


}
