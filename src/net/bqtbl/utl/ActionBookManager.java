package net.bqtbl.utl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import net.bqtbl.structs.Adapter_row;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class ActionBookManager extends SimpleOnGestureListener {
	private Context context;
	private DropboxAPI<AndroidAuthSession> mDBApi;
	private ArrayList<Adapter_row> rows;
	private int position = 0;

	public ActionBookManager(Context con, DropboxAPI<AndroidAuthSession> mDBAp,
			ArrayList<Adapter_row> rows) {
		this.context = con;
		this.mDBApi = mDBAp;
		this.rows = rows;
	}
	
	public void SetPosition(int position)
	{
		this.position = position;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return super.onDown(e);
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		Toast.makeText(context.getApplicationContext(), "Openning Cover, wait...",
				Toast.LENGTH_LONG).show();
		ShowCover(rows.get(position).filename);
		return super.onDoubleTap(e);
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return super.onSingleTapUp(e);
	}

	private void ShowCover(final String fileName) {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			File file, folder;
			FileOutputStream outputStream;
			Bitmap coverImage;
			EpubReader epubReader;
			Book book;

			@Override
			protected void onPreExecute() {
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					folder = context.getCacheDir();
					file = File.createTempFile(fileName, "$tmp$", folder);
					outputStream = new FileOutputStream(file);
					mDBApi.getFile("/" + fileName, null, outputStream, null);
					outputStream.flush();	
					outputStream.close();					
					epubReader = new EpubReader();
					book = epubReader.readEpub(new FileInputStream(file));
					if (book.getCoverImage() != null) {
						coverImage = BitmapFactory.decodeByteArray(book.getCoverImage().getData(), 0, (int)book.getCoverImage().getSize());						
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (book.getCoverImage() != null) {
					AlertDialog.Builder imageDialog = new AlertDialog.Builder(context);
				    ImageView image = new ImageView(context);
				    image.setImageBitmap(coverImage);
				    imageDialog.setView(image);
				    imageDialog.setCancelable(true);
				    imageDialog.create();
				    imageDialog.show();
				}
				else
				Toast.makeText(context.getApplicationContext(), "No Cover",
						Toast.LENGTH_LONG).show();
			}
		};
		task.execute((Void[]) null);
	}
}