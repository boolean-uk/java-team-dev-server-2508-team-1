package com.booleanuk.cohorts.payload.response;

import com.booleanuk.cohorts.models.Profile;
import lombok.Getter;

@Getter
public class ProfileResponse extends Response {
    public void set(Profile profile) {
        Data<Profile> data = new ProfileData();
        data.set(profile);
        super.set(data);
    }
}
