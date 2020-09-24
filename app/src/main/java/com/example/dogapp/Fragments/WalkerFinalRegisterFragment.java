package com.example.dogapp.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.dogapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class WalkerFinalRegisterFragment extends Fragment {

    // Activity requests
    final int CAMERA_REQUEST = 1;
    final int WRITE_PERMISSION_REQUEST = 2;
    final int SELECT_IMAGE = 3;
    final int LOCATION_PERMISSION_REQUEST = 4;
    boolean isFromCamera;
    boolean permission = true;

    Uri fileUri;
    AlertDialog alertDialog;
    Bitmap bitmap1, bitmap2;

    CircleImageView profileBtn;
    TextView pressTv;

    // Firebase storage
    StorageReference myStorageRef;

    private String fullName, email, password, gender, location, type, dateOfBirth;

    public static WalkerFinalRegisterFragment newInstance(String fullName, String email, String password, String date, String gender, String title, String location) {
        WalkerFinalRegisterFragment fragment = new WalkerFinalRegisterFragment();
        Bundle bundle = new Bundle();
        bundle.putString("fullName", fullName);
        bundle.putString("email", email);
        bundle.putString("password", password);
        bundle.putString("date", date);
        bundle.putString("gender", gender);
        bundle.putString("type", title);
        bundle.putString("location", location);
        fragment.setArguments(bundle);
        return fragment;
    }

    public interface MyFinalWalkerFragmentListener {

        void onWalkerRegisterClick(String name, String email, String password, String date, String gender, String title, String location);

        void startWalkerRegisterLoader();

        void stopWalkerRegisterLoader();

        void createWalkerConfirmDialog();

        void onBackThird();
    }

    private MyFinalWalkerFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (MyFinalWalkerFragmentListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("Login activity must implement MyFinalWalkerFragmentListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.register_fragment_layout_3, container, false);

        //get previous fields
        fullName = getArguments().getString("fullName");
        email = getArguments().getString("email");
        password = getArguments().getString("password");
        gender = getArguments().getString("gender");
        type = getArguments().getString("type");
        location = getArguments().getString("location");
        dateOfBirth = getArguments().getString("dateOfBirth");


        pressTv = rootView.findViewById(R.id.press_tv);
        profileBtn = rootView.findViewById(R.id.profile_btn);
        profileBtn.animate().scaleX(1.3f).scaleY(1.3f).setDuration(500).withEndAction(new Runnable() {
            @Override
            public void run() {
                profileBtn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(500).start();
            }
        }).start();
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View dialogView;
                final AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                dialogView = getLayoutInflater().inflate(R.layout.camera_dialog, null);
                alertDialog = builder1.setView(dialogView).show();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                ImageButton camBtn = dialogView.findViewById(R.id.cam_dialog_btn);
                ImageButton galleryBtn = dialogView.findViewById(R.id.gallery_dialog_btn);

                camBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, "Picture");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "from");
                        fileUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        startActivityForResult(intent, CAMERA_REQUEST);
                    }
                });

                galleryBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent, SELECT_IMAGE);
                    }
                });

                //check permissions
                if (Build.VERSION.SDK_INT >= 23) {
                    int hasWritePermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    int hasReadPermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (hasWritePermission != PackageManager.PERMISSION_GRANTED && hasReadPermission != PackageManager.PERMISSION_GRANTED)
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
                }
            }
        });

        Button regBtn = rootView.findViewById(R.id.reg_3_btn);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listener.startWalkerRegisterLoader();

                if (bitmap1 != null)
                    handleUpload(bitmap1);
                else if (bitmap2 != null)
                    handleUpload(bitmap2);

                if (isFromCamera) {
                    getActivity().getContentResolver().delete(fileUri, null, null);
                }

                listener.onWalkerRegisterClick(fullName,email,password,dateOfBirth,gender,type,location);
            }
        });

        return rootView;
    }

    //location or images taken permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "No permissions", Toast.LENGTH_SHORT).show();
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                permission = false;
            } else {
                Toast.makeText(getActivity(), "Permission granted", Toast.LENGTH_SHORT).show();
                permission = true;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getActivity(), fileUri.toString(), Toast.LENGTH_SHORT).show();
                bitmap2 = null;
                try {
                    bitmap1 = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getActivity().getContentResolver(), fileUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                profileBtn.setImageBitmap(bitmap1);
                //getActivity().getContentResolver().delete(fileUri,null, null); // have to transfer to register button
                pressTv.setVisibility(View.GONE);
                isFromCamera = true;
                alertDialog.dismiss();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
                fileUri = null;
            }

        }

        if (requestCode == SELECT_IMAGE && permission == true) {
            if (resultCode == Activity.RESULT_OK) {
                fileUri = data.getData();
                bitmap1 = null;
                try {
                    bitmap2 = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getActivity().getContentResolver(), fileUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                profileBtn.setImageBitmap(bitmap2);
                pressTv.setVisibility(View.GONE);
                isFromCamera = false;
                if (isFromCamera)
                    getActivity().getContentResolver().delete(fileUri, null, null);
                alertDialog.dismiss();
            }
        }
    }

    private void handleUpload(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final StorageReference storage = FirebaseStorage.getInstance().getReference().child("Images").child(getArguments().getString("email") + ".jpeg");

        storage.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(storage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void getDownloadUrl(StorageReference storage) {
        storage.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        setUserProfileUrl(uri);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void setUserProfileUrl(Uri uri) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        if (user != null) {
            user.updateProfile(request)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            listener.stopWalkerRegisterLoader();
                            listener.createWalkerConfirmDialog();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            listener.stopWalkerRegisterLoader();
                        }
                    });
        }
    }
}
