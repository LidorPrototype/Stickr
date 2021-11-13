package com.l_es.kiril_stickers;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class StickerPackDetailsActivity extends AddStickerPackActivity {

    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private StickerPreviewAdapter stickerPreviewAdapter;
    private int numColumns;
    private TextView addToWhatsappButton, alreadyAddedText, packNameTextView,
            packPublisherTextView, packDescriptionTextView, packSizeTextView;
    private FrameLayout addTextContainer;
    private StickerPack stickerPack;
    private View divider;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private ImageView imageViewShare, imageViewLike, imageViewInfo;
    // Like mechanism
    public static boolean[] likes = Utilities.PACKS_LIKES;
    private SharedPreferences prefs;
    private AnimatedVectorDrawable emptyHeart, fillHeart;
    private boolean full = false;
    // Firebase Related
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Stickers Packs");
    private Map<String, Integer> mapLikes = new HashMap<String, Integer>();
    // AdMob Related
    private boolean isFeatured = false, userWatchedAllAd = false, isLoaded = false;
    private AdRequest adRequest;
    private RewardedAd mRewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_details);

        stickerPack = getIntent().getParcelableExtra(Utilities.EXTRA_STICKER_PACK_DATA);
        isFeatured = getIntent().getBooleanExtra(Utilities.IS_FEATURED, false);
        packNameTextView = findViewById(R.id.pack_name);
        packPublisherTextView = findViewById(R.id.author);
        packDescriptionTextView = findViewById(R.id.pack_description);
        packSizeTextView = findViewById(R.id.pack_size);
        ImageView packTrayIcon = findViewById(R.id.cover_image);
        imageViewShare = findViewById(R.id.action_share);
        imageViewLike = findViewById(R.id.action_like);
        imageViewInfo = findViewById(R.id.action_info);

        prefs = this.getSharedPreferences(Utilities.LIKES, 0);
        likes = loadArray(Utilities.LIKES_ARRAY);

        emptyHeart = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.like_heart_empty);
        fillHeart = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.like_heart_fill);
        full = isLiked();
        animate();

        addToWhatsappButton = findViewById(R.id.add_to_whatsapp_button);
        alreadyAddedText = findViewById(R.id.already_added_text);
        addTextContainer = findViewById(R.id.addToWhatsappContainer);
        layoutManager = new GridLayoutManager(this, 1);
        recyclerView = findViewById(R.id.sticker_list);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(pageLayoutListener);
        recyclerView.addOnScrollListener(dividerScrollListener);
        divider = findViewById(R.id.divider);
        if (stickerPreviewAdapter == null) {
            stickerPreviewAdapter = new StickerPreviewAdapter(getLayoutInflater(), R.drawable.sticker_error, getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size), getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_padding), stickerPack);
            recyclerView.setAdapter(stickerPreviewAdapter);
        }
        packNameTextView.setText(stickerPack.name);
        packPublisherTextView.setText(stickerPack.publisher);
        packDescriptionTextView.setText(Utilities.descriptionsMap.get(stickerPack.name));
        packTrayIcon.setImageURI(StickerPackLoader.getStickerAssetUri(stickerPack.identifier, stickerPack.trayImageFile));
        packSizeTextView.setText(Formatter.formatShortFileSize(this, stickerPack.getTotalSize()));
        addToWhatsappButton.setOnClickListener(v -> rewardedAdIntoWhatsapp());
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // Back button logic
        findViewById(R.id.back_btn).setOnClickListener(view -> onBackPressed());

        // Google AdMob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) { }
        });
        // ******* Start - add Dev phone as a tester in development
//        List<String> testDeviceIds = Arrays.asList("F44EEA553A26E309A0D097942B648FE8");
//        RequestConfiguration configuration =
//                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
//        MobileAds.setRequestConfiguration(configuration);
        // ******* End - add Dev phone as a tester in development
        AdView adView = findViewById(R.id.adViewInDetails);
        adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)    // Add the emulator as a test device
                .build();
        adView.loadAd(adRequest);
        loadRewardedAd();

        imageViewShare.setOnClickListener(view -> shareApplication());
        imageViewLike.setOnClickListener(view -> likeCurrentPack());
        imageViewInfo.setOnClickListener(view -> openActionInfoPage());

        // Fonts
        Typeface typefaceRegular = ResourcesCompat.getFont(this, R.font.aduma_regular);
        Typeface typefaceLight = ResourcesCompat.getFont(this, R.font.aduma_light);
        Typeface typefaceBold = ResourcesCompat.getFont(this, R.font.aduma_bold);
        addToWhatsappButton.setTypeface(typefaceRegular);
        alreadyAddedText.setTypeface(typefaceLight);
        packDescriptionTextView.setTypeface(typefaceLight);
        packNameTextView.setTypeface(typefaceBold);
        packPublisherTextView.setTypeface(typefaceRegular);
        packSizeTextView.setTypeface(typefaceLight);

    }

    private void loadRewardedAd() {
        RewardedAd.load(this, "ca-app-pub-4998289698335082/3037269218", adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                super.onAdLoaded(rewardedAd);
                mRewardedAd = rewardedAd;
                isLoaded = true;
                setRewardedAdScreen();
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mRewardedAd = null;
//                Toast.makeText(StickerPackDetailsActivity.this, "FAILED TO LOAD!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setRewardedAdScreen() {
        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
//                Toast.makeText(StickerPackDetailsActivity.this, "Ad failed to show.", Toast.LENGTH_SHORT).show();
                isLoaded = true;
            }
            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                mRewardedAd = null;
            }
            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
//                Toast.makeText(StickerPackDetailsActivity.this, "Ad was dismissed.", Toast.LENGTH_SHORT).show();
                if(userWatchedAllAd){
                    addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name);
                }
                userWatchedAllAd = false;
                loadRewardedAd();
            }
            @Override
            public void onAdImpression() {
                super.onAdImpression();
//                Toast.makeText(StickerPackDetailsActivity.this, "Ad impression detected.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rewardedAdIntoWhatsapp() {
        if(isFeatured){
            if(isLoaded){
                if (mRewardedAd != null) {
                    Activity activityContext = StickerPackDetailsActivity.this;
                    mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            // Handle the reward.
                            int rewardAmount = rewardItem.getAmount();
                            String rewardType = rewardItem.getType();
                            userWatchedAllAd = true;
//                            Toast.makeText(activityContext, "The user earned the reward, " + rewardAmount + " " + rewardType, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }else {
                Toast.makeText(this, "Take a look around first ;-)", Toast.LENGTH_SHORT).show();
            }
        }else{
            addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name);
        }
    }

    // Likes mechanism
    private boolean isLiked(){
        for(int i = 0; i < Utilities.PACKS_NAMES.length; i++){
            if(stickerPack.name.equals(Utilities.PACKS_NAMES[i])){
                if(likes[i]){
                    return true;
                }
            }
        }
        return false;
    }
    private void likeCurrentPack() {
        int idx = 0;
        for(int i = 0; i < Utilities.PACKS_NAMES.length; i++){
            if(stickerPack.name.equals(Utilities.PACKS_NAMES[i])){
                idx = i;
                break;
            }
        }
        likes[idx] = !likes[idx];
        storeArray(likes, Utilities.LIKES_ARRAY);
        animate();
        DocumentReference documentReference = collectionReference.document(stickerPack.name);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    int likesCount = full ? -1 : 1;
                    int oldLikes = ((int)Math.round(documentSnapshot.getDouble(Utilities.LIKES)));
                    likesCount += oldLikes;
                    mapLikes.put(Utilities.LIKES, likesCount);
                    documentReference.set(mapLikes, SetOptions.merge());
                }
            }
        });
    }
    // This method help to animate our heart
    public void animate()
    {
        AnimatedVectorDrawable drawable
                = full
                ? fillHeart
                : emptyHeart;
        imageViewLike.setImageDrawable(drawable);
        drawable.start();
        full = !full;
    }
    public boolean storeArray(boolean[] array, String arrayName) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName, array.length);
        for(int i=0;i<array.length;i++) {
            editor.putBoolean(arrayName + "_" + i, array[i]);
        }
        return editor.commit();
    }
    public boolean[] loadArray(String arrayName) {
        int documentedSize = Utilities.PACKS_NAMES.length;
        int size = prefs.getInt(arrayName, documentedSize);
        if(size < documentedSize){
            size = documentedSize;
        }
        boolean array[] = new boolean[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getBoolean(arrayName + "_" + i, false);

        return array;
    }
    // Share mechanism
    private void shareApplication() {
        try {
            Uri imageUri = Uri.parse("android.resource://" + getPackageName()
                    + "/drawable/" + "the_heart");//R.drawable.the_heart);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
            String applicationName = getResources().getString(R.string.app_name);
            String shareMessage= "\nLet me recommend you " + applicationName + "\n\n";
            shareMessage = shareMessage + "And check out the stickers pack named " + stickerPack.name + "\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
//            shareIntent.setType("image/jpeg");
//            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
//            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch(Exception e) {
            //e.toString();
        }
    }

    private void openActionInfoPage() {
        Uri trayIconUri = StickerPackLoader.getStickerAssetUri(stickerPack.identifier, stickerPack.trayImageFile);
        launchInfoActivity(stickerPack.publisherWebsite, stickerPack.publisherEmail, stickerPack.privacyPolicyWebsite, stickerPack.licenseAgreementWebsite, trayIconUri.toString());
    }

    private void launchInfoActivity(String publisherWebsite, String publisherEmail, String privacyPolicyWebsite, String licenseAgreementWebsite, String trayIconUriString) {
        Intent intent = new Intent(StickerPackDetailsActivity.this, StickerPackInfoActivity.class);
        intent.putExtra(Utilities.EXTRA_STICKER_PACK_ID, stickerPack.identifier);
        intent.putExtra(Utilities.EXTRA_STICKER_PACK_WEBSITE, publisherWebsite);
        intent.putExtra(Utilities.EXTRA_STICKER_PACK_EMAIL, publisherEmail);
        intent.putExtra(Utilities.EXTRA_STICKER_PACK_PRIVACY_POLICY, privacyPolicyWebsite);
        intent.putExtra(Utilities.EXTRA_STICKER_PACK_LICENSE_AGREEMENT, licenseAgreementWebsite);
        intent.putExtra(Utilities.EXTRA_STICKER_PACK_TRAY_ICON, trayIconUriString);
        startActivity(intent);
    }

    private final ViewTreeObserver.OnGlobalLayoutListener pageLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            setNumColumns(recyclerView.getWidth() / recyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size));
        }
    };

    private void setNumColumns(int numColumns) {
        if (this.numColumns != numColumns) {
            layoutManager.setSpanCount(numColumns);
            this.numColumns = numColumns;
            if (stickerPreviewAdapter != null) {
                stickerPreviewAdapter.notifyDataSetChanged();
            }
        }
    }

    private final RecyclerView.OnScrollListener dividerScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            updateDivider(recyclerView);
        }

        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateDivider(recyclerView);
        }

        private void updateDivider(RecyclerView recyclerView) {
            boolean showDivider = recyclerView.computeVerticalScrollOffset() > 0;
            if (divider != null) {
                divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
        whiteListCheckAsyncTask.execute(stickerPack);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    private void updateAddUI(Boolean isWhitelisted) {
        if (isWhitelisted) {
            addToWhatsappButton.setVisibility(View.GONE);
            alreadyAddedText.setVisibility(View.VISIBLE);
            addTextContainer.setBackgroundResource(R.drawable.add_whatsapp_bg_green);
        } else {
            addToWhatsappButton.setVisibility(View.VISIBLE);
            alreadyAddedText.setVisibility(View.GONE);
            addTextContainer.setBackgroundResource(R.drawable.add_whatsapp_bg_red);
        }
    }

    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, Boolean> {
        private final WeakReference<StickerPackDetailsActivity> stickerPackDetailsActivityWeakReference;

        WhiteListCheckAsyncTask(StickerPackDetailsActivity stickerPackListActivity) {
            this.stickerPackDetailsActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        @Override
        protected final Boolean doInBackground(StickerPack... stickerPacks) {
            StickerPack stickerPack = stickerPacks[0];
            final StickerPackDetailsActivity stickerPackDetailsActivity = stickerPackDetailsActivityWeakReference.get();
            if (stickerPackDetailsActivity == null) {
                return false;
            }
            return WhitelistCheck.isWhitelisted(stickerPackDetailsActivity, stickerPack.identifier);
        }

        @Override
        protected void onPostExecute(Boolean isWhitelisted) {
            final StickerPackDetailsActivity stickerPackDetailsActivity = stickerPackDetailsActivityWeakReference.get();
            if (stickerPackDetailsActivity != null) {
                stickerPackDetailsActivity.updateAddUI(isWhitelisted);
            }
        }
    }

}
