package com.hteck.playtube.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.ItemYoutubeViewBinding;
import com.hteck.playtube.service.HistoryService;
import com.hteck.playtube.service.PlaylistService;
import com.hteck.playtube.view.ItemYoutubeViewHolder;
import com.hteck.playtube.view.PlaylistsDialogView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.util.List;
import java.util.UUID;

import static com.nostra13.universalimageloader.utils.MemoryCacheUtils.findCachedBitmapsForImageUri;

public class ViewHelper {
    public static View getYoutubeView(View convertView, ViewGroup group, YoutubeInfo youtubeInfo, View.OnClickListener onClickListener) {
//        View v = getConvertView(convertView, R.layout.item_youtube_view);

        LayoutInflater inflater = MainActivity.getInstance().getLayoutInflater();
//        View v;
        ItemYoutubeViewHolder holder;
        if (convertView == null) {
            ItemYoutubeViewBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.item_youtube_view, group, false);
            holder = new ItemYoutubeViewHolder(itemBinding);
            holder.view = itemBinding.getRoot();
            holder.view.setTag(holder);
//            holder.view.setId((int) R.layout.item_youtube_view);
        } else {
//            if (convertView.getId() != (int) R.layout.item_youtube_view) {
//                ItemYoutubeViewBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.item_youtube_view, group, false);
//                holder = new ItemYoutubeViewHolder(itemBinding);
//                holder.view = itemBinding.getRoot();
//                holder.view.setTag(holder);
//                holder.view.setId((int) R.layout.item_youtube_view);
//            } else {
                holder = (ItemYoutubeViewHolder) convertView.getTag();
//            }
        }
        try {
            if (youtubeInfo == null) {
                return holder.view;
            }
            holder.binding.itemYoutubeTvTitle.setText(youtubeInfo.title.toUpperCase());
            displayYoutubeThumb(holder.binding.itemYoutubeImgThumb, youtubeInfo.imageUrl);
            holder.binding.itemYoutubeTvUploadedDate.setText(youtubeInfo.uploadedDate);
            holder.binding.itemYoutubeTvPlaysNo.setText(Utils.getDisplayViews(youtubeInfo.viewsNo, false));
            holder.binding.itemYoutubeTvLikesNo.setText(Utils.getDisplayLikes(youtubeInfo.likesNo, false));
            holder.binding.itemYoutubeTvTime.setText(Utils.getDisplayTime((int) youtubeInfo.duration));
            holder.binding.itemYoutubeTvUploader.setText(youtubeInfo.uploaderName);
            holder.binding.itemYoutubeImgAction.setTag(youtubeInfo);
            holder.binding.itemYoutubeImgAction.setOnClickListener(onClickListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return holder.view;
    }

    public static View getGridYoutubeView(View convertView, YoutubeInfo youtubeInfo, View.OnClickListener onClickListener) {
        View v = getConvertView(convertView, R.layout.grid_item_youtube_view);

        try {
            TextView textViewTitle = v.findViewById(R.id.item_youtube_tv_title);
            textViewTitle.setText(youtubeInfo.title.toUpperCase());

            ImageView iv = v.findViewById(R.id.item_youtube_img_thumb);
            displayYoutubeThumb(iv, youtubeInfo.imageUrl);
            if (iv.getWidth() == 0) {
                int width = Utils.getYoutubeWidth();
                int height = width * 18 / 32;
                iv.setLayoutParams(new FrameLayout.LayoutParams(width, height));
            }
            TextView textViewUploadedDate = v.findViewById(R.id.item_youtube_tv_uploaded_date);
            textViewUploadedDate.setText(youtubeInfo.uploadedDate);
            TextView textViewPlaysNo = v.findViewById(R.id.item_youtube_tv_plays_no);
            textViewPlaysNo.setText(Utils.getDisplayViews(youtubeInfo.viewsNo, true));

            TextView textViewTime = v.findViewById(R.id.item_youtube_tv_time);
            textViewTime.setText(Utils.getDisplayTime((int) youtubeInfo.duration));
            TextView textViewUploader = v.findViewById(R.id.item_youtube_tv_uploader);
            textViewUploader.setText(youtubeInfo.uploaderName);

            ImageView imageViewAction = v
                    .findViewById(R.id.item_youtube_img_action);

            imageViewAction.setTag(youtubeInfo);
            imageViewAction.setOnClickListener(onClickListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return v;
    }

    public static PopupMenu showYoutubeMenu(Constants.YoutubeListType youtubeListType,
                                            final Object dataContext, final View vDock) {
        PopupMenu popup = new PopupMenu(MainActivity.getInstance(), vDock);

        popup.getMenuInflater().inflate(R.menu.item_video, popup.getMenu());
        final YoutubeInfo youtubeInfo = (YoutubeInfo) vDock.getTag();
        if (youtubeListType != Constants.YoutubeListType.Playlist && youtubeListType != Constants.YoutubeListType.Recent) {
            popup.getMenu().removeItem(R.id.menu_item_remove);
        }
        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    if (item.getItemId() == R.id.menu_item_add_to_playlist) {
                        showPlaylistsToAdd(youtubeInfo);
                    } else if (item.getItemId() == R.id.menu_item_add_to_favourites) {
                        PlaylistService.addYoutubeToFavouritePlaylist(youtubeInfo);
                        Utils.showMessage(Utils.getString(R.string.added));
                        MainActivity.getInstance().refreshPlaylistData();
                    } else if (item.getItemId() == R.id.menu_item_share) {
                        Utils.shareVideo(youtubeInfo);
                    } else if (item.getItemId() == R.id.menu_item_remove) {
                        if (dataContext != null && dataContext instanceof PlaylistInfo) {
                            removeYoutubeFromPlaylist((PlaylistInfo) dataContext, youtubeInfo);
                        } else {
                            removeYoutubeFromHistory(youtubeInfo);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        return popup;
    }

    public static View getConvertView(View convertView, int resId) {
        LayoutInflater inflater = MainActivity.getInstance().getLayoutInflater();
        View v;
        if (convertView == null) {
            v = inflater.inflate(resId, null);
            v.setTag(Constants.CUSTOM_TAG, resId);
        } else {
            if (convertView.getTag(Constants.CUSTOM_TAG) == null || !convertView.getTag(Constants.CUSTOM_TAG).equals(resId)) {
                v = inflater.inflate(resId, null);
                v.setTag(Constants.CUSTOM_TAG, resId);
            } else {
                v = convertView;
            }
        }
        return v;
    }

    public static void displayYoutubeThumb(ImageView imageView, String url) {

        List<Bitmap> bitmaps = MemoryCacheUtils.findCachedBitmapsForImageUri(url, ImageLoader.getInstance().getMemoryCache());
        if (bitmaps.size() == 0) {
            imageView.setImageResource(R.drawable.ic_thumb);
            ImageLoader.getInstance().displayImage(
                    url, imageView);
        } else {
            System.out.println("dungnt: " + bitmaps.get(0).getByteCount());
            imageView.setImageBitmap(bitmaps.get(0));
        }
//        if (imageView.getTag() == null
//                || !imageView.getTag().toString()
//                .equalsIgnoreCase(url)) {
//            imageView.setTag(url);
//            imageView.setImageResource(R.drawable.ic_thumb);
//            if (!Utils.stringIsNullOrEmpty(url)) {
//                ImageLoader.getInstance().displayImage(
//                        url, imageView);
//            }
//        }
    }

    public static void showAddNewPlaylist(final YoutubeInfo youtubeInfo, final Dialog parent) {
        final View v = MainActivity.getInstance().getLayoutInflater().inflate(R.layout.edit_playlist, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.getInstance());
        builder.setPositiveButton(android.R.string.yes, null);
        builder.setNegativeButton(android.R.string.no, null);
        builder.setTitle(Utils.getString(R.string.add_new_playlist));
        builder.setView(v);
        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        try {
                            EditText editText = v.findViewById(R.id.edit_playlist_edit_text);
                            String title = editText.getText().toString().trim();
                            if (Utils.stringIsNullOrEmpty(title)) {
                                return;
                            }
                            PlaylistInfo playlistInfo = new PlaylistInfo();
                            playlistInfo.id = UUID.randomUUID();

                            playlistInfo.title = title;
                            PlaylistService.addPlaylist(playlistInfo, youtubeInfo);
                            Utils.showMessage(Utils.getString(R.string.added));
                            dialog.dismiss();
                            parent.dismiss();
                            MainActivity.getInstance().refreshPlaylistData();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    public static void showAddNewPlaylist(final PlaylistInfo playlistInfo) {
        final View v = MainActivity.getInstance().getLayoutInflater().inflate(R.layout.edit_playlist, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.getInstance());
        builder.setPositiveButton(android.R.string.yes, null);
        builder.setNegativeButton(android.R.string.no, null);
        builder.setTitle(Utils.getString(R.string.add_new_playlist));
        if (playlistInfo != null) {
            EditText editText = v.findViewById(R.id.edit_playlist_edit_text);
            editText.setText(playlistInfo.title);
        }
        builder.setView(v);
        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        try {
                            EditText editText = v.findViewById(R.id.edit_playlist_edit_text);
                            String title = editText.getText().toString().trim();
                            if (Utils.stringIsNullOrEmpty(title)) {
                                return;
                            }
                            PlaylistInfo playlistInfoNew = new PlaylistInfo();
                            if (playlistInfo == null) {
                                playlistInfoNew.id = UUID.randomUUID();
                            } else {
                                playlistInfoNew = playlistInfo;
                            }
                            playlistInfoNew.title = title;
                            if (playlistInfo == null) {
                                PlaylistService.addPlaylist(playlistInfoNew);
                            } else {
                                PlaylistService.updatePlaylist(playlistInfoNew);
                            }
                            Utils.showMessage(Utils.getString(R.string.added));
                            dialog.dismiss();
                            MainActivity.getInstance().refreshPlaylistData();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    public static void showPlaylistsToAdd(YoutubeInfo youtubeInfo) {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.getInstance());
        builder.setNegativeButton(android.R.string.no, null);

        final PlaylistsDialogView v = new PlaylistsDialogView(MainActivity.getInstance(), youtubeInfo);
        builder.setView(v);
        AlertDialog mAlertDialog = builder.create();
        v.mDialogParent = mAlertDialog;
        mAlertDialog.show();
    }

    private static void removeYoutubeFromPlaylist(final PlaylistInfo playlistInfo, final YoutubeInfo youtubeInfo) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.getInstance(),
                AlertDialog.THEME_HOLO_LIGHT);
        String msg = String.format(Utils.getString(R.string.remove_youtube_confirm), youtubeInfo.title);
        alert.setMessage(msg);

        alert.setPositiveButton(Utils.getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                PlaylistService.removeYoutube(playlistInfo, youtubeInfo);
                MainActivity.getInstance().refreshPlaylistData();
            }
        });

        alert.setNeutralButton(Utils.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alert.show();
    }

    private static void removeYoutubeFromHistory(final YoutubeInfo youtubeInfo) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.getInstance(),
                AlertDialog.THEME_HOLO_LIGHT);
        String msg = String.format(Utils.getString(R.string.remove_youtube_confirm), youtubeInfo.title);
        alert.setMessage(msg);

        alert.setPositiveButton(Utils.getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                HistoryService.removeYoutubeHistory(youtubeInfo.id);
                MainActivity.getInstance().refreshHistoryData();
            }
        });

        alert.setNeutralButton(Utils.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alert.show();
    }
}
