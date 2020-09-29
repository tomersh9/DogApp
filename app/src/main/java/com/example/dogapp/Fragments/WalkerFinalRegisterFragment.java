package com.example.dogapp.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class WalkerFinalRegisterFragment extends Fragment {

    // Activity requests
    private final int CAMERA_REQUEST = 1;
    private final int WRITE_PERMISSION_REQUEST = 2;
    private final int SELECT_IMAGE = 3;
    private boolean isFromCamera;
    private boolean permission = true;

    private Uri fileUri;
    private AlertDialog alertDialog;
    private Bitmap bitmap1, bitmap2;

    private CircleImageView profileBtn;
    private TextView pressTv;

    private String fullName, email, password, location, dateOfBirth;
    private Integer gender, age;
    private Boolean type;
    private boolean isValid;

    private TextInputLayout aboutEt;
    private TextInputLayout rangeEt;
    private TextInputLayout sizeEt;
    private TextInputLayout lastCallEt;
    private TextInputLayout paymentEt;
    private TextInputLayout expEt;


    private Integer payPerWalk;
    private Boolean lastCall;
    private Integer kmRange;
    private String rangeResult;
    private String experience;
    private List<String> dogSizeList = new ArrayList<>(); //display on edit text
    private List<Integer> dogValueList = new ArrayList<>(); //data to firebase list
    private boolean[] dogSizeChoices = {false, false, false, false, false};
    //private String dogSizeString;
    //private int[] dogSizeIntArr = {-1, -1, -1, -1, -1};


    public static WalkerFinalRegisterFragment newInstance(String fullName, String email, String password, String date, Integer age, Integer gender, Boolean title, String location) {
        WalkerFinalRegisterFragment fragment = new WalkerFinalRegisterFragment();
        Bundle bundle = new Bundle();
        bundle.putString("fullName", fullName);
        bundle.putString("email", email);
        bundle.putString("password", password);
        bundle.putString("date", date);
        bundle.putInt("gender", gender);
        bundle.putInt("age", age);
        bundle.putBoolean("type", title);
        bundle.putString("location", location);
        fragment.setArguments(bundle);
        return fragment;
    }

    public interface MyFinalWalkerFragmentListener {

        void onWalkerRegisterClick(String name, String email, String password, String date, Integer age, Integer gender, Boolean title, String location,
                                   String aboutMe, String exp, Integer kmRange, List<Integer> dogSizeList, Boolean lastCall, Integer payPerWalk);

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
        gender = getArguments().getInt("gender");
        age = getArguments().getInt("age");
        type = getArguments().getBoolean("type");
        location = getArguments().getString("location");
        dateOfBirth = getArguments().getString("dateOfBirth");

        //assign views
        ImageButton backBtn = rootView.findViewById(R.id.back_frag_btn_3);
        Button regBtn = rootView.findViewById(R.id.reg_3_btn);
        aboutEt = rootView.findViewById(R.id.about_me_et);
        expEt = rootView.findViewById(R.id.exp_et);
        rangeEt = rootView.findViewById(R.id.range_km_et);
        sizeEt = rootView.findViewById(R.id.dog_size_et);
        lastCallEt = rootView.findViewById(R.id.last_call_et);
        paymentEt = rootView.findViewById(R.id.payment_et);
        pressTv = rootView.findViewById(R.id.press_tv_2);
        profileBtn = rootView.findViewById(R.id.profile_btn_2);

        //back arrow
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBackThird();
            }
        });

        //profile image event listener
        setProfileViewsListener();

        expEt.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Years of experience of dog walking")
                        .setCancelable(false)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (experience != null) {
                                    expEt.getEditText().setText(experience);
                                }
                            }
                        }).setSingleChoiceItems(R.array.exp_years_array, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] arr = getActivity().getResources().getStringArray(R.array.exp_years_array);
                        experience = arr[which];
                    }
                }).show();
            }
        });

        //handling user inputs
        sizeEt.getEditText().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.choose_dog_size)
                        .setCancelable(false)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                /*StringBuffer sb = new StringBuffer();

                                for (int i = 0; i < 5; i++) {
                                    if (dogSizeIntArr[i] != -1) {
                                        sb.append(dogSizeIntArr[i] + ",");
                                    }
                                }

                                if (sb.length() == 0) {
                                    sizeEt.getEditText().setText("");
                                    dogSizeString = null;
                                } else {
                                    dogSizeString = sb.toString();
                                    dogSizeString = dogSizeString.substring(0, dogSizeString.length() - 1); //remove ,
                                    sizeEt.getEditText().setText(dogSizeList.toString());
                                    Toast.makeText(getActivity(), dogSizeString + "", Toast.LENGTH_SHORT).show();
                                }*/

                                if(dogValueList.isEmpty()) {
                                    sizeEt.getEditText().setText("");
                                } else {
                                    //Collections.sort(dogSizeList);
                                    Collections.sort(dogValueList);
                                    //Collections.reverse(dogValueList); //small to big
                                    sizeEt.getEditText().setText(dogSizeList.toString());
                                }

                            }
                        })
                        .setMultiChoiceItems(R.array.dog_sizes_array, dogSizeChoices, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                String[] sizeArr = getActivity().getResources().getStringArray(R.array.dog_sizes_array);
                                if(isChecked) {
                                    dogValueList.add((Integer) which); //actual data to store
                                    dogSizeChoices[which] = true; //remember choices
                                    dogSizeList.add(sizeArr[which]); //display only
                                } else {
                                    dogValueList.remove((Integer) which); //actual data to store
                                    dogSizeChoices[which] = false; //remember choices
                                    dogSizeList.remove(sizeArr[which]); //for display only
                                }
                                /*String[] sizeArr = getActivity().getResources().getStringArray(R.array.dog_sizes_array);
                                if (isChecked) {
                                    dogSizeIntArr[which] = which;
                                    dogSizeList.add(sizeArr[which]); //String list of numbers
                                    dogSizeChoices[which] = true;
                                } else {
                                    dogSizeList.remove(sizeArr[which]);
                                    dogSizeChoices[which] = false;
                                    dogSizeIntArr[which] = -1; //no choice
                                }*/
                            }
                        }).show();
            }
        });

        rangeEt.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] rangeStringArray = getActivity().getResources().getStringArray(R.array.km_range);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.choose_km_range)
                        .setCancelable(false)
                        .setSingleChoiceItems(R.array.km_range, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int[] kmArr = getActivity().getResources().getIntArray(R.array.km_range_values);
                                kmRange = kmArr[which]; //save actual data
                                rangeResult = rangeStringArray[which];
                            }
                        }).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (rangeResult != null) {
                            rangeEt.getEditText().setText(rangeResult);
                        }
                    }
                }).show();
            }
        });

        lastCallEt.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.allow_last_call)
                        .setCancelable(false)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (lastCall) {
                                    lastCallEt.getEditText().setText(R.string.yes);
                                } else {
                                    lastCallEt.getEditText().setText(R.string.no);
                                }
                            }
                        }).setSingleChoiceItems(R.array.last_call_array, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String[] lastCallArr = getActivity().getResources().getStringArray(R.array.last_call_array);
                        if (lastCallArr[which].equals(getString(R.string.yes))) {
                            lastCall = true;
                        } else {
                            lastCall = false;
                        }
                    }
                }).show();
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String aboutMe = aboutEt.getEditText().getText().toString();
                paymentEt.clearFocus();
                aboutEt.clearFocus();

                isValid = validateFields(aboutMe, experience, kmRange, dogValueList);

                if (isValid) {

                    listener.startWalkerRegisterLoader();

                    if (bitmap1 != null) {
                        handleUpload(bitmap1);
                    } else if (bitmap2 != null) {
                        handleUpload(bitmap2);
                    } else {
                        //TODO fix register without photo
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_icon_jpg_128);
                        handleUpload(bitmap);
                    }

                    if (isFromCamera) {
                        getActivity().getContentResolver().delete(fileUri, null, null);
                    }

                    listener.onWalkerRegisterClick(fullName, email, password, dateOfBirth, age, gender, type, location,
                            aboutMe, experience, kmRange, dogValueList, lastCall, payPerWalk);
                } else {
                    return;
                }
            }
        });

        return rootView;
    }

    private boolean validateFields(String about, String exp, Integer range, List<Integer> sizeList) {
        if (!validateAboutMe(about) | !validateExperience(exp) | !validateRange(range) | !validateSizes(sizeList) | !validateLastCall() | !validatePayment()) {
            return false;
        }
        return true;
    }

    private boolean validateAboutMe(String about) {
        if (about == null || about.equals("")) {
            aboutEt.setError(getString(R.string.field_empty_error));
            return false;
        } else {
            aboutEt.setError(null);
            return true;
        }
    }

    private boolean validateExperience(String exp) {
        if (experience == null) {
            expEt.setError(getString(R.string.field_empty_error));
            return false;
        } else {
            expEt.setError(null);
            return true;
        }
    }

    private boolean validateRange(Integer range) {
        if (range == null) {
            rangeEt.setError(getString(R.string.field_empty_error));
            return false;
        } else {
            rangeEt.setError(null);
            return true;
        }
    }

    private boolean validateSizes(List<Integer> list) {
        if (list.isEmpty()) {
            sizeEt.setError(getString(R.string.field_empty_error));
            return false;
        } else {
            sizeEt.setError(null);
            return true;
        }
    }

    private boolean validateLastCall() {
        if (lastCallEt.getEditText().getText().toString().isEmpty()) {
            lastCallEt.setError(getString(R.string.field_empty_error));
            return false;
        } else {
            lastCallEt.setError(null);
            return true;
        }
    }

    private boolean validatePayment() {
        if (paymentEt.getEditText().getText().toString().isEmpty()) {
            paymentEt.setError(getString(R.string.field_empty_error));
            return false;
        } else if (Integer.parseInt(paymentEt.getEditText().getText().toString().trim()) == 0) {
            paymentEt.setError(getString(R.string.must_take_charge));
            return false;
        } else {
            paymentEt.setError(null);
            payPerWalk = Integer.parseInt(paymentEt.getEditText().getText().toString());
            return true;
        }
    }

    private void setProfileViewsListener() {
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
    }

    //location or images taken permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), R.string.no_permissions, Toast.LENGTH_SHORT).show();
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                permission = false;
            } else {
                Toast.makeText(getActivity(), R.string.permissions_granted, Toast.LENGTH_SHORT).show();
                permission = true;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                //Toast.makeText(getActivity(), fileUri.toString(), Toast.LENGTH_SHORT).show();
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
        final StorageReference storage = FirebaseStorage.getInstance().getReference().child("Profiles").child(getArguments().getString("email") + ".jpeg");

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
