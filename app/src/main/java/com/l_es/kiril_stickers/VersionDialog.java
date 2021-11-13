package com.l_es.kiril_stickers;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.res.ResourcesCompat;

import java.util.Objects;

/**
 * Created by Lidor on 2/25/2021.
 * Developer name: L-ES
 * _        _   _____     ____    ______
 * | |      |_| |  __ \   / __ \  |  O   |
 * | |      | | | |  | | | |  | | |   ___/
 * | |____  | | | |__| | | |__| | | | \
 * |______| |_| |_____/   \____/  |_|__\
 * ____         ____
 * |  __|       |  __|
 * |  __|   _   |__  |
 * |____|  |_|  |____|
 */
public class VersionDialog extends AppCompatDialogFragment implements View.OnClickListener {

    private TextView textViewHeadline;
    private Button btnUpdateVersion, btnRemindMeLater, btnSkipVersion;
    private LinearLayout container;
    private VersionDialogListener listener;
    private boolean validation;

    public VersionDialog(boolean _validation) {
        this.validation = _validation;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_app_version_dialog, null);

        builder.setView(view);

        textViewHeadline = view.findViewById(R.id.textViewHeadline);
        btnUpdateVersion = view.findViewById(R.id.btnUpdateVersion);
        btnRemindMeLater = view.findViewById(R.id.btnRemindMeLater);
        btnSkipVersion = view.findViewById(R.id.btnSkipVersion);
        container = view.findViewById(R.id.dialog_outer_container);

        // Fonts
        Context context = Objects.requireNonNull(getContext());
        Typeface typefaceRegular = ResourcesCompat.getFont(context, R.font.aduma_regular);
        Typeface typefaceBold = ResourcesCompat.getFont(context, R.font.aduma_bold);
        textViewHeadline.setTypeface(typefaceBold);
        btnUpdateVersion.setTypeface(typefaceRegular);
        btnRemindMeLater.setTypeface(typefaceRegular);
        btnSkipVersion.setTypeface(typefaceRegular);

        container.setSoundEffectsEnabled(false);

        btnUpdateVersion.setOnClickListener(this);
        if(validation){
            btnRemindMeLater.setEnabled(false);
            btnSkipVersion.setEnabled(false);
            btnRemindMeLater.setTextColor(getResources().getColor(R.color.colorWhite));
            btnSkipVersion.setTextColor(getResources().getColor(R.color.colorWhite));
            container.setEnabled(false);
        }else{
            btnRemindMeLater.setOnClickListener(this);
            btnSkipVersion.setOnClickListener(this);
            container.setOnClickListener(this);
        }

        AlertDialog dialog = builder.create();

        if(validation){
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }

    @Override
    public void onClick(View view) {
        int _decision = -2;
        switch (view.getId()) {
            case R.id.btnUpdateVersion:
                _decision = 1;
                break;
            case R.id.btnRemindMeLater:
                _decision = 0;
                break;
            case R.id.btnSkipVersion:
                _decision = -1;
                break;
            case R.id.dialog_outer_container:
                dismiss();
                break;
        }
        if(_decision == -2){ return; }
        listener.applyVersionDecision(_decision);
        dismiss();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (VersionDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Must implement VersionDialogListener");
        }
    }

    public interface VersionDialogListener {
        void applyVersionDecision(int decision);
    }

}


































