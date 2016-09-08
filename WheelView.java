package com.example.androidtest.repository;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WheelView extends ScrollView {
	public static final String TAG = WheelView.class.getSimpleName();
	private Context context;

	private LinearLayout views;
	/**
	 * 坐标值，其实更好的解释是item*Height的值
	 */
	int initialY;
	Runnable scrollerTask;
	/**
	 * 50
	 */
	int newCheck = 50;
	int itemHeight = 0;

	private int scrollDirection = -1;
	private static final int SCROLL_DIRECTION_UP = 0;
	private static final int SCROLL_DIRECTION_DOWN = 1;

	Paint paint;
	int viewWidth;

	public interface OnWheelViewListener {
		public void onSelected(int selectedIndex, String item);
	}

	public WheelView(Context context) {
		super(context);
		init(context);
	}

	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public WheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	List<String> items;

	private List<String> getItems() {
		return items;
	}

	/**
	 * 讲传入list中的值放进LinearLayout中，并设置样式后刷新
	 * 
	 * @param list
	 */
	public void setItems(List<String> list) {
		if (null == items) {
			items = new ArrayList<String>();
		}
		items.clear();
		items.addAll(list);

		/**
		 * 根据理解应该是第一个元素和最后一个元素选中时，其上方和下方还要多出一个元素，所以在这里进行添加
		 * 因为items已经有元素了，然后在0处添加数据，整体往后移动一位，再在最后加一位
		 */
//		for (int i = 0; i < offset; i++) {
//			items.add(0, "");
//			items.add("");
//		}
		if (displayItemCount % 2 != 0) {
			for (int i = 0; i < offset; i++) {
				items.add(0, "");
				items.add("");
			}
		} else if (displayItemCount % 2 == 0) {
			for (int i = 0; i < offset; i++) {
				if (i == 0) {
					items.add(0, "");
					items.add("");
				} else {
					items.add("");
				}
			}
		}
		initData();

	}

	public static final int OFF_SET_DEFAULT = 1;
	/**
	 * 根据理解，应该是list默认是是从0开始，而一些运算时从1开始算，所以第一个值和最后一个值标记时都要作+—操作
	 */
	int offset = OFF_SET_DEFAULT; // 偏移量（需要在最前面和最后面补全）

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * 每页显示的数量
	 */
	int displayItemCount;

	public void setDisplayItemCount(int displayItemCount) {
		this.displayItemCount = displayItemCount;
		if (displayItemCount % 2 == 0) {
			setOffset((displayItemCount - 1) / 2 + 1);
		} else if (displayItemCount % 2 != 0) {
			setOffset((displayItemCount - 1) / 2);
		}
	}

	/**
	 * 选中的号码
	 */
	int selectedIndex = 1;

	private void init(Context context) {
		this.context = context;

		// scrollView = ((ScrollView)this.getParent());
		// Log.d(TAG, "scrollview: " + scrollView);
		Log.d(TAG, "parent: " + this.getParent());
		// this.setOrientation(VERTICAL);
		this.setVerticalScrollBarEnabled(false);

		views = new LinearLayout(context);
		views.setOrientation(LinearLayout.VERTICAL);
		this.addView(views);

		scrollerTask = new Runnable() {

			public void run() {
				// getScrollX、getScrollY得到View移动到的坐标距离原坐标的差值,负值顺着x、y轴方向移动,正值反之
				int newY = getScrollY();
				// Log.e("taxxxxxxxxxxxxx", initialY+"---------------"+newY);
				// 两个值相等，证明停止滑动
				if (initialY - newY == 0) { // stopped
					// offset默认是1，暂时没发现改变，remainder应该是偏移一个item的值，divided是滑动了几个
					/**
					 * 通俗点initialY值就是随着Y值增长的值，多一个item，就是item总数*itemHeight，当从第一个
					 * item跳到第二个item中心时
					 * ，initialY值应该是第一个item的Hieght+第二个itemHeight/2
					 * 由此可判断出，第N个item其实就是initialY值/一个item的Height e.g 一个item
					 * 高是3，当前滑到14，那么当前显示的元素(divided)应该是第4个,偏移量(remainder)是2
					 */
					/**
					 * 多余差值
					 */
					final int remainder = initialY % itemHeight;
					/**
					 * 序数
					 */
					final int divided = initialY / itemHeight;
					// Log.e(TAG, "initialY: " + initialY);
					// Log.e(TAG, "remainder: " + remainder + ", divided: "
					// + divided);
					if (remainder == 0) {
						// selectedIndex = divided + offset;
						selectedIndex = (displayItemCount % 2) != 0 ? (divided + offset)
								: (divided + offset) - 1;
						// 回调函数，执行自定义功能
						onSeletedCallBack();
					} else {
						// 当当前项的字超过线
						if (remainder > itemHeight / 2) {
							/**
							 * 自动定位到顶部： 比如： scrollView.post(new Runnable() {
							 * public void run() {
							 * scrollView.fullScroll(ScrollView.FOCUS_UP); } });
							 */
							WheelView.this.post(new Runnable() {
								@Override
								public void run() {
									// ScrollTo方法设置滚动的位置
									WheelView.this.smoothScrollTo(0, initialY
											- remainder + itemHeight);
									selectedIndex = (displayItemCount % 2) != 0 ? (divided
											+ offset + 1)
											: (divided + offset);
									// selectedIndex = divided + offset + 1;
									// 回调函数，执行自定义功能
									onSeletedCallBack();
								}
							});
						} else {
							WheelView.this.post(new Runnable() {
								@Override
								public void run() {
									WheelView.this.smoothScrollTo(0, initialY
											- remainder);
									// selectedIndex = divided + offset;
									selectedIndex = (displayItemCount % 2) != 0 ? (divided + offset)
											: (divided + offset - 1);
									// 回调函数，执行自定义功能
									onSeletedCallBack();
								}
							});
						}

					}

				} else {
					initialY = getScrollY();
					// 重新调用这个线程
					WheelView.this.postDelayed(scrollerTask, newCheck);
				}
			}
		};

	}

	/**
	 * 初始化设置单页显示个数，然后给LinearLayout添加item
	 */
	private void initData() {
		/**
		 * 每页默认显示3个
		 */
//		displayItemCount = offset * 2 + 1;

		for (String item : items) {
			views.addView(createView(item));
		}

		refreshItemView(0);
	}

	/**
	 * 从String返一个TextView
	 * 
	 * @param item
	 * @return
	 */
	private TextView createView(String item) {
		TextView tv = new TextView(context);
		tv.setLayoutParams(new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		// 设置当TextView中的文字超过TextView的容量时,用省略号代替
		tv.setSingleLine(true);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		tv.setText(item);
		tv.setGravity(Gravity.CENTER);
		int padding = dip2px(15);
		tv.setPadding(padding, padding, padding, padding);
		if (0 == itemHeight) {
			itemHeight = getViewMeasuredHeight(tv);
			Log.d(TAG, "itemHeight: " + itemHeight);
			views.setLayoutParams(new LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, itemHeight
							* displayItemCount));
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this
					.getLayoutParams();
			this.setLayoutParams(new LinearLayout.LayoutParams(lp.width,
					itemHeight * displayItemCount));
		}
		return tv;
	}

	/**
	 * onScrollChanged函数在ScrollView中内容开始滚动时出发被调用。
	 */
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		refreshItemView(t);
		if (t > oldt) {
			// Log.d(TAG, "向下滚动");
			scrollDirection = SCROLL_DIRECTION_DOWN;
		} else {
			// Log.d(TAG, "向上滚动");
			scrollDirection = SCROLL_DIRECTION_UP;
		}

	}

	/**
	 * 刷新界面
	 * 
	 * @param y
	 *            传入的值为当前选中的值
	 */
	private void refreshItemView(int y) {
		int position = y / itemHeight + offset;
		int remainder = y % itemHeight;
		int divided = y / itemHeight;

		// if (remainder == 0) {
		// // 因为0位是空，所以需要从1开始
		// position = divided + offset;
		// } else {
		// if (remainder > itemHeight / 2) {
		// position = divided + offset + 1;
		// }
		// }
		if (displayItemCount % 2 != 0) {
			if (remainder == 0) {
				position = divided + offset;
			} else {
				if (remainder > itemHeight / 2) {
					position = divided + offset + 1;
				}
			}
		} else if (displayItemCount % 2 == 0) {
			if (remainder == 0) {
				position = divided + offset - 1;
			} else {
				if (remainder > itemHeight / 2) {
					position = divided + offset;
				}
			}
		}
		// 所有节点总数
		int childSize = views.getChildCount();
		// 循环设置item颜色
		for (int i = 0; i < childSize; i++) {
			TextView itemView = (TextView) views.getChildAt(i);
			if (null == itemView) {
				return;
			}
			if (position == i) {
				itemView.setTextColor(Color.parseColor("#0288ce"));
			} else {
				itemView.setTextColor(Color.parseColor("#bbbbbb"));
			}
		}
	}

	/**
	 * 获取选中区域的边界
	 */
	int[] selectedAreaBorder;

	/**
	 * 获取两条横线的位置
	 * 
	 * @return
	 */
	private int[] obtainSelectedAreaBorder() {
		if (null == selectedAreaBorder) {
			selectedAreaBorder = new int[2];
			// selectedAreaBorder[0] = itemHeight * offset;
			// selectedAreaBorder[1] = itemHeight * (offset + 1);
			if (displayItemCount % 2 != 0) {
				selectedAreaBorder[0] = itemHeight * offset;
				selectedAreaBorder[1] = itemHeight * (offset + 1);
			} else if (displayItemCount % 2 == 0) {
				selectedAreaBorder[1] = itemHeight * offset;
				selectedAreaBorder[0] = itemHeight * (offset - 1);
			}
		}
		return selectedAreaBorder;
	}

	/**
	 * View的色图方法，注意中间的new Drawable
	 */
	@Override
	public void setBackgroundDrawable(Drawable background) {

		if (viewWidth == 0) {
			viewWidth = ((Activity) context).getWindowManager()
					.getDefaultDisplay().getWidth();
			Log.d(TAG, "viewWidth: " + viewWidth);
		}

		if (null == paint) {
			paint = new Paint();
			paint.setColor(Color.parseColor("#83cde6"));
			paint.setStrokeWidth(dip2px(1f));
		}

		background = new Drawable() {
			@Override
			public void draw(Canvas canvas) {
				// canvas.drawLine(viewWidth * 1 / 6,
				// obtainSelectedAreaBorder()[0], viewWidth * 5 / 6,
				// obtainSelectedAreaBorder()[0], paint);\
				canvas.drawLine(0, obtainSelectedAreaBorder()[0], viewWidth,
						obtainSelectedAreaBorder()[0], paint);
				canvas.drawLine(0, obtainSelectedAreaBorder()[1], viewWidth,
						obtainSelectedAreaBorder()[1], paint);
			}

			@Override
			public void setAlpha(int alpha) {

			}

			@Override
			public void setColorFilter(ColorFilter cf) {

			}

			@Override
			public int getOpacity() {
				return 0;
			}
		};

		super.setBackgroundDrawable(background);

	}

	/**
	 * onSizeChanged（）是在布局发生变化时的回调函数，间接回去调用onMeasure, onLayout函数重新布局
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.d(TAG, "w: " + w + ", h: " + h + ", oldw: " + oldw + ", oldh: "
				+ oldh);
		viewWidth = w;
		setBackgroundDrawable(null);
	}

	/**
	 * 选中回调
	 */
	private void onSeletedCallBack() {
		if (null != onWheelViewListener) {
			onWheelViewListener.onSelected(selectedIndex,
					items.get(selectedIndex));
		}

	}

	/**
	 * 暂时没用
	 * 
	 * @param position
	 */
	public void setSeletion(int position) {
		final int p = position;
		selectedIndex = p + offset;
		this.post(new Runnable() {
			@Override
			public void run() {
				WheelView.this.smoothScrollTo(0, p * itemHeight);
			}
		});

	}

	/**
	 * 获得相应item
	 * 
	 * @return
	 */
	public String getSeletedItem() {
		return items.get(selectedIndex);
	}

	/**
	 * 获得选中项index
	 * 
	 * @return
	 */
	public int getSeletedIndex() {
		return selectedIndex - offset;
	}

	/**
	 * 抛：手指触动屏幕后，稍微滑动后立即松开
	 * onDown-->onScroll-->onScroll-->onScroll-->...-->onFling
	 */
	@Override
	public void fling(int velocityY) {
		super.fling(velocityY / 3);
	}

	/**
	 * ontouch事件，当处于UP状态，执行
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) {

			startScrollerTask();
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 将当前Y坐标值，传入initialY，并延迟50ms进行线程操作
	 * 
	 */
	public void startScrollerTask() {

		initialY = getScrollY();
		/**
		 * 相同：在与UI线程的通信上，Handler与View，其实最终都做了同样的事情。就是将消息传递在UI线程 的消息队列里，执行一些处理操作。
		 * 不同:View.post方法想在非UI线程有效工作。如该方法的注释所说，必须保证该View已经被添加至窗口。
		 */
		this.postDelayed(scrollerTask, newCheck);
	}

	/**
	 * 滚动View监听，下方是getter和setter方法
	 */
	private OnWheelViewListener onWheelViewListener;

	public OnWheelViewListener getOnWheelViewListener() {
		return onWheelViewListener;
	}

	public void setOnWheelViewListener(OnWheelViewListener onWheelViewListener) {
		this.onWheelViewListener = onWheelViewListener;
	}

	/**
	 * dip转px
	 * 
	 * @param dpValue
	 * @return
	 */
	private int dip2px(float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 获取view的测量高度
	 * 
	 * @param view
	 * @return
	 */
	private int getViewMeasuredHeight(View view) {
		/**
		 * 　　1.static int getMode(int measureSpec):根据提供的测量值(格式)提取模式(上述三个模式之一)
		 * 　　2.static int getSize(int
		 * measureSpec):根据提供的测量值(格式)提取大小值(这个大小也就是我们通常所说的大小) 　　3.static int
		 * makeMeasureSpec(int size,int mode):根据提供的大小值和模式创建一个测量值(格式)
		 */
		// MeasureSpec.UNSPECIFIED未指定尺寸, MeasureSpec.EXACTLY精确尺寸,
		// MeasureSpec.AT_MOST最大尺寸
		int width = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		/**
		 * 那么其中的两个值就很好理解了 因为32位的数据中的前两位是代表的模式，那么Integer.MAX_VALUE >>
		 * 2就代表能获取到的最大值（不含模式下的值）
		 * MeasureSpec.AT_MOST这个模式下面高度会在listView、gridView的item集高度和Integer 2
		 * 之间取最小值,也就是包裹内容
		 */
		int expandSpec = View.MeasureSpec.makeMeasureSpec(
				Integer.MAX_VALUE >> 2, View.MeasureSpec.AT_MOST);
		Log.e("Integer.MAX_VALUE >> 2:", (Integer.MAX_VALUE >> 2) + "");
		/**
		 * 显示数据，需要根据数据来计算高度，首先把数据设置到控件中，然后通过View.measure(int widthMeasureSpec,
		 * int heightMeasureSpec)量取高度,并为该View设置坐标。参考代码：
		 */
		view.measure(width, expandSpec);
		return view.getMeasuredHeight();
	}

}
