package yazdaniscodelab.uberclone;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import yazdaniscodelab.uberclone.Model.User;



public class MainActivity extends AppCompatActivity {

    private Button btnSignIn,btnRegistration;


    //Firebase

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private LinearLayout rootlaout;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                        .setDefaultFontPath("fonts/Arkhip_font.ttf")
                                        .setFontAttrId(R.attr.fontPath)
                                        .build()
        );

        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("On Demand Service Apps");

        auth=FirebaseAuth.getInstance();

        db=FirebaseDatabase.getInstance();

        users=db.getReference("Users");

        btnSignIn=findViewById(R.id.signin_id);
        btnRegistration=findViewById(R.id.registration_id);
        rootlaout=findViewById(R.id.rootlayout);


        //Event

        btnRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegistrationDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });


    }

    private void showRegistrationDialog() {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Registraion");
        builder.setMessage("Please Use Email For Registration");

        LayoutInflater inflater=LayoutInflater.from(this);

        View myview=inflater.inflate(R.layout.registration,null);

        final MaterialEditText edtEmail=myview.findViewById(R.id.emailId);
        final MaterialEditText edtPass=myview.findViewById(R.id.textpassword);
        final MaterialEditText edtName=myview.findViewById(R.id.nameId);
        final MaterialEditText edtPhone=myview.findViewById(R.id.phoneId);

        builder.setView(myview);

        builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if (TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(rootlaout,"Please Enter Email",Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(edtPass.getText().toString())){
                    Snackbar.make(rootlaout,"Please Enter Password",Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(edtName.getText().toString())){
                    Snackbar.make(rootlaout,"Please Enter Name",Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
                if (TextUtils.isEmpty(edtPhone.getText().toString())){
                    Snackbar.make(rootlaout,"Please Enter Phone",Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }


                if (edtPass.getText().toString().length()<6){

                    Snackbar.make(rootlaout,"Password Too Short",Snackbar.LENGTH_LONG)
                            .show();

                }

                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        User user=new User();

                        user.setEmail(edtEmail.getText().toString());
                        user.setName(edtName.getText().toString());
                        user.setPass(edtPass.getText().toString());
                        user.setPhone(edtPhone.getText().toString());

                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Snackbar.make(rootlaout,"Registration Successfully Done",Snackbar.LENGTH_LONG).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootlaout,"Registration Failled",Snackbar.LENGTH_LONG).show();

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootlaout,"Registration Failled",Snackbar.LENGTH_LONG).show();
                    }
                });

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();

    }

    private void showLoginDialog(){


        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Login");
        builder.setMessage("Please Use Email For Registration");

        LayoutInflater inflater=LayoutInflater.from(this);

        View login_view=inflater.inflate(R.layout.login,null);

        final MaterialEditText edtEmail=login_view.findViewById(R.id.emailId);
        final MaterialEditText edtPass=login_view.findViewById(R.id.textpassword);

        builder.setView(login_view);

        builder.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                btnSignIn.setEnabled(false);


                if (TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(rootlaout,"Please Enter Email",Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(edtPass.getText().toString())){
                    Snackbar.make(rootlaout,"Please Enter Password",Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (edtPass.getText().toString().length()<6){

                    Snackbar.make(rootlaout,"Password Too Short",Snackbar.LENGTH_LONG)
                            .show();

                }

                final SpotsDialog waitingdialog= new SpotsDialog(MainActivity.this);
                waitingdialog.show();


                //Login Function...

                auth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingdialog.dismiss();
                                startActivity(new Intent(getApplicationContext(),WelcomeActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingdialog.dismiss();
                        Snackbar.make(rootlaout,"Filed"+e.getMessage(),Snackbar.LENGTH_LONG).show();
                        btnSignIn.setEnabled(true);

                    }
                });

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }


}
