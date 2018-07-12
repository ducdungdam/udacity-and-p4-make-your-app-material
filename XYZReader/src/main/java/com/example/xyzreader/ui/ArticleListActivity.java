package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.utils.FormatUtils;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
    LoaderManager.LoaderCallbacks<Cursor> {

  private static final String TAG = ArticleListActivity.class.toString();
  private SwipeRefreshLayout mSwipeRefreshLayout;
  private RecyclerView mRecyclerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_article_list);

    mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

    mRecyclerView = findViewById(R.id.recycler_view);
    getLoaderManager().initLoader(0, null, this);

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowTitleEnabled(false);
    }


    if (savedInstanceState == null) {
      refresh();
    }
  }

  private void refresh() {
    startService(new Intent(this, UpdaterService.class));
  }

  @Override
  protected void onStart() {
    super.onStart();
    registerReceiver(mRefreshingReceiver,
        new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
  }

  @Override
  protected void onStop() {
    super.onStop();
    unregisterReceiver(mRefreshingReceiver);
  }

  private boolean mIsRefreshing = false;

  private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
        mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
        updateRefreshingUI();
      }
    }
  };

  private void updateRefreshingUI() {
    mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return ArticleLoader.newAllArticlesInstance(this);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    Adapter adapter = new Adapter(cursor);
    adapter.setHasStableIds(true);
    mRecyclerView.setAdapter(adapter);
    int columnCount = getResources().getInteger(R.integer.list_column_count);
    StaggeredGridLayoutManager sglm =
        new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
    mRecyclerView.setLayoutManager(sglm);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mRecyclerView.setAdapter(null);
  }

  private class Adapter extends RecyclerView.Adapter<ViewHolder> {

    private Cursor mCursor;

    public Adapter(Cursor cursor) {
      mCursor = cursor;
    }

    @Override
    public long getItemId(int position) {
      mCursor.moveToPosition(position);
      return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
      final ViewHolder vh = new ViewHolder(view);
      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          startActivity(new Intent(Intent.ACTION_VIEW,
              ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))));
        }
      });
      return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      mCursor.moveToPosition(position);
      holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
      holder.subtitleView.setText(String.format(
          "%s\nby %s",
          FormatUtils.formatDate(mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE)),
          mCursor.getString(ArticleLoader.Query.AUTHOR)
      ));

      holder.thumbnailView.setImageUrl(
          mCursor.getString(ArticleLoader.Query.THUMB_URL),
          ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
      holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
    }

    @Override
    public int getItemCount() {
      return mCursor.getCount();
    }
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    public DynamicHeightNetworkImageView thumbnailView;
    public TextView titleView;
    public TextView subtitleView;

    public ViewHolder(View view) {
      super(view);
      thumbnailView = view.findViewById(R.id.article_thumbnail);
      titleView = view.findViewById(R.id.article_title);
      subtitleView = view.findViewById(R.id.article_subtitle);
    }
  }
}
