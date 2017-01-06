package com.example.lost;

public class Item {

  private final String title;
  private final String detail;

  public Item(String title, String detail) {
    this.title = title;
    this.detail = detail;
  }

  public String getTitle() {
    return title;
  }

  public String getDetail() {
    return detail;
  }
}
