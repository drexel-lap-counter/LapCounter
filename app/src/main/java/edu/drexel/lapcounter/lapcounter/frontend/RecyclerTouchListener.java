package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Recycler Touch Listener that is used to define fuctionality of clicking for recycler view.
 */
public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener
{
    private GestureDetector mGestureDetector;
    private ClickListener mClickListener;

    /**
     * Constructor for Recycler Touch Adapter
     * @param context context to generate GestureDetector from.
     * @param recyclerView RecyclerView
     * @param clickListener ClickListener to use for when an item is clicked.
     */
    public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener)
    {
        this.mClickListener = clickListener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener()
        {
            @Override
            public boolean onSingleTapUp(MotionEvent e)
            {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e)
    {
        View child = rv.findChildViewUnder(e.getX(),e.getY());


        if(child != null && mClickListener != null && mGestureDetector.onTouchEvent(e))
        {
            mClickListener.onClick(child,rv.getChildAdapterPosition(child));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e)
    {}

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept)
    {}


    /**
     * Interface establishing what methods the ClickListener needs
     */
    public interface ClickListener
    {
        void onClick(View view, int  position);
    }

}

