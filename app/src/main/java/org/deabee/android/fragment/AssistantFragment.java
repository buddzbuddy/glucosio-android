/*
 * Copyright (C) 2016 Glucosio Foundation
 *
 * This file is part of Glucosio.
 *
 * Glucosio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Glucosio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Glucosio.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.deabee.android.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.deabee.android.GlucosioApplication;
import org.deabee.android.R;
import org.deabee.android.activity.A1cCalculatorActivity;
import org.deabee.android.activity.AddGlucoseActivity;
import org.deabee.android.activity.MainActivity;
import org.deabee.android.adapter.AssistantAdapter;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.object.ActionTip;

import java.io.InputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AssistantFragment extends Fragment {

    private SharedPreferences sharedPref;

    @BindView(R.id.fragment_tips_recyclerview)
    RecyclerView tipsRecycler;
    @BindView(R.id.fragment_assistant_archived)
    LinearLayout archivedButton;
    @BindView(R.id.fragment_assistant_archived_dismiss)
    LinearLayout archivedDismissButton;


    private GlucosioApplication application;

    private AssistantAdapter adapter;
    private ArrayList<ActionTip> actionTips;
    private String[] actionTipTitles;
    private String[] actionTipDescriptions;
    private String[] actionTipActions;

    private Unbinder unbinder;

    public static AssistantFragment newInstance() {
        return new AssistantFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        application = (GlucosioApplication) getActivity().getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assistant, container, false);
        unbinder = ButterKnife.bind(this, view);
        final DatabaseHandler dbHandler = application.getDBHandler();
        String server = dbHandler.getAppSettings().IP_ADDRESS;
        new DownloadImageFromInternet((ImageView) view.findViewById(R.id.imageView5))
                .execute("https://firebasestorage.googleapis.com/v0/b/amcrud-69b6c.appspot.com/o/screenshot_1579614387.jpg?alt=media&token=00b699b0-891b-44b8-a516-901f6f6a1708");
        new DownloadImageFromInternet((ImageView) view.findViewById(R.id.imageView6))
                .execute("https://firebasestorage.googleapis.com/v0/b/amcrud-69b6c.appspot.com/o/screenshot_1577304874.png?alt=media&token=4395d712-7cdf-402a-b756-71a3e4db410b");
        new DownloadImageFromInternet((ImageView) view.findViewById(R.id.imageView7))
                .execute("https://firebasestorage.googleapis.com/v0/b/amcrud-69b6c.appspot.com/o/screenshot_1577304956.png?alt=media&token=8e897cd2-d5cc-4004-a972-ce2e0b7b0b93");
        return view;
        /*AssistantPresenter presenter = new AssistantPresenter(this);
        actionTips = new ArrayList<>();

        actionTipTitles = getResources().getStringArray(R.array.assistant_titles);
        actionTipDescriptions = getResources().getStringArray(R.array.assistant_descriptions);
        actionTipActions = getResources().getStringArray(R.array.assistant_actions);
        populateWithNewTips();


        adapter = new AssistantAdapter(presenter, getActivity().getApplicationContext().getResources(), actionTips);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        tipsRecycler.setLayoutManager(llm);
        tipsRecycler.setAdapter(adapter);
        tipsRecycler.setHasFixedSize(false);

        initSwipeToRemoveTouchHelper();

        // If there aren't dismissed tips, don't show archive button
        if (actionTipTitles.length == adapter.getItemCount()) {
            archivedButton.setVisibility(View.GONE);
        }

        return view;*/
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();

        super.onDestroyView();
    }

    // Swipe to remove functionality
    private void initSwipeToRemoveTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (archivedDismissButton.getVisibility() == View.VISIBLE) {
                    // If we're in archive, restore tips
                    TextView title = (TextView) viewHolder.itemView.findViewById(R.id.fragment_assistant_item_title);
                    removePreference(title.getText().toString());

                    int position = viewHolder.getAdapterPosition();
                    actionTips.remove(position);
                    adapter.notifyDataSetChanged();
                } else {
                    // Else archive them
                    TextView title = (TextView) viewHolder.itemView.findViewById(R.id.fragment_assistant_item_title);
                    addPreference(title.getText().toString());

                    int position = viewHolder.getAdapterPosition();
                    actionTips.remove(position);
                    adapter.notifyDataSetChanged();

                    ((MainActivity) getActivity()).reloadFragmentAdapter();
                }
            }
        };

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(tipsRecycler);
    }

    @OnClick(R.id.fragment_assistant_archived_dismiss)
    void archivedDismissButtonClick() {
        populateWithNewTips();
        adapter.notifyDataSetChanged();
        tipsRecycler.swapAdapter(adapter, false);
        archivedDismissButton.setVisibility(View.GONE);
        archivedButton.setVisibility(View.VISIBLE);

        ((MainActivity) getActivity()).reloadFragmentAdapter();
    }

    @OnClick(R.id.fragment_assistant_archived)
    void archivedButtonClicked() {
        populateWithArchivedTips();
        adapter.notifyDataSetChanged();
        tipsRecycler.swapAdapter(adapter, false);
        archivedDismissButton.setVisibility(View.VISIBLE);
        final Animation slide = new TranslateAnimation(0, 0, 0, 200);
        slide.setDuration(500);

        archivedButton.startAnimation(slide);
        archivedButton.setVisibility(View.GONE);
    }

    private void populateWithNewTips() {
        actionTips.clear();
        for (int i = 0; i < actionTipTitles.length; i++) {
            String actionTipTitle = actionTipTitles[i];
            String actionTipDescription = actionTipDescriptions[i];
            String actionTipAction = actionTipActions[i];

            ActionTip actionTip = new ActionTip();
            actionTip.setTipTitle(actionTipTitle);
            actionTip.setTipDescription(actionTipDescription);
            actionTip.setTipAction(actionTipAction);

            Boolean value = sharedPref.getBoolean(actionTipTitle, false);
            if (!value) {
                actionTips.add(actionTip);
            }
        }
    }

    private void populateWithArchivedTips() {
        actionTips.clear();
        for (int i = 0; i < actionTipTitles.length; i++) {
            String actionTipTitle = actionTipTitles[i];
            String actionTipDescription = actionTipDescriptions[i];
            String actionTipAction = actionTipActions[i];

            ActionTip actionTip = new ActionTip();
            actionTip.setTipTitle(actionTipTitle);
            actionTip.setTipDescription(actionTipDescription);
            actionTip.setTipAction(actionTipAction);

            Boolean value = sharedPref.getBoolean(actionTipTitle, false);
            if (value) {
                actionTips.add(actionTip);
            }
        }
    }

    private void addPreference(String key) {
        sharedPref.edit().putBoolean(key, true).apply();
    }

    private void removePreference(String key) {
        sharedPref.edit().putBoolean(key, false).apply();
    }

    public void addReading() {
        Intent intent = new Intent(getActivity(), AddGlucoseActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    public void startExportActivity() {
        ((MainActivity) getActivity()).showExportCsvDialog();
    }

    public void startA1CCalculatorActivity() {
        Intent intent = new Intent(getActivity(), A1cCalculatorActivity.class);
        startActivity(intent);
    }

    public void openSupportDialog() {
        ((MainActivity) getActivity()).openSupportDialog();
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
            //Toast.makeText(getContext(), "Загружаю картинки...", Toast.LENGTH_SHORT).show();
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
                Toast.makeText(getContext(), "Ошибка при загрузке картинки: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}