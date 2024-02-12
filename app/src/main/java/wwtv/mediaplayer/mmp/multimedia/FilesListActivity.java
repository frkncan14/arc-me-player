
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.util.List;

import com.mediatek.wwtv.mediaplayer.mmp.commonview.MediaGridView;
import com.mediatek.wwtv.mediaplayer.mmp.util.Lists;

import java.util.Observable;
import java.util.Observer;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.AsyncTask.Status;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;

/**
 *
 * The base activity can be used to show file list
 *
 * @author hs_weihuiluo
 * @param <T>
 */
public class FilesListActivity<T extends FileAdapter> extends Activity
    implements OnItemSelectedListener, OnItemClickListener, OnScrollListener, Observer {
  /**
   * Log tag
   */
  private static final String TAG = "FilesListActivity";

  /**
   * List view
   */
  protected AbsListView vList;

  /**
   * ListView adapter
   */
  protected FilesAdapter mAdapter;

  /**
   * Files manager
   */
  protected FilesManager<T> mFilesManager;

  /**
   * Asynchronous load files task
   */
  protected LoadFilesTask mTask;

  /**
   * Is show wait tips dialog
   */
  private boolean mLoadingShow;

  protected List<T> mLoadFiles;
  /**
   * Handler used to send message
   */
  private final Handler mHandler = new Handler();

  /**
   * Show tips dialog thread
   */
  private final Runnable mShowLoading = new Runnable() {
    @Override
    public void run() {
      mLoadingShow = true;
      onPreLoadFiles();
    }
  };

  /**
   * Request focus thread
   */
  private final Runnable mRequestFocus = new Runnable() {
    @Override
    public void run() {
      vList.focusableViewAvailable(vList);
    }
  };

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(setupContentView());
    mLoadFiles = Lists.newArrayList();
    init();
    setFilesManager(setupFilesManager());
    setupListView();
    MtkLog.d(TAG, "onCreate for bindDate");
    bindData(getIntent());
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {

    MtkLog.d(TAG, "-----------onSaveInstanceState ---------");
    super.onSaveInstanceState(outState);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onNewIntent(Intent intent) {
    MtkLog.d(TAG, "onNewIntent for bindDate");
    bindData(intent);
  }

  /**
   * Initialization view and resources
   */
  protected void init() {
  	 MtkLog.i(TAG, "init");
  }

  /**
   * Get intent data from other activity
   *
   * @param intent
   *            : Used to transfer data
   */
  protected void bindData(Intent intent) {
  	 MtkLog.i(TAG, "bindData");
  }

  /**
   * Load the files in the path
   *
   * @param path
   *            :A file path
   */
  protected void loadFiles(String path) {
    mTask = new LoadFilesTask();
    mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
  }

  /**
   * Cancel the current load files task
   */
  protected void cancelLoadFiles() {
    try {
      mHandler.removeCallbacks(mShowLoading);
      if (mLoadingShow) {
        onLoadedFiles();
        mLoadingShow = false;
      }
    } catch (Exception e) {
      MtkLog.i(TAG, "cancelLoadFiles exception");

    }
    if (mTask != null && mTask.getStatus() != Status.FINISHED) {
      MtkLog.i(TAG, "Cancel Last Task !!");
      mTask.cancel(false);
    }
  }

  /**
   * When load files task return null or size==0,show empty tips dialog
   */
  protected void onListEmpty() {
    MtkLog.i(TAG, "List is empty!!");
  }

  /**
   * When load files task return not empty call this method to refresh the
   * interface
   */
  protected void onListNotEmpty() {
    // MtkLog.i(TAG, "List is not empty!!");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onContentChanged() {
    View emptyView = findViewById(R.id.empty);
    vList = (AbsListView) findViewById(R.id.list);
    if (vList == null) {
      throw new RuntimeException(
          "Your content must have a ListView whose id attribute is "
              + "'android.R.id.list'");
    }
    if (emptyView != null) {
      vList.setEmptyView(emptyView);
    }

    mHandler.post(mRequestFocus);
  }

  /**
   * Setup ListView listener and update adapter
   */
  protected void setupListView() {
    vList.setOnItemSelectedListener(this);
    vList.setOnItemClickListener(this);
    vList.setOnScrollListener(this);

    setListAdapter(getAdapter());
  }

  /**
   *
   * Set ListView adapter(will update the UI with adapter data)
   *
   * @param adapter
   *            FilesAdapter
   */
  public void setListAdapter(FilesAdapter adapter) {
    mAdapter = adapter;
    vList.setAdapter(adapter);
  }

  /**
   * Set filesManager
   *
   * @param manager
   *            FilesManager
   */
  public void setFilesManager(FilesManager<T> manager) {
    mFilesManager = manager;
  }

  /**
   * Set up activity contentView
   *
   * @return the layout resource id
   */
  protected int setupContentView() {
    return 0;
  }

  /**
   * Get a listView files adapter
   *
   * @return FilesAdapter
   */
  protected FilesAdapter getAdapter() {
    return null;
  }

  /**
   * Get a files Manager
   *
   * @return FilesManager
   */
  protected FilesManager<T> setupFilesManager() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onStop() {
    cancelLoadFiles();
    super.onStop();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onDestroy() {
    mHandler.removeCallbacks(mRequestFocus);
    mHandler.removeCallbacks(mShowLoading);
    super.onDestroy();
  }

  /**
   * Open a directory with the current path
   *
   * @param path
   *            an absolute file path
   */
  protected void openDir(String path) {
    openDir(path, 0);
  }

  /**
   * Open a directory with the current path and set the current selection
   *
   * @param path
   *            an absolute file path
   * @param selection
   *            the current selected item positon
   */
  protected void openDir(String path, int selection) {
    MtkLog.d(TAG, "Open : " + path);
    cancelLoadFiles();
  }

  /**
   * Play media file with the current path
   *
   * @param path
   *            an absolute file path
   */
  protected void playFile(String path) {
    MtkLog.d(TAG, "Play : " + path);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position,
      long id) {
    onListItemSelected((AbsListView) parent, view, position, id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    onListNothingSelected((AbsListView) parent);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position,
      long id) {
    onListItemClick((AbsListView) parent, view, position, id);
  }

  protected void onListItemSelected(AbsListView l, View v, int position,
      long id) {
      MtkLog.d(TAG, "onListItemSelected");
  }

  protected void onListNothingSelected(AbsListView parent) {
  	MtkLog.d(TAG, "onListNothingSelected");
  }

  protected void onListItemClick(AbsListView l, View v, int position, long id) {
	MtkLog.d(TAG, "onListItemClick");
  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
	  MtkLog.d(TAG, "onScrollStateChanged");

  };

  @Override
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
      int totalItemCount) {
      MtkLog.d(TAG, "onScroll");
  };

  /**
   * Set current selection item with the position
   *
   * @param position
   *            the position you want to focus
   */
  public void setListSelection(int position) {
    if (vList instanceof ListView) {
      // add by shengle for fix CR DTV00442465
      int itemHeight = ScreenConstant.SCREEN_HEIGHT / 18 + ((ListView) vList).getDividerHeight();
      vList.requestFocusFromTouch();
      ((ListView) vList).setSelectionFromTop(position, itemHeight * 6);
    } else {
      vList.setSelection(position);
    }
  }

  /**
   * Get the current selected item position
   *
   * @return position of current selected item
   */
  public int getListSelectedItemPosition() {
    return vList.getSelectedItemPosition();
  }

  /**
   * Get the current selected item resource id
   *
   * @return current selected item resource id
   */
  public long getListSelectedItemId() {
    return vList.getSelectedItemId();
  }

  /**
   * Get the current selected item object
   *
   * @return current selected item
   */
  public T getListSelectedItem() {
    int pos = getListSelectedItemPosition();
    return getListItem(pos);
  }

  /**
   * Get a item object with the position
   *
   * @param position
   *            the item in listView data position
   * @return a filesAdapter or null(if the position<0 or position>size)
   */
  public T getListItem(int position) {
    if (position >= 0 && position < mFilesManager.getFilesCount()) {
      return mFilesManager.getFile(position);
    }
    return null;
  }

  /**
   * Get total files count
   *
   * @return files count
   */
  public int getListItemsCount() {
    return mFilesManager.getFilesCount();
  }

  /**
   * Get files sets;
   *
   * @return files list sets
   */
  public void setListItems(List<T> list) {
    mLoadFiles.clear();
    if (list != null) {
      mLoadFiles.addAll(list);
    }
    // return mFilesManager.getCurrentFiles();
  }

  /**
   * Get files sort type
   *
   * @return sort type(by name,by date ...)
   */
  public int getListSortType() {
    return mFilesManager.getSortType();
  }

  /**
   * Set files sort type
   *
   * @param sortType
   *            final int constants :SORT_BY_NAME SORT_BY_DATE etc
   */
  public void setListSortType(int sortType) {
    mFilesManager.setSortType(sortType);
  }

  /**
   * Set files type
   *
   * @param contentType
   *            multi-media type:photo audio video text
   */
  public void setListContentType(int contentType) {
    mFilesManager.setContentType(contentType);
  }

  /**
   * Get files type
   *
   * @return multi-media type:photo audio video text
   */
  public int getListContentType() {
    return mFilesManager.getContentType();
  }

  /**
   * Get root directory path
   *
   * @return string path
   */
  public String getListRootPath() {
    return mFilesManager.getRootPath();
  }

  /**
   * Get parent directory path
   *
   * @return string path
   */
  public String getListParentPath() {
    return mFilesManager.getParentPath();
  }

  /**
   * Get current directory path
   *
   * @return sting path
   */
  public String getListCurrentPath() {
    return mFilesManager.getCurrentPath();
  }

  /**
   * Set current directory path
   *
   * @param path
   *            sting absolute path
   */
  public void setListCurrentPath(String path) {
    mFilesManager.setCurrentPath(path);
  }

  public AbsListView getListView() {
    return vList;
  }

  public ListAdapter getListAdapter() {
    return mAdapter;
  }

  protected class FilesAdapter extends EfficientAdapter<T> {
    public FilesAdapter(Context context) {
      super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void bindView(View v, T data, int position) {
    	MtkLog.d(TAG, "bindView");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getItemLayout() {
      return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initView(View v, T data, boolean flag) {
    	MtkLog.d(TAG, "initView");
    }

    protected  boolean isSamePath(View v, T data){
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected void updateThumbnail() {
    	MtkLog.d(TAG, "updateThumbnail");
    }

    /**
     * cancel load thumbnail
     */
    protected void cancel() {
    	MtkLog.d(TAG, "cancel");
    }

    /**
     * stop the current load thumbnail thread
     */
    protected void stop() {
    	MtkLog.d(TAG, "stop");
    }

    /**
     * clear the current mRunnable
     */
    public void clearWork()
    {
		MtkLog.d(TAG, "clearWork");

    }
  }

  public abstract class EfficientAdapter<E> extends BaseAdapter {
    private LayoutInflater mInflater;
    private int mItemLayout;
    protected List<E> mDataList;

    public EfficientAdapter(Context context) {
      mInflater = LayoutInflater.from(context);
      mItemLayout = getItemLayout();
    }

    public EfficientAdapter(Context context, List<E> dataList) {
      this(context);
      mDataList = dataList;
    }

    public void setDataSource(List<E> dataList) {
      mDataList = dataList;
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {

      int size = 0;

      if (mDataList != null) {
        size = mDataList.size();
      }

      return size;

    }

    @Override
    public E getItem(int position) {
      if (mDataList == null || mDataList.size() <= position) {
        return null;
      }

      return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      boolean isSamePath = false;
      if (null != convertView){
        isSamePath = isSamePath(convertView, getItem(position));
      }

      if (isSamePath){
        initView(convertView, getItem(position), true);
      } else {
        convertView = newView(parent);
        initView(convertView, getItem(position), false);
      }

        if(position == 0) {
//            if(parent.getChildCount() == position) {
                if(parent instanceof MediaGridView) {
                    if(!((MediaGridView) parent).isOnMeasure) {
                        bindView(convertView, getItem(position), position);
                    }
                }else {
                    bindView(convertView, getItem(position), position);
                }
//            }
        }else {
            bindView(convertView, getItem(position), position);
        }
      return convertView;
    }

    protected View newView(ViewGroup parent) {
      return mInflater.inflate(mItemLayout, parent, false);
    }

    protected abstract int getItemLayout();

    protected abstract void initView(View v, E data, boolean flag);

    protected abstract void bindView(View v, E data, int position);
    protected abstract boolean isSamePath(View v, E data);
  }

  private class LoadFilesTask extends AsyncTask<String, Void, List<T>> {
    private static final String TAG = "LoadFilesTask";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPreExecute() {
      mHandler.postDelayed(mShowLoading, 0);//3000
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<T> doInBackground(String... params) {
      MtkLog.d(TAG, "doInBackground params[0] = " + params[0]);
      return mFilesManager.listAllFiles(params[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(final List<T> result) {
      mHandler.removeCallbacks(mShowLoading);
      if (mLoadingShow) {
        onLoadedFiles();
        mLoadingShow = false;
      }

      refreshListView(result);

      if (null == result || result.isEmpty()) {
        if (mFilesManager instanceof MultiFilesManager) {
          MultiFilesManager manager = (MultiFilesManager) mFilesManager;
          int type = manager.getCurrentSourceType();
          // source_dlna result size=0 no tips dialog, one device and
          // not source_all no tips dialog
          if ((type == MultiFilesManager.SOURCE_DLNA)
              || ((manager.getAllDevicesNum() == MultiFilesManager.ONE_DEVICES)
                  && (type == MultiFilesManager.SOURCE_ALL))) {
            return;
          }
        }
        onListEmpty();
      } else {
        onListNotEmpty();
      }
    }
  }

  /**
   * Show loading files tips dialog
   */
  protected void onPreLoadFiles() {
	  MtkLog.d(TAG, "onPreLoadFiles");
  }

  /**
   * Dismiss loading file tips dialog
   */
  protected void onLoadedFiles() {
	  MtkLog.d(TAG, "onLoadedFiles");
  }

  /**
   * Refresh listView
   */
  protected void refreshListView() {
    MtkLog.d(TAG, "refreshListView() no params:" + vList.getVisibility()
        + "  " + vList.getAdapter());
    setListItems(mFilesManager.getCurrentFiles());
    MtkLog.d(TAG, "refreshListView() no params:" + mLoadFiles.size());
    if (!mLoadFiles.isEmpty() && vList.getVisibility() != View.VISIBLE) {
      vList.setVisibility(View.VISIBLE);
    }
    mAdapter.setDataSource(mLoadFiles);
    // mAdapter.notifyDataSetChanged();
  }

  /**
   * Refresh listView with the given files
   *
   * @param files
   *            files sets
   */
  protected void refreshListView(List<T> files) {
    MtkLog.d(TAG, "refreshListView()  List<T> files:" + (files == null?null:files.size()));
    if (files != null && !files.isEmpty()
        && vList.getVisibility() != View.VISIBLE) {
      vList.setVisibility(View.VISIBLE);
    }
    setListItems(files);
    //vList.setAdapter(mAdapter);
    mAdapter.setDataSource(mLoadFiles);
    // mAdapter.notifyDataSetChanged();
    // vList.setAdapter(mAdapter);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void update(Observable observable, Object data) {
  	MtkLog.d(TAG, "update");
  }
}
