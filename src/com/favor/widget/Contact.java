package com.favor.widget;

public class Contact {

	private final String name;
	private final String address;

	private boolean isSelected;

	public Contact(String name, String address) {
		this.name = name;
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

}
