package cn.wz.scanner.scanlibrary.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 列表分割线.
 */
public class WzSpaceItemDecoration extends RecyclerView.ItemDecoration {

    /** 空白距离. */
    private int space;

    /**
     * 构造方法.
     * @param space 空白距离
     */
    public WzSpaceItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = 0;
        outRect.right = 0;
        outRect.bottom = space;
    }
}