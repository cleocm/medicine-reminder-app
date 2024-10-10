package com.example.medicinereminderapp;
public class Medicine {
    String medicineName, medicineDosage, medicineDescription, medicineTime, medicineImage;

    public Medicine(String medName, String medDose, String medDesc, String intakeTime, String medImgUrl) {
        this.medicineName = medName;
        this.medicineDosage = medDose;
        this.medicineDescription = medDesc;
        this.medicineTime = intakeTime;
        this.medicineImage = medImgUrl;
    }

    public Medicine() {
    }

    public String getMedicineDescription() {
        return medicineDescription;
    }

    public void setMedicineDescription(String medicineDescription) {
        this.medicineDescription = medicineDescription;
    }

    public String getMedicineImage() {
        return medicineImage;
    }

    public void setMedicineImage(String medicineImage) {
        this.medicineImage = medicineImage;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineDosage() {
        return medicineDosage;
    }

    public void setMedicineDosage(String medicineDosage) {
        this.medicineDosage = medicineDosage;
    }

    public String getMedicineTime() {
        return medicineTime;
    }

    public void setMedicineTime(String medicineTime) {
        this.medicineTime = medicineTime;
    }
}

