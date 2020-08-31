package com.km0_compro;

import androidx.annotation.NonNull;

public class Quantity {
    private String quantity;

    public Quantity(String quantity) {
        this.quantity = quantity;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    @NonNull
    @Override
    public String toString() {
        return quantity;
    }

}
