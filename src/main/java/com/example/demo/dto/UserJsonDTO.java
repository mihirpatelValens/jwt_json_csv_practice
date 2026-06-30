package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserJsonDTO {
  @NotBlank
  private String username;

  @Min(1)
  private int age;

  @Email
  private String email;

  @NotBlank
  private String department;

  @NotBlank
  private String organization;

  @NotEmpty
  private List<String> projects;

  @Valid
  private AddressDTO address;
}
