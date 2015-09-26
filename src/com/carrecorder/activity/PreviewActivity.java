package com.carrecorder.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import myjob.carrecorder.R;

import com.carrecorder.conf.ActivityConf;
import com.carrecorder.customer.view.treeview.adapter.SimpleTreeListViewAdapter;
import com.carrecorder.customer.view.treeview.bean.OrgBean;
import com.carrecorder.customer.view.treeview.utils.Node;
import com.carrecorder.customer.view.treeview.utils.adapter.TreeListViewAdapter.OnTreeNodeClickListener;
import com.carrecorder.customer.view.treeview.utils.adapter.TreeListViewAdapter.OnTreeNodeLongClickListener;
import com.carrecorder.db.table.Record;
import com.carrecorder.utils.Common;
import com.carrecorder.utils.camera.CameraUtils;
import com.db.DBExecutor;
import com.db.sql.Sql;
import com.db.sql.SqlFactory;

import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class PreviewActivity extends Activity {
	// tree view
	public ListView mTree;
	public SimpleTreeListViewAdapter<OrgBean> mAdapter;
	public List<OrgBean> mDatas2;
	public int idOfTreeList = 1;

	// flag for check box
	private int range1;
	private int range2;
	private int range3;
	private int range2_1;
	private int range2_2;
	private int range3_1;
	private int range3_2;

	// String for notice text
	private String str1;
	private String str2;
	private String str3;
	private String str4;
	private String str5;

	// view
	private CheckBox recorderCheckBox;
	private CheckBox lightCheckBox;
	private CheckBox lightCheckBox_1;
	private CheckBox lightCheckBox_2;
	private CheckBox overspeedCheckBox;
	private CheckBox overspeedCheckBox_1;
	private CheckBox overspeedCheckBox_2;
	private ImageView confirmView;

	// other
	private Vector<Record> showedRecords;
	private String videoPath;
	private String videoName;
	private DBExecutor db;

	// *********************TreeView*******************//
	private void initEvent() {
		mAdapter.setOnTreeNodeLongClickListener(new OnTreeNodeLongClickListener() {

			@Override
			public void onLongClick(Node node, int position) {
				if (node.getName().length() < 20) {
					return;
				}
				videoName = getVideoName(node);
				videoPath = getVideoPath(node);
				new AlertDialog.Builder(PreviewActivity.this)
						.setMessage("删除记录" + videoName + "?")
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								})
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										if (!CameraUtils.delFile(new File(
												videoPath))) {
											Toast.makeText(
													PreviewActivity.this,
													"没有找到该文件",
													Toast.LENGTH_SHORT).show();
										} else {
											Toast.makeText(
													PreviewActivity.this,
													"已删除视频：" + videoName,
													Toast.LENGTH_SHORT).show();
										}
										Sql sql = new SqlFactory().makeSql(
												Record.class,
												"delete from Record where videoName = '"
														+ videoName + "'");
										db.execute(sql);
										Toast.makeText(PreviewActivity.this,
												"已删除文字记录。", Toast.LENGTH_SHORT)
												.show();
										initialTreeView();
										initEvent();
										try {
											initDB();
										} catch (IllegalArgumentException e) {
											e.printStackTrace();
										} catch (IllegalAccessException e) {
											e.printStackTrace();
										}
									}
								}).show();
			}

		});
		mAdapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {
			@SuppressLint("SdCardPath")
			@Override
			public void onClick(Node node, int position) {
				if (node.isLeaf() && node.getName().length() > 20) {
					// String str = node.getName();
					// str = str.substring(str.length() - 19);
					String str = getVideoName(node);
					String path = getVideoPath(node);
					File testFile = new File(path);
					if (!testFile.exists()) {
						Toast.makeText(PreviewActivity.this, "没有找到视频文件耶",
								Toast.LENGTH_SHORT).show();
						return;
					}
					Intent intent = new Intent(Intent.ACTION_VIEW);
					String type = "video/3gp";
					Uri uri = Uri.parse("file:///sdcard/CarRecorder/video/"
							+ str);
					intent.setDataAndType(uri, type);
					startActivity(intent);

				}
			}
		});
	}

	private String getVideoName(Node node) {
		return Common.regularStr(node.getName(), "<([^<]*)>")
				.replaceAll("<", "").replaceAll(">", "");
	}

	private String getVideoPath(Node node) {
		String str = getVideoName(node);
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/CarRecorder/video/" + str;
		return path;
	}

	private void initView() {
		recorderCheckBox = (CheckBox) findViewById(R.id.recorder_checkbox);
		lightCheckBox = (CheckBox) findViewById(R.id.light_checkbox);
		lightCheckBox_1 = (CheckBox) findViewById(R.id.light_checkbox_1);
		lightCheckBox_2 = (CheckBox) findViewById(R.id.light_checkbox_2);
		overspeedCheckBox = (CheckBox) findViewById(R.id.overspeed_checkbox);
		overspeedCheckBox_1 = (CheckBox) findViewById(R.id.overspeed_checkbox_1);
		overspeedCheckBox_2 = (CheckBox) findViewById(R.id.overspeed_checkbox_2);
		// confirmBtn = (Button) findViewById(R.id.confirm_button);
		confirmView = (ImageView) findViewById(R.id.confirm_view);
		lightCheckBox_1.setVisibility(View.GONE);
		lightCheckBox_2.setVisibility(View.GONE);
		overspeedCheckBox_1.setVisibility(View.GONE);
		overspeedCheckBox_2.setVisibility(View.GONE);
		initialTreeView();
		initEvent();
	}

	private void initialTreeView() {
		mTree = (ListView) findViewById(R.id.id_listview);
		initDatas();
		try {
			mAdapter = new SimpleTreeListViewAdapter<OrgBean>(mTree, this,
					mDatas2, 0);
			mTree.setAdapter(mAdapter);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void initDatas() {
		mDatas2 = new ArrayList<OrgBean>();

		OrgBean bean2 = new OrgBean(idOfTreeList, 0, "总里程");
		idOfTreeList++;
		mDatas2.add(bean2);
		bean2 = new OrgBean(idOfTreeList, 0, "最近");
		idOfTreeList++;
		mDatas2.add(bean2);
		bean2 = new OrgBean(idOfTreeList, 0, "上一周");
		idOfTreeList++;
		mDatas2.add(bean2);
		bean2 = new OrgBean(idOfTreeList, 0, "之前");
		idOfTreeList++;
		mDatas2.add(bean2);

	}

	private void initListener() {
		recorderCheckBox
				.setOnCheckedChangeListener(new RecorderCheckboxListener());
		lightCheckBox.setOnCheckedChangeListener(new LightCheckboxListener());
		overspeedCheckBox
				.setOnCheckedChangeListener(new OverspeedCheckboxListener());
		confirmView.setOnClickListener(new ConfirmBtnListener());
		confirmView.setOnTouchListener(new ConfirmBtnTouchListener());
	}

	private void initDB() throws IllegalArgumentException,
			IllegalAccessException {
		int total = 0;
		String totalStr = "总里程：";
		db = DBExecutor.getInstance(this);
		Sql sql = SqlFactory.find(Record.class);
		List<Record> records = db.executeQuery(sql);
		// 删除
		// String str = SqlFactory.dropTable(Record.class);
		// sql = SqlFactory.makeSql(Record.class, str);
		// db.execute(sql);
		for (Record instance : records) {
			showedRecords.add(instance);
			total += instance.getMelige();
			mAdapter.addExtraNode(
					1,
					"行车距离:" + Common.mDist2kmDistStr(instance.getMelige())
							+ " | " + instance.getDate() + "\n<"
							+ instance.getVideoName() + ">");
		}
		totalStr = totalStr + Common.mDist2kmDistStr(total);
		mAdapter.addExtraNode(0, totalStr);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showedRecords = new Vector<Record>();
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// remove title bar
		setContentView(R.layout.activity_preview);
		initView();
		initListener();
		str4 = "akk";
		str5 = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/Android/";
		CopyAssets(str4, str5);
		try {
			initDB();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void CopyAssets(String assetDir, String dir) {
		String[] files;
		try {
			files = this.getResources().getAssets().list(assetDir);
		} catch (IOException e1) {
			return;
		}
		File mWorkingPath = new File(dir);
		// if this directory does not exists, make one.
		if (!mWorkingPath.exists()) {
			if (!mWorkingPath.mkdirs()) {

			}
		}
		for (int i = 0; i < files.length; i++) {
			try {
				String fileName = files[i];
				// we make sure file name not contains '.' to be a folder.
				if (!fileName.contains(".")) {
					if (0 == assetDir.length()) {
						CopyAssets(fileName, dir + fileName + "/");
					} else {
						CopyAssets(assetDir + "/" + fileName, dir + fileName
								+ "/");
					}
					continue;
				}
				File outFile = new File(mWorkingPath, fileName);
				if (outFile.exists())
					outFile.delete();
				InputStream in = null;
				if (0 != assetDir.length())
					in = getAssets().open(assetDir + "/" + fileName);
				else
					in = getAssets().open(fileName);
				OutputStream out = new FileOutputStream(outFile);

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void isGpsOpen() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean GPS_status = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);// 获得手机是不是设置了GPS开启状态true：gps开启，false：GPS未开启
		String status = "";
		if (GPS_status) {
			status += "GPS已开启";
			Toast.makeText(PreviewActivity.this, status, Toast.LENGTH_LONG)
					.show();
		} else {
			// return false;
			AlertDialog.Builder builder = new AlertDialog.Builder(
					PreviewActivity.this);
			builder.setTitle("需要开启GPS,是否开启");
			builder.setNegativeButton("否",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							return;
						}
					});
			builder.setPositiveButton("是",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface,
								int i) {
							// 按钮事件
							initGPS();
						}
					});
			builder.show();
			// 弹出对话框
		}
		// 弹出Toast
	}

	private void initGPS() {
		LocationManager lm = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		if (!lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			// 转到手机设置界面，用户设置GPS
			Intent intent1 = new Intent(
					Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(intent1, 0); // 设置完成后返回到原来的界面
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this)
					.setMessage("确定退出多媒体行车记录仪？")
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							})
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									finish();
								}
							}).show();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	class RecorderCheckboxListener implements
			CompoundButton.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
			}
		}
	}

	class LightCheckboxListener implements
			CompoundButton.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				lightCheckBox_1.setVisibility(View.VISIBLE);
				lightCheckBox_2.setVisibility(View.VISIBLE);
			}
			if (!isChecked) {
				lightCheckBox_1.setChecked(false);
				lightCheckBox_2.setChecked(false);
				lightCheckBox_1.setVisibility(View.GONE);
				lightCheckBox_2.setVisibility(View.GONE);
			}
		}
	}

	class OverspeedCheckboxListener implements
			CompoundButton.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				isGpsOpen();
				overspeedCheckBox_1.setVisibility(View.VISIBLE);
				overspeedCheckBox_2.setVisibility(View.VISIBLE);
			}
			if (!isChecked) {
				overspeedCheckBox_1.setChecked(false);
				overspeedCheckBox_2.setChecked(false);
				overspeedCheckBox_1.setVisibility(View.GONE);
				overspeedCheckBox_2.setVisibility(View.GONE);
			}
		}
	}

	class ConfirmBtnListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (recorderCheckBox.isChecked()) {
				range1 = 1;
				str1 = "影像记录、";
			} else if (!recorderCheckBox.isChecked()) {
				range1 = 0;
				str1 = "";
			}
			if (lightCheckBox.isChecked()) {
				range2 = 1;
				str2 = "开灯提示、";
			} else if (!lightCheckBox.isChecked()) {
				range2 = 0;
				str2 = "";
			}
			if (lightCheckBox_1.isChecked()) {
				range2_1 = 1;
			} else if (!lightCheckBox_1.isChecked()) {
				range2_1 = 0;
			}
			if (lightCheckBox_2.isChecked()) {
				range2_2 = 1;
			} else if (!lightCheckBox_2.isChecked()) {
				range2_2 = 0;
			}
			if (overspeedCheckBox.isChecked()) {
				range3 = 1;
				str3 = "超速提醒";
			} else if (!overspeedCheckBox.isChecked()) {
				range3 = 0;
				str3 = "";
			}
			if (overspeedCheckBox_1.isChecked()) {
				range3_1 = 1;
			} else if (!overspeedCheckBox_1.isChecked()) {
				range3_1 = 0;
			}
			if (overspeedCheckBox_2.isChecked()) {
				range3_2 = 1;
			} else if (!overspeedCheckBox_2.isChecked()) {
				range3_2 = 0;
			}
			Toast.makeText(getApplicationContext(),
					"您所选择的功能为:" + str1 + str2 + str3, Toast.LENGTH_LONG).show();
			Intent intent = new Intent();
			intent.putExtra(ActivityConf.intent_range1, range1);
			intent.putExtra(ActivityConf.intent_range2, range2);
			intent.putExtra(ActivityConf.intent_range3, range3);
			intent.putExtra(ActivityConf.intent_range2_1, range2_1);
			intent.putExtra(ActivityConf.intent_range2_2, range2_2);
			intent.putExtra(ActivityConf.intent_range3_1, range3_1);
			intent.putExtra(ActivityConf.intent_range3_2, range3_2);
			intent.setClass(PreviewActivity.this, RecorderActivity.class);
			startActivity(intent);
		}

	}

	class ConfirmBtnTouchListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				confirmView
						.setBackgroundResource(R.drawable.start_record_btn_on);
				break;
			case MotionEvent.ACTION_UP:
				confirmView
						.setBackgroundResource(R.drawable.start_record_btn_off);
				break;
			default:
				break;
			}
			return false;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			initialTreeView();
			initEvent();
			initDB();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		try {
			initialTreeView();
			initEvent();
			initDB();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

}
