package com.l_es.kiril_stickers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

public class StickerPackListAdapter extends RecyclerView.Adapter<StickerPackListItemViewHolder> {

    @NonNull
    private List<StickerPack> stickerPacks;
    @NonNull
    private final OnAddButtonClickedListener onAddButtonClickedListener;
    private int maxNumberOfStickersInARow;
    private int minMarginBetweenImages;
    private boolean toAnimate;

    StickerPackListAdapter(@NonNull List<StickerPack> stickerPacks, @NonNull OnAddButtonClickedListener onAddButtonClickedListener, boolean state) {
        this.stickerPacks = stickerPacks;
        this.onAddButtonClickedListener = onAddButtonClickedListener;
        this.toAnimate = state;
    }

    @Override
    public int getItemViewType(int position) { return position; }

    @NonNull
    @Override
    public StickerPackListItemViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int position) {
        final Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        View stickerPackRow = layoutInflater.inflate(R.layout.sticker_packs_list_item, viewGroup, false);
        return new StickerPackListItemViewHolder(stickerPackRow);
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerPackListItemViewHolder viewHolder, final int index) {
        StickerPack pack = stickerPacks.get(index);
        final Context context = viewHolder.publisherView.getContext();
        viewHolder.publisherView.setText(pack.publisher);
        viewHolder.filesizeView.setText(Formatter.formatShortFileSize(context, pack.getTotalSize()));

        viewHolder.titleView.setText(pack.name);
        viewHolder.container.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), StickerPackDetailsActivity.class);
            intent.putExtra(Utilities.EXTRA_SHOW_UP_BUTTON, true);
            intent.putExtra(Utilities.EXTRA_STICKER_PACK_DATA, pack);
            view.getContext().startActivity(intent);
        });
        // For Statistics
        String currentReleaseDate       =   "" + pack.releaseDate;
        String currentPackLikes         =   "" + pack.likes;
        String currentPackDownloads     =   "" + pack.downloads;
        viewHolder.textViewReleaseDate.setText(currentReleaseDate);
        viewHolder.textViewLikes.setText(currentPackLikes);
        viewHolder.textViewDownloads.setText(currentPackDownloads);

        // Fonts
        Typeface typefaceRegular = ResourcesCompat.getFont(context, R.font.aduma_regular);
        Typeface typefaceLight = ResourcesCompat.getFont(context, R.font.aduma_light);
        Typeface typefaceBold = ResourcesCompat.getFont(context, R.font.aduma_bold);
        viewHolder.titleView.setTypeface(typefaceBold);
        viewHolder.publisherView.setTypeface(typefaceRegular);
        viewHolder.filesizeView.setTypeface(typefaceRegular);
        viewHolder.textViewLikes.setTypeface(typefaceLight);
        viewHolder.textViewDownloads.setTypeface(typefaceLight);
        viewHolder.textViewReleaseDate.setTypeface(typefaceLight);

        // Animations
        if(toAnimate){
            viewHolder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_scale_animation));
        }

        viewHolder.imageRowView.removeAllViews();
        //if this sticker pack contains less stickers than the max, then take the smaller size.
        int actualNumberOfStickersToShow = Math.min(maxNumberOfStickersInARow, pack.getStickers().size());
        for (int i = 0; i < actualNumberOfStickersToShow; i++) {
            final SimpleDraweeView rowImage = (SimpleDraweeView) LayoutInflater.from(context).inflate(R.layout.sticker_packs_list_image_item, viewHolder.imageRowView, false);
            rowImage.setImageURI(StickerPackLoader.getStickerAssetUri(pack.identifier, pack.getStickers().get(i).imageFileName));
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rowImage.getLayoutParams();
            final int marginBetweenImages = minMarginBetweenImages - lp.leftMargin - lp.rightMargin;
            if (i != actualNumberOfStickersToShow - 1 && marginBetweenImages > 0) { //do not set the margin for the last image
                lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin + marginBetweenImages, lp.bottomMargin);
                rowImage.setLayoutParams(lp);
            }
            viewHolder.imageRowView.addView(rowImage);
        }
        setAddButtonAppearance(viewHolder.addButton, pack);
    }

    private void setAddButtonAppearance(ImageView addButton, StickerPack pack) {
        if (pack.getIsWhitelisted()) {
            addButton.setImageResource(R.drawable.sticker_3rdparty_added);
            addButton.setClickable(false);
            addButton.setOnClickListener(null);
            setBackground(addButton, null);
        } else {
            addButton.setImageResource(R.drawable.sticker_3rdparty_add);
            addButton.setOnClickListener(view -> onAddButtonClickedListener.onAddButtonClicked(pack));
            TypedValue outValue = new TypedValue();
            addButton.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            addButton.setBackgroundResource(outValue.resourceId);
        }
    }

    private void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }

    @Override
    public int getItemCount() {
        return stickerPacks.size();
    }

    void setImageRowSpec(int maxNumberOfStickersInARow, int minMarginBetweenImages) {
        this.minMarginBetweenImages = minMarginBetweenImages;
        if (this.maxNumberOfStickersInARow != maxNumberOfStickersInARow) {
            this.maxNumberOfStickersInARow = maxNumberOfStickersInARow;
            notifyDataSetChanged();
        }
    }

    void setStickerPackList(List<StickerPack> stickerPackList) {
        this.stickerPacks = stickerPackList;
    }

    public interface OnAddButtonClickedListener {
        void onAddButtonClicked(StickerPack stickerPack);
    }
}
