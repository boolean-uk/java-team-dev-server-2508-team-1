package com.booleanuk.cohorts.payload.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NoteReqeuest {

    private String title;
    private String description;
    private int user_id;
}
