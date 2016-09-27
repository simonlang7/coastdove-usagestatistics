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

package simonlang.coastdove.usagestatistics.ui.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import simonlang.coastdove.lib.CoastDoveModules;
import simonlang.coastdove.usagestatistics.R;
import simonlang.coastdove.usagestatistics.StatisticsListener;
import simonlang.coastdove.usagestatistics.utility.FileHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        FileHelper.checkExternalStoragePermissions(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_register:
                CoastDoveModules.registerModule(this, StatisticsListener.class, getString(R.string.app_name), "*");
                return true;
            case R.id.item_export_db:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FileHelper.exportSQLiteDB(MainActivity.this, FileHelper.Directory.PUBLIC, FileHelper.EXPORTED_DB_FILENAME);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, getString(R.string.database_exported), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
