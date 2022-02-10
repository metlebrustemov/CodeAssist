package com.tyron.code.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutFragment;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.danielstone.materialaboutlibrary.util.OpenSourceLicense;
import com.tyron.code.BuildConfig;
import com.tyron.code.R;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutUsFragment extends MaterialAboutFragment {

    @Override
    protected MaterialAboutList getMaterialAboutList(Context context) {
        MaterialAboutCard appCard = new MaterialAboutCard.Builder()
                .addItem(ConvenienceBuilder.createAppTitleItem(context))
                .addItem(new MaterialAboutActionItem.Builder()
                        .subText(R.string.app_description)
                        .build())
                .addItem(ConvenienceBuilder.createVersionActionItem(context,
                        getDrawable(R.drawable.ic_round_info_24),
                        getString(R.string.app_version),
                        true))
                .addItem(ConvenienceBuilder.createEmailItem(context,
                        getDrawable(R.drawable.about_icon_email),
                        getString(R.string.about_contact_us),
                        false,
                        "contact.tyronscott@gmail.com",
                        ""))
                .addItem(ConvenienceBuilder.createWebsiteActionItem(context,
                        getDrawable(R.drawable.about_icon_github),
                        getString(R.string.about_github),
                        false,
                        Uri.parse("https://github.com/tyron12233/CodeAssist")))
                .addItem(ConvenienceBuilder.createRateActionItem(context,
                        getDrawable(R.drawable.ic_round_star_rate_24),
                        getString(R.string.rate_us),
                        null))
                .build();

        MaterialAboutCard communityCard = new MaterialAboutCard.Builder()
                .title(R.string.community)
                .addItem(ConvenienceBuilder.createWebsiteActionItem(context,
                        getDrawable(R.drawable.ic_icons8_discord),
                        "Discord",
                        false,
                        Uri.parse("https://discord.gg/pffnyE6prs")))
                .addItem(ConvenienceBuilder.createWebsiteActionItem(context,
                        getDrawable(R.drawable.ic_icons8_telegram_app),
                        "Telegram",
                        false,
                        Uri.parse("https://discord.gg/pffnyE6prs")))
                .build();

        MaterialAboutCard licenseCard = ConvenienceBuilder.createLicenseCard(context,
                getDrawable(R.drawable.ic_baseline_menu_book_24),
                getString(R.string.app_name),
                "2022",
                "Tyron",
                OpenSourceLicense.GNU_GPL_3);

        return new MaterialAboutList.Builder()
                .addCard(appCard)
                .addCard(communityCard)
                .addCard(licenseCard)
                .build();
    }

    private Drawable getDrawable(@DrawableRes int drawable) {
        return ResourcesCompat.getDrawable(requireContext().getResources(),
                drawable,
                requireContext().getTheme());
    }
}
