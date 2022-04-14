package com.dq.dragclosedemo.waterfall;

import java.io.Serializable;

public class PhotoBean implements Serializable{

	private String url; //图片url
	private int width;
	private int height;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}
