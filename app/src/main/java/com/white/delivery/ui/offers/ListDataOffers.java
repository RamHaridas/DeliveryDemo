package com.white.delivery.ui.offers;

import android.media.Image;

public class ListDataOffers {
    private Image i;
    private String Heading,subHeading;
    public ListDataOffers(Image i, String heading, String subHeading) {
        this.i = i;
        Heading = heading;
        this.subHeading = subHeading;
    }
    public Image getI() {
        return i;
    }
    public void setI(Image i) {
        this.i = i;
    }
    public String getHeading() {
        return Heading;
    }
    public void setHeading(String heading) {
        Heading = heading;
    }
    public String getSubHeading() {
        return subHeading;
    }
    public void setSubHeading(String subHeading) {
        this.subHeading = subHeading;
    }
}
