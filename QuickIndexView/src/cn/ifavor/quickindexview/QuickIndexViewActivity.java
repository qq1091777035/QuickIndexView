package cn.ifavor.quickindexview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;

import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import cn.ifavor.quickindexview.bean.Person;
import cn.ifavor.quickindexview.constants.Contants;
import cn.ifavor.quickindexview.view.QuickIndexBar;

public class QuickIndexViewActivity extends Activity {

	private ArrayList<Person> names;
	private ListView listview;
	private TextView mIndexLayout;
	private int preIndex = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quick_index);

		// 初始化数据
		namesArrs = getIntent().getStringArrayExtra(
				Contants.CONTACTS_NAMES);

		if (namesArrs != null && namesArrs.length > 0) {
			names = new ArrayList<Person>();
			for (int i = 0; i < namesArrs.length; i++) {
				Person person = new Person(namesArrs[i]);
				names.add(person);
			}
			// 排序
			Collections.sort(names);
		}

		mBar = (QuickIndexBar) findViewById(R.id.bar);
		mIndexLayout = (TextView) findViewById(R.id.tv_index_layout);
		listview = (ListView) findViewById(R.id.list);
		listview.setAdapter(new PersonAdapter());
		// 设置选中监听
		listview.setOnItemClickListener(new QuickIndexContactsItemListener());
		// 设置滚动监听
		listview.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (preIndex != firstVisibleItem){
					// 更新索引
					int index = names.get(firstVisibleItem).getPinyin().charAt(0) - 'A';
					mBar.setCurrentSelectedIndex(index);
					
					preIndex = firstVisibleItem;
				}
			}
		});

		mBar.setOnLetterUpdateListener(new QuickIndexBar.OnLetterUpdateListener() {

			@Override
			public void onUpdate(String letter) {
				int selectedPosition = -1;
				for (int i = 0; i < names.size(); i++) {
					if (letter.equals(names.get(i).getPinyin().charAt(0) + "")) {
						selectedPosition = i;
						break;
					}
				}
				System.out.println("selectedPosition: " + selectedPosition);
				listview.setSelection(selectedPosition);

				// 在索引层中显示索引
				showIndexLayout(letter);
			}

		});
	}

	private Handler mHandler = new Handler();
	private String[] namesArrs;
	private QuickIndexBar mBar;

	/* 在索引层中显示索引 */
	private void showIndexLayout(String letter) {
		mIndexLayout.setVisibility(View.VISIBLE);
		mIndexLayout.setText(letter);
		mHandler.removeCallbacksAndMessages(null);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mIndexLayout.setVisibility(View.INVISIBLE);
			}
		}, 2000);
	}

	private class PersonAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return names.size();
		}

		@Override
		public Object getItem(int position) {
			return names.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(QuickIndexViewActivity.this,
						R.layout.item_list, null);
				holder = new ViewHolder();
				holder.tv_Index = (TextView) convertView
						.findViewById(R.id.tv_index);
				holder.tv_name = (TextView) convertView
						.findViewById(R.id.tv_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			boolean isHide = false;

			if (position == 0) {
				isHide = false;
			} else {
				isHide = names.get(position).getPinyin().charAt(0) == names
						.get(position - 1).getPinyin().charAt(0);
			}

			holder.tv_Index.setVisibility(isHide ? View.GONE : View.VISIBLE);

			Person person = names.get(position);
			// 这里不能直接使用char，直接使用char和int相等，是获取资源
			holder.tv_Index.setText(person.getPinyin().charAt(0) + "");
			holder.tv_name.setText(person.getName());
			return convertView;
		}
	}

	private class ViewHolder {
		TextView tv_Index;
		TextView tv_name;
	}

	private class QuickIndexContactsItemListener implements
			AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (names != null && names.size() > 0) {
				Intent data = new Intent();
				data.putExtra(Contants.INTENT_CONTAC_NAME, names.get(position).getName());
				data.putExtra(Contants.INTENT_CONTAC_INDEX_SORTED, position);
				
				// 找出该联系人在原始数组中的索引
				int contact_index = Arrays.binarySearch(namesArrs, names.get(position).getName());
				data.putExtra(Contants.INTENT_CONTAC_INDEX, contact_index);
				setResult(Contants.CONTACTS_RESULT_CODE, data);
				finish();
			}
		}
	}
}
