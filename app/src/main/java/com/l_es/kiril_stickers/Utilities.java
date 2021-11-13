package com.l_es.kiril_stickers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lidor on 2/14/2021.
 * Developer name: L-ES
 *  _        _   _____     ____    ______
 * | |      |_| |  __ \   / __ \  |  O   |
 * | |      | | | |  | | | |  | | |   ___/
 * | |____  | | | |__| | | |__| | | | \
 * |______| |_| |_____/   \____/  |_|__\
 *  ____         ____
 * |  __|       |  __|
 * |  __|   _   |__  |
 * |____|  |_|  |____|
 */
public class Utilities {

    public static final String CURRENT_VERSION = "6";
    public static final String VALIDATION_MUST = "MUST";
    public static final String VALIDATION_OPTION = "OPTION";
    public static final String SHARED_PREFERENCES = "KirillsStickersSharedPreferences";
    public static final String SHARED_PREFERENCES_FEATURED = "FeaturedPack";
    public static final String SHARED_PREFERENCES_VERSION = "SP_VERSION";
    public static final String IS_FEATURED = "IS_FEATURED";

    public static final Map<String, String> descriptionsMap;
    static {
        Map<String, String> mapInit = new HashMap<String, String>();
        mapInit.put("Asfour 2.0", translateString(R.string.asfour));
        mapInit.put("AsiVeGuri 2.0", translateString(R.string.asi_ve_guri));
        mapInit.put("GurVeOach 2.0", translateString(R.string.gur_ve_oach));
        mapInit.put("HaParlament 2.0", translateString(R.string.haparlament));
        mapInit.put("HaPijamot 2.0", translateString(R.string.hapijamot));
        mapInit.put("Shababnikim 2.0", translateString(R.string.shababnikim));
        mapInit.put("ZaguriEmpire 2.0", translateString(R.string.zaguri_empire));
        mapInit.put("ZotVeZoti 2.0", translateString(R.string.zot_ve_zoti));
        descriptionsMap = Collections.unmodifiableMap(mapInit);
    }

    public static final String[] PACKS_NAMES  = {"HaPijamot 2.0", "ZotVeZoti 2.0", "AsiVeGuri 2.0",
                                                "GurVeOach 2.0", "HaParlament 2.0", "ZaguriEmpire 2.0",
                                                "Shababnikim 2.0", "Asfour 2.0"};
    public static final boolean[] PACKS_LIKES = {false, false, false, false,
                                                false, false, false, false};

    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";

    public static final String LIKES_ARRAY  = "likesArray";
    public static final String DATE         = "Date";
    public static final String DOWNLOADS    = "Downloads";
    public static final String LIKES        = "Likes";

    /**
     * Do not change the strings listed below, as these are used by WhatsApp. And changing these will break the interface between sticker app and WhatsApp.
     */
    public static final String STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier";
    public static final String STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name";
    public static final String STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher";
    public static final String STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon";
    public static final String ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link";
    public static final String IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link";
    public static final String PUBLISHER_EMAIL = "sticker_pack_publisher_email";
    public static final String PUBLISHER_WEBSITE = "sticker_pack_publisher_website";
    public static final String PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website";
    public static final String LICENSE_AGREENMENT_WEBSITE = "sticker_pack_license_agreement_website";
    public static final String IMAGE_DATA_VERSION = "image_data_version";
    public static final String AVOID_CACHE = "whatsapp_will_not_cache_stickers";
    public static final String STICKER_FILE_NAME_IN_QUERY = "sticker_file_name";
    public static final String STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji";
    protected static final String CONTENT_FILE_NAME = "contents.json";

    /**
     * Do not change below values of below 3 lines as this is also used by WhatsApp
     */
    public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
    public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
    public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";
    public static final String EXTRA_STICKER_PACK_WEBSITE = "sticker_pack_website";
    public static final String EXTRA_STICKER_PACK_EMAIL = "sticker_pack_email";
    public static final String EXTRA_STICKER_PACK_PRIVACY_POLICY = "sticker_pack_privacy_policy";
    public static final String EXTRA_STICKER_PACK_LICENSE_AGREEMENT = "sticker_pack_license_agreement";
    public static final String EXTRA_STICKER_PACK_TRAY_ICON = "sticker_pack_tray_icon";
    public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
    public static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";

    /**
     * Application Stores Keys
     */
    protected static final String PLAY_STORE_DOMAIN = "play.google.com";
    protected static final String APPLE_STORE_DOMAIN = "itunes.apple.com";

    // Fonts
//    Typeface typefaceRegular = ResourcesCompat.getFont(this, R.font.aduma_regular);
//    Typeface typefaceLight = ResourcesCompat.getFont(this, R.font.aduma_light);
//    Typeface typefaceBold = ResourcesCompat.getFont(this, R.font.aduma_bold);
//    Typeface typefaceHeavy = ResourcesCompat.getFont(this, R.font.aduma_heavy);

    private static String translateString(int str) {
        String translation = "";
        switch (str){
            case R.string.asi_ve_guri:
                translation = "";
                translation += "אסי וגורי היה שם הבמה של צמד הקומיקאים אסי כהן וגורי אלפי, שפעל בין 1999 ל-2003. הצמד שחרר את אלבום השירים שלו בשנת 2002 והיה פופולרי מאוד באותה תקופה עם מופע קורע מצחוק שניתן למצוא ביוטיוב.";
                break;
            case R.string.gur_ve_oach:
                translation = "";
                translation += "גור ואוח היא סדרת טלוויזיה קומית לילדים ששודרה בערוץ פוקס קידס וג'טיקס עד שנת 2009. הסדרה עוסקת בקורותיהם של שני ילדים בגיל היסודי הגרים במקלט ומתמודדים בהומור עם המתרחש מסביב.";
                break;
            case R.string.haparlament:
                translation = "";
                translation += "הפרלמנט היא סדרת-בת קומית ישראלית של ארץ נהדרת וזוכת פרס האקדמיה לטלוויזיה, ששודרה בערוץ 2 על-ידי הזכיינית קשת החל משנת 2012.";
                break;
            case R.string.hapijamot:
                translation = "";
                translation += "הפיג'מות היא סדרה קומית ישראלית לילדים ונוער, ששודרה בערוץ הילדים בין השנים 2003-2015 במשך תשע עונות. הסדרה מגוללת את סיפורה של חברי להקה כושלת מנתניה, העוברת להתגורר בתל אביב.";
                break;
            case R.string.shababnikim:
                translation = "";
                translation += "שבבניקים גבר! שבבניקים גבר! שבבניקים גבר! שבבניקים גבר! שבבניקים גבר! שבבניקים גבר! שבבניקים גבר! שבבניקים גבר! שבבניקים גבר! ";
                break;
            case R.string.zaguri_empire:
                translation = "";
                translation += "זגורי אימפריה היא סדרת טלוויזיה ישראלית המשודרת בהוט, זוכת פרס האקדמיה. הסדרה מגוללת את סיפורה של משפחת זגורי רוויית המתחים מבאר-שבע, הנאבקת להסיר קללה הרובצת על ראשה מאז מות הסבא.";
                break;
            case R.string.zot_ve_zoti:
                translation = "";
                translation += "זאת וזאתי היא סדרה קומית ישראלית ברשת 13, העוקבת אחר עלילות תותית ומילעל, צמד מוזיקלי כושל עם אמרגן מבוהל. התוכנית זכתה להצלחה ברשת ומספר רב של סצנות מהתוכנית הפכו לממים.";
                break;
            case R.string.asfour:
                translation = "";
                translation += "עספור היא סדרת דרמת מתח ישראלית ששודרה בהוט בין 2010-2011. הסדרה מגוללת את סיפורם של ארבעה חברים המתגוררים בחוות אוטובוסים נטושים, בשולי אזור התעשייה בירושלים ונאבקים להחזיר את השטח לרשותם.";
                break;
        }
        return translation;
    }

}
