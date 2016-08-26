package fr.free.nrw.commons.nearby;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.free.nrw.commons.R;

public class NearbyListFragment extends ListFragment {

    private LatLng mLatestLocation;
    private int mImageSize;
    private boolean mItemClicked;
    ArrayAdapter mAdapter;

    List<Place> places;

    private static final String TAG = "NearbyListFragment";

    public NearbyListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "NearbyListFragment created");

        View view = inflater.inflate(R.layout.fragment_nearby, container, false);

        /*
        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(getActivity());
        progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);
        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) view.getRootView();
        root.addView(progressBar);
*/


        return view;
    }

    //TODO: Do asynchronously?
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //Load from data source (NearbyPlaces.java)
        LatLng mLatestLocation = ((NearbyActivity) getActivity()).getmLatestLocation();
        //FIXME: mLatestLocation not set. Hardcode it first for testing
        places = loadAttractionsFromLocation(mLatestLocation);

        final ListView listview = (ListView) view.findViewById(R.id.listview);
        mAdapter = new NearbyAdapter(getActivity(), places);
        //setListAdapter(mAdapter);
        listview.setAdapter(mAdapter);
        Log.d(TAG, "Adapter set to ListView");
        mAdapter.notifyDataSetChanged();
    }


    private static List<Place> loadAttractionsFromLocation(final LatLng curLatLng) {

        List<Place> places = NearbyPlaces.get();
        if (curLatLng != null) {
            Log.d(TAG, "Sorting places by distance...");
            Collections.sort(places,
                    new Comparator<Place>() {
                        @Override
                        public int compare(Place lhs, Place rhs) {
                            double lhsDistance = computeDistanceBetween(
                                    lhs.location, curLatLng);
                            double rhsDistance = computeDistanceBetween(
                                    rhs.location, curLatLng);
                            return (int) (lhsDistance - rhsDistance);
                        }
                    }
            );
        }
        //FIXME: This doesn't sort appropriately
        for(int i = 0; i < places.size(); i++) {
            System.out.println("Sorted " + places.get(i).name);
        }
        return places;
    }

    private static double computeDistanceBetween(LatLng from, LatLng to) {
        return computeAngleBetween(from, to) * 6371009.0D;
    }

    private static double computeAngleBetween(LatLng from, LatLng to) {
        return distanceRadians(Math.toRadians(from.latitude), Math.toRadians(from.longitude), Math.toRadians(to.latitude), Math.toRadians(to.longitude));
    }


    private static double distanceRadians(double lat1, double lng1, double lat2, double lng2) {
        return arcHav(havDistance(lat1, lat2, lng1 - lng2));
    }

    private static double arcHav(double x) {
        return 2.0D * Math.asin(Math.sqrt(x));
    }

    private static double havDistance(double lat1, double lat2, double dLng) {
        return hav(lat1 - lat2) + hav(dLng) * Math.cos(lat1) * Math.cos(lat2);
    }

    private static double hav(double x) {
        double sinHalf = Math.sin(x * 0.5D);
        return sinHalf * sinHalf;
    }

    private class NearbyAdapter extends ArrayAdapter {

        public List<Place> placesList;
        private Context mContext;

        public NearbyAdapter(Context context, List<Place> places) {
            super(context, 0);
            mContext = context;
            placesList = places;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Place place = (Place) getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_place, parent, false);
            }

            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            TextView tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);

            // Populate the data into the template view using the data object
            tvName.setText(place.name);
            tvDesc.setText(place.description);

            // Return the completed view to render on screen
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
/*
        @Override
        public int getItemCount() {
            return placesList == null ? 0 : placesList.size();
        }

        @Override
        public void onItemClick(View view, int position) {

            if (!mItemClicked) {
                mItemClicked = true;
                View heroView = view.findViewById(android.R.id.icon);
                DetailActivity.launch(
                        getActivity(), mAdapter.mAttractionList.get(position).name, heroView);
            }

        }*/
    }
}
