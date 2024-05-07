package co.drop.dropper;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class TypeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button typeLog;
    private Button typeSU;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        typeSU = findViewById(R.id.typeSU);
        typeSU.setOnClickListener(this);

        typeLog = findViewById(R.id.typeLog);
        typeLog.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

    }

    @Override
    public void onClick(View view) {

        if(view == typeSU){
            startActivity(new Intent(getApplicationContext(),SignupActivity.class));

        }

        if(view == typeLog){
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }
    }
}
