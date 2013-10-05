package net.bqtbl.lists;

import java.text.Collator;
import java.text.Format;
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
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ListActivity;
import android.content.Context;
import net.bqtbl.lists.MyGestureDetector;


public class BookList extends ListActivity {

	final static private String APP_KEY = "b7bmniiymc8xzs5";
	final static private String APP_SECRET = "9ky56d1uqoxsg4m";
	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
	List<Entry> FileList;
	private DropboxAPI<AndroidAuthSession> mDBApi;
	private ListQuestionsAdapter adapter;

	
	private class Adapter_row
	{

		public Adapter_row(String filename, String filedate)
		{
			this.filename = filename;
			this.filedate = filedate;
		}		
		public String filename;
		public String filedate;
	}
	
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
	protected void onResume() {
	    super.onResume();

	    if (mDBApi.getSession().authenticationSuccessful()) {
	        try {
	            mDBApi.getSession().finishAuthentication();
	            //AccessTokenPair tokens = 
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
			       return col.compare(arg0.filedate, arg1.filedate);
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
		ViewHolder holder;
		ArrayList<Adapter_row> rows;
		Format formatter = new SimpleDateFormat("dd/MM/yyyy HH:MM:SS", Locale.getDefault());

		public ListQuestionsAdapter(Context context, ArrayList<Adapter_row> Rows)
		{
			super(context, R.layout.layo_row_booklist, Rows);
			this.context = context;
			this.rows = Rows;
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
			holder.iv_fileico.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(final View view)
				{
					int position = (Integer) view.getTag();
					//Toast.makeText(BookList.this, "Click on Ico : " + String.valueOf(position), Toast.LENGTH_SHORT).show();
					notifyDataSetChanged();
				}
			}); 
			
			holder.iv_fileico.setOnTouchListener(new OnTouchListener() {
	             GestureDetector gestureDetector = new GestureDetector(new MyGestureDetector(BookList.this));
	             @Override
	             public boolean onTouch(View v, MotionEvent event) {
	                    return gestureDetector.onTouchEvent(event);
	             }
	    });

			
			return rowView;
		}
	}
	
}
