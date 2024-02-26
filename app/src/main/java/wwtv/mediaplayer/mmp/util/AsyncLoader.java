package com.mediatek.wwtv.mediaplayer.mmp.util;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import com.mediatek.wwtv.tvcenter.util.MtkLog;

public class AsyncLoader<R> {

    private final List<WorkItem<R>> mQueue = new ArrayList<WorkItem<R>>();

    private static AsyncLoader mAsyncLoader = null;

    private Handler mHandler;

    private final static int MSG_ADD_WORK = 1;

    protected static final String TAG = "AsyncLoader";

    private HandlerThread mThread;
    private boolean mQueueClear=false;
    private LoadWork<R> infoWork;

    public static synchronized AsyncLoader getInstance(int num) {

        if (mAsyncLoader == null) {
            mAsyncLoader = new AsyncLoader("ThunmbnailThread");
        }
        return mAsyncLoader;
    }

    public AsyncLoader(String name) {
        mThread = new HandlerThread(name, Process.THREAD_PRIORITY_URGENT_AUDIO);
        mThread.start();
        mHandler = new Handler(mThread.getLooper()) {
            public void handleMessage(Message msg) {

                switch (msg.what) {
                case MSG_ADD_WORK: {
                    mQueueClear=false;
                    if (!mQueue.isEmpty()) {
                        WorkItem<R> workItem = mQueue.remove(0);
                        MtkLog.i(TAG, mThread.getName() + ":"
                                + workItem.mWork.getClass().getName()
                                + " enter" + " task size:" + mQueue.size()+"mQueueClear"+mQueueClear);
                        Long start = System.currentTimeMillis();

                        //Will Load Video Thumbnail
                        R result = workItem.mWork.load();
                        if(!mQueueClear){
                            //Will Load Audio CoverPictur
                            workItem.mWork.loaded(result);
                        }
                        MtkLog.i(TAG,
                                        mThread.getName() + ":"
                                        + workItem.mWork.getClass().getName()
                                        + " leave cost time:"
                                        + (System.currentTimeMillis() - start)
                                        + " task size:" + mQueue.size()+"mQueueClear"+mQueueClear);
                     }
                     break;

                }
				default:
            		  break;
                }

            }
        };
    }

    public void addWork(LoadWork<R> work) {
        synchronized (mQueue) {
            WorkItem<R> w = new WorkItem<R>(work);
            mQueue.add(w);
            mHandler.sendEmptyMessage(MSG_ADD_WORK);

        }
    }

    public void addWorkTop(LoadWork<R> work) {
        synchronized (mQueue) {
            WorkItem<R> w = new WorkItem<R>(work);
            mQueue.add(0,w);
            mHandler.sendEmptyMessage(MSG_ADD_WORK);

        }
    }



    public void addSelectedInfoWork(LoadWork<R> work){

        synchronized (mQueue) {
            if(infoWork != null){

                cancel(infoWork);
            }
            infoWork = work;


            WorkItem<R> w = new WorkItem<R>(work);
            mQueue.add(0,w);
            mHandler.sendEmptyMessage(MSG_ADD_WORK);

        }

    }

    public int getTaskSize() {
        return mQueue.size();
    }

    public boolean cancel(final LoadWork<R> work) {
        synchronized (mQueue) {
            try{
                int index = findItem(work);

                if (index >= 0) {
                    mQueue.remove(index);
                    return true;
                } else {
                    return false;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    private int findItem(LoadWork<R> work) {
        for (int i = 0; i < mQueue.size(); i++) {
            if (mQueue.get(i).mWork.equals(work)) {
                return i;
            }
        }

        return -1;
    }

    public void clearQueue() {
        MtkLog.d(TAG,"clearQueue ~~");
        synchronized (mQueue) {
            mQueue.clear();
            mQueueClear=true;
        }
    }

    // public void quit() {
    // if (null != mThread) {
    // mThread.quit();
    // }
    // mAsyncLoader = null;
    // mThread = null;
    // mHandler = null;
    // mQueue.clear();
    // }

    public void stop() {
		MtkLog.d(TAG,"stop ~~");

    }

    public void stopLoader() {
        // synchronized (mQueue) {
        // mDone = true;
        // mQueue.notifyAll();
        // }
        //
        // if (mExecutor != null) {
        // mExecutor.shutdownNow();
        // mExecutor = null;
        // }
        // mAsyncLoader = null;

        // new Exception().printStackTrace();
    }

    // private class LoadThread implements Runnable {
    // public void run() {
    // Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
    // Log.i("xy", "Thread.currentThread().getName()====="
    // + Thread.currentThread().getName());
    // Thread.currentThread().setName("AsyncLoaderThread");
    //
    // while (true) {
    // WorkItem<R> workItem = null;
    //
    // synchronized (mQueue) {
    // if (mDone) {
    // // new Exception().printStackTrace();
    // break;
    // }
    //
    // if (!mQueue.isEmpty()) {
    // workItem = mQueue.remove(0);
    // } else {
    // try {
    // mQueue.wait();
    // } catch (InterruptedException ex) {
    // // ignore the exception
    // }
    //
    // continue;
    // }
    // }
    //
    // final R result = workItem.mWork.load();
    // workItem.mWork.loaded(result);
    // }
    // }
    // }

    public interface LoadWork<R> {
        R load();

        void loaded(R result);
    }

    private static class WorkItem<R> {
        LoadWork<R> mWork;

        WorkItem(LoadWork<R> work) {
            mWork = work;
        }
    }
}
