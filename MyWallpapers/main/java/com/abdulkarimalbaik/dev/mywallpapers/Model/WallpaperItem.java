package com.abdulkarimalbaik.dev.mywallpapers.Model;

public class WallpaperItem {

    private String categoryId;
    private String imageUrl;
    private long viewCount;

    public WallpaperItem() {
    }

    public WallpaperItem(String categoryId, String imageUrl) {
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }
}
