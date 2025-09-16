package com.booleanuk.cohorts.payload.response;

import com.booleanuk.cohorts.models.Profile;
import lombok.Getter;

import java.util.List;

@Getter
public class ProfileListData extends Data<List<Profile>> {
    protected List<Profile> profiles;

    @Override
    public void set(List<Profile> profiles) {
        this.profiles = profiles;
    }
}
