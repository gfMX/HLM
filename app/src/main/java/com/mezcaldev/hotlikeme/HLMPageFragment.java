package com.mezcaldev.hotlikeme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class HLMPageFragment extends Fragment {
    int tolerancePixels = 50;

    String userKey = HLMSlidePagerActivity.userKey;

    TextView viewUserAlias;
    ImageView viewUserImage;
    ImageView dropZone1;
    ImageView dropZone2;
    ImageView dropZoneLeft;
    ImageView dropZoneRight;
    DisplayMetrics metrics = new DisplayMetrics();
    int displayHeight;
    boolean flagOne = false;
    boolean flagTwo = false;

    TextView viewUserDescription;
    Toast toastImage;
    String DEBUG_TAG = "Debug: ";


    //Firebase Initialization
    final FirebaseUser firebaseUser = MainActivityFragment.user;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final FirebaseStorage storage = FirebaseStorage.getInstance();
    final StorageReference storageRef = storage.getReferenceFromUrl("gs://project-6344486298585531617.appspot.com");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.hlm_screen_slide_page, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState){

        viewUserAlias = (TextView) view.findViewById(R.id.textView);
        viewUserImage = (ImageView) view.findViewById(R.id.imageView);
        viewUserDescription = (TextView) view.findViewById(R.id.userDescription);

        dropZone1 = (ImageView) view.findViewById(R.id.dropZone1);
        dropZone2 = (ImageView) view.findViewById(R.id.dropZone2);
        dropZoneLeft = (ImageView) view.findViewById(R.id.dropZoneLeft);
        dropZoneRight = (ImageView) view.findViewById(R.id.dropZoneRight);

        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        displayHeight = metrics.heightPixels;

        viewUserImage.setRotation(5 * ((float) Math.random() * 2 - 1));
        viewUserImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                //int xRaw = (int) event.getRawX();
                int yRaw = (int) event.getRawY();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        System.out.println("Down");

                        break;
                    case MotionEvent.ACTION_UP:
                        System.out.println("Up");
                        v.setTranslationY(0);
                        v.setTranslationX(0);
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        System.out.println("Pointer Down");

                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        System.out.println("Pointer Up");

                        break;
                    case MotionEvent.ACTION_MOVE:
                        //System.out.println("Move: " + y + " Display height: " + displayHeight/2);
                        v.setX((v.getX() - v.getWidth() / 2) + x);
                        v.setY((v.getY() - v.getHeight() / 2) + y);
                        if (yRaw < (displayHeight/2 - tolerancePixels) && !flagOne){
                            Toast.makeText(getContext(), "I Like it!", Toast.LENGTH_SHORT).show();
                            flagOne = true;
                            flagTwo = false;
                        }
                        if (yRaw > (displayHeight/2 + tolerancePixels) && !flagTwo){
                            Toast.makeText(getContext(), "Nop!", Toast.LENGTH_SHORT).show();
                            flagOne = false;
                            flagTwo = true;
                        }
                        break;
                }
                return true;
            }
        });

        getUserDetails(userKey);
    }

    private void getUserDetails (String userKey){

        DatabaseReference databaseReference = database.getReference().child("users").child(userKey).child("preferences");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userData = dataSnapshot.getValue().toString();
                System.out.println("User data: " + userData);

                viewUserAlias.setText(dataSnapshot.child("alias").getValue().toString());
                viewUserDescription.setText(dataSnapshot.child("description").getValue().toString());

                //for (DataSnapshot data: dataSnapshot.getChildren()){}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        storageRef.child(userKey).child("/profile_pic/").child("profile_im.jpg").getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        viewUserImage.setImageBitmap(image);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                exception.printStackTrace();
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        viewUserImage.setImageBitmap(null);
        viewUserImage.destroyDrawingCache();
    }
}

