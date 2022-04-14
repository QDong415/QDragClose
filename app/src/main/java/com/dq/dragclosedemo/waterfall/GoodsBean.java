package com.dq.dragclosedemo.waterfall;

import java.io.Serializable;
import java.util.List;

public class GoodsBean implements Serializable{

	private String title;
	private List<PhotoBean> pictures;

	//第一张图的宽高
	private int photoOriginalWidth;
	private int photoOriginalHeight;

	public List<PhotoBean> getPictures() {
		return pictures;
	}

	public void setPictures(List<PhotoBean> pictures) {
		this.pictures = pictures;
		if (pictures != null && pictures.size() > 0) {
			PhotoBean firstPhotoBean = pictures.get(0);
			if (firstPhotoBean.getWidth() != 0 && firstPhotoBean.getHeight() != 0) {
				photoOriginalWidth = firstPhotoBean.getWidth();
				photoOriginalHeight = firstPhotoBean.getHeight();
				return;
			}
		}
	}

	public int getPhotoOriginalHeight() {
		return photoOriginalHeight;
	}

	public int getPhotoOriginalWidth() {
		return photoOriginalWidth;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}

