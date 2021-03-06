/**
 * ===================================================================================
 * [REGISTER ACTIVITY]
 * Kelas yang menampilkan halaman untuk register
 * ------------------------------------------------------------------------------------
 * Author: Kemal Amru Ramadhan
 * Refactoring & Documentation: Ferdinand Antonius
 * ===================================================================================
 */

package pinjemin.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.TreeMap;

import pinjemin.R;
import pinjemin.backgroundTask.RegisterTask;
import pinjemin.backgroundTask.UbahProfilSubmitTask;
import pinjemin.behavior.EditTextTextWatcher;
import pinjemin.session.SessionManager;
import pinjemin.utility.UtilityGUI;


public class UbahProfilActivity extends AppCompatActivity
{
	private Toolbar toolbar;
	private SessionManager sessionManager;

	private TextInputLayout inputLayoutName;
	private TextInputLayout inputLayoutFakultas;
	private TextInputLayout inputLayoutProdi;
	private TextInputLayout inputLayoutTelepon;
	private TextInputLayout inputLayoutBio;

	private EditText inputName;
	private EditText inputFakultas;
	private EditText inputProdi;
	private EditText inputTelepon;
	private EditText inputBio;
	private Button buttonSubmit;

	private String intentName, intentFakultas, intentProdi, intentTelepon, intentBio;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		// initialize toolbar:
		// set toolbar sebagai action bar (main toolbar) untuk activity ini
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle("Ubah Profil");
		setSupportActionBar(toolbar);

		// initialize sessionManager:
		sessionManager = new SessionManager(this);

		// initialize layout:
		inputLayoutName = (TextInputLayout) findViewById(R.id.input_layout_name);
		inputLayoutFakultas = (TextInputLayout) findViewById(R.id.input_layout_fakultas);
		inputLayoutProdi = (TextInputLayout) findViewById(R.id.input_layout_prodi);
		inputLayoutTelepon = (TextInputLayout) findViewById(R.id.input_layout_telepon);
		inputLayoutBio = (TextInputLayout) findViewById(R.id.input_layout_bio);

		// initialize components:
		inputName = (EditText) findViewById(R.id.input_name);
		inputFakultas = (EditText) findViewById(R.id.input_fakultas);
		inputProdi = (EditText) findViewById(R.id.input_prodi);
		inputTelepon = (EditText) findViewById(R.id.input_telepon);
		inputBio = (EditText) findViewById(R.id.input_bio);
		buttonSubmit = (Button) findViewById(R.id.btn_submit);

		// ambil intent yang diberikan ke sini
		Intent intent = getIntent();
		intentName = intent.getStringExtra("realName");
		intentFakultas = intent.getStringExtra("fakultas");
		intentProdi = intent.getStringExtra("prodi");
		intentTelepon = intent.getStringExtra("telepon");
		intentBio = intent.getStringExtra("bio");

		inputName.setText(intentName);
		inputFakultas.setText(intentFakultas);
		inputProdi.setText(intentProdi);
		inputTelepon.setText(intentTelepon);
		inputBio.setText(intentBio);

		// set action listener (text watchers)
		// (Field bio tidak perlu di-validate)
		inputName.addTextChangedListener(new EditTextTextWatcher(
			this, inputName, inputLayoutName, "Masukkan nama lengkap Anda"));
		inputFakultas.addTextChangedListener(new EditTextTextWatcher(
			this, inputFakultas, inputLayoutFakultas, "Masukkan fakultas Anda"));
		inputProdi.addTextChangedListener(new EditTextTextWatcher(
			this, inputProdi, inputLayoutProdi, "Masukkan jurusan Anda"));
		inputTelepon.addTextChangedListener(new EditTextTextWatcher(
			this, inputTelepon, inputLayoutTelepon, "Masukkan nomor telepon Anda"));
		inputTelepon.addTextChangedListener(new EditTextTextWatcher(
			this, inputTelepon, inputLayoutTelepon, "Masukkan angka saja untuk telepon"));

		// NOTE: inner class ButtonSubmitListener diimplementasikan di bawah
		buttonSubmit.setOnClickListener(new ButtonSubmitListener(this));
	}

	/** ==============================================================================
	 * Handler ketika tombol back ditekan
	 * ============================================================================== */
	@Override
	public void onBackPressed() {
		// tutup activity ini
		Log.d("DEBUG", "Keluar dari ubah profil...");
		finish();
	}


	// --- inner class declaration ---

	/** ==============================================================================
	 * Custom implementation kelas View.OnClickListener, digunakan
	 * sebagai action listener untuk buttonSubmit (saat tombol tersebut ditekan)
	 * ============================================================================== */
	private class ButtonSubmitListener implements View.OnClickListener
	{
		Activity activity;

		public ButtonSubmitListener(Activity activity) {
			this.activity = activity;
		}

		@Override
		public void onClick(View view) {
			// cek apakah setiap field sudah diisi dengan benar
			if (!UtilityGUI.assureNotEmpty(activity, inputName, inputLayoutName,
				"Masukkan nama lengkap Anda")) return;
			if (!UtilityGUI.assureNotEmpty(activity, inputFakultas, inputLayoutFakultas,
				"Masukkan fakultas Anda")) return;
			if (!UtilityGUI.assureNotEmpty(activity, inputProdi, inputLayoutProdi,
				"Masukkan jurusan Anda")) return;
			if (!UtilityGUI.assureNotEmpty(activity, inputTelepon, inputLayoutTelepon,
				"Masukkan nomor telepon Anda")) return;
			if (!UtilityGUI.assureNumeric(activity, inputTelepon, inputLayoutTelepon,
				"Masukkan angka saja untuk telepon")) return;

			// ambil data yang dimasukkan user
			String realname = inputName.getText().toString();
			String fakultas = inputFakultas.getText().toString();
			String prodi = inputProdi.getText().toString();
			String telepon = inputTelepon.getText().toString();
			String bio = inputBio.getText().toString();

			// ambil data UID dari sessionManager
			HashMap<String,String> sessionData = sessionManager.getUserDetails();
			String uid = sessionData.get(SessionManager.KEY_UID);

			// susun data untuk dikirim ke server
			TreeMap<String,String> dataToSend = new TreeMap<>();
			dataToSend.put("uid", uid);
			dataToSend.put("realname", realname);
			dataToSend.put("fakultas", fakultas);
			dataToSend.put("prodi", prodi);
			dataToSend.put("telepon", telepon);
			dataToSend.put("bio", bio);
			dataToSend.put("ubah", "iya");

			// bisa reuse dari background task saat register
			UbahProfilSubmitTask registerTask = new UbahProfilSubmitTask(
				UbahProfilActivity.this, dataToSend);
			registerTask.execute();

			// tutup activity ini
			finish();
		}
	}
}