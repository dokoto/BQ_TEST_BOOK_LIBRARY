package net.bqtbl.lists;

import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.bqtbl.R;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.DropboxAPI.Entry;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.ListActivity;
import android.content.Context;
import net.bqtbl.utl.ActionBookManager;
import net.bqtbl.structs.Adapter_row;

 
public class BookList extends ListActivity {

	final static private String APP_KEY = "5fwf3fhkg93g2ix";
	final static private String APP_SECRET = "71xo6y4v1617wwx";
	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
	List<Entry> FileList;
	private DropboxAPI<AndroidAuthSession> mDBApi;
	private ListQuestionsAdapter adapter;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		mDBApi.getSession().startAuthentication(BookList.this);
		adapter = new ListQuestionsAdapter(BookList.this, new ArrayList<Adapter_row>());	

		setListAdapter(adapter);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{	        
			mDBApi.getSession().unlink();
			this.finish();			
		}
		return super.onKeyDown(keyCode, event);
	}	
	
	@Override
	protected void onResume() {
	    super.onResume();

	    if (mDBApi.getSession().authenticationSuccessful()) {
	        try {
	            mDBApi.getSession().finishAuthentication();
	            mDBApi.getSession().getAccessTokenPair();
	            GetFile(mDBApi);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

	private ArrayList<Adapter_row> UpdateAdapter(List<Entry> fileList)
	{
		
		ArrayList<Adapter_row> Rows = new ArrayList<Adapter_row>();
		for( Entry e : fileList )
		{
			if (e.mimeType.compareTo("application/epub+zip") == 0)
				Rows.add(new Adapter_row(e.fileName(), e.modified));
		}		
		return Rows;
	}
	
	private void GetFile(final DropboxAPI<AndroidAuthSession> lmDBApi)
	{
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>()
		{
			Entry existingEntry;		
			ArrayList<Adapter_row> Rows;
			@Override
			protected void onPreExecute()
			{
			}

			@Override
			protected Void doInBackground(Void... arg0)
			{
				try {
					existingEntry = lmDBApi.metadata("/", 0, null, true, null);
					Rows = UpdateAdapter(existingEntry.contents);
				} catch (DropboxException e) {
					e.printStackTrace();
				}	            
				return null;
			}

			@Override
			protected void onPostExecute(Void result)
			{
				adapter.clear();
				adapter.addAll(Rows);
				adapter.setNotifyOnChange(true);
			}
		};
		task.execute((Void[]) null);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_booklist, menu);
		return true;
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
			
		final Collator col = Collator.getInstance();
		switch (item.getItemId())
		{

		case R.id.sortByTitle:			
			adapter.sort(new Comparator<Adapter_row>() {
			    public int compare(Adapter_row arg0, Adapter_row arg1) {
			       return col.compare(arg0.filename, arg1.filename);
			    }
			});
			adapter.setNotifyOnChange(true);
			break;

		case R.id.sortByDate:
			adapter.sort(new Comparator<Adapter_row>() {
			    public int compare(Adapter_row arg0, Adapter_row arg1) {
			    	try {
						return dateFormat.parse(arg0.filedate).compareTo(dateFormat.parse(arg1.filedate));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return -1;
					}
			    }
			});
			adapter.setNotifyOnChange(true);
			break;

		}
		return true;
	}
	
	private static class ViewHolder
	{
		public TextView tv_filename;
		public TextView tv_filedate;
		public ImageView iv_fileico;
	}	
	
	public class ListQuestionsAdapter extends ArrayAdapter<Adapter_row>
	{
		private final Context context;
		private ViewHolder holder;
		public ArrayList<Adapter_row> rows;
		private GestureDetector mDetector;
		private ActionBookManager am;

		public ListQuestionsAdapter(Context context, ArrayList<Adapter_row> Rows)
		{
			super(context, R.layout.layo_row_booklist, Rows);
			this.context = context;
			this.rows = Rows;
			am = new ActionBookManager(BookList.this, mDBApi, rows);			
			mDetector = new GestureDetector(am);
		}
				
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View rowView = convertView;
			if (rowView == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.layo_row_booklist, parent, false);
				holder = new ViewHolder();
				holder.tv_filename = (TextView) rowView.findViewById(R.id.layo_row_booklist_tv_filename);
				holder.tv_filedate = (TextView) rowView.findViewById(R.id.layo_row_booklist_tv_filedate);
				holder.iv_fileico = (ImageView) rowView.findViewById(R.id.layo_row_booklist_iv_bookico);

				rowView.setTag(holder);
			}

			holder = (ViewHolder) rowView.getTag();			
			holder.tv_filename.setText(rows.get(position).filename);
			holder.tv_filedate.setText(rows.get(position).filedate);
			
			
			holder.iv_fileico.setTag(Integer.valueOf(position));
			holder.iv_fileico.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					int position = (Integer) v.getTag();
					am.SetPosition(position);
				    mDetector.onTouchEvent(event);				    
				    return true;
				}});
			
			return rowView;
		}
	}
	
}
