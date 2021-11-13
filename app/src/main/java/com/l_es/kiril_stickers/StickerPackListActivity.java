package com.l_es.kiril_stickers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StickerPackListActivity extends AddStickerPackActivity implements VersionDialog.VersionDialogListener {

    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;
    private RecyclerView packRecyclerView;
    private LinearLayoutManager packLayoutManager;
    private StickerPackListAdapter allStickerPacksListAdapter;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private ArrayList<StickerPack> stickerPackList;
    // Featured Pack
    private StickerPack packFeatured;
    private ConstraintLayout containerFeatured;
    private TextView publisherPack;
    private TextView publisherName;
    private TextView publisherSize;
    private ImageView stickerPreview;
//    private ImageView addFeaturedButton;
    // Firebase Related
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference referenceVersion = db.collection("Application Version").document("version");
    private CollectionReference collectionReference = db.collection("Stickers Packs");
    private Map<String, String> mapDates = new HashMap<>();
    private Map<String, Integer> mapDownloads = new HashMap<>();
    private Map<String, Integer> mapLikes = new HashMap<>();
    boolean state = true;
    private String featuredName = "";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String versionFromServer;
    private Task<DocumentSnapshot> versionRef;
    private ListenerRegistration collectionListenerRef;

    @Override
    protected void onStop() {
        super.onStop();
        collectionListenerRef.remove();
    }

    @Override
    protected void onStart() {
        super.onStart();
        versionRef = referenceVersion.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                versionFromServer = "" + documentSnapshot.getString("ID");
                String validation = "" + documentSnapshot.getString("validation");
                int inAppCurrentVersion = Integer.parseInt(Utilities.CURRENT_VERSION);
                int spCurrentVersion = sharedPreferences.getInt(Utilities.SHARED_PREFERENCES_VERSION, inAppCurrentVersion);
                String applicationVersion = getHigherVersion(inAppCurrentVersion, spCurrentVersion);
                if (!(versionFromServer.equals(applicationVersion))){
                    // There is a newer version on the app store!
                    if(validation.equals(Utilities.VALIDATION_MUST)){
                        openVersionUpdateDialog(true);
                    }else if(validation.equals(Utilities.VALIDATION_OPTION)){
                        openVersionUpdateDialog(false);
                    }
                }
            }
        });
        collectionListenerRef = collectionReference.addSnapshotListener((value, error) -> collectionInit(state));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_list);
        assignVariables();
        stickerPackList = getIntent().getParcelableArrayListExtra(Utilities.EXTRA_STICKER_PACK_LIST_DATA);

        sharedPreferences = getSharedPreferences(Utilities.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        applyFeaturedIndex();
        collectionInit(state);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Google AdMob
        MobileAds.initialize(this, initializationStatus -> { });
        // ******* Start - add Dev phone as a tester in development
        List<String> testDeviceIds = Arrays.asList("F44EEA553A26E309A0D097942B648FE8");
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);
        // ******* End - add Dev phone as a tester in development
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)    // Add the emulator as a test device
                .build();
        adView.loadAd(adRequest);

        /// Featured Pack
        if(!featuredName.isEmpty()){
            Log.d("FEATURE", packFeatured.name);
            publisherPack.setText(packFeatured.name);
            publisherName.setText(packFeatured.publisher);
            publisherSize.setText(Formatter.formatShortFileSize(this, packFeatured.getTotalSize()));
            containerFeatured.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), StickerPackDetailsActivity.class);
                intent.putExtra(Utilities.EXTRA_SHOW_UP_BUTTON, true);
                intent.putExtra(Utilities.EXTRA_STICKER_PACK_DATA, packFeatured);
                intent.putExtra(Utilities.IS_FEATURED, true);
                view.getContext().startActivity(intent);
            });
//            addFeaturedButton.setOnClickListener(view -> onAddButtonClickedListener.onAddButtonClicked(packFeatured));
            stickerPreview.setImageURI(StickerPackLoader.getStickerAssetUri(packFeatured.identifier, packFeatured.getStickers().get(0).imageFileName));
        }


        Typeface typefaceRegular = ResourcesCompat.getFont(this, R.font.aduma_regular);
        Typeface typefaceHeavy = ResourcesCompat.getFont(this, R.font.aduma_heavy);
        TextView textView = findViewById(R.id.sortListHeadline);
        textView.setTypeface(typefaceHeavy);
        publisherPack.setTypeface(typefaceRegular);
        publisherName.setTypeface(typefaceRegular);
        publisherSize.setTypeface(typefaceRegular);




        findViewById(R.id.btn_sortDates).setOnClickListener(v -> {
            /* Normally it's supposed to work like this:
             *   negative => first < second    ,   0 => first = second   ,   positive => first > second
             * I did it to work like this:
             *   negative => first > second    ,   0 => first = second   ,   positive => first < second
             */
            Collections.sort(stickerPackList, (pack1, pack2) -> {	// dd/mm/yyyy
                int d1 = Integer.parseInt(pack1.releaseDate.substring(0,2)),  d2 = Integer.parseInt(pack2.releaseDate.substring(0, 2));
                int m1 = Integer.parseInt(pack1.releaseDate.substring(3, 5)),  m2 = Integer.parseInt(pack2.releaseDate.substring(3, 5));
                int y1 = Integer.parseInt(pack1.releaseDate.substring(6, 10)), y2 = Integer.parseInt(pack2.releaseDate.substring(6, 10));
                if(y1 < y2) { return 1; }
                else if(y1 > y2) { return -1; }
                else {
                    if(m1 < m2) { return 1; }
                    else if(m1 > m2) { return -1; }
                    else {
                        if(d1 > d2) { return -1; }
                        else if(d1 < d2) { return 1; }
                        else { return 0; }
                    }
                }
            });
            ((MotionLayout)findViewById(R.id.motion_layout)).transitionToState(R.id.end_dup);
            waitSeconds();
        });
        findViewById(R.id.btn_sortDownloads).setOnClickListener(v -> {
            /* Normally it's supposed to work like this:
             *   negative => first < second    ,   0 => first = second   ,   positive => first > second
             * I did it to work like this:
             *   negative => first > second    ,   0 => first = second   ,   positive => first < second
             */
            Collections.sort(stickerPackList, (pack1, pack2) -> {
                int pack1Downloads = pack1.downloads;
                int pack2Downloads = pack2.downloads;
                if(pack1Downloads > pack2Downloads){ return -1; }
                else if( pack1Downloads < pack2Downloads){ return 1; }
                else{ return 0; }
            });
            ((MotionLayout)findViewById(R.id.motion_layout)).transitionToState(R.id.end_dup);
            waitSeconds();
        });
        findViewById(R.id.btn_sortLikes).setOnClickListener(v -> {
            /* Normally it's supposed to work like this:
             *   negative => first < second    ,   0 => first = second   ,   positive => first > second
             * I did it to work like this:
             *   negative => first > second    ,   0 => first = second   ,   positive => first < second
             */
            Collections.sort(stickerPackList, (pack1, pack2) -> {
                int pack1Likes = pack1.likes;
                int pack2Likes = pack2.likes;
                if(pack1Likes > pack2Likes){ return -1; }
                else if( pack1Likes < pack2Likes){ return 1; }
                else{ return 0; }
            });
            ((MotionLayout)findViewById(R.id.motion_layout)).transitionToState(R.id.end_dup);
            waitSeconds();
        });
        findViewById(R.id.settings_background).setOnClickListener(v -> ((MotionLayout)findViewById(R.id.motion_layout)).transitionToState(R.id.end_dup));

        // End Space on recycler view
        float offsetPx = getResources().getDimension(R.dimen.bottom_offset_dp);
        BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int) offsetPx);
        packRecyclerView.addItemDecoration(bottomOffsetDecoration);

    }

    private void waitSeconds() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            showStickerPackList(stickerPackList, true);
            allStickerPacksListAdapter.notifyDataSetChanged();
        }, 1500);
    }

    private void collectionInit(boolean toAnimate) {
        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mapDates.clear();
                mapDownloads.clear();
                mapLikes.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    mapDates.put(document.getId(), (document.getString(Utilities.DATE)));
                    mapDownloads.put(document.getId(), ((int)Math.round(document.getDouble(Utilities.DOWNLOADS))));
                    mapLikes.put(document.getId(), ((int)Math.round(document.getDouble(Utilities.LIKES))));
                }
                if (stickerPackList != null) {
                    Log.d("TESTING!!!!!!!!!!!!!", stickerPackList.toString());
                    packListConfigurations();
                    showStickerPackList(stickerPackList, toAnimate);
                }
            }else{
                Toast.makeText(StickerPackListActivity.this, "Some error happened while loading the statistics.", Toast.LENGTH_SHORT).show();
            }
            state = false;
        });
    }

    private void packListConfigurations() {
        // Extract Likes, Downloads and Release Date
        for(int i = 0; i < stickerPackList.size(); i++){
            String like = stickerPackList.get(i).name;
            Log.d("LIKE LIKE LIKE", like);
            stickerPackList.get(i).setStatistics(
                    mapLikes.get(stickerPackList.get(i).name),
                    mapDownloads.get(stickerPackList.get(i).name),
                    mapDates.get(stickerPackList.get(i).name)
            );
        }
    }

    private void applyFeaturedIndex() {
        featuredName = sharedPreferences.getString(Utilities.SHARED_PREFERENCES_FEATURED, "");
        if(featuredName != null){
            featuredName = featuredName.trim();
        }
        if(featuredName != null && featuredName.equals("")){ return; }
        for(int i = 0; i < stickerPackList.size(); i++){
            StickerPack stickerPack = stickerPackList.get(i);
//            String x1 = stickerPack.name;
//            String x2 = featuredName;
//            boolean isIT1 = stickerPack.name.equals(featuredName);
//            boolean isIT2 = x1.equals(x2);
            if(stickerPack.name.equals(featuredName)){
                packFeatured = stickerPackList.get(i);
                stickerPackList.remove(i);
                break;
            }
        }
    }

    private void assignVariables() {
        packRecyclerView        =   findViewById(R.id.sticker_pack_list);
        containerFeatured       =   findViewById(R.id.sticker_store_featured_row_container);
        publisherPack           =   findViewById(R.id.sticker_pack_featured_title);
        publisherName           =   findViewById(R.id.sticker_pack_featured_publisher);
        stickerPreview          =   findViewById(R.id.sticker_packs_featured_preview);
        publisherSize           =   findViewById(R.id.sticker_pack_featured_file_size);
    }

//  Check inApp and sp Versions
    private String getHigherVersion(int inApp, int sp) {
        return inApp > sp ? (inApp + "") : (sp + "");
    }

//  Dialog Related
    public void openVersionUpdateDialog(boolean must){
        VersionDialog versionDialog = new VersionDialog(must);
        versionDialog.show(getSupportFragmentManager(), "update version dialog");
    }
    @Override
    public void applyVersionDecision(int decision) {
        /*
         * decision ==  1    ->      Update Version
         * decision ==  0    ->      Remind Me Later
         * decision == -1    ->      Skip this Version
         */
        editor = sharedPreferences.edit();
        switch (decision){
            case 1:
//                Toast.makeText(this, "Update Version Now!", Toast.LENGTH_SHORT).show();
                // according to:
                //  https://stackoverflow.com/a/11753070/8405296
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;
            case 0:
                Toast.makeText(this, "We will remind you the next time you'll open the app :)", Toast.LENGTH_SHORT).show();
                break;
            case -1:
                Toast.makeText(this, "Update Skipped!", Toast.LENGTH_SHORT).show();
                editor.putInt(Utilities.SHARED_PREFERENCES_VERSION, Integer.parseInt(versionFromServer));
                editor.apply();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
        whiteListCheckAsyncTask.execute(stickerPackList.toArray(new StickerPack[0]));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    private void showStickerPackList(List<StickerPack> _stickerPackList, boolean toAnimate) {
        allStickerPacksListAdapter = new StickerPackListAdapter(_stickerPackList, onAddButtonClickedListener, toAnimate);
        packRecyclerView.setAdapter(allStickerPacksListAdapter);
        packLayoutManager = new LinearLayoutManager(this);
        packLayoutManager.setOrientation(RecyclerView.VERTICAL);
        packRecyclerView.setLayoutManager(packLayoutManager);
        packRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this::recalculateColumnCount);
    }

    private final StickerPackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener = pack -> addStickerPackToWhatsApp(pack.identifier, pack.name);

    private void recalculateColumnCount() {
        final int previewSize = getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size);
        int firstVisibleItemPosition = packLayoutManager.findFirstVisibleItemPosition();
        StickerPackListItemViewHolder viewHolder = (StickerPackListItemViewHolder) packRecyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition);
        if (viewHolder != null) {
            final int widthOfImageRow = viewHolder.imageRowView.getMeasuredWidth();
            final int max = Math.max(widthOfImageRow / previewSize, 1);
            int maxNumberOfImagesInARow = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max);
            int minMarginBetweenImages = (widthOfImageRow - maxNumberOfImagesInARow * previewSize) / (maxNumberOfImagesInARow - 1);
            allStickerPacksListAdapter.setImageRowSpec(maxNumberOfImagesInARow, minMarginBetweenImages);
        }
    }


    public static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, List<StickerPack>> {
        private final WeakReference<StickerPackListActivity> stickerPackListActivityWeakReference;

        WhiteListCheckAsyncTask(StickerPackListActivity stickerPackListActivity) {
            this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        @Override
        protected final List<StickerPack> doInBackground(StickerPack... stickerPackArray) {
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity == null) {
                return Arrays.asList(stickerPackArray);
            }
            for (StickerPack stickerPack : stickerPackArray) {
                stickerPack.setIsWhitelisted(WhitelistCheck.isWhitelisted(stickerPackListActivity, stickerPack.identifier));
            }
            return Arrays.asList(stickerPackArray);
        }

        @Override
        protected void onPostExecute(List<StickerPack> stickerPackList) {
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity != null && stickerPackListActivity.allStickerPacksListAdapter != null) {
                stickerPackListActivity.allStickerPacksListAdapter.setStickerPackList(stickerPackList);
                stickerPackListActivity.allStickerPacksListAdapter.notifyDataSetChanged();
            }
        }
    }

    public static class BottomOffsetDecoration extends RecyclerView.ItemDecoration {
        private int mBottomOffset;
        public BottomOffsetDecoration(int bottomOffset) {
            mBottomOffset = bottomOffset;
        }
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int dataSize = state.getItemCount();
            int position = parent.getChildAdapterPosition(view);
            if (dataSize > 0 && position == dataSize - 1) {
                outRect.set(0, 0, 0, mBottomOffset);
            } else {
                outRect.set(0, 0, 0, 0);
            }
        }
    }
}



















