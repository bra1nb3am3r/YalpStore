package com.github.yeriomin.yalpstore;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.github.yeriomin.yalpstore.model.App;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class AppListActivity extends YalpStoreActivity {

    protected static final String LINE1 = "LINE1";
    protected static final String LINE2 = "LINE2";
    protected static final String LINE3 = "LINE3";
    protected static final String ICON = "ICON";
    protected static final String PACKAGE_NAME = "PACKAGE_NAME";

    protected List<Map<String, Object>> data = new ArrayList<>();

    protected ListAdapter listAdapter;
    protected ListView listView;

    private boolean finishedStart = false;

    abstract protected void loadApps();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applist_activity_layout);

        setListAdapter(getSimpleListAdapter());
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DetailsActivity.start(getApplicationContext(), (String) data.get(position).get(PACKAGE_NAME));
            }
        });
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        View emptyView = findViewById(android.R.id.empty);
        listView = (ListView) findViewById(android.R.id.list);
        if (emptyView != null) {
            listView.setEmptyView(emptyView);
        }
        if (finishedStart) {
            setListAdapter(listAdapter);
        }
        finishedStart = true;
    }

    protected Map<String, Object> formatApp(App app) {
        Map<String, Object> map = new HashMap<>();
        map.put(LINE1, app.getDisplayName());
        map.put(PACKAGE_NAME, app.getPackageName());
        return map;
    }

    protected void addApps(List<App> apps) {
        for (App app: apps) {
            data.add(this.formatApp(app));
        }
        ((SimpleAdapter) getListAdapter()).notifyDataSetChanged();
    }

    private SimpleAdapter getSimpleListAdapter() {

        String[] from = { LINE1, LINE2, LINE3, ICON };
        int[] to = { R.id.text1, R.id.text2, R.id.text3, R.id.icon  };

        SimpleAdapter adapter = new SimpleAdapter(
            this,
            data,
            R.layout.two_line_list_item_with_icon,
            from,
            to);

        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Object value, String textRepresentation) {
                if (view instanceof TextView) {
                    return setViewValue((TextView) view, value);
                } else if (view instanceof ImageView) {
                    return setViewValue((ImageView) view, value);
                }
                return false;
            }

            private boolean setViewValue(TextView view, Object value) {
                if (!(value instanceof String) || ((String) value).isEmpty()) {
                    view.setVisibility(View.GONE);
                }
                return false;
            }

            private boolean setViewValue(ImageView view, Object drawableOrUrl) {
                if (drawableOrUrl instanceof String) {
                    ImageDownloadTask task = new ImageDownloadTask();
                    task.setView(view);
                    task.execute((String) drawableOrUrl);
                } else {
                    view.setImageDrawable((Drawable) drawableOrUrl);
                }
                return true;
            }
        });
        return adapter;
    }

    public void setListAdapter(ListAdapter adapter) {
        synchronized (this) {
            listAdapter = adapter;
            listView.setAdapter(adapter);
        }
    }

    public ListView getListView() {
        return listView;
    }

    public ListAdapter getListAdapter() {
        return listAdapter;
    }
}
