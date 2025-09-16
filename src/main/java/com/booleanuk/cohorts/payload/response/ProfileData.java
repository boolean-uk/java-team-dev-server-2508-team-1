package com.booleanuk.cohorts.payload.response;

import com.booleanuk.cohorts.models.Profile;
import lombok.Getter;

@Getter
public class ProfileData extends Data<Profile> {
    protected Profile profile;

    @Override
    public void set(Profile profile) {
        this.profile = profile;
    }
}
