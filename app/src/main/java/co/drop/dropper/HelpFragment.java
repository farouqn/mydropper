package co.drop.dropper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class HelpFragment extends Fragment implements View.OnClickListener{
    public static HelpFragment newInstance(){
        HelpFragment fragment = new HelpFragment();
        return fragment;
    }

    private ImageView backHelp;
    private RelativeLayout relHelp;
    private ImageView imagePhone;
    private ImageView imageEmail;
    private TextView dropifyPhone;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backHelp = view.findViewById(R.id.backHelp);
        backHelp.setOnClickListener(this);

        dropifyPhone = view.findViewById(R.id.dropifyPhone);

        relHelp = view.findViewById(R.id.relHelp);

        imageEmail = view.findViewById(R.id.imageEmail);
        imageEmail.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

            openGmail();
            }
    private void openGmail(){

    final Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setData(Uri.parse("delivery.dropify@gmail.com"))
            .setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
            startActivity(intent);
            }
            });

            imagePhone = view.findViewById(R.id.imagePhone);

            imagePhone.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

            dialContactPhone();

            }

    private void dialContactPhone() {

            startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", dropifyPhone.getText().toString(), null)));

            }
            });
            }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_help, container,false);

            }

    @Override
    public void onClick(View view) {

            if(view == backHelp){

            relHelp.setVisibility(View.GONE);

            }

            }
            }