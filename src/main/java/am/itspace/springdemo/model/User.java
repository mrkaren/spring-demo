package am.itspace.springdemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue
    private int id;
    private String name;
    private String surname;
    private String username;
    private String password;
    @Enumerated(value = EnumType.STRING)
    private Role role;
    private String profilePic;

}
