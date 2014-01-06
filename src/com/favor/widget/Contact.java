package com.favor.widget;


public class Contact {

	private final String name;
	private final String[] addresses;
	private final String id;

	private boolean isSelected;

	public Contact(String name, String id, String[] addresses) {
		this.name = name;
		this.id = id;
		this.addresses = addresses;
	}
	

	public String getName() {
		return name;
	}
	
	public String id(){return id;}

	public String displayAddress() {
		StringBuilder result = new StringBuilder();
		for (short i = 0; i < addresses.length; i++){
			if (i!=0) result.append(", ");
			result.append(addresses[i]);
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
	
	/**
	 * Strictly a debug method
	 */
	public String toString()
	{
		String log = "ID: " +id+ " Name: "+name+" Address(es): ";
		for (int i = 0; i < addresses.length; i++){
			log+=addresses[i]+", ";
		}
		log = log.substring(0, log.length()-2); //Remove trailing ", "
		return log;
	}

}
