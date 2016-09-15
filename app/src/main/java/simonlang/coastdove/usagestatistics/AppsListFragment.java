package simonlang.coastdove.usagestatistics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import simonlang.coastdove.usagestatistics.app_details.AppDetailsActivity;

/**
 * ListFragment containing all apps that have usage data
 */
public class AppsListFragment extends LoadableListFragment<String> {

    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
        return new AppListLoader(getActivity());
    }

    @Override
    protected void setUpListAdapter() {
        this.adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        setListAdapter(this.adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final String item = (String) getListView().getItemAtPosition(position);

        Intent intent = new Intent(getActivity(), AppDetailsActivity.class);
        intent.putExtra(getString(R.string.extras_package_name), item);
        startActivity(intent);
    }

    @Override
    protected void addProgressBarToViewGroup() {
        ViewGroup root = (ViewGroup)getActivity().findViewById(R.id.fragment_apps_list);
        root.addView(this.progressBar);
    }
}
