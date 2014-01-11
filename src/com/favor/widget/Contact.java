package com.favor.widget;

import java.util.Arrays;


public class Contact {
	
	private final String name;
	private final String[] addresses;
	private final String id;

	private boolean isSelected;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(addresses);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contact other = (Contact) obj;
		if (!Arrays.equals(addresses, other.addresses))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

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
		return addresses.clone();
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
