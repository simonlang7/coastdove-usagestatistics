/*  Coast Dove
    Copyright (C) 2016  Simon Lang
    Contact: simon.lang7 at gmail dot com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package simonlang.coastdove.usagestatistics.app_details;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import simonlang.coastdove.usagestatistics.R;

/**
 * Activity that displays details to an app, such as app usage data sessions
 * and their duration
 */
public class AppDetailsActivity extends AppCompatActivity {

    /** Package name of the app */
    private String appPackageName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        retrieveAppPackageName(savedInstanceState);

        // Set support action bar
        Toolbar toolbar = (Toolbar)findViewById(R.id.detectable_app_toolbar);
        toolbar.setTitle(this.appPackageName);
        setSupportActionBar(toolbar);


    }

    @Override
    protected void onResume() {
        super.onResume();

        retrieveAppPackageName(null);
    }

    private void retrieveAppPackageName(Bundle savedInstanceState) {
        // Get package name
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
                this.appPackageName = null;
            else
                this.appPackageName = extras.getString(getString(R.string.extras_package_name));
        }
        else
            this.appPackageName = (String)savedInstanceState.getSerializable(getString(R.string.extras_package_name));
    }

    /**
     * Returns the package name of the app to be detected
     */
    public String getAppPackageName() {
        return this.appPackageName;
    }
}
