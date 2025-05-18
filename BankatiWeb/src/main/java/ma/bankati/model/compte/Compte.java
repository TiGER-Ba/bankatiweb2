package ma.bankati.model.compte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Compte {
    private Long id;
    private Long userId;
    private Double solde;
    private String devise;
}