package com.favor.widget;

public class Contact {

	private final String name;
	private final String[] addresses;

	private boolean isSelected;

	public Contact(String name, String[] addresses) {
		this.name = name;
		this.addresses = addresses;
	}

	public String getName() {
		return name;
	}

	public String displayAddress() {
		StringBuilder result = new StringBuilder(22);
		for (short i = 0; i < addresses.length; i++){
			if (result.length() + addresses[i].length()+2<=22) result.append(", ").append(addresses[i]);
			else result.append("...");
		}
		return result.toString();
	}
	
	public String[] addresses(){
		return addresses;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

}
