package ru.tinkoff.scrollingpagerindicator;

import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * @author Nikita Olifer
 */
public class ViewPagerAttacher implements ScrollingPagerIndicator.PagerAttacher<ViewPager> {

    private DataSetObserver dataSetObserver;
    private ViewPager.OnPageChangeListener onPageChangeListener;
    private ViewPager pager;

    @Override
    public void attachToPager(@NonNull final ScrollingPagerIndicator indicator, @NonNull final ViewPager pager) {
        final PagerAdapter adapter = pager.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException("Set adapter before call attachToPager() method");
        }

        this.pager = pager;

        indicator.setDotCount(adapter.getCount());
        indicator.setCurrentPosition(pager.getCurrentItem());

        dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                indicator.reattach();
            }

            @Override
            public void onInvalidated() {
                onChanged();
            }
        };
        adapter.registerDataSetObserver(dataSetObserver);

        onPageChangeListener = new ViewPager.OnPageChangeListener() {

            boolean idleState = true;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixel) {
                final float offset;
                // ViewPager may emit negative positionOffset for very fast scrolling
                if (positionOffset < 0) {
                    offset = 0;
                } else if (positionOffset > 1) {
                    offset = 1;
                } else {
                    offset = positionOffset;
                }
                indicator.onPageScrolled(position, offset);
            }

            @Override
            public void onPageSelected(int position) {
                if (idleState) {
                    indicator.setDotCount(adapter.getCount());
                    indicator.setCurrentPosition(pager.getCurrentItem());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                idleState = state == ViewPager.SCROLL_STATE_IDLE;
            }
        };
        pager.addOnPageChangeListener(onPageChangeListener);
    }

    @Override
    public void detachFromPager() {
        PagerAdapter adapter = pager.getAdapter();
        if (adapter != null) {
            adapter.unregisterDataSetObserver(dataSetObserver);
        }
        pager.removeOnPageChangeListener(onPageChangeListener);
    }
}
