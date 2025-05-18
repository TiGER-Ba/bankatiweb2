package ma.bankati.model.users;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data   @NoArgsConstructor @Builder
public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private ERole role;
    private LocalDate creationDate = LocalDate.now();

    // Constructeur personnalisé pour le Builder
    @Builder
    public User(Long id, String firstName, String lastName, String username, String password, ERole role, LocalDate creationDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.creationDate = creationDate != null ? creationDate : LocalDate.now();
    }

    public String toString(){
        return "[" + role.toString()+ "] " + firstName + " " + lastName;
    }

    public String getUserInfos(){
        return "[ login : " + username + ", password : " + password + "]";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ERole getRole() {
        return role;
    }

    public void setRole(ERole role) {
        this.role = role;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }
}