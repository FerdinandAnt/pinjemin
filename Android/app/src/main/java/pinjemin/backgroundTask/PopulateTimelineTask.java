/** ===================================================================================
 * [POPULATE TIMELINE TASK]
 * Helper class untuk mengirim data ke web sercive di server (asynchronously)
 * Dipakai untuk kelas CreatePostDemandActivity, CreatePostSupplyActivity
 * ------------------------------------------------------------------------------------
 * Author: Ferdinand Antonius, Kemal Amru Ramadhan
 * Refactoring & Documentation: Ferdinand Antonius
 * =================================================================================== */

package pinjemin.backgroundTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import pinjemin.adapter.TimelineDemandAdapter;
import pinjemin.adapter.TimelineSupplyAdapter;
import pinjemin.behavior.ClickListener;
import pinjemin.menu_timeline.DetailPostDemandActivity;
import pinjemin.model.PostDemand;
import pinjemin.model.PostSupply;
import pinjemin.utility.UtilityConnection;
import pinjemin.utility.UtilityDate;


public class PopulateTimelineTask extends AsyncTask<Void,Object,Void>
{
	public static final String PHP_PATH_SUPPLY_TIMELINE = "getpenawarantimeline.php";
	public static final String PHP_PATH_DEMAND_TIMELINE = "getpermintaantimeline.php";
	public static final int DEMAND_POST = 1;
	public static final int SUPPLY_POST = 2;

	private Activity activity;
	private Context context;
	private String phpFilePath;
	private int timelineType;
	private boolean isConnectedToServer;

	// bagian RecyclerView:
	private RecyclerView.Adapter adapter;
	private RecyclerView.LayoutManager layoutManager;

	// penampung object RecyclerView:
	private ArrayList<PostSupply> arraySupply;
	private ArrayList<PostDemand> arrayDemand;


	/** ==============================================================================
	 * Constructor kelas PopulateTimelineTask
	 * @param context - context dari mana PopulateTimelineTask dipanggil
	 * @param timelineType - DEMAND_POST atau SUPPLY_POST, tergantung jenis
	 * 	timeline yang akan dimintakan ke server.
	 * ============================================================================== */
	public PopulateTimelineTask(Context context, int timelineType, RecyclerView.Adapter adapter) {
		this.context = context;
		this.context = (Activity) context;
		this.timelineType = timelineType;
		this.adapter = adapter;

		// configure file phpFilePath dan array yang benar
		if (timelineType == SUPPLY_POST) {
			this.phpFilePath = PHP_PATH_SUPPLY_TIMELINE;
			this.arraySupply = ((TimelineSupplyAdapter) adapter).getarrayList();
			this.arraySupply.clear();
		}
		else if (timelineType == DEMAND_POST) {
			this.phpFilePath = PHP_PATH_DEMAND_TIMELINE;
			this.arrayDemand = ((TimelineDemandAdapter) adapter).getarrayList();
			this.arrayDemand.clear();
		}
	}

	/** ==============================================================================
	 * Hal yang perlu dilakukan SEBELUM subclass AsyncTask ini di-execute
	 * ============================================================================== */
	@Override
	protected void onPreExecute() {
		// configure layoutManager
		layoutManager = new LinearLayoutManager(context);
	}

	/** ==============================================================================
	 * Hal yang perlu dilakukan saat subclass AsyncTask ini di-execute
	 * ============================================================================== */
	@Override
	protected Void doInBackground(Void... params) {
		try {
			// kirim permintaan ke server, tanpa mengirimkan parameter apa pun
			this.isConnectedToServer = false;
			String serverResponse = UtilityConnection.runPhp(phpFilePath, null);
			this.isConnectedToServer = true;
			Log.d("DEBUG", serverResponse);

			// parse data JSON yang diterima dari server (berisi daftar post)
			JSONObject jsonResponseObject = new JSONObject(serverResponse);
			JSONArray jsonResponseArray = jsonResponseObject.getJSONArray("server_response");
			int jsonResponseArrayLength = jsonResponseArray.length();

			for (int i = 0; i < jsonResponseArrayLength; i++) {
				JSONObject postInstance = jsonResponseArray.getJSONObject(i);

				// extract fields dari postInstance:
				String dataPID = postInstance.getString("PID");
				String dataUID = postInstance.getString("UID");
				String dataTimestamp = postInstance.getString("Timestamp");
				String dataNamaBarang = postInstance.getString("NamaBarang");
				String dataDeskripsi = postInstance.getString("Deskripsi");
				String dataRealName = postInstance.getString("RealName");
				String dataFormattedDate = UtilityDate.formatTimestampElapsedTime(dataTimestamp);

				if (timelineType == SUPPLY_POST) {
					// dapatkan field khusus untuk post supply (harga)
					String dataHarga = postInstance.getString("Harga");

					// buat instance PostSupply baru
					PostSupply postSupply = new PostSupply(
						dataPID, dataUID, dataTimestamp, dataNamaBarang,
						dataDeskripsi, dataHarga, dataRealName);

					// publish perubahan ke main UI thread
					// pemanggilan ini akan memanggil onProgressUpdate() di bawah
					publishProgress(postSupply);
				}
				else if (timelineType == DEMAND_POST) {
					// dapatkan field khusus untuk post demand (lastNeed)
					String dataLastNeed = postInstance.getString("LastNeed");

					// buat instance PostSupply baru
					PostDemand postDemand = new PostDemand(
						dataPID, dataUID, dataTimestamp, dataNamaBarang,
						dataDeskripsi, dataLastNeed, dataRealName);

					// publish perubahan ke main UI thread
					// pemanggilan ini akan memanggil onProgressUpdate() di bawah
					publishProgress(postDemand);
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			Log.d("DEBUG", "Tried accessing host: " + phpFilePath);
			try {
				// BackgroundTask bukan main thread. pemanggilan methods yang berkaitan dengan UI
				// (e.g. Toast.makeText) harus dipanggil dari main thread, atau akan menghasilkian
				// RuntimeException. Ini cuma akal-akalan saja supaya dijalankan di main thread.
				Log.d("DEBUG", "Pichika!!");
				activity.runOnUiThread(new Runnable()
				{
					public void run() {
						Log.d("DEBUG", "Pichaku!");
						Toast.makeText(context, "Tidak bisa menghubungi server.", Toast.LENGTH_LONG).show();
					}
				});
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
		}

		return null;
	}

	/** ==============================================================================
	 * Hal yang perlu dilakukan SELAMA subclass AsyncTask ini di-execute
	 * @param object - normalnya berisi instance yang baru saja di-parse dari server
	 * ============================================================================== */
	@Override
	protected void onProgressUpdate(Object... object) {
		if (timelineType == SUPPLY_POST) {
			// tambahkan instance PostSupply ke arraySupply
			// notify adapter bahwa datanya sudah berubah (supaya di-relayout)
			arraySupply.add((PostSupply) object[0]);
			adapter.notifyDataSetChanged();
		}
		else if (timelineType == DEMAND_POST) {
			// tambahkan instance PostSupply ke arraySupply
			// notify adapter bahwa datanya sudah berubah (supaya di-relayout)
			arrayDemand.add((PostDemand) object[0]);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onPostExecute(Void aVoid) {
		if (!isConnectedToServer) {
			// methods yang mengakses UI harus dijalankan di thread utama.
			// doInBackground() untuk kelas AsyncTask berjalan di thread sendiri.
			// kalau ditaruh di sana, akan RuntimeException saat mengakses Toast.makeText()
			Toast.makeText(context, "Tidak bisa menghubungi server.", Toast.LENGTH_LONG).show();
		}
	}
}