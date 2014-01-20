package com.pytacular.checkitcloudchecklist;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import static com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import static com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class MainActivity extends SherlockFragmentActivity {
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private final Handler handler = new Handler();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		addTabs(); //Adding tabs
		
		 if (savedInstanceState != null) {
	            int index = savedInstanceState.getInt("selected_tab_index", 0);
	            getSupportActionBar().setSelectedNavigationItem(index);
	        }
	}
	
	@Override 
	protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        int index = getSupportActionBar().getSelectedNavigationIndex();
        outState.putInt("selected_tab_index", index); 
    }

	/**
	 * Creating tabs
	 */
    private void addTabs() {
        ActionBar bar = getSupportActionBar();
        
 
        String defaultTab = getResources().getString(R.string.check_list);
        ActionBar.Tab currentTab =  bar.newTab();
        currentTab.setText(defaultTab);
        currentTab.setTabListener(new TabListener(this, defaultTab, CheckList.class));
        bar.addTab(currentTab);
        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }
	
	
	/**
	 * Responding to menu items
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		 MenuInflater inflater = getSupportMenuInflater();
		    inflater.inflate(R.menu.mainmenu, menu);
		    //return true;
		    final MenuItem refresh = (MenuItem) menu.findItem(R.id.action_refresh);
	        refresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
	            // on selecting show progress spinner for 1s
	            public boolean onMenuItemClick(MenuItem item) {
	                // item.setActionView(R.layout.progress_action);
	                handler.postDelayed(new Runnable() {
	                    public void run() {
	                        refresh.setActionView(null);
	                    }
	                }, 1000);
	                return false;
	            }
	        });
	        return super.onCreateOptionsMenu(menu);
	    }
	/**
	 * When an item from a menu is selected
	 */
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
		ActionBar menubar = getSupportActionBar();
		switch (item.getItemId()) {
		case R.id.action_refresh:
            // switch to a progress animation
            item.setActionView(R.layout.indeterminate_progress_action);
            return true;
	    case R.id.action_settings:
	      Toast.makeText(this, "Settings", Toast.LENGTH_SHORT)
	          .show();
	      break;
	    case R.id.action_switch_theme:
	      Toast.makeText(this, "Switch Theme", Toast.LENGTH_SHORT).show();
	      /*Intent intent = new Intent(this, TabSwipeTestActivity.class);
	      this.startActivity(intent);*/
	      break;

	    default:
	      break;
	    }

	    return true;
	  } 	
}