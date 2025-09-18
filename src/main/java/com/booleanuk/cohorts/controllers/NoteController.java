package com.booleanuk.cohorts.controllers;

import com.booleanuk.cohorts.models.ERole;
import com.booleanuk.cohorts.models.Note;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.payload.request.NoteReqeuest;
import com.booleanuk.cohorts.payload.response.ErrorResponse;
import com.booleanuk.cohorts.repository.NoteRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("notes")
public class NoteController {

    @Autowired private NoteRepository noteRepository;
    @Autowired private UserRepository userRepository;


    @GetMapping
    public ResponseEntity<?> getALlNotes(){

        List<Note> notes = noteRepository.findAll();

        if(notes.isEmpty()){
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.set("No notes created");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

        return new ResponseEntity<>(notes,HttpStatus.OK);
    }

    @GetMapping("id")
    public ResponseEntity<?> getNoteById(@PathVariable int id){

        Note note = noteRepository.findById(id).orElseThrow();

        if(note.getUser() == null || note.getTitle().isEmpty() || note.getDescription().isEmpty()){
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.set("Invalid note data");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(note,HttpStatus.OK);
    }



    @PostMapping
    private ResponseEntity<?> createNote(@RequestBody NoteReqeuest noteReqeuest){

        User user = userRepository.findById(noteReqeuest.getUser_id()).orElse(null);

        if(user == null ){
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.set("No user with that ID exists");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        if(user.getProfile().getRole().getName().equals(ERole.ROLE_TEACHER)){
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.set("Cannot add note to a teacher");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        if(noteReqeuest.getTitle().isEmpty() || noteReqeuest.getDescription().isEmpty()){
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.set("Note is missing description or title");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }


        Note note = new Note();
        note.setTitle(noteReqeuest.getTitle());
        note.setDescription(noteReqeuest.getDescription());
        note.setUser(user);
        note.setCreated(LocalDate.now());
        return new ResponseEntity<>(noteRepository.save(note),HttpStatus.CREATED);

    }
}
