package com.hiennd325.btl_android.model;

import com.hiennd325.btl_android.R;

public enum Category {
    HEALTH("Health", "HEALTH", R.color.cat_health_bg, R.color.cat_health_accent, R.color.cat_health_badge_bg, R.color.cat_health_badge_text, R.drawable.ic_heart),
    WORK("Work", "WORK", R.color.cat_work_bg, R.color.cat_work_accent, R.color.cat_work_badge_bg, R.color.cat_work_badge_text, R.drawable.ic_work),
    MENTAL_HEALTH("Mental Health", "MENTAL HEALTH", R.color.cat_mental_bg, R.color.cat_mental_accent, R.color.cat_mental_badge_bg, R.color.cat_mental_badge_text, R.drawable.ic_mental_health),
    OTHERS("Others", "OTHERS", R.color.cat_others_bg, R.color.cat_others_accent, R.color.cat_others_badge_bg, R.color.cat_others_badge_text, R.drawable.ic_folder);

    private final String displayName;
    private final String badgeText;
    private final int cardBgColorRes;
    private final int accentColorRes;
    private final int badgeBgRes;
    private final int badgeTextColorRes;
    private final int iconRes;

    Category(String displayName, String badgeText, int cardBgColorRes, int accentColorRes, int badgeBgRes, int badgeTextColorRes, int iconRes) {
        this.displayName = displayName;
        this.badgeText = badgeText;
        this.cardBgColorRes = cardBgColorRes;
        this.accentColorRes = accentColorRes;
        this.badgeBgRes = badgeBgRes;
        this.badgeTextColorRes = badgeTextColorRes;
        this.iconRes = iconRes;
    }

    public String getDisplayName() { return displayName; }
    public String getBadgeText() { return badgeText; }
    public int getCardBgColorRes() { return cardBgColorRes; }
    public int getAccentColorRes() { return accentColorRes; }
    public int getBadgeBgRes() { return badgeBgRes; }
    public int getBadgeTextColorRes() { return badgeTextColorRes; }
    public int getIconRes() { return iconRes; }

    public static Category fromString(String text) {
        if (text == null) return OTHERS;
        for (Category c : values()) {
            if (c.name().equalsIgnoreCase(text) || c.displayName.equalsIgnoreCase(text)) {
                return c;
            }
        }
        return OTHERS;
    }
}
