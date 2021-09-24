/*
 * Copyright (C) 2016 Glucosio Foundation
 *
 * This file is part of Glucosio.
 *
 * Glucosio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Glucosio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Glucosio.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.deabee.android.db;

import java.util.Date;

public class InsulinReading {
    private long id;
    private double reading;
    private Date created;
    private Integer insulinType;
    private String mealtimeName;

    public InsulinReading(double reading, Date created, Integer insulinType, String mealtimeName) {
        this.reading = reading;
        this.created = created;
        this.insulinType = insulinType;
        this.mealtimeName = mealtimeName;
    }

    public double getReading() {
        return reading;
    }

    public void setReading(double reading) {
        this.reading = reading;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getInsulinType() {
        return insulinType;
    }

    public void setInsulinType(Integer insulinType) {
        this.insulinType = insulinType;
    }

    public String getMealtimeName() {
        return mealtimeName;
    }

    public void setMealtimeName(String mealtimeName) {
        this.mealtimeName = mealtimeName;
    }
}
