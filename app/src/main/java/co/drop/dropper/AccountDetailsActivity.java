package co.drop.dropper;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AccountDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText accountName;

    private EditText accounNumber;

    private Spinner accountBank;

    private Button accountSave;

    private ProgressDialog progressDialog;

    private DatabaseReference mBankDatabase;

    String userID;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        accountName = findViewById(R.id.accountName);

        accounNumber = findViewById(R.id.accountNumber);

        accountBank = findViewById(R.id.accountBank);

        accountSave = findViewById(R.id.accountSave);
        accountSave.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        userID = firebaseAuth.getInstance().getCurrentUser().getUid();

        mBankDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Carriers").child(userID);
    }

    private void saveAccount(){

        String name = accountName.getText().toString().trim();

        String accnumber= accounNumber.getText().toString().trim();

        String bank = accountBank.getSelectedItem().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Please enter your Name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(accnumber)) {
            Toast.makeText(getApplicationContext(), "Please enter your Account Number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(bank)) {
            Toast.makeText(getApplicationContext(), "Please enter your Bank name", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Saving Information");
        progressDialog.show();

        Map bankInfo = new HashMap();

        bankInfo.put("Account Name", name);
        bankInfo.put("Account Number", accnumber);
        bankInfo.put("Bank", bank);

        mBankDatabase.updateChildren(bankInfo)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), VerificationActivity.class));
                            finish();
                        }else{
                            progressDialog.dismiss();
                            finish();
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {

        if(v == accountSave){

            saveAccount();
        }
    }
}

