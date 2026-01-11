package models;

import java.util.Date;

public class DiscountCode {
    private int discountId;
    private String code;
    private int discountPercentage;
    private Date expiryDate;
    private int usageLimit;
    private int timesUsed;
    private boolean isActive;

    public DiscountCode() {}

    public DiscountCode(String code, int discountPercentage, Date expiryDate, int usageLimit) {
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.expiryDate = expiryDate;
        this.usageLimit = usageLimit;
        this.timesUsed = 0;
        this.isActive = true;
    }

    // Getters and Setters
    public int getDiscountId() { return discountId; }
    public void setDiscountId(int discountId) { this.discountId = discountId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(int discountPercentage) { this.discountPercentage = discountPercentage; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }

    public int getTimesUsed() { return timesUsed; }
    public void setTimesUsed(int timesUsed) { this.timesUsed = timesUsed; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
