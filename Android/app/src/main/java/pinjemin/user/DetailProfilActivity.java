/** ===================================================================================
 * [DETAIL PROFIL ACTIVITY]
 * Activity saat melihat profil suatu user
 * ------------------------------------------------------------------------------------
 * Author: Kemal Amru Ramadhan
 * Refactoring & Doumentation: Ferdinand Antonius
 * =================================================================================== */

package pinjemin.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.TreeMap;

import pinjemin.R;
import pinjemin.backgroundTask.FriendActionTask;
import pinjemin.backgroundTask.ReviewTask;
import pinjemin.backgroundTask.UbahProfilFetchTask;
import pinjemin.menu_friend.FriendRequestFragment;
import pinjemin.menu_friend.FriendTemanAndaFragment;
import pinjemin.session.SessionManager;


public class DetailProfilActivity extends AppCompatActivity
{
	private Toolbar toolbar;

	private TextView outputRealname, outputAccountname, outputRating, outputNumRating,
		outputBio, outputFakultas, outputProdi, outputTelepon, btnUbahProfil, btnSetujuRequest,
		btnTolakRequest, btnBatalRequest, btnTambahTeman, btnHapusTeman;

	private LinearLayout setujuTolak;

	private String uid, realName, accountName, bio, fakultas, prodi,
		telepon, rating, numRating, status;

	private SessionManager sessionManager;
	private String currentUid;


	/** ==============================================================================
	 * Inisialisasi fragments dan loaders, dipanggil sebelum activity di-start
	 * ============================================================================== */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_profil);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle("Detail Profil");
		setSupportActionBar(toolbar);

		sessionManager = new SessionManager(this);
		currentUid = sessionManager.getUserDetails().get(SessionManager.KEY_UID);

		outputRealname = (TextView) findViewById(R.id.output_realname);
		outputAccountname = (TextView) findViewById(R.id.output_accountname);
		outputBio = (TextView) findViewById(R.id.output_bio);
		outputFakultas = (TextView) findViewById(R.id.output_fakultas);
		outputProdi = (TextView) findViewById(R.id.output_prodi);
		outputTelepon = (TextView) findViewById(R.id.output_telepon);
		outputRating = (TextView) findViewById(R.id.output_rating);
		outputNumRating = (TextView) findViewById(R.id.output_numrating);

		btnUbahProfil = (TextView) findViewById(R.id.btn_ubah_profil);
		btnSetujuRequest = (TextView) findViewById(R.id.btn_setuju_request);
		btnTolakRequest = (TextView) findViewById(R.id.btn_tolak_request);
		btnBatalRequest = (TextView) findViewById(R.id.btn_batal_request);
		btnTambahTeman = (TextView) findViewById(R.id.btn_tambah_teman);
		btnHapusTeman = (TextView) findViewById(R.id.btn_hapus_teman);

		setujuTolak = (LinearLayout) findViewById(R.id.setuju_tolak);

		Intent intent = getIntent();
		uid = intent.getStringExtra("uid");
		realName = intent.getStringExtra("realName");
		accountName = intent.getStringExtra("accountName");
		bio = intent.getStringExtra("bio");
		fakultas = intent.getStringExtra("fakultas");
		prodi = intent.getStringExtra("prodi");
		telepon = intent.getStringExtra("telepon");

		numRating = intent.getStringExtra("numRating");
		rating = intent.getStringExtra("rating");
		status = intent.getStringExtra("status");

		outputRealname.setText(realName);
		outputAccountname.setText(accountName);
		outputFakultas.setText(fakultas);
		outputProdi.setText(prodi);
		outputBio.setText(bio);
		outputTelepon.setText("Tambahkan sebagai teman untuk melihat");
		;

		// agar bio tidak terlihat kosong
		if (bio.length() == 0) {
			outputBio.setText("n/a");
		}

		// setting rating dengan bintang
		if (rating.equals("NaN")) {
			outputRating.setText("Belum ada rating");
			outputNumRating.setText("");
		}
		else {
			double ratingDouble = Double.parseDouble(rating);
			String starString = "";

			if (ratingDouble >= 4.5) starString = "\u2605\u2605\u2605\u2605\u2605";
			else if (ratingDouble >= 3.5) starString = "\u2605\u2605\u2605\u2605";
			else if (ratingDouble >= 2.5) starString = "\u2605\u2605\u2605";
			else if (ratingDouble >= 1.5) starString = "\u2605\u2605";
			else starString = "\u2605";

			outputRating.setText(starString + " " + rating);
			outputNumRating.setText(" \u00B7 berdasarkan " + numRating + " review");
		}

		// ubah daata dan action buttoins yang ditampilkan
		// berdasarkan relasi user yang masuk dan yang profil user yang dilihat
		if (status.equalsIgnoreCase("OwnProfile")) {
			btnUbahProfil.setVisibility(View.VISIBLE);
			outputTelepon.setText(telepon);
			btnUbahProfil.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					ubahProfil();
				}
			});
		}
		else if (status.equalsIgnoreCase("NotFriends")) {
			btnTambahTeman.setVisibility(View.VISIBLE);
			btnTambahTeman.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					tambahTeman();
				}
			});

		}
		else if (status.equalsIgnoreCase("Friends")) {
			btnHapusTeman.setVisibility(View.VISIBLE);
			outputTelepon.setText(telepon);
			btnHapusTeman.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					hapusTeman();
				}
			});

		}
		else if (status.equalsIgnoreCase("Requesting")) {
			btnBatalRequest.setVisibility(View.VISIBLE);
			btnBatalRequest.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					batalRequest();
				}
			});
		}
		else if (status.equalsIgnoreCase("Requested")) {
			setujuTolak.setVisibility(View.VISIBLE);
			btnSetujuRequest.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					setujuRequest();
				}
			});
			btnTolakRequest.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					tolakRequest();
				}
			});
		}

		TreeMap<String,String> inputData = new TreeMap<>();
		inputData.put("targetUID", uid);
		ReviewTask task = new ReviewTask(this, inputData);
		task.execute();
	}

	/** ==============================================================================
	 * Handler saat tombol back ditekan
	 * ============================================================================== */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}


	// --- action handlers ---

	/** ==============================================================================
	 * Handler saat tombol ubah profil ditekan
	 * ============================================================================== */
	public void ubahProfil() {
		TreeMap<String,String> input = new TreeMap<>();
		input.put("ownUID", currentUid);
		input.put("targetUID", currentUid);

		UbahProfilFetchTask ubahProfil = new UbahProfilFetchTask(this, input);
		ubahProfil.execute();
	}

	/** ==============================================================================
	 * Handler saat tombol tambah teman ditekan
	 * ============================================================================== */
	public void tambahTeman() {
		TreeMap<String,String> inputSend = new TreeMap<>();
		inputSend.put("ownUID", currentUid);
		inputSend.put("partnerUID", uid);

		FriendActionTask task = new FriendActionTask(this, FriendActionTask.ADD, inputSend, realName);
		task.execute();
		finish();

		FriendRequestFragment.performRefresh();
		FriendTemanAndaFragment.performRefresh();
	}

	/** ==============================================================================
	 * Handler saat tombol hapus teman ditekan
	 * ============================================================================== */
	public void hapusTeman() {
		// membentuk dialog yes/no
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Apakah Anda yakin untuk menghapus pertemanan ini?");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				TreeMap<String,String> inputSend = new TreeMap<>();

				inputSend.put("ownUID", currentUid);
				inputSend.put("partnerUID", uid);

				FriendActionTask task = new FriendActionTask(getApplicationContext(), FriendActionTask.DELETE, inputSend, realName);
				task.execute();
				finish();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

		// tampilkan dialog
		builder.create().show();
	}

	/** ==============================================================================
	 * Handler saat tombol batal memberikan request ditekan
	 * ============================================================================== */
	public void batalRequest() {
		TreeMap<String,String> inputSend = new TreeMap<>();
		inputSend.put("ownUID", currentUid);
		inputSend.put("partnerUID", uid);

		FriendActionTask task = new FriendActionTask(this, FriendActionTask.CANCEL, inputSend, realName);
		task.execute();
		finish();
	}

	/** ==============================================================================
	 * Handler saat tombol tolak request ditekan
	 * ============================================================================== */
	public void tolakRequest() {
		TreeMap<String,String> inputSend = new TreeMap<>();
		inputSend.put("ownUID", currentUid);
		inputSend.put("partnerUID", uid);

		FriendActionTask task = new FriendActionTask(this, FriendActionTask.REJECT, inputSend, realName);
		task.execute();
		finish();

		FriendRequestFragment.performRefresh();
		FriendTemanAndaFragment.performRefresh();
	}

	/** ==============================================================================
	 * Handler saat tombol setujui request ditekan
	 * ============================================================================== */
	public void setujuRequest() {
		TreeMap<String,String> inputSend = new TreeMap<>();
		inputSend.put("ownUID", currentUid);
		inputSend.put("partnerUID", uid);

		FriendActionTask task = new FriendActionTask(this, FriendActionTask.ACCEPT, inputSend, realName);
		task.execute();
		finish();

		FriendRequestFragment.performRefresh();
		FriendTemanAndaFragment.performRefresh();
	}
}
