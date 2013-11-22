package com.example.sociallogin.beans;

import java.io.Serializable;

public class LoggedInUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3057537320209439066L;
	private String firstName, lastName, email, password, bDate, mobileNumber;
	private String gender, socMedName;
	private String medType, medId, medName, medLink, medLocation, medHomeTown,
			medArray;

	public LoggedInUser() {
		firstName = "";
		lastName = "";
		email = "";
		password = "";
		bDate = "";
		mobileNumber = "";
		gender = "";
		socMedName = "";
		medType = "";
		medId = "";
		medName = "";
		medLink = "";
		medLocation = "";
		medHomeTown = "";
		medArray = "";
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getbDate() {
		return bDate;
	}

	public void setbDate(String bDate) {
		this.bDate = bDate;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getSocMedName() {
		return socMedName;
	}

	public void setSocMedName(String socMedName) {
		this.socMedName = socMedName;
	}

	public String getMedType() {
		return medType;
	}

	public void setMedType(String medType) {
		this.medType = medType;
	}

	public String getMedId() {
		return medId;
	}

	public void setMedId(String medId) {
		this.medId = medId;
	}

	public String getMedName() {
		return medName;
	}

	public void setMedName(String medName) {
		this.medName = medName;
	}

	public String getMedLink() {
		return medLink;
	}

	public void setMedLink(String medLink) {
		this.medLink = medLink;
	}

	public String getMedLocation() {
		return medLocation;
	}

	public void setMedLocation(String medLocation) {
		this.medLocation = medLocation;
	}

	public String getMedHomeTown() {
		return medHomeTown;
	}

	public void setMedHomeTown(String medHomeTown) {
		this.medHomeTown = medHomeTown;
	}

	public String getMedArray() {
		return medArray;
	}

	public void setMedArray(String medArray) {
		this.medArray = medArray;
	}

}
