package com.osapps.chat.search;

import android.os.Handler;
import android.widget.SearchView;
import android.widget.TextView;

import com.osapps.chat.utils.ChatFinals;


/**
 * Created by shabat on 8/26/2017.
 */

class ChannelSearchHandler {
    private Handler handler;

    void ChannelSearchHandler(SearchView searchView) {
         handler = new Handler();
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView searchText = (TextView) searchView.findViewById(id);
//        Typeface myCustomFont = Typeface.createFromAsset(companySelectFrag.getActivity().getAssets(),"varela.ttf");
//        searchText.setTypeface(myCustomFont);
        searchView.setQuery("",false);
        searchView.setOnQueryTextListener(listener);
    }

    SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {

            handler.postDelayed(new Runnable() {
                public void run() {
                    // yourMethod();
                }
            }, ChatFinals.CHANNEL_SEARCH_TYPE_WAIT_MILLIS);
            return true;
        }
    };
}
