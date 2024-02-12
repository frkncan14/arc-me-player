package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.util.MenuFatherObject;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIListAdapter;

public class ListAdapterMenu extends BaseAdapter {
	private List<MenuFatherObject> mDataList = new ArrayList<MenuFatherObject>();
	private Context mContext;
	private int mStyle;

	public static final int PAGE_SIZE = 8;

	private int mOffset = 0;

	@SuppressWarnings("unchecked")
	public ListAdapterMenu(Context context, List<?> list, int style) {
		mContext = context;
		mDataList = (List<MenuFatherObject>) list;
		mStyle = style;
	}

	public void initList(List<MenuFatherObject> mList) {
		mDataList = mList;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		int pos = position + mOffset;
		pos = pos >= 0 ? pos : 0;
		pos = pos < mDataList.size() ? pos : mDataList.size() - 1;
		return mDataList.get(pos).enable;
	}

	@Override
	public int getCount() {
		if (mDataList.size() > PAGE_SIZE) {
			if (mDataList.size() - mOffset >= PAGE_SIZE) {
				return PAGE_SIZE;
			}
			return mDataList.size() - mOffset;
		} else {
			return mDataList.size();
		}
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position + mOffset);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		MyViewHolder myViewHolder = null;
		if(convertView == null) {
			myViewHolder = new MyViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(mStyle, null);
			convertView.setLayoutParams(new ListView.LayoutParams(
					LayoutParams.MATCH_PARENT, (int)(ScreenConstant.SCREEN_HEIGHT/18)));
			myViewHolder.menuListTv = (TextView) convertView.findViewById(R.id.mmp_menulist_tv);
			convertView.setTag(myViewHolder);
		}else {
			myViewHolder = (MyViewHolder) convertView.getTag();
		}
		int pos = position+mOffset;
		pos = pos >= 0 ? pos: 0;
		myViewHolder.menuListTv.setText(mDataList.get(pos).content);
		if (!mDataList.get(pos).enable) {
			myViewHolder.menuListTv.setTextColor(Color.DKGRAY);
		}else {
			myViewHolder.menuListTv.setTextColor(Color.WHITE);
		}
		return convertView;
	}

	public static class MyViewHolder {
		TextView menuListTv;
	}

	public void setOffset(int offset) {
		mOffset = offset;

		mMyOffsetListener.offset(this.mOffset);
	}
	public void setMyOffsetListener(MyOffsetListener mMyOffsetListener)
	{
		this.mMyOffsetListener=mMyOffsetListener;
	}
	private MyOffsetListener mMyOffsetListener;
	public interface MyOffsetListener
	{
		void offset(int offset);
	}
	public int getOffset() {
		return mOffset;
	}

	static class ViewHolder {
		TextView tv;
		ImageView img;
	}
}
