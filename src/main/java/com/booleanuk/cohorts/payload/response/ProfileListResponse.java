package com.booleanuk.cohorts.payload.response;

import com.booleanuk.cohorts.models.Profile;
import lombok.Getter;

import java.util.List;

@Getter
public class ProfileListResponse extends Response {
    public void set(List<Profile> profiles) {
        Data<List<Profile>> data = new ProfileListData();
        data.set(profiles);
        super.set(data);
    }
}
