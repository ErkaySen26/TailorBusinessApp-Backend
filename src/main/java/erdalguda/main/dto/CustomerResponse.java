// CustomerResponse.java
package erdalguda.main.dto;

import erdalguda.main.model.Customer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
    private String email;
    private Double height;
    private Double weight;


    public CustomerResponse(Customer c) {
        this.id = c.getId();
        this.firstName = c.getFirstName();
        this.lastName = c.getLastName();
        this.address = c.getAddress();
        this.phone = c.getPhone();
        this.email = c.getEmail();
        this.height = c.getHeight();
        this.weight = c.getWeight();

    }
}