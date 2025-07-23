package com.github.sportbot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProgramId implements Serializable {
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "exercise_type_id")
    private Integer exerciseTypeId;
}
