package com.l_es.kiril_stickers;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class StickerPackListItemViewHolder extends RecyclerView.ViewHolder {

    final View container;
    final TextView titleView, publisherView, filesizeView;
    final ImageView addButton;
    final LinearLayout imageRowView;
    // Likes, Downloads and Release Date
    final TextView textViewReleaseDate, textViewLikes, textViewDownloads;

    StickerPackListItemViewHolder(final View itemView) {
        super(itemView);
        container            =   itemView;
        titleView            =   itemView.findViewById(R.id.sticker_pack_title);
        publisherView        =   itemView.findViewById(R.id.sticker_pack_publisher);
        filesizeView         =   itemView.findViewById(R.id.sticker_pack_filesize);
        addButton            =   itemView.findViewById(R.id.add_button_on_list);
        imageRowView         =   itemView.findViewById(R.id.sticker_packs_list_item_image_list);
        textViewReleaseDate  =   itemView.findViewById(R.id.text_view_release_date);
        textViewLikes        =   itemView.findViewById(R.id.text_view_likes_count);
        textViewDownloads    =   itemView.findViewById(R.id.text_view_downloads_count);
    }
}