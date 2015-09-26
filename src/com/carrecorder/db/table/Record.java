package com.carrecorder.db.table;

import java.sql.Date;
import com.db.annotations.Id;
import com.db.annotations.Table;

@Table(name = "Record")
public class Record {
	@Id(autoIncrement = true)
	private int id;
	public Record(int melige, String date,String videoName) {
		super();
		this.melige = melige;
		this.date = date;
		this.videoName = videoName;
	}
	private int melige;
	private String date;
	private String videoName;
	public String getVideoName() {
		return videoName;
	}
	public void setVideoName(String videoName) {
		this.videoName = videoName;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getMelige() {
		return melige;
	}
	public void setMelige(int melige) {
		this.melige = melige;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
}
