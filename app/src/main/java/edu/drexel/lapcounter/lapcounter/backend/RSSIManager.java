package edu.drexel.lapcounter.lapcounter.backend;

import android.content.Context;

public class RSSIManager {
    public static final int DIRECTION_OUT = 1;
    public static final int DIRECTION_IN = -1;

    private Context mContext;

    public RSSIManager(Context context) {
        mContext = context;
    }
}
