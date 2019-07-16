package com.example.photopaint.ui.actionbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.StateSet;

import com.example.photopaint.messenger.AndroidUtilities;
import org.json.JSONArray;
import org.json.JSONObject;
import com.example.photopaint.messenger.AndroidUtilities;
import com.example.photopaint.messenger.ApplicationLoader;
import com.example.photopaint.messenger.BuildVars;
import com.example.photopaint.messenger.FileLog;
import com.example.photopaint.messenger.Utilities;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.ApplicationLoader;
//import org.telegram.messenger.BuildVars;
//import org.telegram.messenger.FileLog;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.MediaController;
//import org.telegram.messenger.MessagesController;
//import org.telegram.messenger.NotificationCenter;
//import org.telegram.messenger.R;
//import org.telegram.messenger.SharedConfig;
//import org.telegram.messenger.Utilities;
//import org.telegram.messenger.time.SunDate;
//import org.telegram.ui.Components.CombinedDrawable;
//import org.telegram.ui.Components.ScamDrawable;
//import org.telegram.ui.Components.ThemeEditorView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Theme {

    public static class ThemeInfo {
        public String name;
        public String pathToFile;
        public String assetName;

        public int previewBackgroundColor;
        public int previewInColor;
        public int previewOutColor;
        public int sortIndex;

        public JSONObject getSaveJson() {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", name);
                jsonObject.put("path", pathToFile);
                return jsonObject;
            } catch (Exception e) {
                FileLog.e(e);
            }
            return null;
        }

        public String getName() {
            return "Default";
//            if ("Default".equals(name)) {
//                return LocaleController.getString("Default", R.string.Default);
//            } else if ("Blue".equals(name)) {
//                return LocaleController.getString("ThemeBlue", R.string.ThemeBlue);
//            } else if ("Dark".equals(name)) {
//                return LocaleController.getString("ThemeDark", R.string.ThemeDark);
//            } else if ("Dark Blue".equals(name)) {
//                return LocaleController.getString("ThemeDarkBlue", R.string.ThemeDarkBlue);
//            } else if ("Graphite".equals(name)) {
//                return LocaleController.getString("ThemeGraphite", R.string.ThemeGraphite);
//            } else if ("Arctic Blue".equals(name)) {
//                return LocaleController.getString("ThemeArcticBlue", R.string.ThemeArcticBlue);
//            }
//            return name;
        }

        public boolean isDark() {
            return "Dark".equals(name) || "Dark Blue".equals(name) || "Graphite".equals(name);
        }

        public boolean isLight() {
            return pathToFile == null && !isDark();
        }

        public static ThemeInfo createWithJson(JSONObject object) {
            if (object == null) {
                return null;
            }
            try {
                ThemeInfo themeInfo = new ThemeInfo();
                themeInfo.name = object.getString("name");
                themeInfo.pathToFile = object.getString("path");
                return themeInfo;
            } catch (Exception e) {
                FileLog.e(e);
            }
            return null;
        }

        public static ThemeInfo createWithString(String string) {
            if (TextUtils.isEmpty(string)) {
                return null;
            }
            String[] args = string.split("\\|");
            if (args.length != 2) {
                return null;
            }
            ThemeInfo themeInfo = new ThemeInfo();
            themeInfo.name = args[0];
            themeInfo.pathToFile = args[1];
            return themeInfo;
        }
    }

    private static final Object sync = new Object();
    private static final Object wallpaperSync = new Object();

    public static final int ACTION_BAR_PHOTO_VIEWER_COLOR = 0x7f000000;
    public static final int ACTION_BAR_MEDIA_PICKER_COLOR = 0xff333333;
    public static final int ACTION_BAR_VIDEO_EDIT_COLOR = 0xff000000;
    public static final int ACTION_BAR_PLAYER_COLOR = 0xffffffff;
    public static final int ACTION_BAR_PICKER_SELECTOR_COLOR = 0xff3d3d3d;
    public static final int ACTION_BAR_WHITE_SELECTOR_COLOR = 0x40ffffff;
    public static final int ACTION_BAR_AUDIO_SELECTOR_COLOR = 0x2f000000;
    public static final int ARTICLE_VIEWER_MEDIA_PROGRESS_COLOR = 0xffffffff;
    //public static final int INPUT_FIELD_SELECTOR_COLOR = 0xffd6d6d6;

    public static final int AUTO_NIGHT_TYPE_NONE = 0;
    public static final int AUTO_NIGHT_TYPE_SCHEDULED = 1;
    public static final int AUTO_NIGHT_TYPE_AUTOMATIC = 2;

    private static final int LIGHT_SENSOR_THEME_SWITCH_DELAY = 1800;
    private static final int LIGHT_SENSOR_THEME_SWITCH_NEAR_DELAY = 12000;
    private static final int LIGHT_SENSOR_THEME_SWITCH_NEAR_THRESHOLD = 12000;
    private static SensorManager sensorManager;
    private static Sensor lightSensor;
    private static boolean lightSensorRegistered;
    private static float lastBrightnessValue = 1.0f;
    private static long lastThemeSwitchTime;
    private static boolean switchDayRunnableScheduled;
    private static boolean switchNightRunnableScheduled;
    private static Runnable switchDayBrightnessRunnable = new Runnable() {
        @Override
        public void run() {
            switchDayRunnableScheduled = false;
//            applyDayNightThemeMaybe(false);
        }
    };
    private static Runnable switchNightBrightnessRunnable = new Runnable() {
        @Override
        public void run() {
            switchNightRunnableScheduled = false;
//            applyDayNightThemeMaybe(true);
        }
    };
    public static int selectedAutoNightType = AUTO_NIGHT_TYPE_NONE;
    public static boolean autoNightScheduleByLocation;
    public static float autoNightBrighnessThreshold = 0.25f;
    public static int autoNightDayStartTime = 22 * 60;
    public static int autoNightDayEndTime = 8 * 60;
    public static int autoNightSunsetTime = 22 * 60;
    public static int autoNightLastSunCheckDay = -1;
    public static int autoNightSunriseTime = 8 * 60;
    public static String autoNightCityName = "";
    public static double autoNightLocationLatitude = 10000;
    public static double autoNightLocationLongitude = 10000;

    private static class AttachCameraDrawable extends Drawable {

        private Path segment;
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);


        public AttachCameraDrawable() {
            int size = AndroidUtilities.dp(54);
            RectF rect = new RectF(0, 0, size, size);

            segment = new Path();
            segment.moveTo(AndroidUtilities.dp(23), AndroidUtilities.dp(20));
            segment.lineTo(AndroidUtilities.dp(23), 0);
            segment.arcTo(rect, -98, 50, false);
            segment.close();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.save();
            int cx = AndroidUtilities.dp(27);
            canvas.rotate(-90, cx, cx);
            for (int a = 0; a < 6; a++) {
                switch (a) {
                    case 0:
                        paint.setColor(getColor(key_chat_attachCameraIcon1));
                        break;
                    case 1:
                        paint.setColor(getColor(key_chat_attachCameraIcon2));
                        break;
                    case 2:
                        paint.setColor(getColor(key_chat_attachCameraIcon3));
                        break;
                    case 3:
                        paint.setColor(getColor(key_chat_attachCameraIcon4));
                        break;
                    case 4:
                        paint.setColor(getColor(key_chat_attachCameraIcon5));
                        break;
                    case 5:
                        paint.setColor(getColor(key_chat_attachCameraIcon6));
                        break;
                }
                canvas.rotate(60, cx, cx);
                canvas.drawPath(segment, paint);
            }
            canvas.restore();
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }

        @Override
        public int getIntrinsicWidth() {
            return AndroidUtilities.dp(54);
        }

        @Override
        public int getIntrinsicHeight() {
            return AndroidUtilities.dp(54);
        }

        @Override
        public int getMinimumWidth() {
            return AndroidUtilities.dp(54);
        }

        @Override
        public int getMinimumHeight() {
            return AndroidUtilities.dp(54);
        }
    }

    private static Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public static ArrayList<ThemeInfo> themes;
    private static ArrayList<ThemeInfo> otherThemes;
    private static HashMap<String, ThemeInfo> themesDict;
    private static ThemeInfo currentTheme;
    private static ThemeInfo currentNightTheme;
    private static ThemeInfo currentDayTheme;
    private static ThemeInfo defaultTheme;
    private static ThemeInfo previousTheme;

    public static PorterDuffColorFilter colorFilter;
    public static PorterDuffColorFilter colorPressedFilter;
    public static PorterDuffColorFilter colorFilter2;
    public static PorterDuffColorFilter colorPressedFilter2;
    private static int selectedColor;
    private static boolean isCustomTheme;
    private static int serviceMessageColor;
    private static int serviceSelectedMessageColor;
    public static int serviceMessageColorBackup;
    public static int serviceSelectedMessageColorBackup;
    private static int serviceMessage2Color;
    private static int serviceSelectedMessage2Color;
    private static int currentColor;
    private static int currentSelectedColor;
    private static Drawable wallpaper;
    private static Drawable themedWallpaper;
    private static int themedWallpaperFileOffset;
    private static boolean isWallpaperMotion;
    private static boolean isPatternWallpaper;

    public static Paint dividerPaint;
    public static Paint linkSelectionPaint;
    public static Paint checkboxSquare_eraserPaint;
    public static Paint checkboxSquare_checkPaint;
    public static Paint checkboxSquare_backgroundPaint;
    public static Paint avatar_backgroundPaint;

    public static Drawable listSelector;
    public static Drawable avatar_broadcastDrawable;
    public static Drawable avatar_savedDrawable;

    public static Drawable moveUpDrawable;

    public static Paint dialogs_onlineCirclePaint;
    public static Paint dialogs_tabletSeletedPaint;
    public static Paint dialogs_pinnedPaint;
    public static Paint dialogs_countPaint;
    public static Paint dialogs_errorPaint;
    public static Paint dialogs_countGrayPaint;
    public static TextPaint dialogs_namePaint;
    public static TextPaint dialogs_nameEncryptedPaint;
    public static TextPaint dialogs_searchNamePaint;
    public static TextPaint dialogs_searchNameEncryptedPaint;
    public static TextPaint dialogs_messagePaint;
    public static TextPaint dialogs_messageNamePaint;
    public static TextPaint dialogs_messagePrintingPaint;
    public static TextPaint dialogs_timePaint;
    public static TextPaint dialogs_countTextPaint;
    public static TextPaint dialogs_archiveTextPaint;
    public static TextPaint dialogs_onlinePaint;
    public static TextPaint dialogs_offlinePaint;
    public static Drawable dialogs_checkDrawable;
    public static Drawable dialogs_halfCheckDrawable;
    public static Drawable dialogs_clockDrawable;
    public static Drawable dialogs_errorDrawable;
    public static Drawable dialogs_reorderDrawable;
    public static Drawable dialogs_lockDrawable;
    public static Drawable dialogs_groupDrawable;
    public static Drawable dialogs_broadcastDrawable;
    public static Drawable dialogs_botDrawable;
    public static Drawable dialogs_muteDrawable;
    public static Drawable dialogs_verifiedDrawable;
//    public static ScamDrawable dialogs_scamDrawable;
    public static Drawable dialogs_verifiedCheckDrawable;
    public static Drawable dialogs_pinnedDrawable;
    public static Drawable dialogs_mentionDrawable;
    public static Drawable dialogs_holidayDrawable;
    public static Drawable dialogs_archiveDrawable;
    public static Drawable dialogs_unarchiveDrawable;
    public static Drawable dialogs_pinArchiveDrawable;
    public static Drawable dialogs_unpinArchiveDrawable;
    public static boolean dialogs_archiveDrawableRecolored;
    public static boolean dialogs_archiveAvatarDrawableRecolored;
    private static int dialogs_holidayDrawableOffsetX;
    private static int dialogs_holidayDrawableOffsetY;
    private static long lastHolidayCheckTime;
    private static boolean canStartHolidayAnimation;

    public static TextPaint profile_aboutTextPaint;
    public static Drawable profile_verifiedDrawable;
    public static Drawable profile_verifiedCheckDrawable;

    public static Paint chat_docBackPaint;
    public static Paint chat_deleteProgressPaint;
    public static Paint chat_botProgressPaint;
    public static Paint chat_urlPaint;
    public static Paint chat_textSearchSelectionPaint;
    public static Paint chat_instantViewRectPaint;
    public static Paint chat_replyLinePaint;
    public static Paint chat_msgErrorPaint;
    public static Paint chat_statusPaint;
    public static Paint chat_statusRecordPaint;
    public static Paint chat_actionBackgroundPaint;
    public static Paint chat_timeBackgroundPaint;
    public static Paint chat_composeBackgroundPaint;
    public static Paint chat_radialProgressPaint;
    public static Paint chat_radialProgress2Paint;
    public static TextPaint chat_msgTextPaint;
    public static TextPaint chat_actionTextPaint;
    public static TextPaint chat_msgBotButtonPaint;
    public static TextPaint chat_msgGameTextPaint;
    public static TextPaint chat_msgTextPaintOneEmoji;
    public static TextPaint chat_msgTextPaintTwoEmoji;
    public static TextPaint chat_msgTextPaintThreeEmoji;
    public static TextPaint chat_infoPaint;
    public static TextPaint chat_livePaint;
    public static TextPaint chat_docNamePaint;
    public static TextPaint chat_locationTitlePaint;
    public static TextPaint chat_locationAddressPaint;
    public static TextPaint chat_durationPaint;
    public static TextPaint chat_gamePaint;
    public static TextPaint chat_shipmentPaint;
    public static TextPaint chat_instantViewPaint;
    public static TextPaint chat_audioTimePaint;
    public static TextPaint chat_audioTitlePaint;
    public static TextPaint chat_audioPerformerPaint;
    public static TextPaint chat_botButtonPaint;
    public static TextPaint chat_contactNamePaint;
    public static TextPaint chat_contactPhonePaint;
    public static TextPaint chat_timePaint;
    public static TextPaint chat_adminPaint;
    public static TextPaint chat_namePaint;
    public static TextPaint chat_forwardNamePaint;
    public static TextPaint chat_replyNamePaint;
    public static TextPaint chat_replyTextPaint;
    public static TextPaint chat_contextResult_titleTextPaint;
    public static TextPaint chat_contextResult_descriptionTextPaint;

    public static Drawable chat_msgNoSoundDrawable;
    public static Drawable chat_composeShadowDrawable;
    public static Drawable chat_roundVideoShadow;
    public static Drawable chat_msgInDrawable;
    public static Drawable chat_msgInSelectedDrawable;
    public static Drawable chat_msgInShadowDrawable;
    public static Drawable chat_msgOutDrawable;
    public static Drawable chat_msgOutSelectedDrawable;
    public static Drawable chat_msgOutShadowDrawable;
    public static Drawable chat_msgInMediaDrawable;
    public static Drawable chat_msgInMediaSelectedDrawable;
    public static Drawable chat_msgInMediaShadowDrawable;
    public static Drawable chat_msgOutMediaDrawable;
    public static Drawable chat_msgOutMediaSelectedDrawable;
    public static Drawable chat_msgOutMediaShadowDrawable;
    public static Drawable chat_msgOutCheckDrawable;
    public static Drawable chat_msgOutCheckSelectedDrawable;
    public static Drawable chat_msgOutHalfCheckDrawable;
    public static Drawable chat_msgOutHalfCheckSelectedDrawable;
    public static Drawable chat_msgOutClockDrawable;
    public static Drawable chat_msgOutSelectedClockDrawable;
    public static Drawable chat_msgInClockDrawable;
    public static Drawable chat_msgInSelectedClockDrawable;
    public static Drawable chat_msgMediaCheckDrawable;
    public static Drawable chat_msgMediaHalfCheckDrawable;
    public static Drawable chat_msgMediaClockDrawable;
    public static Drawable chat_msgStickerCheckDrawable;
    public static Drawable chat_msgStickerHalfCheckDrawable;
    public static Drawable chat_msgStickerClockDrawable;
    public static Drawable chat_msgStickerViewsDrawable;
    public static Drawable chat_msgInViewsDrawable;
    public static Drawable chat_msgInViewsSelectedDrawable;
    public static Drawable chat_msgOutViewsDrawable;
    public static Drawable chat_msgOutViewsSelectedDrawable;
    public static Drawable chat_msgMediaViewsDrawable;
    public static Drawable chat_msgInMenuDrawable;
    public static Drawable chat_msgInMenuSelectedDrawable;
    public static Drawable chat_msgOutMenuDrawable;
    public static Drawable chat_msgOutMenuSelectedDrawable;
    public static Drawable chat_msgMediaMenuDrawable;
    public static Drawable chat_msgInInstantDrawable;
    public static Drawable chat_msgOutInstantDrawable;
    public static Drawable chat_msgErrorDrawable;
    public static Drawable chat_muteIconDrawable;
    public static Drawable chat_lockIconDrawable;
    public static Drawable chat_inlineResultFile;
    public static Drawable chat_inlineResultAudio;
    public static Drawable chat_inlineResultLocation;
    public static Drawable chat_redLocationIcon;
    public static Drawable chat_msgOutBroadcastDrawable;
    public static Drawable chat_msgMediaBroadcastDrawable;
    public static Drawable chat_msgOutLocationDrawable;
    public static Drawable chat_msgBroadcastDrawable;
    public static Drawable chat_msgBroadcastMediaDrawable;
    public static Drawable chat_contextResult_shadowUnderSwitchDrawable;
    public static Drawable chat_shareDrawable;
    public static Drawable chat_shareIconDrawable;
    public static Drawable chat_replyIconDrawable;
    public static Drawable chat_goIconDrawable;
    public static Drawable chat_botLinkDrawalbe;
    public static Drawable chat_botInlineDrawable;
    public static Drawable chat_systemDrawable;
    public static Drawable chat_msgInCallDrawable;
    public static Drawable chat_msgInCallSelectedDrawable;
    public static Drawable chat_msgOutCallDrawable;
    public static Drawable chat_msgOutCallSelectedDrawable;

    public static Drawable chat_msgCallUpGreenDrawable;
    public static Drawable chat_msgCallDownRedDrawable;
    public static Drawable chat_msgCallDownGreenDrawable;

    public static Drawable chat_msgAvatarLiveLocationDrawable;
    public static Drawable[] chat_attachButtonDrawables = new Drawable[10];
    public static Drawable[] chat_locationDrawable = new Drawable[2];
    public static Drawable[] chat_contactDrawable = new Drawable[2];
    public static Drawable[] chat_cornerOuter = new Drawable[4];
    public static Drawable[] chat_cornerInner = new Drawable[4];
    public static Drawable[][] chat_fileStatesDrawable = new Drawable[10][2];
//    public static CombinedDrawable[][] chat_fileMiniStatesDrawable = new CombinedDrawable[6][2];
    public static Drawable[][] chat_photoStatesDrawables = new Drawable[13][2];

    public static Drawable calllog_msgCallUpRedDrawable;
    public static Drawable calllog_msgCallUpGreenDrawable;
    public static Drawable calllog_msgCallDownRedDrawable;
    public static Drawable calllog_msgCallDownGreenDrawable;

    public static Drawable chat_fileIcon;
    public static Drawable chat_flameIcon;
    public static Drawable chat_gifIcon;

    public static final String key_dialogBackground = "dialogBackground";
    public static final String key_dialogBackgroundGray = "dialogBackgroundGray";
    public static final String key_dialogTextBlack = "dialogTextBlack";
    public static final String key_dialogTextLink = "dialogTextLink";
    public static final String key_dialogLinkSelection = "dialogLinkSelection";
    public static final String key_dialogTextRed = "dialogTextRed";
    public static final String key_dialogTextRed2 = "dialogTextRed2";
    public static final String key_dialogTextBlue = "dialogTextBlue";
    public static final String key_dialogTextBlue2 = "dialogTextBlue2";
    public static final String key_dialogTextBlue3 = "dialogTextBlue3";
    public static final String key_dialogTextBlue4 = "dialogTextBlue4";
    public static final String key_dialogTextGray = "dialogTextGray";
    public static final String key_dialogTextGray2 = "dialogTextGray2";
    public static final String key_dialogTextGray3 = "dialogTextGray3";
    public static final String key_dialogTextGray4 = "dialogTextGray4";
    public static final String key_dialogTextHint = "dialogTextHint";
    public static final String key_dialogInputField = "dialogInputField";
    public static final String key_dialogInputFieldActivated = "dialogInputFieldActivated";
    public static final String key_dialogCheckboxSquareBackground = "dialogCheckboxSquareBackground";
    public static final String key_dialogCheckboxSquareCheck = "dialogCheckboxSquareCheck";
    public static final String key_dialogCheckboxSquareUnchecked = "dialogCheckboxSquareUnchecked";
    public static final String key_dialogCheckboxSquareDisabled = "dialogCheckboxSquareDisabled";
    public static final String key_dialogScrollGlow = "dialogScrollGlow";
    public static final String key_dialogRoundCheckBox = "dialogRoundCheckBox";
    public static final String key_dialogRoundCheckBoxCheck = "dialogRoundCheckBoxCheck";
    public static final String key_dialogBadgeBackground = "dialogBadgeBackground";
    public static final String key_dialogBadgeText = "dialogBadgeText";
    public static final String key_dialogRadioBackground = "dialogRadioBackground";
    public static final String key_dialogRadioBackgroundChecked = "dialogRadioBackgroundChecked";
    public static final String key_dialogProgressCircle = "dialogProgressCircle";
    public static final String key_dialogLineProgress = "dialogLineProgress";
    public static final String key_dialogLineProgressBackground = "dialogLineProgressBackground";
    public static final String key_dialogButton = "dialogButton";
    public static final String key_dialogButtonSelector = "dialogButtonSelector";
    public static final String key_dialogIcon = "dialogIcon";
    public static final String key_dialogRedIcon = "dialogRedIcon";
    public static final String key_dialogGrayLine = "dialogGrayLine";
    public static final String key_dialogTopBackground = "dialogTopBackground";
    public static final String key_dialogCameraIcon = "dialogCameraIcon";
    public static final String key_dialog_inlineProgressBackground = "dialog_inlineProgressBackground";
    public static final String key_dialog_inlineProgress = "dialog_inlineProgress";
    public static final String key_dialogSearchBackground = "dialogSearchBackground";
    public static final String key_dialogSearchHint = "dialogSearchHint";
    public static final String key_dialogSearchIcon = "dialogSearchIcon";
    public static final String key_dialogSearchText = "dialogSearchText";
    public static final String key_dialogFloatingButton = "dialogFloatingButton";
    public static final String key_dialogFloatingButtonPressed = "dialogFloatingButtonPressed";
    public static final String key_dialogFloatingIcon = "dialogFloatingIcon";
    public static final String key_dialogShadowLine = "dialogShadowLine";

    public static final String key_windowBackgroundWhite = "windowBackgroundWhite";
    public static final String key_windowBackgroundUnchecked = "windowBackgroundUnchecked";
    public static final String key_windowBackgroundChecked = "windowBackgroundChecked";
    public static final String key_windowBackgroundCheckText = "windowBackgroundCheckText";
    public static final String key_progressCircle = "progressCircle";
    public static final String key_listSelector = "listSelectorSDK21";
    public static final String key_windowBackgroundWhiteInputField = "windowBackgroundWhiteInputField";
    public static final String key_windowBackgroundWhiteInputFieldActivated = "windowBackgroundWhiteInputFieldActivated";
    public static final String key_windowBackgroundWhiteGrayIcon = "windowBackgroundWhiteGrayIcon";
    public static final String key_windowBackgroundWhiteBlueText = "windowBackgroundWhiteBlueText";
    public static final String key_windowBackgroundWhiteBlueText2 = "windowBackgroundWhiteBlueText2";
    public static final String key_windowBackgroundWhiteBlueText3 = "windowBackgroundWhiteBlueText3";
    public static final String key_windowBackgroundWhiteBlueText4 = "windowBackgroundWhiteBlueText4";
    public static final String key_windowBackgroundWhiteBlueText5 = "windowBackgroundWhiteBlueText5";
    public static final String key_windowBackgroundWhiteBlueText6 = "windowBackgroundWhiteBlueText6";
    public static final String key_windowBackgroundWhiteBlueText7 = "windowBackgroundWhiteBlueText7";
    public static final String key_windowBackgroundWhiteBlueButton = "windowBackgroundWhiteBlueButton";
    public static final String key_windowBackgroundWhiteBlueIcon = "windowBackgroundWhiteBlueIcon";
    public static final String key_windowBackgroundWhiteGreenText = "windowBackgroundWhiteGreenText";
    public static final String key_windowBackgroundWhiteGreenText2 = "windowBackgroundWhiteGreenText2";
    public static final String key_windowBackgroundWhiteRedText = "windowBackgroundWhiteRedText";
    public static final String key_windowBackgroundWhiteRedText2 = "windowBackgroundWhiteRedText2";
    public static final String key_windowBackgroundWhiteRedText3 = "windowBackgroundWhiteRedText3";
    public static final String key_windowBackgroundWhiteRedText4 = "windowBackgroundWhiteRedText4";
    public static final String key_windowBackgroundWhiteRedText5 = "windowBackgroundWhiteRedText5";
    public static final String key_windowBackgroundWhiteRedText6 = "windowBackgroundWhiteRedText6";
    public static final String key_windowBackgroundWhiteGrayText = "windowBackgroundWhiteGrayText";
    public static final String key_windowBackgroundWhiteGrayText2 = "windowBackgroundWhiteGrayText2";
    public static final String key_windowBackgroundWhiteGrayText3 = "windowBackgroundWhiteGrayText3";
    public static final String key_windowBackgroundWhiteGrayText4 = "windowBackgroundWhiteGrayText4";
    public static final String key_windowBackgroundWhiteGrayText5 = "windowBackgroundWhiteGrayText5";
    public static final String key_windowBackgroundWhiteGrayText6 = "windowBackgroundWhiteGrayText6";
    public static final String key_windowBackgroundWhiteGrayText7 = "windowBackgroundWhiteGrayText7";
    public static final String key_windowBackgroundWhiteGrayText8 = "windowBackgroundWhiteGrayText8";
    public static final String key_windowBackgroundWhiteGrayLine = "windowBackgroundWhiteGrayLine";
    public static final String key_windowBackgroundWhiteBlackText = "windowBackgroundWhiteBlackText";
    public static final String key_windowBackgroundWhiteHintText = "windowBackgroundWhiteHintText";
    public static final String key_windowBackgroundWhiteValueText = "windowBackgroundWhiteValueText";
    public static final String key_windowBackgroundWhiteLinkText = "windowBackgroundWhiteLinkText";
    public static final String key_windowBackgroundWhiteLinkSelection = "windowBackgroundWhiteLinkSelection";
    public static final String key_windowBackgroundWhiteBlueHeader = "windowBackgroundWhiteBlueHeader";
    public static final String key_switchTrack = "switchTrack";
    public static final String key_switchTrackChecked = "switchTrackChecked";
    public static final String key_switchTrackBlue = "switchTrackBlue";
    public static final String key_switchTrackBlueChecked = "switchTrackBlueChecked";
    public static final String key_switchTrackBlueThumb = "switchTrackBlueThumb";
    public static final String key_switchTrackBlueThumbChecked = "switchTrackBlueThumbChecked";
    public static final String key_switchTrackBlueSelector = "switchTrackBlueSelector";
    public static final String key_switchTrackBlueSelectorChecked = "switchTrackBlueSelectorChecked";
    public static final String key_switch2Track = "switch2Track";
    public static final String key_switch2TrackChecked = "switch2TrackChecked";
    public static final String key_checkboxSquareBackground = "checkboxSquareBackground";
    public static final String key_checkboxSquareCheck = "checkboxSquareCheck";
    public static final String key_checkboxSquareUnchecked = "checkboxSquareUnchecked";
    public static final String key_checkboxSquareDisabled = "checkboxSquareDisabled";
    public static final String key_windowBackgroundGray = "windowBackgroundGray";
    public static final String key_windowBackgroundGrayShadow = "windowBackgroundGrayShadow";
    public static final String key_emptyListPlaceholder = "emptyListPlaceholder";
    public static final String key_divider = "divider";
    public static final String key_graySection = "graySection";
    public static final String key_graySectionText = "key_graySectionText";
    public static final String key_radioBackground = "radioBackground";
    public static final String key_radioBackgroundChecked = "radioBackgroundChecked";
    public static final String key_checkbox = "checkbox";
    public static final String key_checkboxDisabled = "checkboxDisabled";
    public static final String key_checkboxCheck = "checkboxCheck";
    public static final String key_fastScrollActive = "fastScrollActive";
    public static final String key_fastScrollInactive = "fastScrollInactive";
    public static final String key_fastScrollText = "fastScrollText";

    public static final String key_inappPlayerPerformer = "inappPlayerPerformer";
    public static final String key_inappPlayerTitle = "inappPlayerTitle";
    public static final String key_inappPlayerBackground = "inappPlayerBackground";
    public static final String key_inappPlayerPlayPause = "inappPlayerPlayPause";
    public static final String key_inappPlayerClose = "inappPlayerClose";

    public static final String key_returnToCallBackground = "returnToCallBackground";
    public static final String key_returnToCallText = "returnToCallText";

    public static final String key_contextProgressInner1 = "contextProgressInner1";
    public static final String key_contextProgressOuter1 = "contextProgressOuter1";
    public static final String key_contextProgressInner2 = "contextProgressInner2";
    public static final String key_contextProgressOuter2 = "contextProgressOuter2";
    public static final String key_contextProgressInner3 = "contextProgressInner3";
    public static final String key_contextProgressOuter3 = "contextProgressOuter3";
    public static final String key_contextProgressInner4 = "contextProgressInner4";
    public static final String key_contextProgressOuter4 = "contextProgressOuter4";

    public static final String key_avatar_text = "avatar_text";
    public static final String key_avatar_backgroundSaved = "avatar_backgroundSaved";
    public static final String key_avatar_backgroundArchived = "avatar_backgroundArchived";
    public static final String key_avatar_backgroundArchivedHidden = "avatar_backgroundArchivedHidden";
    public static final String key_avatar_backgroundRed = "avatar_backgroundRed";
    public static final String key_avatar_backgroundOrange = "avatar_backgroundOrange";
    public static final String key_avatar_backgroundViolet = "avatar_backgroundViolet";
    public static final String key_avatar_backgroundGreen = "avatar_backgroundGreen";
    public static final String key_avatar_backgroundCyan = "avatar_backgroundCyan";
    public static final String key_avatar_backgroundBlue = "avatar_backgroundBlue";
    public static final String key_avatar_backgroundPink = "avatar_backgroundPink";
    public static final String key_avatar_backgroundGroupCreateSpanBlue = "avatar_backgroundGroupCreateSpanBlue";

    public static final String key_avatar_backgroundInProfileBlue = "avatar_backgroundInProfileBlue";
    public static final String key_avatar_backgroundActionBarBlue = "avatar_backgroundActionBarBlue";
    public static final String key_avatar_actionBarSelectorBlue = "avatar_actionBarSelectorBlue";
    public static final String key_avatar_actionBarIconBlue = "avatar_actionBarIconBlue";
    public static final String key_avatar_subtitleInProfileBlue = "avatar_subtitleInProfileBlue";

    public static final String key_avatar_nameInMessageRed = "avatar_nameInMessageRed";
    public static final String key_avatar_nameInMessageOrange = "avatar_nameInMessageOrange";
    public static final String key_avatar_nameInMessageViolet = "avatar_nameInMessageViolet";
    public static final String key_avatar_nameInMessageGreen = "avatar_nameInMessageGreen";
    public static final String key_avatar_nameInMessageCyan = "avatar_nameInMessageCyan";
    public static final String key_avatar_nameInMessageBlue = "avatar_nameInMessageBlue";
    public static final String key_avatar_nameInMessagePink = "avatar_nameInMessagePink";

    public static String[] keys_avatar_background = {key_avatar_backgroundRed, key_avatar_backgroundOrange, key_avatar_backgroundViolet, key_avatar_backgroundGreen, key_avatar_backgroundCyan, key_avatar_backgroundBlue, key_avatar_backgroundPink};
    public static String[] keys_avatar_nameInMessage = {key_avatar_nameInMessageRed, key_avatar_nameInMessageOrange, key_avatar_nameInMessageViolet, key_avatar_nameInMessageGreen, key_avatar_nameInMessageCyan, key_avatar_nameInMessageBlue, key_avatar_nameInMessagePink};

    public static final String key_actionBarDefault = "actionBarDefault";
    public static final String key_actionBarDefaultSelector = "actionBarDefaultSelector";
    public static final String key_actionBarWhiteSelector = "actionBarWhiteSelector";
    public static final String key_actionBarDefaultIcon = "actionBarDefaultIcon";
    public static final String key_actionBarActionModeDefault = "actionBarActionModeDefault";
    public static final String key_actionBarActionModeDefaultTop = "actionBarActionModeDefaultTop";
    public static final String key_actionBarActionModeDefaultIcon = "actionBarActionModeDefaultIcon";
    public static final String key_actionBarActionModeDefaultSelector = "actionBarActionModeDefaultSelector";
    public static final String key_actionBarDefaultTitle = "actionBarDefaultTitle";
    public static final String key_actionBarDefaultSubtitle = "actionBarDefaultSubtitle";
    public static final String key_actionBarDefaultSearch = "actionBarDefaultSearch";
    public static final String key_actionBarDefaultSearchPlaceholder = "actionBarDefaultSearchPlaceholder";
    public static final String key_actionBarDefaultSubmenuItem = "actionBarDefaultSubmenuItem";
    public static final String key_actionBarDefaultSubmenuItemIcon = "actionBarDefaultSubmenuItemIcon";
    public static final String key_actionBarDefaultSubmenuBackground = "actionBarDefaultSubmenuBackground";
    public static final String key_actionBarTabActiveText = "actionBarTabActiveText";
    public static final String key_actionBarTabUnactiveText = "actionBarTabUnactiveText";
    public static final String key_actionBarTabLine = "actionBarTabLine";
    public static final String key_actionBarTabSelector = "actionBarTabSelector";
    public static final String key_actionBarDefaultArchived = "actionBarDefaultArchived";
    public static final String key_actionBarDefaultArchivedSelector = "actionBarDefaultArchivedSelector";
    public static final String key_actionBarDefaultArchivedIcon = "actionBarDefaultArchivedIcon";
    public static final String key_actionBarDefaultArchivedTitle = "actionBarDefaultArchivedTitle";
    public static final String key_actionBarDefaultArchivedSearch = "actionBarDefaultArchivedSearch";
    public static final String key_actionBarDefaultArchivedSearchPlaceholder = "actionBarDefaultSearchArchivedPlaceholder";

    public static final String key_chats_onlineCircle = "chats_onlineCircle";
    public static final String key_chats_unreadCounter = "chats_unreadCounter";
    public static final String key_chats_unreadCounterMuted = "chats_unreadCounterMuted";
    public static final String key_chats_unreadCounterText = "chats_unreadCounterText";
    public static final String key_chats_name = "chats_name";
    public static final String key_chats_nameArchived = "chats_nameArchived";
    public static final String key_chats_secretName = "chats_secretName";
    public static final String key_chats_secretIcon = "chats_secretIcon";
    public static final String key_chats_nameIcon = "chats_nameIcon";
    public static final String key_chats_pinnedIcon = "chats_pinnedIcon";
    public static final String key_chats_archiveBackground = "chats_archiveBackground";
    public static final String key_chats_archivePinBackground = "chats_archivePinBackground";
    public static final String key_chats_archiveIcon = "chats_archiveIcon";
    public static final String key_chats_archiveText = "chats_archiveText";
    public static final String key_chats_message = "chats_message";
    public static final String key_chats_messageArchived = "chats_messageArchived";
    public static final String key_chats_message_threeLines = "chats_message_threeLines";
    public static final String key_chats_draft = "chats_draft";
    public static final String key_chats_nameMessage = "chats_nameMessage";
    public static final String key_chats_nameMessageArchived = "chats_nameMessageArchived";
    public static final String key_chats_nameMessage_threeLines = "chats_nameMessage_threeLines";
    public static final String key_chats_nameMessageArchived_threeLines = "chats_nameMessageArchived_threeLines";
    public static final String key_chats_attachMessage = "chats_attachMessage";
    public static final String key_chats_actionMessage = "chats_actionMessage";
    public static final String key_chats_date = "chats_date";
    public static final String key_chats_pinnedOverlay = "chats_pinnedOverlay";
    public static final String key_chats_tabletSelectedOverlay = "chats_tabletSelectedOverlay";
    public static final String key_chats_sentCheck = "chats_sentCheck";
    public static final String key_chats_sentClock = "chats_sentClock";
    public static final String key_chats_sentError = "chats_sentError";
    public static final String key_chats_sentErrorIcon = "chats_sentErrorIcon";
    public static final String key_chats_verifiedBackground = "chats_verifiedBackground";
    public static final String key_chats_verifiedCheck = "chats_verifiedCheck";
    public static final String key_chats_muteIcon = "chats_muteIcon";
    public static final String key_chats_mentionIcon = "chats_mentionIcon";
    public static final String key_chats_menuTopShadow = "chats_menuTopShadow";
    public static final String key_chats_menuTopShadowCats = "chats_menuTopShadowCats";
    public static final String key_chats_menuBackground = "chats_menuBackground";
    public static final String key_chats_menuItemText = "chats_menuItemText";
    public static final String key_chats_menuItemCheck = "chats_menuItemCheck";
    public static final String key_chats_menuItemIcon = "chats_menuItemIcon";
    public static final String key_chats_menuName = "chats_menuName";
    public static final String key_chats_menuPhone = "chats_menuPhone";
    public static final String key_chats_menuPhoneCats = "chats_menuPhoneCats";
    public static final String key_chats_menuTopBackgroundCats = "chats_menuTopBackgroundCats";
    public static final String key_chats_menuTopBackground = "chats_menuTopBackground";
    public static final String key_chats_menuCloud = "chats_menuCloud";
    public static final String key_chats_menuCloudBackgroundCats = "chats_menuCloudBackgroundCats";
    public static final String key_chats_actionIcon = "chats_actionIcon";
    public static final String key_chats_actionBackground = "chats_actionBackground";
    public static final String key_chats_actionPressedBackground = "chats_actionPressedBackground";
    public static final String key_chats_actionUnreadIcon = "chats_actionUnreadIcon";
    public static final String key_chats_actionUnreadBackground = "chats_actionUnreadBackground";
    public static final String key_chats_actionUnreadPressedBackground = "chats_actionUnreadPressedBackground";

    public static final String key_chat_attachCameraIcon1 = "chat_attachCameraIcon1";
    public static final String key_chat_attachCameraIcon2 = "chat_attachCameraIcon2";
    public static final String key_chat_attachCameraIcon3 = "chat_attachCameraIcon3";
    public static final String key_chat_attachCameraIcon4 = "chat_attachCameraIcon4";
    public static final String key_chat_attachCameraIcon5 = "chat_attachCameraIcon5";
    public static final String key_chat_attachCameraIcon6 = "chat_attachCameraIcon6";
    public static final String key_chat_attachMediaBanBackground = "chat_attachMediaBanBackground";
    public static final String key_chat_attachMediaBanText = "chat_attachMediaBanText";

    public static final String key_chat_attachGalleryBackground = "chat_attachGalleryBackground";
    //public static final String key_chat_attachGalleryBackgroundPressed = "chat_attachGalleryBackgroundPressed";
    public static final String key_chat_attachGalleryIcon = "chat_attachGalleryIcon";
    public static final String key_chat_attachVideoBackground = "chat_attachVideoBackground";
    //public static final String key_chat_attachVideoBackgroundPressed = "chat_attachVideoBackgroundPressed";
    public static final String key_chat_attachVideoIcon = "chat_attachVideoIcon";
    public static final String key_chat_attachAudioBackground = "chat_attachAudioBackground";
    //public static final String key_chat_attachAudioBackgroundPressed = "chat_attachAudioBackgroundPressed";
    public static final String key_chat_attachAudioIcon = "chat_attachAudioIcon";
    public static final String key_chat_attachFileBackground = "chat_attachFileBackground";
    //public static final String key_chat_attachFileBackgroundPressed = "chat_attachFileBackgroundPressed";
    public static final String key_chat_attachFileIcon = "chat_attachFileIcon";
    public static final String key_chat_attachContactBackground = "chat_attachContactBackground";
    //public static final String key_chat_attachContactBackgroundPressed = "chat_attachContactBackgroundPressed";
    public static final String key_chat_attachContactIcon = "chat_attachContactIcon";
    public static final String key_chat_attachLocationBackground = "chat_attachLocationBackground";
    //public static final String key_chat_attachLocationBackgroundPressed = "chat_attachLocationBackgroundPressed";
    public static final String key_chat_attachLocationIcon = "chat_attachLocationIcon";
    public static final String key_chat_attachHideBackground = "chat_attachHideBackground";
    //public static final String key_chat_attachHideBackgroundPressed = "chat_attachHideBackgroundPressed";
    public static final String key_chat_attachHideIcon = "chat_attachHideIcon";
    public static final String key_chat_attachSendBackground = "chat_attachSendBackground";
    //public static final String key_chat_attachSendBackgroundPressed = "chat_attachSendBackgroundPressed";
    public static final String key_chat_attachSendIcon = "chat_attachSendIcon";
    public static final String key_chat_attachPollBackground = "chat_attachPollBackground";
    //public static final String key_chat_attachPollBackgroundPressed = "chat_attachPollBackgroundPressed";
    public static final String key_chat_attachPollIcon = "chat_attachPollIcon";

    public static final String key_chat_status = "chat_status";
    public static final String key_chat_inRedCall = "chat_inUpCall";
    public static final String key_chat_inGreenCall = "chat_inDownCall";
    public static final String key_chat_outGreenCall = "chat_outUpCall";
    public static final String key_chat_inBubble = "chat_inBubble";
    public static final String key_chat_inBubbleSelected = "chat_inBubbleSelected";
    public static final String key_chat_inBubbleShadow = "chat_inBubbleShadow";
    public static final String key_chat_outBubble = "chat_outBubble";
    public static final String key_chat_outBubbleSelected = "chat_outBubbleSelected";
    public static final String key_chat_outBubbleShadow = "chat_outBubbleShadow";
    public static final String key_chat_messageTextIn = "chat_messageTextIn";
    public static final String key_chat_messageTextOut = "chat_messageTextOut";
    public static final String key_chat_messageLinkIn = "chat_messageLinkIn";
    public static final String key_chat_messageLinkOut = "chat_messageLinkOut";
    public static final String key_chat_serviceText = "chat_serviceText";
    public static final String key_chat_serviceLink = "chat_serviceLink";
    public static final String key_chat_serviceIcon = "chat_serviceIcon";
    public static final String key_chat_serviceBackground = "chat_serviceBackground";
    public static final String key_chat_serviceBackgroundSelected = "chat_serviceBackgroundSelected";
    public static final String key_chat_shareBackground = "chat_shareBackground";
    public static final String key_chat_shareBackgroundSelected = "chat_shareBackgroundSelected";
    public static final String key_chat_muteIcon = "chat_muteIcon";
    public static final String key_chat_lockIcon = "chat_lockIcon";
    public static final String key_chat_outSentCheck = "chat_outSentCheck";
    public static final String key_chat_outSentCheckSelected = "chat_outSentCheckSelected";
    public static final String key_chat_outSentClock = "chat_outSentClock";
    public static final String key_chat_outSentClockSelected = "chat_outSentClockSelected";
    public static final String key_chat_inSentClock = "chat_inSentClock";
    public static final String key_chat_inSentClockSelected = "chat_inSentClockSelected";
    public static final String key_chat_mediaSentCheck = "chat_mediaSentCheck";
    public static final String key_chat_mediaSentClock = "chat_mediaSentClock";
    public static final String key_chat_inMediaIcon = "chat_inMediaIcon";
    public static final String key_chat_outMediaIcon = "chat_outMediaIcon";
    public static final String key_chat_inMediaIconSelected = "chat_inMediaIconSelected";
    public static final String key_chat_outMediaIconSelected = "chat_outMediaIconSelected";
    public static final String key_chat_mediaTimeBackground = "chat_mediaTimeBackground";
    public static final String key_chat_outViews = "chat_outViews";
    public static final String key_chat_outViewsSelected = "chat_outViewsSelected";
    public static final String key_chat_inViews = "chat_inViews";
    public static final String key_chat_inViewsSelected = "chat_inViewsSelected";
    public static final String key_chat_mediaViews = "chat_mediaViews";
    public static final String key_chat_outMenu = "chat_outMenu";
    public static final String key_chat_outMenuSelected = "chat_outMenuSelected";
    public static final String key_chat_inMenu = "chat_inMenu";
    public static final String key_chat_inMenuSelected = "chat_inMenuSelected";
    public static final String key_chat_mediaMenu = "chat_mediaMenu";
    public static final String key_chat_outInstant = "chat_outInstant";
    public static final String key_chat_outInstantSelected = "chat_outInstantSelected";
    public static final String key_chat_inInstant = "chat_inInstant";
    public static final String key_chat_inInstantSelected = "chat_inInstantSelected";
    public static final String key_chat_sentError = "chat_sentError";
    public static final String key_chat_sentErrorIcon = "chat_sentErrorIcon";
    public static final String key_chat_selectedBackground = "chat_selectedBackground";
    public static final String key_chat_previewDurationText = "chat_previewDurationText";
    public static final String key_chat_previewGameText = "chat_previewGameText";
    public static final String key_chat_inPreviewInstantText = "chat_inPreviewInstantText";
    public static final String key_chat_outPreviewInstantText = "chat_outPreviewInstantText";
    public static final String key_chat_inPreviewInstantSelectedText = "chat_inPreviewInstantSelectedText";
    public static final String key_chat_outPreviewInstantSelectedText = "chat_outPreviewInstantSelectedText";
    public static final String key_chat_secretTimeText = "chat_secretTimeText";
    public static final String key_chat_stickerNameText = "chat_stickerNameText";
    public static final String key_chat_botButtonText = "chat_botButtonText";
    public static final String key_chat_botProgress = "chat_botProgress";
    public static final String key_chat_inForwardedNameText = "chat_inForwardedNameText";
    public static final String key_chat_outForwardedNameText = "chat_outForwardedNameText";
    public static final String key_chat_inViaBotNameText = "chat_inViaBotNameText";
    public static final String key_chat_outViaBotNameText = "chat_outViaBotNameText";
    public static final String key_chat_stickerViaBotNameText = "chat_stickerViaBotNameText";
    public static final String key_chat_inReplyLine = "chat_inReplyLine";
    public static final String key_chat_outReplyLine = "chat_outReplyLine";
    public static final String key_chat_stickerReplyLine = "chat_stickerReplyLine";
    public static final String key_chat_inReplyNameText = "chat_inReplyNameText";
    public static final String key_chat_outReplyNameText = "chat_outReplyNameText";
    public static final String key_chat_stickerReplyNameText = "chat_stickerReplyNameText";
    public static final String key_chat_inReplyMessageText = "chat_inReplyMessageText";
    public static final String key_chat_outReplyMessageText = "chat_outReplyMessageText";
    public static final String key_chat_inReplyMediaMessageText = "chat_inReplyMediaMessageText";
    public static final String key_chat_outReplyMediaMessageText = "chat_outReplyMediaMessageText";
    public static final String key_chat_inReplyMediaMessageSelectedText = "chat_inReplyMediaMessageSelectedText";
    public static final String key_chat_outReplyMediaMessageSelectedText = "chat_outReplyMediaMessageSelectedText";
    public static final String key_chat_stickerReplyMessageText = "chat_stickerReplyMessageText";
    public static final String key_chat_inPreviewLine = "chat_inPreviewLine";
    public static final String key_chat_outPreviewLine = "chat_outPreviewLine";
    public static final String key_chat_inSiteNameText = "chat_inSiteNameText";
    public static final String key_chat_outSiteNameText = "chat_outSiteNameText";
    public static final String key_chat_inContactNameText = "chat_inContactNameText";
    public static final String key_chat_outContactNameText = "chat_outContactNameText";
    public static final String key_chat_inContactPhoneText = "chat_inContactPhoneText";
    public static final String key_chat_inContactPhoneSelectedText = "chat_inContactPhoneSelectedText";
    public static final String key_chat_outContactPhoneText = "chat_outContactPhoneText";
    public static final String key_chat_outContactPhoneSelectedText = "chat_outContactPhoneSelectedText";
    public static final String key_chat_mediaProgress = "chat_mediaProgress";
    public static final String key_chat_inAudioProgress = "chat_inAudioProgress";
    public static final String key_chat_outAudioProgress = "chat_outAudioProgress";
    public static final String key_chat_inAudioSelectedProgress = "chat_inAudioSelectedProgress";
    public static final String key_chat_outAudioSelectedProgress = "chat_outAudioSelectedProgress";
    public static final String key_chat_mediaTimeText = "chat_mediaTimeText";
    public static final String key_chat_adminText = "chat_adminText";
    public static final String key_chat_adminSelectedText = "chat_adminSelectedText";
    public static final String key_chat_inTimeText = "chat_inTimeText";
    public static final String key_chat_outTimeText = "chat_outTimeText";
    public static final String key_chat_inTimeSelectedText = "chat_inTimeSelectedText";
    public static final String key_chat_outTimeSelectedText = "chat_outTimeSelectedText";
    public static final String key_chat_inAudioPerformerText = "chat_inAudioPerfomerText";
    public static final String key_chat_inAudioPerformerSelectedText = "chat_inAudioPerfomerSelectedText";
    public static final String key_chat_outAudioPerformerText = "chat_outAudioPerfomerText";
    public static final String key_chat_outAudioPerformerSelectedText = "chat_outAudioPerfomerSelectedText";
    public static final String key_chat_inAudioTitleText = "chat_inAudioTitleText";
    public static final String key_chat_outAudioTitleText = "chat_outAudioTitleText";
    public static final String key_chat_inAudioDurationText = "chat_inAudioDurationText";
    public static final String key_chat_outAudioDurationText = "chat_outAudioDurationText";
    public static final String key_chat_inAudioDurationSelectedText = "chat_inAudioDurationSelectedText";
    public static final String key_chat_outAudioDurationSelectedText = "chat_outAudioDurationSelectedText";
    public static final String key_chat_inAudioSeekbar = "chat_inAudioSeekbar";
    public static final String key_chat_inAudioCacheSeekbar = "chat_inAudioCacheSeekbar";
    public static final String key_chat_outAudioSeekbar = "chat_outAudioSeekbar";
    public static final String key_chat_outAudioCacheSeekbar = "chat_outAudioCacheSeekbar";
    public static final String key_chat_inAudioSeekbarSelected = "chat_inAudioSeekbarSelected";
    public static final String key_chat_outAudioSeekbarSelected = "chat_outAudioSeekbarSelected";
    public static final String key_chat_inAudioSeekbarFill = "chat_inAudioSeekbarFill";
    public static final String key_chat_outAudioSeekbarFill = "chat_outAudioSeekbarFill";
    public static final String key_chat_inVoiceSeekbar = "chat_inVoiceSeekbar";
    public static final String key_chat_outVoiceSeekbar = "chat_outVoiceSeekbar";
    public static final String key_chat_inVoiceSeekbarSelected = "chat_inVoiceSeekbarSelected";
    public static final String key_chat_outVoiceSeekbarSelected = "chat_outVoiceSeekbarSelected";
    public static final String key_chat_inVoiceSeekbarFill = "chat_inVoiceSeekbarFill";
    public static final String key_chat_outVoiceSeekbarFill = "chat_outVoiceSeekbarFill";
    public static final String key_chat_inFileProgress = "chat_inFileProgress";
    public static final String key_chat_outFileProgress = "chat_outFileProgress";
    public static final String key_chat_inFileProgressSelected = "chat_inFileProgressSelected";
    public static final String key_chat_outFileProgressSelected = "chat_outFileProgressSelected";
    public static final String key_chat_inFileNameText = "chat_inFileNameText";
    public static final String key_chat_outFileNameText = "chat_outFileNameText";
    public static final String key_chat_inFileInfoText = "chat_inFileInfoText";
    public static final String key_chat_outFileInfoText = "chat_outFileInfoText";
    public static final String key_chat_inFileInfoSelectedText = "chat_inFileInfoSelectedText";
    public static final String key_chat_outFileInfoSelectedText = "chat_outFileInfoSelectedText";
    public static final String key_chat_inFileBackground = "chat_inFileBackground";
    public static final String key_chat_outFileBackground = "chat_outFileBackground";
    public static final String key_chat_inFileBackgroundSelected = "chat_inFileBackgroundSelected";
    public static final String key_chat_outFileBackgroundSelected = "chat_outFileBackgroundSelected";
    public static final String key_chat_inVenueInfoText = "chat_inVenueInfoText";
    public static final String key_chat_outVenueInfoText = "chat_outVenueInfoText";
    public static final String key_chat_inVenueInfoSelectedText = "chat_inVenueInfoSelectedText";
    public static final String key_chat_outVenueInfoSelectedText = "chat_outVenueInfoSelectedText";
    public static final String key_chat_mediaInfoText = "chat_mediaInfoText";
    public static final String key_chat_linkSelectBackground = "chat_linkSelectBackground";
    public static final String key_chat_textSelectBackground = "chat_textSelectBackground";
    public static final String key_chat_wallpaper = "chat_wallpaper";
    public static final String key_chat_messagePanelBackground = "chat_messagePanelBackground";
    public static final String key_chat_messagePanelShadow = "chat_messagePanelShadow";
    public static final String key_chat_messagePanelText = "chat_messagePanelText";
    public static final String key_chat_messagePanelHint = "chat_messagePanelHint";
    public static final String key_chat_messagePanelIcons = "chat_messagePanelIcons";
    public static final String key_chat_messagePanelSend = "chat_messagePanelSend";
    public static final String key_chat_messagePanelVoiceLock = "key_chat_messagePanelVoiceLock";
    public static final String key_chat_messagePanelVoiceLockBackground = "key_chat_messagePanelVoiceLockBackground";
    public static final String key_chat_messagePanelVoiceLockShadow = "key_chat_messagePanelVoiceLockShadow";
    public static final String key_chat_topPanelBackground = "chat_topPanelBackground";
    public static final String key_chat_topPanelClose = "chat_topPanelClose";
    public static final String key_chat_topPanelLine = "chat_topPanelLine";
    public static final String key_chat_topPanelTitle = "chat_topPanelTitle";
    public static final String key_chat_topPanelMessage = "chat_topPanelMessage";
    public static final String key_chat_reportSpam = "chat_reportSpam";
    public static final String key_chat_addContact = "chat_addContact";
    public static final String key_chat_inLoader = "chat_inLoader";
    public static final String key_chat_inLoaderSelected = "chat_inLoaderSelected";
    public static final String key_chat_outLoader = "chat_outLoader";
    public static final String key_chat_outLoaderSelected = "chat_outLoaderSelected";
    public static final String key_chat_inLoaderPhoto = "chat_inLoaderPhoto";
    public static final String key_chat_inLoaderPhotoSelected = "chat_inLoaderPhotoSelected";
    public static final String key_chat_inLoaderPhotoIcon = "chat_inLoaderPhotoIcon";
    public static final String key_chat_inLoaderPhotoIconSelected = "chat_inLoaderPhotoIconSelected";
    public static final String key_chat_outLoaderPhoto = "chat_outLoaderPhoto";
    public static final String key_chat_outLoaderPhotoSelected = "chat_outLoaderPhotoSelected";
    public static final String key_chat_outLoaderPhotoIcon = "chat_outLoaderPhotoIcon";
    public static final String key_chat_outLoaderPhotoIconSelected = "chat_outLoaderPhotoIconSelected";
    public static final String key_chat_mediaLoaderPhoto = "chat_mediaLoaderPhoto";
    public static final String key_chat_mediaLoaderPhotoSelected = "chat_mediaLoaderPhotoSelected";
    public static final String key_chat_mediaLoaderPhotoIcon = "chat_mediaLoaderPhotoIcon";
    public static final String key_chat_mediaLoaderPhotoIconSelected = "chat_mediaLoaderPhotoIconSelected";
    public static final String key_chat_inLocationBackground = "chat_inLocationBackground";
    public static final String key_chat_inLocationIcon = "chat_inLocationIcon";
    public static final String key_chat_outLocationBackground = "chat_outLocationBackground";
    public static final String key_chat_outLocationIcon = "chat_outLocationIcon";
    public static final String key_chat_inContactBackground = "chat_inContactBackground";
    public static final String key_chat_inContactIcon = "chat_inContactIcon";
    public static final String key_chat_outContactBackground = "chat_outContactBackground";
    public static final String key_chat_outContactIcon = "chat_outContactIcon";
    public static final String key_chat_inFileIcon = "chat_inFileIcon";
    public static final String key_chat_inFileSelectedIcon = "chat_inFileSelectedIcon";
    public static final String key_chat_outFileIcon = "chat_outFileIcon";
    public static final String key_chat_outFileSelectedIcon = "chat_outFileSelectedIcon";
    public static final String key_chat_replyPanelIcons = "chat_replyPanelIcons";
    public static final String key_chat_replyPanelClose = "chat_replyPanelClose";
    public static final String key_chat_replyPanelName = "chat_replyPanelName";
    public static final String key_chat_replyPanelMessage = "chat_replyPanelMessage";
    public static final String key_chat_replyPanelLine = "chat_replyPanelLine";
    public static final String key_chat_searchPanelIcons = "chat_searchPanelIcons";
    public static final String key_chat_searchPanelText = "chat_searchPanelText";
    public static final String key_chat_secretChatStatusText = "chat_secretChatStatusText";
    public static final String key_chat_fieldOverlayText = "chat_fieldOverlayText";
    public static final String key_chat_stickersHintPanel = "chat_stickersHintPanel";
    public static final String key_chat_botSwitchToInlineText = "chat_botSwitchToInlineText";
    public static final String key_chat_unreadMessagesStartArrowIcon = "chat_unreadMessagesStartArrowIcon";
    public static final String key_chat_unreadMessagesStartText = "chat_unreadMessagesStartText";
    public static final String key_chat_unreadMessagesStartBackground = "chat_unreadMessagesStartBackground";
    public static final String key_chat_inlineResultIcon = "chat_inlineResultIcon";
    public static final String key_chat_emojiPanelBackground = "chat_emojiPanelBackground";
    public static final String key_chat_emojiPanelBadgeBackground = "chat_emojiPanelBadgeBackground";
    public static final String key_chat_emojiPanelBadgeText = "chat_emojiPanelBadgeText";
    public static final String key_chat_emojiSearchBackground = "chat_emojiSearchBackground";
    public static final String key_chat_emojiSearchIcon = "chat_emojiSearchIcon";
    public static final String key_chat_emojiPanelShadowLine = "chat_emojiPanelShadowLine";
    public static final String key_chat_emojiPanelEmptyText = "chat_emojiPanelEmptyText";
    public static final String key_chat_emojiPanelIcon = "chat_emojiPanelIcon";
    public static final String key_chat_emojiBottomPanelIcon = "chat_emojiBottomPanelIcon";
    public static final String key_chat_emojiPanelIconSelected = "chat_emojiPanelIconSelected";
    public static final String key_chat_emojiPanelStickerPackSelector = "chat_emojiPanelStickerPackSelector";
    public static final String key_chat_emojiPanelStickerPackSelectorLine = "chat_emojiPanelStickerPackSelectorLine";
    public static final String key_chat_emojiPanelBackspace = "chat_emojiPanelBackspace";
    public static final String key_chat_emojiPanelMasksIcon = "chat_emojiPanelMasksIcon";
    public static final String key_chat_emojiPanelMasksIconSelected = "chat_emojiPanelMasksIconSelected";
    public static final String key_chat_emojiPanelTrendingTitle = "chat_emojiPanelTrendingTitle";
    public static final String key_chat_emojiPanelStickerSetName = "chat_emojiPanelStickerSetName";
    public static final String key_chat_emojiPanelStickerSetNameHighlight = "chat_emojiPanelStickerSetNameHighlight";
    public static final String key_chat_emojiPanelStickerSetNameIcon = "chat_emojiPanelStickerSetNameIcon";
    public static final String key_chat_emojiPanelTrendingDescription = "chat_emojiPanelTrendingDescription";
    public static final String key_chat_botKeyboardButtonText = "chat_botKeyboardButtonText";
    public static final String key_chat_botKeyboardButtonBackground = "chat_botKeyboardButtonBackground";
    public static final String key_chat_botKeyboardButtonBackgroundPressed = "chat_botKeyboardButtonBackgroundPressed";
    public static final String key_chat_emojiPanelNewTrending = "chat_emojiPanelNewTrending";
    public static final String key_chat_messagePanelVoicePressed = "chat_messagePanelVoicePressed";
    public static final String key_chat_messagePanelVoiceBackground = "chat_messagePanelVoiceBackground";
    public static final String key_chat_messagePanelVoiceShadow = "chat_messagePanelVoiceShadow";
    public static final String key_chat_messagePanelVoiceDelete = "chat_messagePanelVoiceDelete";
    public static final String key_chat_messagePanelVoiceDuration = "chat_messagePanelVoiceDuration";
    public static final String key_chat_recordedVoicePlayPause = "chat_recordedVoicePlayPause";
    public static final String key_chat_recordedVoicePlayPausePressed = "chat_recordedVoicePlayPausePressed";
    public static final String key_chat_recordedVoiceProgress = "chat_recordedVoiceProgress";
    public static final String key_chat_recordedVoiceProgressInner = "chat_recordedVoiceProgressInner";
    public static final String key_chat_recordedVoiceDot = "chat_recordedVoiceDot";
    public static final String key_chat_recordedVoiceBackground = "chat_recordedVoiceBackground";
    public static final String key_chat_recordVoiceCancel = "chat_recordVoiceCancel";
    public static final String key_chat_recordTime = "chat_recordTime";
    public static final String key_chat_messagePanelCancelInlineBot = "chat_messagePanelCancelInlineBot";
    public static final String key_chat_gifSaveHintText = "chat_gifSaveHintText";
    public static final String key_chat_gifSaveHintBackground = "chat_gifSaveHintBackground";
    public static final String key_chat_goDownButton = "chat_goDownButton";
    public static final String key_chat_goDownButtonShadow = "chat_goDownButtonShadow";
    public static final String key_chat_goDownButtonIcon = "chat_goDownButtonIcon";
    public static final String key_chat_goDownButtonCounter = "chat_goDownButtonCounter";
    public static final String key_chat_goDownButtonCounterBackground = "chat_goDownButtonCounterBackground";
    public static final String key_chat_secretTimerBackground = "chat_secretTimerBackground";
    public static final String key_chat_secretTimerText = "chat_secretTimerText";

    public static final String key_passport_authorizeBackground = "passport_authorizeBackground";
    public static final String key_passport_authorizeBackgroundSelected = "passport_authorizeBackgroundSelected";
    public static final String key_passport_authorizeText = "passport_authorizeText";

    public static final String key_profile_creatorIcon = "profile_creatorIcon";
    public static final String key_profile_title = "profile_title";
    public static final String key_profile_actionIcon = "profile_actionIcon";
    public static final String key_profile_actionBackground = "profile_actionBackground";
    public static final String key_profile_actionPressedBackground = "profile_actionPressedBackground";
    public static final String key_profile_verifiedBackground = "profile_verifiedBackground";
    public static final String key_profile_verifiedCheck = "profile_verifiedCheck";
    public static final String key_profile_status = "profile_status";

    public static final String key_sharedMedia_startStopLoadIcon = "sharedMedia_startStopLoadIcon";
    public static final String key_sharedMedia_linkPlaceholder = "sharedMedia_linkPlaceholder";
    public static final String key_sharedMedia_linkPlaceholderText = "sharedMedia_linkPlaceholderText";
    public static final String key_sharedMedia_photoPlaceholder = "sharedMedia_photoPlaceholder";
    public static final String key_sharedMedia_actionMode = "sharedMedia_actionMode";

    public static final String key_featuredStickers_addedIcon = "featuredStickers_addedIcon";
    public static final String key_featuredStickers_buttonProgress = "featuredStickers_buttonProgress";
    public static final String key_featuredStickers_addButton = "featuredStickers_addButton";
    public static final String key_featuredStickers_addButtonPressed = "featuredStickers_addButtonPressed";
    public static final String key_featuredStickers_delButton = "featuredStickers_delButton";
    public static final String key_featuredStickers_delButtonPressed = "featuredStickers_delButtonPressed";
    public static final String key_featuredStickers_buttonText = "featuredStickers_buttonText";
    public static final String key_featuredStickers_unread = "featuredStickers_unread";

    public static final String key_stickers_menu = "stickers_menu";
    public static final String key_stickers_menuSelector = "stickers_menuSelector";

    public static final String key_changephoneinfo_image = "changephoneinfo_image";
    public static final String key_changephoneinfo_changeText = "key_changephoneinfo_changeText";

    public static final String key_groupcreate_hintText = "groupcreate_hintText";
    public static final String key_groupcreate_cursor = "groupcreate_cursor";
    public static final String key_groupcreate_sectionShadow = "groupcreate_sectionShadow";
    public static final String key_groupcreate_sectionText = "groupcreate_sectionText";
    public static final String key_groupcreate_spanText = "groupcreate_spanText";
    public static final String key_groupcreate_spanBackground = "groupcreate_spanBackground";
    public static final String key_groupcreate_spanDelete = "groupcreate_spanDelete";

    public static final String key_contacts_inviteBackground = "contacts_inviteBackground";
    public static final String key_contacts_inviteText = "contacts_inviteText";

    public static final String key_login_progressInner = "login_progressInner";
    public static final String key_login_progressOuter = "login_progressOuter";

    public static final String key_musicPicker_checkbox = "musicPicker_checkbox";
    public static final String key_musicPicker_checkboxCheck = "musicPicker_checkboxCheck";
    public static final String key_musicPicker_buttonBackground = "musicPicker_buttonBackground";
    public static final String key_musicPicker_buttonIcon = "musicPicker_buttonIcon";

    public static final String key_picker_enabledButton = "picker_enabledButton";
    public static final String key_picker_disabledButton = "picker_disabledButton";
    public static final String key_picker_badge = "picker_badge";
    public static final String key_picker_badgeText = "picker_badgeText";

    public static final String key_location_markerX = "location_markerX";
    public static final String key_location_sendLocationBackground = "location_sendLocationBackground";
    public static final String key_location_sendLiveLocationBackground = "location_sendLiveLocationBackground";
    public static final String key_location_sendLocationIcon = "location_sendLocationIcon";
    public static final String key_location_sendLiveLocationIcon = "location_sendLiveLocationIcon";
    public static final String key_location_liveLocationProgress = "location_liveLocationProgress";
    public static final String key_location_placeLocationBackground = "location_placeLocationBackground";
    public static final String key_dialog_liveLocationProgress = "dialog_liveLocationProgress";

    public static final String key_files_folderIcon = "files_folderIcon";
    public static final String key_files_folderIconBackground = "files_folderIconBackground";
    public static final String key_files_iconText = "files_iconText";

    public static final String key_sessions_devicesImage = "sessions_devicesImage";

    public static final String key_calls_callReceivedGreenIcon = "calls_callReceivedGreenIcon";
    public static final String key_calls_callReceivedRedIcon = "calls_callReceivedRedIcon";

    public static final String key_undo_background = "undo_background";
    public static final String key_undo_cancelColor = "undo_cancelColor";
    public static final String key_undo_infoColor = "undo_infoColor";

    public static final String key_sheet_scrollUp = "key_sheet_scrollUp";
    public static final String key_sheet_other = "key_sheet_other";

    //ununsed
    public static final String key_chat_outBroadcast = "chat_outBroadcast";
    public static final String key_chat_mediaBroadcast = "chat_mediaBroadcast";

    public static final String key_player_actionBar = "player_actionBar";
    public static final String key_player_actionBarSelector = "player_actionBarSelector";
    public static final String key_player_actionBarTitle = "player_actionBarTitle";
    public static final String key_player_actionBarTop = "player_actionBarTop";
    public static final String key_player_actionBarSubtitle = "player_actionBarSubtitle";
    public static final String key_player_actionBarItems = "player_actionBarItems";
    public static final String key_player_background = "player_background";
    public static final String key_player_time = "player_time";
    public static final String key_player_progressBackground = "player_progressBackground";
    public static final String key_player_progressCachedBackground = "key_player_progressCachedBackground";
    public static final String key_player_progress = "player_progress";
    public static final String key_player_placeholder = "player_placeholder";
    public static final String key_player_placeholderBackground = "player_placeholderBackground";
    public static final String key_player_button = "player_button";
    public static final String key_player_buttonActive = "player_buttonActive";

    private static HashMap<String, Integer> defaultColors = new HashMap<>();
    private static HashMap<String, String> fallbackKeys = new HashMap<>();
    private static HashMap<String, Integer> currentColors;
    private static HashMap<String, Integer> animatingColors;

    static {
        defaultColors.put(key_dialogBackground, 0xffffffff);
        defaultColors.put(key_dialogBackgroundGray, 0xfff0f0f0);
        defaultColors.put(key_dialogTextBlack, 0xff222222);
        defaultColors.put(key_dialogTextLink, 0xff2678b6);
        defaultColors.put(key_dialogLinkSelection, 0x3362a9e3);
        defaultColors.put(key_dialogTextRed, 0xffcd5a5a);
        defaultColors.put(key_dialogTextRed2, 0xffde3a3a);
        defaultColors.put(key_dialogTextBlue, 0xff2f8cc9);
        defaultColors.put(key_dialogTextBlue2, 0xff3a95d5);
        defaultColors.put(key_dialogTextBlue3, 0xff3ec1f9);
        defaultColors.put(key_dialogTextBlue4, 0xff19a7e8);
        defaultColors.put(key_dialogTextGray, 0xff348bc1);
        defaultColors.put(key_dialogTextGray2, 0xff757575);
        defaultColors.put(key_dialogTextGray3, 0xff999999);
        defaultColors.put(key_dialogTextGray4, 0xffb3b3b3);
        defaultColors.put(key_dialogTextHint, 0xff979797);
        defaultColors.put(key_dialogIcon, 0xff676b70);
        defaultColors.put(key_dialogRedIcon, 0xffe14d4d);
        defaultColors.put(key_dialogGrayLine, 0xffd2d2d2);
        defaultColors.put(key_dialogTopBackground, 0xff6fb2e5);
        defaultColors.put(key_dialogInputField, 0xffdbdbdb);
        defaultColors.put(key_dialogInputFieldActivated, 0xff37a9f0);
        defaultColors.put(key_dialogCheckboxSquareBackground, 0xff43a0df);
        defaultColors.put(key_dialogCheckboxSquareCheck, 0xffffffff);
        defaultColors.put(key_dialogCheckboxSquareUnchecked, 0xff737373);
        defaultColors.put(key_dialogCheckboxSquareDisabled, 0xffb0b0b0);
        defaultColors.put(key_dialogRadioBackground, 0xffb3b3b3);
        defaultColors.put(key_dialogRadioBackgroundChecked, 0xff37a9f0);
        defaultColors.put(key_dialogProgressCircle, 0xff527da3);
        defaultColors.put(key_dialogLineProgress, 0xff527da3);
        defaultColors.put(key_dialogLineProgressBackground, 0xffdbdbdb);
        defaultColors.put(key_dialogButton, 0xff4991cc);
        defaultColors.put(key_dialogButtonSelector, 0x0f000000);
        defaultColors.put(key_dialogScrollGlow, 0xfff5f6f7);
        defaultColors.put(key_dialogRoundCheckBox, 0xff4cb4f5);
        defaultColors.put(key_dialogRoundCheckBoxCheck, 0xffffffff);
        defaultColors.put(key_dialogBadgeBackground, 0xff3ec1f9);
        defaultColors.put(key_dialogBadgeText, 0xffffffff);
        defaultColors.put(key_dialogCameraIcon, 0xffffffff);
        defaultColors.put(key_dialog_inlineProgressBackground, 0xf6f0f2f5);
        defaultColors.put(key_dialog_inlineProgress, 0xff6b7378);
        defaultColors.put(key_dialogSearchBackground, 0xfff2f4f5);
        defaultColors.put(key_dialogSearchHint, 0xff98a0a7);
        defaultColors.put(key_dialogSearchIcon, 0xffa1a8af);
        defaultColors.put(key_dialogSearchText, 0xff222222);
        defaultColors.put(key_dialogFloatingButton, 0xff4cb4f5);
        defaultColors.put(key_dialogFloatingButtonPressed, 0xff4cb4f5);
        defaultColors.put(key_dialogFloatingIcon, 0xffffffff);
        defaultColors.put(key_dialogShadowLine, 0x12000000);

        defaultColors.put(key_windowBackgroundWhite, 0xffffffff);
        defaultColors.put(key_windowBackgroundUnchecked, 0xff9da7b1);
        defaultColors.put(key_windowBackgroundChecked, 0xff579ed9);
        defaultColors.put(key_windowBackgroundCheckText, 0xffffffff);
        defaultColors.put(key_progressCircle, 0xff527da3);
        defaultColors.put(key_windowBackgroundWhiteGrayIcon, 0xff81868b);
        defaultColors.put(key_windowBackgroundWhiteBlueText, 0xff4092cd);
        defaultColors.put(key_windowBackgroundWhiteBlueText2, 0xff3a95d5);
        defaultColors.put(key_windowBackgroundWhiteBlueText3, 0xff2678b6);
        defaultColors.put(key_windowBackgroundWhiteBlueText4, 0xff4d83b3);
        defaultColors.put(key_windowBackgroundWhiteBlueText5, 0xff4c8eca);
        defaultColors.put(key_windowBackgroundWhiteBlueText6, 0xff3a8ccf);
        defaultColors.put(key_windowBackgroundWhiteBlueText7, 0xff377aae);
        defaultColors.put(key_windowBackgroundWhiteBlueButton, 0xff1e88d3);
        defaultColors.put(key_windowBackgroundWhiteBlueIcon, 0xff379de5);
        defaultColors.put(key_windowBackgroundWhiteGreenText, 0xff26972c);
        defaultColors.put(key_windowBackgroundWhiteGreenText2, 0xff37a818);
        defaultColors.put(key_windowBackgroundWhiteRedText, 0xffcd5a5a);
        defaultColors.put(key_windowBackgroundWhiteRedText2, 0xffdb5151);
        defaultColors.put(key_windowBackgroundWhiteRedText3, 0xffd24949);
        defaultColors.put(key_windowBackgroundWhiteRedText4, 0xffcf3030);
        defaultColors.put(key_windowBackgroundWhiteRedText5, 0xffed3939);
        defaultColors.put(key_windowBackgroundWhiteRedText6, 0xffff6666);
        defaultColors.put(key_windowBackgroundWhiteGrayText, 0xff838c96);
        defaultColors.put(key_windowBackgroundWhiteGrayText2, 0xff8a8a8a);
        defaultColors.put(key_windowBackgroundWhiteGrayText3, 0xff999999);
        defaultColors.put(key_windowBackgroundWhiteGrayText4, 0xff808080);
        defaultColors.put(key_windowBackgroundWhiteGrayText5, 0xffa3a3a3);
        defaultColors.put(key_windowBackgroundWhiteGrayText6, 0xff757575);
        defaultColors.put(key_windowBackgroundWhiteGrayText7, 0xffc6c6c6);
        defaultColors.put(key_windowBackgroundWhiteGrayText8, 0xff6d6d72);
        defaultColors.put(key_windowBackgroundWhiteGrayLine, 0xffdbdbdb);
        defaultColors.put(key_windowBackgroundWhiteBlackText, 0xff222222);
        defaultColors.put(key_windowBackgroundWhiteHintText, 0xffa8a8a8);
        defaultColors.put(key_windowBackgroundWhiteValueText, 0xff3a95d5);
        defaultColors.put(key_windowBackgroundWhiteLinkText, 0xff2678b6);
        defaultColors.put(key_windowBackgroundWhiteLinkSelection, 0x3362a9e3);
        defaultColors.put(key_windowBackgroundWhiteBlueHeader, 0xff3a95d5);
        defaultColors.put(key_windowBackgroundWhiteInputField, 0xffdbdbdb);
        defaultColors.put(key_windowBackgroundWhiteInputFieldActivated, 0xff37a9f0);
        defaultColors.put(key_switchTrack, 0xffb0b5ba);
        defaultColors.put(key_switchTrackChecked, 0xff52ade9);
        defaultColors.put(key_switchTrackBlue, 0xff828e99);
        defaultColors.put(key_switchTrackBlueChecked, 0xff3c88c7);
        defaultColors.put(key_switchTrackBlueThumb, 0xffffffff);
        defaultColors.put(key_switchTrackBlueThumbChecked, 0xffffffff);
        defaultColors.put(key_switchTrackBlueSelector, 0x17404a53);
        defaultColors.put(key_switchTrackBlueSelectorChecked, 0x21024781);
        defaultColors.put(key_switch2Track, 0xfff57e7e);
        defaultColors.put(key_switch2TrackChecked, 0xff52ade9);
        defaultColors.put(key_checkboxSquareBackground, 0xff43a0df);
        defaultColors.put(key_checkboxSquareCheck, 0xffffffff);
        defaultColors.put(key_checkboxSquareUnchecked, 0xff737373);
        defaultColors.put(key_checkboxSquareDisabled, 0xffb0b0b0);
        defaultColors.put(key_listSelector, 0x0f000000);
        defaultColors.put(key_radioBackground, 0xffb3b3b3);
        defaultColors.put(key_radioBackgroundChecked, 0xff37a9f0);
        defaultColors.put(key_windowBackgroundGray, 0xfff0f0f0);
        defaultColors.put(key_windowBackgroundGrayShadow, 0xff000000);
        defaultColors.put(key_emptyListPlaceholder, 0xff959595);
        defaultColors.put(key_divider, 0xffd9d9d9);
        defaultColors.put(key_graySection, 0xffeef3f5);
        defaultColors.put(key_graySectionText, 0xff7f8991);
        defaultColors.put(key_contextProgressInner1, 0xffbfdff6);
        defaultColors.put(key_contextProgressOuter1, 0xff2b96e2);
        defaultColors.put(key_contextProgressInner2, 0xffbfdff6);
        defaultColors.put(key_contextProgressOuter2, 0xffffffff);
        defaultColors.put(key_contextProgressInner3, 0xffb3b3b3);
        defaultColors.put(key_contextProgressOuter3, 0xffffffff);
        defaultColors.put(key_contextProgressInner4, 0xffcacdd0);
        defaultColors.put(key_contextProgressOuter4, 0xff2f3438);
        defaultColors.put(key_fastScrollActive, 0xff52a3db);
        defaultColors.put(key_fastScrollInactive, 0xffc9cdd1);
        defaultColors.put(key_fastScrollText, 0xffffffff);

        defaultColors.put(key_avatar_text, 0xffffffff);

        defaultColors.put(key_avatar_backgroundSaved, 0xff66bffa);
        defaultColors.put(key_avatar_backgroundArchived, 0xffa9b6c1);
        defaultColors.put(key_avatar_backgroundArchivedHidden, 0xffc6c9cc);
        defaultColors.put(key_avatar_backgroundRed, 0xffe56555);
        defaultColors.put(key_avatar_backgroundOrange, 0xfff28c48);
        defaultColors.put(key_avatar_backgroundViolet, 0xff8e85ee);
        defaultColors.put(key_avatar_backgroundGreen, 0xff76c84d);
        defaultColors.put(key_avatar_backgroundCyan, 0xff5fbed5);
        defaultColors.put(key_avatar_backgroundBlue, 0xff549cdd);
        defaultColors.put(key_avatar_backgroundPink, 0xfff2749a);
        defaultColors.put(key_avatar_backgroundGroupCreateSpanBlue, 0xffe6eff7);

        defaultColors.put(key_avatar_backgroundInProfileBlue, 0xff5085b1);
        defaultColors.put(key_avatar_backgroundActionBarBlue, 0xff598fba);
        defaultColors.put(key_avatar_subtitleInProfileBlue, 0xffd7eafa);
        defaultColors.put(key_avatar_actionBarSelectorBlue, 0xff4981ad);
        defaultColors.put(key_avatar_actionBarIconBlue, 0xffffffff);

        defaultColors.put(key_avatar_nameInMessageRed, 0xffca5650);
        defaultColors.put(key_avatar_nameInMessageOrange, 0xffd87b29);
        defaultColors.put(key_avatar_nameInMessageViolet, 0xff4e92cc);
        defaultColors.put(key_avatar_nameInMessageGreen, 0xff50b232);
        defaultColors.put(key_avatar_nameInMessageCyan, 0xff379eb8);
        defaultColors.put(key_avatar_nameInMessageBlue, 0xff4e92cc);
        defaultColors.put(key_avatar_nameInMessagePink, 0xff4e92cc);

        defaultColors.put(key_actionBarDefault, 0xff527da3);
        defaultColors.put(key_actionBarDefaultIcon, 0xffffffff);
        defaultColors.put(key_actionBarActionModeDefault, 0xffffffff);
        defaultColors.put(key_actionBarActionModeDefaultTop, 0x10000000);
        defaultColors.put(key_actionBarActionModeDefaultIcon, 0xff676a6f);
        defaultColors.put(key_actionBarDefaultTitle, 0xffffffff);
        defaultColors.put(key_actionBarDefaultSubtitle, 0xffd5e8f7);
        defaultColors.put(key_actionBarDefaultSelector, 0xff406d94);
        defaultColors.put(key_actionBarWhiteSelector, 0x2f000000);
        defaultColors.put(key_actionBarDefaultSearch, 0xffffffff);
        defaultColors.put(key_actionBarDefaultSearchPlaceholder, 0x88ffffff);
        defaultColors.put(key_actionBarDefaultSubmenuItem, 0xff222222);
        defaultColors.put(key_actionBarDefaultSubmenuItemIcon, 0xff676b70);
        defaultColors.put(key_actionBarDefaultSubmenuBackground, 0xffffffff);
        defaultColors.put(key_actionBarActionModeDefaultSelector, 0xfff0f0f0);
        defaultColors.put(key_actionBarTabActiveText, 0xffffffff);
        defaultColors.put(key_actionBarTabUnactiveText, 0xffd5e8f7);
        defaultColors.put(key_actionBarTabLine, 0xffffffff);
        defaultColors.put(key_actionBarTabSelector, 0xff406d94);

        defaultColors.put(key_actionBarDefaultArchived, 0xff6f7a87);
        defaultColors.put(key_actionBarDefaultArchivedSelector, 0xff5e6772);
        defaultColors.put(key_actionBarDefaultArchivedIcon, 0xffffffff);
        defaultColors.put(key_actionBarDefaultArchivedTitle, 0xffffffff);
        defaultColors.put(key_actionBarDefaultArchivedSearch, 0xffffffff);
        defaultColors.put(key_actionBarDefaultArchivedSearchPlaceholder, 0x88ffffff);

        defaultColors.put(key_chats_onlineCircle, 0xff4bcb1c);
        defaultColors.put(key_chats_unreadCounter, 0xff4ecc5e);
        defaultColors.put(key_chats_unreadCounterMuted, 0xffc6c9cc);
        defaultColors.put(key_chats_unreadCounterText, 0xffffffff);
        defaultColors.put(key_chats_archiveBackground, 0xff66a9e0);
        defaultColors.put(key_chats_archivePinBackground, 0xff9faab3);
        defaultColors.put(key_chats_archiveIcon, 0xffffffff);
        defaultColors.put(key_chats_archiveText, 0xffffffff);
        defaultColors.put(key_chats_name, 0xff222222);
        defaultColors.put(key_chats_nameArchived, 0xff525252);
        defaultColors.put(key_chats_secretName, 0xff00a60e);
        defaultColors.put(key_chats_secretIcon, 0xff19b126);
        defaultColors.put(key_chats_nameIcon, 0xff242424);
        defaultColors.put(key_chats_pinnedIcon, 0xffa8a8a8);
        defaultColors.put(key_chats_message, 0xff8b8d8f);
        defaultColors.put(key_chats_messageArchived, 0xff919191);
        defaultColors.put(key_chats_message_threeLines, 0xff8e9091);
        defaultColors.put(key_chats_draft, 0xffdd4b39);
        defaultColors.put(key_chats_nameMessage, 0xff3c7eb0);
        defaultColors.put(key_chats_nameMessageArchived, 0xff8b8d8f);
        defaultColors.put(key_chats_nameMessage_threeLines, 0xff424449);
        defaultColors.put(key_chats_nameMessageArchived_threeLines, 0xff5e5e5e);
        defaultColors.put(key_chats_attachMessage, 0xff3c7eb0);
        defaultColors.put(key_chats_actionMessage, 0xff3c7eb0);
        defaultColors.put(key_chats_date, 0xff95999C);
        defaultColors.put(key_chats_pinnedOverlay, 0x08000000);
        defaultColors.put(key_chats_tabletSelectedOverlay, 0x0f000000);
        defaultColors.put(key_chats_sentCheck, 0xff46aa36);
        defaultColors.put(key_chats_sentClock, 0xff75bd5e);
        defaultColors.put(key_chats_sentError, 0xffd55252);
        defaultColors.put(key_chats_sentErrorIcon, 0xffffffff);
        defaultColors.put(key_chats_verifiedBackground, 0xff33a8e6);
        defaultColors.put(key_chats_verifiedCheck, 0xffffffff);
        defaultColors.put(key_chats_muteIcon, 0xffbdc1c4);
        defaultColors.put(key_chats_mentionIcon, 0xffffffff);
        defaultColors.put(key_chats_menuBackground, 0xffffffff);
        defaultColors.put(key_chats_menuItemText, 0xff444444);
        defaultColors.put(key_chats_menuItemCheck, 0xff598fba);
        defaultColors.put(key_chats_menuItemIcon, 0xff889198);
        defaultColors.put(key_chats_menuName, 0xffffffff);
        defaultColors.put(key_chats_menuPhone, 0xffffffff);
        defaultColors.put(key_chats_menuPhoneCats, 0xffc2e5ff);
        defaultColors.put(key_chats_menuCloud, 0xffffffff);
        defaultColors.put(key_chats_menuCloudBackgroundCats, 0xff427ba9);
        defaultColors.put(key_chats_actionIcon, 0xffffffff);
        defaultColors.put(key_chats_actionBackground, 0xff65a9e0);
        defaultColors.put(key_chats_actionPressedBackground, 0xff569dd6);
        defaultColors.put(key_chats_actionUnreadIcon, 0xff737373);
        defaultColors.put(key_chats_actionUnreadBackground, 0xffffffff);
        defaultColors.put(key_chats_actionUnreadPressedBackground, 0xfff2f2f2);
        defaultColors.put(key_chats_menuTopBackgroundCats, 0xff598fba);

        defaultColors.put(key_chat_attachCameraIcon1, 0xffff7d30);
        defaultColors.put(key_chat_attachCameraIcon2, 0xffeb5850);
        defaultColors.put(key_chat_attachCameraIcon3, 0xff43a9f2);
        defaultColors.put(key_chat_attachCameraIcon4, 0xffb455e0);
        defaultColors.put(key_chat_attachCameraIcon5, 0xff61d061);
        defaultColors.put(key_chat_attachCameraIcon6, 0xfffec125);
        defaultColors.put(key_chat_attachMediaBanBackground, 0xff464646);
        defaultColors.put(key_chat_attachMediaBanText, 0xffffffff);

        defaultColors.put(key_chat_attachGalleryBackground, 0xffa47ad9);
        //defaultColors.put(key_chat_attachGalleryBackgroundPressed, 0xffa47ad9);
        defaultColors.put(key_chat_attachGalleryIcon, 0xffffffff);
        defaultColors.put(key_chat_attachVideoBackground, 0xffe37179);
        //defaultColors.put(key_chat_attachVideoBackgroundPressed, 0xffe37179);
        defaultColors.put(key_chat_attachVideoIcon, 0xffffffff);
        defaultColors.put(key_chat_attachAudioBackground, 0xfff68751);
        //defaultColors.put(key_chat_attachAudioBackgroundPressed, 0xfff68751);
        defaultColors.put(key_chat_attachAudioIcon, 0xffffffff);
        defaultColors.put(key_chat_attachFileBackground, 0xff34a0f4);
        //defaultColors.put(key_chat_attachFileBackgroundPressed, 0xff34a0f4);
        defaultColors.put(key_chat_attachFileIcon, 0xffffffff);
        defaultColors.put(key_chat_attachContactBackground, 0xff3ebffa);
        //defaultColors.put(key_chat_attachContactBackgroundPressed, 0xff3ebffa);
        defaultColors.put(key_chat_attachContactIcon, 0xffffffff);
        defaultColors.put(key_chat_attachLocationBackground, 0xff3fc87a);
        //defaultColors.put(key_chat_attachLocationBackgroundPressed, 0xff3fc87a);
        defaultColors.put(key_chat_attachLocationIcon, 0xffffffff);
        defaultColors.put(key_chat_attachHideBackground, 0xffaeaab8);
        //defaultColors.put(key_chat_attachHideBackgroundPressed, 0xffaeaab8);
        defaultColors.put(key_chat_attachHideIcon, 0xffffffff);
        defaultColors.put(key_chat_attachSendBackground, 0xff3ebffa);
        //defaultColors.put(key_chat_attachSendBackgroundPressed, 0xff3ebffa);
        defaultColors.put(key_chat_attachPollBackground, 0xfff5c34d);
        //defaultColors.put(key_chat_attachPollBackgroundPressed, 0xfff68751);
        defaultColors.put(key_chat_attachPollIcon, 0xffffffff);


        defaultColors.put(key_chat_status, 0xffd5e8f7);
        defaultColors.put(key_chat_inGreenCall, 0xff00c853);
        defaultColors.put(key_chat_inRedCall, 0xffff4848);
        defaultColors.put(key_chat_outGreenCall, 0xff00c853);
        defaultColors.put(key_chat_attachSendIcon, 0xffffffff);
        defaultColors.put(key_chat_shareBackground, 0x66728fa6);
        defaultColors.put(key_chat_shareBackgroundSelected, 0x99728fa6);
        defaultColors.put(key_chat_lockIcon, 0xffffffff);
        defaultColors.put(key_chat_muteIcon, 0xffb1cce3);
        defaultColors.put(key_chat_inBubble, 0xffffffff);
        defaultColors.put(key_chat_inBubbleSelected, 0xffecf7fd);
        defaultColors.put(key_chat_inBubbleShadow, 0xff1d3753);
        defaultColors.put(key_chat_outBubble, 0xffefffde);
        defaultColors.put(key_chat_outBubbleSelected, 0xffd9f7c5);
        defaultColors.put(key_chat_outBubbleShadow, 0xff1e750c);
        defaultColors.put(key_chat_inMediaIcon, 0xffffffff);
        defaultColors.put(key_chat_inMediaIconSelected, 0xffeff8fe);
        defaultColors.put(key_chat_outMediaIcon, 0xffefffde);
        defaultColors.put(key_chat_outMediaIconSelected, 0xffe1f8cf);
        defaultColors.put(key_chat_messageTextIn, 0xff000000);
        defaultColors.put(key_chat_messageTextOut, 0xff000000);
        defaultColors.put(key_chat_messageLinkIn, 0xff2678b6);
        defaultColors.put(key_chat_messageLinkOut, 0xff2678b6);
        defaultColors.put(key_chat_serviceText, 0xffffffff);
        defaultColors.put(key_chat_serviceLink, 0xffffffff);
        defaultColors.put(key_chat_serviceIcon, 0xffffffff);
        defaultColors.put(key_chat_mediaTimeBackground, 0x66000000);
        defaultColors.put(key_chat_outSentCheck, 0xff5db050);
        defaultColors.put(key_chat_outSentCheckSelected, 0xff5db050);
        defaultColors.put(key_chat_outSentClock, 0xff75bd5e);
        defaultColors.put(key_chat_outSentClockSelected, 0xff75bd5e);
        defaultColors.put(key_chat_inSentClock, 0xffa1aab3);
        defaultColors.put(key_chat_inSentClockSelected, 0xff93bdca);
        defaultColors.put(key_chat_mediaSentCheck, 0xffffffff);
        defaultColors.put(key_chat_mediaSentClock, 0xffffffff);
        defaultColors.put(key_chat_inViews, 0xffa1aab3);
        defaultColors.put(key_chat_inViewsSelected, 0xff93bdca);
        defaultColors.put(key_chat_outViews, 0xff6eb257);
        defaultColors.put(key_chat_outViewsSelected, 0xff6eb257);
        defaultColors.put(key_chat_mediaViews, 0xffffffff);
        defaultColors.put(key_chat_inMenu, 0xffb6bdc5);
        defaultColors.put(key_chat_inMenuSelected, 0xff98c1ce);
        defaultColors.put(key_chat_outMenu, 0xff91ce7e);
        defaultColors.put(key_chat_outMenuSelected, 0xff91ce7e);
        defaultColors.put(key_chat_mediaMenu, 0xffffffff);
        defaultColors.put(key_chat_outInstant, 0xff55ab4f);
        defaultColors.put(key_chat_outInstantSelected, 0xff489943);
        defaultColors.put(key_chat_inInstant, 0xff3a8ccf);
        defaultColors.put(key_chat_inInstantSelected, 0xff3079b5);
        defaultColors.put(key_chat_sentError, 0xffdb3535);
        defaultColors.put(key_chat_sentErrorIcon, 0xffffffff);
        defaultColors.put(key_chat_selectedBackground, 0x280a90f0);
        defaultColors.put(key_chat_previewDurationText, 0xffffffff);
        defaultColors.put(key_chat_previewGameText, 0xffffffff);
        defaultColors.put(key_chat_inPreviewInstantText, 0xff3a8ccf);
        defaultColors.put(key_chat_outPreviewInstantText, 0xff55ab4f);
        defaultColors.put(key_chat_inPreviewInstantSelectedText, 0xff3079b5);
        defaultColors.put(key_chat_outPreviewInstantSelectedText, 0xff489943);
        defaultColors.put(key_chat_secretTimeText, 0xffe4e2e0);
        defaultColors.put(key_chat_stickerNameText, 0xffffffff);
        defaultColors.put(key_chat_botButtonText, 0xffffffff);
        defaultColors.put(key_chat_botProgress, 0xffffffff);
        defaultColors.put(key_chat_inForwardedNameText, 0xff3886c7);
        defaultColors.put(key_chat_outForwardedNameText, 0xff55ab4f);
        defaultColors.put(key_chat_inViaBotNameText, 0xff3a8ccf);
        defaultColors.put(key_chat_outViaBotNameText, 0xff55ab4f);
        defaultColors.put(key_chat_stickerViaBotNameText, 0xffffffff);
        defaultColors.put(key_chat_inReplyLine, 0xff599fd8);
        defaultColors.put(key_chat_outReplyLine, 0xff6eb969);
        defaultColors.put(key_chat_stickerReplyLine, 0xffffffff);
        defaultColors.put(key_chat_inReplyNameText, 0xff3a8ccf);
        defaultColors.put(key_chat_outReplyNameText, 0xff55ab4f);
        defaultColors.put(key_chat_stickerReplyNameText, 0xffffffff);
        defaultColors.put(key_chat_inReplyMessageText, 0xff000000);
        defaultColors.put(key_chat_outReplyMessageText, 0xff000000);
        defaultColors.put(key_chat_inReplyMediaMessageText, 0xffa1aab3);
        defaultColors.put(key_chat_outReplyMediaMessageText, 0xff65b05b);
        defaultColors.put(key_chat_inReplyMediaMessageSelectedText, 0xff89b4c1);
        defaultColors.put(key_chat_outReplyMediaMessageSelectedText, 0xff65b05b);
        defaultColors.put(key_chat_stickerReplyMessageText, 0xffffffff);
        defaultColors.put(key_chat_inPreviewLine, 0xff70b4e8);
        defaultColors.put(key_chat_outPreviewLine, 0xff88c97b);
        defaultColors.put(key_chat_inSiteNameText, 0xff3a8ccf);
        defaultColors.put(key_chat_outSiteNameText, 0xff55ab4f);
        defaultColors.put(key_chat_inContactNameText, 0xff4e9ad4);
        defaultColors.put(key_chat_outContactNameText, 0xff55ab4f);
        defaultColors.put(key_chat_inContactPhoneText, 0xff2f3438);
        defaultColors.put(key_chat_inContactPhoneSelectedText, 0xff2f3438);
        defaultColors.put(key_chat_outContactPhoneText, 0xff354234);
        defaultColors.put(key_chat_outContactPhoneSelectedText, 0xff354234);
        defaultColors.put(key_chat_mediaProgress, 0xffffffff);
        defaultColors.put(key_chat_inAudioProgress, 0xffffffff);
        defaultColors.put(key_chat_outAudioProgress, 0xffefffde);
        defaultColors.put(key_chat_inAudioSelectedProgress, 0xffeff8fe);
        defaultColors.put(key_chat_outAudioSelectedProgress, 0xffe1f8cf);
        defaultColors.put(key_chat_mediaTimeText, 0xffffffff);
        defaultColors.put(key_chat_inTimeText, 0xffa1aab3);
        defaultColors.put(key_chat_outTimeText, 0xff70b15c);
        defaultColors.put(key_chat_adminText, 0xffc0c6cb);
        defaultColors.put(key_chat_adminSelectedText, 0xff89b4c1);
        defaultColors.put(key_chat_inTimeSelectedText, 0xff89b4c1);
        defaultColors.put(key_chat_outTimeSelectedText, 0xff70b15c);
        defaultColors.put(key_chat_inAudioPerformerText, 0xff2f3438);
        defaultColors.put(key_chat_inAudioPerformerSelectedText, 0xff2f3438);
        defaultColors.put(key_chat_outAudioPerformerText, 0xff354234);
        defaultColors.put(key_chat_outAudioPerformerSelectedText, 0xff354234);
        defaultColors.put(key_chat_inAudioTitleText, 0xff4e9ad4);
        defaultColors.put(key_chat_outAudioTitleText, 0xff55ab4f);
        defaultColors.put(key_chat_inAudioDurationText, 0xffa1aab3);
        defaultColors.put(key_chat_outAudioDurationText, 0xff65b05b);
        defaultColors.put(key_chat_inAudioDurationSelectedText, 0xff89b4c1);
        defaultColors.put(key_chat_outAudioDurationSelectedText, 0xff65b05b);
        defaultColors.put(key_chat_inAudioSeekbar, 0xffe4eaf0);
        defaultColors.put(key_chat_inAudioCacheSeekbar, 0x3fe4eaf0);
        defaultColors.put(key_chat_outAudioSeekbar, 0xffbbe3ac);
        defaultColors.put(key_chat_outAudioCacheSeekbar, 0x3fbbe3ac);
        defaultColors.put(key_chat_inAudioSeekbarSelected, 0xffbcdee8);
        defaultColors.put(key_chat_outAudioSeekbarSelected, 0xffa9dd96);
        defaultColors.put(key_chat_inAudioSeekbarFill, 0xff72b5e8);
        defaultColors.put(key_chat_outAudioSeekbarFill, 0xff78c272);
        defaultColors.put(key_chat_inVoiceSeekbar, 0xffdee5eb);
        defaultColors.put(key_chat_outVoiceSeekbar, 0xffbbe3ac);
        defaultColors.put(key_chat_inVoiceSeekbarSelected, 0xffbcdee8);
        defaultColors.put(key_chat_outVoiceSeekbarSelected, 0xffa9dd96);
        defaultColors.put(key_chat_inVoiceSeekbarFill, 0xff72b5e8);
        defaultColors.put(key_chat_outVoiceSeekbarFill, 0xff78c272);
        defaultColors.put(key_chat_inFileProgress, 0xffebf0f5);
        defaultColors.put(key_chat_outFileProgress, 0xffdaf5c3);
        defaultColors.put(key_chat_inFileProgressSelected, 0xffcbeaf6);
        defaultColors.put(key_chat_outFileProgressSelected, 0xffc5eca7);
        defaultColors.put(key_chat_inFileNameText, 0xff4e9ad4);
        defaultColors.put(key_chat_outFileNameText, 0xff55ab4f);
        defaultColors.put(key_chat_inFileInfoText, 0xffa1aab3);
        defaultColors.put(key_chat_outFileInfoText, 0xff65b05b);
        defaultColors.put(key_chat_inFileInfoSelectedText, 0xff89b4c1);
        defaultColors.put(key_chat_outFileInfoSelectedText, 0xff65b05b);
        defaultColors.put(key_chat_inFileBackground, 0xffebf0f5);
        defaultColors.put(key_chat_outFileBackground, 0xffdaf5c3);
        defaultColors.put(key_chat_inFileBackgroundSelected, 0xffcbeaf6);
        defaultColors.put(key_chat_outFileBackgroundSelected, 0xffc5eca7);
        defaultColors.put(key_chat_inVenueInfoText, 0xffa1aab3);
        defaultColors.put(key_chat_outVenueInfoText, 0xff65b05b);
        defaultColors.put(key_chat_inVenueInfoSelectedText, 0xff89b4c1);
        defaultColors.put(key_chat_outVenueInfoSelectedText, 0xff65b05b);
        defaultColors.put(key_chat_mediaInfoText, 0xffffffff);
        defaultColors.put(key_chat_linkSelectBackground, 0x3362a9e3);
        defaultColors.put(key_chat_textSelectBackground, 0x6662a9e3);
        defaultColors.put(key_chat_emojiPanelBackground, 0xfff0f2f5);
        defaultColors.put(key_chat_emojiPanelBadgeBackground, 0xff4da6ea);
        defaultColors.put(key_chat_emojiPanelBadgeText, 0xffffffff);
        defaultColors.put(key_chat_emojiSearchBackground, 0xffe5e9ee);
        defaultColors.put(key_chat_emojiSearchIcon, 0xff94a1af);
        defaultColors.put(key_chat_emojiPanelShadowLine, 0x12000000);
        defaultColors.put(key_chat_emojiPanelEmptyText, 0xff949ba1);
        defaultColors.put(key_chat_emojiPanelIcon, 0xff9da4ab);
        defaultColors.put(key_chat_emojiBottomPanelIcon, 0xff8c9197);
        defaultColors.put(key_chat_emojiPanelIconSelected, 0xff2b97e2);
        defaultColors.put(key_chat_emojiPanelStickerPackSelector, 0xffe2e5e7);
        defaultColors.put(key_chat_emojiPanelStickerPackSelectorLine, 0xff56abf0);
        defaultColors.put(key_chat_emojiPanelBackspace, 0xff8c9197);
        defaultColors.put(key_chat_emojiPanelMasksIcon, 0xffffffff);
        defaultColors.put(key_chat_emojiPanelMasksIconSelected, 0xff62bfe8);
        defaultColors.put(key_chat_emojiPanelTrendingTitle, 0xff222222);
        defaultColors.put(key_chat_emojiPanelStickerSetName, 0xff828b94);
        defaultColors.put(key_chat_emojiPanelStickerSetNameHighlight, 0xff278ddb);
        defaultColors.put(key_chat_emojiPanelStickerSetNameIcon, 0xffb1b6bc);
        defaultColors.put(key_chat_emojiPanelTrendingDescription, 0xff8a8a8a);
        defaultColors.put(key_chat_botKeyboardButtonText, 0xff36474f);
        defaultColors.put(key_chat_botKeyboardButtonBackground, 0xffe4e7e9);
        defaultColors.put(key_chat_botKeyboardButtonBackgroundPressed, 0xffccd1d4);
        defaultColors.put(key_chat_unreadMessagesStartArrowIcon, 0xffa2b5c7);
        defaultColors.put(key_chat_unreadMessagesStartText, 0xff5695cc);
        defaultColors.put(key_chat_unreadMessagesStartBackground, 0xffffffff);
        defaultColors.put(key_chat_inFileIcon, 0xffa2b5c7);
        defaultColors.put(key_chat_inFileSelectedIcon, 0xff87b6c5);
        defaultColors.put(key_chat_outFileIcon, 0xff85bf78);
        defaultColors.put(key_chat_outFileSelectedIcon, 0xff85bf78);
        defaultColors.put(key_chat_inLocationBackground, 0xffebf0f5);
        defaultColors.put(key_chat_inLocationIcon, 0xffa2b5c7);
        defaultColors.put(key_chat_outLocationBackground, 0xffdaf5c3);
        defaultColors.put(key_chat_outLocationIcon, 0xff87bf78);
        defaultColors.put(key_chat_inContactBackground, 0xff72b5e8);
        defaultColors.put(key_chat_inContactIcon, 0xffffffff);
        defaultColors.put(key_chat_outContactBackground, 0xff78c272);
        defaultColors.put(key_chat_outContactIcon, 0xffefffde);
        defaultColors.put(key_chat_outBroadcast, 0xff46aa36);
        defaultColors.put(key_chat_mediaBroadcast, 0xffffffff);
        defaultColors.put(key_chat_searchPanelIcons, 0xff676a6f);
        defaultColors.put(key_chat_searchPanelText, 0xff676a6f);
        defaultColors.put(key_chat_secretChatStatusText, 0xff7f7f7f);
        defaultColors.put(key_chat_fieldOverlayText, 0xff3a8ccf);
        defaultColors.put(key_chat_stickersHintPanel, 0xffffffff);
        defaultColors.put(key_chat_replyPanelIcons, 0xff57a8e6);
        defaultColors.put(key_chat_replyPanelClose, 0xff8e959b);
        defaultColors.put(key_chat_replyPanelName, 0xff3a8ccf);
        defaultColors.put(key_chat_replyPanelMessage, 0xff222222);
        defaultColors.put(key_chat_replyPanelLine, 0xffe8e8e8);
        defaultColors.put(key_chat_messagePanelBackground, 0xffffffff);
        defaultColors.put(key_chat_messagePanelText, 0xff000000);
        defaultColors.put(key_chat_messagePanelHint, 0xffa4acb3);
        defaultColors.put(key_chat_messagePanelShadow, 0xff000000);
        defaultColors.put(key_chat_messagePanelIcons, 0xff8e959b);
        defaultColors.put(key_chat_recordedVoicePlayPause, 0xffffffff);
        defaultColors.put(key_chat_recordedVoicePlayPausePressed, 0xffd9eafb);
        defaultColors.put(key_chat_recordedVoiceDot, 0xffda564d);
        defaultColors.put(key_chat_recordedVoiceBackground, 0xff559ee3);
        defaultColors.put(key_chat_recordedVoiceProgress, 0xffa2cef8);
        defaultColors.put(key_chat_recordedVoiceProgressInner, 0xffffffff);
        defaultColors.put(key_chat_recordVoiceCancel, 0xff999999);
        defaultColors.put(key_chat_messagePanelSend, 0xff62b0eb);
        defaultColors.put(key_chat_messagePanelVoiceLock, 0xffa4a4a4);
        defaultColors.put(key_chat_messagePanelVoiceLockBackground, 0xffffffff);
        defaultColors.put(key_chat_messagePanelVoiceLockShadow, 0xff000000);
        defaultColors.put(key_chat_recordTime, 0xff4d4c4b);
        defaultColors.put(key_chat_emojiPanelNewTrending, 0xff4da6ea);
        defaultColors.put(key_chat_gifSaveHintText, 0xffffffff);
        defaultColors.put(key_chat_gifSaveHintBackground, 0xcc111111);
        defaultColors.put(key_chat_goDownButton, 0xffffffff);
        defaultColors.put(key_chat_goDownButtonShadow, 0xff000000);
        defaultColors.put(key_chat_goDownButtonIcon, 0xff8e959b);
        defaultColors.put(key_chat_goDownButtonCounter, 0xffffffff);
        defaultColors.put(key_chat_goDownButtonCounterBackground, 0xff4da2e8);
        defaultColors.put(key_chat_messagePanelCancelInlineBot, 0xffadadad);
        defaultColors.put(key_chat_messagePanelVoicePressed, 0xffffffff);
        defaultColors.put(key_chat_messagePanelVoiceBackground, 0xff5795cc);
        defaultColors.put(key_chat_messagePanelVoiceShadow, 0x0d000000);
        defaultColors.put(key_chat_messagePanelVoiceDelete, 0xff737373);
        defaultColors.put(key_chat_messagePanelVoiceDuration, 0xffffffff);
        defaultColors.put(key_chat_inlineResultIcon, 0xff5795cc);
        defaultColors.put(key_chat_topPanelBackground, 0xffffffff);
        defaultColors.put(key_chat_topPanelClose, 0xffa8a8a8);
        defaultColors.put(key_chat_topPanelLine, 0xff6c9fd2);
        defaultColors.put(key_chat_topPanelTitle, 0xff3a8ccf);
        defaultColors.put(key_chat_topPanelMessage, 0xff999999);
        defaultColors.put(key_chat_reportSpam, 0xffcf5957);
        defaultColors.put(key_chat_addContact, 0xff4a82b5);
        defaultColors.put(key_chat_inLoader, 0xff72b5e8);
        defaultColors.put(key_chat_inLoaderSelected, 0xff65abe0);
        defaultColors.put(key_chat_outLoader, 0xff78c272);
        defaultColors.put(key_chat_outLoaderSelected, 0xff6ab564);
        defaultColors.put(key_chat_inLoaderPhoto, 0xffa2b8c8);
        defaultColors.put(key_chat_inLoaderPhotoSelected, 0xffa2b5c7);
        defaultColors.put(key_chat_inLoaderPhotoIcon, 0xfffcfcfc);
        defaultColors.put(key_chat_inLoaderPhotoIconSelected, 0xffebf0f5);
        defaultColors.put(key_chat_outLoaderPhoto, 0xff85bf78);
        defaultColors.put(key_chat_outLoaderPhotoSelected, 0xff7db870);
        defaultColors.put(key_chat_outLoaderPhotoIcon, 0xffdaf5c3);
        defaultColors.put(key_chat_outLoaderPhotoIconSelected, 0xffc0e8a4);
        defaultColors.put(key_chat_mediaLoaderPhoto, 0x66000000);
        defaultColors.put(key_chat_mediaLoaderPhotoSelected, 0x7f000000);
        defaultColors.put(key_chat_mediaLoaderPhotoIcon, 0xffffffff);
        defaultColors.put(key_chat_mediaLoaderPhotoIconSelected, 0xffd9d9d9);
        defaultColors.put(key_chat_secretTimerBackground, 0xcc3e648e);
        defaultColors.put(key_chat_secretTimerText, 0xffffffff);

        defaultColors.put(key_profile_creatorIcon, 0xff3a95d5);
        defaultColors.put(key_profile_actionIcon, 0xff81868a);
        defaultColors.put(key_profile_actionBackground, 0xffffffff);
        defaultColors.put(key_profile_actionPressedBackground, 0xfff2f2f2);
        defaultColors.put(key_profile_verifiedBackground, 0xffb2d6f8);
        defaultColors.put(key_profile_verifiedCheck, 0xff4983b8);
        defaultColors.put(key_profile_title, 0xffffffff);
        defaultColors.put(key_profile_status, 0xffd7eafa);

        defaultColors.put(key_player_actionBar, 0xffffffff);
        defaultColors.put(key_player_actionBarSelector, 0x0f000000);
        defaultColors.put(key_player_actionBarTitle, 0xff2f3438);
        defaultColors.put(key_player_actionBarTop, 0x99000000);
        defaultColors.put(key_player_actionBarSubtitle, 0xff8a8a8a);
        defaultColors.put(key_player_actionBarItems, 0xff8a8a8a);
        defaultColors.put(key_player_background, 0xffffffff);
        defaultColors.put(key_player_time, 0xff8c9296);
        defaultColors.put(key_player_progressBackground, 0xffe9eff5);
        defaultColors.put(key_player_progressCachedBackground, 0xffe9eff5);
        defaultColors.put(key_player_progress, 0xff4b9fe3);
        defaultColors.put(key_player_placeholder, 0xffa8a8a8);
        defaultColors.put(key_player_placeholderBackground, 0xfff0f0f0);
        defaultColors.put(key_player_button, 0xff333333);
        defaultColors.put(key_player_buttonActive, 0xff4ca8ea);

        defaultColors.put(key_sheet_scrollUp, 0xffe1e4e8);
        defaultColors.put(key_sheet_other, 0xffc9cdd3);

        defaultColors.put(key_files_folderIcon, 0xff999999);
        defaultColors.put(key_files_folderIconBackground, 0xfff0f0f0);
        defaultColors.put(key_files_iconText, 0xffffffff);

        defaultColors.put(key_sessions_devicesImage, 0xff969696);

        defaultColors.put(key_passport_authorizeBackground, 0xff45abef);
        defaultColors.put(key_passport_authorizeBackgroundSelected, 0xff409ddb);
        defaultColors.put(key_passport_authorizeText, 0xffffffff);

        defaultColors.put(key_location_markerX, 0xff808080);
        defaultColors.put(key_location_sendLocationBackground, 0xff6da0d4);
        defaultColors.put(key_location_sendLiveLocationBackground, 0xffff6464);
        defaultColors.put(key_location_sendLocationIcon, 0xffffffff);
        defaultColors.put(key_location_sendLiveLocationIcon, 0xffffffff);
        defaultColors.put(key_location_liveLocationProgress, 0xff359fe5);
        defaultColors.put(key_location_placeLocationBackground, 0xff4ca8ea);
        defaultColors.put(key_dialog_liveLocationProgress, 0xff359fe5);

        defaultColors.put(key_calls_callReceivedGreenIcon, 0xff00c853);
        defaultColors.put(key_calls_callReceivedRedIcon, 0xffff4848);

        defaultColors.put(key_featuredStickers_addedIcon, 0xff50a8eb);
        defaultColors.put(key_featuredStickers_buttonProgress, 0xffffffff);
        defaultColors.put(key_featuredStickers_addButton, 0xff50a8eb);
        defaultColors.put(key_featuredStickers_addButtonPressed, 0xff439bde);
        defaultColors.put(key_featuredStickers_delButton, 0xffd95757);
        defaultColors.put(key_featuredStickers_delButtonPressed, 0xffc64949);
        defaultColors.put(key_featuredStickers_buttonText, 0xffffffff);
        defaultColors.put(key_featuredStickers_unread, 0xff4da6ea);

        defaultColors.put(key_inappPlayerPerformer, 0xff2f3438);
        defaultColors.put(key_inappPlayerTitle, 0xff2f3438);
        defaultColors.put(key_inappPlayerBackground, 0xffffffff);
        defaultColors.put(key_inappPlayerPlayPause, 0xff62b0eb);
        defaultColors.put(key_inappPlayerClose, 0xffa8a8a8);

        defaultColors.put(key_returnToCallBackground, 0xff44a1e3);
        defaultColors.put(key_returnToCallText, 0xffffffff);

        defaultColors.put(key_sharedMedia_startStopLoadIcon, 0xff36a2ee);
        defaultColors.put(key_sharedMedia_linkPlaceholder, 0xfff0f3f5);
        defaultColors.put(key_sharedMedia_linkPlaceholderText, 0xffb7bec3);
        defaultColors.put(key_sharedMedia_photoPlaceholder, 0xffedf3f7);
        defaultColors.put(key_sharedMedia_actionMode, 0xff4687b3);

        defaultColors.put(key_checkbox, 0xff5ec245);
        defaultColors.put(key_checkboxCheck, 0xffffffff);
        defaultColors.put(key_checkboxDisabled, 0xffb0b9c2);

        defaultColors.put(key_stickers_menu, 0xffb6bdc5);
        defaultColors.put(key_stickers_menuSelector, 0x0f000000);

        defaultColors.put(key_changephoneinfo_image, 0xffa8a8a8);
        defaultColors.put(key_changephoneinfo_changeText, 0xff4d83b3);

        defaultColors.put(key_groupcreate_hintText, 0xffa1aab3);
        defaultColors.put(key_groupcreate_cursor, 0xff52a3db);
        defaultColors.put(key_groupcreate_sectionShadow, 0xff000000);
        defaultColors.put(key_groupcreate_sectionText, 0xff7c8288);
        defaultColors.put(key_groupcreate_spanText, 0xff222222);
        defaultColors.put(key_groupcreate_spanBackground, 0xfff2f2f2);
        defaultColors.put(key_groupcreate_spanDelete, 0xffffffff);

        defaultColors.put(key_contacts_inviteBackground, 0xff55be61);
        defaultColors.put(key_contacts_inviteText, 0xffffffff);

        defaultColors.put(key_login_progressInner, 0xffe1eaf2);
        defaultColors.put(key_login_progressOuter, 0xff62a0d0);

        defaultColors.put(key_musicPicker_checkbox, 0xff29b6f7);
        defaultColors.put(key_musicPicker_checkboxCheck, 0xffffffff);
        defaultColors.put(key_musicPicker_buttonBackground, 0xff5cafea);
        defaultColors.put(key_musicPicker_buttonIcon, 0xffffffff);
        defaultColors.put(key_picker_enabledButton, 0xff19a7e8);
        defaultColors.put(key_picker_disabledButton, 0xff999999);
        defaultColors.put(key_picker_badge, 0xff29b6f7);
        defaultColors.put(key_picker_badgeText, 0xffffffff);

        defaultColors.put(key_chat_botSwitchToInlineText, 0xff4391cc);

        defaultColors.put(key_undo_background, 0xea272f38);
        defaultColors.put(key_undo_cancelColor, 0xff85caff);
        defaultColors.put(key_undo_infoColor, 0xffffffff);

        fallbackKeys.put(key_chat_adminText, key_chat_inTimeText);
        fallbackKeys.put(key_chat_adminSelectedText, key_chat_inTimeSelectedText);
        fallbackKeys.put(key_player_progressCachedBackground, key_player_progressBackground);
        fallbackKeys.put(key_chat_inAudioCacheSeekbar, key_chat_inAudioSeekbar);
        fallbackKeys.put(key_chat_outAudioCacheSeekbar, key_chat_outAudioSeekbar);
        fallbackKeys.put(key_chat_emojiSearchBackground, key_chat_emojiPanelStickerPackSelector);
        fallbackKeys.put(key_location_sendLiveLocationIcon, key_location_sendLocationIcon);
        fallbackKeys.put(key_changephoneinfo_changeText, key_windowBackgroundWhiteBlueText4);
        fallbackKeys.put(key_graySectionText, key_windowBackgroundWhiteGrayText2);
        fallbackKeys.put(key_chat_inMediaIcon, key_chat_inBubble);
        fallbackKeys.put(key_chat_outMediaIcon, key_chat_outBubble);
        fallbackKeys.put(key_chat_inMediaIconSelected, key_chat_inBubbleSelected);
        fallbackKeys.put(key_chat_outMediaIconSelected, key_chat_outBubbleSelected);
        fallbackKeys.put(key_chats_actionUnreadIcon, key_profile_actionIcon);
        fallbackKeys.put(key_chats_actionUnreadBackground, key_profile_actionBackground);
        fallbackKeys.put(key_chats_actionUnreadPressedBackground, key_profile_actionPressedBackground);
        fallbackKeys.put(key_dialog_inlineProgressBackground, key_windowBackgroundGray);
        fallbackKeys.put(key_dialog_inlineProgress, key_chats_menuItemIcon);
        fallbackKeys.put(key_groupcreate_spanDelete, key_chats_actionIcon);
        fallbackKeys.put(key_sharedMedia_photoPlaceholder, key_windowBackgroundGray);
        fallbackKeys.put(key_chat_attachPollBackground, key_chat_attachAudioBackground);
        fallbackKeys.put(key_chat_attachPollIcon, key_chat_attachAudioIcon);
        fallbackKeys.put(key_chats_onlineCircle, key_windowBackgroundWhiteBlueText);
        fallbackKeys.put(key_windowBackgroundWhiteBlueButton, key_windowBackgroundWhiteValueText);
        fallbackKeys.put(key_windowBackgroundWhiteBlueIcon, key_windowBackgroundWhiteValueText);
        fallbackKeys.put(key_undo_background, key_chat_gifSaveHintBackground);
        fallbackKeys.put(key_undo_cancelColor, key_chat_gifSaveHintText);
        fallbackKeys.put(key_undo_infoColor, key_chat_gifSaveHintText);
        fallbackKeys.put(key_windowBackgroundUnchecked, key_windowBackgroundWhite);
        fallbackKeys.put(key_windowBackgroundChecked, key_windowBackgroundWhite);
        fallbackKeys.put(key_switchTrackBlue, key_switchTrack);
        fallbackKeys.put(key_switchTrackBlueChecked, key_switchTrackChecked);
        fallbackKeys.put(key_switchTrackBlueThumb, key_windowBackgroundWhite);
        fallbackKeys.put(key_switchTrackBlueThumbChecked, key_windowBackgroundWhite);
        fallbackKeys.put(key_windowBackgroundCheckText, key_windowBackgroundWhiteBlackText);
        fallbackKeys.put(key_contextProgressInner4, key_contextProgressInner1);
        fallbackKeys.put(key_contextProgressOuter4, key_contextProgressOuter1);
        fallbackKeys.put(key_switchTrackBlueSelector, key_listSelector);
        fallbackKeys.put(key_switchTrackBlueSelectorChecked, key_listSelector);
        fallbackKeys.put(key_chat_emojiBottomPanelIcon, key_chat_emojiPanelIcon);
        fallbackKeys.put(key_chat_emojiSearchIcon, key_chat_emojiPanelIcon);
        fallbackKeys.put(key_chat_emojiPanelStickerSetNameHighlight, key_windowBackgroundWhiteBlueText4);
        fallbackKeys.put(key_chat_emojiPanelStickerPackSelectorLine, key_chat_emojiPanelIconSelected);
        fallbackKeys.put(key_sharedMedia_actionMode, key_actionBarDefault);
        fallbackKeys.put(key_sheet_scrollUp, key_chat_emojiPanelStickerPackSelector);
        fallbackKeys.put(key_sheet_other, key_player_actionBarItems);
        fallbackKeys.put(key_dialogSearchBackground, key_chat_emojiPanelStickerPackSelector);
        fallbackKeys.put(key_dialogSearchHint, key_chat_emojiPanelIcon);
        fallbackKeys.put(key_dialogSearchIcon, key_chat_emojiPanelIcon);
        fallbackKeys.put(key_dialogSearchText, key_windowBackgroundWhiteBlackText);
        fallbackKeys.put(key_dialogFloatingButton, key_dialogRoundCheckBox);
        fallbackKeys.put(key_dialogFloatingButtonPressed, key_dialogRoundCheckBox);
        fallbackKeys.put(key_dialogFloatingIcon, key_dialogRoundCheckBoxCheck);
        fallbackKeys.put(key_dialogShadowLine, key_chat_emojiPanelShadowLine);
        fallbackKeys.put(key_actionBarDefaultArchived, key_actionBarDefault);
        fallbackKeys.put(key_actionBarDefaultArchivedSelector, key_actionBarDefaultSelector);
        fallbackKeys.put(key_actionBarDefaultArchivedIcon, key_actionBarDefaultIcon);
        fallbackKeys.put(key_actionBarDefaultArchivedTitle, key_actionBarDefaultTitle);
        fallbackKeys.put(key_actionBarDefaultArchivedSearch, key_actionBarDefaultSearch);
        fallbackKeys.put(key_actionBarDefaultArchivedSearchPlaceholder, key_actionBarDefaultSearchPlaceholder);
        fallbackKeys.put(key_chats_message_threeLines, key_chats_message);
        fallbackKeys.put(key_chats_nameMessage_threeLines, key_chats_nameMessage);
        fallbackKeys.put(key_chats_nameArchived, key_chats_name);
        fallbackKeys.put(key_chats_nameMessageArchived, key_chats_nameMessage);
        fallbackKeys.put(key_chats_nameMessageArchived_threeLines, key_chats_nameMessage);
        fallbackKeys.put(key_chats_messageArchived, key_chats_message);
        fallbackKeys.put(key_avatar_backgroundArchived, key_chats_unreadCounterMuted);
        fallbackKeys.put(key_avatar_backgroundArchivedHidden, key_chats_unreadCounterMuted);
        fallbackKeys.put(key_chats_archiveBackground, key_chats_actionBackground);
        fallbackKeys.put(key_chats_archivePinBackground, key_chats_unreadCounterMuted);
        fallbackKeys.put(key_chats_archiveIcon, key_chats_actionIcon);
        fallbackKeys.put(key_chats_archiveText, key_chats_actionIcon);
        fallbackKeys.put(key_actionBarDefaultSubmenuItemIcon, key_dialogIcon);
        fallbackKeys.put(key_checkboxDisabled, key_chats_unreadCounterMuted);
        fallbackKeys.put(key_chat_status, key_actionBarDefaultSubtitle);
        fallbackKeys.put(key_chat_inGreenCall, key_calls_callReceivedGreenIcon);
        fallbackKeys.put(key_chat_inRedCall, key_calls_callReceivedRedIcon);
        fallbackKeys.put(key_chat_outGreenCall, key_calls_callReceivedGreenIcon);
        fallbackKeys.put(key_actionBarTabActiveText, key_actionBarDefaultTitle);
        fallbackKeys.put(key_actionBarTabUnactiveText, key_actionBarDefaultSubtitle);
        fallbackKeys.put(key_actionBarTabLine, key_actionBarDefaultTitle);
        fallbackKeys.put(key_actionBarTabSelector, key_actionBarDefaultSelector);
        fallbackKeys.put(key_profile_status, key_avatar_subtitleInProfileBlue);
        fallbackKeys.put(key_chats_menuTopBackgroundCats, key_avatar_backgroundActionBarBlue);

        themes = new ArrayList<>();
        otherThemes = new ArrayList<>();
        themesDict = new HashMap<>();
        currentColors = new HashMap<>();

        ThemeInfo themeInfo = new ThemeInfo();
        themeInfo.name = "Default";
        themeInfo.previewBackgroundColor = 0xffcfd9e3;
        themeInfo.previewInColor = 0xffffffff;
        themeInfo.previewOutColor = 0xfff0fee0;
        themeInfo.sortIndex = 0;
        themes.add(currentDayTheme = currentTheme = defaultTheme = themeInfo);
        themesDict.put("Default", defaultTheme);

        themeInfo = new ThemeInfo();
        themeInfo.name = "Dark";
        themeInfo.assetName = "dark.attheme";
        themeInfo.previewBackgroundColor = 0xff5a5d61;
        themeInfo.previewInColor = 0xff747a84;
        themeInfo.previewOutColor = 0xff82a8e3;
        themeInfo.sortIndex = 3;
        themes.add(themeInfo);
        themesDict.put("Dark", themeInfo);

        themeInfo = new ThemeInfo();
        themeInfo.name = "Blue";
        themeInfo.assetName = "bluebubbles.attheme";
        themeInfo.previewBackgroundColor = 0xff95beec;
        themeInfo.previewInColor = 0xffffffff;
        themeInfo.previewOutColor = 0xffd0e6ff;
        themeInfo.sortIndex = 1;
        themes.add(themeInfo);
        themesDict.put("Blue", themeInfo);

        themeInfo = new ThemeInfo();
        themeInfo.name = "Dark Blue";
        themeInfo.assetName = "darkblue.attheme";
        themeInfo.previewBackgroundColor = 0xff5f6e82;
        themeInfo.previewInColor = 0xff76869c;
        themeInfo.previewOutColor = 0xff82a8e3;
        themeInfo.sortIndex = 2;
        themes.add(themeInfo);
        themesDict.put("Dark Blue", currentNightTheme = themeInfo);

        if (BuildVars.DEBUG_VERSION) {
            themeInfo = new ThemeInfo();
            themeInfo.name = "Graphite";
            themeInfo.assetName = "graphite.attheme";
            themeInfo.previewBackgroundColor = 0xff7a7e89;
            themeInfo.previewInColor = 0xff989ba3;
            themeInfo.previewOutColor = 0xffa4bff9;
            themeInfo.sortIndex = 4;
            themes.add(themeInfo);
            themesDict.put("Graphite", themeInfo);
        }

        themeInfo = new ThemeInfo();
        themeInfo.name = "Arctic Blue";
        themeInfo.assetName = "arctic.attheme";
        themeInfo.previewBackgroundColor = 0xffffffff;
        themeInfo.previewInColor = 0xffebeef4;
        themeInfo.previewOutColor = 0xff7cb2fe;
        themeInfo.sortIndex = 5;
        themes.add(themeInfo);
        themesDict.put("Arctic Blue", themeInfo);

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", Activity.MODE_PRIVATE);
        String themesString = preferences.getString("themes2", null);
        if (!TextUtils.isEmpty(themesString)) {
            try {
                JSONArray jsonArray = new JSONArray(themesString);
                for (int a = 0; a < jsonArray.length(); a++) {
                    themeInfo = ThemeInfo.createWithJson(jsonArray.getJSONObject(a));
                    if (themeInfo != null) {
                        otherThemes.add(themeInfo);
                        themes.add(themeInfo);
                        themesDict.put(themeInfo.name, themeInfo);
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        } else {
            themesString = preferences.getString("themes", null);
            if (!TextUtils.isEmpty(themesString)) {
                String[] themesArr = themesString.split("&");
                for (int a = 0; a < themesArr.length; a++) {
                    themeInfo = ThemeInfo.createWithString(themesArr[a]);
                    if (themeInfo != null) {
                        otherThemes.add(themeInfo);
                        themes.add(themeInfo);
                        themesDict.put(themeInfo.name, themeInfo);
                    }
                }
            }
            saveOtherThemes();
            preferences.edit().remove("themes").commit();
        }

        sortThemes();

        ThemeInfo applyingTheme = null;
    }

    private static Method StateListDrawable_getStateDrawableMethod;
    private static Field BitmapDrawable_mColorFilter;


    @SuppressLint("PrivateApi")
    private static Drawable getStateDrawable(Drawable drawable, int index) {
        if (StateListDrawable_getStateDrawableMethod == null) {
            try {
                StateListDrawable_getStateDrawableMethod = StateListDrawable.class.getDeclaredMethod("getStateDrawable", int.class);
            } catch (Throwable ignore) {

            }
        }
        if (StateListDrawable_getStateDrawableMethod == null) {
            return null;
        }
        try {
            return (Drawable) StateListDrawable_getStateDrawableMethod.invoke(drawable, index);
        } catch (Exception ignore) {

        }
        return null;
    }

    public static Drawable createEmojiIconSelectorDrawable(Context context, int resource, int defaultColor, int pressedColor) {
        Resources resources = context.getResources();
        Drawable defaultDrawable = resources.getDrawable(resource).mutate();
        if (defaultColor != 0) {
            defaultDrawable.setColorFilter(new PorterDuffColorFilter(defaultColor, PorterDuff.Mode.MULTIPLY));
        }
        Drawable pressedDrawable = resources.getDrawable(resource).mutate();
        if (pressedColor != 0) {
            pressedDrawable.setColorFilter(new PorterDuffColorFilter(pressedColor, PorterDuff.Mode.MULTIPLY));
        }
        StateListDrawable stateListDrawable = new StateListDrawable() {
            @Override
            public boolean selectDrawable(int index) {
                if (Build.VERSION.SDK_INT < 21) {
                    Drawable drawable = getStateDrawable(this, index);
                    ColorFilter colorFilter = null;
                    if (drawable instanceof BitmapDrawable) {
                        colorFilter = ((BitmapDrawable) drawable).getPaint().getColorFilter();
                    } else if (drawable instanceof NinePatchDrawable) {
                        colorFilter = ((NinePatchDrawable) drawable).getPaint().getColorFilter();
                    }
                    boolean result = super.selectDrawable(index);
                    if (colorFilter != null) {
                        drawable.setColorFilter(colorFilter);
                    }
                    return result;
                }
                return super.selectDrawable(index);
            }
        };
        stateListDrawable.setEnterFadeDuration(1);
        stateListDrawable.setExitFadeDuration(200);
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
        stateListDrawable.addState(new int[]{}, defaultDrawable);
        return stateListDrawable;
    }


    public static boolean canStartHolidayAnimation() {
        return canStartHolidayAnimation;
    }

    public static int getEventType() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int monthOfYear = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int minutes = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        int eventType = -1;
        if (monthOfYear == 11 && dayOfMonth >= 24 && dayOfMonth <= 31 || monthOfYear == 0 && dayOfMonth == 1) {
            eventType = 0;
        }
        return eventType;
    }

    public static Drawable createSimpleSelectorDrawable(Context context, int resource, int defaultColor, int pressedColor) {
        Resources resources = context.getResources();
        Drawable defaultDrawable = resources.getDrawable(resource).mutate();
        if (defaultColor != 0) {
            defaultDrawable.setColorFilter(new PorterDuffColorFilter(defaultColor, PorterDuff.Mode.MULTIPLY));
        }
        Drawable pressedDrawable = resources.getDrawable(resource).mutate();
        if (pressedColor != 0) {
            pressedDrawable.setColorFilter(new PorterDuffColorFilter(pressedColor, PorterDuff.Mode.MULTIPLY));
        }
        StateListDrawable stateListDrawable = new StateListDrawable() {
            @Override
            public boolean selectDrawable(int index) {
                if (Build.VERSION.SDK_INT < 21) {
                    Drawable drawable = getStateDrawable(this, index);
                    ColorFilter colorFilter = null;
                    if (drawable instanceof BitmapDrawable) {
                        colorFilter = ((BitmapDrawable) drawable).getPaint().getColorFilter();
                    } else if (drawable instanceof NinePatchDrawable) {
                        colorFilter = ((NinePatchDrawable) drawable).getPaint().getColorFilter();
                    }
                    boolean result = super.selectDrawable(index);
                    if (colorFilter != null) {
                        drawable.setColorFilter(colorFilter);
                    }
                    return result;
                }
                return super.selectDrawable(index);
            }
        };
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
        return stateListDrawable;
    }

    public static Drawable createCircleDrawable(int size, int color) {
        OvalShape ovalShape = new OvalShape();
        ovalShape.resize(size, size);
        ShapeDrawable defaultDrawable = new ShapeDrawable(ovalShape);
        defaultDrawable.getPaint().setColor(color);
        return defaultDrawable;
    }

    public static Drawable createSimpleSelectorCircleDrawable(int size, int defaultColor, int pressedColor) {
        OvalShape ovalShape = new OvalShape();
        ovalShape.resize(size, size);
        ShapeDrawable defaultDrawable = new ShapeDrawable(ovalShape);
        defaultDrawable.getPaint().setColor(defaultColor);
        ShapeDrawable pressedDrawable = new ShapeDrawable(ovalShape);
        if (Build.VERSION.SDK_INT >= 21) {
            pressedDrawable.getPaint().setColor(0xffffffff);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{pressedColor}
            );
            return new RippleDrawable(colorStateList, defaultDrawable, pressedDrawable);
        } else {
            pressedDrawable.getPaint().setColor(pressedColor);
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
            stateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
            stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
            return stateListDrawable;
        }
    }

    public static Drawable createRoundRectDrawable(int rad, int defaultColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        return defaultDrawable;
    }

    public static Drawable createSimpleSelectorRoundRectDrawable(int rad, int defaultColor, int pressedColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        ShapeDrawable pressedDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        pressedDrawable.getPaint().setColor(pressedColor);
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
        return stateListDrawable;
    }

    public static Drawable getRoundRectSelectorDrawable(int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            Drawable maskDrawable = createRoundRectDrawable(AndroidUtilities.dp(3), 0xffffffff);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{(color & 0x00ffffff) | 0x19000000}
            );
            return new RippleDrawable(colorStateList, null, maskDrawable);
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, createRoundRectDrawable(AndroidUtilities.dp(3), (color & 0x00ffffff) | 0x19000000));
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, createRoundRectDrawable(AndroidUtilities.dp(3), (color & 0x00ffffff) | 0x19000000));
            stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(0x00000000));
            return stateListDrawable;
        }
    }

    public static Drawable createSelectorWithBackgroundDrawable(int backgroundColor, int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            Drawable maskDrawable = new ColorDrawable(backgroundColor);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{color}
            );
            return new RippleDrawable(colorStateList, new ColorDrawable(backgroundColor), maskDrawable);
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(color));
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(color));
            stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(backgroundColor));
            return stateListDrawable;
        }
    }

    public static Drawable getSelectorDrawable(boolean whiteBackground) {
        return getSelectorDrawable(getColor(key_listSelector), whiteBackground);
    }

    public static Drawable getSelectorDrawable(int color, boolean whiteBackground) {
        if (whiteBackground) {
            if (Build.VERSION.SDK_INT >= 21) {
                Drawable maskDrawable = new ColorDrawable(0xffffffff);
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{StateSet.WILD_CARD},
                        new int[]{color}
                );
                return new RippleDrawable(colorStateList, new ColorDrawable(getColor(key_windowBackgroundWhite)), maskDrawable);
            } else {
                StateListDrawable stateListDrawable = new StateListDrawable();
                stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(color));
                stateListDrawable.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(color));
                stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(getColor(key_windowBackgroundWhite)));
                return stateListDrawable;
            }
        } else {
            return createSelectorDrawable(color, 2);
        }
    }

    public static Drawable createSelectorDrawable(int color) {
        return createSelectorDrawable(color, 1);
    }

    public static Drawable createSelectorDrawable(int color, final int maskType) {
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= 21) {
            Drawable maskDrawable = null;
            if (maskType == 1 && Build.VERSION.SDK_INT >= 23) {
                maskDrawable = null;
            } else if (maskType == 1 || maskType == 3 || maskType == 4) {
                maskPaint.setColor(0xffffffff);
                maskDrawable = new Drawable() {
                    @Override
                    public void draw(Canvas canvas) {
                        android.graphics.Rect bounds = getBounds();
                        int rad;
                        if (maskType == 1) {
                            rad = AndroidUtilities.dp(20);
                        } else if (maskType == 3) {
                            rad = (Math.max(bounds.width(), bounds.height()) / 2);
                        } else {
                            rad = (int) Math.ceil(Math.sqrt((bounds.left - bounds.centerX()) * (bounds.left - bounds.centerX()) + (bounds.top - bounds.centerY()) * (bounds.top - bounds.centerY())));
                        }
                        canvas.drawCircle(bounds.centerX(), bounds.centerY(), rad, maskPaint);
                    }

                    @Override
                    public void setAlpha(int alpha) {

                    }

                    @Override
                    public void setColorFilter(ColorFilter colorFilter) {

                    }

                    @Override
                    public int getOpacity() {
                        return PixelFormat.UNKNOWN;
                    }
                };
            } else if (maskType == 2) {
                maskDrawable = new ColorDrawable(0xffffffff);
            }
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{color}
            );
            RippleDrawable rippleDrawable = new RippleDrawable(colorStateList, null, maskDrawable);
            if (maskType == 1 && Build.VERSION.SDK_INT >= 23) {
                rippleDrawable.setRadius(AndroidUtilities.dp(20));
            }
            return rippleDrawable;
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(color));
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(color));
            stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(0x00000000));
            return stateListDrawable;
        }
    }

    private static void sortThemes() {
        Collections.sort(themes, new Comparator<ThemeInfo>() {
            @Override
            public int compare(ThemeInfo o1, ThemeInfo o2) {
                if (o1.pathToFile == null && o1.assetName == null) {
                    return -1;
                } else if (o2.pathToFile == null && o2.assetName == null) {
                    return 1;
                }
                return o1.name.compareTo(o2.name);
            }
        });
    }

    private static void saveOtherThemes() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        JSONArray array = new JSONArray();
        for (int a = 0; a < otherThemes.size(); a++) {
            JSONObject jsonObject = otherThemes.get(a).getSaveJson();
            if (jsonObject != null) {
                array.put(jsonObject);
            }
        }
        editor.putString("themes2", array.toString());
        editor.commit();
    }

    public static HashMap<String, Integer> getDefaultColors() {
        return defaultColors;
    }

    public static String getCurrentThemeName() {
        String text = currentDayTheme.getName();
        if (text.toLowerCase().endsWith(".attheme")) {
            text = text.substring(0, text.lastIndexOf('.'));
        }
        return text;
    }

    public static String getCurrentNightThemeName() {
        if (currentNightTheme == null) {
            return "";
        }
        String text = currentNightTheme.getName();
        if (text.toLowerCase().endsWith(".attheme")) {
            text = text.substring(0, text.lastIndexOf('.'));
        }
        return text;
    }

    public static ThemeInfo getCurrentTheme() {
        return currentDayTheme != null ? currentDayTheme : defaultTheme;
    }

    public static ThemeInfo getCurrentNightTheme() {
        return currentNightTheme;
    }

    public static boolean isCurrentThemeNight() {
        return currentTheme == currentNightTheme;
    }

    public static boolean isCurrentThemeDefault() {
        return currentTheme == defaultTheme;
    }

    private static long getAutoNightSwitchThemeDelay() {
        long newTime = SystemClock.elapsedRealtime();
        if (Math.abs(lastThemeSwitchTime - newTime) >= LIGHT_SENSOR_THEME_SWITCH_NEAR_THRESHOLD) {
            return LIGHT_SENSOR_THEME_SWITCH_DELAY;
        }
        return LIGHT_SENSOR_THEME_SWITCH_NEAR_DELAY;
    }

    public static void applyDialogsTheme() {
        if (dialogs_namePaint == null) {
            return;
        }
        dialogs_namePaint.setColor(getColor(key_chats_name));
        dialogs_nameEncryptedPaint.setColor(getColor(key_chats_secretName));
        dialogs_searchNamePaint.setColor(getColor(key_chats_name));
        dialogs_searchNameEncryptedPaint.setColor(getColor(key_chats_secretName));
        dialogs_messagePaint.setColor(dialogs_messagePaint.linkColor = getColor(key_chats_message));
        dialogs_messageNamePaint.setColor(dialogs_messageNamePaint.linkColor = getColor(key_chats_nameMessage_threeLines));
        dialogs_tabletSeletedPaint.setColor(getColor(key_chats_tabletSelectedOverlay));
        dialogs_pinnedPaint.setColor(getColor(key_chats_pinnedOverlay));
        dialogs_timePaint.setColor(getColor(key_chats_date));
        dialogs_countTextPaint.setColor(getColor(key_chats_unreadCounterText));
        dialogs_archiveTextPaint.setColor(getColor(key_chats_archiveText));
        dialogs_messagePrintingPaint.setColor(getColor(key_chats_actionMessage));
        dialogs_countPaint.setColor(getColor(key_chats_unreadCounter));
        dialogs_countGrayPaint.setColor(getColor(key_chats_unreadCounterMuted));
        dialogs_errorPaint.setColor(getColor(key_chats_sentError));
        dialogs_onlinePaint.setColor(getColor(key_windowBackgroundWhiteBlueText3));
        dialogs_offlinePaint.setColor(getColor(key_windowBackgroundWhiteGrayText3));

        setDrawableColorByKey(dialogs_lockDrawable, key_chats_secretIcon);
        setDrawableColorByKey(dialogs_checkDrawable, key_chats_sentCheck);
        setDrawableColorByKey(dialogs_halfCheckDrawable, key_chats_sentCheck);
        setDrawableColorByKey(dialogs_clockDrawable, key_chats_sentClock);
        setDrawableColorByKey(dialogs_errorDrawable, key_chats_sentErrorIcon);
        setDrawableColorByKey(dialogs_groupDrawable, key_chats_nameIcon);
        setDrawableColorByKey(dialogs_broadcastDrawable, key_chats_nameIcon);
        setDrawableColorByKey(dialogs_botDrawable, key_chats_nameIcon);
        setDrawableColorByKey(dialogs_pinnedDrawable, key_chats_pinnedIcon);
        setDrawableColorByKey(dialogs_reorderDrawable, key_chats_pinnedIcon);
        setDrawableColorByKey(dialogs_muteDrawable, key_chats_muteIcon);
        setDrawableColorByKey(dialogs_mentionDrawable, key_chats_mentionIcon);
        setDrawableColorByKey(dialogs_verifiedDrawable, key_chats_verifiedBackground);
        setDrawableColorByKey(dialogs_verifiedCheckDrawable, key_chats_verifiedCheck);
        setDrawableColorByKey(dialogs_holidayDrawable, key_actionBarDefaultTitle);
//        setDrawableColorByKey(dialogs_scamDrawable, key_chats_draft);
    }

    public static void destroyResources() {
        for (int a = 0; a < chat_attachButtonDrawables.length; a++) {
            if (chat_attachButtonDrawables[a] != null) {
                chat_attachButtonDrawables[a].setCallback(null);
            }
        }
    }

    public static void applyChatServiceMessageColor() {
        applyChatServiceMessageColor(null);
    }

    public static void applyChatServiceMessageColor(int[] custom) {
        if (chat_actionBackgroundPaint == null) {
            return;
        }
        Integer serviceColor;
        Integer servicePressedColor;
        serviceMessageColor = serviceMessageColorBackup;
        serviceSelectedMessageColor = serviceSelectedMessageColorBackup;
        if (custom != null && custom.length >= 2) {
            serviceColor = custom[0];
            servicePressedColor = custom[1];
            serviceMessageColor = custom[0];
            serviceSelectedMessageColor = custom[1];
        } else {
            serviceColor = currentColors.get(key_chat_serviceBackground);
            servicePressedColor = currentColors.get(key_chat_serviceBackgroundSelected);
        }
        Integer serviceColor2 = serviceColor;
        Integer servicePressedColor2 = servicePressedColor;

        if (serviceColor == null) {
            serviceColor = serviceMessageColor;
            serviceColor2 = serviceMessage2Color;
        }
        if (servicePressedColor == null) {
            servicePressedColor = serviceSelectedMessageColor;
            servicePressedColor2 = serviceSelectedMessage2Color;
        }
        if (currentColor != serviceColor) {
            chat_actionBackgroundPaint.setColor(serviceColor);
            colorFilter = new PorterDuffColorFilter(serviceColor, PorterDuff.Mode.MULTIPLY);
            colorFilter2 = new PorterDuffColorFilter(serviceColor2, PorterDuff.Mode.MULTIPLY);
            currentColor = serviceColor;
            if (chat_cornerOuter[0] != null) {
                for (int a = 0; a < 4; a++) {
                    chat_cornerOuter[a].setColorFilter(colorFilter);
                    chat_cornerInner[a].setColorFilter(colorFilter);
                }
            }
        }
        if (currentSelectedColor != servicePressedColor) {
            currentSelectedColor = servicePressedColor;
            colorPressedFilter = new PorterDuffColorFilter(servicePressedColor, PorterDuff.Mode.MULTIPLY);
            colorPressedFilter2 = new PorterDuffColorFilter(servicePressedColor2, PorterDuff.Mode.MULTIPLY);
        }
    }

    private static ColorFilter currentShareColorFilter;
    private static int currentShareColorFilterColor;
    private static ColorFilter currentShareSelectedColorFilter;
    private static  int currentShareSelectedColorFilterColor;
    public static ColorFilter getShareColorFilter(int color, boolean selected) {
        if (selected) {
            if (currentShareSelectedColorFilter == null || currentShareSelectedColorFilterColor != color) {
                currentShareSelectedColorFilterColor = color;
                currentShareSelectedColorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
            return currentShareSelectedColorFilter;
        } else {
            if (currentShareColorFilter == null || currentShareColorFilterColor != color) {
                currentShareColorFilterColor = color;
                currentShareColorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
            return currentShareColorFilter;
        }
    }

    public static void applyProfileTheme() {
        if (profile_verifiedDrawable == null) {
            return;
        }

        profile_aboutTextPaint.setColor(getColor(key_windowBackgroundWhiteBlackText));
        profile_aboutTextPaint.linkColor = getColor(key_windowBackgroundWhiteLinkText);

        setDrawableColorByKey(profile_verifiedDrawable, key_profile_verifiedBackground);
        setDrawableColorByKey(profile_verifiedCheckDrawable, key_profile_verifiedCheck);
    }

    public static Drawable getThemedDrawable(Context context, int resId, String key) {
        return getThemedDrawable(context, resId, getColor(key));
    }

    public static Drawable getThemedDrawable(Context context, int resId, int color) {
        if (context == null) {
            return null;
        }
        Drawable drawable = context.getResources().getDrawable(resId).mutate();
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        return drawable;
    }

    public static int getDefaultColor(String key) {
        Integer value = defaultColors.get(key);
        if (value == null) {
            if (key.equals(key_chats_menuTopShadow)) {
                return 0;
            }
            return 0xffff0000;
        }
        return value;
    }

    public static boolean hasThemeKey(String key) {
        return currentColors.containsKey(key);
    }

    public static Integer getColorOrNull(String key) {
        Integer color = currentColors.get(key);
        if (color == null) {
            String fallbackKey = fallbackKeys.get(key);
            if (fallbackKey != null) {
                color = currentColors.get(key);
            }
            if (color == null) {
                color = defaultColors.get(key);
            }
        }
        return color;
    }

    public static boolean isAnimatingColor() {
        return animatingColors != null;
    }

    public static void setAnimatedColor(String key, int value) {
        if (animatingColors == null) {
            return;
        }
        animatingColors.put(key, value);
    }

    public static int getColor(String key) {
        return getColor(key, null);
    }

    public static int getColor(String key, boolean[] isDefault) {
        if (animatingColors != null) {
            Integer color = animatingColors.get(key);
            if (color != null) {
                return color;
            }
        }
        if (isCurrentThemeDefault()) {
            if (key.equals(key_chat_serviceBackground)) {
                return serviceMessageColor;
            } else if (key.equals(key_chat_serviceBackgroundSelected)) {
                return serviceSelectedMessageColor;
            }
            return getDefaultColor(key);
        }
        Integer color = currentColors.get(key);
        if (color == null) {
            String fallbackKey = fallbackKeys.get(key);
            if (fallbackKey != null) {
                color = currentColors.get(fallbackKey);
            }
            if (color == null) {
                if (isDefault != null) {
                    isDefault[0] = true;
                }
                if (key.equals(key_chat_serviceBackground)) {
                    return serviceMessageColor;
                } else if (key.equals(key_chat_serviceBackgroundSelected)) {
                    return serviceSelectedMessageColor;
                }
                return getDefaultColor(key);
            }
        }
        return color;
    }

    public static void setColor(String key, int color, boolean useDefault) {
        if (key.equals(key_chat_wallpaper)) {
            color = 0xff000000 | color;
        }

        if (useDefault) {
            currentColors.remove(key);
        } else {
            currentColors.put(key, color);
        }

        if (key.equals(key_chat_serviceBackground) || key.equals(key_chat_serviceBackgroundSelected)) {
            applyChatServiceMessageColor();
        }
    }


    public static void setDrawableColor(Drawable drawable, int color) {
        if (drawable == null) {
            return;
        }
        if (drawable instanceof ShapeDrawable) {
            ((ShapeDrawable) drawable).getPaint().setColor(color);
        } else {
            drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        }
    }

    public static void setDrawableColorByKey(Drawable drawable, String key) {
        if (key == null) {
            return;
        }
        setDrawableColor(drawable, getColor(key));
    }

    public static void setEmojiDrawableColor(Drawable drawable, int color, boolean selected) {
        if (drawable instanceof StateListDrawable) {
            try {
                if (selected) {
                    Drawable state = getStateDrawable(drawable, 0);
                    if (state instanceof ShapeDrawable) {
                        ((ShapeDrawable) state).getPaint().setColor(color);
                    } else {
                        state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                } else {
                    Drawable state = getStateDrawable(drawable, 1);
                    if (state instanceof ShapeDrawable) {
                        ((ShapeDrawable) state).getPaint().setColor(color);
                    } else {
                        state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                }
            } catch (Throwable ignore) {

            }
        }
    }

    public static void setSelectorDrawableColor(Drawable drawable, int color, boolean selected) {
        if (drawable instanceof StateListDrawable) {
            try {
                if (selected) {
                    Drawable state = getStateDrawable(drawable, 0);
                    if (state instanceof ShapeDrawable) {
                        ((ShapeDrawable) state).getPaint().setColor(color);
                    } else {
                        state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                    state = getStateDrawable(drawable, 1);
                    if (state instanceof ShapeDrawable) {
                        ((ShapeDrawable) state).getPaint().setColor(color);
                    } else {
                        state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                } else {
                    Drawable state = getStateDrawable(drawable, 2);
                    if (state instanceof ShapeDrawable) {
                        ((ShapeDrawable) state).getPaint().setColor(color);
                    } else {
                        state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                }
            } catch (Throwable ignore) {

            }
        } else if (Build.VERSION.SDK_INT >= 21 && drawable instanceof RippleDrawable) {
            RippleDrawable rippleDrawable = (RippleDrawable) drawable;
            if (selected) {
                rippleDrawable.setColor(new ColorStateList(
                        new int[][]{StateSet.WILD_CARD},
                        new int[]{color}
                ));
            } else {
                if (rippleDrawable.getNumberOfLayers() > 0) {
                    Drawable drawable1 = rippleDrawable.getDrawable(0);
                    if (drawable1 instanceof ShapeDrawable) {
                        ((ShapeDrawable) drawable1).getPaint().setColor(color);
                    } else {
                        drawable1.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                }
            }
        }
    }

    public static boolean hasWallpaperFromTheme() {
        return currentColors.containsKey(key_chat_wallpaper) || themedWallpaperFileOffset > 0;
    }

    public static boolean isCustomTheme() {
        return isCustomTheme;
    }

    public static int getSelectedColor() {
        return selectedColor;
    }

    private static void calcBackgroundColor(Drawable drawable, int save) {
        if (save != 2) {
            int[] result = AndroidUtilities.calcDrawableColor(drawable);
            serviceMessageColor = serviceMessageColorBackup = result[0];
            serviceSelectedMessageColor = serviceSelectedMessageColorBackup = result[1];
            serviceMessage2Color = result[2];
            serviceSelectedMessage2Color = result[3];
        }
    }

    public static int getServiceMessageColor() {
        Integer serviceColor = currentColors.get(key_chat_serviceBackground);
        return serviceColor == null ? serviceMessageColor : serviceColor;
    }

    public static Drawable getCachedWallpaperNonBlocking() {
        if (themedWallpaper != null) {
            return themedWallpaper;
        } else {
            return wallpaper;
        }
    }

    public static boolean isWallpaperMotion() {
        return isWallpaperMotion;
    }

    public static boolean isPatternWallpaper() {
        return isPatternWallpaper;
    }
}