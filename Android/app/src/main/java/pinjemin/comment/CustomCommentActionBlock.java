/** ===================================================================================
 * [CUSTOM COMMENT BLOCK]
 * Kelas yang berfungsi untuk membuat kotak action buttons di bawah thread komentar
 * ------------------------------------------------------------------------------------
 * Author: Ferdinand Antonius
 * Refactoring & Doumentation: Ferdinand Antonius
 * =================================================================================== */

package pinjemin.comment;

// pochika

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TreeMap;

import pinjemin.backgroundTask.CommentActionTask;
import pinjemin.session.SessionManager;
import pinjemin.utility.UtilityGUI;


public class CustomCommentActionBlock
{
	public static final int ACTIONS_NONE = 0;
	public static final int ACTIONS_CAN_INITIATE = 1;
	public static final int ACTIONS_CAN_CONFIRM = 2;
	public static final int ACTIONS_CAN_CANCEL = 3;

	public static final int COLOR_BLACK = 0xff000000;
	public static final int COLOR_WHITE = 0x50ffffff;
	public static final int COLOR_GRAY = 0xffcecece;

	public static final int COLOR_TRANSPARENT = 0x00ffffff;
	public static final int COLOR_BUTTON = 0xff009688;

	private LinearLayout actionButtonsViewContainer;
	private LinearLayout buttonsContainer;
	private Activity activity;
	private int parentUID;
	private int postPID;
	private int loggedInUID;
	private int possibleAction;
	private int targetUID;

	/** ==============================================================================
	 * Constructor kelas CustomCommentActionBlock
	 * ============================================================================== */
	public CustomCommentActionBlock(Activity activity, int postPID, int parentUID, int possibleAction, int targetUID) {
		this.activity = activity;
		this.parentUID = parentUID;
		this.postPID = postPID;
		this.possibleAction = possibleAction;
		this.targetUID = targetUID;

		// dapatkan UID yang sedang login
		SessionManager sessionManager = new SessionManager(activity);
		this.loggedInUID = Integer.parseInt(
			sessionManager.getUserDetails().get(SessionManager.KEY_UID));

		// attach view ke dalam satu LinearLayout
		constructLinearLayout();
	}

	/** ==============================================================================
	 * Untuk meng-instantiate LinearLayout actionbuttonsViewContainer dan meng-attach
	 * semua child view ke dalam actionbuttonsViewContainer.
	 * ============================================================================== */
	private void constructLinearLayout() {
		// menghitung konversi satuan dari dp ke px
		int padding_10dp = UtilityGUI.dpIntoPixel(activity, 10);
		int padding_5dp = UtilityGUI.dpIntoPixel(activity, 5);

		// initialize actionButtonsViewContainer:
		// Ini menampung upperBorder, buttonsContainer, dan bottomBorder
		actionButtonsViewContainer = new LinearLayout(activity);
		actionButtonsViewContainer.setLayoutParams(new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT));
		actionButtonsViewContainer.setPadding(0, 0, 0, 0);
		actionButtonsViewContainer.setOrientation(LinearLayout.VERTICAL);
		actionButtonsViewContainer.setBackgroundColor(COLOR_WHITE);

		// initialize buttonContainer:
		// digunakan untuk menampung buttons-nya saja dalam horizontal linear layout
		buttonsContainer = new LinearLayout(activity);
		buttonsContainer.setLayoutParams(new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT));
		buttonsContainer.setPadding(0, 0, padding_5dp, 0);
		buttonsContainer.setOrientation(LinearLayout.HORIZONTAL);
		buttonsContainer.setGravity(Gravity.RIGHT);
		buttonsContainer.setBackgroundColor(COLOR_TRANSPARENT);

		// configure garis border atas dan bawah
		// setLayoutParams: width=MATCH_PARENT, height=1px
		View upperBorder = new View(activity);
		upperBorder.setLayoutParams(new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, 1));
		upperBorder.setBackgroundColor(COLOR_GRAY);

		View bottomBorder = new View(activity);
		bottomBorder.setLayoutParams(new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, 1));
		bottomBorder.setBackgroundColor(COLOR_GRAY);


		//----------------------------------------
		// ATTACH BUTTON SAJA KE buttonsContainer
		//----------------------------------------

		// tampung dulu semua tombolnya di ArrayList
		// NOTE: inner class ButtonTextView dideklarasikan di bawah
		ArrayList<ButtonTextView> buttonsArray = new ArrayList<>();

		// in any case, tombol balas selalu ditampilkan
		ButtonTextView balasButton = new ButtonTextView(activity, "BALAS", new View.OnClickListener()
		{
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(activity.getApplicationContext(), CommentActivity.class);
				intent.putExtra("parentUid", "" + parentUID);
				intent.putExtra("ownUid", "" + loggedInUID);
				intent.putExtra("pid", "" + postPID);
				intent.putExtra("type", "reply");

				activity.startActivity(intent);
				Log.d("DEBUG", "Tadinya mau bales...");
			}
		});
		buttonsArray.add(balasButton);

		// bisa initiate penyerahan barang
		if (possibleAction == ACTIONS_CAN_INITIATE) {
			ButtonTextView initiateButton = new ButtonTextView(
				activity, "PENYERAHAN BARANG", new View.OnClickListener()
			{
				@Override
				public void onClick(View view) {
					Log.d("DEBUG", "Tadinya mau initiate...");
					Intent intent = new Intent(activity, UbahDeadlineActivity.class);
					intent.putExtra("pid", "" + postPID);
					intent.putExtra("targetUID", "" + targetUID);

					activity.startActivity(intent);

				}
			});
			buttonsArray.add(initiateButton);
		}

		// bisa confirm penyerahan barang
		else if (possibleAction == ACTIONS_CAN_CONFIRM) {
			ButtonTextView confirmInitiateButton = new ButtonTextView(
				activity, "SUDAH DITERIMA!", new View.OnClickListener()
			{
				@Override
				public void onClick(View view) {
					Log.d("DEBUG", "Tadinya mau confirm initiate...");
					TreeMap<String,String> inputData = new TreeMap<>();
					inputData.put("PID", "" + postPID);
					inputData.put("ownUID", "" + loggedInUID);
					inputData.put("targetUID", "" + targetUID);

					CommentActionTask task = new CommentActionTask(activity, CommentActionTask.CONFIRM_TRANSFER, inputData);
					task.execute();
				}
			});
			buttonsArray.add(confirmInitiateButton);
		}

		// bisa batalkan initiate penyerahan barang
		else if (possibleAction == ACTIONS_CAN_CANCEL) {
			ButtonTextView cancelInitiateButton = new ButtonTextView(
				activity, "NGGAK JADI NGASIH", new View.OnClickListener()
			{
				@Override
				public void onClick(View view) {
					Log.d("DEBUG", "Tadinya mau cancel initiate...");
					TreeMap<String,String> inputData = new TreeMap<>();
					inputData.put("PID", "" + postPID);
					inputData.put("ownUID", "" + loggedInUID);
					inputData.put("targetUID", "" + targetUID);

					CommentActionTask task = new CommentActionTask(activity, CommentActionTask.CANCEL_TRANSFER, inputData);
					task.execute();
				}
			});
			buttonsArray.add(cancelInitiateButton);
		}

		// attach semua button ke commentEntryViewContainer
		for (ButtonTextView actionButton : buttonsArray) {
			buttonsContainer.addView(actionButton.getTextView());
		}

		//--------------------------------------------
		// ATTACH SEMUA ke actionButtonsViewContainer
		//--------------------------------------------

		// attach semua ke commentEntryViewContainer
		actionButtonsViewContainer.addView(upperBorder);
		actionButtonsViewContainer.addView(buttonsContainer);
		actionButtonsViewContainer.addView(bottomBorder);
	}

	/** ==============================================================================
	 * Mengembalikan instance LinearLayout yang mengandung action buttons untuk
	 * thread komentar
	 * @return instance LinearLayout, siap untuk di-attach ke parent view-nya.
	 * ============================================================================== */
	public LinearLayout getLinearLayout() {
		return actionButtonsViewContainer;
	}


	// --- inner class declarations ---

	/** ==============================================================================
	 * View object untuk tombol-tombol actions
	 * ============================================================================== */
	class ButtonTextView
	{
		TextView textView;

		public ButtonTextView(Activity activity, String text, View.OnClickListener clickListener) {
			// menghitung konversi satuan dari dp ke px
			int padding_10dp = UtilityGUI.dpIntoPixel(activity, 10);

			// buat TextView baru
			// NOTE: setClickable penting agar bisa di-klik!!!
			textView = new TextView(activity);
			textView.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
			textView.setText(text);
			textView.setTextColor(COLOR_BUTTON);
			textView.setPadding(padding_10dp, padding_10dp, padding_10dp, padding_10dp);
			textView.setTypeface(null, Typeface.BOLD);
			textView.setClickable(true);
			actionButtonsViewContainer.setBackgroundColor(COLOR_WHITE);

			// add listener ke textView
			textView.setOnClickListener(clickListener);
		}

		public TextView getTextView() {
			return textView;
		}
	}
}
